package com.example.moxmemorygame

import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class DependencyInjectionTest : KoinTest {

    @Test
    fun `check all koin modules`() {
        // Stop any existing Koin instance started by the Application class via Robolectric
        stopKoin()

        checkModules(
            parameters = {
                val mockNavController = mock(NavHostController::class.java)
                parametersOf(mockNavController)
            }
        ) {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(appModules)
        }
    }
}