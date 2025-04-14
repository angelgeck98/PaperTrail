package com.example.papertrail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.papertrail.ui.screens.CameraScreen
import com.example.papertrail.ui.screens.ReceiptDetailScreen
import com.example.papertrail.ui.theme.PaperTrailTheme

// Navigation enum
enum class Screen {
    HOME,
    CAMERA,
    RECEIPT_DETAIL,
    RECEIPTS
}

class MainActivity : ComponentActivity() {
    private var hasCameraPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Log.e("MainActivity", "Camera permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check camera permission
        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            PaperTrailTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showReceiptDetail by remember { mutableStateOf(false) }
                    var receiptText by remember { mutableStateOf("") }

                    if (showReceiptDetail) {
                        ReceiptDetailScreen(
                            text = receiptText,
                            onBack = { showReceiptDetail = false },
                            onNewReceipt = {
                                showReceiptDetail = false
                                receiptText = ""
                            }
                        )
                    } else if (hasCameraPermission) {
                        CameraScreen(
                            onOcrComplete = { text: String ->
                                receiptText = text
                                showReceiptDetail = true
                            }
                        )
                    } else {
                        // Permission request UI
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
                        }

                        Screen.CAMERA -> {
                            if (hasCameraPermission) {
                                CameraScreen(
                                    onOcrComplete = { text ->
                                        receiptText = text
                                        screen = Screen.RECEIPT_DETAIL
                                    }
                                )
                            } else {
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
                                        onClick = {
                                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    ) {
                                        Text("Grant Permission")
                                    }
                                }
                            }
                        }

                        Screen.RECEIPT_DETAIL -> {
                            ReceiptDetailScreen(
                                text = receiptText,
                                onBack = { screen = Screen.HOME },
                                onNewReceipt = {
                                    receiptText = ""
                                    screen = Screen.CAMERA
                                }
                            )
                        }

                        Screen.RECEIPTS -> {
                            ReceiptsScreen(
                                onBack = { screen = Screen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
