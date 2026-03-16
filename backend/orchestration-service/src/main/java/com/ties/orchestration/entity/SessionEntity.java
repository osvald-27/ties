package com.ties.orchestration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * SessionEntity — JPA Entity
 *
 * Maps directly to the mission_sessions table in PostgreSQL.
 * Orchestration service reads and updates session state
 * as confirmation events arrive from storage-service.
 *
 * Maps to init.sql table:
 * mission_sessions (
 *   id, session_identifier, device_id,
 *   started_at, completed_at, status, total_blocks
 * )
 *
 * Session lifecycle states:
 * SESSION_INITIATED  → session created on device
 * BLOCKS_RECEIVING   → confirmation events arriving
 * SESSION_COMPLETED  → all blocks confirmed, ready for AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mission_sessions")
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Unique identifier from the acquisition device.
     * Groups all capture blocks from one mapping session.
     */
    @Column(name = "session_identifier", unique = true, nullable = false)
    private String sessionIdentifier;

    /**
     * Foreign key reference to devices table.
     */
    @Column(name = "device_id")
    private UUID deviceId;

    /**
     * Timestamp when the session was initiated.
     */
    @Column(name = "started_at")
    private Instant startedAt;

    /**
     * Timestamp when the session was completed.
     * Null until all blocks are confirmed.
     * Set by orchestration-service when session completes.
     */
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Current lifecycle status of the session.
     * Updated by orchestration-service as blocks arrive.
     */
    @Column(name = "status")
    private String status;

    /**
     * Total number of confirmed blocks in this session.
     * Incremented by orchestration-service on each
     * confirmation event received.
     */
    @Column(name = "total_blocks")
    private Integer totalBlocks;
}