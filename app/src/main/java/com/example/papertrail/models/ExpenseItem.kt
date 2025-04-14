package com.example.papertrail.models

import java.time.LocalDate

data class ExpenseItem(
    val name: String,
    val amount: Double,
    val category: String,
    val date: LocalDate? = null
) 