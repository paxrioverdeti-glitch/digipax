package com.example.pxrioverde.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * AdminTheme - Esquema de cores exclusivo para o módulo administrativo.
 * Sincronizado com a identidade visual da área do usuário.
 */
private val AdminColorScheme = lightColorScheme(
    primary = Color(0xFF1B4332),       // Verde Escuro oficial do app
    onPrimary = Color.White,
    background = Color(0xFFF8F9FA),    // Fundo cinza claro profissional
    onBackground = Color(0xFF1B4332),  // Títulos no verde oficial
    surface = Color.White,
    onSurface = Color(0xFF1B4332),     // Elementos de superfície no verde oficial
    secondary = Color(0xFF2D6A4F),
    onSecondary = Color.White,
    tertiary = Color(0xFF40916C),
    onTertiary = Color.White,
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
)

@Composable
fun AdminTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        typography = MaterialTheme.typography,
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
