package com.example.moxmemorygame

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@RunWith(AndroidJUnit4::class)
class DependencyInjectionTest : KoinTest {

    // This is the direct answer to the error "Missing MockProvider".
    // We register a Mockito-based provider before the test runs.
    @Before
    fun setup() {
        MockProvider.register { clazz ->
            Mockito.mock(clazz.java)
        }
    }

    @Test
    fun `check all koin modules`() {
        // CRUCIAL: Stop any Koin instance that might have been started by the Application class
        // which is instantiated by Robolectric. This ensures `checkModules` starts clean.
        stopKoin()

        // we can now run checkModules. It will start its own temporary Koin application.
        // When it encounters a definition it cannot resolve, it will use the registered MockProvider.
        checkModules {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(appModules)
        }
    }

    // It's good practice to stop Koin after the test class finishes.
    @After
    fun tearDown() {
        stopKoin()
    }
}