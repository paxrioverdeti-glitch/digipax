package com.example.pxrioverde.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.pxrioverde.model.DanoEncontradoDto
import com.example.pxrioverde.model.VehicleDamageReportItem
import com.example.pxrioverde.viewmodel.VehicleDamageViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

/**
 * Aba Administrativa dedicada ao Relatório de Avarias e Danos da Frota.
 * Exibe vistorias com `houve_novo_dano == true`, dados do motorista, veículo,
 * resumo da análise automática, badges de gravidade e galeria comparativa de fotos.
 */
@Composable
fun VehicleDamageReportsScreen(
    refreshRequest: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: VehicleDamageViewModel = koinViewModel()
) {
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val onlyDamaged by viewModel.onlyDamagedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedImageModal by viewModel.selectedImageForModal.collectAsState()
    val analyzingTripIds by viewModel.analyzingTripIds.collectAsState()

    // Recarrega sempre que a aba é aberta (e quando o admin pede atualização),
    // para mostrar as viagens que acabaram de ser finalizadas.
    LaunchedEffect(refreshRequest) {
        viewModel.loadReports()
    }

    // Filtra relatórios por busca textual (motorista, placa ou nome do carro)
    val filteredReports = remember(reports, searchQuery) {
        if (searchQuery.isBlank()) {
            reports
        } else {
            val query = searchQuery.lowercase().trim()
            reports.filter { item ->
                item.driverName.lowercase().contains(query) ||
                item.carName.lowercase().contains(query) ||
                item.carLicensePlate.lowercase().contains(query) ||
                item.resumoGeral.lowercase().contains(query)
            }
        }
    }


    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            
            // CABEÇALHO DA ABA
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Avarias & Vistorias de Lataria",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Relatório de danos identificados automaticamente via IA",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = { viewModel.loadReports() },
                    modifier = Modifier.clip(CircleShape).background(Color(0xFFEEF2FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Atualizar Vistorias",
                        tint = Color(0xFF3B82F6)
                    )
                }
            }

            // CARDS DE RESUMO DE MÉTRICAS
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Números com nome claro: o que é vistoria, o que é dano e o que precisa de olho humano
                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Vistorias",
                    value = reports.size.toString(),
                    icon = Icons.Outlined.DirectionsCar,
                    iconColor = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFFEFF6FF)
                )

                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Com novo dano",
                    value = reports.count { it.reviewStatus == "confirmado" || (it.reviewStatus == null && it.houveNovoDano) }.toString(),
                    icon = Icons.Outlined.Warning,
                    iconColor = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFFEF2F2)
                )

                MetricSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Para revisar",
                    value = reports.count { it.reviewStatus == null && it.needsReview && !it.houveNovoDano }.toString(),
                    icon = Icons.Outlined.Search,
                    iconColor = Color(0xFFF59E0B),
                    backgroundColor = Color(0xFFFFFBEB)
                )
            }

            Text(
                text = "Vistorias: viagens finalizadas com fotos. Com novo dano: a IA tem 60% ou mais de certeza. Para revisar: a IA não conseguiu analisar ou as fotos não servem.",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val reviewError by viewModel.reviewError.collectAsState()
            reviewError?.let { msg ->
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(msg, color = Color(0xFF991B1B), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.clearReviewError() }) { Text("OK") }
                    }
                }
            }

            // ANALISAR TODAS AS PENDENTES (uma por vez, com progresso e botão de parar)
            val batchProgress by viewModel.batchProgress.collectAsState()
            val pendingCount = reports.count { it.isAnalysisPending }
            batchProgress?.let { (done, total) ->
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Analisando ${done + 1} de $total vistorias...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.stopBatch() }) { Text("Parar") }
                        }
                        LinearProgressIndicator(
                            progress = { done.toFloat() / total },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Uma vistoria por vez. Mantenha o app aberto durante o processamento.",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            } ?: run {
                if (pendingCount > 0) {
                    Button(
                        onClick = { viewModel.analyzeAllPending() },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        Text(
                            "Analisar todas as pendentes ($pendingCount)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // BARRA DE PESQUISA
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                placeholder = { Text("Buscar motorista, veículo ou placa...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // CHIPS DE FILTRO
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = onlyDamaged,
                    onClick = { viewModel.setOnlyDamagedFilter(true) },
                    label = { Text("Precisam de atenção") },
                    leadingIcon = if (onlyDamaged) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )

                FilterChip(
                    selected = !onlyDamaged,
                    onClick = { viewModel.setOnlyDamagedFilter(false) },
                    label = { Text("Todas as Vistorias") },
                    leadingIcon = if (!onlyDamaged) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            // CONTEÚDO PRINCIPAL (LISTA DE AVARIAS)
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color(0xFFF0FDF4)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF16A34A)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Nenhum relatório encontrado" else "Nenhum novo dano detectado",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tente outros termos de pesquisa." else "Todas as vistorias veiculares recentes foram concluídas sem novas avarias.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredReports, key = { it.id }) { report ->
                        VehicleDamageReportCard(
                            item = report,
                            onImageClick = { title, url, danos ->
                                viewModel.openImageModal(title, url, danos)
                            },
                            onReview = { status -> viewModel.reviewInspection(report.tripId, status) },
                            isAnalyzing = report.tripId in analyzingTripIds,
                            onAnalyzeNow = { viewModel.analyzeNow(report.tripId) }
                        )
                    }
                }
            }
        }

        // DIÁLOGO MODAL PARA EXPANDIR FOTOS DE AUDITORIA
        selectedImageModal?.let { modal ->
            val title = modal.title
            val url = modal.url
            Dialog(onDismissRequest = { viewModel.closeImageModal() }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            IconButton(onClick = { viewModel.closeImageModal() }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        DamagePhoto(
                            url = url,
                            danos = modal.danos,
                            contentDescription = title,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        )
                        if (modal.danos.any { it.caixa != null }) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Quadrado vermelho: dano provável. Amarelo: indício fraco. A posição é aproximada.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card individual representando um relatório de avaria de veículo com
 * dados do motorista, dados do carro, resumo da análise automática, badges e galeria de fotos.
 */
@Composable
private fun VehicleDamageReportCard(
    item: VehicleDamageReportItem,
    onImageClick: (title: String, url: String, danos: List<DanoEncontradoDto>) -> Unit,
    onReview: (status: String?) -> Unit,
    isAnalyzing: Boolean = false,
    onAnalyzeNow: () -> Unit = {}
) {
    // A decisão do gestor vale mais que a da IA
    val isDamage = when (item.reviewStatus) {
        "confirmado" -> true
        "falso_alarme" -> false
        else -> item.houveNovoDano
    }
    val isPendingReview = item.reviewStatus == null && item.needsReview && !item.houveNovoDano
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // CABEÇALHO DO CARD (MOTORISTA & STATUS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.driverAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.driverAvatarUrl,
                            contentDescription = item.driverName,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.driverName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = formatTimestamp(item.analyzedAt),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // BADGE DE STATUS DA VISTORIA (3 estados: dano / revisar / limpo)
                if (isPendingReview) {
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Text(
                            text = "REVISAR FOTOS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                } else if (isDamage) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.reviewStatus == "confirmado") "AVARIA CONFIRMADA" else "NOVO DANO DETECTADO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = if (item.reviewStatus == "falso_alarme") "FALSO ALARME" else "SEM NOVO DANO",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // DADOS DO VEÍCULO
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.carName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF334155)
                )
                if (item.carLicensePlate.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.carLicensePlate.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // RESUMO DA ANÁLISE AUTOMÁTICA
            if (item.resumoGeral.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Resumo da Análise (IA):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = item.resumoGeral,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            // LISTA DE DANOS DETALHADOS
            if (item.danosEncontrados.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Avarias Identificadas (${item.danosEncontrados.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.danosEncontrados.forEach { dano ->
                        DanoItemCard(dano = dano)
                    }
                }
            }

            // ANALISAR AGORA: vistoria que a IA ainda não analisou (ou falhou)
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Analisando com a IA... (pode levar até 1 minuto)",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            } else if (item.isAnalysisPending) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onAnalyzeNow,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Analisar agora com a IA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                }
            }

            // DECISÃO DO GESTOR (Passo 3)
            Spacer(modifier = Modifier.height(14.dp))
            if (item.reviewStatus == null) {
                Text(
                    text = "Sua avaliação:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onReview("confirmado") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) { Text("Confirmar avaria", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { onReview("falso_alarme") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Falso alarme", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534)) }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (item.reviewStatus == "confirmado") "Você confirmou esta avaria." else "Você marcou como falso alarme.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onReview(null) }) { Text("Desfazer", fontSize = 12.sp) }
                }
            }

            // ESPAÇO DA GALERIA DE FOTOS PARA AUDITORIA DO GESTOR
            val hasPhotos = item.initialPhotoUrls.isNotEmpty() || item.finalPhotoUrls.isNotEmpty()
            if (hasPhotos) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Fotos de Vistoria (Auditoria):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.initialPhotoUrls.isNotEmpty()) {
                        PhotoGallerySection(
                            title = "Fotos Iniciais (Antes da Corrida)",
                            urls = item.initialPhotoUrls,
                            // Danos que já estavam no carro aparecem marcados na foto INICIAL desta viagem
                            danosForIndex = { index ->
                                if (item.reviewStatus == "falso_alarme") emptyList()
                                else item.danosEncontrados.filter { it.isBetweenTrips && it.fotoIndex == index }
                            },
                            onImageClick = { url, danos -> onImageClick("Foto Inicial", url, danos) }
                        )
                    }

                    if (item.finalPhotoUrls.isNotEmpty()) {
                        PhotoGallerySection(
                            title = "Fotos Finais (Depois da Corrida)",
                            urls = item.finalPhotoUrls,
                            // Só mostra quadrados se a IA apontou e o gestor não marcou falso alarme
                            danosForIndex = { index ->
                                if (item.reviewStatus == "falso_alarme") emptyList()
                                else item.danosEncontrados.filter { !it.isBetweenTrips && it.fotoIndex == index }
                            },
                            onImageClick = { url, danos -> onImageClick("Foto Final", url, danos) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exibe cada avaria individual com seu badge colorido de gravidade.
 */
@Composable
private fun DanoItemCard(dano: DanoEncontradoDto) {
    val (severityColor, severityBg) = when (dano.gravidade.lowercase().trim()) {
        "grave" -> Color(0xFFDC2626) to Color(0xFFFEF2F2)
        "moderado" -> Color(0xFFD97706) to Color(0xFFFFFBEB)
        else -> Color(0xFFCA8A04) to Color(0xFFFEFCE8)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dano.tipoDano.ifBlank { "Avaria" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )
                    if (dano.angulo.isNotBlank()) {
                        Text(
                            text = " • ${dano.angulo}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                if (dano.isBetweenTrips) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Já estava no carro ao pegar (surgiu depois da viagem anterior)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }
                if (dano.descricao.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dano.descricao,
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
                dano.confianca?.let { conf ->
                    val percent = (conf * 100).toInt()
                    val isLikely = conf >= 0.6
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLikely) "Certeza da IA: $percent%"
                               else "Indício fraco ($percent%): pode ser reflexo, sujeira ou ângulo",
                        fontSize = 11.sp,
                        fontWeight = if (isLikely) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLikely) Color(0xFF1B4332) else Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = severityBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = dano.gravidade.uppercase().ifBlank { "LEVE" },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = severityColor
                )
            }
        }
    }
}

/**
 * Seção de galeria horizontal para miniaturas das fotos de vistoria.
 */
@Composable
private fun PhotoGallerySection(
    title: String,
    urls: List<String>,
    danosForIndex: (Int) -> List<DanoEncontradoDto> = { emptyList() },
    onImageClick: (url: String, danos: List<DanoEncontradoDto>) -> Unit
) {
    Column {
        Text(title, fontSize = 11.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(urls) { index, url ->
                val danos = danosForIndex(index)
                val hasBox = danos.any { it.caixa != null }
                DamagePhoto(
                    url = url,
                    danos = danos,
                    contentDescription = title,
                    showLabels = false,
                    modifier = Modifier
                        .height(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            if (hasBox) 2.dp else 1.dp,
                            if (hasBox) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onImageClick(url, danos) }
                )
            }
        }
    }
}

@Composable
private fun MetricSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(text = title, fontSize = 11.sp, color = Color(0xFF475569))
            }
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        "$day/$month/$year às $hour:$minute"
    } catch (_: Exception) {
        ""
    }
}
