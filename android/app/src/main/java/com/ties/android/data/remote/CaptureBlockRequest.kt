package com.ties.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * CaptureBlockRequest
 *
 * JSON payload sent to ingestion-service REST endpoint.
 * Matches the CaptureBlock model on the backend.
 */
data class CaptureBlockRequest(
    @SerializedName("block_identifier")       val blockIdentifier:      String,
    @SerializedName("device_identifier")      val deviceIdentifier:     String,
    @SerializedName("session_identifier")     val sessionIdentifier:    String,
    @SerializedName("capture_start_timestamp") val captureStartTimestamp: Long,
    @SerializedName("capture_end_timestamp")   val captureEndTimestamp:   Long,
    @SerializedName("frames")                 val frames:               List<FrameRequest>,
    @SerializedName("spatial_label")          val spatialLabel:         String,
    @SerializedName("quality_score")          val qualityScore:         Double,
    @SerializedName("checksum")               val checksum:             String,
    @SerializedName("transmission_status")    val transmissionStatus:   String,
    @SerializedName("session_state")          val sessionState:         String
)

data class FrameRequest(
    @SerializedName("frame_index")        val frameIndex:        Int,
    @SerializedName("captured_at")        val capturedAt:        Long,
    @SerializedName("camera_orientation") val cameraOrientation: Map<String, Float>,
    @SerializedName("exposure_params")    val exposureParams:    Map<String, String>,
    @SerializedName("quality_flag")       val qualityFlag:       Boolean,
    @SerializedName("image_data")         val imageData:         String
)