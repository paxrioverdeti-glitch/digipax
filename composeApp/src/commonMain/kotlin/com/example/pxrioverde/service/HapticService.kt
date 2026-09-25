package com.example.pxrioverde.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

class HapticService(private val haptic: HapticFeedback) {
    fun success() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun heavy() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberHapticService(): HapticService {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { HapticService(haptic) }
}
