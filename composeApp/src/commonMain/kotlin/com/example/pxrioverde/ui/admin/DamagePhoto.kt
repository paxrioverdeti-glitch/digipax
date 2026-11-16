package com.example.pxrioverde.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.pxrioverde.model.DanoEncontradoDto

/**
 * Foto com quadrados vermelhos em volta dos danos que a IA encontrou.
 * A imagem é desenhada no próprio formato dela (proporção), para o quadrado cair
 * no lugar certo. A posição vem da IA e é aproximada: serve para guiar o olho.
 */
@Composable
fun DamagePhoto(
    url: String,
    danos: List<DanoEncontradoDto>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    var ratio by remember(url) { mutableStateOf(4f / 3f) }
    val boxes = danos.filter { it.caixa?.size == 4 }
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.aspectRatio(ratio)) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) ratio = size.width / size.height
            }
        )
        if (boxes.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = if (showLabels) 3f * density else 2f
                boxes.forEach { dano ->
                    val (ymin, xmin, ymax, xmax) = dano.caixa!!
                    val strong = (dano.confianca ?: 1.0) >= 0.6
                    val color = if (strong) Color(0xFFEF4444) else Color(0xFFF59E0B)
                    val topLeft = Offset((xmin * size.width).toFloat(), (ymin * size.height).toFloat())
                    val boxSize = Size(((xmax - xmin) * size.width).toFloat(), ((ymax - ymin) * size.height).toFloat())
                    drawRect(color, topLeft, boxSize, style = Stroke(width = stroke))

                    if (showLabels) {
                        val pct = dano.confianca?.let { " ${(it * 100).toInt()}%" } ?: ""
                        val layout = textMeasurer.measure(
                            "${dano.tipoDano}$pct",
                            style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        val labelY = (topLeft.y - layout.size.height - 4f).coerceAtLeast(0f)
                        drawRect(
                            color,
                            Offset(topLeft.x, labelY),
                            Size(layout.size.width + 10f, layout.size.height + 4f)
                        )
                        drawText(layout, topLeft = Offset(topLeft.x + 5f, labelY + 2f))
                    }
                }
            }
        }
    }
}
