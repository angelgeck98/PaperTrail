package com.example.papertrail.parsers

import com.example.papertrail.models.ExpenseItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class ReceiptParser {
    private val datePatterns = listOf(
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    private val amountPattern = Pattern.compile("\\$?\\d+\\.\\d{2}")
    private val linePattern = Pattern.compile("(.+?)\\s+(\\$?\\d+\\.\\d{2})")

    fun parseReceipt(text: String): List<ExpenseItem> {
        val lines = text.lines()
        val items = mutableListOf<ExpenseItem>()
        var date: LocalDate? = null

        // First try to find date
        for (line in lines) {
            date = tryParseDate(line)
            if (date != null) break
        }

        // Then parse items
        for (line in lines) {
            val matcher = linePattern.matcher(line)
            if (matcher.find()) {
                val name = matcher.group(1).trim()
                val amount = matcher.group(2).replace("$", "").toDouble()
                val category = categorizeItem(name)
                
                items.add(ExpenseItem(name, amount, category, date))
            }
        }

        return items
    }

    private fun tryParseDate(line: String): LocalDate? {
        for (pattern in datePatterns) {
            try {
                return LocalDate.parse(line.trim(), pattern)
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun categorizeItem(name: String): String {
        val lowerName = name.lowercase()
        return when {
            lowerName.contains("coffee") || lowerName.contains("tea") -> "Beverages"
            lowerName.contains("lunch") || lowerName.contains("dinner") || lowerName.contains("breakfast") -> "Meals"
            lowerName.contains("pen") || lowerName.contains("paper") || lowerName.contains("notebook") -> "Office Supplies"
            lowerName.contains("gas") || lowerName.contains("fuel") -> "Transportation"
            else -> "Other"
        }
    }
} 