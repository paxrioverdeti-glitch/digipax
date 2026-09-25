package com.example.pxrioverde.ui.components

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*

@Composable
actual fun LottieAnimation(
    resName: String,
    modifier: Modifier,
    tintColor: Color?
) {
    val composition = rememberLottieComposition(resName)
    
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever,
        speed = if (resName == "carregamento") 2.0f else 1.0f
    )
}

@Composable
actual fun LottieAnimation(
    composition: Any?,
    modifier: Modifier,
    iterations: Int,
    speed: Float
) {
    val lottieComposition = composition as? LottieComposition ?: return

    val progress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = iterations,
        speed = speed
    )

    com.airbnb.lottie.compose.LottieAnimation(
        composition = lottieComposition,
        progress = { progress },
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
actual fun rememberLottieComposition(resName: String): Any? {
    val context = LocalContext.current
    
    val resId = remember(resName) {
        var id = context.resources.getIdentifier(resName, "raw", context.packageName)
        if (id == 0) {
            id = context.resources.getIdentifier(resName, "raw", "com.example.pxrioverde")
        }
        if (id == 0 && resName == "carregamento") {
            id = com.example.pxrioverde.R.raw.carregamento
        }
        id
    }

    val compositionResult = com.airbnb.lottie.compose.rememberLottieComposition(
        if (resId != 0) LottieCompositionSpec.RawRes(resId) 
        else LottieCompositionSpec.RawRes(com.example.pxrioverde.R.raw.animacao1)
    )
    
    return compositionResult.value
}
