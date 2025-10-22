package com.example.moxmemorygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moxmemorygame.ui.NavGraph
import com.example.moxmemorygame.ui.theme.MoxMemoryGameTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // According to the documentation, installSplashScreen should be called BEFORE super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Determine whether to enable edge-to-edge based on screen width
        val screenWidthDp = resources.configuration.screenWidthDp
        // We define a threshold for tablets; 600dp is common, but you can adjust it
        val isConsideredPhone = screenWidthDp < 600

        if (isConsideredPhone) {
            enableEdgeToEdge() // Enable only for "phones"
        }
        // For "tablets" (screenWidthDp >= 600), enableEdgeToEdge() is not called,
        // so the app won't be edge-to-edge, and the status bar won't cover the content.

        setContent {
            MoxMemoryGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavGraph(innerPadding = innerPadding)
                }
            }
        }
    }
}