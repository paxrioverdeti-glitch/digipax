package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.pxrioverde.ui.platform.IosBackDispatcher

/**
 * No iPhone nao existe botao fisico de voltar. A tela registra aqui o que fazer ao voltar,
 * e o gesto de deslizar da borda esquerda (IosEdgeSwipeBack, aplicado na raiz do app) chama.
 */
@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnBack by rememberUpdatedState(onBack)
    val entry = remember {
        IosBackDispatcher.Entry(isEnabled = { currentEnabled }, onBack = { currentOnBack() })
    }
    DisposableEffect(entry) {
        IosBackDispatcher.add(entry)
        onDispose { IosBackDispatcher.remove(entry) }
    }
}
