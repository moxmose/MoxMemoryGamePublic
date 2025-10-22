package com.example.moxmemorygame.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moxmemorygame.R

val kanit = FontFamily(
    Font(R.font.kanit, FontWeight.Normal),
    Font(R.font.kanit_medium, FontWeight.Medium)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = kanit,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 20.sp, // Modified from 22.sp
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = kanit,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp
    )
)
