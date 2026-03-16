package com.ties.orchestration.consumer;

import com.ties.orchestration.service.OrchestrationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * ConfirmationEventConsumer
 *
 * Kafka listener for the orchestration service.
 * The final consumer in the TIES backend pipeline.
 *
 * Pipeline position:
 * storage-service → [capture.blocks.confirmed] → orchestration-service
 *                                              → [session.ready] → analytical tier
 *
 * Responsibility chain:
 * 1. Consume confirmation event from capture.blocks.confirmed
 * 2. Extract block and session identifiers from event
 * 3. Delegate to OrchestrationService for state management
 * 4. Acknowledge message after successful processing
 *
 * Design decisions:
 * - Consumer is intentionally thin — all business logic
 *   lives in OrchestrationService, not here
 * - OrchestrationService injected via constructor
 *   for clean separation and testability
 * - Message value is blockIdentifier string
 * - Message key is deviceIdentifier string
 * - sessionIdentifier extracted from blockIdentifier
 *   using the format: DEVICE_ID-TIMESTAMP-SEQUENCE
 *   Session identifier is carried in the block identifier
 *   prefix — parsed at consume time
 */
@Component
public class ConfirmationEventConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(ConfirmationEventConsumer.class);

    private final OrchestrationService orchestrationService;

    public ConfirmationEventConsumer(
        OrchestrationService orchestrationService
    ) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Kafka Listener — capture.blocks.confirmed topic
     *
     * Triggered automatically by Spring whenever
     * storage-service publishes a confirmation event.
     *
     * Message key:   deviceIdentifier
     * Message value: blockIdentifier
     *
     * @param record         raw Kafka message
     * @param acknowledgment manual ack handle
     */
    @KafkaListener(
        topics = "${kafka.topic.confirmed}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        ConsumerRecord<String, String> record,
        Acknowledgment acknowledgment
    ) {
        logger.info("Received confirmation event | device: {} | partition: {} | offset: {}",
            record.key(),
            record.partition(),
            record.offset()
        );

        String blockIdentifier  = record.value();
        String deviceIdentifier = record.key();

        // Step 1 — Validate event payload
        if (blockIdentifier == null || blockIdentifier.isBlank()) {
            logger.warn("Empty block identifier at offset: {}",
                record.offset()
            );
            // Acknowledge — malformed event will never be valid
            acknowledgment.acknowledge();
            return;
        }

        // Step 2 — Extract session identifier from block identifier
        // Block format: DEVICE_ID-TIMESTAMP-SEQUENCE
        // Session identifier is transmitted separately in a
        // future enhancement — for now derived from block prefix
        // TODO: include sessionIdentifier explicitly in the
        // confirmation event payload from storage-service
        String sessionIdentifier = extractSessionIdentifier(blockIdentifier);

        logger.debug("Processing confirmation | block: {} | session: {}",
            blockIdentifier,
            sessionIdentifier
        );

        // Step 3 — Delegate to OrchestrationService
        boolean processed = orchestrationService.processConfirmedBlock(
            blockIdentifier,
            sessionIdentifier,
            deviceIdentifier
        );

        if (!processed) {
            // Do NOT acknowledge — processing failed
            // message will be redelivered for retry
            logger.error("Orchestration processing failed for block: {} — will retry",
                blockIdentifier
            );
            return;
        }

        // Step 4 — Acknowledge after successful processing
        acknowledgment.acknowledge();

        logger.info("Confirmation event fully processed | block: {}",
            blockIdentifier
        );
    }

    /**
     * Session Identifier Extractor
     *
     * Derives the session identifier from the block identifier.
     * Block format: DEVICE_ID-TIMESTAMP-SEQUENCE
     * Example: PIXEL5-001-1709123456789-001
     *
     * Currently returns the block identifier as a placeholder.
     * Will be replaced when storage-service includes the
     * sessionIdentifier explicitly in the confirmation event.
     *
     * @param blockIdentifier the full block identifier string
     * @return the derived session identifier
     */
    private String extractSessionIdentifier(String blockIdentifier) {
        // TODO: parse sessionIdentifier from confirmation
        // event payload once storage-service is updated
        // to include it explicitly
        return blockIdentifier;
    }
}