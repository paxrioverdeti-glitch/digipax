package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.model.User
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.StrategicEmptyState
import com.example.pxrioverde.viewmodel.TripViewModel
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclePortal(
    user: User,
    viewModel: TripViewModel,
    onNavigateToCheckOut: (Booking) -> Unit,
    onNavigateToReserve: () -> Unit,
    isDesktop: Boolean = false
) {
    val bookings by viewModel.bookings.collectAsState()
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(user.id) {
        viewModel.loadBookings(user.id)
    }

    if (bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancelar Agendamento") },
            text = { Text("Deseja realmente cancelar seu agendamento para ${bookingToCancel?.destination}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookingToCancel?.id?.let { id ->
                            viewModel.cancelBooking(id, user.id) {
                                bookingToCancel = null
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("SIM, CANCELAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("VOLTAR", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F7F6),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isDesktop) {
                Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
                    TopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        title = { 
                            Text(
                                "Meus Agendamentos", 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ) 
                        },
                        actions = {
                            if (bookings.isNotEmpty()) {
                                Button(
                                    onClick = onNavigateToReserve,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("+ Novo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (isDesktop) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Meus Agendamentos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B4332)
                    )
                    Button(
                        onClick = onNavigateToReserve,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+ NOVO AGENDAMENTO", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (bookings.isEmpty()) {
                StrategicEmptyState(
                    title = "Nada agendado por aqui!",
                    description = "Que tal planejar sua próxima rota? 🚗💨",
                    icon = CorporateIcons.MapPin,
                    buttonText = "AGENDAR AGORA",
                    onButtonClick = onNavigateToReserve
                )
            } else {
                if (isDesktop) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 350.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        gridItems(bookings) { booking ->
                            when (booking.status) {
                                "finalizado" -> FinishedBookingItemCard(booking = booking)
                                "cancelado" -> CanceledBookingItemCard(booking = booking)
                                else -> BookingItemCard(
                                    booking = booking, 
                                    onCheckOut = { onNavigateToCheckOut(booking) },
                                    onCancel = { bookingToCancel = booking }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(bookings) { booking ->
                            when (booking.status) {
                                "finalizado" -> FinishedBookingItemCard(booking = booking)
                                "cancelado" -> CanceledBookingItemCard(booking = booking)
                                else -> BookingItemCard(
                                    booking = booking, 
                                    onCheckOut = { onNavigateToCheckOut(booking) },
                                    onCancel = { bookingToCancel = booking }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(booking: Booking, onCheckOut: () -> Unit, onCancel: () -> Unit) {
    val isInProgress = booking.status.equals("EM_ANDAMENTO", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCheckOut() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val statusColor = when {
                        booking.isEmergency -> Color(0xFFE07A5F)
                        booking.status == "em_andamento" -> Color(0xFFE07A5F)
                        else -> Color(0xFF2D6A4F)
                    }
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val labelText = when {
                            booking.isEmergency -> "EMERGÊNCIA"
                            booking.status == "em_andamento" -> "EM ANDAMENTO"
                            else -> "agendado"
                        }
                        Text(
                            text = labelText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = booking.destination ?: "Destino não informado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332),
                        maxLines = 1
                    )
                    Text(
                        text = booking.department ?: "Sem Setor",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = booking.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp), 
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = CorporateIcons.Clock,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val timeRange = when {
                    booking.isEmergency -> "${booking.scheduledStartTime} (Início)"
                    booking.status == "em_andamento" && booking.startTime != null ->
                        "${booking.startTime} (Iniciado)"
                    booking.scheduledEndTime != null -> {
                        val start = booking.scheduledStartTime
                        val end = booking.scheduledEndTime
                        if (booking.returnDate != null && booking.returnDate != booking.date) {
                            val returnDay = try {
                                val date = LocalDate.parse(booking.returnDate)
                                "${date.dayOfMonth}/${date.monthNumber}"
                            } catch(_: Exception) { "" }
                            "$start às $end ($returnDay)"
                        } else {
                            "$start às $end"
                        }
                    }
                    else -> "${booking.scheduledStartTime} (Saída)"
                }
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!isInProgress) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Text("CANCELAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onCheckOut,
                    modifier = Modifier.weight(if (isInProgress) 1f else 1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInProgress) Color(0xFFE07A5F) else Color(0xFF2D6A4F)
                    )
                ) {
                    Icon(
                        imageVector = if (isInProgress) CorporateIcons.Home else CorporateIcons.Camera, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isInProgress) "FINALIZAR VIAGEM" else "CHECK-IN",
                        fontWeight = FontWeight.Bold, 
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FinishedBookingItemCard(booking: Booking) {
    InactiveBookingItemCard(
        booking = booking,
        label = "VIAGEM CONCLUÍDA",
        showStats = true
    )
}

@Composable
fun CanceledBookingItemCard(booking: Booking) {
    InactiveBookingItemCard(
        booking = booking,
        label = "AGENDAMENTO CANCELADO",
        showStats = false
    )
}

@Composable
fun InactiveBookingItemCard(
    booking: Booking,
    label: String,
    showStats: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFFF5F7F6),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = booking.destination ?: "Sem Destino",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332),
                        maxLines = 1
                    )
                }
                
                Text(
                    text = booking.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp), 
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Horários
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CorporateIcons.Clock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (showStats) "HORÁRIO REAL" else "HORÁRIO AGENDADO", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        if (showStats) "${booking.startTime ?: "--:--"} às ${booking.endTime ?: "--:--"}"
                        else "${booking.scheduledStartTime} às ${booking.scheduledEndTime ?: "--:--"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                // KM Rodados (apenas se showStats for true)
                if (showStats) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "DISTÂNCIA", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = CorporateIcons.Monitor,
                                contentDescription = null,
                                tint = Color(0xFF2D6A4F),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        val diff = (booking.finalKm ?: 0.0) - (booking.initialKm ?: 0.0)
                        val displayKm = maxOf(0.0, diff)
                        Text(
                            "${displayKm.toInt()} KM rodados",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D6A4F)
                        )
                    }
                }
            }
        }
    }
}
