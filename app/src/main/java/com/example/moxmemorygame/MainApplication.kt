package com.example.moxmemorygame

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

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
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(appModules)
        }
    }
}