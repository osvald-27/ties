import os

base = os.path.join(os.path.expanduser("~"), "ties")
app = os.path.join(base, "android", "app", "src", "main")
kotlin = os.path.join(app, "java", "com", "ties", "android")
res = os.path.join(app, "res")

files = {}

# ─────────────────────────────────────────────
# Build files
# ─────────────────────────────────────────────

files[os.path.join(base, "android", "build.gradle.kts")] = '''
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
'''.strip()

files[os.path.join(base, "android", "settings.gradle.kts")] = '''
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TIES"
include(":app")
'''.strip()

files[os.path.join(base, "android", "app", "build.gradle.kts")] = '''
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ties.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ties.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    composeOptions { kotlinCompilerExtensionVersion = "1.5.4" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
'''.strip()

# ─────────────────────────────────────────────
# AndroidManifest.xml
# ─────────────────────────────────────────────

files[os.path.join(app, "AndroidManifest.xml")] = '''
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Camera — frame capture -->
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- Internet — transmission to backend -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Network state — detect connection before transmitting -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Foreground service — keep capture running in background -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />

    <!-- Wake lock — prevent CPU sleep during capture session -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Camera hardware required -->
    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

    <application
        android:name=".TiesApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TIES"
        android:usesCleartextTraffic="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TIES">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
'''.strip()

# ─────────────────────────────────────────────
# Application entry point
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "TiesApplication.kt")] = '''
package com.ties.android

import android.app.Application

/**
 * TIES Application
 *
 * Entry point for the Android application.
 * Initialises app-wide dependencies at startup.
 */
class TiesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
'''.strip()

# ─────────────────────────────────────────────
# MainActivity
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "MainActivity.kt")] = '''
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
'''.strip()

# ─────────────────────────────────────────────
# Theme
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "ui", "theme", "TiesTheme.kt")] = '''
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
'''.strip()

# ─────────────────────────────────────────────
# Navigation
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "ui", "navigation", "TiesNavGraph.kt")] = '''
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
'''.strip()

# ─────────────────────────────────────────────
# Screens
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "ui", "screens", "mission", "MissionScreen.kt")] = '''
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
'''.strip()

files[os.path.join(kotlin, "ui", "screens", "capture", "CaptureScreen.kt")] = '''
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
'''.strip()

files[os.path.join(kotlin, "ui", "screens", "status", "StatusScreen.kt")] = '''
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
'''.strip()

# ─────────────────────────────────────────────
# Domain models
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "domain", "model", "CaptureBlock.kt")] = '''
package com.ties.android.domain.model

/**
 * CaptureBlock
 *
 * Domain model representing a bundle of frames
 * captured at a single observation position.
 * Assembled by BlockAssembler and queued locally
 * before transmission to ingestion-service.
 */
data class CaptureBlock(
    val blockIdentifier:      String,
    val deviceIdentifier:     String,
    val sessionIdentifier:    String,
    val captureStartTimestamp: Long,
    val captureEndTimestamp:   Long,
    val frames:               List<FrameMetadata>,
    val spatialLabel:         String,
    val qualityScore:         Double,
    val checksum:             String,
    val transmissionStatus:   String = "PENDING",
    val sessionState:         String = "CAPTURE_WINDOW_ACTIVE"
)
'''.strip()

files[os.path.join(kotlin, "domain", "model", "FrameMetadata.kt")] = '''
package com.ties.android.domain.model

/**
 * FrameMetadata
 *
 * Metadata for a single captured frame.
 * Attached to each frame by the CaptureManager
 * at the moment of capture.
 */
data class FrameMetadata(
    val frameIndex:        Int,
    val capturedAt:        Long,
    val cameraOrientation: CameraOrientation,
    val exposureParams:    ExposureParams,
    val qualityFlag:       Boolean,
    val imageData:         ByteArray
)
'''.strip()

files[os.path.join(kotlin, "domain", "model", "CameraOrientation.kt")] = '''
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
'''.strip()

files[os.path.join(kotlin, "domain", "model", "ExposureParams.kt")] = '''
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
'''.strip()

# ─────────────────────────────────────────────
# Local database (Room)
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "data", "local", "QueuedBlock.kt")] = '''
package com.ties.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * QueuedBlock — Room Entity
 *
 * Persists a serialized CaptureBlock to local SQLite
 * via Room while waiting for network connectivity.
 *
 * blockJson stores the full CaptureBlock as JSON string.
 * TransmissionManager reads rows with status = PENDING
 * and transmits them when connection is available.
 */
@Entity(tableName = "queued_blocks")
data class QueuedBlock(
    @PrimaryKey
    val blockIdentifier: String,
    val blockJson:        String,
    val status:           String = "PENDING",
    val createdAt:        Long   = System.currentTimeMillis(),
    val retryCount:       Int    = 0
)
'''.strip()

files[os.path.join(kotlin, "data", "local", "QueuedBlockDao.kt")] = '''
package com.ties.android.data.local

import androidx.room.*

/**
 * QueuedBlockDao
 *
 * Room DAO for all local queue operations.
 * TransmissionManager uses this to read pending blocks
 * and update their status after transmission.
 */
@Dao
interface QueuedBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: QueuedBlock)

    @Query("SELECT * FROM queued_blocks WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingBlocks(): List<QueuedBlock>

    @Query("UPDATE queued_blocks SET status = :status WHERE blockIdentifier = :blockIdentifier")
    suspend fun updateStatus(blockIdentifier: String, status: String)

    @Query("UPDATE queued_blocks SET retryCount = retryCount + 1 WHERE blockIdentifier = :blockIdentifier")
    suspend fun incrementRetry(blockIdentifier: String)

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'ACKNOWLEDGED'")
    suspend fun getAcknowledgedCount(): Int

    @Query("SELECT COUNT(*) FROM queued_blocks WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int

    @Query("DELETE FROM queued_blocks WHERE status = 'ACKNOWLEDGED'")
    suspend fun clearAcknowledged()
}
'''.strip()

files[os.path.join(kotlin, "data", "local", "TiesDatabase.kt")] = '''
package com.ties.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * TiesDatabase — Room Database
 *
 * Local SQLite database for offline block queuing.
 * Single instance shared across the app via companion object.
 */
@Database(
    entities = [QueuedBlock::class],
    version = 1,
    exportSchema = false
)
abstract class TiesDatabase : RoomDatabase() {

    abstract fun queuedBlockDao(): QueuedBlockDao

    companion object {
        @Volatile
        private var INSTANCE: TiesDatabase? = null

        fun getDatabase(context: Context): TiesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TiesDatabase::class.java,
                    "ties_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
'''.strip()

# ─────────────────────────────────────────────
# Network — Retrofit API
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "data", "remote", "IngestionApiService.kt")] = '''
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
'''.strip()

files[os.path.join(kotlin, "data", "remote", "CaptureBlockRequest.kt")] = '''
package com.ties.android.data.remote

import com.google.gson.annotations.SerializedName

/**
 * CaptureBlockRequest
 *
 * JSON payload sent to ingestion-service REST endpoint.
 * Matches the CaptureBlock model on the backend.
 */
data class CaptureBlockRequest(
    @SerializedName("block_identifier")       val blockIdentifier:      String,
    @SerializedName("device_identifier")      val deviceIdentifier:     String,
    @SerializedName("session_identifier")     val sessionIdentifier:    String,
    @SerializedName("capture_start_timestamp") val captureStartTimestamp: Long,
    @SerializedName("capture_end_timestamp")   val captureEndTimestamp:   Long,
    @SerializedName("frames")                 val frames:               List<FrameRequest>,
    @SerializedName("spatial_label")          val spatialLabel:         String,
    @SerializedName("quality_score")          val qualityScore:         Double,
    @SerializedName("checksum")               val checksum:             String,
    @SerializedName("transmission_status")    val transmissionStatus:   String,
    @SerializedName("session_state")          val sessionState:         String
)

data class FrameRequest(
    @SerializedName("frame_index")        val frameIndex:        Int,
    @SerializedName("captured_at")        val capturedAt:        Long,
    @SerializedName("camera_orientation") val cameraOrientation: Map<String, Float>,
    @SerializedName("exposure_params")    val exposureParams:    Map<String, String>,
    @SerializedName("quality_flag")       val qualityFlag:       Boolean,
    @SerializedName("image_data")         val imageData:         String
)
'''.strip()

files[os.path.join(kotlin, "data", "remote", "CaptureBlockResponse.kt")] = '''
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
'''.strip()

files[os.path.join(kotlin, "data", "remote", "RetrofitClient.kt")] = '''
package com.ties.android.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient
 *
 * Singleton Retrofit instance for all network calls.
 * Base URL points to ingestion-service REST endpoint.
 *
 * For prototype: ingestion-service runs on local machine
 * Android emulator uses 10.0.2.2 to reach localhost
 * Physical device needs the machine IP address
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8081/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val ingestionApi: IngestionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IngestionApiService::class.java)
    }
}
'''.strip()

# ─────────────────────────────────────────────
# Domain — managers
# ─────────────────────────────────────────────

files[os.path.join(kotlin, "domain", "manager", "ChecksumGenerator.kt")] = '''
package com.ties.android.domain.manager

import java.security.MessageDigest

/**
 * ChecksumGenerator
 *
 * Computes SHA-256 checksum for a CaptureBlock.
 * Must match the checksum verification logic in
 * validation-service on the backend exactly.
 *
 * Hash input: blockIdentifier + deviceIdentifier + sessionIdentifier
 * This is the same formula used in CaptureBlockValidator.java
 */
object ChecksumGenerator {

    fun generate(
        blockIdentifier:  String,
        deviceIdentifier: String,
        sessionIdentifier: String
    ): String {
        val raw = blockIdentifier + deviceIdentifier + sessionIdentifier
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
'''.strip()

files[os.path.join(kotlin, "domain", "manager", "BlockAssembler.kt")] = '''
package com.ties.android.domain.manager

import com.ties.android.domain.model.CaptureBlock
import com.ties.android.domain.model.FrameMetadata

/**
 * BlockAssembler
 *
 * Assembles a list of FrameMetadata objects into
 * a complete CaptureBlock ready for transmission.
 *
 * Called by CaptureManager when 8-10 frames have
 * been collected for a capture window.
 */
object BlockAssembler {

    private const val DEVICE_ID = "PIXEL5-PROTOTYPE-001"

    fun assemble(
        frames:           List<FrameMetadata>,
        sessionIdentifier: String,
        spatialLabel:     String
    ): CaptureBlock {

        val blockIdentifier = "$DEVICE_ID-${System.currentTimeMillis()}-${(1..999).random()}"
        val qualityScore    = computeQualityScore(frames)
        val checksum        = ChecksumGenerator.generate(
            blockIdentifier,
            DEVICE_ID,
            sessionIdentifier
        )

        return CaptureBlock(
            blockIdentifier       = blockIdentifier,
            deviceIdentifier      = DEVICE_ID,
            sessionIdentifier     = sessionIdentifier,
            captureStartTimestamp = frames.first().capturedAt,
            captureEndTimestamp   = frames.last().capturedAt,
            frames                = frames,
            spatialLabel          = spatialLabel,
            qualityScore          = qualityScore,
            checksum              = checksum
        )
    }

    /**
     * Compute quality score from frame quality flags.
     * Ratio of frames that passed device-side validation.
     */
    private fun computeQualityScore(frames: List<FrameMetadata>): Double {
        val passed = frames.count { it.qualityFlag }
        return passed.toDouble() / frames.size
    }
}
'''.strip()

files[os.path.join(kotlin, "domain", "manager", "TransmissionManager.kt")] = '''
package com.ties.android.domain.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import com.ties.android.data.local.QueuedBlock
import com.ties.android.data.local.QueuedBlockDao
import com.ties.android.data.remote.CaptureBlockRequest
import com.ties.android.data.remote.RetrofitClient
import com.ties.android.domain.model.CaptureBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TransmissionManager
 *
 * Handles queuing and transmission of CaptureBlocks
 * to ingestion-service.
 *
 * Flow:
 * 1. Block assembled by BlockAssembler
 * 2. Block serialized to JSON and saved to Room
 * 3. TransmissionManager checks connectivity
 * 4. If connected — transmit all pending blocks
 * 5. Update block status to ACKNOWLEDGED or FAILED
 *
 * Offline safe — blocks persist in Room until
 * connectivity is restored.
 */
class TransmissionManager(
    private val context: Context,
    private val dao:     QueuedBlockDao
) {
    private val gson = Gson()
    private val tag  = "TransmissionManager"

    /**
     * Queue a block for transmission.
     * Always saves to Room first — transmits if connected.
     */
    suspend fun queueBlock(block: CaptureBlock) {
        withContext(Dispatchers.IO) {
            val json = gson.toJson(block)
            val queued = QueuedBlock(
                blockIdentifier = block.blockIdentifier,
                blockJson       = json
            )
            dao.insert(queued)
            Log.d(tag, "Block queued: ${block.blockIdentifier}")

            if (isConnected()) {
                transmitPending()
            }
        }
    }

    /**
     * Transmit all pending blocks in queue order.
     * Called when connectivity is detected.
     */
    suspend fun transmitPending() {
        withContext(Dispatchers.IO) {
            val pending = dao.getPendingBlocks()
            Log.d(tag, "Transmitting ${pending.size} pending blocks")

            for (queued in pending) {
                transmitBlock(queued)
            }
        }
    }

    private suspend fun transmitBlock(queued: QueuedBlock) {
        try {
            dao.updateStatus(queued.blockIdentifier, "TRANSMITTING")

            val block   = gson.fromJson(queued.blockJson, CaptureBlock::class.java)
            val request = mapToRequest(block)
            val response = RetrofitClient.ingestionApi.submitCaptureBlock(request)

            if (response.isSuccessful) {
                dao.updateStatus(queued.blockIdentifier, "ACKNOWLEDGED")
                Log.d(tag, "Block acknowledged: ${queued.blockIdentifier}")
            } else {
                dao.updateStatus(queued.blockIdentifier, "FAILED")
                dao.incrementRetry(queued.blockIdentifier)
                Log.w(tag, "Block rejected by server: ${queued.blockIdentifier} | ${response.code()}")
            }

        } catch (e: Exception) {
            dao.updateStatus(queued.blockIdentifier, "PENDING")
            dao.incrementRetry(queued.blockIdentifier)
            Log.e(tag, "Transmission failed: ${queued.blockIdentifier} | ${e.message}")
        }
    }

    private fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun mapToRequest(block: CaptureBlock): CaptureBlockRequest {
        return CaptureBlockRequest(
            blockIdentifier       = block.blockIdentifier,
            deviceIdentifier      = block.deviceIdentifier,
            sessionIdentifier     = block.sessionIdentifier,
            captureStartTimestamp = block.captureStartTimestamp,
            captureEndTimestamp   = block.captureEndTimestamp,
            frames                = block.frames.map { frame ->
                com.ties.android.data.remote.FrameRequest(
                    frameIndex        = frame.frameIndex,
                    capturedAt        = frame.capturedAt,
                    cameraOrientation = mapOf(
                        "pitch" to frame.cameraOrientation.pitch,
                        "yaw"   to frame.cameraOrientation.yaw,
                        "roll"  to frame.cameraOrientation.roll
                    ),
                    exposureParams    = mapOf(
                        "iso"           to frame.exposureParams.iso.toString(),
                        "shutter_speed" to frame.exposureParams.shutterSpeed,
                        "aperture"      to frame.exposureParams.aperture.toString(),
                        "white_balance" to frame.exposureParams.whiteBalance
                    ),
                    qualityFlag       = frame.qualityFlag,
                    imageData         = android.util.Base64.encodeToString(
                        frame.imageData,
                        android.util.Base64.NO_WRAP
                    )
                )
            },
            spatialLabel          = block.spatialLabel,
            qualityScore          = block.qualityScore,
            checksum              = block.checksum,
            transmissionStatus    = block.transmissionStatus,
            sessionState          = block.sessionState
        )
    }
}
'''.strip()

# ─────────────────────────────────────────────
# Create all files
# ─────────────────────────────────────────────

created = 0
for path, content in files.items():
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content)
    created += 1

print(f"✓ {created} Android app files created")
for path in files:
    print(f"  {path.replace(os.path.join(base, ''), '')}")