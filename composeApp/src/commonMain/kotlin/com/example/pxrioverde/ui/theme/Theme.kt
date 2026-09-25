package com.example.pxrioverde.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

import com.example.pxrioverde.util.AccessibilitySettings

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF74C69D),
    secondary = Color(0xFF95D5B2),
    tertiary = Color(0xFFB7E4C7),
    background = Color(0xFF101412),
    surface = Color(0xFF18201C),
    onPrimary = Color(0xFF102017),
    onSecondary = Color(0xFF102017),
    onTertiary = Color(0xFF102017),
    onBackground = Color(0xFFE8F3EC),
    onSurface = Color(0xFFE8F3EC),
    surfaceVariant = Color(0xFF29352E),
    onSurfaceVariant = Color(0xFFC1D0C6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B4332),
    secondary = Color(0xFF2D6A4F),
    tertiary = Color(0xFF40916C),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B4332),
    onSurface = Color(0xFF1B4332)
)

private val HighContrastColorScheme = lightColorScheme(
    primary = Color(0xFF004D40),
    secondary = Color(0xFF000000),
    tertiary = Color(0xFF004D40),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun PxrioverdeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        AccessibilitySettings.isHighAccessibilityModeEnabled -> HighContrastColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Tipografia dinâmica baseada em acessibilidade
    val baseTypography = Typography()
    val typography = if (AccessibilitySettings.isHighAccessibilityModeEnabled) {
        Typography(
            displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * 1.15f),
            displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * 1.15f),
            displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * 1.15f),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * 1.15f),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * 1.15f),
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * 1.15f),
            titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * 1.15f),
            titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * 1.15f),
            titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * 1.15f),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * 1.15f),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * 1.15f),
            bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * 1.15f),
            labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * 1.15f),
            labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * 1.15f),
            labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * 1.15f)
        )
    } else {
        baseTypography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
