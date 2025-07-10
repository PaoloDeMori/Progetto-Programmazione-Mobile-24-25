package com.example.appranzo

import android.app.Application
import com.example.appranzo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class APPranzoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@APPranzoApp)
            modules(appModule)
        }
    }
}
