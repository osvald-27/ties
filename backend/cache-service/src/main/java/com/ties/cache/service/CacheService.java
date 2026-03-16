package com.ties.cache.service;

import com.ties.cache.model.BlockCacheEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * CacheService
 *
 * Handles all Redis read and write operations for the
 * cache service. This is the only class in the entire
 * TIES system that directly touches Redis.
 *
 * Redis key pattern:
 * "block:{blockIdentifier}"
 * Example: "block:PIXEL5-001-1709123456789-001"
 *
 * Design decisions:
 * - All Redis operations wrapped in try-catch so a
 *   Redis failure never crashes the Kafka consumer
 * - TTL applied on every write — entries expire
 *   automatically, no manual cleanup needed
 * - exists() check before write prevents overwriting
 *   a valid cache entry with duplicate Kafka messages
 */
@Service
public class CacheService {

    private static final Logger logger =
        LoggerFactory.getLogger(CacheService.class);

    // Redis key prefix for all block cache entries
    private static final String BLOCK_KEY_PREFIX = "block:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.block.ttl.seconds}")
    private long blockTtlSeconds;

    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Cache a block entry in Redis.
     *
     * Builds the Redis key from the block identifier,
     * sets the value as a BlockCacheEntry JSON object,
     * and applies the configured TTL.
     *
     * @param entry the BlockCacheEntry to store in Redis
     * @return true if write succeeded, false if failed
     */
    public boolean cacheBlock(BlockCacheEntry entry) {

        String key = BLOCK_KEY_PREFIX + entry.getBlockIdentifier();

        try {
            // Check for duplicate — don't overwrite
            // a valid entry with the same block
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                logger.warn("Block already cached — skipping: {}",
                    entry.getBlockIdentifier()
                );
                return true;
            }

            // Write entry with TTL
            redisTemplate.opsForValue().set(
                key,
                entry,
                Duration.ofSeconds(blockTtlSeconds)
            );

            logger.info("Block cached in Redis | key: {} | ttl: {}s",
                key,
                blockTtlSeconds
            );

            return true;

        } catch (Exception e) {
            logger.error("Redis write failed for key: {} | reason: {}",
                key,
                e.getMessage()
            );
            return false;
        }
    }

    /**
     * Retrieve a block entry from Redis.
     *
     * Returns null if the key does not exist or
     * if the entry has expired.
     *
     * @param blockIdentifier the unique block identifier
     * @return BlockCacheEntry if found, null if not cached
     */
    public BlockCacheEntry getBlock(String blockIdentifier) {

        String key = BLOCK_KEY_PREFIX + blockIdentifier;

        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                logger.debug("Cache miss for key: {}", key);
                return null;
            }

            logger.debug("Cache hit for key: {}", key);
            return (BlockCacheEntry) value;

        } catch (Exception e) {
            logger.error("Redis read failed for key: {} | reason: {}",
                key,
                e.getMessage()
            );
            return null;
        }
    }

    /**
     * Check if a block is currently cached in Redis.
     *
     * @param blockIdentifier the unique block identifier
     * @return true if block exists in Redis cache
     */
    public boolean isBlockCached(String blockIdentifier) {

        String key = BLOCK_KEY_PREFIX + blockIdentifier;

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));

        } catch (Exception e) {
            logger.error("Redis exists check failed for key: {} | reason: {}",
                key,
                e.getMessage()
            );
            return false;
        }
    }

    /**
     * Evict a block entry from Redis.
     *
     * Called when a block is invalidated or needs
     * to be refreshed with updated data.
     *
     * @param blockIdentifier the unique block identifier
     * @return true if eviction succeeded
     */
    public boolean evictBlock(String blockIdentifier) {

        String key = BLOCK_KEY_PREFIX + blockIdentifier;

        try {
            Boolean deleted = redisTemplate.delete(key);

            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Block evicted from Redis cache: {}", key);
            } else {
                logger.warn("Block not found in cache for eviction: {}", key);
            }

            return Boolean.TRUE.equals(deleted);

        } catch (Exception e) {
            logger.error("Redis eviction failed for key: {} | reason: {}",
                key,
                e.getMessage()
            );
            return false;
        }
    }

    /**
     * Build a BlockCacheEntry from confirmation event data.
     *
     * Called by the Kafka consumer when a confirmation
     * event arrives from storage-service. Sets cachedAt
     * to the current timestamp.
     *
     * @param blockIdentifier  the unique block identifier
     * @param deviceIdentifier the device that captured the block
     * @param sessionIdentifier the mission session identifier
     * @param qualityScore     block quality confidence score
     * @param frameCount       number of frames in the block
     * @param spatialLabel     human readable location label
     * @param captureStart     timestamp of first frame capture
     * @return BlockCacheEntry ready for Redis storage
     */
    public BlockCacheEntry buildCacheEntry(
        String blockIdentifier,
        String deviceIdentifier,
        String sessionIdentifier,
        Double qualityScore,
        Integer frameCount,
        String spatialLabel,
        Instant captureStart
    ) {
        return BlockCacheEntry.builder()
            .blockIdentifier(blockIdentifier)
            .deviceIdentifier(deviceIdentifier)
            .sessionIdentifier(sessionIdentifier)
            .qualityScore(qualityScore)
            .frameCount(frameCount)
            .spatialLabel(spatialLabel)
            .captureStartTimestamp(captureStart)
            .transmissionStatus("ACKNOWLEDGED")
            .cachedAt(Instant.now())
            .build();
    }
}