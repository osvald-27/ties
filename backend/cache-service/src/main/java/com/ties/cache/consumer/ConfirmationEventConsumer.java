package com.ties.cache.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ties.cache.model.BlockCacheEntry;
import com.ties.cache.service.CacheService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * ConfirmationEventConsumer
 *
 * Kafka listener for the cache service.
 * Consumes storage confirmation events from storage-service
 * and writes block summary data to Redis.
 *
 * Pipeline position:
 * storage-service → [capture.blocks.confirmed] → cache-service
 *                                              → orchestration-service
 *
 * Note: cache-service and orchestration-service both consume
 * from capture.blocks.confirmed independently via separate
 * consumer groups. Kafka delivers to both simultaneously.
 *
 * Responsibility chain:
 * 1. Consume confirmation event from capture.blocks.confirmed
 * 2. Parse block identifier from event payload
 * 3. Build BlockCacheEntry from event data
 * 4. Write entry to Redis with TTL
 * 5. Acknowledge message after successful Redis write
 *
 * Design decisions:
 * - Confirmation event payload is just the blockIdentifier
 *   string — storage-service sends minimal data
 * - CacheService handles all Redis operations and
 *   is injected via constructor for testability
 * - Failed Redis write does NOT acknowledge — message
 *   will be redelivered so cache stays consistent
 */
@Component
public class ConfirmationEventConsumer {

    private static final Logger logger =
        LoggerFactory.getLogger(ConfirmationEventConsumer.class);

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    public ConfirmationEventConsumer(CacheService cacheService) {
        this.cacheService = cacheService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Kafka Listener — capture.blocks.confirmed topic
     *
     * Triggered automatically by Spring whenever
     * storage-service publishes a confirmation event.
     *
     * The message key is the deviceIdentifier.
     * The message value is the blockIdentifier string.
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

        // Step 1 — Extract block identifier from message value
        String blockIdentifier = record.value();
        String deviceIdentifier = record.key();

        if (blockIdentifier == null || blockIdentifier.isBlank()) {
            logger.warn("Empty block identifier in confirmation event at offset: {}",
                record.offset()
            );
            // Acknowledge — malformed event will never be valid
            acknowledgment.acknowledge();
            return;
        }

        logger.debug("Processing confirmation for block: {}", blockIdentifier);

        // Step 2 — Build cache entry from event data
        // Note: confirmation event only carries blockIdentifier
        // and deviceIdentifier — other fields stored with
        // defaults until full block lookup is implemented
        BlockCacheEntry cacheEntry = cacheService.buildCacheEntry(
            blockIdentifier,
            deviceIdentifier,
            null,
            null,
            null,
            null,
            null
        );

        // Step 3 — Write to Redis
        boolean cached = cacheService.cacheBlock(cacheEntry);

        if (!cached) {
            // Do NOT acknowledge — Redis write failed
            // message will be redelivered for retry
            logger.error("Redis write failed for block: {} — will retry",
                blockIdentifier
            );
            return;
        }

        // Step 4 — Acknowledge after successful Redis write
        acknowledgment.acknowledge();

        logger.info("Confirmation event processed | block: {} cached successfully",
            blockIdentifier
        );
    }
}