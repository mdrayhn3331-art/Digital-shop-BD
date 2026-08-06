package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Order
import com.example.data.models.Payment
import com.example.data.models.Product
import com.example.data.supabase.ShopRepository
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.TechBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    repository: ShopRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin by repository.isAdmin.collectAsState()
    val products by repository.products.collectAsState()
    val orders by repository.orders.collectAsState()
    val payments by repository.payments.collectAsState()
    val settings by repository.settings.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Products, 1: Orders, 2: Payments, 3: Settings
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    if (!isAdmin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Access Restricted", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Only admin email mdrayhn3331@gmail.com can access this control panel.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                    ) {
                        Text("Back to Shop")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Control Panel", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Rayhan Digital Shop", fontSize = 12.sp, color = NagadOrange)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(
                            onClick = { showAddProductDialog = true },
                            modifier = Modifier.testTag("admin_add_product_top_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Product", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatBadge(title = "Products", value = products.size.toString())
                StatBadge(title = "Orders", value = orders.size.toString())
                StatBadge(title = "Payments", value = payments.size.toString())
            }

            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkNavy,
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Products", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = NagadOrange,
                    unselectedContentColor = Color.LightGray,
                    modifier = Modifier.testTag("admin_tab_products")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Orders", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = NagadOrange,
                    unselectedContentColor = Color.LightGray,
                    modifier = Modifier.testTag("admin_tab_orders")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Payments", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = NagadOrange,
                    unselectedContentColor = Color.LightGray,
                    modifier = Modifier.testTag("admin_tab_payments")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Settings", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = NagadOrange,
                    unselectedContentColor = Color.LightGray,
                    modifier = Modifier.testTag("admin_tab_settings")
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTab) {
                    0 -> AdminProductsTab(
                        products = products,
                        onEdit = { editingProduct = it },
                        onDelete = { repository.deleteProduct(it.id) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() } },
                        onAdd = { showAddProductDialog = true }
                    )
                    1 -> AdminOrdersTab(
                        orders = orders,
                        onUpdateStatus = { id, status -> repository.updateOrderStatus(id, status) }
                    )
                    2 -> AdminPaymentsTab(
                        payments = payments,
                        onUpdateStatus = { id, status -> repository.updatePaymentStatus(id, status) }
                    )
                    3 -> AdminSettingsTab(
                        settings = settings,
                        onSave = { name, email, phone, bkash, nagad, loc ->
                            repository.updateShopSettings(name, email, phone, bkash, nagad, loc) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog || editingProduct != null) {
        ProductDialog(
            product = editingProduct,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { p ->
                if (editingProduct != null) {
                    repository.updateProduct(p) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                } else {
                    repository.addProduct(p) { _, msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                }
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }
}

@Composable
fun StatBadge(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = title, fontSize = 11.sp, color = Color.LightGray)
    }
}

// TAB 1: PRODUCTS
@Composable
fun AdminProductsTab(
    products: List<Product>,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onAdd: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_add_new_product_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Product", fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.image,
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("৳ ${product.price.toInt()} | Stock: ${product.stock}", fontSize = 12.sp, color = TechBlue)
                            Text("Category: ${product.category}", fontSize = 11.sp, color = Color.Gray)
                        }

                        IconButton(onClick = { onEdit(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TechBlue)
                        }

                        IconButton(onClick = { onDelete(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// TAB 2: ORDERS
@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    onUpdateStatus: (String, String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer orders found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Order #${order.id}", fontWeight = FontWeight.Bold)
                            StatusChip(status = order.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Products: ${order.products}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Total Amount: ৳ ${order.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = TechBlue, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Update Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatusButton("Processing", order.status == "Processing") { onUpdateStatus(order.id, "Processing") }
                            StatusButton("Delivered", order.status == "Delivered") { onUpdateStatus(order.id, "Delivered") }
                            StatusButton("Cancelled", order.status == "Cancelled") { onUpdateStatus(order.id, "Cancelled") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) TechBlue else Color.Transparent,
            contentColor = if (isSelected) Color.White else TechBlue
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 10.sp)
    }
}

// TAB 3: PAYMENTS
@Composable
fun AdminPaymentsTab(
    payments: List<Payment>,
    onUpdateStatus: (String, String) -> Unit
) {
    if (payments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer payments submitted yet", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(payments, key = { it.id }) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Method: ${payment.paymentMethod}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (payment.status) {
                                            "Approved" -> Color(0xFFDCFCE7)
                                            "Rejected" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(payment.status, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Sender #: ${payment.senderNumber}", fontSize = 12.sp)
                        Text("TrxID: ${payment.transactionId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Amount: ৳ ${payment.amount.toInt()}", fontWeight = FontWeight.ExtraBold, color = TechBlue, fontSize = 15.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onUpdateStatus(payment.id, "Approved") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve")
                            }

                            Button(
                                onClick = { onUpdateStatus(payment.id, "Rejected") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 4: SHOP SETTINGS
@Composable
fun AdminSettingsTab(
    settings: com.example.data.models.ShopSettings,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var shopName by remember { mutableStateOf(settings.shopName) }
    var email by remember { mutableStateOf(settings.email) }
    var phone by remember { mutableStateOf(settings.phone) }
    var bkash by remember { mutableStateOf(settings.bkash) }
    var nagad by remember { mutableStateOf(settings.nagad) }
    var location by remember { mutableStateOf(settings.location) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Shop Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("Shop Name") },
            modifier = Modifier.fillMaxWidth().testTag("settings_shop_name_input"),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Admin Gmail") },
            modifier = Modifier.fillMaxWidth().testTag("settings_email_input"),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth().testTag("settings_phone_input"),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = bkash,
            onValueChange = { bkash = it },
            label = { Text("bKash Number") },
            modifier = Modifier.fillMaxWidth().testTag("settings_bkash_input"),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = nagad,
            onValueChange = { nagad = it },
            label = { Text("Nagad Number") },
            modifier = Modifier.fillMaxWidth().testTag("settings_nagad_input"),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Shop Location") },
            modifier = Modifier.fillMaxWidth().testTag("settings_location_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSave(shopName, email, phone, bkash, nagad, location) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_shop_settings_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Shop Settings", fontWeight = FontWeight.Bold)
        }
    }
}

// Add/Edit Product Dialog
@Composable
fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "1000") }
    var category by remember { mutableStateOf(product?.category ?: "Gadgets") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var image by remember { mutableStateOf(product?.image ?: "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&q=80") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_name")
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_price")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Gadgets, Electronics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_category")
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_stock")
                )

                OutlinedTextField(
                    value = image,
                    onValueChange = { image = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_image")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_product_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pPrice = price.toDoubleOrNull() ?: 1000.0
                    val pStock = stock.toIntOrNull() ?: 10
                    val p = (product ?: Product()).copy(
                        name = name,
                        price = pPrice,
                        category = category,
                        description = description,
                        image = image,
                        stock = pStock
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                modifier = Modifier.testTag("dialog_save_product_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
