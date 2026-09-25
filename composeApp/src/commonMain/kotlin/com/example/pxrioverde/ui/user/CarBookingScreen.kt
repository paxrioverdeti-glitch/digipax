package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
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
                    destination = destination,
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
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1B4332),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .padding(top = 56.dp, bottom = 40.dp)
                        .padding(horizontal = 24.dp)
                ) {
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
                                "Novo Agendamento",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                }

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
