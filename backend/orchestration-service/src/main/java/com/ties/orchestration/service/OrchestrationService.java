package com.ties.orchestration.service;

import com.ties.orchestration.entity.SessionEntity;
import com.ties.orchestration.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * OrchestrationService
 *
 * The brain of the TIES backend pipeline.
 * Tracks mission session state and decides when a session
 * is complete enough to trigger AI processing.
 *
 * Responsibilities:
 * 1. Update session state in PostgreSQL as blocks arrive
 * 2. Detect when a session has reached completion
 * 3. Publish session.ready event to trigger analytical tier
 *
 * Design decisions:
 * - Session completion is currently triggered when the
 *   session status in PostgreSQL is SESSION_COMPLETED
 *   This is set by the Android app when the user ends
 *   the mapping session
 * - OrchestrationService does not count total expected
 *   blocks — it trusts the session status flag from
 *   the acquisition device
 * - All Kafka produce operations wrapped in try-catch
 *   so a broker failure does not crash the service
 */
@Service
public class OrchestrationService {

    private static final Logger logger =
        LoggerFactory.getLogger(OrchestrationService.class);

    private final SessionRepository sessionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.session.ready}")
    private String sessionReadyTopic;

    public OrchestrationService(
        SessionRepository sessionRepository,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.sessionRepository = sessionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Process a confirmed block event.
     *
     * Called by the Kafka consumer for each confirmation
     * event received from storage-service.
     *
     * Steps:
     * 1. Look up the session in PostgreSQL
     * 2. Increment the confirmed block count
     * 3. Check if session is marked as completed
     * 4. If completed — publish session.ready event
     *
     * @param blockIdentifier   the confirmed block identifier
     * @param sessionIdentifier the session this block belongs to
     * @param deviceIdentifier  the device that captured the block
     * @return true if processing succeeded
     */
    public boolean processConfirmedBlock(
        String blockIdentifier,
        String sessionIdentifier,
        String deviceIdentifier
    ) {
        logger.debug("Processing confirmed block: {} for session: {}",
            blockIdentifier,
            sessionIdentifier
        );

        // Step 1 — Look up session in PostgreSQL
        Optional<SessionEntity> sessionOpt =
            sessionRepository.findBySessionIdentifier(sessionIdentifier);

        if (sessionOpt.isEmpty()) {
            logger.warn("Session not found in PostgreSQL: {} | block: {}",
                sessionIdentifier,
                blockIdentifier
            );
            // Session may not exist yet — not a fatal error
            // Return true to acknowledge and move on
            return true;
        }

        SessionEntity session = sessionOpt.get();

        // Step 2 — Increment confirmed block count
        sessionRepository.incrementTotalBlocks(sessionIdentifier);

        logger.info("Block count incremented for session: {} | block: {}",
            sessionIdentifier,
            blockIdentifier
        );

        // Step 3 — Check if session is completed
        if ("SESSION_COMPLETED".equals(session.getStatus())) {

            logger.info("Session completed — triggering analytical tier: {}",
                sessionIdentifier
            );

            // Step 4 — Mark completion timestamp in PostgreSQL
            sessionRepository.completeSession(
                sessionIdentifier,
                Instant.now()
            );

            // Step 5 — Publish session.ready event
            return publishSessionReady(sessionIdentifier, deviceIdentifier);
        }

        logger.debug("Session still receiving blocks: {} | status: {}",
            sessionIdentifier,
            session.getStatus()
        );

        return true;
    }

    /**
     * Publish session.ready event to analytical tier.
     *
     * The session.ready event signals the analytical tier
     * that all blocks for a session are stored in PostgreSQL
     * and MongoDB and are ready for AI frame fusion processing.
     *
     * Message key:   deviceIdentifier
     * Message value: sessionIdentifier
     *
     * @param sessionIdentifier the completed session identifier
     * @param deviceIdentifier  the device that ran the session
     * @return true if publish succeeded
     */
    private boolean publishSessionReady(
        String sessionIdentifier,
        String deviceIdentifier
    ) {
        try {
            kafkaTemplate.send(
                sessionReadyTopic,
                deviceIdentifier,
                sessionIdentifier
            );

            logger.info("session.ready event published | session: {} | topic: {}",
                sessionIdentifier,
                sessionReadyTopic
            );

            return true;

        } catch (Exception e) {
            logger.error("Failed to publish session.ready event | session: {} | reason: {}",
                sessionIdentifier,
                e.getMessage()
            );
            return false;
        }
    }
}