package com.example.pxrioverde

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.pxrioverde.App
import com.example.pxrioverde.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Px Rio Verde",
    ) {
        App()
    }
}
