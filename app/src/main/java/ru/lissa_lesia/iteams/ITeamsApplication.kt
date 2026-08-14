package ru.lissa_lesia.iteams

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.lissa_lesia.iteams.di.appModule

class ITeamsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@ITeamsApplication)
            modules(appModule)
        }
    }
}