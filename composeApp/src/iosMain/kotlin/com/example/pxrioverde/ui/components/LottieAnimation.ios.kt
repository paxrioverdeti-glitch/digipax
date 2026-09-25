package com.example.pxrioverde.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color

@Composable
actual fun LottieAnimation(
    resName: String,
    modifier: Modifier,
    tintColor: Color?
) {
    // iOS placeholder
    Box(modifier = modifier)
}

@Composable
actual fun LottieAnimation(
    composition: Any?,
    modifier: Modifier,
    iterations: Int,
    speed: Float
) {
    // iOS placeholder
    Box(modifier = modifier)
}

@Composable
actual fun rememberLottieComposition(resName: String): Any? {
    return null
}
