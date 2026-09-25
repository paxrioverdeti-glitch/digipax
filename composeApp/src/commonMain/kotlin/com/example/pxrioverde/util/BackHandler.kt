package com.example.pxrioverde.util

import androidx.compose.runtime.Composable

/**
 * Interface multiplataforma para lidar com o botão "voltar" do sistema.
 */
@Composable
expect fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)
