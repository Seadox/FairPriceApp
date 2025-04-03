package com.seadox.fairprice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.seadox.fairprice.ui.theme.FairPriceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            FairPriceTheme(darkTheme = false) {
                if (showSplash) {
                    SplashScreen {
                        showSplash = false
                    }
                } else {
                    MainScreen()
                }
            }
        }
    }
}