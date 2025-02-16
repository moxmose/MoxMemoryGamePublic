package com.example.moxmemorygame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.R

@Composable
fun OpeningMenuScreen(
    navHostController: NavHostController,
    innerPadding: PaddingValues
) {
    BackgroundImg()
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth()
    ) {
        Button(
            onClick = {},
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
            Text( text = "Start Game")
        }
        Button(
            onClick = {},
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
            Text( text = "Settings")
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
    val fakeNavController = rememberNavController() // Crea un NavHostController "fake"
    val fakePadding = PaddingValues(16.dp) // Crea dei PaddingValues "fake"
    OpeningMenuScreen(fakeNavController, fakePadding)
}