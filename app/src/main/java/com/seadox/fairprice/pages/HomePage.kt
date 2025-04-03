package com.seadox.fairprice.pages

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.seadox.fairprice.R
import com.seadox.fairprice.api.Client
import com.seadox.fairprice.api.requests.CartRequest
import com.seadox.fairprice.items.Cart
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.utils.DataStoreManager
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val mockNavController = rememberNavController()
    HomePage(mockNavController)
}

@Composable
fun HomePage(navController: NavHostController, modifier: Modifier = Modifier) {
    var myCart by remember { mutableStateOf(Cart(0.0)) }
    val scope = rememberCoroutineScope()
    var loadProducts by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadStores by remember { mutableStateOf<List<String>>(emptyList()) }
    var fetchCart by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(key1 = "loadData") {
        scope.launch {
            DataStoreManager.getInstance().getDataStore().collect {
                loadProducts = it?.products ?: emptyList()
                loadStores = it?.stores ?: emptyList()

                fetchCart = true
            }
        }
    }

    LaunchedEffect(key1 = loadProducts, key2 = loadStores) {
        if (fetchCart) {
            getCartFromServer(
                CartRequest(loadProducts, loadStores),
                onSuccess = {
                    myCart = it
                },
                onError = {
                    Log.e("Cart Error", "HomePage: Error getting cart from server: $it")
                }
            )
//            fetchCart = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp, 16.dp, 0.dp, 0.dp)
        ) {
            if (loadStores.isEmpty() || loadProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 16.dp, 0.dp, 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (loadProducts.isEmpty()) "המוצרים ריקים. אנא הוסף מוצרים לעגלה" else "רשימת החנויות ריקה. אנא הוסף חנויות לעגלה",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            CartItems(navController, myCart)
        }
    }
}

@Composable
fun CartItems(navController: NavHostController, cart: Cart) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            cart.chains.forEach { (chainName, products) ->
                if (products.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = chainName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    products.forEach { product ->
                        item {
                            ProductItemView(navController, product)
                        }
                    }
                }
            }
        }
        TotalAmount(
            total = cart.totalPrice,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(4.dp)
        )
    }
}

@Composable
fun ProductItemView(navController: NavHostController, product: ProductItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF4F6F9), shape = MaterialTheme.shapes.small)
            .padding(8.dp)
            .clickable { navController.navigate("detail/${Uri.encode(Json.encodeToString(product))}") },
        verticalAlignment = Alignment.CenterVertically,

        ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.imageUrl)
                .crossfade(true)
                .memoryCacheKey(product.imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.READ_ONLY)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .build(),
            contentDescription = "Product Image",
            modifier = Modifier
                .size(80.dp)
                .padding(top = 8.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = product.manufacturerItemDescription,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "קוד מוצר: ${product.itemCode}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "${product.itemPrice} ₪",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun TotalAmount(total: Double = 0.0, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(bottom = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF4CAF50),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "סה\"כ: $total ₪",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getCartFromServer(
    cartRequest: CartRequest,
    onSuccess: (Cart) -> Unit,
    onError: (String) -> Unit
) {
    Client.getCart(
        cartRequest,
        successCallback = {
            onSuccess(it)
        },
        failureCallback = {
            onError(it)
        }
    )
}