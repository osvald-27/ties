package com.ties.storage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * BlockRepository — PostgreSQL
 *
 * Handles all database operations for the capture_blocks
 * table in PostgreSQL.
 *
 * Extends JpaRepository which provides standard CRUD
 * operations out of the box:
 * - save()       → INSERT or UPDATE
 * - findById()   → SELECT by primary key
 * - delete()     → DELETE
 * - findAll()    → SELECT all
 *
 * We only need to define custom queries that go beyond
 * what JpaRepository provides automatically.
 *
 * Design decisions:
 * - UUID used as primary key type — matches init.sql schema
 * - Custom update query used for status changes to avoid
 *   loading the full entity just to change one field
 * - @Transactional on write operations ensures atomicity
 */
@Repository
public interface BlockRepository extends JpaRepository<BlockEntity, UUID> {

    /**
     * Check if a block already exists by its identifier.
     * Used to prevent duplicate storage of the same block
     * if Kafka delivers the message more than once.
     *
     * @param blockIdentifier the unique block identifier
     * @return true if block already exists in PostgreSQL
     */
    boolean existsByBlockIdentifier(String blockIdentifier);

    /**
     * Update transmission status of a block.
     * Called after successful MongoDB write to mark
     * the block as fully stored.
     *
     * @param blockIdentifier the unique block identifier
     * @param status          the new transmission status
     */
    @Modifying
    @Transactional
    @Query("UPDATE BlockEntity b SET b.transmissionStatus = :status " +
           "WHERE b.blockIdentifier = :blockIdentifier")
    void updateTransmissionStatus(
        @Param("blockIdentifier") String blockIdentifier,
        @Param("status") String status
    );
}