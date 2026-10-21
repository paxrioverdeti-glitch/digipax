package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalFocusManager
import com.example.pxrioverde.ui.components.LottieAnimation
import com.example.pxrioverde.ui.components.rememberLottieComposition
import com.example.pxrioverde.ui.components.LottieConstants
import com.example.pxrioverde.model.User
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.model.Car
import com.example.pxrioverde.model.Booking
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

import com.example.pxrioverde.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CarBookingScreen(
    user: User,
    viewModel: TripViewModel,
    onBack: () -> Unit,
    onTakePhoto: (Int) -> Unit = {},
    defaultDepartment: String = ""
) {
    // PRÉ-CARREGAMENTO DA ANIMAÇÃO:
    val loadingComposition = rememberLottieComposition("carregamento")

    var selectedTab by remember { mutableStateOf(0) } // 0 = Agendar, 1 = Veículos Agendados
    var isEmergency by remember { mutableStateOf(false) }
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var selectedDepartment by remember(defaultDepartment) { mutableStateOf(defaultDepartment) }
    var destination by remember { mutableStateOf("") }
    var initialKm by remember { mutableStateOf("") }
    
    val photoLabels = listOf("Frente", "Traseira", "Lateral Esq.", "Lateral Dir.")

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val cars by viewModel.availableCars.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val isUploading by viewModel.isBookingSubmitting.collectAsState()

    // Controla se devemos mostrar a overlay de progresso/sucesso
    var showOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAvailableCars()
        viewModel.loadAllBookings()
        viewModel.resetSuccessState()
    }

    // EFEITO PARA PROCESSAR O AGENDAMENTO SEM CONGELAR A UI
    LaunchedEffect(showOverlay) {
        // O carregamento inicial dos agendamentos pode estar ativo quando o usuário confirma.
        // O cadastro não deve esperar esse estado, pois a mudança de isUploading não reinicia
        // este efeito e fazia o primeiro toque apenas exibir o overlay.
        if (showOverlay && !isSuccess && errorMessage == null) {
            try {
                // Cálculo de tempo usando a nova Clock API do kotlin.time para evitar conflitos
                val nowInstant = Clock.System.now()
                val dtInstant = Instant.fromEpochMilliseconds(nowInstant.toEpochMilliseconds())
                val now = dtInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                
                // Formatação auxiliar local para o booking
                fun formatTimeLocal(time: LocalTime?): String {
                    if (time == null) return ""
                    val hour = time.hour.toString().padStart(2, '0')
                    val minute = time.minute.toString().padStart(2, '0')
                    return "$hour:$minute"
                }

                val booking = Booking(
                    carId = selectedCar?.id ?: "",
                    userId = user.id,
                    userName = user.displayName,
                    date = if (isEmergency) now.date.toString() else startDate.toString(),
                    scheduledStartTime = if (isEmergency) now.time.toString().substring(0, 5) else formatTimeLocal(startTime),
                    scheduledEndTime = if (isEmergency) null else formatTimeLocal(endTime),
                    destination = destination.trim(),
                    department = selectedDepartment,
                    isEmergency = isEmergency,
                    returnDate = if (isEmergency) null else endDate.toString(),
                    returnTime = if (isEmergency) null else formatTimeLocal(endTime),
                    initialKm = if (isEmergency) initialKm.toDoubleOrNull() else null
                )
                
                viewModel.createBooking(booking) { success, error ->
                    if (success) {
                        // A tela de sucesso é exibida imediatamente; a disponibilidade
                        // será atualizada quando o fluxo de veículos for reaberto.
                    } else {
                        showOverlay = false
                        errorMessage = error
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao processar data: ${e.message}"
                showOverlay = false
            }
        }
    }

    // Processamento da disponibilidade baseada em dados reais (filtrando finalizados)
    val availability = remember(allBookings) {
        val activeBookings = allBookings.filter { it.status != "finalizado" && it.status != "cancelado" }
        
        val bookingsInfo = activeBookings.map { b ->
            BookingInfo(
                userName = b.userName,
                date = try { LocalDate.parse(b.date) } catch(e: Exception) { LocalDate(2000,1,1) },
                startTime = b.scheduledStartTime,
                endTime = b.scheduledEndTime ?: ""
            )
        }
        
        val partially = bookingsInfo.map { it.date }.distinct()
        
        VehicleBookingAvailability(
            partiallyBookedDates = partially,
            fullyBookedDates = emptyList(),
            bookings = bookingsInfo
        )
    }

    var vehicleExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }

    val departments = listOf("T.I", "Financeiro", "Comercial", "Vendas", "DP", "Compras","Recepção","Bancário","Cobrança","Manutenção","Recuperação","Convênios","Marketing","Telemarketing","Med Saúde")

    val capturedPhotos by viewModel.capturedPhotos.collectAsState()

    val isFormComplete = if (isEmergency) {
        selectedCar != null && destination.isNotBlank() && selectedDepartment.isNotBlank() && initialKm.isNotBlank() &&
        capturedPhotos.all { it != null }
    } else {
        val isTimeValid = if (startDate != null && startTime != null && endDate != null && endTime != null) {
            if (startDate == endDate) {
                val startVal = startTime!!.hour * 60 + startTime!!.minute
                val endVal = endTime!!.hour * 60 + endTime!!.minute
                endVal > startVal
            } else {
                endDate!! > startDate!!
            }
        } else false

        selectedCar != null && destination.isNotBlank() && selectedDepartment.isNotBlank() && isTimeValid
    }

    // Funções auxiliares para formatar na UI
    fun formatDate(date: LocalDate?): String {
        if (date == null) return ""
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val year = date.year
        return "$day/$month/$year"
    }

    fun formatTime(time: LocalTime?): String {
        if (time == null) return ""
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    // Modais
    if (showStartDatePicker) {
        CustomVehicleDatePicker(
            availability = availability,
            onDateSelected = { millis ->
                millis?.let {
                    startDate = Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.UTC).date
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        CustomVehicleDatePicker(
            availability = availability,
            onDateSelected = { millis ->
                millis?.let {
                    endDate = Instant.fromEpochMilliseconds(it)
                        .toLocalDateTime(TimeZone.UTC).date
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showStartTimePicker) {
        CustomVehicleTimePicker(
            onTimeSelected = { h, m ->
                startTime = LocalTime(h, m)
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        CustomVehicleTimePicker(
            onTimeSelected = { h, m ->
                endTime = LocalTime(h, m)
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Histórico de Reservas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (availability.bookings.isEmpty()) {
                    Text("Nenhuma reserva encontrada.", color = Color.Gray)
                } else {
                    val sortedBookings = availability.bookings.sortedByDescending { it.date }
                    
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        sortedBookings.forEach { booking ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = Color(0xFFF5F7F6),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(booking.userName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${booking.date.dayOfMonth}/${booking.date.monthNumber}/${booking.date.year}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Text("${booking.startTime} às ${booking.endTime}", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7F6),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (selectedTab == 0) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null
                                    showOverlay = true
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                enabled = isFormComplete,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFormComplete) Color(0xFF2D6A4F) else Color.LightGray,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                                    disabledContentColor = Color.Gray
                                )
                            ) {
                                Text(
                                    if (isEmergency) "INICIAR VIAGEM" else "CONFIRMAR AGENDAMENTO",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header com Navegação por Abas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1B4332),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(top = 56.dp, bottom = 32.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(CorporateIcons.Back, contentDescription = "Voltar", tint = Color.White)
                                }
                                Text(
                                    if (selectedTab == 0) "Novo Agendamento" else "Veículos Agendados",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                            }
                        }

                        // Seletor de Abas Inteligente
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 0 },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        CorporateIcons.Car,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Color(0xFF1B4332) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Agendar",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 0) Color(0xFF1B4332) else Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 1 },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        CorporateIcons.Calendar,
                                        contentDescription = null,
                                        tint = if (selectedTab == 1) Color(0xFF1B4332) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Agendados",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Color(0xFF1B4332) else Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .offset(y = (-24).dp) 
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                    // Alerta de Erro
                    errorMessage?.let { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(CorporateIcons.Alert, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(msg, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Agendamento Emergencial
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFEBEE), 
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isEmergency,
                                    onCheckedChange = { isEmergency = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F))
                                )
                                Icon(
                                    imageVector = CorporateIcons.Alert,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Agendamento Emergencial",
                                    color = Color(0xFFD32F2F),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (isEmergency) {
                            TextButton(
                                onClick = { showHistorySheet = true },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1B4332))
                            ) {
                                Icon(CorporateIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ver Disponibilidade", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Card de formulário
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Veículo
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Veículo", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = vehicleExpanded,
                                        onExpandedChange = { vehicleExpanded = !vehicleExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedCar?.let { "${it.name} - ${it.licensePlate}" } ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            maxLines = 1,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            placeholder = { 
                                                Text(
                                                    "Selecionar", 
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                ) 
                                            },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            leadingIcon = { Icon(CorporateIcons.Car, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1B4332)) },
                                            trailingIcon = { 
                                                Box(modifier = Modifier.padding(end = 4.dp)) {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded)
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color(0xFF1B4332),
                                                unfocusedLabelColor = Color.Gray,
                                                focusedLabelColor = Color(0xFF1B4332)
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = vehicleExpanded,
                                            onDismissRequest = { vehicleExpanded = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            cars.forEach { car ->
                                                DropdownMenuItem(
                                                    text = { 
                                                        Text(
                                                            "${car.name} - ${car.licensePlate}",
                                                            color = Color.Black,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        ) 
                                                    },
                                                    onClick = {
                                                        selectedCar = car
                                                        vehicleExpanded = false
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = Color.Black
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Departamento
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Departamento", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = deptExpanded,
                                        onExpandedChange = { deptExpanded = !deptExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedDepartment,
                                            onValueChange = {},
                                            readOnly = true,
                                            maxLines = 1,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            placeholder = { 
                                                Text(
                                                    "Selecionar", 
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                ) 
                                            },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            trailingIcon = { 
                                                Box(modifier = Modifier.padding(end = 4.dp)) {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded)
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color(0xFF1B4332),
                                                unfocusedLabelColor = Color.Gray,
                                                focusedLabelColor = Color(0xFF1B4332)
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = deptExpanded,
                                            onDismissRequest = { deptExpanded = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            departments.forEach { dept ->
                                                DropdownMenuItem(
                                                    text = { 
                                                        Text(
                                                            dept,
                                                            color = Color.Black,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        ) 
                                                    },
                                                    onClick = {
                                                        selectedDepartment = dept
                                                        deptExpanded = false
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = Color.Black
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Destino e KM Inicial
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(if (isEmergency) 1.5f else 2f)) {
                                    Text("Destino", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = destination,
                                        onValueChange = { destination = it },
                                        placeholder = { Text("Digite o destino", fontSize = 14.sp) },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        leadingIcon = { Icon(CorporateIcons.MapPin, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1B4332)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.LightGray,
                                            focusedBorderColor = Color(0xFF1B4332),
                                            focusedLabelColor = Color(0xFF1B4332)
                                        )
                                    )
                                }

                                if (isEmergency) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("KM Inicial", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = initialKm,
                                            onValueChange = { initialKm = it.filter { char -> char.isDigit() } },
                                            placeholder = { Text("Ex: 12500", fontSize = 14.sp) },
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            leadingIcon = { Icon(CorporateIcons.Monitor, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1B4332)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray,
                                                focusedBorderColor = Color(0xFF1B4332),
                                                focusedLabelColor = Color(0xFF1B4332)
                                            )
                                        )
                                    }
                                }
                            }

                            if (!isEmergency) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Data de Saída", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Box {
                                            OutlinedTextField(
                                                value = formatDate(startDate),
                                                onValueChange = { },
                                                readOnly = true,
                                                placeholder = { Text("dd/mm/aaaa", fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = false,
                                                shape = RoundedCornerShape(12.dp),
                                                leadingIcon = { Icon(CorporateIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = Color.LightGray,
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledPlaceholderColor = Color.Gray,
                                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Hora de Saída", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Box {
                                            OutlinedTextField(
                                                value = formatTime(startTime),
                                                onValueChange = { },
                                                readOnly = true,
                                                placeholder = { Text("--:--", fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = false,
                                                shape = RoundedCornerShape(12.dp),
                                                leadingIcon = { Icon(CorporateIcons.Clock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = Color.LightGray,
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledPlaceholderColor = Color.Gray,
                                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Box(modifier = Modifier.matchParentSize().clickable { showStartTimePicker = true })
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Data de Entrega", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Box {
                                            OutlinedTextField(
                                                value = formatDate(endDate),
                                                onValueChange = { },
                                                readOnly = true,
                                                placeholder = { Text("dd/mm/aaaa", fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = false,
                                                shape = RoundedCornerShape(12.dp),
                                                leadingIcon = { Icon(CorporateIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = Color.LightGray,
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledPlaceholderColor = Color.Gray,
                                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Hora de Entrega", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Box {
                                            OutlinedTextField(
                                                value = formatTime(endTime),
                                                onValueChange = { },
                                                readOnly = true,
                                                placeholder = { Text("--:--", fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = false,
                                                shape = RoundedCornerShape(12.dp),
                                                leadingIcon = { Icon(CorporateIcons.Clock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledBorderColor = Color.LightGray,
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledPlaceholderColor = Color.Gray,
                                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                            Box(modifier = Modifier.matchParentSize().clickable { showEndTimePicker = true })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isEmergency) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Fotos Obrigatórias (4)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B4332)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        PhotoPlaceholder(
                                            label = photoLabels[0],
                                            hasPhoto = capturedPhotos[0] != null,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onTakePhoto(0) }
                                        )
                                        PhotoPlaceholder(
                                            label = photoLabels[1],
                                            hasPhoto = capturedPhotos[1] != null,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onTakePhoto(1) }
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        PhotoPlaceholder(
                                            label = photoLabels[2],
                                            hasPhoto = capturedPhotos[2] != null,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onTakePhoto(2) }
                                        )
                                        PhotoPlaceholder(
                                            label = photoLabels[3],
                                            hasPhoto = capturedPhotos[3] != null,
                                            modifier = Modifier.weight(1f),
                                            onClick = { onTakePhoto(3) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Espaço para não ficar colado na barra fixa
                    Spacer(Modifier.height(100.dp))
                }
            } else {
                ScheduledCarsCalendarView(
                    allBookings = allBookings,
                    cars = cars,
                    onNavigateToNewBooking = { selectedTab = 0 }
                )
            }
            }
        }

        // OVERLAY DE CARREGAMENTO (Em cima do Scaffold para evitar travamentos)
        if (showOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (isSuccess) {
                    BookingSuccessScreen(
                        onDismiss = onBack,
                        isEmergency = isEmergency
                    )
                } else if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LottieAnimation(
                            composition = loadingComposition,
                            modifier = Modifier.size(200.dp),
                            iterations = LottieConstants.IterateForever,
                            speed = 2f
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            if (isEmergency) "Iniciando viagem..." else "Confirmando agendamento...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4332)
                        )
                    }
                } else {
                    LaunchedEffect(Unit) { showOverlay = false }
                }
            }
        }
    }
}

// Modelagem de Dados para Disponibilidade
data class VehicleBookingAvailability(
    val partiallyBookedDates: List<LocalDate> = emptyList(),
    val fullyBookedDates: List<LocalDate> = emptyList(),
    val bookings: List<BookingInfo> = emptyList()
)

data class BookingInfo(
    val userName: String,
    val date: LocalDate,
    val startTime: String,
    val endTime: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CustomVehicleDatePicker(
    availability: VehicleBookingAvailability,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val brandGreen = Color(0xFF1B5E20)
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedDateForDetails by remember { mutableStateOf<LocalDate?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val selectableDates = remember(availability.fullyBookedDates) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.UTC).date
                return !availability.fullyBookedDates.contains(date)
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        selectableDates = selectableDates
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val date = Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(TimeZone.UTC).date
            
            val isPartiallyBooked = availability.partiallyBookedDates.contains(date)
            val isFullyBooked = availability.fullyBookedDates.contains(date)

            if (isPartiallyBooked || isFullyBooked) {
                selectedDateForDetails = date
                showBottomSheet = true
            }
        }
    }

    if (showBottomSheet && selectedDateForDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            val dateBookings = availability.bookings.filter { it.date == selectedDateForDetails }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Agendamentos em ${selectedDateForDetails?.dayOfMonth}/${selectedDateForDetails?.monthNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = brandGreen
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (dateBookings.isEmpty()) {
                    Text("Nenhum detalhe encontrado para esta data.", color = Color.Gray)
                } else {
                    dateBookings.forEach { booking ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = Color(0xFFF5F7F6),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(booking.userName, fontWeight = FontWeight.Bold)
                                Text("${booking.startTime} às ${booking.endTime}", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onDateSelected(datePickerState.selectedDateMillis) },
                colors = ButtonDefaults.textButtonColors(contentColor = brandGreen)
            ) { Text("OK", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                titleContentColor = brandGreen,
                headlineContentColor = brandGreen,
                weekdayContentColor = Color.DarkGray,
                subheadContentColor = brandGreen,
                navigationContentColor = brandGreen,
                yearContentColor = brandGreen,
                selectedDayContainerColor = brandGreen,
                selectedDayContentColor = Color.White,
                todayContentColor = brandGreen,
                todayDateBorderColor = brandGreen,
                selectedYearContainerColor = brandGreen,
                selectedYearContentColor = Color.White,
                dayContentColor = Color.DarkGray,
                disabledDayContentColor = Color.LightGray
            )
        )
        
        Row(
            modifier = Modifier.padding(start = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(brandGreen, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Dias com reservas parciais (Toque para ver)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomVehicleTimePicker(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val brandGreen = Color(0xFF1B5E20)
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text("OK", color = brandGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF5F7F6),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color.DarkGray,
                        selectorColor = brandGreen,
                        containerColor = Color.White,
                        periodSelectorSelectedContainerColor = brandGreen.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = brandGreen,
                        timeSelectorSelectedContainerColor = brandGreen,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = Color(0xFFF5F7F6),
                        timeSelectorUnselectedContentColor = Color.DarkGray
                    )
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledCarsCalendarView(
    allBookings: List<Booking>,
    cars: List<Car>,
    onNavigateToNewBooking: () -> Unit
) {
    val today = remember {
        val now = Clock.System.now()
        now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    var selectedCalendarDate by remember { mutableStateOf(today) }
    var currentDisplayMonthDate by remember { mutableStateOf(today) }
    var selectedCarFilter by remember { mutableStateOf<String?>(null) }

    val activeBookings = remember(allBookings) {
        allBookings.filter { it.status.uppercase() != "CANCELADO" }
    }

    val daysWithBookings = remember(activeBookings) {
        activeBookings.mapNotNull { booking ->
            parseBookingDate(booking.date)
        }.toSet()
    }

    val bookingsForSelectedDate = remember(activeBookings, selectedCalendarDate, selectedCarFilter) {
        activeBookings.filter { booking ->
            val bDate = parseBookingDate(booking.date)
            val matchesDate = bDate == selectedCalendarDate
            val matchesCar = selectedCarFilter == null || booking.carId == selectedCarFilter
            matchesDate && matchesCar
        }.sortedBy { it.scheduledStartTime }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = (-16).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MiniCalendarCard(
            displayDate = currentDisplayMonthDate,
            selectedDate = selectedCalendarDate,
            daysWithBookings = daysWithBookings,
            today = today,
            onDateSelected = { selectedCalendarDate = it },
            onMonthChanged = { currentDisplayMonthDate = it },
            onTodayClicked = {
                currentDisplayMonthDate = today
                selectedCalendarDate = today
            }
        )

        if (cars.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCarFilter == null,
                        onClick = { selectedCarFilter = null },
                        label = { Text("Todos os Carros", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1B4332),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                items(cars) { car ->
                    FilterChip(
                        selected = selectedCarFilter == car.id,
                        onClick = {
                            selectedCarFilter = if (selectedCarFilter == car.id) null else car.id
                        },
                        label = { Text(car.name, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1B4332),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Agendamentos do Dia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
                Text(
                    text = formatLongDatePt(selectedCalendarDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Surface(
                color = Color(0xFF1B4332).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${bookingsForSelectedDate.size} ${if (bookingsForSelectedDate.size == 1) "reserva" else "reservas"}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332)
                )
            }
        }

        if (bookingsForSelectedDate.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B4332).copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            CorporateIcons.Calendar,
                            contentDescription = null,
                            tint = Color(0xFF1B4332),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Nenhum veículo agendado",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Não há reservas marcadas para este dia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToNewBooking,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B4332)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B4332))
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("AGENDAR VEÍCULO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                bookingsForSelectedDate.forEach { booking ->
                    val car = cars.firstOrNull { it.id == booking.carId }
                    ScheduledBookingCard(booking = booking, car = car)
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun MiniCalendarCard(
    displayDate: LocalDate,
    selectedDate: LocalDate,
    daysWithBookings: Set<LocalDate>,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    onTodayClicked: () -> Unit
) {
    val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    val dayHeaders = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    val year = displayDate.year
    val month = displayDate.monthNumber
    val daysInMonth = getDaysInMonth(year, month)
    val firstDayOffset = (LocalDate(year, month, 1).dayOfWeek.isoDayNumber % 7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${monthNames[month - 1]} $year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332)
                    )
                    Spacer(Modifier.width(8.dp))
                    if (displayDate.year != today.year || displayDate.monthNumber != today.monthNumber) {
                        Surface(
                            color = Color(0xFF1B4332).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onTodayClicked() }
                        ) {
                            Text(
                                "HOJE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4332)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val newYear = if (month == 1) year - 1 else year
                            val newMonth = if (month == 1) 12 else month - 1
                            onMonthChanged(LocalDate(newYear, newMonth, 1))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(CorporateIcons.Back, "Mês anterior", tint = Color(0xFF1B4332), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            val newYear = if (month == 12) year + 1 else year
                            val newMonth = if (month == 12) 1 else month + 1
                            onMonthChanged(LocalDate(newYear, newMonth, 1))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(CorporateIcons.ChevronRight, "Próximo mês", tint = Color(0xFF1B4332), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayHeaders.forEach { dayHeader ->
                    Text(
                        text = dayHeader,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            val totalCells = firstDayOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNumber = cellIndex - firstDayOffset + 1

                            if (cellIndex < firstDayOffset || dayNumber > daysInMonth) {
                                Spacer(modifier = Modifier.weight(1f).height(38.dp))
                            } else {
                                val thisDate = LocalDate(year, month, dayNumber)
                                val isSelected = thisDate == selectedDate
                                val isToday = thisDate == today
                                val hasBookings = daysWithBookings.contains(thisDate)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFF1B4332)
                                            else if (isToday) Color(0xFF1B4332).copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) Color(0xFF1B4332) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onDateSelected(thisDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else if (isToday) Color(0xFF1B4332) else Color.DarkGray
                                        )
                                        if (hasBookings) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else Color(0xFF2E7D32))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduledBookingCard(
    booking: Booking,
    car: Car?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B4332).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(CorporateIcons.Car, null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = car?.name ?: "Veículo",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!car?.licensePlate.isNullOrBlank()) {
                            Surface(
                                color = Color.Gray.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = car.licensePlate,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                val (statusText, statusBg, statusColor) = when (booking.status.lowercase()) {
                    "em_andamento", "em andamento" -> Triple("EM ANDAMENTO", Color(0xFFE3F2FD), Color(0xFF1976D2))
                    "finalizado" -> Triple("FINALIZADO", Color(0xFFF5F5F5), Color(0xFF757575))
                    "cancelado" -> Triple("CANCELADO", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                    else -> if (booking.isEmergency) Triple("EMERGENCIAL", Color(0xFFFFEBEE), Color(0xFFD32F2F))
                            else Triple("AGENDADO", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(CorporateIcons.User, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        booking.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!booking.department.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFF1B4332).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            booking.department!!,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1B4332),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(CorporateIcons.Clock, null, tint = Color(0xFF1B4332), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    val timeRange = if (!booking.scheduledEndTime.isNullOrBlank()) {
                        "${booking.scheduledStartTime} - ${booking.scheduledEndTime}"
                    } else {
                        booking.scheduledStartTime
                    }
                    Text(
                        timeRange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4332)
                    )
                }

                if (!booking.resolvedDestination.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(CorporateIcons.MapPin, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            booking.resolvedDestination!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
        else -> 30
    }
}

private fun parseBookingDate(dateStr: String): LocalDate? {
    if (dateStr.isBlank()) return null
    return try {
        LocalDate.parse(dateStr)
    } catch (e: Exception) {
        try {
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                LocalDate(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            } else null
        } catch (e2: Exception) {
            null
        }
    }
}

private fun formatLongDatePt(date: LocalDate): String {
    val daysPt = listOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
    val monthsPt = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val dayOfWeekName = daysPt[date.dayOfWeek.isoDayNumber % 7]
    val monthName = monthsPt[date.monthNumber - 1]
    return "$dayOfWeekName, ${date.dayOfMonth} de $monthName de ${date.year}"
}
