package com.ties.android.domain.model

/**
 * FrameMetadata
 *
 * Metadata for a single captured frame.
 * Attached to each frame by the CaptureManager
 * at the moment of capture.
 */
data class FrameMetadata(
    val frameIndex:        Int,
    val capturedAt:        Long,
    val cameraOrientation: CameraOrientation,
    val exposureParams:    ExposureParams,
    val qualityFlag:       Boolean,
    val imageData:         ByteArray
)