package com.example.papertrail.receipt

data class Receipt(
    val storeName: String? = null,
    val date: String? = null,
    val total: Double? = null,
    val items: List<ReceiptItem> = emptyList()
)

data class ReceiptItem(
    val name: String,
    val price: Double,
    val quantity: Int = 1
) 