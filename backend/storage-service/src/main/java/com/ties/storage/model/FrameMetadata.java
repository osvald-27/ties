package com.ties.storage.model;

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
 * Represents a single frame within a CaptureBlock received
 * from validation-service.
 *
 * Local copy scoped to storage service package.
 * Each microservice owns its own model classes.
 *
 * Storage service maps each FrameMetadata instance into
 * a FrameDocument and persists it to MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrameMetadata {

    @JsonProperty("frame_index")
    private Integer frameIndex;

    @JsonProperty("captured_at")
    private Instant capturedAt;

    @JsonProperty("camera_orientation")
    private JsonNode cameraOrientation;

    @JsonProperty("exposure_params")
    private JsonNode exposureParams;

    @JsonProperty("quality_flag")
    private Boolean qualityFlag;
}