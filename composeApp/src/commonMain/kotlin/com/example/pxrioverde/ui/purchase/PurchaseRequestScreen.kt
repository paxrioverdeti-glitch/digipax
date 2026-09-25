package com.example.pxrioverde.ui.purchase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.*
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.util.DateUtils
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseRequestScreen(
    user: User,
    sector: String,
    onSave: (PurchaseRequest) -> Unit,
    onBack: () -> Unit,
    isUploading: Boolean = false
) {
    val today = remember { 
        val now = Clock.System.now()
        DateUtils.formatIsoDate(now.toString()).split(" às").first() 
    }

    var state by remember(sector) { mutableStateOf(PurchaseRequest(
        requesterId = user.id,
        requesterName = user.displayName,
        department = sector,
        approverName = "Natália"
    )) }
    var estimatedValueText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val corporateGreen = Color(0xFF1B4332)
    val borderColor = Color.LightGray.copy(alpha = 0.5f)

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("SOLICITAÇÃO DE COMPRA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(CorporateIcons.Back, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = corporateGreen)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- POLÍTICA DE COMPRA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "POLÍTICA DE COMPRA", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 12.sp, 
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    val policies = listOf(
                        "Todas as solicitações devem ser formalizadas por este documento.",
                        "Solicitações Incompletas serão devolvidas para correção.",
                        "O Departamento de Compras é responsável pela cotação, negociação e escolha do fornecedor.",
                        "Nenhuma compra poderá ser realizada sem a devida aprovação."
                    )
                    policies.forEach { policy ->
                        Text("• $policy", fontSize = 11.sp, color = Color.DarkGray, lineHeight = 14.sp)
                    }
                }
            }

            // --- DADOS DO SOLICITANTE ---
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor).padding(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DADOS DO SOLICITANTE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = corporateGreen)
                    
                    FormRow(label = "DEPARTAMENTO:", value = state.department)
                    FormRow(label = "NOME SOLICITANTE:", value = state.requesterName)
                    FormRow(label = "DATA DA SOLICITAÇÃO:", value = today)
                    
                    OutlinedTextField(
                        value = state.deliveryLocation,
                        onValueChange = { state = state.copy(deliveryLocation = it) },
                        label = { Text("LOCAL P/ ENTREGA:", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = corporateGreen,
                            unfocusedContainerColor = Color(0xFFF5F7F6)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            }

            // --- TIPO DE SOLICITAÇÃO ---
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor).padding(12.dp)) {
                Column {
                    Text("TIPO DE SOLICITAÇÃO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = corporateGreen)
                    Spacer(Modifier.height(8.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PurchaseType.entries.forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(type = type) }) {
                                RadioButton(
                                    selected = state.type == type,
                                    onClick = { state = state.copy(type = type) },
                                    colors = RadioButtonDefaults.colors(selectedColor = corporateGreen)
                                )
                                Text(type.label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // --- DETALHES DO PEDIDO ---
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor).padding(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DETALHES DO PEDIDO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = corporateGreen)

                    DocumentTextField(
                        label = "NOME DO ITEM OU SERVIÇO:",
                        value = state.itemName,
                        onValueChange = { state = state.copy(itemName = it) }
                    )

                    DocumentTextField(
                        label = "QUANTIDADE:",
                        value = state.quantity,
                        onValueChange = { state = state.copy(quantity = it) }
                    )

                    DocumentTextField(
                        label = "VALOR ESTIMADO (UNIDADE OU TOTAL):",
                        value = estimatedValueText,
                        onValueChange = { 
                            val filtered = it.replace(",", ".")
                            if (filtered.isEmpty() || filtered.toDoubleOrNull() != null || filtered.endsWith(".")) {
                                estimatedValueText = it
                                state = state.copy(estimatedValue = filtered.toDoubleOrNull())
                            }
                        },
                        prefix = { Text("R$ ", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )

                    DocumentTextField(
                        label = "JUSTIFICATIVA PARA A COMPRA:",
                        value = state.justification,
                        onValueChange = { state = state.copy(justification = it) },
                        singleLine = false,
                        minLines = 2
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("EXISTE FORNECEDOR SUGERIDO?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(hasSuggestedVendor = true) }) {
                            RadioButton(selected = state.hasSuggestedVendor, onClick = { state = state.copy(hasSuggestedVendor = true) }, colors = RadioButtonDefaults.colors(selectedColor = corporateGreen))
                            Text("SIM", fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(hasSuggestedVendor = false) }) {
                            RadioButton(selected = !state.hasSuggestedVendor, onClick = { state = state.copy(hasSuggestedVendor = false) }, colors = RadioButtonDefaults.colors(selectedColor = corporateGreen))
                            Text("NÃO", fontSize = 11.sp)
                        }
                    }

                    AnimatedVisibility(visible = state.hasSuggestedVendor) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DocumentTextField(
                                label = "NOME FORNECEDOR:",
                                value = state.vendorName ?: "",
                                onValueChange = { state = state.copy(vendorName = it) }
                            )
                            DocumentTextField(
                                label = "CONTATO:",
                                value = state.vendorContact ?: "",
                                onValueChange = { state = state.copy(vendorContact = it) }
                            )
                        }
                    }

                    DocumentTextField(
                        label = "DESCRIÇÃO TÉCNICA (ESPECIFICAÇÃO, MARCA, MODELO, COR, ETC):",
                        value = state.technicalDescription,
                        onValueChange = { state = state.copy(technicalDescription = it) },
                        singleLine = false,
                        minLines = 3
                    )
                }
            }

            // --- FORMA DE PAGAMENTO E NOTA FISCAL ---
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor).padding(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("FORMA DE PAGAMENTO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = corporateGreen)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethod.entries.forEach { method ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(paymentMethod = method) }) {
                                    RadioButton(
                                        selected = state.paymentMethod == method,
                                        onClick = { state = state.copy(paymentMethod = method) },
                                        colors = RadioButtonDefaults.colors(selectedColor = corporateGreen)
                                    )
                                    Text(method.label.uppercase(), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = borderColor)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("POSSUI NOTA FISCAL?", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(hasInvoice = true) }) {
                            RadioButton(selected = state.hasInvoice, onClick = { state = state.copy(hasInvoice = true) }, colors = RadioButtonDefaults.colors(selectedColor = corporateGreen))
                            Text("SIM", fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(hasInvoice = false) }) {
                            RadioButton(selected = !state.hasInvoice, onClick = { state = state.copy(hasInvoice = false) }, colors = RadioButtonDefaults.colors(selectedColor = corporateGreen))
                            Text("NÃO", fontSize = 11.sp)
                        }
                    }
                }
            }

            // --- TIPO DE URGÊNCIA ---
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, borderColor).padding(12.dp)) {
                Column {
                    Text("TIPO DE URGÊNCIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = corporateGreen)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UrgencyLevel.entries.forEach { level ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { state = state.copy(urgency = level) }) {
                                RadioButton(
                                    selected = state.urgency == level,
                                    onClick = { state = state.copy(urgency = level) },
                                    colors = RadioButtonDefaults.colors(selectedColor = corporateGreen)
                                )
                                Text(level.label.uppercase(), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // APROVAÇÃO (Informativo de quem aprova)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5F7F6),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(CorporateIcons.Check, null, tint = corporateGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Solicitação será enviada para aprovação de: ",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Natália Arantes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = corporateGreen
                    )
                }
            }

            // BOTÃO ENVIAR
            Button(
                onClick = { onSave(state) },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = corporateGreen),
                enabled = !isUploading && state.itemName.isNotBlank() && state.justification.isNotBlank()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("PROTOCOLO DE ENVIO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(140.dp))
        Text(value, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = Color.Black)
    }
}

@Composable
fun DocumentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1,
    prefix: @Composable (() -> Unit)? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            prefix = prefix,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF1B4332),
                unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}
