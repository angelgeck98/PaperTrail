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
import androidx.compose.material.icons.filled.Settings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCaptureReceipt: () -> Unit,
    onViewReceipts: () -> Unit,
    onSettingsClick: () -> Unit

) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PaperTrail") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween, // space out content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // top buffer

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PaperTrail",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onCaptureReceipt,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📸 Scan a New Receipt" ,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onViewReceipts,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📁 Saved Receipts" ,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Version footer
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

