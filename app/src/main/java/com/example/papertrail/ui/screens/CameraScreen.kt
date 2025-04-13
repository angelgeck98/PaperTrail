package com.example.papertrail.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.papertrail.camera.CameraManager
import com.example.papertrail.ocr.OCRProcessor
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

sealed class CameraState {
    object Loading : CameraState()
    object PermissionRequired : CameraState()
    data class Error(val message: String) : CameraState()
    object Active : CameraState()
    object Processing : CameraState()
}

@Composable
fun CameraScreen(
    onOcrComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraState by remember { mutableStateOf<CameraState>(CameraState.Loading) }
    var resetCamera by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val ocrProcessor = remember { OCRProcessor() }
    val cameraManager = remember { CameraManager(context) }
    val scanAreaColor = Color.Blue.copy(alpha = 0.3f)

    // Camera restart effect
    LaunchedEffect(resetCamera) {
        if (resetCamera) {
            delay(300) // Brief delay to allow cleanup
            cameraManager.cleanup()
            previewView?.let { view ->
                cameraManager.initializeCamera(view, lifecycleOwner) { error: Exception ->
                    Log.e("CAMERA", "Restart failed: ${error.message}")
                    cameraState = CameraState.Error("Failed to restart camera: ${error.message}")
                }
            }
            resetCamera = false
        }
    }

    // Check camera permission
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraState = if (isGranted) CameraState.Active else CameraState.PermissionRequired
    }

    // Request permission if needed
    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            cameraState = CameraState.Active
        }
    }

    Box(modifier.fillMaxSize()) {
        when (cameraState) {
            is CameraState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CameraState.PermissionRequired -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera permission is required to take photos",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
            is CameraState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (cameraState as CameraState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            cameraState = CameraState.Active
                            resetCamera = true
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
            CameraState.Processing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            CameraState.Active -> {
                // Camera preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            previewView = this
                            cameraManager.initializeCamera(this, lifecycleOwner) { e: Exception ->
                                cameraState = CameraState.Error("Failed to initialize camera: ${e.message}")
                                Log.e("CameraScreen", "Camera error", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // OCR guide box
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 280.dp, height = 180.dp)
                        .border(2.dp, Color.White)
                        .background(scanAreaColor)
                )

                // Capture button
                Button(
                    onClick = {
                        if (cameraState == CameraState.Active) {
                            cameraState = CameraState.Processing
                            cameraManager.takePicture(
                                onSuccess = { bitmap: Bitmap ->
                                    ocrProcessor.processImage(bitmap) { text: String ->
                                        onOcrComplete(text)
                                        cameraState = CameraState.Active
                                        resetCamera = true
                                    }
                                },
                                onError = { error: String ->
                                    cameraState = CameraState.Error(error)
                                    Log.e("CameraScreen", "Failed to take picture", Exception(error))
                                    resetCamera = true
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .size(64.dp),
                    enabled = cameraState == CameraState.Active
                ) {
                    Text("📸")
                }
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.cleanup()
            cameraExecutor.shutdown()
        }
    }
} 