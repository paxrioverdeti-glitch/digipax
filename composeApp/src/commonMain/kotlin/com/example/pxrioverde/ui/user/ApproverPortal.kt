package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.example.pxrioverde.model.ComunicadoState
import com.example.pxrioverde.ui.components.AdminEmptyState
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.hr.ComunicadoAusenciaScreen
import com.example.pxrioverde.viewmodel.AbsenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproverPortal(
    approverId: String,
    viewModel: AbsenceViewModel
) {
    val absences by viewModel.approverAbsences.collectAsState()
    val supervisors by viewModel.fixedSupervisors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAbsence by viewModel.selectedAbsence.collectAsState()

    LaunchedEffect(approverId) {
        viewModel.loadApproverAbsences(approverId)
    }

    if (selectedAbsence == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F6))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (absences.isEmpty() && !isLoading) {
                    AdminEmptyState(
                        title = "Tudo em dia!",
                        description = "Não há solicitações de ausência aguardando sua revisão no momento.",
                        icon = rememberVectorPainter(CorporateIcons.Check),
                        actionLabel = "Atualizar",
                        onActionClick = { viewModel.loadApproverAbsences(approverId) }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(absences) { absence ->
                            ApproverAbsenceItemCard(absence, onClick = { viewModel.selectAbsence(absence) })
                        }
                    }
                }
            }
        }
    } else {
        ComunicadoAusenciaScreen(
            state = selectedAbsence!!,
            onStateChange = { viewModel.selectAbsence(it) },
            onSave = { updatedState ->
                updatedState.adminFeedback?.let { feedback ->
                    viewModel.updateFeedback(updatedState.id ?: "", feedback) {
                        viewModel.selectAbsence(null)
                        viewModel.loadApproverAbsences(approverId)
                    }
                } ?: run { viewModel.selectAbsence(null) }
            },
            onBack = { viewModel.selectAbsence(null) },
            supervisors = supervisors,
            isReadOnly = true,
            isAdminMode = true // Permite avaliar
        )
    }
}

@Composable
fun ApproverAbsenceItemCard(absence: ComunicadoState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarUrl = absence.userAvatarUrl
            val isValidUrl = !avatarUrl.isNullOrBlank() && (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))
            
            if (isValidUrl) {
                KamelImage(
                    resource = asyncPainterResource(avatarUrl!!),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF1B4332))
                        }
                    },
                    onFailure = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B4332).copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CorporateIcons.User, 
                                contentDescription = null, 
                                tint = Color(0xFF1B4332), 
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B4332).copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CorporateIcons.User, 
                        contentDescription = null, 
                        tint = Color(0xFF1B4332), 
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = absence.userName, 
                    fontWeight = FontWeight.ExtraBold, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1B4332)
                )
                Text(
                    text = "${absence.type.label} • ${absence.date}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
            
            if (absence.adminFeedback != null) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle, 
                        contentDescription = "Avaliado", 
                        tint = Color(0xFF2E7D32), 
                        modifier = Modifier.padding(6.dp).size(20.dp)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFFFA000).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PENDENTE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = CorporateIcons.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
