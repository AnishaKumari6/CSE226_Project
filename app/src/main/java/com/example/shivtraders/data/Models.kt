package com.example.shivtraders.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productCode: String,
    val name: String,
    val categoryId: Long,
    val hsnCode: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val currentStock: Int,
    val minimumStock: Int,
    val imageUri: String? = null,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: String,
    val quantity: Int,
    val previousStock: Int,
    val newStock: Int,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)