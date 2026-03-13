package com.ties.validation.config;

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
 * Configures how the validation service publishes
 * successfully validated capture blocks forward to
 * the storage-service via Kafka.
 *
 * This is the first service in the TIES pipeline that
 * both consumes AND produces Kafka messages, acting as
 * a pass-through validator between ingestion and storage.
 *
 * Design decisions:
 * - KafkaTemplate is the Spring abstraction for sending
 *   messages — cleaner than using raw Kafka producer API
 * - acks=all ensures the message is confirmed by all
 *   Kafka broker replicas before the send is considered
 *   successful — critical for data integrity
 * - Retries set to 3 to handle transient broker failures
 *   without losing validated blocks
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
     * instances. Producers are thread-safe and reused
     * across all sends — not created per message.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {

        logger.debug("Configuring Kafka producer factory");
        logger.debug("Bootstrap servers: {}", bootstrapServers);

        Map<String, Object> config = new HashMap<>();

        // Where is Kafka running?
        config.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers
        );

        // How to serialize the message key
        // Keys are device identifiers — simple strings
        config.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );

        // How to serialize the message value
        // Values are JSON strings of valid