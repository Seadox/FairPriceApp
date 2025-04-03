package com.seadox.fairprice.pages


import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
import com.seadox.fairprice.api.requests.ProductsRequest
import com.seadox.fairprice.api.response.ProductResponse
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.utils.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Preview(showBackground = true)
@Composable
fun PreviewSearchPage() {
    val mockNavController = rememberNavController()
    SearchPage(navController = mockNavController, scannerOpened = {})
}

@Composable
fun SearchPage(
    navController: NavHostController,
    scannerOpened: (isOpen: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val limit = 10

    var query by rememberSaveable { mutableStateOf("") }
    var lastQuery by remember { mutableStateOf("") }
    var products by remember { mutableStateOf(listOf<ProductItem>()) }
    var newKey by rememberSaveable { mutableIntStateOf(0) }

    var showScanner by remember { mutableStateOf(false) }
    val permissionResult = remember { mutableStateOf(false) }

    val loadProducts = DataStoreManager.getInstance().getProducts()

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(loadProducts) {
        getStoreDataByIds(
            loadProducts,
            successCallback = { response ->
                products = response
            },
            failureCallback = { error ->
                Log.d("TAG", "error SearchPage: $error")
            }
        )
    }

    LaunchedEffect(query) {
        if (query.isNotEmpty() && query.length > 2) {
            delay(500)

            if (query != lastQuery) {
                lastQuery = query

                if (query.isNotEmpty() && query.length > 2) {
                    getProductsByName(query, newKey, limit, {
                        products = it.products
                        newKey = it.nextKey

                        Log.d("ptt", "SearchPage: $products")
                    }, {
                        Log.d("SearchPage", "error SearchPage: $it")
                    })
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    placeholder = { Text("חפש מוצרים") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            vertical = 8.dp
                        )
                        .background(
                            Color(0xFFF4F6F9),
                            shape = MaterialTheme.shapes.medium
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.medium
                        ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                // Scanner Button
                Button(
                    onClick = {
                        showScanner = true
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.scanner),
                        contentDescription = "Scanner"
                    )
                }
            }
            Box(modifier = Modifier.pointerInput(Unit) {
                detectTransformGestures { _, pan, _, _ ->
                    if (pan.y > 0) {
                        keyboardController?.hide()
                    }
                }
            }) {
                GridItems(
                    navController, products, query, newKey, limit,
                    updateProductList = { products = it },
                    updateNewKey = { newKey = it },
                    onProductClick = { product ->
                        if (!showScanner)
                            navController.navigate("detail/${Uri.encode(Json.encodeToString(product))}")
                    },
                )
            }
        }
    }

    if (showScanner) {
        val cameraPermissionRequest =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                permissionResult.value = isGranted
            }

        LaunchedEffect(Unit) {
            cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)
        }

        if (permissionResult.value) {
            scannerOpened(true)
            BarcodeScannerScreen(onBarcodeScanned = {
                query = it
                showScanner = false
                scannerOpened(false)
            }, onBackPress = {
                showScanner = false
                scannerOpened(false)
            })
        }
    }
}

@Composable
fun GridItems(
    navController: NavHostController,
    products: List<ProductItem>,
    query: String = "",
    newKey: Int = 0,
    limit: Int = 10,
    updateProductList: (List<ProductItem>) -> Unit = {},
    updateNewKey: (Int) -> Unit = {},
    onProductClick: (ProductItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gridState = remember { LazyGridState() }
    var isBottomReached by remember { mutableStateOf(false) }

    val bottomReached = rememberUpdatedState {
        if (query.length > 2 && newKey > 0) {
            getProductsByName(query, newKey, limit, {
                updateProductList(products + it.products)
                updateNewKey(it.nextKey)
            }, {
                Log.e("Search Error", "error SearchPage: $it")
            })
        }
    }

    // Monitor scroll position
    LaunchedEffect(gridState.layoutInfo) {
        val layoutInfo = gridState.layoutInfo

        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val totalItemCount = layoutInfo.totalItemsCount

        if (lastVisibleItemIndex == totalItemCount - 1 && !isBottomReached && query.length > 2) {
            bottomReached.value()
            isBottomReached = true
        } else if (lastVisibleItemIndex != totalItemCount - 1) {
            isBottomReached = false
        }
    }

    LazyVerticalGrid(
        reverseLayout = false,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        columns = GridCells.Fixed(2),
        state = gridState
    ) {
        itemsIndexed(products) { _, item ->
            GridItem(navController, item, onProductClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GridItem(
    navController: NavHostController,
    item: ProductItem,
    onProductClick: (ProductItem) -> Unit = {},
) {
    val savedProduct =
        DataStoreManager.getInstance().isProductInDataStoreItem(item.itemCode.toString())

    var isFavorite by remember { mutableStateOf(item.isFavorite) }
    var savedProductState by remember { mutableStateOf(savedProduct) }

    val productName = item.manufacturerItemDescription
    val price = item.itemPrice


    val truncatedName = if (productName.length > 16) {
        productName.take(14) + "..."
    } else {
        productName
    }

    LaunchedEffect(savedProduct) {
        savedProductState = savedProduct
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(150.dp)
            .padding(8.dp)
            .clickable { onProductClick(item) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp)
        ) {
            // Product Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .memoryCacheKey(item.imageUrl)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.READ_ONLY)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .build(),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 8.dp),
                contentScale = ContentScale.Fit
            )

            // Product Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                // Product Price
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "$price ₪",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CB86A)
                        ),
                        maxLines = 1
                    )
                }
                // Add/Remove Button
                AddRemoveBtn(savedProductState, item) { newSavedState ->
                    savedProductState = newSavedState
                }
            }

        }
    }
}

@Composable
fun AddRemoveBtn(savedProduct: Boolean, item: ProductItem, onStateChange: (Boolean) -> Unit) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (!savedProduct) Color(0xFF00C853) else Color(0xFFB84C4C),
                shape = RoundedCornerShape(40, 0, 40, 0)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (!savedProduct) {
                    addProductToCart(item)
                    onStateChange(true)
                } else {
                    removeProductFromCart(item)
                    onStateChange(false)
                }
            }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (!savedProduct) Icons.Default.Add else Icons.Default.Clear,
            contentDescription = "Add to Cart",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun addProductToCart(product: ProductItem) {
    val id = "${product.chainId}_${product.itemCode}"

    CoroutineScope(Dispatchers.IO).launch {
        DataStoreManager.getInstance().addProductToDataStoreItem(id).saveToDataStore()
    }
}

fun removeProductFromCart(product: ProductItem) {
    val id = "${product.chainId}_${product.itemCode}"

    CoroutineScope(Dispatchers.IO).launch {
        DataStoreManager.getInstance().removeProductFromDataStoreItem(id)
            .saveToDataStore()
    }
}

fun getProductsByName(
    query: String,
    nextKey: Int = 0,
    limit: Int = 10,
    successCallback: (ProductResponse) -> Unit,
    failureCallback: (String) -> Unit
) {
    Client.getProductsByName(
        name = query,
        nextKey = nextKey,
        limit = limit,
        successCallback = { response ->
            successCallback(response)
        },
        failureCallback = { error ->
            failureCallback(error)
        }
    )
}

fun getStoreDataByIds(
    ids: List<String>,
    successCallback: (List<ProductItem>) -> Unit,
    failureCallback: (String) -> Unit
) {
    Client.getProductsByIds(
        ids = ProductsRequest(products = ids),
        successCallback = { response ->
            successCallback(response)
        },
        failureCallback = { error ->
            failureCallback(error)
        }
    )
}

