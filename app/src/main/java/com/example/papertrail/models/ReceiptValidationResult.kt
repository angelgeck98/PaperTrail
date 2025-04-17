package com.example.papertrail.models

data class ReceiptValidationResult(
    val isMatched: Boolean,
    val parsedSubtotal: Double,
    val parsedTax: Double,
    val parsedTotal: Double,
    val confidence: Double = 1.0,
    val validationMessage: String = ""
) {
    companion object {
        fun createError(message: String) = ReceiptValidationResult(
            isMatched = false,
            parsedSubtotal = 0.0,
            parsedTax = 0.0,
            parsedTotal = 0.0,
            confidence = 0.0,
            validationMessage = message
        )
    }
} 