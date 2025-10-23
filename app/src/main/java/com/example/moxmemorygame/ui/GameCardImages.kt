package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import com.example.moxmemorygame.R

data class GameCardImages (
    @param:DrawableRes
    val image: List<Int> = listOf(
        R.drawable.img_s_00,
        R.drawable.img_s_01,
        R.drawable.img_s_02,
        R.drawable.img_s_03,
        R.drawable.img_s_04,
        R.drawable.img_s_05,
        R.drawable.img_s_06,
        R.drawable.img_s_07,
        R.drawable.img_s_08,
        R.drawable.img_s_09
    )
)

// The "class GameCardImage" was removed because it was unused.
