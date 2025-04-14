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
import com.example.papertrail.models.Budget
import com.example.papertrail.models.BudgetPeriod
import com.example.papertrail.models.ExpenseItem
import com.example.papertrail.ui.screens.*
import com.example.papertrail.ui.theme.PaperTrailTheme
import java.time.LocalDate

// Navigation enum
enum class Screen {
    HOME,
    CAMERA,
    EXPENSE_BREAKDOWN,
    RECEIPTS,
    BUDGET_DASHBOARD,
    CATEGORY_EDITOR
}

class MainActivity : ComponentActivity() {
    private var hasCameraPermission by mutableStateOf(false)
    private var currentScreen by mutableStateOf(Screen.HOME)
    private var receiptText by mutableStateOf("")
    private var budgets by mutableStateOf(listOf<Budget>())
    private var selectedCategory by mutableStateOf("")
    private var expenseItems by mutableStateOf(listOf<ExpenseItem>())

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
                    when (currentScreen) {
                        Screen.HOME -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { currentScreen = Screen.CAMERA }
                                ) {
                                    Text("Take Photo")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { currentScreen = Screen.RECEIPTS }
                                ) {
                                    Text("View Receipts")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { currentScreen = Screen.BUDGET_DASHBOARD }
                                ) {
                                    Text("Budget Dashboard")
                                }
                            }
                        }

                        Screen.CAMERA -> {
                            if (hasCameraPermission) {
                                CameraScreen(
                                    onOcrComplete = { text ->
                                        receiptText = text
                                        currentScreen = Screen.EXPENSE_BREAKDOWN
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

                        Screen.EXPENSE_BREAKDOWN -> {
                            ExpenseBreakdownScreen(
                                ocrText = receiptText,
                                onBack = { currentScreen = Screen.HOME },
                                onSave = { items ->
                                    expenseItems = items
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.RECEIPTS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Receipts Screen (Coming Soon)")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { currentScreen = Screen.HOME }
                                ) {
                                    Text("Back to Home")
                                }
                            }
                        }

                        Screen.BUDGET_DASHBOARD -> {
                            BudgetDashboardScreen(
                                budgets = budgets,
                                onBack = { currentScreen = Screen.HOME },
                                onEditCategory = { category ->
                                    selectedCategory = category
                                    currentScreen = Screen.CATEGORY_EDITOR
                                }
                            )
                        }

                        Screen.CATEGORY_EDITOR -> {
                            CategoryEditorScreen(
                                category = selectedCategory,
                                budget = budgets.find { it.category == selectedCategory },
                                onSave = { budget ->
                                    budgets = budgets.filter { it.category != budget.category } + budget
                                    currentScreen = Screen.BUDGET_DASHBOARD
                                },
                                onDelete = {
                                    budgets = budgets.filter { it.category != selectedCategory }
                                    currentScreen = Screen.BUDGET_DASHBOARD
                                },
                                onBack = { currentScreen = Screen.BUDGET_DASHBOARD }
                            )
                        }
                    }
                }
            }
        }
    }
}
