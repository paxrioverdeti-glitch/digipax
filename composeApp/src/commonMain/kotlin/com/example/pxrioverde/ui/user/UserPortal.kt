package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.StrategicEmptyState
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.util.DateUtils
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.example.pxrioverde.util.AdaptiveUtils
import com.example.pxrioverde.util.WindowSizeClass
import androidx.compose.foundation.BorderStroke
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPortal(
    user: com.example.pxrioverde.model.User,
    viewModel: TicketViewModel,
    onSelectTicket: (Ticket) -> Unit = {},
    onNavigateToTicketCreate: (String?) -> Unit = {},
    isDesktop: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    val tickets by viewModel.tickets.collectAsState()
    val selectedTicket by viewModel.selectedTicket.collectAsState()
    
    val filteredTickets = remember(tickets, searchQuery) {
        if (searchQuery.isBlank()) tickets
        else tickets.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true) 
        }
    }
    
    LaunchedEffect(user.id) {
        viewModel.loadTickets(user.id)
    }

    if (isDesktop) {
        // LAYOUT DESKTOP: Split Screen (Lista à esquerda, Detalhes à direita)
        Row(modifier = Modifier.fillMaxSize()) {
            // Lado Esquerdo: Lista Compacta
            Column(modifier = Modifier.width(380.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface)) {
                // Header da Lista com Busca e Novo Chamado
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Meus Chamados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { onNavigateToTicketCreate(null) }) {
                            Text("+ NOVO", fontWeight = FontWeight.Black)
                        }
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(CorporateIcons.Support, null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                if (filteredTickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum chamado encontrado.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTickets.sortedByDescending { it.createdAt }) { ticket ->
                            UserTicketItemSmall(
                                ticket = ticket,
                                isSelected = selectedTicket?.id == ticket.id,
                                onClick = { 
                                    viewModel.selectTicket(ticket)
                                    onSelectTicket(ticket)
                                }
                            )
                        }
                    }
                }
            }

            VerticalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Lado Direito: Detalhes
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.background)) {
                if (selectedTicket != null) {
                    com.example.pxrioverde.ui.ticket.TicketDetailScreen(
                        viewModel = viewModel,
                        userName = user.displayName,
                        onBack = { viewModel.selectTicket(null as Ticket?) },
                        isDesktop = true
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(CorporateIcons.Receipt, null, modifier = Modifier.size(64.dp), tint = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Selecione um chamado da lista para ver a conversa", color = Color.Gray)
                        }
                    }
                }
            }
        }
    } else {
        // LAYOUT MOBILE (Atual)
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
                    TopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        title = { Text("Meus Chamados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
                        actions = {
                            if (tickets.isNotEmpty()) {
                                Button(
                                    onClick = { onNavigateToTicketCreate(null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) { Text("+ Novo", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White, actionIconContentColor = Color.White)
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                if (tickets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar chamados...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(CorporateIcons.Support, null, modifier = Modifier.size(18.dp)) },
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (filteredTickets.isEmpty()) {
                    StrategicEmptyState(title = "Tudo em ordem!", description = "Você não tem nenhum chamado no momento.", icon = CorporateIcons.Support, buttonText = "ABRIR CHAMADO", onButtonClick = { onNavigateToTicketCreate(null) })
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(filteredTickets.sortedByDescending { it.status == TicketStatus.NA_FILA }) { ticket ->
                            TicketItem(ticket, onClick = {
                                viewModel.selectTicket(ticket)
                                onSelectTicket(ticket)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserTicketItemSmall(ticket: Ticket, isSelected: Boolean, onClick: () -> Unit) {
    val statusColor = when (ticket.status) {
        TicketStatus.NA_FILA -> MaterialTheme.colorScheme.error
        TicketStatus.EM_ANALISE -> MaterialTheme.colorScheme.primary
        TicketStatus.RESOLVIDO -> Color(0xFF2E7D32)
        else -> Color.Gray
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = statusColor) {}
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ticket.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(DateUtils.formatIsoDate(ticket.createdAt), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(CorporateIcons.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket, onClick: () -> Unit) {
    val statusColor = when (ticket.status) {
        TicketStatus.NA_FILA -> MaterialTheme.colorScheme.error
        TicketStatus.EM_ANALISE -> MaterialTheme.colorScheme.primary
        TicketStatus.RESOLVIDO -> Color(0xFF2E7D32)
        TicketStatus.CANCELADO -> Color.Gray
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(ticket.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        val statusLabel = when(ticket.status) {
                            TicketStatus.NA_FILA -> "NA FILA"
                            TicketStatus.EM_ANALISE -> "EM ANÁLISE"
                            TicketStatus.AGUARDANDO_PECA -> "AGUARDANDO PEÇA"
                            TicketStatus.EM_ATENDIMENTO -> "EM ATENDIMENTO"
                            TicketStatus.RESOLVIDO -> "RESOLVIDO"
                            TicketStatus.CANCELADO -> "CANCELADO"
                        }
                        Text(statusLabel, color = statusColor, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ticket.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(CorporateIcons.Support, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Protocolo: #${ticket.id?.takeLast(5) ?: "---"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                    Text(text = DateUtils.formatIsoDate(ticket.createdAt), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(imageVector = CorporateIcons.ChevronRight, contentDescription = null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
    }
}
