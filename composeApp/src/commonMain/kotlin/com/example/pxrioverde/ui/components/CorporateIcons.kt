package com.example.pxrioverde.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object CorporateIcons {
    val Analytics: ImageVector get() = BarChart
    val Home: ImageVector
        get() = ImageVector.Builder(
            name = "Home",
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
            moveTo(3f, 9f)
            lineTo(12f, 2f)
            lineTo(21f, 9f)
            verticalLineTo(20f)
            curveTo(21f, 20.53f, 20.79f, 21.04f, 20.41f, 21.41f)
            curveTo(20.04f, 21.79f, 19.53f, 22f, 19f, 22f)
            horizontalLineTo(5f)
            curveTo(4.47f, 22f, 3.96f, 21.79f, 3.59f, 21.41f)
            curveTo(3.21f, 21.04f, 3f, 20.53f, 3f, 20f)
            verticalLineTo(9f)
            close()
        }.build()

    val Monitor: ImageVector
        get() = ImageVector.Builder(
            name = "Monitor",
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
            moveTo(4f, 3f)
            horizontalLineTo(20f)
            curveTo(21.1f, 3f, 22f, 3.9f, 22f, 5f)
            verticalLineTo(15f)
            curveTo(22f, 16.1f, 21.1f, 17f, 20f, 17f)
            horizontalLineTo(4f)
            curveTo(2.9f, 17f, 2f, 16.1f, 2f, 15f)
            verticalLineTo(5f)
            curveTo(2f, 3.9f, 2.9f, 3f, 4f, 3f)
            close()
            moveTo(8f, 21f)
            horizontalLineTo(16f)
            moveTo(12f, 17f)
            verticalLineTo(21f)
        }.build()

    val Tickets: ImageVector
        get() = ImageVector.Builder(
            name = "Tickets",
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
            moveTo(8f, 6f)
            horizontalLineTo(21f)
            moveTo(8f, 12f)
            horizontalLineTo(21f)
            moveTo(8f, 18f)
            horizontalLineTo(21f)
            moveTo(3f, 6f)
            horizontalLineTo(3.01f)
            moveTo(3f, 12f)
            horizontalLineTo(3.01f)
            moveTo(3f, 18f)
            horizontalLineTo(3.01f)
        }.build()

    val Car: ImageVector
        get() = ImageVector.Builder(
            name = "Car",
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
            moveTo(7f, 17f)
            curveTo(5.34f, 17f, 4f, 18.34f, 4f, 20f)
            curveTo(4f, 21.66f, 5.34f, 23f, 7f, 23f)
            curveTo(8.66f, 23f, 10f, 21.66f, 10f, 20f)
            curveTo(10f, 18.34f, 8.66f, 17f, 7f, 17f)
            close()
            moveTo(17f, 17f)
            curveTo(15.34f, 17f, 14f, 18.34f, 14f, 20f)
            curveTo(14f, 21.66f, 15.34f, 23f, 17f, 23f)
            curveTo(18.66f, 23f, 20f, 21.66f, 20f, 20f)
            curveTo(20f, 18.34f, 18.66f, 17f, 17f, 17f)
            close()
            moveTo(18.92f, 6.01f)
            curveTo(18.72f, 5.42f, 18.16f, 5f, 17.5f, 5f)
            horizontalLineTo(6.5f)
            curveTo(5.84f, 5f, 5.29f, 5.42f, 5.08f, 6.01f)
            lineTo(3f, 12f)
            verticalLineTo(19f)
            curveTo(3f, 19.55f, 3.45f, 20f, 4f, 20f)
            horizontalLineTo(5f)
            curveTo(5.55f, 20f, 6f, 19.55f, 6f, 19f)
            verticalLineTo(18f)
            horizontalLineTo(18f)
            verticalLineTo(19f)
            curveTo(18f, 19.55f, 18.45f, 20f, 19f, 20f)
            horizontalLineTo(20f)
            curveTo(20.55f, 20f, 21f, 19.55f, 21f, 19f)
            verticalLineTo(12f)
            lineTo(18.92f, 6.01f)
            close()
        }.build()

    val Support: ImageVector
        get() = ImageVector.Builder(
            name = "Support",
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
            moveTo(14.7f, 6.3f)
            curveTo(15.1f, 5.9f, 15.1f, 5.2f, 14.7f, 4.8f)
            lineTo(13.2f, 3.3f)
            curveTo(12.8f, 2.9f, 12.1f, 2.9f, 11.7f, 3.3f)
            lineTo(3.3f, 11.7f)
            curveTo(2.9f, 12.1f, 2.9f, 12.8f, 3.3f, 13.2f)
            lineTo(4.8f, 14.7f)
            curveTo(5.2f, 15.1f, 5.9f, 15.1f, 6.3f, 14.7f)
            lineTo(14.7f, 6.3f)
            close()
            moveTo(20.7f, 12.3f)
            curveTo(21.1f, 11.9f, 21.1f, 11.2f, 20.7f, 10.8f)
            lineTo(19.2f, 9.3f)
            curveTo(18.8f, 8.9f, 18.1f, 8.9f, 17.7f, 9.3f)
            lineTo(9.3f, 17.7f)
            curveTo(8.9f, 18.1f, 8.9f, 18.8f, 9.3f, 19.2f)
            lineTo(10.8f, 20.7f)
            curveTo(11.2f, 21.1f, 11.9f, 21.1f, 12.3f, 20.7f)
            lineTo(20.7f, 12.3f)
            close()
        }.build()

    val Sparkle: ImageVector
        get() = ImageVector.Builder(
            name = "Sparkle",
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
            moveTo(12f, 3f)
            lineTo(14.5f, 9.5f)
            lineTo(21f, 12f)
            lineTo(14.5f, 14.5f)
            lineTo(12f, 21f)
            lineTo(9.5f, 14.5f)
            lineTo(3f, 12f)
            lineTo(9.5f, 9.5f)
            lineTo(12f, 3f)
            close()
        }.build()

    val Send: ImageVector
        get() = ImageVector.Builder(
            name = "Send",
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
            moveTo(22f, 2f)
            lineTo(11f, 13f)
            moveTo(22f, 2f)
            lineTo(15f, 22f)
            lineTo(11f, 13f)
            lineTo(2f, 9f)
            lineTo(22f, 2f)
            close()
        }.build()

    val Notifications: ImageVector
        get() = ImageVector.Builder(
            name = "Notifications",
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
            moveTo(18f, 8f)
            curveTo(18f, 6.41f, 17.37f, 4.88f, 16.24f, 3.76f)
            curveTo(15.12f, 2.63f, 13.59f, 2f, 12f, 2f)
            curveTo(10.41f, 2f, 8.88f, 2.63f, 7.76f, 3.76f)
            curveTo(6.63f, 4.88f, 6f, 6.41f, 6f, 8f)
            curveTo(6f, 15f, 3f, 17f, 3f, 17f)
            horizontalLineTo(21f)
            curveTo(21f, 17f, 18f, 15f, 18f, 8f)
            close()
            moveTo(13.73f, 21f)
            curveTo(13.55f, 21.3f, 13.3f, 21.55f, 13f, 21.73f)
            curveTo(12.7f, 21.91f, 12.35f, 22f, 12f, 22f)
            curveTo(11.65f, 22f, 11.3f, 21.91f, 11f, 21.73f)
            curveTo(10.7f, 21.55f, 10.45f, 21.3f, 10.27f, 21f)
        }.build()

    val Camera: ImageVector
        get() = ImageVector.Builder(
            name = "Camera",
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
            moveTo(23f, 19f)
            curveTo(23f, 20.1f, 22.1f, 21f, 21f, 21f)
            horizontalLineTo(3f)
            curveTo(1.9f, 21f, 1f, 20.1f, 1f, 19f)
            verticalLineTo(8f)
            curveTo(1f, 6.9f, 1.9f, 6f, 3f, 6f)
            horizontalLineTo(7f)
            lineTo(9f, 3f)
            horizontalLineTo(15f)
            lineTo(17f, 6f)
            horizontalLineTo(21f)
            curveTo(22.1f, 6f, 23f, 6.9f, 23f, 8f)
            verticalLineTo(19f)
            close()
            moveTo(12f, 17f)
            curveTo(14.21f, 17f, 16f, 15.21f, 16f, 13f)
            curveTo(16f, 10.79f, 14.21f, 9f, 12f, 9f)
            curveTo(9.79f, 9f, 8f, 10.79f, 8f, 13f)
            curveTo(8f, 15.21f, 9.79f, 17f, 12f, 17f)
            close()
        }.build()

    val Attachment: ImageVector
        get() = ImageVector.Builder(
            name = "Attachment",
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
            moveTo(21.44f, 11.05f)
            lineTo(12.12f, 20.37f)
            curveTo(10.17f, 22.32f, 7f, 22.32f, 5.05f, 20.37f)
            curveTo(3.1f, 18.42f, 3.1f, 15.25f, 5.05f, 13.3f)
            lineTo(14.37f, 3.98f)
            curveTo(15.67f, 2.68f, 17.78f, 2.68f, 19.08f, 3.98f)
            curveTo(20.38f, 5.28f, 20.38f, 7.39f, 19.08f, 8.69f)
            lineTo(9.76f, 18.01f)
            curveTo(9.11f, 18.66f, 8.05f, 18.66f, 7.4f, 18.01f)
            curveTo(6.75f, 17.36f, 6.75f, 16.3f, 7.4f, 15.65f)
            lineTo(15.28f, 7.77f)
        }.build()

    val Back: ImageVector
        get() = ImageVector.Builder(
            name = "Back",
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
            moveTo(19f, 12f)
            horizontalLineTo(5f)
            moveTo(5f, 12f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(12f, 5f)
        }.build()

    val Check: ImageVector
        get() = ImageVector.Builder(
            name = "Check",
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
            moveTo(20f, 6f)
            lineTo(9f, 17f)
            lineTo(4f, 12f)
            }.build()

    val Alert: ImageVector
        get() = ImageVector.Builder(
            name = "Alert",
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
            moveTo(12f, 9f)
            verticalLineTo(13f)
            moveTo(12f, 17f)
            horizontalLineTo(12.01f)
            moveTo(10.29f, 3.86f)
            lineTo(1.82f, 18f)
            curveTo(1.44f, 18.67f, 1.92f, 19.5f, 2.7f, 19.5f)
            horizontalLineTo(21.3f)
            curveTo(22.08f, 19.5f, 22.56f, 18.67f, 22.18f, 18f)
            lineTo(13.71f, 3.86f)
            curveTo(13.33f, 3.22f, 12.67f, 3.22f, 12.29f, 3.86f)
            close()
        }.build()

    val Calendar: ImageVector
        get() = ImageVector.Builder(
            name = "Calendar",
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
            moveTo(3f, 4f)
            horizontalLineTo(21f)
            curveTo(22.1f, 4f, 23f, 4.9f, 23f, 6f)
            verticalLineTo(20f)
            curveTo(23f, 21.1f, 22.1f, 22f, 21f, 22f)
            horizontalLineTo(3f)
            curveTo(1.9f, 22f, 1f, 21.1f, 1f, 20f)
            verticalLineTo(6f)
            curveTo(1f, 4.9f, 1.9f, 4f, 3f, 4f)
            close()
            moveTo(16f, 2f)
            verticalLineTo(6f)
            moveTo(8f, 2f)
            verticalLineTo(6f)
            moveTo(1f, 10f)
            horizontalLineTo(23f)
        }.build()

    val Clock: ImageVector
        get() = ImageVector.Builder(
            name = "Clock",
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
            moveTo(12f, 22f)
            curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
            curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
            curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
            curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
            close()
            moveTo(12f, 6f)
            verticalLineTo(12f)
            lineTo(16f, 14f)
        }.build()

    val Lock: ImageVector
        get() = ImageVector.Builder(
            name = "Lock",
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
            moveTo(19f, 11f)
            horizontalLineTo(5f)
            curveTo(3.895f, 11f, 3f, 11.895f, 3f, 13f)
            verticalLineTo(20f)
            curveTo(3f, 21.105f, 3.895f, 22f, 5f, 22f)
            horizontalLineTo(19f)
            curveTo(20.105f, 22f, 21f, 21.105f, 21f, 20f)
            verticalLineTo(13f)
            curveTo(21f, 11.895f, 20.105f, 11f, 19f, 11f)
            close()
            moveTo(7f, 11f)
            verticalLineTo(7f)
            curveTo(7f, 4.239f, 9.239f, 2f, 12f, 2f)
            curveTo(14.761f, 2f, 17f, 4.239f, 17f, 7f)
            verticalLineTo(11f)
        }.build()

    val MapPin: ImageVector
        get() = ImageVector.Builder(
            name = "MapPin",
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
            moveTo(20f, 10f)
            curveTo(20f, 14.993f, 14.461f, 20.193f, 12.601f, 21.799f)
            curveTo(12.247f, 22.105f, 11.753f, 22.105f, 11.399f, 21.799f)
            curveTo(9.539f, 20.193f, 4f, 14.993f, 4f, 10f)
            curveTo(4f, 5.582f, 7.582f, 2f, 12f, 2f)
            curveTo(16.418f, 2f, 20f, 5.582f, 20f, 10f)
            close()
            moveTo(12f, 13f)
            curveTo(13.657f, 13f, 15f, 11.657f, 15f, 10f)
            curveTo(15f, 8.343f, 13.657f, 7f, 12f, 7f)
            curveTo(10.343f, 7f, 9f, 8.343f, 9f, 10f)
            curveTo(9f, 11.657f, 10.343f, 13f, 12f, 13f)
            close()
        }.build()

    val Wallet: ImageVector
        get() = ImageVector.Builder(
            name = "Wallet",
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
            moveTo(20f, 12f)
            verticalLineTo(8f)
            curveTo(20f, 6.895f, 19.105f, 6f, 18f, 6f)
            horizontalLineTo(6f)
            curveTo(4.895f, 6f, 4f, 6.895f, 4f, 8f)
            verticalLineTo(16f)
            curveTo(4f, 17.105f, 4.895f, 18f, 6f, 18f)
            horizontalLineTo(18f)
            curveTo(19.105f, 18f, 20f, 17.105f, 20f, 16f)
            verticalLineTo(15f)
            moveTo(20f, 12f)
            horizontalLineTo(18f)
            curveTo(17.172f, 12f, 16.5f, 12.672f, 16.5f, 13.5f)
            curveTo(16.5f, 14.328f, 17.172f, 15f, 18f, 15f)
            horizontalLineTo(20f)
            moveTo(20f, 12f)
            verticalLineTo(15f)
        }.build()

    val Receipt: ImageVector
        get() = ImageVector.Builder(
            name = "Receipt",
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
            moveTo(4f, 2f)
            verticalLineTo(22f)
            lineTo(6.5f, 20f)
            lineTo(9f, 22f)
            lineTo(11.5f, 20f)
            lineTo(14f, 22f)
            lineTo(16.5f, 20f)
            lineTo(19f, 22f)
            verticalLineTo(2f)
            lineTo(16.5f, 4f)
            lineTo(14f, 2f)
            lineTo(11.5f, 4f)
            lineTo(9f, 2f)
            lineTo(6.5f, 4f)
            lineTo(4f, 2f)
            close()
            moveTo(8f, 8f)
            horizontalLineTo(16f)
            moveTo(8f, 12f)
            horizontalLineTo(16f)
            moveTo(8f, 16f)
            horizontalLineTo(13f)
        }.build()

    val Gift: ImageVector
        get() = ImageVector.Builder(
            name = "Gift",
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
            moveTo(20f, 12f)
            verticalLineTo(22f)
            horizontalLineTo(4f)
            verticalLineTo(12f)
            moveTo(22f, 7f)
            horizontalLineTo(2f)
            verticalLineTo(12f)
            horizontalLineTo(22f)
            verticalLineTo(7f)
            close()
            moveTo(12f, 22f)
            verticalLineTo(7f)
            moveTo(12f, 7f)
            horizontalLineTo(7.5f)
            curveTo(5.567f, 7f, 4f, 5.433f, 4f, 3.5f)
            curveTo(4f, 1.567f, 5.567f, 0f, 7.5f, 0f)
            curveTo(9.5f, 0f, 12f, 2.5f, 12f, 7f)
            close()
            moveTo(12f, 7f)
            horizontalLineTo(16.5f)
            curveTo(18.433f, 7f, 20f, 5.433f, 20f, 3.5f)
            curveTo(20f, 1.567f, 18.433f, 0f, 16.5f, 0f)
            curveTo(14.5f, 0f, 12f, 2.5f, 12f, 7f)
            close()
        }.build()

    val UserAdd: ImageVector
        get() = ImageVector.Builder(
            name = "UserAdd",
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
            moveTo(16f, 21f)
            verticalLineTo(19f)
            curveTo(16f, 17.94f, 15.58f, 16.92f, 14.83f, 16.17f)
            curveTo(14.08f, 15.42f, 13.06f, 15f, 12f, 15f)
            horizontalLineTo(5f)
            curveTo(3.94f, 15f, 2.92f, 15.42f, 2.17f, 16.17f)
            curveTo(1.42f, 16.92f, 1f, 17.94f, 1f, 19f)
            verticalLineTo(21f)
            moveTo(8.5f, 11f)
            curveTo(10.433f, 11f, 12f, 9.433f, 12f, 7.5f)
            curveTo(12f, 5.567f, 10.433f, 4f, 8.5f, 4f)
            curveTo(6.567f, 4f, 5f, 5.567f, 5f, 7.5f)
            curveTo(5f, 9.433f, 6.567f, 11f, 8.5f, 11f)
            close()
            moveTo(19f, 8f)
            verticalLineTo(14f)
            moveTo(22f, 11f)
            horizontalLineTo(16f)
        }.build()

    val Pet: ImageVector
        get() = ImageVector.Builder(
            name = "Pet",
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
            moveTo(4.5f, 10.125f)
            curveTo(5.328f, 10.125f, 6f, 9.453f, 6f, 8.625f)
            curveTo(6f, 7.797f, 5.328f, 7.125f, 4.5f, 7.125f)
            curveTo(3.672f, 7.125f, 3f, 7.797f, 3f, 8.625f)
            curveTo(3f, 9.453f, 3.672f, 10.125f, 4.5f, 10.125f)
            close()
            moveTo(9f, 6f)
            curveTo(9.828f, 6f, 10.5f, 5.328f, 10.5f, 4.5f)
            curveTo(10.5f, 3.672f, 9.828f, 3f, 9f, 3f)
            curveTo(8.172f, 3f, 7.5f, 3.672f, 7.5f, 4.5f)
            curveTo(7.5f, 5.328f, 8.172f, 6f, 9f, 6f)
            close()
            moveTo(15f, 6f)
            curveTo(15.828f, 6f, 16.5f, 5.328f, 16.5f, 4.5f)
            curveTo(16.5f, 3.672f, 15.828f, 3f, 15f, 3f)
            curveTo(14.172f, 3f, 13.5f, 3.672f, 13.5f, 4.5f)
            curveTo(13.5f, 5.328f, 14.172f, 6f, 15f, 6f)
            close()
            moveTo(19.5f, 10.125f)
            curveTo(20.328f, 10.125f, 21f, 9.453f, 21f, 8.625f)
            curveTo(21f, 7.797f, 20.328f, 7.125f, 19.5f, 7.125f)
            curveTo(18.672f, 7.125f, 18f, 7.797f, 18f, 8.625f)
            curveTo(18f, 9.453f, 18.672f, 10.125f, 19.5f, 10.125f)
            close()
            moveTo(17.344f, 11.25f)
            curveTo(16.406f, 11.25f, 15.75f, 12.047f, 15.75f, 13.125f)
            curveTo(15.75f, 14.578f, 14.062f, 15.75f, 12f, 15.75f)
            curveTo(9.938f, 15.75f, 8.25f, 14.578f, 8.25f, 13.125f)
            curveTo(8.25f, 12.047f, 7.594f, 11.25f, 6.656f, 11.25f)
            curveTo(5.203f, 11.25f, 4.5f, 12.75f, 4.5f, 14.25f)
            curveTo(4.5f, 18.375f, 7.875f, 21.75f, 12f, 21.75f)
            curveTo(16.125f, 21.75f, 19.5f, 18.375f, 19.5f, 14.25f)
            curveTo(19.5f, 12.75f, 18.797f, 11.25f, 17.344f, 11.25f)
            close()
        }.build()

    val Bell: ImageVector
        get() = ImageVector.Builder(
            name = "Bell",
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
            moveTo(18f, 8f)
            curveTo(18f, 6.409f, 17.368f, 4.883f, 16.243f, 3.757f)
            curveTo(15.117f, 2.632f, 13.591f, 2f, 12f, 2f)
            curveTo(10.409f, 2f, 8.883f, 2.632f, 7.757f, 3.757f)
            curveTo(6.632f, 4.883f, 6f, 6.409f, 6f, 8f)
            curveTo(6f, 15f, 3f, 17f, 3f, 17f)
            horizontalLineTo(21f)
            curveTo(21f, 17f, 18f, 15f, 18f, 8f)
            close()
            moveTo(13.73f, 21f)
            curveTo(13.554f, 21.303f, 13.303f, 21.554f, 13f, 21.73f)
            curveTo(12.697f, 21.906f, 12.351f, 22f, 12f, 22f)
            curveTo(11.649f, 22f, 11.303f, 21.906f, 11f, 21.73f)
            curveTo(10.697f, 21.554f, 10.446f, 21.303f, 10.27f, 21f)
        }.build()

    val Wifi: ImageVector
        get() = ImageVector.Builder(
            name = "Wifi",
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
            moveTo(12f, 20f)
            horizontalLineTo(12.01f)
            moveTo(2f, 8.82f)
            curveTo(7.52f, 3.3f, 16.48f, 3.3f, 22f, 8.82f)
            moveTo(5f, 12.86f)
            curveTo(8.87f, 8.99f, 15.13f, 8.99f, 19f, 12.86f)
            moveTo(8.5f, 16.43f)
            curveTo(10.43f, 14.5f, 13.57f, 14.5f, 15.5f, 16.43f)
        }.build()

    val Heart: ImageVector
        get() = ImageVector.Builder(
            name = "Heart",
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
            moveTo(20.84f, 4.61f)
            curveTo(20.329f, 4.099f, 19.723f, 3.693f, 19.055f, 3.417f)
            curveTo(18.388f, 3.141f, 17.672f, 3f, 16.95f, 3f)
            curveTo(16.228f, 3f, 15.512f, 3.141f, 14.845f, 3.417f)
            curveTo(14.177f, 3.693f, 13.571f, 4.099f, 13.06f, 4.61f)
            lineTo(12f, 5.67f)
            lineTo(10.94f, 4.61f)
            curveTo(9.908f, 3.578f, 8.509f, 3f, 7.05f, 3f)
            curveTo(5.591f, 3f, 4.192f, 3.578f, 3.16f, 4.61f)
            curveTo(2.128f, 5.642f, 1.55f, 7.041f, 1.55f, 8.5f)
            curveTo(1.55f, 9.959f, 2.128f, 11.358f, 3.16f, 12.39f)
            lineTo(12f, 21.35f)
            lineTo(20.84f, 12.39f)
            curveTo(21.351f, 11.879f, 21.757f, 11.273f, 22.033f, 10.605f)
            curveTo(22.309f, 9.938f, 22.45f, 9.222f, 22.45f, 8.5f)
            curveTo(22.45f, 7.778f, 22.309f, 7.062f, 22.033f, 6.395f)
            curveTo(21.757f, 5.727f, 21.351f, 5.121f, 20.84f, 4.61f)
            close()
        }.build()

    val User: ImageVector
        get() = ImageVector.Builder(
            name = "User",
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
            moveTo(20f, 21f)
            verticalLineTo(19f)
            curveTo(20f, 17.94f, 19.58f, 16.92f, 18.83f, 16.17f)
            curveTo(18.08f, 15.42f, 17.06f, 15f, 16f, 15f)
            horizontalLineTo(8f)
            curveTo(6.94f, 15f, 5.92f, 15.42f, 5.17f, 16.17f)
            curveTo(4.42f, 16.92f, 4f, 17.94f, 4f, 19f)
            verticalLineTo(21f)
            moveTo(12f, 11f)
            curveTo(14.21f, 11f, 16f, 9.21f, 16f, 7f)
            curveTo(16f, 4.79f, 14.21f, 3f, 12f, 3f)
            curveTo(9.79f, 3f, 8f, 4.79f, 8f, 7f)
            curveTo(8f, 9.21f, 9.79f, 11f, 12f, 11f)
            close()
        }.build()

    val Visibility: ImageVector
        get() = ImageVector.Builder(
            name = "Visibility",
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
            moveTo(12.0f, 4.5f)
            curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
            curveTo(2.73f, 16.39f, 7.0f, 19.5f, 12.0f, 19.5f)
            curveTo(17.0f, 19.5f, 21.27f, 16.39f, 23.0f, 12.0f)
            curveTo(21.27f, 7.61f, 17.0f, 4.5f, 12.0f, 4.5f)
            close()
            moveTo(12.0f, 17.0f)
            curveTo(9.24f, 17.0f, 7.0f, 14.76f, 7.0f, 12.0f)
            curveTo(7.0f, 9.24f, 9.24f, 7.0f, 12.0f, 7.0f)
            curveTo(14.76f, 7.0f, 17.0f, 9.24f, 17.0f, 12.0f)
            curveTo(17.0f, 14.76f, 14.76f, 17.0f, 12.0f, 17.0f)
            close()
            moveTo(12.0f, 9.0f)
            curveTo(10.34f, 9.0f, 9.0f, 10.34f, 9.0f, 12.0f)
            curveTo(9.0f, 13.66f, 10.34f, 15.0f, 12.0f, 15.0f)
            curveTo(13.66f, 15.0f, 15.0f, 13.66f, 15.0f, 12.0f)
            curveTo(15.0f, 10.34f, 13.66f, 9.0f, 12.0f, 9.0f)
            close()
        }.build()

    val VisibilityOff: ImageVector
        get() = ImageVector.Builder(
            name = "VisibilityOff",
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
            moveTo(12.0f, 7.0f)
            curveTo(14.76f, 7.0f, 17.0f, 9.24f, 17.0f, 12.0f)
            curveTo(17.0f, 13.02f, 16.69f, 13.97f, 16.16f, 14.75f)
            lineTo(18.91f, 17.5f)
            curveTo(20.59f, 16.03f, 21.99f, 14.15f, 23.0f, 12.0f)
            curveTo(21.27f, 7.61f, 17.0f, 4.5f, 12.0f, 4.5f)
            curveTo(10.63f, 4.5f, 9.33f, 4.75f, 8.13f, 5.21f)
            lineTo(10.02f, 7.1f)
            curveTo(10.64f, 7.03f, 11.27f, 7.0f, 12.0f, 7.0f)
            close()
            moveTo(2.0f, 4.27f)
            lineTo(4.28f, 6.55f)
            curveTo(2.9f, 8.04f, 1.8f, 9.88f, 1.0f, 12.0f)
            curveTo(2.73f, 16.39f, 7.0f, 19.5f, 12.0f, 19.5f)
            curveTo(13.9f, 19.5f, 15.68f, 19.06f, 17.26f, 18.28f)
            lineTo(19.73f, 20.75f)
            lineTo(21.0f, 19.48f)
            lineTo(3.27f, 3.0f)
            lineTo(2.0f, 4.27f)
            close()
            moveTo(7.53f, 9.8f)
            lineTo(9.2f, 11.47f)
            curveTo(9.08f, 11.64f, 9.0f, 11.81f, 9.0f, 12.0f)
            curveTo(9.0f, 13.66f, 10.34f, 15.0f, 12.0f, 15.0f)
            curveTo(12.19f, 15.0f, 12.36f, 14.92f, 12.53f, 14.8f)
            lineTo(14.2f, 16.47f)
            curveTo(13.52f, 16.81f, 12.78f, 17.0f, 12.0f, 17.0f)
            curveTo(9.24f, 17.0f, 7.0f, 14.76f, 7.0f, 12.0f)
            curveTo(7.0f, 11.22f, 7.19f, 10.48f, 7.53f, 9.8f)
            close()
        }.build()

    val BarChart: ImageVector
        get() = ImageVector.Builder(
            name = "BarChart",
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
            moveTo(12f, 20f)
            verticalLineTo(10f)
            moveTo(18f, 20f)
            verticalLineTo(4f)
            moveTo(6f, 20f)
            verticalLineTo(16f)
        }.build()

    val ChevronRight: ImageVector
        get() = ImageVector.Builder(
            name = "ChevronRight",
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
            moveTo(9f, 18f)
            lineTo(15f, 12f)
            lineTo(9f, 6f)
        }.build()

    val Close: ImageVector
        get() = ImageVector.Builder(
            name = "Close",
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
            moveTo(18f, 6f)
            lineTo(6f, 18f)
            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }.build()

    val Download: ImageVector
        get() = ImageVector.Builder(
            name = "Download",
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
            moveTo(21f, 15f)
            verticalLineTo(19f)
            curveTo(21f, 19.5304f, 20.7893f, 20.0391f, 20.4142f, 20.4142f)
            curveTo(20.0391f, 20.7893f, 19.5304f, 21f, 19f, 21f)
            horizontalLineTo(5f)
            curveTo(4.46957f, 21f, 3.96086f, 20.7893f, 3.58579f, 20.4142f)
            curveTo(3.21071f, 20.0391f, 3f, 19.5304f, 3f, 19f)
            verticalLineTo(15f)
            moveTo(7f, 10f)
            lineTo(12f, 15f)
            lineTo(17f, 10f)
            moveTo(12f, 15f)
            verticalLineTo(3f)
        }.build()

    val Exit: ImageVector
        get() = ImageVector.Builder(
            name = "Exit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(10.09f, 15.59f)
            lineTo(11.5f, 17f)
            lineTo(16.5f, 12f)
            lineTo(11.5f, 7f)
            lineTo(10.09f, 8.41f)
            lineTo(12.67f, 11f)
            horizontalLineTo(3f)
            verticalLineTo(13f)
            horizontalLineTo(12.67f)
            lineTo(10.09f, 15.59f)
            close()
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(9f)
            horizontalLineTo(5f)
            verticalLineTo(5f)
            horizontalLineTo(19f)
            verticalLineTo(19f)
            horizontalLineTo(5f)
            verticalLineTo(15f)
            horizontalLineTo(3f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
        }.build()

    val Mural: ImageVector
        get() = ImageVector.Builder(
            name = "Mural",
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
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.89f, 3f, 3f, 3.89f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.89f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(7f, 7f)
            horizontalLineTo(17f)
            moveTo(7f, 11f)
            horizontalLineTo(17f)
            moveTo(7f, 15f)
            horizontalLineTo(13f)
        }.build()

    val Idea: ImageVector
        get() = ImageVector.Builder(
            name = "Idea",
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
            moveTo(9f, 18f)
            horizontalLineTo(15f)
            moveTo(10f, 21f)
            horizontalLineTo(14f)
            moveTo(12f, 2f)
            curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
            curveTo(5f, 11.38f, 6.19f, 13.47f, 8f, 14.74f)
            verticalLineTo(17f)
            curveTo(8f, 17.55f, 8.45f, 18f, 9f, 18f)
            horizontalLineTo(15f)
            curveTo(15.55f, 18f, 16f, 17.55f, 16f, 17f)
            verticalLineTo(14.74f)
            curveTo(17.81f, 13.47f, 19f, 11.38f, 19f, 9f)
            curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
            close()
        }.build()
}
