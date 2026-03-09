package com.ties.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIES Aerial Mapping System
 * Ingestion Service — Entry Point
 *
 * This service is responsible for:
 * 1. Listening to Kafka topic: capture.blocks
 * 2. Receiving capture blocks from mobile acquisition devices
 * 3. Validating payload integrity and metadata structure
 * 4. Forwarding valid blocks to downstream processing services
 *
 * In the prototype configuration, the mobile acquisition device
 * is a Google Pixel 5 smartphone acting as the drone replacement.
 */
@SpringBootApplication
@EnableKafka
public class IngestionApplication {

    private static final Logger logger = 
        LoggerFactory.getLogger(IngestionApplication.class);

    public static void main(String[] args) {

        logger.info("==============================================");
        logger.info(" TIES Ingestion Service Starting Up");
        logger.info(" Aerial Mapping System — Backend Tier");
        logger.info("==============================================");

        SpringApplication.run(IngestionApplication.class, args);

        logger.info("==============================================");
        logger.info(" Ingestion Service Ready");
        logger.info(" Listening for capture blocks on Kafka");
        logger.info("==============================================");
    }
}