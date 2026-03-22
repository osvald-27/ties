package com.ties.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ties.android.ui.screens.mission.MissionScreen
import com.ties.android.ui.screens.capture.CaptureScreen
import com.ties.android.ui.screens.status.StatusScreen

/**
 * TiesNavGraph
 *
 * Defines all navigation routes in the app.
 *
 * Routes:
 * - mission  : start/stop session screen (home)
 * - capture  : live camera preview and auto-capture
 * - status   : queue and transmission status
 */
object Routes {
    const val MISSION = "mission"
    const val CAPTURE = "capture"
    const val STATUS  = "status"
}

@Composable
fun TiesNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MISSION
    ) {
        composable(Routes.MISSION) {
            MissionScreen(navController = navController)
        }
        composable(Routes.CAPTURE) {
            CaptureScreen(navController = navController)
        }
        composable(Routes.STATUS) {
            StatusScreen(navController = navController)
        }
    }
}