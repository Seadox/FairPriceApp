package com.seadox.fairprice.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.seadox.fairprice.R
import com.seadox.fairprice.api.Client
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.items.ProductPriceInChain
import com.seadox.fairprice.utils.DataStoreManager
import com.seadox.fairprice.utils.Utils
import java.text.DecimalFormat

@Preview(showBackground = true)
@Composable
fun PreviewDetailPage() {
    DetailPage(product = ProductItem())
}

@Composable
fun DetailPage(product: ProductItem?) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val favorStores = DataStoreManager.getInstance().getStores()

    val favorStoresPrices = product?.pricesInChains?.filter { store ->
        val id = "${store.chainId}_${store.storeId}"
        favorStores.any { it == id }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            product?.let {
                ProductDetails(
                    product = it,
                    selectedTabIndex = selectedTabIndex,
                    favorStores = if (selectedTabIndex == 0) favorStoresPrices
                        ?: emptyList() else it.pricesInChains
                )
                if (favorStoresPrices?.isNotEmpty() == true)
                    StoresDetailsTabs(
                        stores = if (selectedTabIndex == 0) favorStoresPrices else it.pricesInChains,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                else
                    StoresDetails(stores = it.pricesInChains)
            }
        }
    }
}

@Composable
fun ProductDetails(
    product: ProductItem,
    selectedTabIndex: Int,
    favorStores: List<ProductPriceInChain> = emptyList()
) {
    val maxPrice = favorStores.maxByOrNull { it.price }?.price ?: 0.0
    val minPrice = favorStores.minByOrNull { it.price }?.price ?: 0.0
    val priceDifference = ((maxPrice - minPrice) / maxPrice) * 100

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = product.manufacturerItemDescription,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Text(
            text = "ארץ מוצא: ${product.manufactureCountry}",
            fontSize = 16.sp,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "קוד מוצר: ${product.itemCode}",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Product Image Column
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(
                    if (Utils.isUrl(product.imageUrl)) product.imageUrl else Utils.base64ToByteArray(
                        product.imageUrl
                    )
                )
                .size(Size.ORIGINAL)
                .build(),
            placeholder = painterResource(id = R.drawable.loading),
            error = painterResource(id = R.drawable.no_image),
            contentDescription = "Product Image",
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )

        // Product Details Column
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = when {
                    favorStores.isNotEmpty() && selectedTabIndex == 0 -> "בסניפים המועדפים"
                    favorStores.isNotEmpty() && selectedTabIndex == 1 -> "בעיר המועדפת"
                    else -> "בכל הרשתות"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.fillMaxWidth()
            )
            // Cheapest Price
            Text(
                text = "המחיר הזול ביותר",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (favorStores.isEmpty()) "${product.cheapestPrice} ₪" else "$minPrice ₪",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CB86A),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Most Expensive Price
            Text(
                text = "המחיר היקר ביותר",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (favorStores.isEmpty()) "${product.mostExpensivePrice} ₪" else "$maxPrice ₪",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEE3665),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            // Percentage Difference
            Text(
                text = "הפרש מחירים",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (favorStores.isEmpty()) "${DecimalFormat("#.0").format(product.priceDifference)}%"
                else "${DecimalFormat("#.0").format(priceDifference)}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF95929),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StoresDetailsTabs(
    stores: List<ProductPriceInChain>,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF000000)
                )
            },
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Text(
                        text = "מחירים בסניפים מועדפים",
                        color = Color(0xFF000000),
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Text(
                        text = "מחירים בעיר המועדפת",
                        color = Color(0xFF000000),
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        when (selectedTabIndex) {
            0 -> StoresDetails(stores)
            1 -> StoresDetails(stores)
        }
    }
}

@Composable
fun StoresDetails(stores: List<ProductPriceInChain>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stores.size) { index ->
                StorePriceItem(
                    storePrices = stores[index]
                )
            }
        }
    }
}

@Composable
fun StorePriceItem(storePrices: ProductPriceInChain) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F6F9), shape = MaterialTheme.shapes.medium)
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp, 8.dp, 16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (storePrices.address.isNotEmpty()) "${storePrices.chainName} - ${storePrices.address}, ${storePrices.city}" else storePrices.chainName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "מחיר: ${storePrices.price} ₪",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        if (storePrices.promo?.isNotEmpty() == true) {
            PromoIcon(storePrices.promo)
        }
    }
}

@Composable
fun PromoIcon(description: String) {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            showDialog = true
        },
        modifier = Modifier
            .size(42.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.promo),
            colorFilter = ColorFilter.tint(Color(0xFFEE3665)),
            contentDescription = "Promo",
            modifier = Modifier.size(24.dp)
        )
    }

    if (showDialog) {
        PromoDialog(description, onDismiss = { showDialog = false })
    }
}

@Composable
fun PromoDialog(description: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "מידע על המבצע",
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = description,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEE3665),
                ),
                onClick = {
                    onDismiss()
                }
            ) {
                Text("סגור")
            }
        },
    )
}

fun getProductDetails(
    code: String,
    successCallback: (ProductItem) -> Unit,
    failureCallback: (String) -> Unit
) {
    Client.getProductByCode(
        code = code,
        successCallback = { product ->
            successCallback(product)
        },
        failureCallback = { error ->
            failureCallback(error)
        }
    )
}