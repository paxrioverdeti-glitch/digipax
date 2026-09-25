package com.example.pxrioverde.ui.admin

import com.example.pxrioverde.util.AdaptiveUtils
import com.example.pxrioverde.util.WindowSizeClass
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import com.example.pxrioverde.viewmodel.AnalyticsViewModel
import com.example.pxrioverde.viewmodel.AnalyticsUiState
import com.example.pxrioverde.ui.admin.components.KmUsageChart
import com.example.pxrioverde.ui.admin.components.TicketGroupPerformanceCard
import com.example.pxrioverde.ui.admin.components.Appointment
import com.example.pxrioverde.ui.admin.components.AppointmentRowItem
import com.example.pxrioverde.ui.admin.components.PhotoDetailsDialog
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.AdminMetricCard
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car
import kotlinx.datetime.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    viewModel: AnalyticsViewModel,
    isDesktop: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedMonth by remember { mutableStateOf("Geral") }
    var selectedYear by remember { mutableStateOf("2026") }
    var selectedUser by remember { mutableStateOf("Todos") }
    var selectedVehicle by remember { mutableStateOf("Todos") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var supportMonthFilter by remember { mutableStateOf("Geral") }
    var showSupportMonthDropdown by remember { mutableStateOf(false) }
    var currentActivityTab by remember { mutableStateOf("Agendados") } // "Agendados", "Finalizados", "Chamados", "Compras"
    var selectedAppointmentForPhotos by remember { mutableStateOf<Appointment?>(null) }
    var selectedTicketForDetails by remember { mutableStateOf<com.example.pxrioverde.model.Ticket?>(null) }
    var showTicketAttachmentDialog by remember { mutableStateOf(false) }

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showUserDropdown by remember { mutableStateOf(false) }
    var showVehicleDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val months = listOf("Geral", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val years = remember {
        val startYear = 2024
        val endYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        val list = mutableListOf("Geral")
        for (y in startYear..maxOf(startYear, endYear)) {
            list.add(y.toString())
        }
        list
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val widthSizeClass = AdaptiveUtils.calculateWindowSizeClass(maxWidth)
        val isWide = isDesktop || widthSizeClass != WindowSizeClass.COMPACT
        
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            }
            is AnalyticsUiState.Success -> {
                val appointments = state.data.appointments
                val tickets = state.data.allTickets
                val userList = remember(appointments, tickets) { 
                    (listOf("Todos") + (appointments.map { it.userName } + tickets.map { it.userName }).distinct().sorted())
                }

                val filteredAppointments = remember(appointments, currentActivityTab, selectedMonth, selectedYear, selectedUser, selectedVehicle, selectedDate) {
                    appointments.filter { appointment ->
                        val statusMatch = if (currentActivityTab == "Finalizados") appointment.isFinished else !appointment.isFinished
                        val userMatch = if (selectedUser == "Todos") true else appointment.userName == selectedUser
                        val vehicleMatch = if (selectedVehicle == "Todos") true else appointment.vehicleName == selectedVehicle
                        
                        // Filtro de Calendário (Prioridade Alta)
                        val dateMatch = if (selectedDate != null) {
                            appointment.date == selectedDate.toString()
                        } else {
                            val dateParts = appointment.date.split("-")
                            val yearMatch = if (selectedYear == "Geral") true else dateParts.firstOrNull() == selectedYear
                            val monthIndex = months.indexOf(selectedMonth)
                            val monthMatch = if (selectedMonth == "Geral") true 
                                            else dateParts.getOrNull(1) == monthIndex.toString().padStart(2, '0')
                            yearMatch && monthMatch
                        }
                        
                        userMatch && vehicleMatch && dateMatch && (if (currentActivityTab == "Chamados") true else statusMatch)
                    }
                }

                val filteredTickets = remember(tickets, selectedUser) {
                    tickets.filter { ticket ->
                        val userMatch = if (selectedUser == "Todos") true else ticket.userName == selectedUser
                        userMatch
                    }
                }

                val purchases = state.data.allPurchases
                val filteredPurchases = remember(purchases, selectedMonth, selectedYear, selectedUser) {
                    purchases.filter { purchase ->
                        val userMatch = if (selectedUser == "Todos") true else purchase.requesterName == selectedUser
                        val dt = Instant.fromEpochMilliseconds(purchase.createdAt).toLocalDateTime(TimeZone.currentSystemDefault())
                        val yearMatch = if (selectedYear == "Geral") true else dt.year.toString() == selectedYear
                        val monthIndex = months.indexOf(selectedMonth)
                        val monthMatch = if (selectedMonth == "Geral") true 
                                        else dt.month.ordinal + 1 == monthIndex
                        userMatch && yearMatch && monthMatch
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Relatórios e Métricas",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // --- SEÇÃO: FILTROS ---
                    item {
                        FilterSection(
                            selectedMonth = selectedMonth,
                            onMonthClick = { showMonthDropdown = true },
                            selectedYear = selectedYear,
                            onYearClick = { showYearDropdown = true },
                            selectedUser = selectedUser,
                            onUserClick = { showUserDropdown = true },
                            selectedVehicle = selectedVehicle,
                            onVehicleClick = { showVehicleDropdown = true },
                            selectedDate = selectedDate,
                            onDateClick = { showDatePicker = true },
                            onClearDate = { selectedDate = null },
                            showMonthDropdown = showMonthDropdown,
                            onMonthDismiss = { showMonthDropdown = false },
                            months = months,
                            onMonthSelect = { selectedMonth = it; showMonthDropdown = false },
                            showYearDropdown = showYearDropdown,
                            onYearDismiss = { showYearDropdown = false },
                            years = years,
                            onYearSelect = { selectedYear = it; showYearDropdown = false },
                            showUserDropdown = showUserDropdown,
                            onUserDismiss = { showUserDropdown = false },
                            userList = userList,
                            onUserSelect = { selectedUser = it; showUserDropdown = false },
                            showVehicleDropdown = showVehicleDropdown,
                            onVehicleDismiss = { showVehicleDropdown = false },
                            vehicleList = listOf("Todos") + state.data.availableVehicles,
                            onVehicleSelect = { selectedVehicle = it; showVehicleDropdown = false },
                            isDateFilterVisible = currentActivityTab != "Chamados",
                            isVehicleFilterVisible = currentActivityTab != "Chamados"
                        )
                    }

                    // --- SEÇÃO 1: GESTÃO DE FROTAS (KM) ---
                    item { 
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader("Gestão de Frotas", painterResource(Res.drawable.car))
                            IconButton(onClick = { viewModel.exportTripsReport() }) {
                                Icon(CorporateIcons.Download, contentDescription = "Exportar CSV", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    item {
                        val totalKmFiltered = filteredAppointments.sumOf { it.kmTraveled }
                        val totalKmGeneral = state.data.kmMetrics.sumOf { it.totalKm }

                        if (isWide) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                AdminMetricCard(
                                    title = "KM Filtrado",
                                    value = totalKmFiltered.toString(),
                                    unit = "km",
                                    icon = painterResource(Res.drawable.car),
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    title = "KM Total Geral",
                                    value = totalKmGeneral.toInt().toString(),
                                    unit = "km",
                                    icon = painterResource(Res.drawable.car),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AdminMetricCard(
                                    title = "KM Filtrado",
                                    value = totalKmFiltered.toString(),
                                    unit = "km",
                                    icon = painterResource(Res.drawable.car),
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    title = "KM Total Geral",
                                    value = totalKmGeneral.toInt().toString(),
                                    unit = "km",
                                    icon = painterResource(Res.drawable.car),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    item { KmUsageChart(state.data.kmMetrics) }

                    // --- SEÇÃO 2: SUPORTE E CHAMADOS ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SectionHeader("Suporte e Chamados", rememberVectorPainter(CorporateIcons.Tickets))
                                IconButton(onClick = { viewModel.exportTicketsReport() }) {
                                    Icon(CorporateIcons.Download, contentDescription = "Exportar CSV", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            
                            // Filtro de Mês Local para esta seção
                            Box {
                                FilterChip(
                                    selected = supportMonthFilter != "Geral",
                                    onClick = { showSupportMonthDropdown = true },
                                    label = { Text(supportMonthFilter, style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = Color.Gray
                                    )
                                )
                                DropdownMenu(
                                    expanded = showSupportMonthDropdown,
                                    onDismissRequest = { showSupportMonthDropdown = false },
                                    modifier = Modifier.background(Color.White).heightIn(max = 300.dp)
                                ) {
                                    months.forEach { month ->
                                        DropdownMenuItem(
                                            text = { Text(month) },
                                            onClick = { 
                                                supportMonthFilter = month
                                                showSupportMonthDropdown = false 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        // Cálculo dinâmico baseado no filtro de mês local
                        val filteredSupportTickets = if (supportMonthFilter == "Geral") {
                            tickets
                        } else {
                            val monthIdx = months.indexOf(supportMonthFilter)
                            tickets.filter { t ->
                                try {
                                    val date = Instant.parse(t.createdAt!!).toLocalDateTime(TimeZone.currentSystemDefault())
                                    date.month.ordinal + 1 == monthIdx
                                } catch (e: Exception) { false }
                            }
                        }

                        val gobahFiltered = filteredSupportTickets.filter { com.example.pxrioverde.model.TicketConstants.isGobah(it.sector) }
                        val tiFiltered = filteredSupportTickets.filter { !com.example.pxrioverde.model.TicketConstants.isGobah(it.sector) }

                        val tiPending = tiFiltered.count { it.status != com.example.pxrioverde.model.TicketStatus.RESOLVIDO && it.status != com.example.pxrioverde.model.TicketStatus.CANCELADO }
                        val tiResolved = tiFiltered.count { it.status == com.example.pxrioverde.model.TicketStatus.RESOLVIDO }
                        
                        val gobahPending = gobahFiltered.count { it.status != com.example.pxrioverde.model.TicketStatus.RESOLVIDO && it.status != com.example.pxrioverde.model.TicketStatus.CANCELADO }
                        val gobahResolved = gobahFiltered.count { it.status == com.example.pxrioverde.model.TicketStatus.RESOLVIDO }

                        // Métrica de Avaliação Média
                        val ticketsWithRating = filteredSupportTickets.filter { it.rating != null && it.rating!! > 0 }
                        val avgRating = if (ticketsWithRating.isNotEmpty()) ticketsWithRating.map { it.rating!! }.average() else 0.0

                        if (isWide) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                AdminMetricCard(
                                    title = "Pendentes T.I",
                                    value = tiPending.toString(),
                                    unit = "chamados",
                                    icon = rememberVectorPainter(CorporateIcons.Tickets),
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    title = "Pendentes Gobah",
                                    value = gobahPending.toString(),
                                    unit = "chamados",
                                    icon = rememberVectorPainter(CorporateIcons.Tickets),
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    title = "Média Avaliação",
                                    value = if(avgRating > 0) ((avgRating * 10).toInt() / 10.0).toString() else "--",
                                    unit = "estrelas",
                                    icon = rememberVectorPainter(Icons.Default.Star),
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    title = "Total Avaliados",
                                    value = ticketsWithRating.size.toString(),
                                    unit = "feedbacks",
                                    icon = rememberVectorPainter(CorporateIcons.Support),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AdminMetricCard(
                                        title = "Pendentes T.I",
                                        value = tiPending.toString(),
                                        unit = "chamados",
                                        icon = rememberVectorPainter(CorporateIcons.Tickets),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminMetricCard(
                                        title = "Pendentes Gobah",
                                        value = gobahPending.toString(),
                                        unit = "chamados",
                                        icon = rememberVectorPainter(CorporateIcons.Tickets),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AdminMetricCard(
                                        title = "Média Avaliação",
                                        value = if(avgRating > 0) ((avgRating * 10).toInt() / 10.0).toString() else "--",
                                        unit = "estrelas",
                                        icon = rememberVectorPainter(Icons.Default.Star),
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminMetricCard(
                                        title = "Total Avaliados",
                                        value = ticketsWithRating.size.toString(),
                                        unit = "feedbacks",
                                        icon = rememberVectorPainter(CorporateIcons.Support),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Nota: O TicketGroupPerformanceCard continuará mostrando o desempenho GERAL do banco para manter a tendência histórica,
                    // mas os cards numéricos acima respondem ao filtro de mês.
                    item { TicketGroupPerformanceCard(state.data.tiPerformance, state.data.gobahPerformance) }

                    // --- SEÇÃO 3: RELATÓRIO FINANCEIRO (COMPRAS) ---
                    item {
                        SectionHeader("Relatório Financeiro", rememberVectorPainter(CorporateIcons.Receipt))
                    }
                    item {
                        val approvedInPeriod = filteredPurchases.filter { it.status == com.example.pxrioverde.model.PurchaseStatus.APROVADO || it.status == com.example.pxrioverde.model.PurchaseStatus.COMPRADO }
                        val totalValue = approvedInPeriod.sumOf { it.approvedValue ?: 0.0 }
                        
                        val spendingBySector = approvedInPeriod.groupBy { it.department }
                            .map { (dept, list) -> dept to list.sumOf { it.approvedValue ?: 0.0 } }
                            .sortedByDescending { it.second }

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            AdminMetricCard(
                                title = "Total Aprovado no Período",
                                value = "R$ ${((totalValue * 100).toInt() / 100.0)}",
                                unit = "",
                                icon = rememberVectorPainter(CorporateIcons.Receipt),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (spendingBySector.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Gastos por Setor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Spacer(Modifier.height(12.dp))
                                        spendingBySector.forEach { (sector, value) ->
                                            SectorSpendingRow(sector, value, totalValue)
                                            if (sector != spendingBySector.last().first) {
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SEÇÃO 4: DETALHAMENTO DE ATIVIDADES ---
                    item { 
                        Spacer(Modifier.height(8.dp))
                        SectionHeader("Detalhamento de Atividades", rememberVectorPainter(CorporateIcons.Clock)) 
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            val tabModifier = Modifier.weight(1f)
                            ActivityTab(tabModifier, "Agendados", currentActivityTab == "Agendados") { currentActivityTab = "Agendados" }
                            ActivityTab(tabModifier, "Finalizados", currentActivityTab == "Finalizados") { currentActivityTab = "Finalizados" }
                            ActivityTab(tabModifier, "Chamados", currentActivityTab == "Chamados") { currentActivityTab = "Chamados" }
                            ActivityTab(tabModifier, "Compras", currentActivityTab == "Compras") { currentActivityTab = "Compras" }
                        }
                    }

                    if (isWide) {
                        item {
                            Box(modifier = Modifier.heightIn(max = 800.dp)) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 350.dp),
                                    modifier = Modifier.fillMaxWidth().height(600.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    if (currentActivityTab == "Chamados") {
                                        gridItems(filteredTickets) { ticket ->
                                            AnalyticsTicketRowItem(
                                                ticket = ticket,
                                                onClick = { selectedTicketForDetails = ticket }
                                            )
                                        }
                                    } else if (currentActivityTab == "Compras") {
                                        gridItems(filteredPurchases) { purchase ->
                                            PurchaseAnalyticsRowItem(purchase)
                                        }
                                    } else {
                                        gridItems(filteredAppointments) { appointment ->
                                            AppointmentRowItem(
                                                appointment = appointment,
                                                onViewPhotosClick = { selectedAppointmentForPhotos = appointment }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (currentActivityTab == "Chamados") {
                            items(filteredTickets) { ticket ->
                                AnalyticsTicketRowItem(
                                    ticket = ticket,
                                    onClick = { selectedTicketForDetails = ticket }
                                )
                            }
                        } else if (currentActivityTab == "Compras") {
                            items(filteredPurchases) { purchase ->
                                PurchaseAnalyticsRowItem(purchase)
                            }
                        } else {
                            items(filteredAppointments) { appointment ->
                                AppointmentRowItem(
                                    appointment = appointment,
                                    onViewPhotosClick = { selectedAppointmentForPhotos = appointment }
                                )
                            }
                        }
                    }
                }
            }
            is AnalyticsUiState.Error -> {
                ErrorState(state.message) { viewModel.loadData() }
            }
        }

        selectedAppointmentForPhotos?.let { appointment ->
            PhotoDetailsDialog(appointment = appointment, onDismiss = { selectedAppointmentForPhotos = null })
        }

        selectedTicketForDetails?.let { ticket ->
            AlertDialog(
                onDismissRequest = {
                    selectedTicketForDetails = null
                    showTicketAttachmentDialog = false
                },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ticket.title)
                        ticket.rating?.let {
                            Spacer(Modifier.width(8.dp))
                            com.example.pxrioverde.ui.admin.components.TicketRatingBar(it)
                        }
                    }
                },
                text = {
                    Column {
                        Text("Usuário: ${ticket.userName}", fontWeight = FontWeight.Bold)
                        Text("Setor: ${ticket.sector}")
                        Spacer(Modifier.height(8.dp))
                        Text(ticket.description)

                        if (!ticket.imageUrl.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            AsyncImage(
                                model = ticket.imageUrl,
                                contentDescription = "Anexo do chamado",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showTicketAttachmentDialog = true },
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        ticket.ratingComment?.let { comment ->
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))
                            Text("Feedback do Usuário:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(comment, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedTicketForDetails = null
                        showTicketAttachmentDialog = false
                    }) { Text("OK") }
                }
            )
        }

        if (showTicketAttachmentDialog) {
            selectedTicketForDetails?.imageUrl?.let { imageUrl ->
                com.example.pxrioverde.ui.admin.components.AvatarFullscreenDialog(
                    url = imageUrl,
                    name = "Anexo do chamado",
                    onDismiss = { showTicketAttachmentDialog = false }
                )
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.painter.Painter) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    selectedMonth: String,
    onMonthClick: () -> Unit,
    selectedYear: String,
    onYearClick: () -> Unit,
    selectedUser: String,
    onUserClick: () -> Unit,
    selectedVehicle: String,
    onVehicleClick: () -> Unit,
    selectedDate: LocalDate?,
    onDateClick: () -> Unit,
    onClearDate: () -> Unit,
    showMonthDropdown: Boolean,
    onMonthDismiss: () -> Unit,
    months: List<String>,
    onMonthSelect: (String) -> Unit,
    showYearDropdown: Boolean,
    onYearDismiss: () -> Unit,
    years: List<String>,
    onYearSelect: (String) -> Unit,
    showUserDropdown: Boolean,
    onUserDismiss: () -> Unit,
    userList: List<String>,
    onUserSelect: (String) -> Unit,
    showVehicleDropdown: Boolean,
    onVehicleDismiss: () -> Unit,
    vehicleList: List<String>,
    onVehicleSelect: (String) -> Unit,
    isDateFilterVisible: Boolean = true,
    isVehicleFilterVisible: Boolean = true
) {
    val filterColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = Color.White,
        selectedTrailingIconColor = Color.White,
        containerColor = Color.White,
        labelColor = Color.Gray
    )

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(CorporateIcons.Support, contentDescription = "Filtros", tint = Color.Gray, modifier = Modifier.size(20.dp))

        if (isDateFilterVisible) {
            // Filtro de Calendário
            FilterChip(
                selected = selectedDate != null,
                onClick = onDateClick,
                label = { 
                    Text(if (selectedDate == null) "Data" else "${selectedDate.dayOfMonth}/${selectedDate.month.ordinal + 1}")
                },
                leadingIcon = { Icon(CorporateIcons.Calendar, null, modifier = Modifier.size(16.dp)) },
                trailingIcon = if (selectedDate != null) {
                    {
                        IconButton(onClick = onClearDate, modifier = Modifier.size(16.dp)) {
                            Icon(CorporateIcons.Close, null)
                        }
                    }
                } else null,
                colors = filterColors
            )
        }

        if (selectedDate == null || !isDateFilterVisible) {
            if (isDateFilterVisible) {
                // Filtro de Mês
                Box {
                    FilterChip(
                        selected = selectedMonth != "Geral",
                        onClick = onMonthClick,
                        label = { Text(selectedMonth) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = filterColors,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedMonth != "Geral",
                            borderColor = if(selectedMonth != "Geral") Color.Transparent else Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                    DropdownMenu(expanded = showMonthDropdown, onDismissRequest = onMonthDismiss) {
                        months.forEach { month -> DropdownMenuItem(text = { Text(month) }, onClick = { onMonthSelect(month) }) }
                    }
                }

                // Filtro de Ano
                Box {
                    FilterChip(
                        selected = selectedYear != "Geral",
                        onClick = onYearClick,
                        label = { Text(selectedYear) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        colors = filterColors,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedYear != "Geral",
                            borderColor = if(selectedYear != "Geral") Color.Transparent else Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                    DropdownMenu(expanded = showYearDropdown, onDismissRequest = onYearDismiss) {
                        years.forEach { year -> DropdownMenuItem(text = { Text(year) }, onClick = { onYearSelect(year) }) }
                    }
                }
            }
        }

        Box {
            FilterChip(
                selected = selectedUser != "Todos",
                onClick = onUserClick,
                label = { Text(if(selectedUser == "Todos") "Usuário" else selectedUser) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                colors = filterColors,
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedUser != "Todos", borderColor = Color.LightGray.copy(alpha = 0.5f))
            )
            DropdownMenu(expanded = showUserDropdown, onDismissRequest = onUserDismiss) {
                userList.forEach { user -> DropdownMenuItem(text = { Text(user) }, onClick = { onUserSelect(user) }) }
            }
        }

        if (isVehicleFilterVisible) {
            Box {
                FilterChip(
                    selected = selectedVehicle != "Todos",
                    onClick = onVehicleClick,
                    label = { Text(if(selectedVehicle == "Todos") "Veículo" else selectedVehicle) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = filterColors,
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedVehicle != "Todos", borderColor = Color.LightGray.copy(alpha = 0.5f))
                )
                DropdownMenu(expanded = showVehicleDropdown, onDismissRequest = onVehicleDismiss) {
                    vehicleList.forEach { vehicle -> DropdownMenuItem(text = { Text(vehicle) }, onClick = { onVehicleSelect(vehicle) }) }
                }
            }
        }
    }
}

@Composable
fun AnalyticsTicketRowItem(ticket: com.example.pxrioverde.model.Ticket, onClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (ticket.rating != null && !ticket.ratingComment.isNullOrBlank()) {
                    isExpanded = !isExpanded
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                var showAvatarDialog by remember { mutableStateOf(false) }

                AsyncImage(
                    model = ticket.userAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .clickable { if (ticket.userAvatarUrl != null) showAvatarDialog = true },
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(CorporateIcons.User),
                    error = rememberVectorPainter(CorporateIcons.User)
                )

                if (showAvatarDialog) {
                    com.example.pxrioverde.ui.admin.components.AvatarFullscreenDialog(
                        url = ticket.userAvatarUrl!!,
                        name = ticket.userName,
                        onDismiss = { showAvatarDialog = false }
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Usuário: ${ticket.userName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        
                        ticket.rating?.let { rating ->
                            if (rating > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                com.example.pxrioverde.ui.admin.components.TicketRatingBar(rating)
                            }
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    com.example.pxrioverde.ui.admin.components.TicketStatusBadge(ticket.status)
                    if (ticket.ratingComment != null) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.List,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp).padding(top = 4.dp)
                        )
                    }
                }
            }

            if (isExpanded && !ticket.ratingComment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Feedback:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${ticket.ratingComment}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        
                        TextButton(
                            onClick = onClick,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.align(Alignment.End).height(32.dp)
                        ) {
                            Text("VER DETALHES", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityTab(modifier: Modifier, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SectorSpendingRow(sector: String, value: Double, total: Double) {
    val percentage = if (total > 0) (value / total).toFloat() else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sector, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("R$ ${((value * 100).toInt() / 100.0)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B4332))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = Color(0xFF2D6A4F),
            trackColor = Color.LightGray.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun PurchaseAnalyticsRowItem(purchase: com.example.pxrioverde.model.PurchaseRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color(0xFF1B4332).copy(alpha = 0.1f)
                ) {
                    Icon(CorporateIcons.Receipt, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF1B4332))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(purchase.itemName, fontWeight = FontWeight.Bold)
                    Text("De: ${purchase.requesterName} • ${purchase.department}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                com.example.pxrioverde.ui.purchase.StatusBadge(purchase.status)
            }
            
            if (purchase.approvedValue != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Valor Aprovado:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("R$ ${purchase.approvedValue}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Tentar Novamente") }
    }
}
