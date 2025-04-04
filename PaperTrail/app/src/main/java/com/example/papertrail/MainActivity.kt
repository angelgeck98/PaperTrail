// MainActivity.kt
package com.example.papertrail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.papertrail.ui.theme.PaperTrailTheme
import java.io.InputStream
import androidx.activity.compose.rememberLauncherForActivityResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperTrailTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OCRScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRScreen() {
    val context = LocalContext.current
    var extractedText by remember { mutableStateOf("Extracted text will appear here") }
    var isLoading by remember { mutableStateOf(false) }

    // Use LaunchedEffect to create OCRProcessor only once, outside of composition
    val ocrProcessor = remember {
        try {
            OCRProcessor()
        } catch (e: Exception) {
            null // Handle the case where OCRProcessor can't be initialized (like in Preview)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { imageUri ->
                isLoading = true
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null && ocrProcessor != null) {
                        ocrProcessor.processImage(
                            bitmap,
                            onSuccess = { text ->
                                extractedText = text
                                isLoading = false
                            },
                            onError = { e ->
                                extractedText = "Error: ${e.message}"
                                isLoading = false
                            }
                        )
                    } else {
                        extractedText = "Error: Could not process image"
                        isLoading = false
                    }
                } catch (e: Exception) {
                    extractedText = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Processing..." else "Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = extractedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OCRScreenPreview() {
    PaperTrailTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            OCRScreen()
        }
    }
}