package com.example.moxmemorygame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.moxmemorygame.ui.Screen

@Composable
fun TestOpeningMenuScreen(
    navController: NavController,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Opening Menu")
        Button(onClick = { navController.navigate(Screen.PreferencesScreen.route) }) {
            Text("Go to Preferences")
        }
        Button(onClick = { navController.navigate(Screen.GameScreen.route) }) {
            Text("Go to Game")
        }
    }
}

@Composable
fun TestPreferencesScreen(navController: NavController,
                          innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Preferences")
    }
}

@Composable
fun TestGameScreen(navController: NavController,
                   innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Game")
    }
}