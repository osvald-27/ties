package com.ties.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * QueuedBlock — Room Entity
 *
 * Persists a serialized CaptureBlock to local SQLite
 * via Room while waiting for network connectivity.
 *
 * blockJson stores the full CaptureBlock as JSON string.
 * TransmissionManager reads rows with status = PENDING
 * and transmits them when connection is available.
 */
@Entity(tableName = "queued_blocks")
data class QueuedBlock(
    @PrimaryKey
    val blockIdentifier: String,
    val blockJson:        String,
    val status:           String = "PENDING",
    val createdAt:        Long   = System.currentTimeMillis(),
    val retryCount:       Int    = 0
)