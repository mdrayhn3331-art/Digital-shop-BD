package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Product
import com.example.data.supabase.ShopRepository
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BentoActivePill
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoDarkPurple
import com.example.ui.theme.BentoDarkText
import com.example.ui.theme.BentoSubtext
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NagadOrange

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

enum class Screen {
    Home,
    ProductDetail,
    Cart,
    Checkout,
    Orders,
    Profile,
    Auth,
    Admin
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val repository = remember { ShopRepository(context) }
    val cartItems by repository.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BentoBackground,
        bottomBar = {
            // Hide bottom bar on detail/checkout/auth screens if needed, or show everywhere for quick access
            if (currentScreen in listOf(Screen.Home, Screen.Cart, Screen.Orders, Screen.Profile, Screen.Admin)) {
                BentoBottomNavigationBar(
                    currentScreen = currentScreen,
                    cartCount = cartCount,
                    onNavigate = { screen -> currentScreen = screen }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    repository = repository,
                    onProductClick = { product ->
                        selectedProduct = product
                        currentScreen = Screen.ProductDetail
                    },
                    onCartClick = { currentScreen = Screen.Cart },
                    onProfileClick = { currentScreen = Screen.Profile },
                    onAdminClick = { currentScreen = Screen.Admin }
                )

                Screen.ProductDetail -> selectedProduct?.let { product ->
                    ProductDetailScreen(
                        product = product,
                        repository = repository,
                        onBack = { currentScreen = Screen.Home },
                        onGoToCart = { currentScreen = Screen.Cart }
                    )
                } ?: run {
                    currentScreen = Screen.Home
                }

                Screen.Cart -> CartScreen(
                    repository = repository,
                    onBack = { currentScreen = Screen.Home },
                    onCheckout = { currentScreen = Screen.Checkout }
                )

                Screen.Checkout -> CheckoutScreen(
                    repository = repository,
                    onBack = { currentScreen = Screen.Cart },
                    onOrderSuccess = { currentScreen = Screen.Orders }
                )

                Screen.Orders -> OrderHistoryScreen(
                    repository = repository,
                    onBack = { currentScreen = Screen.Home }
                )

                Screen.Profile -> ProfileScreen(
                    repository = repository,
                    onBack = { currentScreen = Screen.Home },
                    onGoToAuth = { currentScreen = Screen.Auth },
                    onGoToOrders = { currentScreen = Screen.Orders },
                    onGoToAdmin = { currentScreen = Screen.Admin }
                )

                Screen.Auth -> AuthScreen(
                    repository = repository,
                    onAuthSuccess = { currentScreen = Screen.Profile },
                    onBack = { currentScreen = Screen.Profile }
                )

                Screen.Admin -> AdminPanelScreen(
                    repository = repository,
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }
    }
}

@Composable
fun BentoBottomNavigationBar(
    currentScreen: Screen,
    cartCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFFF3EDF7))
            .border(1.dp, BentoBorder)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BentoNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == Screen.Home,
                onClick = { onNavigate(Screen.Home) },
                tag = "nav_home"
            )

            BentoNavItem(
                icon = Icons.Default.ShoppingCart,
                label = "Cart",
                badgeCount = cartCount,
                isSelected = currentScreen == Screen.Cart,
                onClick = { onNavigate(Screen.Cart) },
                tag = "nav_cart"
            )

            BentoNavItem(
                icon = Icons.Default.ReceiptLong,
                label = "Orders",
                isSelected = currentScreen == Screen.Orders,
                onClick = { onNavigate(Screen.Orders) },
                tag = "nav_orders"
            )

            BentoNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == Screen.Profile || currentScreen == Screen.Admin,
                onClick = { onNavigate(Screen.Profile) },
                tag = "nav_profile"
            )
        }
    }
}

@Composable
fun BentoNavItem(
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BentoActivePill else Color.Transparent)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = NagadOrange,
                            contentColor = Color.White
                        ) {
                            Text(badgeCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) BentoDarkPurple else BentoSubtext,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BentoDarkText else BentoSubtext
        )
    }
}

