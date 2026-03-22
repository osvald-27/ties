package com.ties.android.domain.manager

import java.security.MessageDigest

/**
 * ChecksumGenerator
 *
 * Computes SHA-256 checksum for a CaptureBlock.
 * Must match the checksum verification logic in
 * validation-service on the backend exactly.
 *
 * Hash input: blockIdentifier + deviceIdentifier + sessionIdentifier
 * This is the same formula used in CaptureBlockValidator.java
 */
object ChecksumGenerator {

    fun generate(
        blockIdentifier:  String,
        deviceIdentifier: String,
        sessionIdentifier: String
    ): String {
        val raw = blockIdentifier + deviceIdentifier + sessionIdentifier
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}