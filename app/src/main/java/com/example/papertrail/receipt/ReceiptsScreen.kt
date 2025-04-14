package com.example.papertrail.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

@Composable
fun ReceiptsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var receipts by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        receipts = com.example.papertrail.utils.ImageStorage.getReceiptImages(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Receipts", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (receipts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No receipts found.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(receipts) { file ->
                    val bitmap = remember(file) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Receipt",
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to Home")
        }
    }
}
