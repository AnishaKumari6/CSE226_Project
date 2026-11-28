package com.example.shivtraders.utils

import android.content.Context
import android.net.Uri
import com.example.shivtraders.data.Category
import com.example.shivtraders.data.Product
import com.example.shivtraders.data.StockMovement

object CsvExporter {

    fun export(
        context: Context,
        uri: Uri,
        products: List<Product>,
        categories: List<Category>,
        movements: List<StockMovement>
    ) {
        val categoryMap = categories.associate {
            it.id to it.name
        }

        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                writer ->

            writer.appendLine("SHIV TRADERS - INVENTORY REPORT")
            writer.appendLine()

            writer.appendLine(
                "Product ID,Product Name,Category,HSN,Purchase Price," +
                        "Selling Price,Current Stock,Minimum Stock,Stock Value"
            )

            products.forEach { product ->
                val name = csv(product.name)
                val category = csv(
                    categoryMap[product.categoryId] ?: "Uncategorized"
                )

                writer.appendLine(
                    listOf(
                        csv(product.productCode),
                        name,
                        category,
                        csv(product.hsnCode),
                        product.purchasePrice.toString(),
                        product.sellingPrice.toString(),
                        product.currentStock.toString(),
                        product.minimumStock.toString(),
                        (product.currentStock * product.purchasePrice).toString()
                    ).joinToString(",")
                )
            }

            writer.appendLine()
            writer.appendLine("STOCK MOVEMENT HISTORY")
            writer.appendLine(
                "Product,Type,Quantity,Previous Stock,New Stock,Reason,Timestamp"
            )

            movements.forEach { movement ->
                writer.appendLine(
                    listOf(
                        csv(movement.productName),
                        csv(movement.type),
                        movement.quantity.toString(),
                        movement.previousStock.toString(),
                        movement.newStock.toString(),
                        csv(movement.reason),
                        movement.timestamp.toString()
                    ).joinToString(",")
                )
            }
        } ?: throw IllegalStateException("Unable to create export file.")
    }

    private fun csv(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}