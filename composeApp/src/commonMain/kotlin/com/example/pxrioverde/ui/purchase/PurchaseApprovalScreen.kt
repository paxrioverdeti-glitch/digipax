package com.example.pxrioverde.ui.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.PurchaseRequest
import com.example.pxrioverde.model.PurchaseStatus
import com.example.pxrioverde.model.UrgencyLevel
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.viewmodel.PurchaseUiState
import com.example.pxrioverde.viewmodel.PurchaseViewModel
import com.example.pxrioverde.ui.hr.ReadOnlyField
import com.example.pxrioverde.ui.hr.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseApprovalScreen(
    viewModel: PurchaseViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedRequest by remember { mutableStateOf<PurchaseRequest?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllRequests()
    }

    if (selectedRequest == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Aprovações de Compra", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(CorporateIcons.Back, "Voltar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B4332))
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.surface)) {
                when (val state = uiState) {
                    is PurchaseUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1B4332))
                    }
                    is PurchaseUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.requests, key = { it.id ?: it.itemName }) { request ->
                                PurchaseRequestItemCard(request) {
                                    selectedRequest = request
                                }
                            }
                        }
                    }
                    is PurchaseUiState.Empty -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(CorporateIcons.Alert, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(16.dp))
                            Text("Nenhuma solicitação pendente", color = Color.Gray)
                        }
                    }
                    is PurchaseUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.loadAllRequests() }, modifier = Modifier.padding(16.dp)) {
                                Text("Tentar Novamente")
                            }
                        }
                    }
                }
            }
        }
    } else {
        PurchaseEvaluationDialog(
            request = selectedRequest!!,
            isUploading = viewModel.isUploading.collectAsState().value,
            onDismiss = { selectedRequest = null },
            onConfirm = { status, comment, approvedValue ->
                viewModel.updateRequestStatus(selectedRequest!!.id!!, status, comment, approvedValue) {
                    selectedRequest = null
                    viewModel.loadAllRequests()
                }
            }
        )
    }
}

@Composable
fun PurchaseRequestItemCard(request: PurchaseRequest, onClick: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(request.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("Solicitante: ${request.requesterName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Urgência: ${request.urgency.label}", color = if (request.urgency == UrgencyLevel.ALTA) Color.Red else Color.Gray, fontSize = 12.sp)
            }
            Icon(CorporateIcons.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEvaluationDialog(
    request: PurchaseRequest,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PurchaseStatus, String?, Double?) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var approvedValue by remember { mutableStateOf(request.estimatedValue?.toString() ?: "") }
    var selectedStatus by remember { mutableStateOf(PurchaseStatus.APROVADO) }

    AlertDialog(
        onDismissRequest = if (isUploading) ({}) else onDismiss,
        title = { Text("Avaliar Solicitação", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "Detalhes do Item") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReadOnlyField(label = "Item", value = request.itemName)
                        ReadOnlyField(label = "Qtd", value = request.quantity)
                        ReadOnlyField(label = "Valor Estimado", value = request.estimatedValue?.let { "R$ $it" } ?: "N/A")
                        ReadOnlyField(label = "Justificativa", value = request.justification)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Decisão", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    
                    DecisionOption(
                        label = "Aprovar",
                        selected = selectedStatus == PurchaseStatus.APROVADO,
                        onClick = { if (!isUploading) selectedStatus = PurchaseStatus.APROVADO }
                    )
                    
                    DecisionOption(
                        label = "Programar",
                        selected = selectedStatus == PurchaseStatus.PROGRAMADO,
                        onClick = { if (!isUploading) selectedStatus = PurchaseStatus.PROGRAMADO }
                    )
                    
                    DecisionOption(
                        label = "Rejeitar",
                        selected = selectedStatus == PurchaseStatus.REJEITADO,
                        onClick = { if (!isUploading) selectedStatus = PurchaseStatus.REJEITADO }
                    )
                }

                if (selectedStatus == PurchaseStatus.APROVADO) {
                    OutlinedTextField(
                        value = approvedValue,
                        onValueChange = { 
                            val filtered = it.replace(",", ".")
                            if (filtered.isEmpty() || filtered.toDoubleOrNull() != null) approvedValue = filtered 
                        },
                        label = { Text("Valor Aprovado") },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentário / Observação (Opcional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    enabled = !isUploading,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(selectedStatus, comment.ifBlank { null }, approvedValue.toDoubleOrNull()) 
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("SALVAR AVALIAÇÃO")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) { Text("CANCELAR") }
        }
    )
}

@Composable
fun DecisionOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
