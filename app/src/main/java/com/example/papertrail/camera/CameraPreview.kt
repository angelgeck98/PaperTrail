package com.example.papertrail.camera

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
    cameraState: CameraState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { 
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            Log.d("CameraPreview", "Created PreviewView with COMPATIBLE mode")
        }
    }

    LaunchedEffect(Unit) {
        try {
            Log.d("CameraPreview", "Starting camera initialization")
            cameraState.initializeCamera(
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                onSuccess = {
                    Log.d("CameraPreview", "Camera initialized successfully")
                },
                onError = { e ->
                    Log.e("CameraPreview", "Failed to initialize camera", e)
                }
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Error during camera initialization", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
} 