package com.ties.android.domain.manager

import com.ties.android.domain.model.CaptureBlock
import com.ties.android.domain.model.FrameMetadata

/**
 * BlockAssembler
 *
 * Assembles a list of FrameMetadata objects into
 * a complete CaptureBlock ready for transmission.
 *
 * Called by CaptureManager when 8-10 frames have
 * been collected for a capture window.
 */
object BlockAssembler {

    private const val DEVICE_ID = "PIXEL5-PROTOTYPE-001"

    fun assemble(
        frames:           List<FrameMetadata>,
        sessionIdentifier: String,
        spatialLabel:     String
    ): CaptureBlock {

        val blockIdentifier = "$DEVICE_ID-${System.currentTimeMillis()}-${(1..999).random()}"
        val qualityScore    = computeQualityScore(frames)
        val checksum        = ChecksumGenerator.generate(
            blockIdentifier,
            DEVICE_ID,
            sessionIdentifier
        )

        return CaptureBlock(
            blockIdentifier       = blockIdentifier,
            deviceIdentifier      = DEVICE_ID,
            sessionIdentifier     = sessionIdentifier,
            captureStartTimestamp = frames.first().capturedAt,
            captureEndTimestamp   = frames.last().capturedAt,
            frames                = frames,
            spatialLabel          = spatialLabel,
            qualityScore          = qualityScore,
            checksum              = checksum
        )
    }

    /**
     * Compute quality score from frame quality flags.
     * Ratio of frames that passed device-side validation.
     */
    private fun computeQualityScore(frames: List<FrameMetadata>): Double {
        val passed = frames.count { it.qualityFlag }
        return passed.toDouble() / frames.size
    }
}