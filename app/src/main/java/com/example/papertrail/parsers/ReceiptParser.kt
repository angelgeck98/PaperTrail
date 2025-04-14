package com.example.papertrail.parsers

import com.example.papertrail.models.ExpenseItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class ReceiptParser {
    private val amountPattern = Pattern.compile("\\$?\\d+\\.\\d{2}")
    private val datePatterns = listOf(
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MMM dd, yyyy"),
        DateTimeFormatter.ofPattern("dd MMM yyyy")
    )

    private val categoryKeywords = mapOf(
        "FOOD & DINING" to listOf(
            "coffee", "restaurant", "cafe", "food", "meal", "lunch", "dinner", "breakfast",
            "pizza", "burger", "sandwich", "salad", "sushi", "chinese", "italian", "mexican",
            "bakery", "dessert", "ice cream", "drink", "beverage", "tea", "juice", "soda"
        ),
        "OFFICE SUPPLIES" to listOf(
            "office", "supplies", "stationery", "paper", "ink", "printer", "pen", "pencil",
            "notebook", "folder", "binder", "stapler", "tape", "scissors", "marker", "highlighter",
            "envelope", "stamp", "postage", "shipping", "mail"
        ),
        "TRAVEL & TRANSPORT" to listOf(
            "gas", "fuel", "transport", "taxi", "uber", "lyft", "bus", "train", "subway",
            "metro", "airport", "flight", "hotel", "lodging", "parking", "toll", "rental",
            "car", "bike", "scooter"
        ),
        "ENTERTAINMENT" to listOf(
            "movie", "cinema", "concert", "show", "ticket", "theater", "museum", "gallery",
            "park", "zoo", "aquarium", "amusement", "game", "sports", "gym", "fitness",
            "yoga", "pool", "golf", "tennis"
        ),
        "SHOPPING" to listOf(
            "store", "shop", "market", "mall", "retail", "clothing", "shoes", "accessories",
            "electronics", "phone", "computer", "laptop", "tablet", "camera", "appliance",
            "furniture", "decor", "garden", "hardware"
        ),
        "HEALTH & WELLNESS" to listOf(
            "pharmacy", "drug", "medicine", "vitamin", "supplement", "doctor", "dentist",
            "clinic", "hospital", "medical", "health", "wellness", "spa", "massage",
            "salon", "barber", "hair", "nails", "beauty"
        ),
        "EDUCATION" to listOf(
            "school", "university", "college", "course", "class", "book", "textbook",
            "library", "tuition", "fee", "education", "learning", "training", "workshop",
            "seminar", "conference"
        ),
        "UTILITIES" to listOf(
            "electric", "gas", "water", "internet", "phone", "cable", "tv", "streaming",
            "subscription", "utility", "bill", "payment", "service", "maintenance"
        )
    )

    fun parseReceipt(text: String): List<ExpenseItem> {
        val lines = text.split("\n")
        val items = mutableListOf<ExpenseItem>()
        var date: LocalDate? = null

        // First, try to find the date
        date = findDate(lines)

        // Then parse items
        lines.forEach { line ->
            val amount = findAmount(line)
            if (amount != null) {
                val name = extractItemName(line, amount)
                val category = determineCategory(name)
                items.add(ExpenseItem(name, amount, category, date))
            }
        }

        return items
    }

    private fun findDate(lines: List<String>): LocalDate? {
        for (line in lines) {
            for (pattern in datePatterns) {
                try {
                    return LocalDate.parse(line.trim(), pattern)
                } catch (e: Exception) {
                    // Try next pattern
                }
            }
        }
        return null
    }

    private fun findAmount(line: String): Double? {
        val matcher = amountPattern.matcher(line)
        if (matcher.find()) {
            val amountStr = matcher.group().replace("$", "")
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    private fun extractItemName(line: String, amount: Double): String {
        val amountStr = String.format("$%.2f", amount)
        return line.replace(amountStr, "")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun determineCategory(itemName: String): String {
        val lowerName = itemName.lowercase()
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { lowerName.contains(it) }) {
                return category
            }
        }
        return "OTHER"
    }
} 