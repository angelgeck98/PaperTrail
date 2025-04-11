package com.example.papertrail.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onPhotoCaptured: (Bitmap, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraState by remember { mutableStateOf<CameraState>(CameraState.Loading) }
    var showReceiptCropScreen by remember { mutableStateOf(false) }
    var uncroppedImage by remember { mutableStateOf<Bitmap?>(null) }
    var resetCamera by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val ocrProcessor = remember { OCRProcessor() }
    val cameraManager = remember { CameraManager(context) }

    // Camera restart effect
    LaunchedEffect(resetCamera) {
        if (resetCamera) {
            delay(300) // Brief delay to allow cleanup
            cameraManager.cleanup()
            previewView?.let { view ->
                cameraManager.initializeCamera(view, lifecycleOwner) { error ->
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
    ) { isGranted ->
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

    if (showReceiptCropScreen && uncroppedImage != null) {
        ReceiptCropScreen(
            originalBitmap = uncroppedImage!!,
            onCropConfirmed = { croppedBitmap ->
                showReceiptCropScreen = false
                cameraState = CameraState.Processing
                ocrProcessor.processImage(croppedBitmap) { text ->
                    onPhotoCaptured(croppedBitmap, text)
                    cameraState = CameraState.Active
                    resetCamera = true // Restart camera after processing
                }
            },
            onCancel = {
                showReceiptCropScreen = false
                cameraState = CameraState.Active
                resetCamera = true // Restart camera after cancel
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                                resetCamera = true // Restart camera on retry
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
                CameraState.Processing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processing image...")
                        }
                    }
                }
                CameraState.Active -> {
                    // Camera preview
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    previewView = this
                                    cameraManager.initializeCamera(this, lifecycleOwner) { e ->
                                        cameraState = CameraState.Error("Failed to initialize camera: ${e.message}")
                                        Log.e("CameraScreen", "Camera error", e)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Capture button
                    Button(
                        onClick = {
                            cameraState = CameraState.Processing
                            cameraManager.takePicture(
                                onSuccess = { bitmap ->
                                    uncroppedImage = bitmap
                                    showReceiptCropScreen = true
                                },
                                onError = { error ->
                                    cameraState = CameraState.Error(error)
                                    Log.e("CameraScreen", "Failed to take picture", Exception(error))
                                    resetCamera = true // Restart camera on error
                                }
                            )
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(64.dp),
                        enabled = cameraState == CameraState.Active
                    ) {
                        Text("📸")
                    }
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