package com.example.shivtraders.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // PRODUCTS

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getProducts(): Flow<List<Product>>

    @Query("""
        SELECT * FROM products
        WHERE isActive = 1 AND
        (name LIKE '%' || :query || '%' OR
         productCode LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProduct(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun deactivateProduct(id: Long)

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getProductCount(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(currentStock), 0)
        FROM products WHERE isActive = 1
    """)
    fun getTotalStock(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1 AND currentStock <= minimumStock
    """)
    fun getLowStockCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1 AND currentStock = 0
    """)
    fun getOutOfStockCount(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(currentStock * purchasePrice), 0.0)
        FROM products WHERE isActive = 1
    """)
    fun getInventoryValue(): Flow<Double>

    @Query("""
        SELECT * FROM products
        WHERE isActive = 1 AND currentStock <= minimumStock
        ORDER BY currentStock ASC
    """)
    fun getLowStockProducts(): Flow<List<Product>>

    // CATEGORIES

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategory(id: Long): Category?

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE categoryId = :categoryId AND isActive = 1
    """)
    suspend fun countProductsInCategory(categoryId: Long): Int

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: Long)

    // STOCK MOVEMENTS

    @Insert
    suspend fun insertMovement(movement: StockMovement)

    @Query("""
        SELECT * FROM stock_movements
        ORDER BY timestamp DESC
    """)
    fun getMovements(): Flow<List<StockMovement>>

    @Query("""
        SELECT * FROM stock_movements
        WHERE productId = :productId
        ORDER BY timestamp DESC
    """)
    fun getProductMovements(productId: Long): Flow<List<StockMovement>>

    @Query("""
        SELECT * FROM stock_movements
        WHERE timestamp BETWEEN :start AND :end
        ORDER BY timestamp DESC
    """)
    fun getMovementsByDate(
        start: Long,
        end: Long
    ): Flow<List<StockMovement>>

    @Query("SELECT COUNT(*) FROM categories")
    fun getCategoryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCountValue(): Int
}