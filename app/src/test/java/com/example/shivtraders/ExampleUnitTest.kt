package com.example.shivtraders

import com.example.shivtraders.data.Category
import com.example.shivtraders.data.Product
import com.example.shivtraders.data.StockMovement
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun testProductCreationDefaults() {
        val product = Product(
            productCode = "ST-1001",
            name = "Crystal Beads",
            categoryId = 1L,
            hsnCode = "701810",
            purchasePrice = 150.0,
            sellingPrice = 250.0,
            currentStock = 10,
            minimumStock = 5
        )

        assertEquals("ST-1001", product.productCode)
        assertEquals("Crystal Beads", product.name)
        assertTrue(product.isActive)
        assertEquals(10, product.currentStock)
        assertEquals(5, product.minimumStock)
    }

    @Test
    fun testStockMovementTypes() {
        val movementIn = StockMovement(
            productId = 1L,
            productName = "Crystal Beads",
            type = "IN",
            quantity = 5,
            previousStock = 10,
            newStock = 15
        )

        assertEquals("IN", movementIn.type)
        assertEquals(15, movementIn.newStock)

        val movementAdjustmentZero = StockMovement(
            productId = 1L,
            productName = "Crystal Beads",
            type = "ADJUSTMENT",
            quantity = -15,
            previousStock = 15,
            newStock = 0
        )

        assertEquals("ADJUSTMENT", movementAdjustmentZero.type)
        assertEquals(0, movementAdjustmentZero.newStock)
    }

    @Test
    fun testCategoryCreation() {
        val category = Category(name = "Glassware", description = "Glass products")
        assertEquals("Glassware", category.name)
        assertEquals("Glass products", category.description)
    }
}
