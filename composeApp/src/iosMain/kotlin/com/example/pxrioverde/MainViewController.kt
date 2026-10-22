package com.example.pxrioverde

import androidx.compose.ui.window.ComposeUIViewController
import com.example.pxrioverde.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}