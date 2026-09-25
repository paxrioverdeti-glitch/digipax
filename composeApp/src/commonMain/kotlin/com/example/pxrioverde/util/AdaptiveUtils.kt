package com.example.pxrioverde.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT, MEDIUM, EXPANDED
}

data class WindowAdaptiveInfo(
    val widthSizeClass: WindowSizeClass,
    val heightSizeClass: WindowSizeClass
)

object AdaptiveUtils {
    fun calculateWindowSizeClass(width: Dp): WindowSizeClass {
        return when {
            width < 600.dp -> WindowSizeClass.COMPACT
            width < 840.dp -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }

    fun calculateHeightSizeClass(height: Dp): WindowSizeClass {
        return when {
            height < 480.dp -> WindowSizeClass.COMPACT
            height < 900.dp -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }
    }
}
