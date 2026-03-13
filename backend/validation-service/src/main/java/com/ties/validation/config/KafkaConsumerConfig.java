package com.ties.validation.config;

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
 * Configures how the validation service connects to
 * and consumes messages from Apache Kafka topics.
 *
 * Design decisions:
 * - Same manual acknowledgement pattern as ingestion-service
 *   ensuring a block is only confirmed after passing all
 *   validation checks
 * - Separate consumer group from ingestion-service so Kafka
 *   delivers all messages to both services independently
 * - Concurrency of 3 matches ingestion-service throughput
 *   so validation never becomes the pipeline bottleneck
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * ConsumerFactory
     *
     * Produces Kafka consumer instances for the validation
     * service. Each consumer reads from the validated
     * capture blocks topic published by ingestion-service.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {

        logger.debug("Configuring Kafka consumer factory");
        logger.debug("Bootstrap servers: {}", bootstrapServers);
        logger.debug("Consumer group: {}", groupId);

        Map<String, Object> config = new HashMap<>();

        config.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers
        );

        config.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            groupId
        );

        config.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );

        config.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );

        config.put(
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
            "earliest"
        );

        // Manual commit — only acknowledge after all
        // validation checks pass successfully
        config.put(
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
            false
        );

        config.put(
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
            10
        );

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * KafkaListenerContainerFactory
     *
     * Wraps the ConsumerFactory with Spring-managed
     * manual acknowledgement and concurrency support.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
        kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        factory.setConcurrency(3);

        factory.getContainerProperties().setAckMode(
            ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );

        logger.info("Kafka listener container factory configured");
        logger.info("Concurrency: 3 | AckMode: MANUAL_IMMEDIATE");

        return factory;
    }
}