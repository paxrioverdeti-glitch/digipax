package com.example.pxrioverde.ui.hr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.example.pxrioverde.model.AbsenceType
import com.example.pxrioverde.model.ComunicadoState
import com.example.pxrioverde.model.AdminFeedback
import com.example.pxrioverde.model.Supervisor
import com.example.pxrioverde.ui.components.CorporateIcons
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadoAusenciaScreen(
    state: ComunicadoState,
    onStateChange: (ComunicadoState) -> Unit,
    onSave: (ComunicadoState) -> Unit,
    onBack: () -> Unit,
    supervisors: List<Supervisor> = emptyList(),
    isReadOnly: Boolean = false,
    isAdminMode: Boolean = true // Por padrão para fins de teste/demo no admin
) {
    val scrollState = rememberScrollState()
    val corporateGreen = Color(0xFF1B4332)
    val lightGrayBg = Color(0xFFF8F9FA)
    
    var showEvaluationDialog by remember { mutableStateOf(false) }

    // Estados para Date e Time Pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var currentPickingField by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    fun formatSelectedDate(millis: Long?): String {
        if (millis == null) return ""
        val instant = Instant.fromEpochMilliseconds(millis)
        val date = instant.toLocalDateTime(TimeZone.UTC).date
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    }

    fun formatSelectedTime(hour: Int, minute: Int): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = formatSelectedDate(datePickerState.selectedDateMillis)
                    if (currentPickingField == "date") {
                        onStateChange(state.copy(date = formatted))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = formatSelectedTime(timePickerState.hour, timePickerState.minute)
                    when (currentPickingField) {
                        "expectedTime" -> onStateChange(state.copy(expectedTime = formatted))
                        "effectiveTime" -> onStateChange(state.copy(effectiveTime = formatted))
                        "exitTime" -> onStateChange(state.copy(exitTime = formatted))
                        "returnTime" -> onStateChange(state.copy(returnTime = formatted))
                        "originalTime" -> onStateChange(state.copy(originalTime = formatted))
                        "newTime" -> onStateChange(state.copy(newTime = formatted))
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("CANCELAR") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Comunicado de Ausência", 
                        color = Color.White, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(CorporateIcons.Back, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = corporateGreen)
            )
        },
        containerColor = lightGrayBg,
        floatingActionButton = {
            if (isAdminMode && isReadOnly && state.adminFeedback == null) {
                ExtendedFloatingActionButton(
                    onClick = { showEvaluationDialog = true },
                    containerColor = corporateGreen,
                    contentColor = Color.White,
                    icon = { Icon(CorporateIcons.Check, null) },
                    text = { Text("AVALIAR AGORA") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SEÇÃO DO ADMINISTRADOR (Parecer RH) ---
            if (isReadOnly && state.adminFeedback != null) {
                AdminFeedbackSection(state.adminFeedback)
            }

            // --- SEÇÃO 1: IDENTIFICAÇÃO ---
            SectionCard(title = "Identificação") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar do Usuário
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFF1B4332).copy(alpha = 0.1f)
                    ) {
                        val avatarUrl = state.userAvatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            io.kamel.image.KamelImage(
                                resource = io.kamel.image.asyncPainterResource(avatarUrl),
                                contentDescription = "Avatar de ${state.userName}",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                onLoading = {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
                                }
                            )
                        } else {
                            Icon(
                                CorporateIcons.User,
                                contentDescription = null,
                                tint = Color(0xFF1B4332),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ReadOnlyField(label = "Colaborador", value = state.userName.ifBlank { "---" })
                        ReadOnlyField(label = "Setor", value = state.sector.ifBlank { "---" })
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                TypeSelector(
                    selectedType = state.type,
                    onTypeSelected = { onStateChange(state.copy(type = it)) },
                    enabled = !isReadOnly
                )

                if (!isReadOnly) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ApproverSelector(
                        selectedApproverName = state.targetApproverName,
                        supervisors = supervisors,
                        onSupervisorSelected = { supervisor ->
                            onStateChange(state.copy(
                                targetApproverId = supervisor.id,
                                targetApproverName = supervisor.name
                            ))
                        }
                    )
                } else if (!state.targetApproverName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ReadOnlyField(label = "Encaminhado para", value = state.targetApproverName)
                }
            }

            // --- SEÇÃO 2: DETALHES DA OCORRÊNCIA ---
            SectionCard(title = "Detalhes da Ocorrência") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DynamicOccurrenceFields(
                        state = state, 
                        onStateChange = onStateChange, 
                        isReadOnly = isReadOnly,
                        onPickDate = { 
                            currentPickingField = it
                            showDatePicker = true 
                        },
                        onPickTime = {
                            currentPickingField = it
                            showTimePicker = true
                        }
                    )
                }
            }

            // --- SEÇÃO 3: MOTIVO ---
            SectionCard(title = "Motivo") {
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = { onStateChange(state.copy(reason = it)) },
                    label = { Text("Descreva o motivo (Ex: Consulta, assuntos pessoais)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isReadOnly,
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = corporateGreen,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }

            // --- BOTÃO DE AÇÃO ---
            if (!isReadOnly) {
                val isApproverSelected = !state.targetApproverId.isNullOrBlank()
                
                Button(
                    onClick = { onSave(state) },
                    enabled = isApproverSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = corporateGreen,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text(
                        if (isApproverSelected) "ENVIAR COMUNICADO" else "SELECIONE UM SUPERVISOR",
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEvaluationDialog) {
        AdminEvaluationDialog(
            currentFeedback = state.adminFeedback ?: AdminFeedback(),
            onDismiss = { showEvaluationDialog = false },
            onConfirm = { feedback ->
                val updatedState = state.copy(adminFeedback = feedback)
                onStateChange(updatedState)
                showEvaluationDialog = false
                onSave(updatedState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproverSelector(
    selectedApproverName: String?,
    supervisors: List<Supervisor>,
    onSupervisorSelected: (Supervisor) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("Encaminhar para aprovação de:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedApproverName ?: "Selecione um lider ou supervisor",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                if (supervisors.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Carregando supervisores...", color = Color.Gray) },
                        onClick = { }
                    )
                }
                supervisors.forEach { supervisor ->
                    DropdownMenuItem(
                        text = { 
                            Text(supervisor.name, fontWeight = FontWeight.Bold)
                        },
                        onClick = {
                            onSupervisorSelected(supervisor)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B4332).copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSelector(
    selectedType: AbsenceType,
    onTypeSelected: (AbsenceType) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("Tipo de Ocorrência", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            OutlinedTextField(
                value = selectedType.label,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                AbsenceType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicOccurrenceFields(
    state: ComunicadoState,
    onStateChange: (ComunicadoState) -> Unit,
    isReadOnly: Boolean,
    onPickDate: (String) -> Unit = {},
    onPickTime: (String) -> Unit = {}
) {
    val fieldModifier = Modifier.fillMaxWidth()

    // Campo comum: Data
    Box(modifier = if (!isReadOnly) Modifier.clickable { onPickDate("date") } else Modifier) {
        OutlinedTextField(
            value = state.date,
            onValueChange = { },
            label = { Text("Data da Ocorrência") },
            modifier = fieldModifier,
            enabled = false,
            readOnly = true,
            leadingIcon = { Icon(CorporateIcons.Calendar, null, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Black,
                disabledLabelColor = Color.Gray,
                disabledLeadingIconColor = Color(0xFF1B4332)
            )
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Campos Específicos com Animação
    AnimatedVisibility(
        visible = state.type == AbsenceType.ATRASO,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("expectedTime") } else Modifier)) {
                OutlinedTextField(
                    value = state.expectedTime,
                    onValueChange = { },
                    label = { Text("H. Prevista") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    )
                )
            }
            Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("effectiveTime") } else Modifier)) {
                OutlinedTextField(
                    value = state.effectiveTime,
                    onValueChange = { },
                    label = { Text("H. Efetiva") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    )
                )
            }
        }
    }

    AnimatedVisibility(
        visible = state.type == AbsenceType.SAIDA_ANTECIPADA,
        enter = expandVertically() + fadeIn()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("exitTime") } else Modifier)) {
                OutlinedTextField(
                    value = state.exitTime,
                    onValueChange = { },
                    label = { Text("Hora Saída") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    )
                )
            }
            OutlinedTextField(
                value = state.missingHours,
                onValueChange = { onStateChange(state.copy(missingHours = it)) },
                label = { Text("Horas Faltas") },
                modifier = Modifier.weight(1f),
                enabled = !isReadOnly,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = state.type == AbsenceType.RETIRADA,
        enter = expandVertically() + fadeIn()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("exitTime") } else Modifier)) {
                    OutlinedTextField(
                        value = state.exitTime,
                        onValueChange = { },
                        label = { Text("H. Saída") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("returnTime") } else Modifier)) {
                    OutlinedTextField(
                        value = state.returnTime,
                        onValueChange = { },
                        label = { Text("H. Retorno") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black,
                            disabledLabelColor = Color.Gray
                        )
                    )
                }
            }
            OutlinedTextField(
                value = state.missingHours,
                onValueChange = { onStateChange(state.copy(missingHours = it)) },
                label = { Text("Total de Horas Faltosas") },
                modifier = fieldModifier,
                enabled = !isReadOnly,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = state.type == AbsenceType.FALTA,
        enter = expandVertically() + fadeIn()
    ) {
        OutlinedTextField(
            value = state.missingDays,
            onValueChange = { onStateChange(state.copy(missingDays = it)) },
            label = { Text("Dias/Horas Faltosas") },
            modifier = fieldModifier,
            enabled = !isReadOnly,
            shape = RoundedCornerShape(12.dp)
        )
    }

    AnimatedVisibility(
        visible = state.type == AbsenceType.TROCA_HORARIO,
        enter = expandVertically() + fadeIn()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("originalTime") } else Modifier)) {
                OutlinedTextField(
                    value = state.originalTime,
                    onValueChange = { },
                    label = { Text("H. Original") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    )
                )
            }
            Box(modifier = Modifier.weight(1f).then(if (!isReadOnly) Modifier.clickable { onPickTime("newTime") } else Modifier)) {
                OutlinedTextField(
                    value = state.newTime,
                    onValueChange = { },
                    label = { Text("Novo Horário") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Black,
                        disabledLabelColor = Color.Gray
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminFeedbackSection(feedback: AdminFeedback) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Avaliação do Departamento Pessoal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Autorização
                feedback.isAuthorized?.let { auth ->
                    AdminChip(
                        label = if (auth) "Autorizado" else "Não Autorizado",
                        color = if (auth) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }

                // Comprovação
                feedback.isProven?.let { proven ->
                    AdminChip(
                        label = if (proven) "Comprovante Entregue" else "Sem Comprovação",
                        color = if (proven) Color(0xFF1976D2) else Color(0xFFFFA000)
                    )
                }

                // Instruções (Múltiplas)
                feedback.instructions.forEach { instruction ->
                    val chipColor = when (instruction) {
                        "Descontar" -> Color(0xFFD32F2F)
                        "Abonar" -> Color(0xFF2E7D32)
                        "Compensar" -> Color(0xFF1976D2)
                        else -> Color.Gray
                    }
                    AdminChip(label = instruction, color = chipColor)
                }
            }
        }
    }
}

@Composable
fun AdminChip(label: String, color: Color) {
    ElevatedAssistChip(
        onClick = {},
        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        colors = AssistChipDefaults.elevatedAssistChipColors(
            labelColor = color,
            leadingIconContentColor = color
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun AdminEvaluationDialog(
    currentFeedback: AdminFeedback,
    onDismiss: () -> Unit,
    onConfirm: (AdminFeedback) -> Unit
) {
    var isAuthorized by remember { mutableStateOf(currentFeedback.isAuthorized ?: true) }
    var isProven by remember { mutableStateOf(currentFeedback.isProven ?: true) }
    var selectedInstruction by remember { mutableStateOf(currentFeedback.instructions.firstOrNull()) }

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = onDismiss,
        title = { Text("Avaliação do Departamento Pessoal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Autorização
                Column {
                    Text("Autorização", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isAuthorized, onClick = { isAuthorized = true })
                        Text("Autorizado", modifier = Modifier.clickable { isAuthorized = true })
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = !isAuthorized, onClick = { isAuthorized = false })
                        Text("Não Autorizado", modifier = Modifier.clickable { isAuthorized = false })
                    }
                }

                // Comprovação
                Column {
                    Text("Comprovação", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isProven, onClick = { isProven = true })
                        Text("Entregue", modifier = Modifier.clickable { isProven = true })
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = !isProven, onClick = { isProven = false })
                        Text("Não Entregue", modifier = Modifier.clickable { isProven = false })
                    }
                }

                // Instruções
                Column {
                    Text("Instruções ao DP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val options = listOf("Descontar", "Abonar", "Compensar")
                    options.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedInstruction == option,
                                onClick = { selectedInstruction = option }
                            )
                            Text(option, modifier = Modifier.clickable { selectedInstruction = option })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(AdminFeedback(isAuthorized, isProven, listOfNotNull(selectedInstruction)))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
            ) {
                Text("SALVAR AVALIAÇÃO")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
