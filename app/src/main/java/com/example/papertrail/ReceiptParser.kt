package com.example.papertrail

import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

data class ReceiptInfo(
    val store: String? = null,
    val date: Date? = null,
    val total: Double? = null,
    val items: List<ReceiptItem> = emptyList()
)

data class ReceiptItem(
    val name: String,
    val price: Double
)

class ReceiptParser {
    private val datePatterns = listOf(
        SimpleDateFormat("MM/dd/yyyy"),
        SimpleDateFormat("MM-dd-yyyy"),
        SimpleDateFormat("yyyy-MM-dd")
    )
    
    private val pricePattern = Pattern.compile("\\$?\\d+\\.\\d{2}")
    private val itemPattern = Pattern.compile("([^$]+)\\s+\\$?(\\d+\\.\\d{2})")

    fun parse(text: String): ReceiptInfo {
        val lines = text.lines()
        var store: String? = null
        var date: Date? = null
        var total: Double? = null
        val items = mutableListOf<ReceiptItem>()

        for (line in lines) {
            // Try to find store name (usually at the top)
            if (store == null && line.length > 0 && !line.contains("$")) {
                store = line.trim()
            }

            // Try to find date
            if (date == null) {
                for (pattern in datePatterns) {
                    try {
                        date = pattern.parse(line.trim())
                        break
                    } catch (e: Exception) {
                        // Continue to next pattern
                    }
                }
            }

            // Try to find total
            if (total == null) {
                val totalMatch = pricePattern.matcher(line)
                if (totalMatch.find() && line.lowercase().contains("total")) {
                    total = totalMatch.group().replace("$", "").toDoubleOrNull()
                }
            }

            // Try to find items
            val itemMatch = itemPattern.matcher(line)
            if (itemMatch.find()) {
                val name = itemMatch.group(1).trim()
                val price = itemMatch.group(2).toDoubleOrNull()
                if (price != null) {
                    items.add(ReceiptItem(name, price))
                }
            }
        }

        return ReceiptInfo(
            store = store,
            date = date,
            total = total,
            items = items
        )
    }
}