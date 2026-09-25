package com.example.pxrioverde.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalUriHandler
import com.example.pxrioverde.model.analytics.KmMetric
import com.example.pxrioverde.model.analytics.TicketMetric
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.GlassCard
import com.example.pxrioverde.util.DateUtils
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

data class Appointment(
    val id: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val date: String,
    val startTime: String,
    val endTime: String,
    val destination: String,
    val kmTraveled: Int,
    val photoUrls: List<String> = emptyList(),
    val endPhotoUrls: List<String> = emptyList(),
    val isFinished: Boolean = false,
    val photoPath: String? = null,
    val vehicleName: String? = null
)

@Composable
fun AdminFinishedTripItem(
    userName: String,
    userAvatarUrl: String? = null,
    km: Int,
    date: String,
    photoPath: String?,
    modifier: Modifier = Modifier
) {
    var showAvatarDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .clickable { if (userAvatarUrl != null) showAvatarDialog = true },
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(CorporateIcons.User),
                        error = rememberVectorPainter(CorporateIcons.User)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DateUtils.formatSimpleDate(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    color = Color(0xFF2D6A4F).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$km KM",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2D6A4F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (photoPath != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                val imageUrl = formatTripPhotoUrl(photoPath)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto da viagem",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    if (showAvatarDialog) {
        AvatarFullscreenDialog(
            url = userAvatarUrl!!,
            name = userName,
            onDismiss = { showAvatarDialog = false }
        )
    }
}

@Composable
fun TicketStatusBadge(status: com.example.pxrioverde.model.TicketStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        com.example.pxrioverde.model.TicketStatus.NA_FILA -> Color(0xFFD32F2F) to "NA FILA"
        com.example.pxrioverde.model.TicketStatus.EM_ANALISE -> Color(0xFFF57C00) to "EM ANÁLISE"
        com.example.pxrioverde.model.TicketStatus.AGUARDANDO_PECA -> Color(0xFF1976D2) to "AGUARDANDO PEÇA"
        com.example.pxrioverde.model.TicketStatus.EM_ATENDIMENTO -> Color(0xFF388E3C) to "EM ATENDIMENTO"
        com.example.pxrioverde.model.TicketStatus.RESOLVIDO -> Color(0xFF2E7D32) to "RESOLVIDO"
        com.example.pxrioverde.model.TicketStatus.CANCELADO -> Color(0xFF757575) to "CANCELADO"
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TicketRatingBar(rating: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFB300) else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun KmSummaryCard(metrics: List<KmMetric>) {
    val totalKm = metrics.sumOf { it.totalKm }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total KM no Mês", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Text("$totalKm km", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TicketSummaryCard(metrics: List<TicketMetric>) {
    val avgTime = if (metrics.isNotEmpty()) metrics.map { it.averageResolutionTimeHours }.average() else 0.0
    val avgTimeStr = ((avgTime * 10).toInt() / 10.0).toString()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tempo Médio de Resposta", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Text("${avgTimeStr}h", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppointmentRowItem(appointment: Appointment, onViewPhotosClick: () -> Unit) {
    var showAvatarDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = appointment.userAvatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .clickable { if (appointment.userAvatarUrl != null) showAvatarDialog = true },
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(CorporateIcons.User),
                error = rememberVectorPainter(CorporateIcons.User)
            )
            
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.userName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(appointment.destination, color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)
                Text("Veículo: ${appointment.vehicleName ?: "---"}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                Text("${DateUtils.formatSimpleDate(appointment.date)} • ${appointment.startTime}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                if (appointment.isFinished) {
                    Text("${appointment.kmTraveled} km percorridos", color = Color(0xFF2D6A4F), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            val photoCount = appointment.photoUrls.size + appointment.endPhotoUrls.size
            IconButton(onClick = onViewPhotosClick) {
                Icon(
                    CorporateIcons.Camera,
                    contentDescription = if (photoCount > 0) "Ver $photoCount fotos" else "Sem fotos registradas",
                    tint = if (photoCount > 0) MaterialTheme.colorScheme.primary else Color.LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showAvatarDialog) {
        AvatarFullscreenDialog(
            url = appointment.userAvatarUrl!!,
            name = appointment.userName,
            onDismiss = { showAvatarDialog = false }
        )
    }
}

private fun formatTripPhotoUrl(url: String): String {
    val normalized = url.trim()
    if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
        return normalized
            .replace("/storage/v1/object/public/", "/storage/v1/render/image/public/")
            .let { "$it?width=800&quality=75&resize=contain" }
    }
    return "https://yfkpqeeoticvajgitkrw.supabase.co/storage/v1/render/image/public/trip_photos/$normalized?width=800&quality=75&resize=contain"
}

@Composable
fun AvatarFullscreenDialog(url: String, name: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 700.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = url,
                    contentDescription = "Foto de perfil de $name",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                // Overlay com nome e botão fechar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(CorporateIcons.Close, contentDescription = "Fechar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun PhotoDetailsDialog(appointment: Appointment, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val photoLabels = listOf("Frente", "Traseira", "Lateral Esq.", "Lateral Dir.")
    
    fun formatSupabaseUrl(url: String): String {
        return formatTripPhotoUrl(url)
    }

    val allPhotos = remember(appointment) {
        val start = appointment.photoUrls.mapIndexed { index, url -> 
            Triple(formatSupabaseUrl(url), photoLabels.getOrNull(index) ?: "Foto", "INÍCIO DA VIAGEM")
        }
        val end = appointment.endPhotoUrls.mapIndexed { index, url -> 
            Triple(formatSupabaseUrl(url), photoLabels.getOrNull(index) ?: "Foto", "FIM DA VIAGEM")
        }
        start + end
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Fotos da Jornada", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7F6), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Motorista: ${appointment.userName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Veículo: ${appointment.vehicleName ?: "---"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Km Percorrido: ${appointment.kmTraveled} km", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                if (allPhotos.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { allPhotos.size })
                    
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F7F6)),
                            pageSpacing = 12.dp
                        ) { page ->
                            val (url, label, stage) = allPhotos[page]
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "$stage - $label",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                
                                IconButton(
                                    onClick = { uriHandler.openUri(url) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                ) {
                                    Icon(CorporateIcons.Download, contentDescription = "Baixar", tint = Color.White, modifier = Modifier.size(20.dp))
                                }

                                Surface(
                                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
                                    color = if (stage.contains("INÍCIO")) Color(0xFF2D6A4F) else Color(0xFFE07A5F),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        stage,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Surface(
                                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        label,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // SETAS DE NAVEGAÇÃO ELEGANTES
                        if (pagerState.currentPage > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp)
                                    .size(40.dp)
                                    .clickable { 
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                    },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.8f),
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Anterior",
                                    tint = Color(0xFF1B4332),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (pagerState.currentPage < allPhotos.size - 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp)
                                    .size(40.dp)
                                    .clickable { 
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                    },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.8f),
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Próximo",
                                    tint = Color(0xFF1B4332),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Indicadores
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(allPhotos.size) { iteration ->
                            val isCurrent = pagerState.currentPage == iteration
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(width = if (isCurrent) 24.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) Color(0xFF2D6A4F) else Color.LightGray.copy(alpha = 0.5f))
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F7F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(CorporateIcons.Alert, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Nenhuma foto disponível", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
                ) {
                    Text("FECHAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
