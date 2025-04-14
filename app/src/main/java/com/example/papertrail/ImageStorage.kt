package com.example.papertrail.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object ImageStorage {
    private const val RECEIPTS_DIR = "receipts"

    fun saveReceiptImage(context: Context, bitmap: Bitmap): File {
        val receiptsDir = File(context.filesDir, RECEIPTS_DIR).apply { mkdirs() }
        val file = File(receiptsDir, "receipt_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }

    fun getReceiptImages(context: Context): List<File> {
        val receiptsDir = File(context.filesDir, RECEIPTS_DIR)
        return receiptsDir.listFiles()?.toList() ?: emptyList()
    }
}
