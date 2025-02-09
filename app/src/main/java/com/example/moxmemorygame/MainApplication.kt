package com.example.moxmemorygame

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

/**
 * [MyApplication] is the main Application class for this Android application.
 *
 * It extends the [Application] class and is responsible for initializing
 * the Koin dependency injection framework during application startup.
 *
 * This class is referenced in the AndroidManifest.xml file to ensure that it is
 * instantiated when the application starts.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}

// TODO: add main screen, where
// TODO: add preference screen for background
// TODO: add preference screen for cards sets
// TODO: improve cards preference screen for select single cards and dimension