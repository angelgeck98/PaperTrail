package com.example.papertrail.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.papertrail.models.ReceiptValidationResult
import com.example.papertrail.parsers.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class BudgetState(
    val dailyBudget: Double = 0.0,
    val weeklyBudget: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val dailySpent: Double = 0.0,
    val weeklySpent: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

class BudgetViewModel : ViewModel() {
    private val _budgetState = MutableStateFlow(BudgetState())
    val budgetState: StateFlow<BudgetState> = _budgetState.asStateFlow()

    private val receiptParser = ReceiptParser()

    fun addOcrExpenseToBudget(validatedReceipt: ReceiptValidationResult) {
        if (!validatedReceipt.isMatched) {
            // Handle validation failure
            return
        }

        viewModelScope.launch {
            val currentState = _budgetState.value
            val expenseAmount = validatedReceipt.parsedTotal

            // Update budget state
            _budgetState.value = currentState.copy(
                dailySpent = currentState.dailySpent + expenseAmount,
                weeklySpent = currentState.weeklySpent + expenseAmount,
                monthlySpent = currentState.monthlySpent + expenseAmount,
                lastUpdated = LocalDateTime.now()
            )

            // Check if we need to reset daily/weekly budgets
            val now = LocalDate.now()
            if (currentState.lastUpdated.toLocalDate() != now) {
                resetDailyBudget()
            }
            if (currentState.lastUpdated.toLocalDate().dayOfWeek != now.dayOfWeek) {
                resetWeeklyBudget()
            }
            if (currentState.lastUpdated.toLocalDate().month != now.month) {
                resetMonthlyBudget()
            }
        }
    }

    private fun resetDailyBudget() {
        _budgetState.value = _budgetState.value.copy(
            dailySpent = 0.0
        )
    }

    private fun resetWeeklyBudget() {
        _budgetState.value = _budgetState.value.copy(
            weeklySpent = 0.0
        )
    }

    private fun resetMonthlyBudget() {
        _budgetState.value = _budgetState.value.copy(
            monthlySpent = 0.0
        )
    }

    fun setBudgets(daily: Double, weekly: Double, monthly: Double) {
        _budgetState.value = _budgetState.value.copy(
            dailyBudget = daily,
            weeklyBudget = weekly,
            monthlyBudget = monthly
        )
    }
} 