package com.example.pxrioverde.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pxrioverde.model.User
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.ui.components.LottieAnimation
import com.example.pxrioverde.util.LocalStorage
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import androidx.compose.foundation.BorderStroke

import androidx.compose.ui.platform.LocalUriHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCreateScreen(
    user: User,
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    onPickFile: () -> Unit,
    onTakeNoFoto: () -> Unit,
    selectedFileName: String,
    preselectedSector: String? = null,
    defaultSector: String = "",
    defaultComputer: String = ""
) {
    val uriHandler = LocalUriHandler.current
    
    // Carregar dados persistidos ou usar defaults
    var userName by remember { mutableStateOf(LocalStorage.getString("user_login", user.displayName)) }
    var computer by remember(defaultComputer) { mutableStateOf(if (defaultComputer.isNotBlank()) defaultComputer else LocalStorage.getString("machine_name", "")) }
    var sector by remember(defaultSector) { mutableStateOf(if (defaultSector.isNotBlank()) defaultSector else LocalStorage.getString("user_sector", "")) }
    
    var description by remember { mutableStateOf("") }
    var supportSector by remember { mutableStateOf(preselectedSector ?: "") }
    var expanded by remember { mutableStateOf(false) } // Controle do dropdown
    var isSuccess by remember { mutableStateOf(false) }

    val gobahOptions = listOf(
        "Impressora", "Pasta Corporativa", "Odoo", "E-mail Corporativo",
        "Acesso ao Computador", "Atualizar/ Instalar programas",
        "Resetar senha PC", "Computador lento", "Programa do Pc não abre",
        "Criar/Excluir usuário", "Pacote office/WPS", "Outro"
    )

    val isGobah = remember(supportSector) { gobahOptions.contains(supportSector) }

    // OTIMIZAÇÃO: Estado derivado para evitar recomposições inúteis
    val isFormValid by remember {
        derivedStateOf {
            description.isNotBlank() &&
            sector.isNotBlank() &&
            computer.isNotBlank() &&
            supportSector.isNotBlank()
        }
    }

    val tiInternoOptions = listOf(
        "Internet", "Dmpax", "Aplicativos internos/Externos", "Teams",
        "Equipamentos com Defeitos", "Celulares", "Linhas Telefonicas",
        "Sala de Servidores", "Sala de Treinamento", "AFA atendimento", "Outro (T.I)"
    )

    val isUploading by viewModel.isUploading.collectAsState()

    if (isUploading || isSuccess) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isSuccess) {
                TicketSuccessScreen(
                    onDismiss = onBack,
                    isUrgent = preselectedSector != null,
                    isGobah = isGobah,
                    onWhatsAppRedirect = {
                        val message = """
                            PAX RIO VERDE - NOVO CHAMADO
                            Usuário: $userName
                            Máquina: $computer
                            Setor: $sector
                            Descrição do Chamado: $description
                        """.trimIndent()
                        
                        val encodedMessage = message.replace("\n", "%0A").replace(" ", "%20")
                        val whatsappUrl = "https://wa.me/556435130114?text=$encodedMessage"
                        try {
                            uriHandler.openUri(whatsappUrl)
                        } catch (e: Exception) {
                            println("Erro ao abrir WhatsApp: ${e.message}")
                        }
                    }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        resName = "animacao1", // Ou outra animação de loading se preferir
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Enviando chamado...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332)
                    )
                }
            }
        }
        return
    }

    // Estilo para label obrigatório com asterisco vermelho
    fun mandatoryLabel(text: String): AnnotatedString = buildAnnotatedString {
        append(text)
        withStyle(style = SpanStyle(color = Color.Red)) {
            append(" *")
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color(0xFFF5F7F6),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho Customizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(CorporateIcons.Back, contentDescription = "Voltar", tint = Color.White)
                    }
                    
                    Text(
                        text = "Novo Chamado",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 1. Informações do Solicitante
                Text(
                    text = "Informações do Solicitante",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Usuário Solicitante (Desativado / Lock)
                OutlinedTextField(
                    value = userName,
                    onValueChange = {},
                    label = { Text("Usuário Solicitante") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    enabled = false,
                    leadingIcon = {
                        Icon(CorporateIcons.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.2f),
                        disabledBorderColor = Color.LightGray,
                        disabledTextColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = sector,
                    onValueChange = { sector = it },
                    label = { Text(mandatoryLabel("Setor / Departamento")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = computer,
                    onValueChange = { computer = it },
                    label = { Text("Nome do Computador / Máquina") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(24.dp))

                // 2. Detalhes do Chamado
                Text(
                    text = "Detalhes do Chamado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (preselectedSector != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = CorporateIcons.Alert,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Este é um atendimento rápido para casos de URGÊNCIA.",
                                color = Color(0xFFE65100),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Dropdown para Setor de Suporte
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ... (resto do campo de texto igual)
                    OutlinedTextField(
                        value = supportSector,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(mandatoryLabel("Setor de Suporte (Destino)")) },
                        placeholder = { Text("Selecione uma opção") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        // Seção Gobah!
                        Text(
                            text = "Gobah!",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                        gobahOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = selectionOption,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                onClick = {
                                    supportSector = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Seção T.I interno
                        Text(
                            text = "T.I interno",
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                        tiInternoOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = selectionOption,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                onClick = {
                                    supportSector = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                if (supportSector == "Pasta Corporativa") {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF9C4), // Amarelo claro
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFBC02D))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(CorporateIcons.Lock, null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Atenção: Solicitações de acesso a pastas corporativas devem ser realizadas apenas por SUPERVISORES.",
                                color = Color(0xFFF57F17),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 500) description = it },
                    label = { Text(mandatoryLabel("Descrição Detalhada")) },
                    placeholder = { Text("Digite o problema...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = "${description.length} / 500",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                )

                Spacer(Modifier.height(24.dp))

                // 3. Área de Anexos
                if (!isGobah) {
                    Text(
                        text = "Anexar Arquivos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onPickFile,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4A148C), // Roxo-escuro
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(CorporateIcons.Attachment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Arquivo", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onTakeNoFoto,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4A148C), // Roxo-escuro
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(CorporateIcons.Camera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Câmera", fontSize = 12.sp)
                                }
                            }

                            if (selectedFileName.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(CorporateIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(selectedFileName, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))

                // Botão de Ação (Acompanha a aba - scrollable)
                Button(
                    onClick = {
                        // Gravar informações localmente
                        LocalStorage.putString("user_login", userName)
                        LocalStorage.putString("machine_name", computer)
                        LocalStorage.putString("user_sector", sector)

                        viewModel.submitTicket(
                            userId = user.id,
                            userName = userName,
                            title = supportSector, 
                            computer = computer,
                            sector = supportSector, // SALVAR O TIPO DO CHAMADO NO CAMPO SECTOR PARA AS MÉTRICAS
                            description = description,
                            onSuccess = { isSuccess = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B4332), // Verde-escuro sólido
                        contentColor = Color.White
                    ),
                    enabled = !isUploading && isFormValid
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("ABRIR CHAMADO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.navigationBarsPadding()) // Espaço para a barra de navegação do sistema
            }
        }
    }
}
