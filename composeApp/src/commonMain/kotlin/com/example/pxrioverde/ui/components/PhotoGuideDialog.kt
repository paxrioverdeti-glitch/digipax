package com.example.pxrioverde.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Guia mostrado antes de cada foto da vistoria, para que as fotos de início e fim
 * saiam do mesmo jeito (mesmo ângulo e distância). Isso é o que mais melhora a IA.
 * Índices seguem a ordem de captura do app: 0 Frente, 1 Traseira, 2 Lateral Esq., 3 Lateral Dir.
 */
@Composable
fun PhotoGuideDialog(photoIndex: Int, onOpenCamera: () -> Unit, onDismiss: () -> Unit) {
    val green = Color(0xFF1B4332)
    val (title, tip) = when (photoIndex) {
        0 -> "Frente do veículo" to "Fique de frente para o carro, no meio, a uns 3 passos. Pegue o para-choque e os faróis inteiros."
        1 -> "Traseira do veículo" to "Fique atrás do carro, no meio, a uns 3 passos. Pegue o para-choque, as lanternas e a placa."
        2 -> "Lateral esquerda" to "Fique ao lado esquerdo, na altura das portas, a uns 4 passos. Pegue o carro inteiro, da frente à traseira."
        else -> "Lateral direita" to "Fique ao lado direito, na altura das portas, a uns 4 passos. Pegue o carro inteiro, da frente à traseira."
    }
    val isSide = photoIndex >= 2

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Foto ${photoIndex + 1} de 4: $title", fontWeight = FontWeight.Bold, color = green) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Moldura de exemplo: o carro deve ocupar quase todo o quadro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFF5F7F6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        // moldura tracejada (área que o carro deve preencher)
                        drawRoundRect(
                            color = green,
                            style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))),
                            cornerRadius = CornerRadius(16f)
                        )
                        // silhueta simples do carro
                        val carW = if (isSide) size.width * 0.86f else size.width * 0.5f
                        val bodyH = size.height * 0.36f
                        val left = (size.width - carW) / 2
                        val bodyTop = size.height * 0.42f
                        val carColor = green.copy(alpha = 0.35f)
                        drawRoundRect(carColor, Offset(left, bodyTop), Size(carW, bodyH), CornerRadius(18f))
                        val roofW = carW * (if (isSide) 0.55f else 0.75f)
                        drawRoundRect(
                            carColor,
                            Offset(left + (carW - roofW) / 2, bodyTop - bodyH * 0.65f),
                            Size(roofW, bodyH * 0.75f),
                            CornerRadius(22f)
                        )
                        val wheelR = bodyH * 0.32f
                        val wheelY = bodyTop + bodyH
                        if (isSide) {
                            drawCircle(green.copy(alpha = 0.6f), wheelR, Offset(left + carW * 0.22f, wheelY))
                            drawCircle(green.copy(alpha = 0.6f), wheelR, Offset(left + carW * 0.78f, wheelY))
                        } else {
                            drawCircle(green.copy(alpha = 0.6f), wheelR * 0.8f, Offset(left + carW * 0.15f, wheelY))
                            drawCircle(green.copy(alpha = 0.6f), wheelR * 0.8f, Offset(left + carW * 0.85f, wheelY))
                        }
                    }
                }
                Text(tip, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Celular deitado (horizontal), sem flash, sem pessoas na frente. Repita igual na volta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenCamera,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green)
            ) { Text("Abrir câmera", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}
