package com.example.pxrioverde

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.example.pxrioverde.di.initKoin
import com.example.pxrioverde.di.koinInitError
import platform.UIKit.UIViewController

@Composable
fun SafeApp() {
    val error = koinInitError
    if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Erro ao iniciar dependências:\n\n$error",
                color = Color.Red
            )
        }
    } else {
        App()
    }
}

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { SafeApp() }
}