package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.ComunicadoState
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.StrategicEmptyState
import com.example.pxrioverde.viewmodel.AbsenceViewModel
import com.example.pxrioverde.ui.hr.ComunicadoAusenciaScreen
import com.example.pxrioverde.util.CommonBackHandler
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAbsencePortal(
    userId: String,
    userName: String,
    avatarUrl: String?,
    sector: String,
    viewModel: AbsenceViewModel,
    onBack: () -> Unit
) {
    val userAbsences by viewModel.userAbsences.collectAsState()
    val selectedAbsence by viewModel.selectedAbsence.collectAsState()
    val supervisors by viewModel.fixedSupervisors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    CommonBackHandler(enabled = selectedAbsence != null) {
        viewModel.selectAbsence(null)
    }
    
    var showCreateForm by remember { mutableStateOf(false) }
    var newAbsenceState by remember { mutableStateOf(ComunicadoState(
        userId = userId,
        userName = userName,
        avatarUrl = avatarUrl,
        sector = sector
    )) }

    LaunchedEffect(userId) {
        viewModel.loadUserAbsences(userId)
    }

    if (showCreateForm) {
        ComunicadoAusenciaScreen(
            state = newAbsenceState,
            onStateChange = { newAbsenceState = it },
            onSave = { state ->
                viewModel.submitAbsence(state) {
                    showCreateForm = false
                    viewModel.loadUserAbsences(userId)
                }
            },
            onBack = { showCreateForm = false },
            supervisors = supervisors,
            isAdminMode = false
        )
    } else if (selectedAbsence != null) {
        ComunicadoAusenciaScreen(
            state = selectedAbsence!!,
            onStateChange = { viewModel.selectAbsence(it) },
            onSave = { /* Read-only for user when viewing history */ },
            onBack = { viewModel.selectAbsence(null) },
            supervisors = supervisors,
            isReadOnly = true,
            isAdminMode = false
        )
    } else {
        Scaffold(
            topBar = {
                Surface(color = Color(0xFF1B4332), shadowElevation = 4.dp) {
                    TopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        title = { Text("Meus Comunicados", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(CorporateIcons.Back, "Voltar", tint = Color.White)
                            }
                        },
                        actions = {
                            if (userAbsences.isNotEmpty()) {
                                TextButton(onClick = { 
                                    newAbsenceState = ComunicadoState(
                                        userId = userId,
                                        userName = userName,
                                        avatarUrl = avatarUrl,
                                        sector = sector
                                    )
                                    showCreateForm = true 
                                }) {
                                    Text("+ NOVO", color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B4332))
                    )
                }
            },
            containerColor = Color(0xFFF5F7F6)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isLoading && userAbsences.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1B4332))
                } else if (userAbsences.isEmpty()) {
                    StrategicEmptyState(
                        title = "Nenhum comunicado",
                        description = "Você ainda não enviou nenhum comunicado de ausência ao RH.",
                        icon = CorporateIcons.Exit,
                        buttonText = "NOVO COMUNICADO",
                        onButtonClick = { 
                            newAbsenceState = ComunicadoState(
                                userId = userId,
                                userName = userName,
                                avatarUrl = avatarUrl,
                                sector = sector
                            )
                            showCreateForm = true 
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userAbsences) { absence ->
                            UserAbsenceItemCard(absence, onClick = { 
                                if (absence.adminFeedback != null) {
                                    viewModel.markAbsenceAsViewed(absence.id ?: "")
                                }
                                viewModel.selectAbsence(absence) 
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAbsenceItemCard(absence: ComunicadoState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    onLoading = {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF1B4332))
                        }
                    },
                    onFailure = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B4332).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(CorporateIcons.Exit, null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B4332).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        CorporateIcons.Exit,
                        contentDescription = null,
                        tint = Color(0xFF1B4332),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(absence.type.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(absence.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            if (absence.adminFeedback != null) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "AVALIADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFFFA000).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "PENDENTE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
