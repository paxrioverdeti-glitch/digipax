package com.example.pxrioverde.util

import androidx.compose.runtime.Composable

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No iOS o botão voltar físico não existe, a navegação é geralmente por gestos ou botões na UI.
    // Esta implementação é um stub para manter a compatibilidade commonMain.
}
