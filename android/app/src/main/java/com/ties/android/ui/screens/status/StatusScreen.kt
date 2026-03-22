package com.ties.android.ui.screens.status

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * StatusScreen
 *
 * Shows the local queue status and transmission progress.
 * Displays how many blocks are queued, pending, and sent.
 *
 * TODO: wire StatusViewModel to Room database queue
 */
@Composable
fun StatusScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Queue Status",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        StatusCard(label = "Queued blocks",      value = "0")
        Spacer(modifier = Modifier.height(12.dp))
        StatusCard(label = "Transmitting",        value = "0")
        Spacer(modifier = Modifier.height(12.dp))
        StatusCard(label = "Acknowledged",        value = "0")
        Spacer(modifier = Modifier.height(12.dp))
        StatusCard(label = "Failed",              value = "0")

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Mission")
        }
    }
}

@Composable
fun StatusCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}