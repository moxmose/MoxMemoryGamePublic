package com.example.moxmemorygame

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Assicurati sia presente
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
        // Secondo la documentazione, installSplashScreen va chiamato PRIMA di super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState) // Chiamata a super.onCreate

        // Determina se abilitare edge-to-edge in base alla larghezza dello schermo
        val screenWidthDp = resources.configuration.screenWidthDp
        // Definiamo una soglia per tablet; 600dp è comune, ma puoi aggiustarla
        val isConsideredPhone = screenWidthDp < 600

        if (isConsideredPhone) {
            enableEdgeToEdge() // Abilita solo per i "telefoni"
        }
        // Per i "tablet" (screenWidthDp >= 600), enableEdgeToEdge() non viene chiamato,
        // quindi l'app non sarà edge-to-edge e la status bar non coprirà il contenuto.

        setContent {
            MoxMemoryGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavGraph(innerPadding = innerPadding)
                    /*MainApp(
                        modifier = Modifier.padding(innerPadding)
                    )*/
                }
            }
        }
    }
}