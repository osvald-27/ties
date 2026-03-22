package com.ties.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

/**
 * TiesTheme
 *
 * App-wide Material3 theme.
 * Supports light and dark mode automatically.
 */
@Composable
fun TiesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}