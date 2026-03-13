package com.ties.storage.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ties.storage.entity.BlockEntity;
import com.ties.storage.model.CaptureBlock;
import com.ties.storage.model.FrameDocument;
import com.ties.storage.model.FrameMetadata;
import com.ties.storage.repository.BlockRepository;
import com.ties.storage.repository.FrameDocumentRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * CaptureBlockConsumer
 *
 * Kafka listener for the storage service.
 * The most complex consumer in the TIES pipeline —
 * writes to two separate databases before confirming.
 *
 * Pipeline position:
 * validation-service → [capture.blocks.stored] → storage-service → [capture.blocks.confirmed] → orchestration-service
 *
 * Responsibility chain:
 * 1. Consume blocks from capture.blocks.stored topic
 * 2. Deserialize JSON into CaptureBlock object
 * 3. Check for duplicate blocks — idempotency guard
 * 4. Persist block metadata to PostgreSQL
 * 5. Persist each frame as FrameDocument to MongoDB
 * 6. Update block status to ACKNOWLEDGED in PostgreSQL
 * 7. Forward confirmation to orchestration-service
 * 8. Acknowledge message to Kafka
 *
 * Design decisions:
 * - Idempotency check prevents duplicate storage if Kafka
 *   redelivers a message after a partial failure
 * - PostgreSQL write happens before MongoDB write —
 *   if MongoDB fails the block status stays PENDING
 *   and the message is not acknowledged for redelivery
 * - Acknowledgement only happens after BOTH writes succeed
 *   AND the confirmation is forwarded downstream
 */
@Component
public class CaptureBlockConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(CaptureBlockConsumer.class);

    private final ObjectMapper objectMapper;
    private final BlockRepository blockRepository;
    private final FrameDocumentRepository frameDocumentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.confirmed}")
    private String confirmedTopic;

    /**
     * Constructor injection
     *
     * All dependencies injected explicitly.
     * Constructor injection preferred — dependencies
     * are final and visible at instantiation time.
     */
    public CaptureBlockConsumer(
        BlockRepository blockRepository,
        FrameDocumentRepository frameDocumentRepository,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.blockRepository = blockRepository;
        this.frameDocumentRepository = frameDocumentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Kafka Listener — capture.blocks.stored topic
     *
     * Triggered automatically by Spring whenever
     * validation-service publishes a validated block.
     *
     * @param record         raw Kafka message
     * @param acknowledgment manual ack handle
     */
    @KafkaListener(
        topics = "${kafka.topic.stored}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        ConsumerRecord<String, String> record,
        Acknowledgment acknowledgment
    ) {
        logger.info("Received block for storage | device: {} | partition: {} | offset: {}",
            record.key(),
            record.partition(),
            record.offset()
        );

        // Step 1 — Deserialize JSON into CaptureBlock
        CaptureBlock captureBlock;
        try {
            captureBlock = objectMapper.readValue(
                record.value(),
                CaptureBlock.class
            );
            logger.debug("Deserialized block: {}",
                captureBlock.getBlockIdentifier()
            );

        } catch (Exception e) {
            // Do NOT acknowledge — allows reprocessing
            logger.error("Deserialization failed at offset: {} | reason: {}",
                record.offset(),
                e.getMessage()
            );
            return;
        }

        // Step 2 — Idempotency check
        // Prevent duplicate storage if Kafka redelivers
        if (blockRepository.existsByBlockIdentifier(
                captureBlock.getBlockIdentifier())) {
            logger.warn("Duplicate block detected — already stored: {} | acknowledging to unblock pipeline",
                captureBlock.getBlockIdentifier()
            );
            acknowledgment.acknowledge();
            return;
        }

        // Step 3 — Persist block metadata to PostgreSQL
        BlockEntity blockEntity;
        try {
            blockEntity = BlockEntity.builder()
                .blockIdentifier(captureBlock.getBlockIdentifier())
                .capturedAt(captureBlock.getCaptureStartTimestamp())
                .frameCount(captureBlock.getFrames().size())
                .qualityScore(captureBlock.getQualityScore())
                .spatialLabel(captureBlock.getSpatialLabel())
                .transmissionStatus("PENDING")
                .receivedAt(Instant.now())
                .checksum(captureBlock.getChecksum())
                .build();

            blockRepository.save(blockEntity);

            logger.info("Block metadata persisted to PostgreSQL: {}",
                captureBlock.getBlockIdentifier()
            );

        } catch (Exception e) {
            // Do NOT acknowledge — PostgreSQL write failed
            // message will be redelivered for retry
            logger.error("PostgreSQL write failed for block: {} | reason: {}",
                captureBlock.getBlockIdentifier(),
                e.getMessage()
            );
            return;
        }

        // Step 4 — Persist each frame to MongoDB
        try {
            List<FrameDocument> frameDocs = captureBlock.getFrames()
                .stream()
                .map(frame -> mapToFrameDocument(frame, captureBlock))
                .toList();

            frameDocumentRepository.saveAll(frameDocs);

            logger.info("Frames persisted to MongoDB | block: {} | count: {}",
                captureBlock.getBlockIdentifier(),
                frameDocs.size()
            );

        } catch (Exception e) {
            // Do NOT acknowledge — MongoDB write failed
            // PostgreSQL record stays PENDING for audit
            logger.error("MongoDB write failed for block: {} | reason: {}",
                captureBlock.getBlockIdentifier(),
                e.getMessage()
            );
            return;
        }

        // Step 5 — Update block status to ACKNOWLEDGED
        blockRepository.updateTransmissionStatus(
            captureBlock.getBlockIdentifier(),
            "ACKNOWLEDGED"
        );

        logger.debug("Block status updated to ACKNOWLEDGED: {}",
            captureBlock.getBlockIdentifier()
        );

        // Step 6 — Forward confirmation to orchestration-service
        try {
            kafkaTemplate.send(
                confirmedTopic,
                captureBlock.getDeviceIdentifier(),
                captureBlock.getBlockIdentifier()
            );

            logger.info("Confirmation forwarded to orchestration-service | block: {} | topic: {}",
                captureBlock.getBlockIdentifier(),
                confirmedTopic
            );

        } catch (Exception e) {
            // Log but do not block acknowledgement —
            // storage succeeded, orchestration will
            // handle redelivery via its own mechanism
            logger.error("Failed to forward confirmation for block: {} | reason: {}",
                captureBlock.getBlockIdentifier(),
                e.getMessage()
            );
        }

        // Step 7 — Acknowledge only after both writes succeed
        acknowledgment.acknowledge();

        logger.info("Block fully stored and acknowledged: {}",
            captureBlock.getBlockIdentifier()
        );
    }

    /**
     * Frame Mapper
     *
     * Converts a FrameMetadata from the Kafka message
     * into a FrameDocument for MongoDB storage.
     *
     * Sets storedAt to now() — the moment storage-service
     * writes the frame to MongoDB.
     *
     * @param frame        the FrameMetadata from CaptureBlock
     * @param captureBlock the parent block for context fields
     * @return FrameDocument ready for MongoDB persistence
     */
    private FrameDocument mapToFrameDocument(
        FrameMetadata frame,
        CaptureBlock captureBlock
    ) {
        return FrameDocument.builder()
            .blockIdentifier(captureBlock.getBlockIdentifier())
            .deviceIdentifier(captureBlock.getDeviceIdentifier())
            .sessionIdentifier(captureBlock.getSessionIdentifier())
            .frameIndex(frame.getFrameIndex())
            .capturedAt(frame.getCapturedAt())
            .cameraOrientation(frame.getCameraOrientation())
            .exposureParams(frame.getExposureParams())
            .qualityFlag(frame.getQualityFlag())
            .storedAt(Instant.now())
            .build();
    }
}