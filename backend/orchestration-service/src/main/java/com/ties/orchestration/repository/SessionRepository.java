package com.ties.orchestration.repository;

import com.ties.orchestration.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * SessionRepository — PostgreSQL
 *
 * Handles all database operations for the mission_sessions
 * table in PostgreSQL.
 *
 * Design decisions:
 * - findBySessionIdentifier used to look up sessions
 *   by the string identifier from Kafka messages
 * - incrementTotalBlocks uses a direct UPDATE query
 *   to avoid race conditions when multiple consumer
 *   threads update the same session simultaneously
 * - completeSession sets both status and completed_at
 *   in a single atomic query
 */
@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    /**
     * Find a session by its string identifier.
     * Used when processing confirmation events to locate
     * the session a block belongs to.
     *
     * @param sessionIdentifier the unique session identifier
     * @return Optional SessionEntity if found
     */
    Optional<SessionEntity> findBySessionIdentifier(
        String sessionIdentifier
    );

    /**
     * Increment confirmed block count for a session.
     * Uses direct UPDATE to avoid race conditions across
     * the 3 concurrent consumer threads.
     *
     * @param sessionIdentifier the unique session identifier
     */
    @Modifying
    @Transactional
    @Query("UPDATE SessionEntity s SET s.totalBlocks = s.totalBlocks + 1 " +
           "WHERE s.sessionIdentifier = :sessionIdentifier")
    void incrementTotalBlocks(
        @Param("sessionIdentifier") String sessionIdentifier
    );

    /**
     * Mark a session as completed.
     * Called when orchestration-service determines all
     * blocks for the session have been confirmed.
     *
     * @param sessionIdentifier the unique session identifier
     * @param completedAt       timestamp of completion
     */
    @Modifying
    @Transactional
    @Query("UPDATE SessionEntity s SET s.status = 'SESSION_COMPLETED', " +
           "s.completedAt = :completedAt " +
           "WHERE s.sessionIdentifier = :sessionIdentifier")
    void completeSession(
        @Param("sessionIdentifier") String sessionIdentifier,
        @Param("completedAt") Instant completedAt
    );
}
