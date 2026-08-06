package com.example.data.supabase

import android.content.Context
import android.util.Log
import com.example.data.models.CartItem
import com.example.data.models.Order
import com.example.data.models.Payment
import com.example.data.models.Product
import com.example.data.models.ShopSettings
import com.example.data.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ShopRepository(private val context: Context) {

    private val supabase = SupabaseClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    // State Flows
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _settings = MutableStateFlow(ShopSettings())
    val settings: StateFlow<ShopSettings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Initial load
        refreshData()
    }

    fun clearMessage() {
        _message.value = null
    }

    fun refreshData() {
        scope.launch {
            _isLoading.value = true
            try {
                // Fetch Settings
                val shopSettings = supabase.getSettings()
                _settings.value = shopSettings

                // Fetch Products
                val remoteProducts = supabase.getProducts()
                if (remoteProducts.isNotEmpty()) {
                    _products.value = remoteProducts
                } else {
                    // Seed initial products if DB is empty
                    val defaultProducts = getInitialProducts()
                    _products.value = defaultProducts
                    defaultProducts.forEach { supabase.addProduct(it) }
                }

                // Fetch Orders and Payments if logged in
                val user = _currentUser.value
                if (user != null) {
                    val userIdParam = if (_isAdmin.value) null else user.id
                    _orders.value = supabase.getOrders(userIdParam)
                    _payments.value = supabase.getPayments(userIdParam)
                } else if (_isAdmin.value) {
                    _orders.value = supabase.getOrders(null)
                    _payments.value = supabase.getPayments(null)
                }
            } catch (e: Exception) {
                Log.e("ShopRepository", "Error refreshing data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- AUTHENTICATION ---

    fun login(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            val result = supabase.signIn(email, pass)
            _isLoading.value = false

            result.onSuccess { (user, token) ->
                _currentUser.value = user
                _isLoggedIn.value = true
                _isAdmin.value = (user.email.lowercase() == SupabaseConfig.ADMIN_EMAIL.lowercase())
                refreshData()
                onResult(true, "Login successful!")
            }.onFailure { err ->
                onResult(false, err.message ?: "Login failed. Check credentials.")
            }
        }
    }

    fun register(name: String, email: String, phone: String, address: String, pass: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            val result = supabase.signUp(email, pass, name, phone, address)
            _isLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                _isLoggedIn.value = true
                _isAdmin.value = (user.email.lowercase() == SupabaseConfig.ADMIN_EMAIL.lowercase())
                refreshData()
                onResult(true, "Registration successful!")
            }.onFailure { err ->
                onResult(false, err.message ?: "Registration failed.")
            }
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            val res = supabase.sendPasswordReset(email)
            _isLoading.value = false
            onResult(true, "Password reset link sent to $email (if registered).")
        }
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _isAdmin.value = false
        _cartItems.value = emptyList()
        _orders.value = emptyList()
        _payments.value = emptyList()
    }

    // --- CART ---

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(product = product, quantity = quantity))
        }
        _cartItems.value = currentList
        _message.value = "${product.name} added to Cart!"
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // --- CHECKOUT & ORDERS ---

    fun placeOrder(
        paymentMethod: String,
        senderNumber: String,
        transactionId: String,
        amount: Double,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please login to place an order.")
            return
        }

        val cart = _cartItems.value
        if (cart.isEmpty()) {
            onResult(false, "Your cart is empty.")
            return
        }

        scope.launch {
            _isLoading.value = true

            // Convert cart items to simplified string representation
            val productsSummary = cart.joinToString("; ") { "${it.product.name} (x${it.quantity}) - ৳${it.product.price * it.quantity}" }

            val totalAmount = cart.sumOf { it.product.price * it.quantity }

            // 1. Create Order
            val newOrder = Order(
                id = UUID.randomUUID().toString().take(8),
                userId = user.id,
                products = productsSummary,
                totalPrice = totalAmount,
                status = "Pending",
                createdAt = System.currentTimeMillis().toString()
            )

            val orderRes = supabase.addOrder(newOrder)

            // 2. Create Payment Record
            val newPayment = Payment(
                id = UUID.randomUUID().toString().take(8),
                userId = user.id,
                paymentMethod = paymentMethod,
                senderNumber = senderNumber,
                transactionId = transactionId.ifEmpty { "COD-${System.currentTimeMillis().toString().takeLast(6)}" },
                amount = amount,
                status = "Pending",
                createdAt = System.currentTimeMillis().toString()
            )

            val paymentRes = supabase.addPayment(newPayment)

            // Clear Cart and refresh
            clearCart()
            refreshData()

            _isLoading.value = false
            onResult(true, "Order placed successfully! Transaction ID: ${newPayment.transactionId}")
        }
    }

    // --- ADMIN ACTIONS ---

    fun addProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            val pId = if (product.id.isEmpty()) UUID.randomUUID().toString() else product.id
            val newP = product.copy(id = pId, createdAt = System.currentTimeMillis().toString())
            val res = supabase.addProduct(newP)
            if (res.getOrDefault(false)) {
                _products.value = listOf(newP) + _products.value
                onResult(true, "Product added successfully!")
            } else {
                // Local fallback update
                _products.value = listOf(newP) + _products.value
                onResult(true, "Product added locally.")
            }
            _isLoading.value = false
        }
    }

    fun updateProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            supabase.updateProduct(product)
            val list = _products.value.toMutableList()
            val idx = list.indexOfFirst { it.id == product.id }
            if (idx >= 0) {
                list[idx] = product
                _products.value = list
            }
            _isLoading.value = false
            onResult(true, "Product updated!")
        }
    }

    fun deleteProduct(productId: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            _isLoading.value = true
            supabase.deleteProduct(productId)
            _products.value = _products.value.filter { it.id != productId }
            _isLoading.value = false
            onResult(true, "Product deleted!")
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        scope.launch {
            supabase.updateOrderStatus(orderId, status)
            val currentList = _orders.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == orderId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(status = status)
                _orders.value = currentList
            }
        }
    }

    fun updatePaymentStatus(paymentId: String, status: String) {
        scope.launch {
            supabase.updatePaymentStatus(paymentId, status)
            val currentList = _payments.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == paymentId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(status = status)
                _payments.value = currentList
            }
        }
    }

    fun updateShopSettings(
        shopName: String,
        email: String,
        phone: String,
        bkash: String,
        nagad: String,
        location: String,
        onResult: (Boolean, String) -> Unit
    ) {
        scope.launch {
            _isLoading.value = true
            val newSettings = ShopSettings(
                id = "1",
                shopName = shopName,
                email = email,
                phone = phone,
                bkash = bkash,
                nagad = nagad,
                location = location
            )
            supabase.updateSettings(newSettings)
            _settings.value = newSettings
            _isLoading.value = false
            onResult(true, "Shop settings updated successfully!")
        }
    }

    private fun getInitialProducts(): List<Product> {
        return listOf(
            Product(
                id = "p1",
                name = "Wireless Earbuds Pro",
                price = 1850.0,
                image = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500&q=80",
                category = "Gadgets",
                description = "Active Noise Cancellation, 30H Battery Life, Deep Bass Sound, Bluetooth 5.3.",
                stock = 25
            ),
            Product(
                id = "p2",
                name = "Smart Watch Ultra 8",
                price = 2400.0,
                image = "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=500&q=80",
                category = "Electronics",
                description = "2.0-inch HD Curved Display, Heart Rate & SpO2 Monitor, Wireless Charging, Alloy Case.",
                stock = 15
            ),
            Product(
                id = "p3",
                name = "65W GaN Fast Charger",
                price = 1250.0,
                image = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=500&q=80",
                category = "Accessories",
                description = "Dual Type-C & USB-A Ports, Ultra Fast PD Charging for Laptops, Smartphones, and Tablets.",
                stock = 40
            ),
            Product(
                id = "p4",
                name = "Digital Windows & Office License",
                price = 950.0,
                image = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                category = "Digital Services",
                description = "Original Genuine Lifetime Digital Product Activation Code. Instant email delivery.",
                stock = 100
            ),
            Product(
                id = "p5",
                name = "RGB Gaming Headset 7.1",
                price = 2200.0,
                image = "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=500&q=80",
                category = "Accessories",
                description = "7.1 Surround Sound, Noise Isolating Mic, Breathable Memory Foam Earcups, Dynamic RGB.",
                stock = 18
            ),
            Product(
                id = "p6",
                name = "Mechanical Keyboard RGB",
                price = 3200.0,
                image = "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500&q=80",
                category = "Electronics",
                description = "Custom Hot-swappable Switches, Per-key RGB Backlighting, Tactile Mechanical feel.",
                stock = 12
            ),
            Product(
                id = "p7",
                name = "10000mAh Magnetic Power Bank",
                price = 1650.0,
                image = "https://images.unsplash.com/photo-1609592424074-9f8bc2a2c6d4?w=500&q=80",
                category = "Gadgets",
                description = "Wireless Magnetic Fast Snap Charging, Compact Design, LED Battery Level Indicator.",
                stock = 30
            )
        )
    }
}
