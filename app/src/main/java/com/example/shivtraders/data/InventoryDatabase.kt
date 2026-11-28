package com.example.shivtraders.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction

@Database(
    entities = [
        Product::class,
        Category::class,
        StockMovement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "shiv_traders_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

class InventoryRepository(
    private val database: InventoryDatabase
) {
    val dao = database.inventoryDao()

    suspend fun updateStock(
        productId: Long,
        type: String,
        quantity: Int,
        reason: String
    ) {
        if (type == "ADJUSTMENT") {
            require(quantity >= 0) {
                "Quantity must be greater than or equal to zero."
            }
        } else {
            require(quantity > 0) {
                "Quantity must be greater than zero."
            }
        }

        require(type in listOf("IN", "OUT", "ADJUSTMENT")) {
            "Invalid stock movement type."
        }

        database.withTransaction {
            val product = dao.getProduct(productId)
                ?: throw IllegalArgumentException("Product not found.")

            require(product.isActive) {
                "This product is inactive."
            }

            val previous = product.currentStock

            val updated = when (type) {
                "IN" -> previous + quantity
                "OUT" -> previous - quantity
                else -> quantity
            }

            require(updated >= 0) {
                "Insufficient stock. Available: $previous boxes."
            }

            dao.updateProduct(
                product.copy(currentStock = updated)
            )

            dao.insertMovement(
                StockMovement(
                    productId = product.id,
                    productName = product.name,
                    type = type,
                    quantity = if (type == "ADJUSTMENT") {
                        updated - previous
                    } else {
                        quantity
                    },
                    previousStock = previous,
                    newStock = updated,
                    reason = reason
                )
            )
        }
    }

    suspend fun addProduct(product: Product): Long {
        require(product.productCode.isNotBlank())
        require(product.name.isNotBlank())
        require(product.purchasePrice >= 0)
        require(product.sellingPrice >= 0)
        require(product.currentStock >= 0)
        require(product.minimumStock >= 0)

        return database.withTransaction {
            val id = dao.insertProduct(product)

            if (product.currentStock > 0) {
                dao.insertMovement(
                    StockMovement(
                        productId = id,
                        productName = product.name,
                        type = "IN",
                        quantity = product.currentStock,
                        previousStock = 0,
                        newStock = product.currentStock,
                        reason = "Initial stock"
                    )
                )
            }

            id
        }
    }

    suspend fun editProduct(product: Product) {
        require(product.name.isNotBlank())
        require(product.purchasePrice >= 0)
        require(product.sellingPrice >= 0)
        require(product.minimumStock >= 0)

        database.withTransaction {
            val old = dao.getProduct(product.id)
                ?: throw IllegalArgumentException("Product not found.")

            require(old.currentStock == product.currentStock) {
                "Use stock adjustment to change inventory."
            }

            dao.updateProduct(product)
        }
    }

    suspend fun addCategory(category: Category) {
        require(category.name.isNotBlank())
        dao.insertCategory(category)
    }

    suspend fun removeCategory(id: Long) {
        val count = dao.countProductsInCategory(id)
        require(count == 0) {
            "This category still contains products."
        }
        dao.deleteCategory(id)
    }
}