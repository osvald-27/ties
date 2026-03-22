package com.ties.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * CaptureBlockResponse
 *
 * Response from ingestion-service after receiving
 * a CaptureBlock. Contains acknowledgement status.
 */
data class CaptureBlockResponse(
    @SerializedName("block_identifier") val blockIdentifier: String,
    @SerializedName("status")           val status:          String,
    @SerializedName("message")          val message:         String
)