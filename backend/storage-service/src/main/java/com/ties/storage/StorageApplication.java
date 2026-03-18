package com.ties.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIES Aerial Mapping System
 * Storage Service — Entry Point
 *
 * This service is responsible for:
 * 1. Consuming validated capture blocks from Kafka topic: capture.blocks.stored
 * 2. Persisting block metadata to PostgreSQL
 * 3. Persisting frame images to MongoDB
 * 4. Updating mission session state in PostgreSQL
 * 5. Forwarding storage confirmation to orchestration-service
 *
 * Storage tier design:
 * - PostgreSQL stores relational metadata (blocks, sessions, devices)
 * - MongoDB stores raw frame image data (binary, schema-flexible)
 * - Redis caching handled by cache-service (separate microservice)
 */
@SpringBootApplication
public class StorageApplication {

    private static final Logger logger =
        LoggerFactory.getLogger(StorageApplication.class);

    public static void main(String[] args) {

        logger.info("==============================================");
        logger.info(" TIES Storage Service Starting Up");
        logger.info(" Aerial Mapping System — Backend Tier");
        logger.info("==============================================");

        SpringApplication.run(StorageApplication.class, args);

        logger.info("==============================================");
        logger.info(" Storage Service Ready");
        logger.info(" Awaiting validated blocks from validation-service");
        logger.info("==============================================");
    }
}