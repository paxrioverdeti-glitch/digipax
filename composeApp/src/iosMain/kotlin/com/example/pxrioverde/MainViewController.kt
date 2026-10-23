package com.example.pxrioverde

import androidx.compose.ui.window.ComposeUIViewController
import com.example.pxrioverde.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    try {
        initKoin()
    } catch (e: Throwable) {
        println("CRITICAL: Error in initKoin: ${e.message}")
    }

    return ComposeUIViewController { App() }
}