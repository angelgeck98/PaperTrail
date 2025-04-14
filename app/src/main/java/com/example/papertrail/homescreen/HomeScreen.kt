// HomeScreen.kt (EA)
package com.example.papertrail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onScanReceiptClick: () -> Unit,
    onViewReceiptsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PaperTrail", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onScanReceiptClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan New Receipt")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onViewReceiptsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Scanned Receipts")
        }
    }
}
