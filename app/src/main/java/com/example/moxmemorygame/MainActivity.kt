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
import com.example.moxmemorygame.ui.theme.MoxMemoryGameTheme

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

                    MainApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                    /*    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    ) */
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val screenDensity = configuration.densityDpi / 160f
    val screenHeightPx = (configuration.screenHeightDp.toFloat() * screenDensity).toInt()
    val screenWidthPx = (configuration.screenWidthDp.toFloat() * screenDensity).toInt()

    Text(
        text = "Hello $name! H=$screenHeight W=$screenWidth,\n Density=$screenDensity Hpx=$screenHeightPx Wpx=$screenWidthPx",
        modifier = modifier
    )
    //Image(imageVector = ImageVector.vectorResource(id = R.drawable.retrocarta_2_small), contentDescription = null)
    Box()
    {

        Image(
            painter = painterResource(id = R.drawable.background_00),
            contentDescription = null,
            alpha = 0.7f,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            Box(modifier = Modifier.size(width = 100.dp, height = 150.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.card_back),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

            }
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.card_back),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )

            }
            Box(modifier = Modifier.size(width = 150.dp, height = 200.dp))
            {
                Image(
                    painter = painterResource(id = R.drawable.card_back),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoxMemoryGameTheme {
        Greeting("Android")
    }
}