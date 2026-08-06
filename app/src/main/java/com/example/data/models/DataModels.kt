package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "phone") val phone: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "image") val image: String = "",
    @Json(name = "category") val category: String = "Digital Products",
    @Json(name = "description") val description: String = "",
    @Json(name = "stock") val stock: Int = 10,
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    @Json(name = "productId") val productId: String = "",
    @Json(name = "productName") val productName: String = "",
    @Json(name = "price") val price: Double = 0.0,
    @Json(name = "quantity") val quantity: Int = 1,
    @Json(name = "image") val image: String = ""
)

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "id") val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "products") val products: String = "", // JSON string storing list of OrderItem
    @Json(name = "total_price") val totalPrice: Double = 0.0,
    @Json(name = "status") val status: String = "Pending", // Pending, Processing, Delivered, Cancelled
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class Payment(
    @Json(name = "id") val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "payment_method") val paymentMethod: String = "", // bKash, Nagad, Cash on Delivery
    @Json(name = "sender_number") val senderNumber: String = "",
    @Json(name = "transaction_id") val transactionId: String = "",
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "status") val status: String = "Pending", // Pending, Approved, Rejected
    @Json(name = "created_at") val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class ShopSettings(
    @Json(name = "id") val id: String = "1",
    @Json(name = "shop_name") val shopName: String = "Rayhan Digital Shop",
    @Json(name = "email") val email: String = "mdrayhn3331@gmail.com",
    @Json(name = "phone") val phone: String = "01876872469",
    @Json(name = "bkash") val bkash: String = "01876872469",
    @Json(name = "nagad") val nagad: String = "01876872469",
    @Json(name = "location") val location: String = "Chittagong, Fatikchhari, Khagrachhari"
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)
