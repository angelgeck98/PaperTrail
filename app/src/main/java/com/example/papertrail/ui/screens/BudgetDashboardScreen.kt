package com.example.papertrail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.papertrail.viewmodels.BudgetViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDashboardScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetState by viewModel.budgetState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf("") }
    var editingAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Budgets")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BudgetCard(
                    title = "Daily Budget",
                    budget = budgetState.dailyBudget,
                    spent = budgetState.dailySpent,
                    remaining = budgetState.dailyBudget - budgetState.dailySpent
                )
            }
            item {
                BudgetCard(
                    title = "Weekly Budget",
                    budget = budgetState.weeklyBudget,
                    spent = budgetState.weeklySpent,
                    remaining = budgetState.weeklyBudget - budgetState.weeklySpent
                )
            }
            item {
                BudgetCard(
                    title = "Monthly Budget",
                    budget = budgetState.monthlyBudget,
                    spent = budgetState.monthlySpent,
                    remaining = budgetState.monthlyBudget - budgetState.monthlySpent
                )
            }
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Budget") },
                text = {
                    Column {
                        Text("Select budget to edit:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BudgetTypeChip(
                                text = "Daily",
                                selected = editingBudget == "daily",
                                onClick = { editingBudget = "daily" }
                            )
                            BudgetTypeChip(
                                text = "Weekly",
                                selected = editingBudget == "weekly",
                                onClick = { editingBudget = "weekly" }
                            )
                            BudgetTypeChip(
                                text = "Monthly",
                                selected = editingBudget == "monthly",
                                onClick = { editingBudget = "monthly" }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editingAmount,
                            onValueChange = { editingAmount = it },
                            label = { Text("New Budget Amount") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = editingAmount.toDoubleOrNull() ?: 0.0
                            when (editingBudget) {
                                "daily" -> viewModel.setBudgets(
                                    daily = amount,
                                    weekly = budgetState.weeklyBudget,
                                    monthly = budgetState.monthlyBudget
                                )
                                "weekly" -> viewModel.setBudgets(
                                    daily = budgetState.dailyBudget,
                                    weekly = amount,
                                    monthly = budgetState.monthlyBudget
                                )
                                "monthly" -> viewModel.setBudgets(
                                    daily = budgetState.dailyBudget,
                                    weekly = budgetState.weeklyBudget,
                                    monthly = amount
                                )
                            }
                            showEditDialog = false
                            editingBudget = ""
                            editingAmount = ""
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetCard(
    title: String,
    budget: Double,
    spent: Double,
    remaining: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val progress = remember(spent, budget) {
        if (budget > 0.0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    progress > 0.9f -> MaterialTheme.colorScheme.error
                    progress > 0.7f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Budget", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(budget), style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("Spent", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(spent), style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    Text("Remaining", style = MaterialTheme.typography.bodySmall)
                    Text(currencyFormat.format(remaining), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun BudgetTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}