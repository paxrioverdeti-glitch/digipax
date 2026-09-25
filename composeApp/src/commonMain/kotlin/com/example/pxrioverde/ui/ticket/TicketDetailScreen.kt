package com.example.pxrioverde.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketMessage
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.ui.ticket.components.TicketRatingDisplay
import com.example.pxrioverde.ui.ticket.components.TicketRatingInput
import com.example.pxrioverde.util.DateUtils
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: TicketViewModel,
    userName: String,
    onBack: () -> Unit,
    isDesktop: Boolean = false
) {
    val ticket by viewModel.selectedTicket.collectAsState()
    val messages by viewModel.messages.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll automático para a última mensagem ou seção de avaliação
    LaunchedEffect(messages.size, ticket?.status) {
        val totalItems = messages.size + 2 // InfoCard + Messages + (Rating or BottomPadding)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7F6),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isDesktop) {
                Surface(color = Color.White, shadowElevation = 2.dp) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    ticket?.title ?: "Carregando...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Protocolo #${ticket?.id?.takeLast(5) ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(CorporateIcons.Back, contentDescription = "Voltar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Lista de Mensagens
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info do Chamado (Cabeçalho da conversa)
                item {
                    TicketInfoCard(ticket)
                }

                items(messages) { message ->
                    ChatBubble(message, isMe = message.authorName == userName)
                }

                // Seção de Avaliação (Apenas se Resolvido)
                if (ticket?.status == TicketStatus.RESOLVIDO) {
                    item {
                        if (ticket?.rating != null && ticket?.rating!! > 0) {
                            TicketRatingDisplay(
                                rating = ticket?.rating!!,
                                comment = ticket?.ratingComment
                            )
                        } else {
                            TicketRatingInput(
                                onRatingSubmit = { rating, comment ->
                                    viewModel.rateTicket(rating, comment)
                                }
                            )
                        }
                    }
                }
            }

            // Barra de Resposta (Esconder se resolvido ou cancelado)
            if (ticket?.status != TicketStatus.RESOLVIDO && ticket?.status != TicketStatus.CANCELADO) {
                Surface(
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Escreva uma mensagem...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7F6),
                                unfocusedContainerColor = Color(0xFFF5F7F6),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        FloatingActionButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(commentText, userName, false)
                                    commentText = ""
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Icon(CorporateIcons.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                // Feedback visual de que o chamado está encerrado
                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (ticket?.status == TicketStatus.RESOLVIDO) 
                            "Este chamado foi resolvido e está encerrado." 
                            else "Este chamado foi cancelado.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TicketInfoCard(ticket: Ticket?) {
    if (ticket == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(CorporateIcons.Receipt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Descrição Original", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(ticket.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            
            if (!ticket.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ticket.imageUrl,
                    contentDescription = "Anexo",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Aberto em ${DateUtils.formatIsoDate(ticket.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChatBubble(message: TicketMessage, isMe: Boolean) {
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else Color.White
    val contentColor = if (isMe) Color.White else Color.DarkGray
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isMe) {
            Text(
                message.authorName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor
                )
            }
        }
        
        // Usando o formatIsoDate simplificado se o formatTime não estiver disponível
        Text(
            DateUtils.formatIsoDate(message.timestamp.toString()).split(" às ").lastOrNull() ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
