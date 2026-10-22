package com.example.pxrioverde.di

import org.koin.core.context.startKoin

private var koinInitialized = false

fun initKoin() {
    if (!koinInitialized) {
        startKoin {
            modules(appModule)
        }
        koinInitialized = true
    }
}
