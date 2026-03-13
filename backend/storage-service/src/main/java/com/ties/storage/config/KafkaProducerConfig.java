package com.ties.storage.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration
 *
 * Configures how the storage service publishes
 * storage confirmation events forward to
 * orchestration-service via Kafka.
 *
 * Pipeline position:
 * validation-service → [capture.blocks.stored] → storage-service → [capture.blocks.confirmed] → orchestration-service
 *
 * Design decisions:
 * - acks=all ensures confirmation is only sent after
 *   Kafka broker replicas all received the message
 * - Idempotence enabled prevents duplicate confirmations
 *   during retries — orchestration-service must never
 *   process the same block twice
 * - Retries set to 3 for transient broker failures
 */
@Configuration
public class KafkaProducerConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * ProducerFactory
     *
     * Creates the factory that produces Kafka producer
     * instances for forwarding storage confirmations
     * to orchestration-service.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {

        logger.debug("Configuring Kafka producer factory");
        logger.debug("Bootstrap servers: {}", bootstrapServers);

        Map<String, Object> config = new HashMap<>();

        config.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers
        );

        config.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );

        config.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );

        // Wait for all replicas to confirm receipt
        config.put(
            ProducerConfig.ACKS_CONFIG,
            "all"
        );

        // Retry up to 3 times on transient failures
        config.put(
            ProducerConfig.RETRIES_CONFIG,
            3
        );

        // Prevent duplicate confirmations during retries
        config.put(
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
            true
        );

        logger.info("Kafka producer factory configured");
        logger.info("Acks: all | Retries: 3 | Idempotence: enabled");

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate
     *
     * Spring abstraction for sending Kafka messages.
     * Injected into CaptureBlockConsumer to forward
     * storage confirmations to orchestration-service.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        logger.info("KafkaTemplate initialized");
        return new KafkaTemplate<>(producerFactory());
    }
}