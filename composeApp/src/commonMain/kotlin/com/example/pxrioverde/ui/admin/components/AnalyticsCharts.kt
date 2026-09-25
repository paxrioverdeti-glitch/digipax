package com.example.pxrioverde.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pxrioverde.model.analytics.KmMetric
import com.example.pxrioverde.model.analytics.TicketMetric
import com.example.pxrioverde.model.analytics.TicketGroupMetric

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
fun KmUsageChart(metrics: List<KmMetric>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)

    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "KM Rodado por Mês", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            if (metrics.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sem dados de KM no período", color = Color.Gray)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height - 40f // Reservar espaço para labels
                    val maxKm = (metrics.maxOfOrNull { it.totalKm } ?: 1.0).coerceAtLeast(1.0)
                    val spacing = width / (metrics.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val fillPath = Path()

                    metrics.forEachIndexed { i, metric ->
                        val x = i * spacing
                        val y = height - (metric.totalKm.toFloat() / maxKm.toFloat() * height)

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                        
                        if (i == metrics.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }

                        // Desenhar Nome do Mês
                        val textLayout = textMeasurer.measure(metric.month, style = labelStyle)
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = androidx.compose.ui.geometry.Offset(x - (textLayout.size.width / 2), height + 10f)
                        )
                    }

                    // 1. Desenhar Área Sombreada (Gradiente)
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 2. Desenhar Linha
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Desenhar Pontos
                    metrics.forEachIndexed { i, metric ->
                        val x = i * spacing
                        val y = height - (metric.totalKm.toFloat() / maxKm.toFloat() * height)
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketGroupPerformanceCard(ti: TicketGroupMetric, gobah: TicketGroupMetric) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Eficiência de Resolução", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Taxa de chamados finalizados vs pendentes", 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PerformanceRow(ti, MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(20.dp))
            PerformanceRow(gobah, Color(0xFF1976D2)) // Azul para Gobah para diferenciar visualmente
        }
    }
}

@Composable
private fun PerformanceRow(metric: TicketGroupMetric, color: Color) {
    val progress = metric.percentResolved / 100f
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                metric.name, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold
            )
            Text(
                "${metric.percentResolved.toInt()}% Concluído", 
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Black
            )
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${metric.pending} pendentes", 
                style = MaterialTheme.typography.labelSmall, 
                color = if (metric.pending > 0) Color(0xFFE07A5F) else Color.Gray
            )
            Text(
                "${metric.resolved} resolvidos", 
                style = MaterialTheme.typography.labelSmall, 
                color = Color.Gray
            )
        }
    }
}
