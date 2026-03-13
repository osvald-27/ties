package com.ties.validation.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ties.validation.model.CaptureBlock;
import com.ties.validation.validator.CaptureBlockValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * CaptureBlockConsumer
 *
 * Kafka listener for the validation service.
 * Sits directly downstream of ingestion-service in the
 * TIES pipeline.
 *
 * Pipeline position:
 * ingestion-service → [capture.blocks.validated] → validation-service → [capture.blocks.stored] → storage-service
 *
 * Responsibility chain:
 * 1. Consume blocks from capture.blocks.validated topic
 * 2. Deserialize JSON into CaptureBlock object
 * 3. Run deep validation via CaptureBlockValidator
 * 4. Publish passing blocks to capture.blocks.stored topic
 * 5. Acknowledge message to Kafka after processing
 *
 * Design decisions:
 * - CaptureBlockValidator injected via constructor —
 *   explicit dependency, easy to test and mock
 * - KafkaTemplate injected for producing forward —
 *   same pattern used across all producer services
 * - Failed deserialization does not acknowledge —
 *   same pattern as ingestion-service for consistency
 * - Validation failures acknowledge and discard —
 *   retrying a semantically invalid block is pointless
 */
@Component
public class CaptureBlockConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(CaptureBlockConsumer.class);

    private final ObjectMapper objectMapper;
    private final CaptureBlockValidator validator;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.stored}")
    private String storedTopic;

    /**
     * Constructor injection
     *
     * Spring automatically injects CaptureBlockValidator
     * and KafkaTemplate beans defined in config classes.
     * Constructor injection is preferred over @Autowired
     * because dependencies are explicit and final.
     */
    public CaptureBlockConsumer(
        CaptureBlockValidator validator,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.validator = validator;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Kafka Listener — capture.blocks.validated topic
     *
     * Triggered automatically by Spring whenever ingestion-service
     * publishes a validated capture block.
     *
     * @param record         raw Kafka message with key and value
     * @param acknowledgment manual ack handle
     */
    @KafkaListener(
        topics = "${kafka.topic.validated}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        ConsumerRecord<String, String> record,
        Acknowledgment acknowledgment
    ) {
        logger.info("Received block for validation | device: {} | partition: {} | offset: {}",
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

        // Step 2 — Run deep validation
        if (!validator.validate(captureBlock)) {
            logger.warn("Block failed deep validation: {} | acknowledging to unblock pipeline",
                captureBlock.getBlockIdentifier()
            );
            // Acknowledge — semantically invalid blocks
            // will never pass regardless of retries
            acknowledgment.acknowledge();
            return;
        }

        // Step 3 — Serialize validated block back to JSON
        String validatedJson;
        try {
            validatedJson = objectMapper.writeValueAsString(captureBlock);

        } catch (Exception e) {
            logger.error("Serialization failed for block: {} | reason: {}",
                captureBlock.getBlockIdentifier(),
                e.getMessage()
            );
            // Do NOT acknowledge — serialization failure
            // is unexpected and worth retrying
            return;
        }

        // Step 4 — Forward to storage-service via Kafka
        kafkaTemplate.send(
            storedTopic,
            captureBlock.getDeviceIdentifier(),
            validatedJson
        );

        logger.info("Block forwarded to storage-service | block: {} | topic: {}",
            captureBlock.getBlockIdentifier(),
            storedTopic
        );

        // Step 5 — Acknowledge only after successful forward
        acknowledgment.acknowledge();
    }
}