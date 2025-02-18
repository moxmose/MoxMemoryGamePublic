package com.example.moxmemorygame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.R

@Composable
fun OpeningMenuScreen(
    navigateToGame: () -> Unit,
    navigateToPreferences: () -> Unit,
//    navHostController: NavHostController,
    innerPadding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), // Apply innerPadding here
        contentAlignment = Alignment.Center
    ) {
        BackgroundImg()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            Text(
                text = "MOX MEMORY GAME",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { navigateToGame() },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 1.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "START GAME")
            }
            Button(
                onClick = { navigateToPreferences() },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 1.dp,
                    bottomStart = 1.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "SETTINGS")
            }
        }
    }
}

/**
 * Set background
 */
@Composable
fun BackgroundImg() {
    Image(
        painter = painterResource(id = R.drawable.background_00),
        contentDescription = null,
        alpha = 0.5f,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Preview
@Composable
fun OpeningMenuScreenPreview() {
    //val fakeNavController = rememberNavController() // Crea un NavHostController "fake"
    val fakeNavigation: () -> Unit = {}
    val fakePadding = PaddingValues(16.dp) // Crea dei PaddingValues "fake"
    OpeningMenuScreen(fakeNavigation, fakeNavigation, fakePadding)
}