package com.ties.validation.validator;

import com.ties.validation.model.CaptureBlock;
import com.ties.validation.model.FrameMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * CaptureBlockValidator
 *
 * Performs deep validation of capture blocks received
 * from ingestion-service. Ingestion-service handles
 * structural validation — this service handles semantic
 * and integrity validation.
 *
 * Validation chain:
 * 1. Frame count within expected range (8-10)
 * 2. Frame indices are sequential with no gaps
 * 3. Frame timestamps fall within block capture window
 * 4. Checksum matches recomputed payload hash
 *
 * Design decisions:
 * - Each validation step is a separate private method
 *   so failures are logged with precise reasons
 * - Checksum uses SHA-256 — same algorithm expected
 *   from the acquisition device
 * - Validator is stateless — safe for concurrent use
 *   across the 3 consumer threads in the listener
 */
@Component
public class CaptureBlockValidator {

    private static final Logger logger =
        LoggerFactory.getLogger(CaptureBlockValidator.class);

    // Expected frame count range per capture block
    private static final int MIN_FRAMES = 8;
    private static final int MAX_FRAMES = 10;

    /**
     * Primary validation entry point.
     *
     * Runs all validation checks against the block.
     * Returns false on the first failed check —
     * no point running further checks on a bad block.
     *
     * @param block the deserialized CaptureBlock to validate
     * @return true if block passes all validation checks
     */
    public boolean validate(CaptureBlock block) {

        logger.debug("Starting deep validation for block: {}",
            block.getBlockIdentifier()
        );

        if (!isFrameCountValid(block)) {
            return false;
        }

        if (!areFrameIndicesSequential(block)) {
            return false;
        }

        if (!areFrameTimestampsValid(block)) {
            return false;
        }

        if (!isChecksumValid(block)) {
            return false;
        }

        logger.info("Block passed all validation checks: {}",
            block.getBlockIdentifier()
        );

        return true;
    }

    /**
     * Frame Count Validator
     *
     * Confirms the block contains between 8 and 10 frames.
     * Blocks outside this range indicate a capture cycle
     * that was interrupted or corrupted on the device.
     */
    private boolean isFrameCountValid(CaptureBlock block) {

        int count = block.getFrames().size();

        if (count < MIN_FRAMES || count > MAX_FRAMES) {
            logger.warn("Frame count out of range for block: {} | count: {} | expected: {}-{}",
                block.getBlockIdentifier(),
                count,
                MIN_FRAMES,
                MAX_FRAMES
            );
            return false;
        }

        logger.debug("Frame count valid: {} | count: {}",
            block.getBlockIdentifier(),
            count
        );

        return true;
    }

    /**
     * Frame Index Sequence Validator
     *
     * Confirms frame indices are zero-indexed and sequential
     * with no gaps. Example for 8 frames: 0,1,2,3,4,5,6,7
     *
     * Gaps indicate frames were lost during device assembly
     * of the capture block before transmission.
     */
    private boolean areFrameIndicesSequential(CaptureBlock block) {

        List<FrameMetadata> frames = block.getFrames();

        for (int i = 0; i < frames.size(); i++) {

            Integer index = frames.get(i).getFrameIndex();

            if (index == null) {
                logger.warn("Null frame index at position: {} in block: {}",
                    i,
                    block.getBlockIdentifier()
                );
                return false;
            }

            if (index != i) {
                logger.warn("Frame index gap detected in block: {} | expected: {} | found: {}",
                    block.getBlockIdentifier(),
                    i,
                    index
                );
                return false;
            }
        }

        logger.debug("Frame indices sequential for block: {}",
            block.getBlockIdentifier()
        );

        return true;
    }

    /**
     * Frame Timestamp Validator
     *
     * Confirms each frame's capturedAt timestamp falls
     * between the block's captureStartTimestamp and
     * captureEndTimestamp.
     *
     * Frames outside this window indicate clock drift
     * or corruption during block assembly on the device.
     */
    private boolean areFrameTimestampsValid(CaptureBlock block) {

        if (block.getCaptureStartTimestamp() == null ||
            block.getCaptureEndTimestamp() == null) {
            logger.warn("Missing block timestamps for block: {}",
                block.getBlockIdentifier()
            );
            return false;
        }

        List<FrameMetadata> frames = block.getFrames();

        for (FrameMetadata frame : frames) {

            if (frame.getCapturedAt() == null) {
                logger.warn("Null capturedAt for frame: {} in block: {}",
                    frame.getFrameIndex(),
                    block.getBlockIdentifier()
                );
                return false;
            }

            boolean afterStart = !frame.getCapturedAt()
                .isBefore(block.getCaptureStartTimestamp());

            boolean beforeEnd = !frame.getCapturedAt()
                .isAfter(block.getCaptureEndTimestamp());

            if (!afterStart || !beforeEnd) {
                logger.warn("Frame timestamp outside block window | block: {} | frame: {} | capturedAt: {}",
                    block.getBlockIdentifier(),
                    frame.getFrameIndex(),
                    frame.getCapturedAt()
                );
                return false;
            }
        }

        logger.debug("Frame timestamps valid for block: {}",
            block.getBlockIdentifier()
        );

        return true;
    }

    /**
     * Checksum Validator
     *
     * Recomputes SHA-256 hash of the block identifier
     * combined with device identifier and session identifier
     * and compares against the checksum sent by the device.
     *
     * A mismatch means the payload was corrupted in transit
     * through the Kafka pipeline.
     *
     * Note: The full checksum specification must match
     * exactly what the Android app computes on the device.
     * Both sides must hash the same fields in the same order.
     */
    private boolean isChecksumValid(CaptureBlock block) {

        try {
            // Build the raw string the device hashed
            // Must match the Android app checksum implementation
            String raw = block.getBlockIdentifier()
                + block.getDeviceIdentifier()
                + block.getSessionIdentifier();

            // Recompute SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                raw.getBytes(StandardCharsets.UTF_8)
            );

            // Convert bytes to hex string
            String recomputed = HexFormat.of().formatHex(hashBytes);

            if (!recomputed.equals(block.getChecksum())) {
                logger.warn("Checksum mismatch for block: {} | expected: {} | received: {}",
                    block.getBlockIdentifier(),
                    recomputed,
                    block.getChecksum()
                );
                return false;
            }

            logger.debug("Checksum valid for block: {}",
                block.getBlockIdentifier()
            );

            return true;

        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available: {}", e.getMessage());
            return false;
        }
    }
}