
package com.example.shivtraders

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.example.shivtraders.data.InventoryDatabase
import com.example.shivtraders.data.InventoryRepository
import com.example.shivtraders.ui.AuthenticationScreen
import com.example.shivtraders.ui.ShivTradersTheme
import com.example.shivtraders.ui.InventoryApp

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = InventoryDatabase.getDatabase(this)
        val repository = InventoryRepository(database)

        setContent {
            ShivTradersTheme {

                var isAuthenticated by remember {
                    mutableStateOf(false)
                }

                if (isAuthenticated) {
                    InventoryApp(repository)
                } else {
                    AuthenticationScreen(
                        activity = this,
                        onAuthenticated = {
                            isAuthenticated = true
                        }
                    )
                }
            }
        }
    }
}
