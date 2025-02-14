package com.example.moxmemorygame

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.NavGraph
import com.example.moxmemorygame.ui.TimerViewModel
import com.example.moxmemorygame.ui.theme.MoxMemoryGameTheme
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoxMemoryGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    // set and lock screen in portrait mode
                    val context = LocalContext.current
                    (context as? Activity)?.requestedOrientation = ActivityInfo. SCREEN_ORIENTATION_PORTRAIT

                    NavGraph()
                    /*MainApp(
                        modifier = Modifier.padding(innerPadding)
                    )*/
                }
            }
        }
    }
}