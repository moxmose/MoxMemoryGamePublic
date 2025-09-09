package com.example.moxmemorygame


import androidx.navigation.NavHostController
import com.example.moxmemorygame.ui.GameViewModel
import com.example.moxmemorygame.ui.NavigationManager
import com.example.moxmemorygame.ui.OpeningMenuViewModel
import com.example.moxmemorygame.ui.PreferencesViewModel
import com.example.moxmemorygame.ui.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val myAppModule = module {
    viewModel {
        TimerViewModel()
    }

    single<CoroutineScope>(named("ApplicationScope")) {CoroutineScope(SupervisorJob() + Dispatchers.Default)}
    // AppSettinfDataStore can now the scope or use its default
    //single { RealAppSettingsDataStore(androidContext()/*, get(named("ApplicationScope"))*/) }
    single<IAppSettingsDataStore> { RealAppSettingsDataStore(androidContext()/*, get(named("ApplicationScope"))*/) }

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
    viewModel { (navController: NavHostController) ->
        OpeningMenuViewModel(
            navController = navController,
            appSettingsDataStore = get()
            )
    }
}

val navigationModule = module {
    single {
        NavigationManager()
    }
}

val appModules = listOf(myAppModule, navigationModule)