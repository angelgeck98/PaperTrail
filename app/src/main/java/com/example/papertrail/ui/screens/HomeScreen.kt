package com.example.papertrail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.papertrail.models.ExpenseItem
import com.example.papertrail.parsers.ReceiptParser
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCaptureReceipt: () -> Unit,
    onViewReceipts: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PaperTrail") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to PaperTrail!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = onCaptureReceipt,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📸 Capture a New Receipt")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewReceipts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📁 View Saved Receipts")
            }
        }
    }
}
