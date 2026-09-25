package com.example.pxrioverde

import android.app.Application
import app.rive.runtime.kotlin.core.Rive
import com.example.pxrioverde.util.LocalStorage

import com.example.pxrioverde.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PxrioverdeApp : Application() {
    companion object {
        lateinit var instance: PxrioverdeApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializa o Koin
        startKoin {
            androidContext(this@PxrioverdeApp)
            modules(appModule)
        }

        // Inicializa o LocalStorage
        LocalStorage.initialize(this)
        // Inicializa o Rive Runtime
        Rive.init(this)
    }
}
