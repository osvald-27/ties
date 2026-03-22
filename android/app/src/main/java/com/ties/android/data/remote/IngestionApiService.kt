package com.ties.android.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * IngestionApiService — Retrofit Interface
 *
 * Defines the REST endpoint on ingestion-service
 * that receives CaptureBlocks from the Android app.
 *
 * Base URL configured in RetrofitClient.
 * Endpoint must match the controller added to
 * ingestion-service in the next backend update.
 */
interface IngestionApiService {

    @POST("/api/v1/capture-blocks")
    suspend fun submitCaptureBlock(
        @Body block: CaptureBlockRequest
    ): Response<CaptureBlockResponse>
}