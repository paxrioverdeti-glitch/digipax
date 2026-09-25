package com.example.pxrioverde.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

@Composable
expect fun LottieAnimation(
    resName: String,
    modifier: Modifier = Modifier,
    tintColor: Color? = null
)

@Composable
expect fun LottieAnimation(
    composition: Any?,
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    speed: Float = 1f
)

@Composable
expect fun rememberLottieComposition(resName: String): Any?

object LottieConstants {
    const val IterateForever: Int = Int.MAX_VALUE
}
