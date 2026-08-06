package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.Product
import com.example.data.supabase.ShopRepository
import com.example.ui.theme.BentoActivePill
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoDarkPurple
import com.example.ui.theme.BentoDarkText
import com.example.ui.theme.BentoLightPurpleContainer
import com.example.ui.theme.BentoPrimaryPurple
import com.example.ui.theme.BentoStatusBanner
import com.example.ui.theme.BentoSubtext
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BkashPink
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.OnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ShopRepository,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val products by repository.products.collectAsState()
    val cartItems by repository.cartItems.collectAsState()
    val settings by repository.settings.collectAsState()
    val isAdmin by repository.isAdmin.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Gadgets", "Electronics", "Accessories", "Digital Services")
    val cartCount = cartItems.sumOf { it.quantity }

    val filteredProducts = products.filter { p ->
        val matchesCategory = (selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = p.name.contains(searchQuery, ignoreCase = true) || p.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Featured Hero Product for Bento Card
    val featuredProduct = products.firstOrNull { it.category == "Digital Services" } ?: products.firstOrNull()

    Scaffold(
        containerColor = BentoBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBackground,
                    titleContentColor = BentoDarkText,
                    actionIconContentColor = BentoDarkText
                ),
                title = {
                    Column {
                        Text(
                            text = settings.shopName.ifEmpty { "Rayhan Digital" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = settings.location.ifEmpty { "Chittagong, BD" },
                            fontSize = 12.sp,
                            color = BentoSubtext,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = onAdminClick,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BentoActivePill)
                                .testTag("admin_panel_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = BentoDarkPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BentoActivePill)
                            .testTag("cart_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = NagadOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text(cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = BentoDarkPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BentoActivePill)
                            .testTag("profile_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = BentoDarkPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BentoBackground)
        ) {
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search digital products...", fontSize = 14.sp, color = BentoSubtext) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoSubtext) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = BentoSubtext)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BentoSurface,
                        unfocusedContainerColor = BentoSurface,
                        focusedBorderColor = BentoPrimaryPurple,
                        unfocusedBorderColor = BentoBorder
                    )
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Bento Feature Hero Card (Spans 2 columns)
                if (searchQuery.isEmpty() && selectedCategory == "All" && featuredProduct != null) {
                    item(span = { GridItemSpan(2) }) {
                        BentoHeroCard(
                            product = featuredProduct,
                            onGetClick = { onProductClick(featuredProduct) }
                        )
                    }

                    // bKash / Nagad Status Bento Card (Spans 2 columns)
                    item(span = { GridItemSpan(2) }) {
                        BentoPaymentStatusCard(bkash = settings.bkash, nagad = settings.nagad)
                    }

                    // Categories Filter Header (Spans 2 columns)
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            text = category,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoPrimaryPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = BentoSurface,
                                        labelColor = BentoDarkText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = BentoBorder,
                                        selectedBorderColor = BentoPrimaryPurple
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("category_chip_$category")
                                )
                            }
                        }
                    }
                }

                // If filtering/searching, show Category chips at top
                if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            text = category,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoPrimaryPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = BentoSurface,
                                        labelColor = BentoDarkText
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.testTag("category_chip_$category")
                                )
                            }
                        }
                    }
                }

                // Product Items Grid
                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No products found", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoSubtext)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Try searching with different keywords.", fontSize = 12.sp, color = BentoSubtext)
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        BentoProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            onAddToCart = { repository.addToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BentoHeroCard(
    product: Product,
    onGetClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onGetClick() },
        colors = CardDefaults.cardColors(containerColor = BentoLightPurpleContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(BentoDarkPurple)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "EXCLUSIVE OFFER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = BentoDarkPurple,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.description.take(60) + if (product.description.length > 60) "..." else "",
                fontSize = 13.sp,
                color = BentoDarkPurple.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onGetClick,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoDarkPurple),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Get ৳${product.price.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = "🎨 Digital Key",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkPurple.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun BentoPaymentStatusCard(bkash: String, nagad: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoStatusBanner)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BkashPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("bK", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width((-6).dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NagadOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Na", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Instant Payment Support",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoDarkText
                    )
                    Text(
                        text = "bKash: $bkash | Nagad: $nagad",
                        fontSize = 10.sp,
                        color = BentoSubtext
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ONLINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnlineGreen
                )
            }
        }
    }
}

@Composable
fun BentoProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BentoBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BentoBackground)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoDarkPurple.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.category,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = BentoDarkText
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "৳ ${product.price.toInt()}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = BentoPrimaryPurple
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (product.stock > 0) "Stock: ${product.stock}" else "Out of stock",
                    fontSize = 10.sp,
                    color = if (product.stock > 0) OnlineGreen else Color(0xFFEF4444)
                )

                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoActivePill, contentColor = BentoDarkPurple),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

