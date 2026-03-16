package com.ties.orchestration.config;

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
 * Configures how the orchestration service publishes
 * session.ready events to trigger the analytical tier
 * when a mission session is fully confirmed.
 *
 * Pipeline position:
 * orchestration-service → [session.ready] → analytical tier
 *
 * Design decisions:
 * - acks=all ensures session.ready event is only sent
 *   after all Kafka broker replicas confirm receipt
 * - Idempotence prevents duplicate session.ready events
 *   which would trigger duplicate AI processing runs
 * - This is the final Kafka produce in the backend tier
 */
@Configuration
public class KafkaProducerConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {

        logger.debug("Configuring Kafka producer factory");

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

        // Prevent duplicate session.ready events
        config.put(
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
            true
        );

        logger.info("Kafka producer factory configured");
        logger.info("Acks: all | Retries: 3 | Idempotence: enabled");

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        logger.info("KafkaTemplate initialized");
        return new KafkaTemplate<>(producerFactory());
    }
}