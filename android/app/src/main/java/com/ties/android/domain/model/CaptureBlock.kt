package com.ties.android.domain.model

/**
 * CaptureBlock
 *
 * Domain model representing a bundle of frames
 * captured at a single observation position.
 * Assembled by BlockAssembler and queued locally
 * before transmission to ingestion-service.
 */
data class CaptureBlock(
    val blockIdentifier:      String,
    val deviceIdentifier:     String,
    val sessionIdentifier:    String,
    val captureStartTimestamp: Long,
    val captureEndTimestamp:   Long,
    val frames:               List<FrameMetadata>,
    val spatialLabel:         String,
    val qualityScore:         Double,
    val checksum:             String,
    val transmissionStatus:   String = "PENDING",
    val sessionState:         String = "CAPTURE_WINDOW_ACTIVE"
)