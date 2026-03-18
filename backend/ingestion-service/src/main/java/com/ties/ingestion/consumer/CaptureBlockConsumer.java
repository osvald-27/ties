package com.ties.ingestion.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ties.ingestion.model.CaptureBlock;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * CaptureBlockConsumer
 *
 * The core Kafka listener of the ingestion service.
 * Sits at the entry point of the entire TIES backend pipeline.
 *
 * Responsibility chain:
 * 1. Receive raw JSON message from Kafka topic: capture.blocks
 * 2. Deserialize JSON into a CaptureBlock object
 * 3. Validate the block has the minimum required fields
 * 4. Validate quality score meets processing threshold
 * 5. Acknowledge the message to Kafka (manual ack mode)
 * 6. Forward valid blocks downstream (validation-service)
 *
 * Design decisions:
 * - ObjectMapper is instantiated once as a field, not inside
 *   the listener method, to avoid object creation on every
 *   message received
 * - JavaTimeModule registered so Jackson can deserialize
 *   Instant timestamps from the JSON payload
 * - Failed deserialization does NOT acknowledge the message
 *   so it can be reprocessed or sent to a dead letter topic
 * - Validation failures ARE acknowledged to prevent the same
 *   bad payload from blocking the pipeline indefinitely
 */
@Component
public class CaptureBlockConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(CaptureBlockConsumer.class);

    private static final double QUALITY_THRESHOLD = 0.5;

    private final ObjectMapper objectMapper;

    public CaptureBlockConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Kafka Listener — capture.blocks topic
     *
     * Triggered automatically by Spring whenever a new message
     * arrives on the capture.blocks Kafka topic.
     *
     * @param record         the raw Kafka message with key and value
     * @param acknowledgment manual ack handle — must be called to
     *                       confirm the message was processed
     */
    @KafkaListener(
        topics = "${kafka.topic.capture}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        ConsumerRecord<String, String> record,
        Acknowledgment acknowledgment
    ) {
        logger.info("Received capture block from device: {} | partition: {} | offset: {}",
            record.key(),
            record.partition(),
            record.offset()
        );

        // Step 1 — Deserialize raw JSON into CaptureBlock
        CaptureBlock captureBlock;
        try {
            captureBlock = objectMapper.readValue(
                record.value(),
                CaptureBlock.class
            );
            logger.debug("Deserialized block: {}", captureBlock.getBlockIdentifier());

        } catch (Exception e) {
            // Do NOT acknowledge — allows reprocessing or dead letter handling
            logger.error("Deserialization failed at offset: {} | reason: {}",
                record.offset(),
                e.getMessage()
            );
            return;
        }

        // Step 2 — Validate required fields are present
        if (!isStructureValid(captureBlock)) {
            logger.warn("Block failed structure validation: {} | acknowledging to unblock pipeline",
                captureBlock.getBlockIdentifier()
            );
            // Acknowledge — this payload will never be valid regardless of retries
            acknowledgment.acknowledge();
            return;
        }

        // Step 3 — Validate quality score meets threshold
        if (captureBlock.getQualityScore() < QUALITY_THRESHOLD) {
            logger.warn("Block rejected — quality too low: {} | score: {} | threshold: {}",
                captureBlock.getBlockIdentifier(),
                captureBlock.getQualityScore(),
                QUALITY_THRESHOLD
            );
            // Acknowledge and discard — low quality is not a retry scenario
            acknowledgment.acknowledge();
            return;
        }

        // Step 4 — Block is valid, acknowledge and forward downstream
        logger.info("Block accepted: {} | device: {} | frames: {} | quality: {}",
            captureBlock.getBlockIdentifier(),
            captureBlock.getDeviceIdentifier(),
            captureBlock.getFrames() != null ? captureBlock.getFrames().size() : 0,
            captureBlock.getQualityScore()
        );

        acknowledgment.acknowledge();

        // TODO: forward to validation-service via Kafka producer
        // This will be implemented when validation-service is built
    }

    /**
     * Structure Validator
     *
     * Checks that all minimum required fields are present
     * before the block enters the processing pipeline.
     *
     * @param block the deserialized CaptureBlock to validate
     * @return true if all required fields are present
     */
    private boolean isStructureValid(CaptureBlock block) {

        if (block.getBlockIdentifier() == null || block.getBlockIdentifier().isBlank()) {
            logger.warn("Validation failed — missing blockIdentifier");
            return false;
        }

        if (block.getDeviceIdentifier() == null || block.getDeviceIdentifier().isBlank()) {
            logger.warn("Validation failed — missing deviceIdentifier");
            return false;
        }

        if (block.getSessionIdentifier() == null || block.getSessionIdentifier().isBlank()) {
            logger.warn("Validation failed — missing sessionIdentifier");
            return false;
        }

        if (block.getFrames() == null || block.getFrames().isEmpty()) {
            logger.warn("Validation failed — no frames in block: {}",
                block.getBlockIdentifier()
            );
            return false;
        }

        if (block.getQualityScore() == null) {
            logger.warn("Validation failed — missing qualityScore: {}",
                block.getBlockIdentifier()
            );
            return false;
        }

        if (block.getChecksum() == null || block.getChecksum().isBlank()) {
            logger.warn("Validation failed — missing checksum: {}",
                block.getBlockIdentifier()
            );
            return false;
        }

        return true;
    }
}