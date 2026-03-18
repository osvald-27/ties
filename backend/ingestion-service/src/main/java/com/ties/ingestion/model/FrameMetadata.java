package com.ties.ingestion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * FrameMetadata Model
 *
 * Represents the metadata for a single captured frame
 * within a CaptureBlock. Each CaptureBlock contains
 * a list of these — typically 8 to 10 per block.
 *
 * This model maps directly to the frame_metadata table
 * in PostgreSQL and is also embedded inside CaptureBlock
 * payloads received from the acquisition device via Kafka.
 *
 * Design decisions:
 * - JsonNode used for camera_orientation and exposure_params
 *   because their internal structure may vary across device
 *   types (phone vs drone). Keeping them flexible avoids
 *   breaking changes when new devices are added.
 * - qualityFlag is a simple boolean set by the device after
 *   lightweight per-frame validation before transmission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrameMetadata {

    /**
     * Position of this frame within its parent CaptureBlock.
     * Zero-indexed. A block with 8 frames has indices 0 to 7.
     * Used to reconstruct frame order during processing.
     */
    @JsonProperty("frame_index")
    private Integer frameIndex;

    /**
     * Timestamp when this specific frame was captured.
     * Falls between captureStartTimestamp and captureEndTimestamp
     * of the parent CaptureBlock.
     * Stored as Unix timestamp in milliseconds.
     */
    @JsonProperty("captured_at")
    private Instant capturedAt;

    /**
     * Camera orientation at the moment this frame was captured.
     * Contains pitch, yaw, roll values from the device sensors.
     *
     * Example structure:
     * {
     *   "pitch": -2.3,
     *   "yaw": 134.7,
     *   "roll": 0.5
     * }
     *
     * Stored as flexible JsonNode because drone and phone
     * sensors may report different fields.
     */
    @JsonProperty("camera_orientation")
    private JsonNode cameraOrientation;

    /**
     * Camera exposure parameters at the moment of capture.
     * Describes the lighting conditions and sensor settings
     * used for this frame.
     *
     * Example structure:
     * {
     *   "iso": 400,
     *   "shutter_speed": "1/500",
     *   "aperture": 1.8,
     *   "white_balance": "AUTO"
     * }
     *
     * Stored as flexible JsonNode to support different
     * camera hardware across device types.
     */
    @JsonProperty("exposure_params")
    private JsonNode exposureParams;

    /**
     * Whether this frame passed the device-side quality check.
     * Set by the mobile app or drone edge system before
     * the frame is included in the CaptureBlock.
     *
     * true  = frame passed basic quality validation
     * false = frame is flagged but still transmitted
     *         so the backend can make the final decision
     *
     * A CaptureBlock with many false-flagged frames will
     * likely receive a low qualityScore overall.
     */
    @JsonProperty("quality_flag")
    private Boolean qualityFlag;
}