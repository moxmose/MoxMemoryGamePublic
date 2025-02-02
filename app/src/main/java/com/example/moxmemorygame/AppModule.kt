package com.example.moxmemorygame


import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.TimerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        TimerViewModel()
    }

    viewModel {
        GameViewModel(
            get()
        )
    }

}