package com.example.papertrail.models

import java.time.LocalDate

data class Budget(
    val category: String,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: LocalDate = LocalDate.now(),
    val spent: Double = 0.0
) {
    val remaining: Double
        get() = amount - spent

    val progress: Float
        get() = (spent / amount).toFloat()

    val isOverBudget: Boolean
        get() = spent > amount
}

enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
} 