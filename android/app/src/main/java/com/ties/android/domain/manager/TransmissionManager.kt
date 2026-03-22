package com.ties.android.domain.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import com.ties.android.data.local.QueuedBlock
import com.ties.android.data.local.QueuedBlockDao
import com.ties.android.data.remote.CaptureBlockRequest
import com.ties.android.data.remote.RetrofitClient
import com.ties.android.domain.model.CaptureBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TransmissionManager
 *
 * Handles queuing and transmission of CaptureBlocks
 * to ingestion-service.
 *
 * Flow:
 * 1. Block assembled by BlockAssembler
 * 2. Block serialized to JSON and saved to Room
 * 3. TransmissionManager checks connectivity
 * 4. If connected — transmit all pending blocks
 * 5. Update block status to ACKNOWLEDGED or FAILED
 *
 * Offline safe — blocks persist in Room until
 * connectivity is restored.
 */
class TransmissionManager(
    private val context: Context,
    private val dao:     QueuedBlockDao
) {
    private val gson = Gson()
    private val tag  = "TransmissionManager"

    /**
     * Queue a block for transmission.
     * Always saves to Room first — transmits if connected.
     */
    suspend fun queueBlock(block: CaptureBlock) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(block)
            val queued = QueuedBlock(
                blockIdentifier = block.blockIdentifier,
                blockJson       = json
            )
            dao.insert(queued)
            Log.d(tag, "Block queued: ${block.blockIdentifier}")

            if (isConnected()) {
                transmitPending()
            }
        }
    }

    /**
     * Transmit all pending blocks in queue order.
     * Called when connectivity is detected.
     */
    suspend fun transmitPending() {
        withContext(Dispatchers.IO) {
            val pending = dao.getPendingBlocks()
            Log.d(tag, "Transmitting ${pending.size} pending blocks")

            for (queued in pending) {
                transmitBlock(queued)
            }
        }
    }

    private suspend fun transmitBlock(queued: QueuedBlock) {
        try {
            dao.updateStatus(queued.blockIdentifier, "TRANSMITTING")

            val block   = gson.fromJson(queued.blockJson, CaptureBlock::class.java)
            val request = mapToRequest(block)
            val response = RetrofitClient.ingestionApi.submitCaptureBlock(request)

            if (response.isSuccessful) {
                dao.updateStatus(queued.blockIdentifier, "ACKNOWLEDGED")
                Log.d(tag, "Block acknowledged: ${queued.blockIdentifier}")
            } else {
                dao.updateStatus(queued.blockIdentifier, "FAILED")
                dao.incrementRetry(queued.blockIdentifier)
                Log.w(tag, "Block rejected by server: ${queued.blockIdentifier} | ${response.code()}")
            }

        } catch (e: Exception) {
            dao.updateStatus(queued.blockIdentifier, "PENDING")
            dao.incrementRetry(queued.blockIdentifier)
            Log.e(tag, "Transmission failed: ${queued.blockIdentifier} | ${e.message}")
        }
    }

    private fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun mapToRequest(block: CaptureBlock): CaptureBlockRequest {
        return CaptureBlockRequest(
            blockIdentifier       = block.blockIdentifier,
            deviceIdentifier      = block.deviceIdentifier,
            sessionIdentifier     = block.sessionIdentifier,
            captureStartTimestamp = block.captureStartTimestamp,
            captureEndTimestamp   = block.captureEndTimestamp,
            frames                = block.frames.map { frame ->
                com.ties.android.data.remote.FrameRequest(
                    frameIndex        = frame.frameIndex,
                    capturedAt        = frame.capturedAt,
                    cameraOrientation = mapOf(
                        "pitch" to frame.cameraOrientation.pitch,
                        "yaw"   to frame.cameraOrientation.yaw,
                        "roll"  to frame.cameraOrientation.roll
                    ),
                    exposureParams    = mapOf(
                        "iso"           to frame.exposureParams.iso.toString(),
                        "shutter_speed" to frame.exposureParams.shutterSpeed,
                        "aperture"      to frame.exposureParams.aperture.toString(),
                        "white_balance" to frame.exposureParams.whiteBalance
                    ),
                    qualityFlag       = frame.qualityFlag,
                    imageData         = android.util.Base64.encodeToString(
                        frame.imageData,
                        android.util.Base64.NO_WRAP
                    )
                )
            },
            spatialLabel          = block.spatialLabel,
            qualityScore          = block.qualityScore,
            checksum              = block.checksum,
            transmissionStatus    = block.transmissionStatus,
            sessionState          = block.sessionState
        )
    }
}