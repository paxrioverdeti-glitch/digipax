package com.example.pxrioverde.di

import org.koin.core.context.startKoin

private var koinInitialized = false
var koinInitError: String? = null

fun initKoin() {
    if (!koinInitialized) {
        try {
            startKoin {
                modules(appModule)
            }
            koinInitialized = true
        } catch (e: Throwable) {
            koinInitError = e.message ?: e.toString()
            println("DEBUG: Koin initialization notice: ${e.message}")
        }
    }
}
