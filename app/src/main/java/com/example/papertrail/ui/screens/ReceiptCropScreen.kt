package com.example.papertrail.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ReceiptCropScreen(
    originalBitmap: Bitmap,
    onCropConfirmed: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var startOffset by remember { mutableStateOf<Offset?>(null) }
    var currentOffset by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image with draggable crop overlay
        Image(
            bitmap = originalBitmap.asImageBitmap(),
            contentDescription = "Captured Receipt",
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startOffset = offset
                            currentOffset = offset
                        },
                        onDrag = { _, dragAmount ->
                            currentOffset = currentOffset?.plus(dragAmount)
                        },
                        onDragEnd = {
                            startOffset?.let { start ->
                                currentOffset?.let { end ->
                                    val left = minOf(start.x, end.x)
                                    val top = minOf(start.y, end.y)
                                    val right = maxOf(start.x, end.x)
                                    val bottom = maxOf(start.y, end.y)
                                    cropRect = Rect(
                                        left.toInt(),
                                        top.toInt(),
                                        right.toInt(),
                                        bottom.toInt()
                                    )
                                }
                            }
                            startOffset = null
                            currentOffset = null
                        }
                    )
                },
            contentScale = ContentScale.Fit
        )

        // Crop overlay
        if (cropRect != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = cropRect!!.left.dp,
                            y = cropRect!!.top.dp
                        )
                        .size(
                            width = (cropRect!!.right - cropRect!!.left).dp,
                            height = (cropRect!!.bottom - cropRect!!.top).dp
                        )
                        .background(Color.Transparent)
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    cropRect?.let { rect ->
                        onCropConfirmed(originalBitmap.crop(rect))
                    }
                },
                enabled = cropRect != null
            ) {
                Text("Confirm Crop")
            }
        }
    }
}

private fun Bitmap.crop(rect: Rect): Bitmap {
    return Bitmap.createBitmap(
        this,
        rect.left,
        rect.top,
        rect.width(),
        rect.height()
    )
} 