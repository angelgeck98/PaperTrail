package com.example.papertrail.receipt

class ReceiptParser {
    fun parseReceipt(text: String): Receipt {
        val lines = text.lines()
        var storeName: String? = null
        var date: String? = null
        var total: Double? = null
        val items = mutableListOf<ReceiptItem>()

        // Simple parsing logic - you can enhance this based on your needs
        for (line in lines) {
            when {
                // Look for store name (usually at the top)
                storeName == null && line.isNotBlank() -> {
                    storeName = line.trim()
                }
                // Look for date (usually contains numbers and separators)
                date == null && line.matches(Regex(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*")) -> {
                    date = line.trim()
                }
                // Look for total (usually contains currency symbol and is at the bottom)
                line.contains("total", ignoreCase = true) -> {
                    val totalMatch = Regex("\\d+\\.\\d{2}").find(line)
                    total = totalMatch?.value?.toDoubleOrNull()
                }
                // Look for items (usually contain price)
                else -> {
                    val priceMatch = Regex("\\d+\\.\\d{2}").find(line)
                    priceMatch?.let {
                        val price = it.value.toDoubleOrNull()
                        if (price != null) {
                            val itemName = line.substringBefore(it.value).trim()
                            if (itemName.isNotBlank()) {
                                items.add(ReceiptItem(name = itemName, price = price))
                            }
                        }
                    }
                }
            }
        }

        return Receipt(
            storeName = storeName,
            date = date,
            total = total,
            items = items
        )
    }
} 