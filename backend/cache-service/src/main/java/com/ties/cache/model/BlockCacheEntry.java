package com.ties.cache.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * BlockCacheEntry — Redis Cache Model
 *
 * Represents the data stored in Redis for a single
 * confirmed capture block.
 *
 * Why not cache the full CaptureBlock?
 * - Full blocks with all frame data are large
 * - Downstream services only need summary metadata
 *   for fast lookup — not full frame arrays
 * - Keeping cache entries small improves Redis
 *   memory efficiency and read performance
 * - This is a purpose-built read model containing
 *   only the fields other services actually need
 *
 * Redis key pattern:
 * "block:{blockIdentifier}"
 * Example: "block:PIXEL5-001-1709123456789-001"
 *
 * TTL: 3600 seconds (1 hour) — configured in
 * application.properties as cache.block.ttl.seconds
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockCacheEntry {

    /**
     * Unique identifier of the cached block.
     * Used as part of the Redis key.
     */
    @JsonProperty("block_identifier")
    private String blockIdentifier;

    /**
     * Identifier of the device that produced this block.
     * Cached for fast device-level lookups.
     */
    @JsonProperty("device_identifier")
    private String deviceIdentifier;

    /**
     * Identifier of the mission session.
     * Cached for fast session-level lookups.
     */
    @JsonProperty("session_identifier")
    private String sessionIdentifier;

    /**
     * Quality confidence score of the block.
     * Cached so analytical tier can filter by quality
     * without hitting PostgreSQL.
     */
    @JsonProperty("quality_score")
    private Double qualityScore;

    /**
     * Total number of frames in the block.
     * Cached for quick frame count checks.
     */
    @JsonProperty("frame_count")
    private Integer frameCount;

    /**
     * Human readable spatial label from mobile app.
     * Cached for display in presentation tier.
     */
    @JsonProperty("spatial_label")
    private String spatialLabel;

    /**
     * Timestamp when the first frame was captured.
     * Cached for timeline display in presentation tier.
     */
    @JsonProperty("capture_start_timestamp")
    private Instant captureStartTimestamp;

    /**
     * Current transmission status of the block.
     * ACKNOWLEDGED = fully stored in PostgreSQL and MongoDB
     */
    @JsonProperty("transmission_status")
    private String transmissionStatus;

    /**
     * Timestamp when this entry was written to Redis.
     * Used for cache diagnostics and TTL tracking.
     */
    @JsonProperty("cached_at")
    private Instant cachedAt;
}
