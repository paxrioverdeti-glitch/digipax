package com.example.pxrioverde.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CorporateMic: ImageVector
    get() {
        if (_corporateMic != null) return _corporateMic!!
        _corporateMic = ImageVector.Builder(
            name = "CorporateMic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Microphone Head (Capsule)
            moveTo(12f, 4f)
            curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
            verticalLineTo(12f)
            curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
            curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
            verticalLineTo(8f)
            curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
            close()
            
            // Outer Support (The U-shape)
            moveTo(19f, 11f)
            curveTo(19f, 14.87f, 15.87f, 18f, 12f, 18f)
            curveTo(8.13f, 18f, 5f, 14.87f, 5f, 11f)
            
            // Bottom Stand
            moveTo(12f, 18f)
            verticalLineTo(21f)
            moveTo(9f, 21f)
            horizontalLineTo(15f)
        }.build()
        return _corporateMic!!
    }

private var _corporateMic: ImageVector? = null
