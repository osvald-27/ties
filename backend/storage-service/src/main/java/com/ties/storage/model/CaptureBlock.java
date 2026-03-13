package com.ties.storage.model;

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
 * Represents a validated capture block received from
 * validation-service via the capture.blocks.stored
 * Kafka topic.
 *
 * Local copy scoped to storage service package.
 * Each microservice owns its own model classes — services
 * must never share model classes across service boundaries.
 *
 * Storage service responsibilities for this object:
 * 1. Persist block metadata to PostgreSQL capture_blocks table
 * 2. Persist each frame as a FrameDocument to MongoDB
 * 3. Update mission session state in PostgreSQL
 * 4. Forward confirmation to orchestration-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptureBlock {

    @JsonProperty("block_identifier")
    private String blockIdentifier;

    @JsonProperty("device_identifier")
    private String deviceIdentifier;

    @JsonProperty("session_identifier")
    private String sessionIdentifier;

    @JsonProperty("capture_start_timestamp")
    private Instant captureStartTimestamp;

    @JsonProperty("capture_end_timestamp")
    private Instant captureEndTimestamp;

    @JsonProperty("frames")
    private List<FrameMetadata> frames;

    @JsonProperty("spatial_label")
    private String spatialLabel;

    @JsonProperty("quality_score")
    private Double qualityScore;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("transmission_status")
    private String transmissionStatus;

    @JsonProperty("session_state")
    private String sessionState;
}
