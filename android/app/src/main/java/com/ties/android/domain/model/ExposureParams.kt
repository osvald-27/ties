package com.ties.android.domain.model

/**
 * ExposureParams
 *
 * Camera exposure settings at the moment a frame
 * was captured. Read from CameraX capture result.
 */
data class ExposureParams(
    val iso:           Int,
    val shutterSpeed:  String,
    val aperture:      Float,
    val whiteBalance:  String
)