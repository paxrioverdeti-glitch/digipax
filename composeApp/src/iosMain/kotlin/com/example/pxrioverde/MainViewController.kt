package com.example.pxrioverde

import androidx.compose.ui.window.ComposeUIViewController
import com.example.pxrioverde.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}