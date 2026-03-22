package com.ties.android.domain.model

/**
 * CameraOrientation
 *
 * Gyroscope and accelerometer readings at the
 * moment a frame was captured.
 * Attached by SensorRepository to each frame.
 */
data class CameraOrientation(
    val pitch: Float,
    val yaw:   Float,
    val roll:  Float
)