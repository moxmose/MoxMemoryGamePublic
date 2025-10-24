package com.example.moxmemorygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moxmemorygame.ui.BackgroundMusicManager
import com.example.moxmemorygame.ui.NavGraph
import com.example.moxmemorygame.ui.theme.MoxMemoryGameTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    // Inject the BackgroundMusicManager. Koin will create and manage its lifecycle.
    private val backgroundMusicManager: BackgroundMusicManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // According to the documentation, installSplashScreen should be called BEFORE super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // By referencing the manager here, we ensure Koin initializes it and starts the observers.
        // The manager will then handle its own lifecycle internally.
        backgroundMusicManager.toString() // This forces Koin to create the instance

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

    override fun onResume() {
        super.onResume()
        // Resume music when the app comes into the foreground.
        backgroundMusicManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Pause music when the app goes into the background.
        backgroundMusicManager.onPause()
    }
}