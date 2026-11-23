package com.shivtraders.inventory.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shivtraders.inventory.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.shivtraders.inventory.utils.CsvExporter

private enum class Page {
    HOME, PRODUCTS, STOCK, HISTORY, MORE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryApp(repository: InventoryRepository) {

    val dao = repository.dao
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val products by dao.getProducts().collectAsState(emptyList())
    val categories by dao.getCategories().collectAsState(emptyList())
    val movements by dao.getMovements().collectAsState(emptyList())

    val productCount by dao.getProductCount().collectAsState(0)
    val totalStock by dao.getTotalStock().collectAsState(0)
    val lowStock by dao.getLowStockCount().collectAsState(0)
    val outStock by dao.getOutOfStockCount().collectAsState(0)
    val stockValue by dao.getInventoryValue().collectAsState(0.0)

    var page by remember { mutableStateOf(Page.HOME) }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    var showProductSheet by remember { mutableStateOf(false) }
    var showStockSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var stockProduct by remember { mutableStateOf<Product?>(null) }
    var detailProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        if (dao.getCategoryCountValue() == 0) {
            listOf(
                "Glass Beads & Smallwares",
                "Lighting & Luminaires",
                "Imitation Jewellery",
                "Decorative Glassware",
                "Glass Bottles & Containers"
            ).forEach {
                try {
                    repository.addCategory(Category(name = it))
                } catch (_: Exception) {}
            }
        }
    }

    val visibleProducts = products.filter { product ->
        val matchesQuery =
            product.name.contains(query, ignoreCase = true) ||
                    product.productCode.contains(query, ignoreCase = true) ||
                    product.hsnCode.contains(query, ignoreCase = true)

        val matchesCategory =
            selectedCategory == null ||
                    product.categoryId == selectedCategory

        matchesQuery && matchesCategory
    }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Canvas)
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "SHIV TRADERS",
                        fontSize = 12.sp,
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Burgundy
                    )
                    Text(
                        when (page) {
                            Page.HOME -> "Overview"
                            Page.PRODUCTS -> "Products"
                            Page.STOCK -> "Inventory"
                            Page.HISTORY -> "Activity"
                            Page.MORE -> "More"
                        },
                        fontSize = 25.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }

                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .clip(CircleShape)
                        .background(BurgundySoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ST",
                        color = Burgundy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 0.dp
            ) {
                val navItems = listOf(
                    Triple(Page.HOME, Icons.Rounded.GridView, "Home"),
                    Triple(Page.PRODUCTS, Icons.Rounded.Inventory2, "Products"),
                    Triple(Page.STOCK, Icons.Rounded.Warehouse, "Stock"),
                    Triple(Page.HISTORY, Icons.Rounded.History, "Activity"),
                    Triple(Page.MORE, Icons.Rounded.Menu, "More")
                )

                navItems.forEach { (target, icon, label) ->
                    NavigationBarItem(
                        selected = page == target,
                        onClick = {
                            page = target
                            query = ""
                        },
                        icon = {
                            Icon(icon, contentDescription = label)
                        },
                        label = {
                            Text(label, fontSize = 10.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Burgundy,
                            selectedTextColor = Burgundy,
                            indicatorColor = BurgundySoft,
                            unselectedIconColor = Muted
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = page == Page.PRODUCTS,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductSheet = true
                    },
                    containerColor = Burgundy,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add product")
                }
            }
        }
    ) { padding ->

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(120))
            },
            label = "page"
        ) { currentPage ->

            when (currentPage) {
                Page.HOME -> DashboardScreen(
                    modifier = Modifier.padding(padding),
                    productCount = productCount,
                    totalStock = totalStock,
                    lowStock = lowStock,
                    outStock = outStock,
                    stockValue = stockValue,
                    products = products,
                    movements = movements,
                    dateFormat = dateFormat,
                    onProducts = { page = Page.PRODUCTS },
                    onStock = { page = Page.STOCK },
                    onAdd = {
                        editingProduct = null
                        showProductSheet = true
                    },
                    onMovement = {
                        stockProduct = it
                        showStockSheet = true
                    }
                )

                Page.PRODUCTS -> ProductsScreen(
                    modifier = Modifier.padding(padding),
                    products = visibleProducts,
                    categories = categories,
                    query = query,
                    onQuery = { query = it },
                    selectedCategory = selectedCategory,
                    onCategory = { selectedCategory = it },
                    onProduct = { detailProduct = it },
                    onEdit = {
                        editingProduct = it
                        showProductSheet = true
                    },
                    onStock = {
                        stockProduct = it
                        showStockSheet = true
                    }
                )

                Page.STOCK -> StockScreen(
                    modifier = Modifier.padding(padding),
                    products = products,
                    onStock = {
                        stockProduct = it
                        showStockSheet = true
                    }
                )

                Page.HISTORY -> HistoryScreen(
                    modifier = Modifier.padding(padding),
                    movements = movements,
                    dateFormat = dateFormat
                )

                Page.MORE -> MoreScreen(
                    modifier = Modifier.padding(padding),
                    categories = categories,
                    stockValue = stockValue,
                    products = products,
                    movements = movements,
                    onCategories = { showCategorySheet = true }
                )
            }
        }
    }

    if (showProductSheet) {
        ProductSheet(
            product = editingProduct,
            categories = categories,
            repository = repository,
            onDismiss = { showProductSheet = false },
            onSave = {
                showProductSheet = false
                Toast.makeText(context, "Product saved", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val currentStockProd = stockProduct
    if (showStockSheet && currentStockProd != null) {
        StockSheet(
            product = currentStockProd,
            repository = repository,
            onDismiss = { showStockSheet = false },
            onSave = {
                showStockSheet = false
                Toast.makeText(context, "Inventory updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCategorySheet) {
        CategorySheet(
            repository = repository,
            categories = categories,
            onDismiss = { showCategorySheet = false }
        )
    }

    val currentDetailProd = detailProduct
    if (currentDetailProd != null) {
        ProductDetailSheet(
            product = currentDetailProd,
            categories = categories,
            movements = movements.filter {
                it.productId == currentDetailProd.id
            },
            onDismiss = { detailProduct = null },
            onEdit = {
                editingProduct = currentDetailProd
                detailProduct = null
                showProductSheet = true
            },
            onDelete = {
                val id = currentDetailProd.id
                detailProduct = null
                scope.launch {
                    try {
                        dao.deactivateProduct(id)
                    } catch (_: Exception) {}
                }
            },
            dateFormat = dateFormat
        )
    }
}

@Composable
private fun DashboardScreen(
    modifier: Modifier = Modifier,
    productCount: Int,
    totalStock: Int,
    lowStock: Int,
    outStock: Int,
    stockValue: Double,
    products: List<Product>,
    movements: List<StockMovement>,
    dateFormat: SimpleDateFormat,
    onProducts: () -> Unit,
    onStock: () -> Unit,
    onAdd: () -> Unit,
    onMovement: (Product) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 21.dp,
            end = 21.dp,
            top = 12.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        item {
            Text(
                "Your inventory,\nat a glance.",
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Products",
                    value = productCount.toString(),
                    icon = Icons.Rounded.Inventory2
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Total boxes",
                    value = totalStock.toString(),
                    icon = Icons.Rounded.Warehouse
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Low stock",
                    value = lowStock.toString(),
                    icon = Icons.Rounded.Warning,
                    tint = Warning
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Out of stock",
                    value = outStock.toString(),
                    icon = Icons.Rounded.ErrorOutline,
                    tint = Danger
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(23.dp))
                    .background(Burgundy)
                    .padding(22.dp)
            ) {
                Text(
                    "INVENTORY VALUATION",
                    fontSize = 10.sp,
                    letterSpacing = 1.7.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(9.dp))

                Text(
                    "₹${String.format(Locale.getDefault(), "%,.2f", stockValue)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Based on current stock × purchase price",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Add,
                    title = "New product",
                    onClick = onAdd
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.SwapVert,
                    title = "Update stock",
                    onClick = onStock
                )
                QuickAction(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Inventory2,
                    title = "Products",
                    onClick = onProducts
                )
            }
        }

        item {
            Text(
                "Needs attention",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        val alerts = products.filter {
            it.currentStock <= it.minimumStock
        }.take(5)

        if (alerts.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.CheckCircle,
                    title = "All looking good",
                    message = "No products currently need restocking."
                )
            }
        } else {
            items(alerts, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onClick = { onMovement(product) }
                )
            }
        }

        item {
            Text(
                "Recent activity",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (movements.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "Nothing here yet",
                    message = "Stock activity will appear here."
                )
            }
        } else {
            items(movements.take(5), key = { it.id }) { movement ->
                MovementRow(movement, dateFormat)
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = Burgundy
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .padding(16.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            value,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
        Text(label, fontSize = 12.sp, color = Muted)
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 17.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Burgundy, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProductsScreen(
    modifier: Modifier = Modifier,
    products: List<Product>,
    categories: List<Category>,
    query: String,
    onQuery: (String) -> Unit,
    selectedCategory: Long?,
    onCategory: (Long?) -> Unit,
    onProduct: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onStock: (Product) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Everything in one place.",
                fontSize = 14.sp,
                color = Muted
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, null)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQuery("") }) {
                            Icon(Icons.Rounded.Close, null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderSoft,
                    focusedBorderColor = Burgundy,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedContainerColor = SurfaceWhite
                )
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(
                    androidx.compose.foundation.rememberScrollState()
                )
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategory(null) },
                    label = { Text("All") }
                )

                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category.id,
                        onClick = {
                            onCategory(category.id)
                        },
                        label = { Text(category.name) }
                    )
                }
            }
        }

        item {
            Text(
                "${products.size} products",
                fontSize = 13.sp,
                color = Muted
            )
        }

        if (products.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Inventory2,
                    title = "No products found",
                    message = "Add a product or change your search."
                )
            }
        }

        items(products, key = { it.id }) { product ->
            ProductCatalogCard(
                product = product,
                categories = categories,
                onClick = { onProduct(product) },
                onEdit = { onEdit(product) },
                onStock = { onStock(product) }
            )
        }
    }
}

@Composable
private fun ProductCatalogCard(
    product: Product,
    categories: List<Category>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onStock: () -> Unit
) {
    val category = categories.find {
        it.id == product.categoryId
    }?.name ?: "Uncategorized"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            ProductImage(
                uri = product.imageUri,
                size = 74.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    product.productCode,
                    color = Muted,
                    fontSize = 11.sp
                )

                Text(
                    category,
                    color = Muted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            StockBadge(product)
        }

        Spacer(Modifier.height(13.dp))
        HorizontalDivider(color = BorderSoft)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AVAILABLE", fontSize = 9.sp, color = Muted)
                Text(
                    "${product.currentStock} boxes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("SELLING PRICE", fontSize = 9.sp, color = Muted)
                Text(
                    "₹${product.sellingPrice}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Edit")
            }

            Button(
                onClick = onStock,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Burgundy
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.SwapVert, null, Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text("Update stock")
            }
        }
    }
}

@Composable
private fun StockBadge(product: Product) {
    val color = when {
        product.currentStock == 0 -> Danger
        product.currentStock <= product.minimumStock -> Warning
        else -> Emerald
    }

    val background = when {
        product.currentStock == 0 -> DangerLight
        product.currentStock <= product.minimumStock -> WarningLight
        else -> EmeraldLight
    }

    Text(
        when {
            product.currentStock == 0 -> "Empty"
            product.currentStock <= product.minimumStock -> "Low"
            else -> "Healthy"
        },
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun ProductImage(
    uri: String?,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Canvas),
        contentAlignment = Alignment.Center
    ) {
        if (!uri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Rounded.Inventory2,
                null,
                tint = Muted.copy(alpha = 0.6f),
                modifier = Modifier.size(size / 2.5f)
            )
        }
    }
}

@Composable
private fun StockScreen(
    modifier: Modifier = Modifier,
    products: List<Product>,
    onStock: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = products.filter {
        it.name.contains(query, true) ||
                it.productCode.contains(query, true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Text(
                "Know what you have.",
                fontSize = 14.sp,
                color = Muted
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Find inventory...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Stock overview", fontWeight = FontWeight.SemiBold)
                Text("${filtered.size} items", color = Muted, fontSize = 12.sp)
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Warehouse,
                    title = "No inventory",
                    message = "Your products will appear here."
                )
            }
        }

        items(filtered, key = { it.id }) { product ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(19.dp))
                    .background(SurfaceWhite)
                    .clickable { onStock(product) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImage(product.imageUri, 53.dp)
                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            product.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            product.productCode,
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        "${product.currentStock}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (
                            product.currentStock <= product.minimumStock
                        ) Warning else Emerald
                    )
                }

                Spacer(Modifier.height(12.dp))

                val progress = if (product.minimumStock > 0) {
                    (product.currentStock.toFloat() /
                            (product.minimumStock * 2f)).coerceIn(0f, 1f)
                } else {
                    if (product.currentStock > 0) 1f else 0f
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape),
                    color = if (
                        product.currentStock <= product.minimumStock
                    ) Warning else Emerald,
                    trackColor = Canvas
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current: ${product.currentStock} boxes", fontSize = 11.sp)
                    Text("Minimum: ${product.minimumStock}", fontSize = 11.sp, color = Muted)
                }

                Spacer(Modifier.height(9.dp))

                Button(
                    onClick = { onStock(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.SwapVert, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Manage stock")
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductSheet(
    product: Product?,
    categories: List<Category>,
    repository: InventoryRepository,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(product?.name ?: "") }
    var hsn by remember { mutableStateOf(product?.hsnCode ?: "") }
    var purchase by remember {
        mutableStateOf(product?.purchasePrice?.toString() ?: "")
    }
    var selling by remember {
        mutableStateOf(product?.sellingPrice?.toString() ?: "")
    }
    var stock by remember {
        mutableStateOf(product?.currentStock?.toString() ?: "0")
    }
    var minimum by remember {
        mutableStateOf(product?.minimumStock?.toString() ?: "5")
    }
    var description by remember {
        mutableStateOf(product?.description ?: "")
    }
    var imageUri by remember {
        mutableStateOf(product?.imageUri)
    }
    var categoryId by remember {
        mutableLongStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: 0L)
    }

    LaunchedEffect(categories) {
        if (categoryId == 0L && categories.isNotEmpty()) {
            categoryId = categories.first().id
        }
    }
    var categoryMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // The selected image can still be used for this session.
            }
            imageUri = it.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Canvas,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = {
            BottomSheetHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp)
                .padding(bottom = 35.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (product == null) "New product" else "Edit product",
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "Add the details below to keep your catalog accurate.",
                color = Muted,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(SurfaceWhite)
                        .clickable {
                            imagePicker.launch(arrayOf("image/*"))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected product",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Rounded.AddPhotoAlternate,
                                null,
                                tint = Burgundy,
                                modifier = Modifier.size(29.dp)
                            )
                            Spacer(Modifier.height(5.dp))
                            Text("Add photo", fontSize = 11.sp, color = Muted)
                        }
                    }
                }
            }

            FormField(
                "Product name *",
                name,
                { name = it },
                "e.g. Crystal Glass Beads"
            )

            Column {
                Text("Category *", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))

                Box {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.name
                            ?: "Select category",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                categoryMenu = true
                            }) {
                                Icon(Icons.Rounded.ExpandMore, null)
                            }
                        },
                        shape = RoundedCornerShape(14.dp)
                    )

                    DropdownMenu(
                        expanded = categoryMenu,
                        onDismissRequest = { categoryMenu = false }
                    ) {
                        categories.forEach {
                            DropdownMenuItem(
                                text = { Text(it.name) },
                                onClick = {
                                    categoryId = it.id
                                    categoryMenu = false
                                }
                            )
                        }
                    }
                }
            }

            FormField("HSN code *", hsn, { hsn = it }, "HSN code")

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    FormField(
                        "Purchase price (₹) *",
                        purchase,
                        { purchase = it },
                        "0.00",
                        number = true
                    )
                }
                Column(Modifier.weight(1f)) {
                    FormField(
                        "Selling price (₹) *",
                        selling,
                        { selling = it },
                        "0.00",
                        number = true
                    )
                }
            }

            if (product == null) {
                FormField(
                    "Initial stock (boxes)",
                    stock,
                    { stock = it },
                    "0",
                    number = true
                )
            } else {
                Text(
                    "Current stock: ${product.currentStock} boxes. Use Stock Management to change it.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            FormField(
                "Minimum stock alert (boxes)",
                minimum,
                { minimum = it },
                "5",
                number = true
            )

            FormField(
                "Description",
                description,
                { description = it },
                "Optional product details",
                singleLine = false
            )

            error?.let { err ->
                Text(err, color = Danger, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (saving) return@Button

                    val p = purchase.toDoubleOrNull()
                    val s = selling.toDoubleOrNull()
                    val q = stock.toIntOrNull()
                    val min = minimum.toIntOrNull()

                    when {
                        name.isBlank() -> error = "Enter a product name."
                        categoryId == 0L -> error = "Select a category."
                        hsn.isBlank() -> error = "Enter an HSN code."
                        p == null || p < 0 -> error = "Enter a valid purchase price."
                        s == null || s < 0 -> error = "Enter a valid selling price."
                        min == null || min < 0 -> error = "Enter a valid minimum stock."
                        product == null && (q == null || q < 0) ->
                            error = "Enter a valid initial stock."
                        else -> {
                            error = null
                            saving = true

                            scope.launch {
                                try {
                                    if (product == null) {
                                        val code = "ST-" +
                                                System.currentTimeMillis()
                                                    .toString().takeLast(7)

                                        repository.addProduct(
                                            Product(
                                                productCode = code,
                                                name = name.trim(),
                                                categoryId = categoryId,
                                                hsnCode = hsn.trim(),
                                                purchasePrice = p,
                                                sellingPrice = s,
                                                currentStock = q ?: 0,
                                                minimumStock = min,
                                                imageUri = imageUri,
                                                description = description.trim()
                                            )
                                        )
                                    } else {
                                        repository.editProduct(
                                            product.copy(
                                                name = name.trim(),
                                                categoryId = categoryId,
                                                hsnCode = hsn.trim(),
                                                purchasePrice = p,
                                                sellingPrice = s,
                                                minimumStock = min,
                                                imageUri = imageUri,
                                                description = description.trim()
                                            )
                                        )
                                    }

                                    onSave()
                                } catch (e: Exception) {
                                    error = e.message ?: "Unable to save product."
                                    saving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !saving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Burgundy
                )
            ) {
                Text(
                    if (saving) "Saving..." else
                        if (product == null) "Create product" else "Save changes"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockSheet(
    product: Product,
    repository: InventoryRepository,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf("IN") }
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Canvas,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp)
                .padding(bottom = 35.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                "Manage stock",
                fontSize = 27.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(product.name, color = Muted, fontSize = 14.sp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(17.dp))
                    .background(SurfaceWhite)
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AVAILABLE STOCK", color = Muted, fontSize = 10.sp)
                    Text(
                        "${product.currentStock} boxes",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    Icons.Rounded.Warehouse,
                    null,
                    tint = Burgundy,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text("Select operation", fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(
                    "IN" to "Stock In",
                    "OUT" to "Stock Out",
                    "ADJUSTMENT" to "Adjust"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = type == value,
                        onClick = {
                            type = value
                            quantity = ""
                            error = null
                        },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Text(
                when (type) {
                    "IN" -> "Number of boxes received"
                    "OUT" -> "Number of boxes removed"
                    else -> "Actual physical stock count"
                },
                color = Muted,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    quantity = it.filter(Char::isDigit)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(if (type == "ADJUSTMENT") "New stock quantity" else "Quantity")
                },
                suffix = { Text("boxes") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                shape = RoundedCornerShape(15.dp),
                singleLine = true
            )

            FormField(
                "Reason / Note",
                reason,
                { reason = it },
                when (type) {
                    "IN" -> "Purchase received"
                    "OUT" -> "Stock issued"
                    else -> "Physical stock correction"
                }
            )

            if (type == "ADJUSTMENT") {
                Text(
                    "This replaces the current quantity with the physical count.",
                    color = Warning,
                    fontSize = 12.sp
                )
            }

            error?.let { err ->
                Text(err, color = Danger, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (saving) return@Button

                    val amount = quantity.toIntOrNull()
                    if (amount == null || (type != "ADJUSTMENT" && amount <= 0) || (type == "ADJUSTMENT" && amount < 0)) {
                        error = if (type == "ADJUSTMENT") {
                            "Enter a valid stock quantity (0 or more)."
                        } else {
                            "Enter a quantity greater than zero."
                        }
                        return@Button
                    }

                    saving = true
                    error = null

                    scope.launch {
                        try {
                            repository.updateStock(
                                productId = product.id,
                                type = type,
                                quantity = amount,
                                reason = reason.ifBlank {
                                    when (type) {
                                        "IN" -> "Stock received"
                                        "OUT" -> "Stock issued"
                                        else -> "Stock adjustment"
                                    }
                                }
                            )
                            onSave()
                        } catch (e: Exception) {
                            error = e.message ?: "Stock update failed."
                            saving = false
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (type) {
                        "OUT" -> Warning
                        "ADJUSTMENT" -> Ink
                        else -> Burgundy
                    }
                )
            ) {
                Text(if (saving) "Updating..." else "Confirm stock update")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailSheet(
    product: Product,
    categories: List<Category>,
    movements: List<StockMovement>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Canvas,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp)
                .padding(bottom = 35.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProductImage(product.imageUri, 85.dp)
                Spacer(Modifier.width(15.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        product.name,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(product.productCode, color = Muted, fontSize = 12.sp)
                    Text(
                        categories.find { it.id == product.categoryId }?.name
                            ?: "Uncategorized",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Available boxes",
                    value = product.currentStock.toString(),
                    icon = Icons.Rounded.Warehouse
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Minimum stock",
                    value = product.minimumStock.toString(),
                    icon = Icons.Rounded.Warning,
                    tint = Warning
                )
            }

            DetailLine("HSN code", product.hsnCode)
            DetailLine("Purchase price", "₹${product.purchasePrice}")
            DetailLine("Selling price", "₹${product.sellingPrice}")
            DetailLine("Category", categories.find {
                it.id == product.categoryId
            }?.name ?: "Uncategorized")
            DetailLine("Product ID", product.productCode)

            if (product.description.isNotBlank()) {
                Text("Description", color = Muted, fontSize = 12.sp)
                Text(product.description, fontSize = 14.sp)
            }

            Text(
                "Recent movements",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (movements.isEmpty()) {
                Text("No stock movement records.", color = Muted)
            } else {
                movements.take(8).forEach {
                    MovementRow(it, dateFormat)
                }
            }

            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.Edit, null)
                Spacer(Modifier.width(7.dp))
                Text("Edit product")
            }

            OutlinedButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Danger
                )
            ) {
                Icon(Icons.Rounded.Archive, null)
                Spacer(Modifier.width(7.dp))
                Text("Archive product")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Archive product?") },
            text = {
                Text("This product will be hidden from the active catalog. Its historical records will remain.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) {
                    Text("Archive", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Muted, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun HistoryScreen(
    modifier: Modifier = Modifier,
    movements: List<StockMovement>,
    dateFormat: SimpleDateFormat
) {
    var filter by remember { mutableStateOf("ALL") }

    val filtered = movements.filter {
        filter == "ALL" || it.type == filter
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            Text(
                "Every movement, accounted for.",
                fontSize = 14.sp,
                color = Muted
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "ALL" to "All",
                    "IN" to "Stock In",
                    "OUT" to "Stock Out",
                    "ADJUSTMENT" to "Adjustments"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = filter == value,
                        onClick = { filter = value },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }

        item {
            Text(
                "${filtered.size} records",
                color = Muted,
                fontSize = 12.sp
            )
        }

        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "No activity yet",
                    message = "Stock changes will appear here."
                )
            }
        }

        items(filtered, key = { it.id }) {
            MovementRow(it, dateFormat)
        }
    }
}

@Composable
private fun MovementRow(
    movement: StockMovement,
    dateFormat: SimpleDateFormat
) {
    val color = when (movement.type) {
        "IN" -> Emerald
        "OUT" -> Warning
        else -> Ink
    }

    val label = when (movement.type) {
        "IN" -> "Stock received"
        "OUT" -> "Stock removed"
        else -> "Stock adjusted"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(SurfaceWhite)
            .padding(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (movement.type) {
                            "IN" -> EmeraldLight
                            "OUT" -> WarningLight
                            else -> Canvas
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (movement.type) {
                        "IN" -> Icons.Rounded.Add
                        "OUT" -> Icons.Rounded.Remove
                        else -> Icons.Rounded.SwapVert
                    },
                    null,
                    tint = color
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    movement.productName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(label, fontSize = 11.sp, color = Muted)
            }

            Text(
                when (movement.type) {
                    "IN" -> "+${movement.quantity}"
                    "OUT" -> "-${movement.quantity}"
                    else -> "%+d".format(movement.quantity)
                },
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 17.sp
            )
        }

        Spacer(Modifier.height(11.dp))
        HorizontalDivider(color = BorderSoft)
        Spacer(Modifier.height(9.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                dateFormat.format(Date(movement.timestamp)),
                color = Muted,
                fontSize = 10.sp
            )
            Text(
                "${movement.previousStock} → ${movement.newStock} boxes",
                color = Ink,
                fontSize = 10.sp
            )
        }

        if (movement.reason.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text(movement.reason, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MoreScreen(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    stockValue: Double,
    products: List<Product>,
    movements: List<StockMovement>,
    onCategories: () -> Unit
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                CsvExporter.export(
                    context,
                    uri,
                    products,
                    categories,
                    movements
                )
                Toast.makeText(
                    context,
                    "Inventory exported successfully.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(21.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Business tools", fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Text("Manage your inventory workspace.", color = Muted, fontSize = 13.sp)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(21.dp))
                    .background(Burgundy)
                    .padding(20.dp)
            ) {
                Text("SHIV TRADERS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "₹${String.format(Locale.getDefault(), "%,.2f", stockValue)}",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Current inventory purchase valuation", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        item {
            ToolRow(Icons.Rounded.Category, "Categories", "${categories.size} categories", onCategories)
        }

        item {
            ToolRow(
                Icons.Rounded.FileDownload,
                "Export inventory",
                "Save products and stock history as CSV"
            ) {
                exportLauncher.launch("shiv_traders_inventory.csv")
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(19.dp))
                    .background(SurfaceWhite)
                    .padding(17.dp)
            ) {
                Text("About", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(9.dp))
                Text("Shiv Traders", fontSize = 14.sp)
                Text("Inventory Management · Version 1.0", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(9.dp))
                Text(
                    "All inventory information is stored locally on this device.",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ToolRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Burgundy, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Muted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    repository: InventoryRepository,
    categories: List<Category>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Canvas,
        dragHandle = { BottomSheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 35.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Categories",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )

            categories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceWhite)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Category,
                        null,
                        tint = Burgundy
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        category.name,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    repository.removeCategory(category.id)
                                    Toast.makeText(context, "Category removed", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Cannot remove category", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete category",
                            tint = Danger.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text("Add a category", fontWeight = FontWeight.SemiBold)

            FormField("Category name", name, { name = it }, "Category name")
            FormField(
                "Description",
                description,
                { description = it },
                "Optional"
            )

            error?.let { err ->
                Text(err, color = Danger, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Enter a category name."
                    } else {
                        scope.launch {
                            try {
                                repository.addCategory(
                                    Category(
                                        name = name.trim(),
                                        description = description.trim()
                                    )
                                )
                                name = ""
                                description = ""
                                error = null
                            } catch (_: Exception) {
                                error = "Category already exists or could not be saved."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("Add category")
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    number: Boolean = false,
    singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            label,
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (number) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Text
                }
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Emerald,
                unfocusedBorderColor = BorderSoft,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            )
        )
    }
}

@Composable
private fun BottomSheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 10.dp)
            .width(38.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(Color(0xFFD5D8D1))
    )
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(21.dp))
            .background(SurfaceWhite)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            tint = Emerald.copy(alpha = 0.75f),
            modifier = Modifier.size(35.dp)
        )

        Spacer(Modifier.height(13.dp))

        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            message,
            color = Muted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductImage(product.imageUri, 48.dp)

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                product.productCode,
                fontSize = 10.sp,
                color = Muted
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${product.currentStock} boxes",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (product.currentStock == 0) Danger else Warning
            )
            Text("Minimum ${product.minimumStock}", fontSize = 10.sp, color = Muted)
        }
    }
}


