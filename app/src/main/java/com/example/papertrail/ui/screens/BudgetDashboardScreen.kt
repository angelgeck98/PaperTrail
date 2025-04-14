package com.example.papertrail.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.papertrail.models.Budget
import com.example.papertrail.models.BudgetPeriod
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDashboardScreen(
    budgets: List<Budget>,
    onBack: () -> Unit,
    onEditCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    val filteredBudgets = budgets.filter { it.period == selectedPeriod }
    val totalBudget = filteredBudgets.sumOf { it.amount }
    val totalSpent = filteredBudgets.sumOf { it.spent }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Period selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total budget card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currencyFormat.format(totalBudget),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Spent: ${currencyFormat.format(totalSpent)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LinearProgressIndicator(
                        progress = (totalSpent / totalBudget).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category budgets
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredBudgets) { budget ->
                    CategoryBudgetCard(
                        budget = budget,
                        onEdit = { onEditCategory(budget.category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(
    budget: Budget,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currencyFormat.format(budget.amount),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Spent: ${currencyFormat.format(budget.spent)}",
                style = MaterialTheme.typography.bodyMedium
            )

            LinearProgressIndicator(
                progress = budget.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = if (budget.isOverBudget) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Edit")
            }
        }
    }
} 