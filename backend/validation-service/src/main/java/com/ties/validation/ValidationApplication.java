package com.ties.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIES Aerial Mapping System
 * Validation Service — Entry Point
 *
 * This service is responsible for:
 * 1. Consuming validated capture blocks from Kafka topic: capture.blocks.validated
 * 2. Verifying checksum integrity of each block
 * 3. Confirming device is registered in PostgreSQL
 * 4. Confirming frame count is within expected range (8-10)
 * 5. Forwarding clean blocks to storage-service via Kafka
 */
@SpringBootApplication
public class ValidationApplication {

    private static final Logger logger =
        LoggerFactory.getLogger(ValidationApplication.class);

    public static void main(String[] args) {

        logger.info("==============================================");
        logger.info(" TIES Validation Service Starting Up");
        logger.info(" Aerial Mapping System — Backend Tier");
        logger.info("==============================================");

        SpringApplication.run(ValidationApplication.class, args);

        logger.info("==============================================");
        logger.info(" Validation Service Ready");
        logger.info(" Awaiting capture blocks from ingestion-service");
        logger.info("==============================================");
    }
}