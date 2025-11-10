package com.example.moxmemorygame

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProvider
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DependencyInjectionTest : KoinTest {

    @Before
    fun setup() {
        // Register a Mockito-based MockProvider to allow Koin's `checkModules`
        // to create mock instances for definitions with unresolved parameters.
        MockProvider.register { clazz ->
            Mockito.mock(clazz.java)
        }
    }

    @Test
    fun verifiesKoinModules() {
        // Stop any Koin instance started by the Application class (via Robolectric)
        // to ensure `checkModules` can start its own clean Koin application.
        stopKoin()

        // `checkModules` verifies that all definitions in the provided modules can be resolved.
        checkModules {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(appModules)
        }
    }

    @After
    fun tearDown() {
        // Stop Koin after the test to leave a clean state for subsequent tests.
        stopKoin()
    }
}