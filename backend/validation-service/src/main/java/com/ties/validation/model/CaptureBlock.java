package com.ties.validation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * CaptureBlock Model
 *
 * Represents a capture block received from ingestion-service
 * via the capture.blocks.validated Kafka topic.
 *
 * This is a local copy of the CaptureBlock model scoped to
 * the validation service package. Each microservice owns its
 * own model classes — this is intentional in microservice
 * architecture. Services are independent and should not
 * share model classes across service boundaries.
 *
 * Validation service responsibilities for this object:
 * 1. Verify checksum matches payload content
 * 2. Confirm device is registered in PostgreSQL
 * 3. Confirm frame count is within expected range (8-10)
 * 4. Forward to storage-service if all checks pass
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureBlock {

    /**
     * Unique identifier for this capture block.
     * Format: DEVICE_ID-TIMESTAMP-SEQUENCE
     * Example: PIXEL5-001-1709123456789-001
     */
    @JsonProperty("block_identifier")
    private String blockIdentifier;

    /**
     * Unique identifier of the device that produced this block.
     * Validated against the devices table in PostgreSQL.
     * Blocks from unregistered devices are rejected.
     */
    @JsonProperty("device_identifier")
    private String deviceIdentifier;

    /**
     * Unique identifier of the mission session.
     * Groups all capture blocks from one mapping session.
     */
    @JsonProperty("session_identifier")
    private String sessionIdentifier;

    /**
     * Timestamp when the first frame was captured.
     */
    @JsonProperty("capture_start_timestamp")
    private Instant captureStartTimestamp;

    /**
     * Timestamp when the last frame was captured.
     */
    @JsonProperty("capture_end_timestamp")
    private Instant captureEndTimestamp;

    /**
     * Array of frame metadata objects.
     * Expected count: 8 to 10 frames per block.
     * Blocks outside this range fail validation.
     */
    @JsonProperty("frames")
    private List<FrameMetadata> frames;

    /**
     * Human readable spatial label from the mobile app.
     */
    @JsonProperty("spatial_label")
    private String spatialLabel;

    /**
     * Quality confidence score.
     * Range: 0.0 to 1.0
     * Already pre-filtered by ingestion-service above 0.5
     */
    @JsonProperty("quality_score")
    private Double qualityScore;

    /**
     * Checksum of the entire payload.
     * Validation service recomputes and compares this value
     * to detect any corruption during Kafka transmission.
     */
    @JsonProperty("checksum")
    private String checksum;

    /**
     * Transmission status set by the acquisition device.
     */
    @JsonProperty("transmission_status")
    private String transmissionStatus;

    /**
     * Session lifecycle state.
     */
    @JsonProperty("session_state")
    private String sessionState;
}