package com.ties.android.data.local

import androidx.room.*

/**
 * QueuedBlockDao
 *
 * Room DAO for all local queue operations.
 * TransmissionManager uses this to read pending blocks
 * and update their status after transmission.
 */
@Dao
interface QueuedBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: QueuedBlock)

    @Query("SELECT * FROM queued_blocks WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingBlocks(): List<QueuedBlock>

    @Query("UPDATE queued_blocks SET status = :status WHERE blockIdentifier = :blockIdentifier")
    suspend fun updateStatus(blockIdentifier: String, status: String)

    @Query("UPDATE queued_blocks SET retryCount = retryCount + 1 WHERE blockIdentifier = :blockIdentifier")
    suspend fun incrementRetry(blockIdentifier: String)

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'ACKNOWLEDGED'")
    suspend fun getAcknowledgedCount(): Int

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int

    @Query("DELETE FROM queued_blocks WHERE status = 'ACKNOWLEDGED'")
    suspend fun clearAcknowledged()
}