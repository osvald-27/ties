package com.ties.storage.repository;

import com.ties.storage.model.FrameDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FrameDocumentRepository — MongoDB
 *
 * Handles all database operations for the frames
 * collection in MongoDB mapping_images database.
 *
 * Extends MongoRepository which provides standard CRUD
 * operations out of the box — same pattern as JpaRepository
 * but for MongoDB documents instead of SQL rows.
 *
 * Design decisions:
 * - String used as ID type — MongoDB ObjectId is a string
 * - findByBlockIdentifier used by analytical tier later
 *   to retrieve all frames belonging to a block for
 *   AI frame fusion processing
 * - findByDeviceIdentifier used for device-level queries
 *   and diagnostics
 */
@Repository
public interface FrameDocumentRepository extends MongoRepository<FrameDocument, String> {

    /**
     * Retrieve all frames belonging to a specific block.
     * Used by analytical tier for frame fusion processing.
     *
     * @param blockIdentifier the unique block identifier
     * @return list of all FrameDocuments for this block
     */
    List<FrameDocument> findByBlockIdentifier(String blockIdentifier);

    /**
     * Retrieve all frames captured by a specific device.
     * Used for device-level diagnostics and reporting.
     *
     * @param deviceIdentifier the unique device identifier
     * @return list of all FrameDocuments from this device
     */
    List<FrameDocument> findByDeviceIdentifier(String deviceIdentifier);

    /**
     * Check if frames already exist for a block.
     * Used to prevent duplicate frame storage if Kafka
     * delivers the same block more than once.
     *
     * @param blockIdentifier the unique block identifier
     * @return true if frames already stored for this block
     */
    boolean existsByBlockIdentifier(String blockIdentifier);
}