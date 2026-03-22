package com.ties.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ties.android.ui.navigation.TiesNavGraph
import com.ties.android.ui.theme.TiesTheme

/**
 * MainActivity
 *
 * Single activity — hosts the Jetpack Compose UI.
 * Navigation handled by TiesNavGraph.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TiesNavGraph()
                }
            }
        }
    }
}