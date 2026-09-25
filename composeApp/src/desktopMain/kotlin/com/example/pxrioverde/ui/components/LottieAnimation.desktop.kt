package com.example.pxrioverde.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun LottieAnimation(
    resName: String,
    modifier: Modifier,
    tintColor: Color?
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Lottie: $resName")
    }
}

@Composable
actual fun LottieAnimation(
    composition: Any?,
    modifier: Modifier,
    iterations: Int,
    speed: Float
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Lottie Anim")
    }
}

@Composable
actual fun rememberLottieComposition(resName: String): Any? {
    return null
}
