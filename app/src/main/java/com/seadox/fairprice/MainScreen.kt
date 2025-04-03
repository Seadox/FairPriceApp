package com.seadox.fairprice

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seadox.fairprice.items.NavItem
import com.seadox.fairprice.items.ProductItem
import com.seadox.fairprice.pages.DetailPage
import com.seadox.fairprice.pages.HomePage
import com.seadox.fairprice.pages.SearchPage
import com.seadox.fairprice.pages.SettingsPage
import kotlinx.serialization.json.Json

@SuppressLint("RememberReturnType")
@Preview(showBackground = true)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    var scannerOpened by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem("home", "סל קניות", R.drawable.cart_icon),
        NavItem("search", "חיפוש", R.drawable.search),
        NavItem("settings", "הגדרות", R.drawable.settings),
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                        ?: "home"
                if (!scannerOpened) {
                    CustomNavigationBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        items = navItems
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 300)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 300)) },
            ) {
                composable("home") { HomePage(navController) }
                composable("search") { SearchPage(navController, { scannerOpened = it }) }
                composable("detail/{product}") { backStackEntry ->
                    val productJson = backStackEntry.arguments?.getString("product")
                    val product = productJson?.let { Json.decodeFromString<ProductItem>(it) }
                    product?.let { DetailPage(it) }
                }
                composable("settings") { SettingsPage() }
            }
        }
    }
}

@Composable
fun CustomNavigationBar(
    navController: NavHostController,
    currentRoute: String,
    items: List<NavItem>
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                tint = if (currentRoute == item.route)
                                    Color(0xFF0049FF)
                                else
                                    Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        if (currentRoute == item.route) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0049FF)
                            )
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0049FF),
                    unselectedIconColor = Color.Black,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
