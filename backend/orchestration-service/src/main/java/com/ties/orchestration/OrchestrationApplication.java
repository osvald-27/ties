package com.ties.orchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIES Aerial Mapping System
 * Orchestration Service — Entry Point
 *
 * This service is responsible for:
 * 1. Consuming storage confirmation events from Kafka
 * 2. Tracking how many blocks have been confirmed
 *    per mission session
 * 3. Detecting when all blocks for a session are stored
 * 4. Updating mission session status in PostgreSQL
 * 5. Triggering the analytical tier when a session
 *    is fully confirmed and ready for AI processing
 *
 * Orchestration service is the brain of the pipeline.
 * It does not move data — it tracks state and decides
 * when the next phase of processing should begin.
 *
 * Pipeline position:
 * storage-service → [capture.blocks.confirmed] → orchestration-service
 *                                              → [session.ready] → analytical tier
 */
@SpringBootApplication
public class OrchestrationApplication {

    private static final Logger logger =
        LoggerFactory.getLogger(OrchestrationApplication.class);

    public static void main(String[] args) {

        logger.info("==============================================");
        logger.info(" TIES Orchestration Service Starting Up");
        logger.info(" Aerial Mapping System — Backend Tier");
        logger.info("==============================================");

        SpringApplication.run(OrchestrationApplication.class, args);

        logger.info("==============================================");
        logger.info(" Orchestration Service Ready");
        logger.info(" Tracking mission session completion");
        logger.info("==============================================");
    }
}