package com.example.moxmemorygame


import androidx.navigation.NavHostController
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.NavigationManager
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.example.moxmemorygame.ui.TimerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val myAppModule = module {
    viewModel {
        TimerViewModel()
    }

    single { AppSettingsDataStore(androidContext()) } // Fornisci AppSettingsDataStore

    viewModel { (navController: NavHostController) ->
        GameViewModel(
            navController = navController,
            timerViewModel = get(),
            appSettingsDataStore = get()
        )
    }

    viewModel { (navController: NavHostController) ->
        PreferencesViewModel(
            navController = navController,
            appSettingsDataStore = get()
        )
    }

    // OpeningMenuViewModel could be instantiated using a factory, but a VM is more appropriated
    //factory { (navController: NavHostController) -> OpeningMenuViewModel(navController) }
    viewModel {
        (navController: NavHostController) -> OpeningMenuViewModel(navController)
    }
}

val navigationModule = module {
    single {
        NavigationManager()
    }
}

val appModules = listOf(myAppModule, navigationModule)