package com.example.pxrioverde.util

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop não possui botão de voltar físico, então não faz nada.
}
