package com.ties.android.ui.screens.capture

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * CaptureScreen
 *
 * Live camera preview with auto-capture timer.
 * Frames are captured automatically every 500ms.
 * When 8-10 frames are collected they are assembled
 * into a CaptureBlock and queued for transmission.
 *
 * TODO: wire CameraX preview composable
 * TODO: wire CaptureViewModel for auto-capture logic
 */
@Composable
fun CaptureScreen(navController: NavController) {

    var frameCount by remember { mutableStateOf(0) }
    var isCapturing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture Session",
            style = MaterialTheme.typography.headlineMedium
        )

        // Camera preview placeholder
        // Replaced by CameraX AndroidView in next iteration
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.large
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Camera Preview",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Frames captured: $frameCount",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = if (isCapturing) "CAPTURING" else "STOPPED",
                color = if (isCapturing)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { isCapturing = !isCapturing },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCapturing)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isCapturing) "Stop Session" else "Start Capturing")
        }
    }
}