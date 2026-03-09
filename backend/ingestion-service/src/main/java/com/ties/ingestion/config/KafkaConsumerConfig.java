package com.ties.ingestion.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 *
 * Configures how the ingestion service connects to
 * and consumes messages from Apache Kafka topics.
 *
 * Design decisions:
 * - Manual acknowledgement mode ensures messages are only
 * confirmed after successful processing
 * - Concurrency level of 3 allows parallel message processing
 * - String deserializer used because phone sends JSON strings
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(KafkaConsumerConfig.class);

    // These values are injected from application.properties
    // The ${} syntax reads the value at runtime
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * ConsumerFactory
     *
     * Creates the factory that produces Kafka consumer instances.
     * Each consumer instance connects to Kafka and reads messages
     * from assigned topic partitions.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        logger.debug("Configuring Kafka consumer factory");
        logger.debug("Bootstrap servers: {}", bootstrapServers);
        logger.debug("Consumer group: {}", groupId);

        // Build the configuration map
        Map<String, Object> config = new HashMap<>();

        // Where is Kafka running?
        config.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers
        );

        // Which consumer group does this service belong to?
        // All instances of ingestion-service share this group
        // Kafka distributes messages between them automatically
        config.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            groupId
        );

        // How to deserialize the message key
        // Keys are simple strings (device identifiers)
        config.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );

        // How to deserialize the message value
        // Values are JSON strings containing capture block data
        config.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );

        // Where to start reading when this service
        // connects to a topic for the first time
        // earliest = read everything from the beginning
        config.put(
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            "earliest"
        );

        // Disable automatic offset commit
        // We commit manually ONLY after successful processing
        // This prevents data loss if processing fails
        config.put(
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
            false
        );

        // Maximum messages to fetch per poll cycle
        config.put(
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
            10
        );

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * KafkaListenerContainerFactory
     *
     * Creates the container that manages our @KafkaListener
     * annotated methods. This factory wraps the ConsumerFactory
     * and adds Spring-specific features like error handling
     * and manual acknowledgement support.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        // Use our configured consumer factory
        factory.setConsumerFactory(consumerFactory());

        // How many threads process messages simultaneously
        // 3 means 3 capture blocks can be processed at the same time
        factory.setConcurrency(3);

        // Manual acknowledgement mode
        // Our code must explicitly confirm each message
        // after successful processing
        // This is critical for data integrity
        factory.getContainerProperties().setAckMode(
            ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );

        logger.info("Kafka listener container factory configured");
        logger.info("Concurrency: 3 | AckMode: MANUAL_IMMEDIATE");

        return factory;
    }
}