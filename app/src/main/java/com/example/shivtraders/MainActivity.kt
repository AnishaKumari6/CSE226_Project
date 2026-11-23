package com.example.shivtraders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shivtraders.inventory.data.InventoryDatabase
import com.shivtraders.inventory.data.InventoryRepository
import com.shivtraders.inventory.ui.ShivTradersTheme
import com.shivtraders.inventory.ui.InventoryApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = InventoryDatabase.getDatabase(this)
        val repository = InventoryRepository(database)

        setContent {
            ShivTradersTheme {
                InventoryApp(repository)
            }
        }
    }
}