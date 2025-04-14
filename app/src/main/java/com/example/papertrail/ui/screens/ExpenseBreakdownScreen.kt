package com.example.papertrail.ui.screens

import android.graphics.Color
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.papertrail.models.ExpenseItem
import com.example.papertrail.parsers.ReceiptParser
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBreakdownScreen(
    ocrText: String,
    onBack: () -> Unit,
    onSave: (List<ExpenseItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val parser = remember { ReceiptParser() }
    var expenseItems by remember(ocrText) { mutableStateOf(parser.parseReceipt(ocrText)) }
    var editingItem by remember { mutableStateOf<ExpenseItem?>(null) }
    val totalAmount = remember(expenseItems) { expenseItems.sumOf { it.amount } }
    val categoryBreakdown = remember(expenseItems) {
        expenseItems.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
    }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Breakdown") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onSave(expenseItems) }) {
                        Icon(Icons.Default.Save, "Save")
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
            // Total amount card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currencyFormat.format(totalAmount),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category breakdown
            Text(
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            CategoryBreakdownPieChart(
                breakdown = categoryBreakdown,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expense items list
            Text(
                text = "Expense Items",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenseItems) { item ->
                    ExpenseItemRow(
                        item = item,
                        onEdit = { editingItem = item }
                    )
                }
            }
        }
    }

    // Edit dialog
    editingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Expense") },
            text = {
                Column {
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { newName ->
                            expenseItems = expenseItems.map { 
                                if (it == item) it.copy(name = newName) else it 
                            }
                        },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = item.amount.toString(),
                        onValueChange = { newAmount ->
                            newAmount.toDoubleOrNull()?.let { amount ->
                                expenseItems = expenseItems.map { 
                                    if (it == item) it.copy(amount = amount) else it 
                                }
                            }
                        },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("$") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = item.category,
                        onValueChange = { newCategory ->
                            expenseItems = expenseItems.map { 
                                if (it == item) it.copy(category = newCategory) else it 
                            }
                        },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun ExpenseItemRow(
    item: ExpenseItem,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodyMedium
                )
                item.date?.let { date ->
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(item.amount),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownPieChart(
    breakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                setUsePercentValues(true)
                description.isEnabled = false
                setExtraOffsets(5f, 10f, 5f, 5f)
                dragDecelerationFrictionCoef = 0.95f
                isDrawHoleEnabled = true
                setHoleColor(Color.WHITE)
                setTransparentCircleColor(Color.WHITE)
                setTransparentCircleAlpha(110)
                holeRadius = 58f
                transparentCircleRadius = 61f
                setDrawCenterText(true)
                rotationAngle = 0f
                isRotationEnabled = true
                isHighlightPerTapEnabled = true
                animateY(1400)
                legend.isEnabled = true
            }
        },
        modifier = modifier,
        update = { chart ->
            val entries = breakdown.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 3f
                selectionShift = 5f
                colors = ColorTemplate.MATERIAL_COLORS.toList()
            }
            chart.data = PieData(dataSet).apply {
                setValueTextSize(11f)
                setValueTextColor(Color.WHITE)
            }
            chart.invalidate()
        }
    )
} 