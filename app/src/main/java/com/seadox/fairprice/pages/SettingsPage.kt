package com.seadox.fairprice.pages

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seadox.fairprice.R
import com.seadox.fairprice.api.Client
import com.seadox.fairprice.api.requests.SearchStoresRequest
import com.seadox.fairprice.items.Chain
import com.seadox.fairprice.items.Store
import com.seadox.fairprice.utils.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun SettingsPagePreview() {
    SettingsPage()
}

@Composable
fun SettingsPage(modifier: Modifier = Modifier) {
    var settingsOptions by remember { mutableStateOf(listOf<Store>()) }
    var chains by remember { mutableStateOf(listOf<Chain>()) }
    val loadStores = DataStoreManager.getInstance().getStores()

    getStoresDataByIds(
        loadStores,
        successCallback = {
            settingsOptions = it
        },
        failureCallback = {
            Log.d("ptt", "SettingsPage: getStoresDataByIds: $it")
        }
    )

    if (chains.isEmpty()) {
        getSupportedChains(
            successCallback = {
                chains = it
            },
            failureCallback = {
                Log.d("ptt", "SettingsPage: getSupportedChains: $it")
            }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "הגדרות",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Stores(loadStores, settingsOptions) {
                settingsOptions = it
            }

            SupportedChains(chains)

            Terms()
        }
    }
}

@Composable
fun Terms() {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "מגבלות אחריות ושימוש באפליקציה",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse List" else "Expand List"
            )
        }
    }

    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.terms_of_use),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun SupportedChains(chains: List<Chain> = listOf()) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "רשתות נתמכות",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse List" else "Expand List"
            )
        }
    }
    if (isExpanded) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            columns = GridCells.Fixed(2),
        ) {
            items(chains.size) {
                ChainColumnItem(chains[it])
            }
        }
    }
}

@Composable
fun Stores(
    stores: List<String> = listOf(),
    settingsOptions: List<Store> = listOf(),
    updateSettingsOptions: (List<Store>) -> Unit = {}
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var openSearchBar by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    var loadStores = stores

    LaunchedEffect(query) {
        if (query.isNotEmpty() && query.length > 2) {
            delay(500)

            getStoresFromServer(
                query,
                successCallback = {
                    updateSettingsOptions(it)
                },
                failureCallback = {
                    Log.d("ptt", "SettingsPage: $it")
                }
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "חנויות מועדפות",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Plus Button
        if (isExpanded) {
            IconButton(onClick = { openSearchBar = !openSearchBar }) {
                Icon(
                    imageVector = if (!openSearchBar) Icons.Default.Add else Icons.Default.Clear,
                    contentDescription = "Add New Option"
                )
            }
        }

        // Arrow Down Button
        IconButton(onClick = {
            isExpanded = !isExpanded
            openSearchBar = false
            query = ""

            loadStores = DataStoreManager.getInstance().getStores()

            getStoresDataByIds(
                loadStores,
                successCallback = {
                    updateSettingsOptions(it)
                },
                failureCallback = {
                    Log.d("ptt", "SettingsPage: $it")
                }
            )
        }) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse List" else "Expand List"
            )
        }
    }

    // Search Bar
    if (openSearchBar and isExpanded) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("חיפוש לפי מיקום") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }

    if (isExpanded) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            items(settingsOptions, key = { "${it.chainId}_${it.storeId}" }) {
                StoresColumnItem(it)
            }
        }
    }
}

@Composable
fun StoresColumnItem(store: Store) {
    val id = "${store.chainId}_${store.storeId}"

    val savedStore =
        DataStoreManager.getInstance().isStoreInDataStoreItem(id)

    var isFavorite by remember { mutableStateOf(savedStore) }

    Card(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(3f)
                ) {
                    // Chain Name
                    Text(
                        text = store.chainName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1
                    )

                    // Address
                    Text(
                        text = if ((store.address == "unknown") or (store.city == "unknown"))
                            store.storeName
                        else "${store.address}, ${store.city}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                    )
                }
                // Favorite
                IconButton(onClick = {
                    favoriteStoreClicked(isFavorite, store)

                    isFavorite = !isFavorite
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "favorite",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun ChainColumnItem(chain: Chain) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),

            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (chain.supported) Icons.Default.Check else Icons.Default.Clear,
                contentDescription = "favorite",
                tint = if (chain.supported) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = chain.chainName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun favoriteStoreClicked(isFavorite: Boolean, store: Store) {
    CoroutineScope(Dispatchers.IO).launch {
        if (!isFavorite) {
            DataStoreManager
                .getInstance()
                .addStoreToDataStoreItem("${store.chainId}_${store.storeId}")
                .saveToDataStore()
        } else {
            DataStoreManager
                .getInstance()
                .removeStoreFromDataStoreItem("${store.chainId}_${store.storeId}")
                .saveToDataStore()
        }
    }
}

fun getStoresDataByIds(
    ids: List<String>,
    successCallback: (List<Store>) -> Unit,
    failureCallback: (String) -> Unit
) {
    Client.getStoresByIds(
        SearchStoresRequest(ids),
        successCallback = {
            successCallback(it)
        },
        failureCallback = {
            failureCallback(it)
        }
    )
}

fun getStoresFromServer(
    query: String,
    successCallback: (List<Store>) -> Unit,
    failureCallback: (String) -> Unit
) {
    if (query.isNotEmpty() && query.length > 2) {
        Client.getStores(
            query,
            successCallback = {
                successCallback(it)
            },
            failureCallback = {
                failureCallback(it)
            }
        )
    }
}

fun getSupportedChains(
    successCallback: (List<Chain>) -> Unit,
    failureCallback: (String) -> Unit
) {
    Client.getSupportedChains(
        successCallback = {
            successCallback(it)
        },
        failureCallback = {
            failureCallback(it)
        }
    )
}

