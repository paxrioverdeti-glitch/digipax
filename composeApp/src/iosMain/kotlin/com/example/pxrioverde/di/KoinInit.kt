package com.example.pxrioverde.di

import org.koin.core.context.startKoin

private var koinInitialized = false

fun initKoin() {
    if (!koinInitialized) {
        try {
            startKoin {
                modules(appModule)
            }
            koinInitialized = true
        } catch (e: Exception) {
            println("DEBUG: Koin initialization notice: ${e.message}")
        }
    }
}
