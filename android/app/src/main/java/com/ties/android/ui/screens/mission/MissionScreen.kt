package com.ties.android.ui.screens.mission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ties.android.ui.navigation.Routes

/**
 * MissionScreen
 *
 * Home screen of the TIES app.
 * User starts a new mapping session here.
 * Navigates to CaptureScreen when session starts.
 */
@Composable
fun MissionScreen(navController: NavController) {

    var sessionLabel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TIES Mapping",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aerial Pollution Mapping System",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = sessionLabel,
            onValueChange = { sessionLabel = it },
            label = { Text("Location label") },
            placeholder = { Text("e.g. North corridor, Floor 2") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Routes.CAPTURE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = sessionLabel.isNotBlank()
        ) {
            Text("Start Mapping Session")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate(Routes.STATUS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("View Queue Status")
        }
    }
}