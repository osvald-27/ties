package com.ties.validation.model;

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
 * within a CaptureBlock received from ingestion-service.
 *
 * Local copy scoped to validation service package.
 * Each microservice owns its own model classes — services
 * must never share model classes across service boundaries.
 *
 * Validation service checks performed on each frame:
 * - qualityFlag must be present
 * - capturedAt must fall within the parent block window
 * - frameIndex must be sequential with no gaps
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrameMetadata {

    /**
     * Position of this frame within its parent CaptureBlock.
     * Zero-indexed. Validation checks for sequential indices
     * with no gaps — missing indices indicate lost frames.
     */
    @JsonProperty("frame_index")
    private Integer frameIndex;

    /**
     * Timestamp when this specific frame was captured.
     * Must fall between captureStartTimestamp and
     * captureEndTimestamp of the parent CaptureBlock.
     * Frames outside this window are flagged as corrupt.
     */
    @JsonProperty("captured_at")
    private Instant capturedAt;

    /**
     * Camera orientation at moment of capture.
     * pitch, yaw, roll from device sensors.
     * Stored as flexible JsonNode — varies across
     * device types (phone vs drone hardware).
     */
    @JsonProperty("camera_orientation")
    private JsonNode cameraOrientation;

    /**
     * Camera exposure parameters at moment of capture.
     * iso, shutter_speed, aperture, white_balance.
     * Stored as flexible JsonNode — varies across
     * device types (phone vs drone hardware).
     */
    @JsonProperty("exposure_params")
    private JsonNode exposureParams;

    /**
     * Whether this frame passed device-side quality check.
     * true  = frame passed basic validation on device
     * false = frame flagged but transmitted for backend
     *         to make the final processing decision
     */
    @JsonProperty("quality_flag")
    private Boolean qualityFlag;
}