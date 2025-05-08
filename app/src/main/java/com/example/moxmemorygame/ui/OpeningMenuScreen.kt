package com.example.moxmemorygame.ui

import android.annotation.SuppressLint
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
import androidx.navigation.compose.rememberNavController
import com.example.moxmemorygame.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun OpeningMenuScreen(
    viewModel: OpeningMenuViewModel = koinViewModel(),
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
                onClick = viewModel::onStartGameClicked,
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
                onClick = viewModel::onSettingsClicked,
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun OpeningMenuScreenPreview() {
    val fakeViewModel = OpeningMenuViewModel(navController = rememberNavController())
    val fakePadding = PaddingValues(16.dp)
    OpeningMenuScreen(fakeViewModel, fakePadding)
}