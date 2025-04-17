package com.example.papertrail.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraState(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    
    fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            Log.d("CameraState", "Initializing camera...")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            
            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    Log.d("CameraState", "Got camera provider")
                    
                    // Try to use the back camera first, if not available, use the front camera
                    val cameraSelector = try {
                        Log.d("CameraState", "Trying to use back camera")
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } catch (e: Exception) {
                        Log.w("CameraState", "Back camera not available, trying front camera", e)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    
                    bindCameraUseCases(previewView, cameraSelector)
                    Log.d("CameraState", "Camera initialized successfully")
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("CameraState", "Failed to initialize camera", e)
                    onError(e)
                }
            }, cameraExecutor)
        } catch (e: Exception) {
            Log.e("CameraState", "Failed to get camera provider", e)
            onError(e)
        }
    }
    
    private fun bindCameraUseCases(previewView: PreviewView, cameraSelector: CameraSelector) {
        val cameraProvider = cameraProvider ?: run {
            Log.e("CameraState", "Camera provider is null")
            return
        }
        
        Log.d("CameraState", "Binding camera use cases")
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            Log.d("CameraState", "Successfully bound camera use cases")
        } catch (e: Exception) {
            Log.e("CameraState", "Failed to bind camera use cases", e)
            throw e
        }
    }
    
    fun takePicture(
        onSuccess: (Bitmap) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(Exception("Camera not initialized"))
            return
        }
        
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = image.toBitmap()
                        onSuccess(bitmap)
                    } catch (e: Exception) {
                        onError(e)
                    } finally {
                        image.close()
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
    
    fun cleanup() {
        Log.d("CameraState", "Cleaning up camera resources")
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
} 