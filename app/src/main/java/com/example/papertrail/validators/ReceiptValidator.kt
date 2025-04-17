package com.example.papertrail.validators

import com.example.papertrail.models.ReceiptValidationResult
import java.util.regex.Pattern

class ReceiptValidator {
    private val amountPattern = Pattern.compile("\\d+\\.\\d{2}")
    private val subtotalPatterns = listOf(
        "subtotal",
        "sub total",
        "sub-total",
        "sub-total:",
        "subtotal:"
    )
    private val taxPatterns = listOf(
        "tax",
        "tax:",
        "vat",
        "vat:",
        "gst",
        "gst:"
    )
    private val totalPatterns = listOf(
        "total",
        "total:",
        "amount due",
        "amount due:",
        "final total",
        "final total:"
    )

    fun validateReceiptTotals(ocrText: String): ReceiptValidationResult {
        val lines = ocrText.lines()
        
        val subtotal = findAmount(lines, subtotalPatterns)
        val tax = findAmount(lines, taxPatterns)
        val total = findAmount(lines, totalPatterns)

        if (subtotal == null || tax == null || total == null) {
            return ReceiptValidationResult.createError(
                "Could not find all required amounts in receipt"
            )
        }

        val calculatedTotal = subtotal + tax
        val isMatched = Math.abs(calculatedTotal - total) < 0.01 // Allow 1 cent difference

        return ReceiptValidationResult(
            isMatched = isMatched,
            parsedSubtotal = subtotal,
            parsedTax = tax,
            parsedTotal = total,
            confidence = if (isMatched) 1.0 else 0.8,
            validationMessage = if (isMatched) "Receipt totals match" 
                else "Receipt totals do not match (expected: $calculatedTotal, found: $total)"
        )
    }

    private fun findAmount(lines: List<String>, patterns: List<String>): Double? {
        for (line in lines) {
            val lowerLine = line.lowercase()
            for (pattern in patterns) {
                if (lowerLine.contains(pattern)) {
                    val matcher = amountPattern.matcher(line)
                    if (matcher.find()) {
                        return matcher.group().toDouble()
                    }
                }
            }
        }
        return null
    }
} 