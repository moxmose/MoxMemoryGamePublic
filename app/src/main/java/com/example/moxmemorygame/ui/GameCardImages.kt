package com.example.moxmemorygame.ui

import androidx.annotation.DrawableRes
import com.example.moxmemorygame.R

data class GameCardImages (
    @DrawableRes
    val image: List<Int> = listOf(
        R.drawable.bb_inhouse_02,
        R.drawable.bb_inhouse_03,
        R.drawable.bb_inhouse_07,
//        R.drawable.bb_inhouse_09,
        R.drawable.bb_nanna_01,
        R.drawable.bb_nanna_04,
        R.drawable.bb_nanna_05,
//        R.drawable.bb_nanna_16,
        R.drawable.bb_outdoor_11,
        R.drawable.bb_outdoor_12,
        R.drawable.bb_outdoor_14,
        R.drawable.bb_zinghiri_06
    )
)

class GameCardImage (
    val id: Int,
    @DrawableRes
    var image: Int
) {
}