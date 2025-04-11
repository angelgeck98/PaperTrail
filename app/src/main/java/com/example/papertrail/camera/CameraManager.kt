package com.example.papertrail.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executors

@OptIn(markerClass = [ExperimentalGetImage::class])
class CameraManager(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private var camera: androidx.camera.core.Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isInitialized = false

    fun initializeCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onError: (Exception) -> Unit
    ) {
        if (isInitialized) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display.rotation)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                isInitialized = true
                Log.d("CameraManager", "Camera initialized successfully")
            } catch (e: Exception) {
                Log.e("CameraManager", "Failed to initialize camera", e)
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePicture(
        onSuccess: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = this.imageCapture ?: run {
            onError("Camera not ready")
            return
        }

        try {
            // Try direct capture first
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    @ExperimentalGetImage
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = image.use { proxy ->
                                convertYuvToBitmap(proxy)
                            }
                            onSuccess(bitmap)
                        } catch (e: Exception) {
                            Log.e("CameraManager", "Direct capture failed, trying fallback", e)
                            // Fallback to file-based capture
                            takePictureFallback(onSuccess, onError)
                        }
                    }

                    override fun onError(ex: ImageCaptureException) {
                        Log.e("CameraManager", "Direct capture failed, trying fallback", ex)
                        // Fallback to file-based capture
                        takePictureFallback(onSuccess, onError)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("CameraManager", "Direct capture failed, trying fallback", e)
            // Fallback to file-based capture
            takePictureFallback(onSuccess, onError)
        }
    }

    private fun takePictureFallback(
        onSuccess: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = this.imageCapture ?: run {
            onError("Camera not ready")
            return
        }

        val file = File.createTempFile("receipt", ".jpg", context.cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)?.apply {
                            // Auto-rotate if needed
                            Matrix().apply {
                                postRotate(90f)
                            }.let { matrix ->
                                Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
                            }
                        } ?: run {
                            onError("Failed to decode image")
                            return
                        }
                        onSuccess(bitmap)
                    } finally {
                        file.delete() // Clean up
                    }
                }

                override fun onError(ex: ImageCaptureException) {
                    onError("Capture failed: ${ex.message}")
                    file.delete()
                }
            }
        )
    }

    @ExperimentalGetImage
    private fun convertYuvToBitmap(image: ImageProxy): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        // Create bitmap with correct dimensions
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )

        // Ensure buffer capacity
        buffer.rewind()
        if (buffer.remaining() >= bitmap.byteCount) {
            bitmap.copyPixelsFromBuffer(buffer)
        } else {
            throw IllegalStateException("Buffer underflow: Needed ${bitmap.byteCount} bytes, got ${buffer.remaining()}")
        }

        // Rotate according to device orientation
        return bitmap.rotate(image.imageInfo.rotationDegrees.toFloat())
    }

    fun cleanup() {
        try {
            cameraProvider?.unbindAll()
            camera = null
            imageCapture = null
            cameraProvider = null
            isInitialized = false
            cameraExecutor.shutdown()
            Log.d("CameraManager", "Camera resources cleaned up")
        } catch (e: Exception) {
            Log.e("CameraManager", "Error during cleanup", e)
        }
    }
}

// Extension function for rotation
private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
