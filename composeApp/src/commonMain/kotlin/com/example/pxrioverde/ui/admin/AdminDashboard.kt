package com.example.pxrioverde.ui.admin

import androidx.compose.foundation.BorderStroke
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
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.model.*
import com.example.pxrioverde.service.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import com.example.pxrioverde.viewmodel.*
import org.koin.compose.viewmodel.koinViewModel
import com.example.pxrioverde.ui.components.GlassCard
import com.example.pxrioverde.ui.theme.AdminTheme
import com.example.pxrioverde.ui.components.AdminEmptyState
import com.example.pxrioverde.ui.components.AdminMetricCard
import com.example.pxrioverde.ui.admin.components.AdminHeader
import com.example.pxrioverde.ui.admin.components.AdminSideBar
import com.example.pxrioverde.ui.feed.MuralScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil3.compose.AsyncImage
import com.example.pxrioverde.util.CommonBackHandler
import com.example.pxrioverde.util.DateUtils
import kotlinx.datetime.*
import org.jetbrains.compose.resources.painterResource
import com.example.pxrioverde.service.NotificationService
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun AdminDashboard(
    user: User, 
    viewModel: TicketViewModel, 
    tripViewModel: TripViewModel,
    notificationService: NotificationService,
    onLogout: () -> Unit,
    analyticsViewModel: AnalyticsViewModel,
    absenceViewModel: AbsenceViewModel,
    adminViewModel: AdminViewModel = koinViewModel(),
    purchaseViewModel: PurchaseViewModel = koinViewModel(),
    feedViewModel: FeedViewModel = koinViewModel()
) {
    AdminTheme {
        val selectedSection by adminViewModel.selectedSection.collectAsState()
        val showProfile by adminViewModel.showProfile.collectAsState()
        val showNotifications by adminViewModel.showNotifications.collectAsState()
        val selectedAbsence by adminViewModel.selectedAbsence.collectAsState()
        val selectedTicket by viewModel.selectedTicket.collectAsState()

        CommonBackHandler(enabled = selectedTicket != null || selectedAbsence != null || selectedSection != 0 || showProfile || showNotifications) {
            if (selectedTicket != null) {
                viewModel.selectTicket(null as Ticket?)
            } else {
                adminViewModel.goBack()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthSizeClass = AdaptiveUtils.calculateWindowSizeClass(maxWidth)
            val isWideScreen = widthSizeClass != WindowSizeClass.COMPACT

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AdminSideBar(
                        selectedSection = selectedSection,
                        onSectionSelected = { 
                            adminViewModel.selectSection(it)
                        },
                        modifier = Modifier.width(85.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background)) {
                        AdminHeader(
                            selectedTabIndex = selectedSection,
                            onTabSelected = { adminViewModel.selectSection(it) },
                            notificationService = notificationService,
                            onNotificationsClick = { 
                                adminViewModel.setShowNotifications(true)
                            },
                            onProfileClick = { 
                                adminViewModel.setShowProfile(true)
                            },
                            isDesktop = true
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            AdminContentArea(
                                selectedSection = selectedSection,
                                showProfile = showProfile,
                                showNotifications = showNotifications,
                                user = user,
                                viewModel = viewModel,
                                analyticsViewModel = analyticsViewModel,
                                notificationService = notificationService,
                                onLogout = onLogout,
                                onBackProfile = { adminViewModel.setShowProfile(false) },
                                onBackNotifications = { adminViewModel.setShowNotifications(false) },
                                selectedAbsence = selectedAbsence,
                                onSelectAbsence = { adminViewModel.selectAbsence(it) },
                                absenceViewModel = absenceViewModel,
                                purchaseViewModel = purchaseViewModel,
                                feedViewModel = feedViewModel,
                                isDesktop = true
                            )
                        }

                    }
                }
            } else {
                Scaffold(
                    topBar = {
                        if (selectedTicket == null && selectedAbsence == null) {
                            AdminHeader(
                                selectedTabIndex = selectedSection,
                                onTabSelected = { 
                                    adminViewModel.selectSection(it)
                                },
                                notificationService = notificationService,
                                onNotificationsClick = { 
                                    adminViewModel.setShowNotifications(true)
                                },
                                onProfileClick = { 
                                    adminViewModel.setShowProfile(true)
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (!showProfile && !showNotifications && selectedTicket == null && selectedAbsence == null) {
                            NavigationBar(
                                containerColor = Color.White,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = selectedSection == 0,
                                    onClick = { adminViewModel.selectSection(0) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedSection == 0) Icons.Filled.ConfirmationNumber else Icons.Outlined.ConfirmationNumber,
                                            contentDescription = "Chamados"
                                        )
                                    },
                                    label = { Text("Chamados") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF1B4332),
                                        selectedTextColor = Color(0xFF1B4332),
                                        indicatorColor = Color(0xFF1B4332).copy(alpha = 0.1f),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedSection == 1,
                                    onClick = { adminViewModel.selectSection(1) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedSection == 1) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar,
                                            contentDescription = "Veículos"
                                        )
                                    },
                                    label = { Text("Veículos") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF1B4332),
                                        selectedTextColor = Color(0xFF1B4332),
                                        indicatorColor = Color(0xFF1B4332).copy(alpha = 0.1f),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedSection == 2,
                                    onClick = { adminViewModel.selectSection(2) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedSection == 2) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                                            contentDescription = "Métricas"
                                        )
                                    },
                                    label = { Text("Métricas") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF1B4332),
                                        selectedTextColor = Color(0xFF1B4332),
                                        indicatorColor = Color(0xFF1B4332).copy(alpha = 0.1f),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedSection == 3,
                                    onClick = { adminViewModel.selectSection(3) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedSection == 3) Icons.Filled.Star else Icons.Default.Star,
                                            contentDescription = "Ausências"
                                        )
                                    },
                                    label = { Text("DP") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF1B4332),
                                        selectedTextColor = Color(0xFF1B4332),
                                        indicatorColor = Color(0xFF1B4332).copy(alpha = 0.1f),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedSection == 5,
                                    onClick = { adminViewModel.selectSection(5) },
                                    icon = {
                                        Icon(
                                            imageVector = CorporateIcons.Mural,
                                            contentDescription = "Mural"
                                        )
                                    },
                                    label = { Text("Mural") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF1B4332),
                                        selectedTextColor = Color(0xFF1B4332),
                                        indicatorColor = Color(0xFF1B4332).copy(alpha = 0.1f),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        AdminContentArea(
                            selectedSection = selectedSection,
                            showProfile = showProfile,
                            showNotifications = showNotifications,
                            user = user,
                            viewModel = viewModel,
                            analyticsViewModel = analyticsViewModel,
                            notificationService = notificationService,
                            onLogout = onLogout,
                            onBackProfile = { adminViewModel.setShowProfile(false) },
                            onBackNotifications = { adminViewModel.setShowNotifications(false) },
                            selectedAbsence = selectedAbsence,
                            onSelectAbsence = { adminViewModel.selectAbsence(it) },
                            absenceViewModel = absenceViewModel,
                            purchaseViewModel = purchaseViewModel,
                            feedViewModel = feedViewModel,
                            isDesktop = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminContentArea(
    selectedSection: Int,
    showProfile: Boolean,
    showNotifications: Boolean,
    user: User,
    viewModel: TicketViewModel,
    analyticsViewModel: AnalyticsViewModel,
    notificationService: NotificationService,
    onLogout: () -> Unit,
    onBackProfile: () -> Unit,
    onBackNotifications: () -> Unit,
    selectedAbsence: ComunicadoState?,
    onSelectAbsence: (ComunicadoState?) -> Unit,
    absenceViewModel: AbsenceViewModel,
    purchaseViewModel: PurchaseViewModel,
    feedViewModel: FeedViewModel,
    isDesktop: Boolean
) {
    when {
        showProfile -> AdminProfileScreen(
            user = user, 
            onLogout = onLogout, 
            onBack = onBackProfile
        )
        showNotifications -> AdminNotificationsScreen(
            notificationService = notificationService,
            onBack = onBackNotifications
        )
        else -> when (selectedSection) {
            0 -> TicketManagementSection(user, viewModel, isDesktop)
            1 -> CarManagementSection(isDesktop)
            2 -> AdminAnalyticsScreen(viewModel = analyticsViewModel, isDesktop = isDesktop)
            3 -> AbsenceManagementSection(selectedAbsence, onSelectAbsence, absenceViewModel, isDesktop)
            4 -> PurchaseManagementSection(user, purchaseViewModel, isDesktop)
            5 -> MuralScreen(user = user, viewModel = feedViewModel)
        }
    }
}

@Composable
fun TicketManagementSection(user: User, viewModel: TicketViewModel, isDesktop: Boolean = false) {
    val allTickets by viewModel.tickets.collectAsState()
    val selectedTicket by viewModel.selectedTicket.collectAsState()
    
    var sectorFilter by remember { mutableStateOf("Todos") }
    var statusFilter by remember { mutableStateOf("Pendentes") }
    var monthFilter by remember { mutableStateOf("Geral") }

    val months = listOf("Geral", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

    val filteredTickets = remember(allTickets, sectorFilter, statusFilter, monthFilter) {
        allTickets.filter { ticket ->
            val isTicketGobah = com.example.pxrioverde.model.TicketConstants.isGobah(ticket.sector)
            val sectorMatch = when(sectorFilter) {
                "Gobah" -> isTicketGobah
                "T.I Interno" -> !isTicketGobah
                else -> true
            }
            val isTicketResolved = ticket.status == TicketStatus.RESOLVIDO || ticket.status == TicketStatus.CANCELADO
            val statusMatch = if (statusFilter == "Resolvidos") isTicketResolved else !isTicketResolved
            val dateMatch = if (monthFilter == "Geral") true else {
                try {
                    val instant = kotlinx.datetime.Instant.parse(ticket.createdAt!!)
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val monthIndex = months.indexOf(monthFilter)
                    localDate.month.number == monthIndex
                } catch (e: Exception) { false }
            }
            sectorMatch && statusMatch && dateMatch
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllTickets()
        viewModel.observeAllTicketsRealtime()
    }

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(350.dp).fillMaxHeight().background(Color.White)) {
                AdminTicketFilters(
                    sectorFilter = sectorFilter,
                    onSectorChange = { sectorFilter = it },
                    statusFilter = statusFilter,
                    onStatusChange = { statusFilter = it },
                    monthFilter = monthFilter,
                    onMonthChange = { monthFilter = it },
                    months = months,
                    onRefresh = { viewModel.loadAllTickets() }
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                if (filteredTickets.isEmpty()) {
                    AdminEmptyState(title = "Nenhum chamado", description = "Tente outros filtros.", icon = rememberVectorPainter(CorporateIcons.Tickets), actionLabel = "Limpar", onActionClick = { sectorFilter = "Todos"; statusFilter = "Pendentes"; monthFilter = "Geral" })
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredTickets) { ticket ->
                            val isSelected = selectedTicket?.id == ticket.id
                            AdminTicketItemSmall(
                                ticket = ticket, 
                                isSelected = isSelected,
                                onClick = { viewModel.selectTicket(ticket) }
                            )
                        }
                    }
                }
            }

            VerticalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF5F7F6))) {
                if (selectedTicket != null) {
                    TicketDetailView(
                        user = user,
                        ticket = selectedTicket!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectTicket(null as Ticket?) },
                        isDesktop = true
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(CorporateIcons.Tickets, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Text("Selecione um chamado para visualizar os detalhes", color = Color.Gray)
                        }
                    }
                }
            }
        }
    } else {
        if (selectedTicket == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filtros no topo com padding adequado
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    AdminTicketFilters(
                        sectorFilter = sectorFilter,
                        onSectorChange = { sectorFilter = it },
                        statusFilter = statusFilter,
                        onStatusChange = { statusFilter = it },
                        monthFilter = monthFilter,
                        onMonthChange = { monthFilter = it },
                        months = months,
                        onRefresh = { viewModel.loadAllTickets() }
                    )
                }

                if (filteredTickets.isEmpty()) {
                    AdminEmptyState(title = "Nenhum chamado", description = "Tente outros filtros.", icon = rememberVectorPainter(CorporateIcons.Tickets), actionLabel = "Limpar", onActionClick = { sectorFilter = "Todos"; statusFilter = "Pendentes"; monthFilter = "Geral" })
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredTickets) { ticket ->
                            AdminTicketItem(ticket, onClick = { viewModel.selectTicket(ticket) })
                        }
                    }
                }
            }
        } else {
            TicketDetailView(user = user, ticket = selectedTicket!!, viewModel = viewModel, onBack = { viewModel.selectTicket(null as Ticket?) })
        }
    }
}

@Composable
fun AdminTicketFilters(
    sectorFilter: String,
    onSectorChange: (String) -> Unit,
    statusFilter: String,
    onStatusChange: (String) -> Unit,
    monthFilter: String,
    onMonthChange: (String) -> Unit,
    months: List<String>,
    onRefresh: () -> Unit
) {
    var showMonthDropdown by remember { mutableStateOf(false) }
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = Color.White,
        containerColor = Color.White,
        labelColor = Color.Gray
    )

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(selected = sectorFilter == "T.I Interno", onClick = { onSectorChange(if (sectorFilter == "T.I Interno") "Todos" else "T.I Interno") }, label = { Text("T.I", fontSize = 11.sp) }, colors = chipColors)
        FilterChip(selected = sectorFilter == "Gobah", onClick = { onSectorChange(if (sectorFilter == "Gobah") "Todos" else "Gobah") }, label = { Text("Gobah", fontSize = 11.sp) }, colors = chipColors)
        Box {
            FilterChip(selected = monthFilter != "Geral", onClick = { showMonthDropdown = true }, label = { Text(monthFilter, fontSize = 11.sp) }, colors = chipColors, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) })
            DropdownMenu(expanded = showMonthDropdown, onDismissRequest = { showMonthDropdown = false }, modifier = Modifier.background(Color.White)) {
                months.forEach { month -> DropdownMenuItem(text = { Text(month) }, onClick = { onMonthChange(month); showMonthDropdown = false }) }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = { onStatusChange(if (statusFilter == "Pendentes") "Resolvidos" else "Pendentes"); onRefresh() }) {
            Text(statusFilter.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun AdminTicketItemSmall(ticket: Ticket, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ticket.userAvatarUrl,
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(CorporateIcons.User)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(ticket.userName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            com.example.pxrioverde.ui.admin.components.TicketStatusBadge(ticket.status)
        }
    }
}

@Composable
fun CarManagementSection(isDesktop: Boolean = false) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    val cars = remember { mutableStateListOf<Car>() }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = supabase.from("cars").select().decodeList<Car>()
            cars.clear()
            cars.addAll(response)
        } catch (e: Exception) {
            errorMessage = "Não foi possível carregar os veículos: ${e.message ?: "erro desconhecido"}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gestão de Veículos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.car),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Veículo")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        }

        if (!isLoading && cars.isEmpty()) {
            AdminEmptyState(
                title = "Nenhum veículo",
                description = "Sua frota está vazia.",
                icon = painterResource(Res.drawable.car),
                actionLabel = "Adicionar Veículo",
                onActionClick = { showAddDialog = true }
            )
        } else {
            if (isDesktop) {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    gridItems(cars) { car ->
                        CarCard(car)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(cars) { car ->
                        CarCard(car)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCarDialog(
            isSaving = isSaving,
            onDismiss = { showAddDialog = false },
            onConfirm = { newCar ->
                scope.launch {
                    isSaving = true
                    errorMessage = null
                    try {
                        val savedCar = supabase.from("cars").insert(newCar) {
                            select()
                        }.decodeSingle<Car>()
                        cars.add(savedCar)
                        showAddDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Não foi possível salvar o veículo: ${e.message ?: "erro desconhecido"}"
                    } finally {
                        isSaving = false
                    }
                }
            }
        )
    }
}

@Composable
fun CarCard(car: Car) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(Res.drawable.car), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(car.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${car.model} • ${car.licensePlate.uppercase()}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Car) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Veículo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome (ex: Corolla)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving)
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Marca/Modelo") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving)
                OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isSaving)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Car(
                            id = null,
                            name = name.trim(),
                            model = model.trim(),
                            licensePlate = plate.trim().uppercase()
                        )
                    )
                },
                enabled = !isSaving && name.isNotBlank() && model.isNotBlank() && plate.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") } }
    )
}

@Composable
fun AdminTicketItem(ticket: Ticket, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        println("DEBUG: ADMIN_AVATAR - Ticket: ${ticket.title} | AvatarURL: ${ticket.userAvatarUrl}")
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(model = ticket.userAvatarUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.1f)), contentScale = ContentScale.Crop, placeholder = rememberVectorPainter(CorporateIcons.User))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ticket.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(text = "Usuário: ${ticket.userName}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    if (ticket.rating != null && ticket.rating > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        com.example.pxrioverde.ui.admin.components.TicketRatingBar(ticket.rating)
                    }
                }
                com.example.pxrioverde.ui.admin.components.TicketStatusBadge(ticket.status)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailView(
    user: User,
    ticket: Ticket,
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    isDesktop: Boolean = false
) {
    var commentText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        if (!isDesktop) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(CorporateIcons.Back, null) }
                Text("Detalhes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = ticket.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = ticket.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!ticket.imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showAttachmentDialog = true }, contentAlignment = Alignment.Center) {
                            AsyncImage(model = ticket.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }
            }

            Text(text = "Status:", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
                TicketStatus.entries.forEach { status -> StatusFilterChip(status = status, isSelected = ticket.status == status, onClick = { viewModel.updateStatus(status) }) }
            }

            Text(text = "Histórico", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            messages.forEach { message -> Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { AdminChatBubble(message) } }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Surface(color = Color.White, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp).windowInsetsPadding(WindowInsets.navigationBars), verticalAlignment = Alignment.CenterVertically) {
                TextField(value = commentText, onValueChange = { commentText = it }, modifier = Modifier.weight(1f), placeholder = { Text("Responder...") }, shape = RoundedCornerShape(24.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
                IconButton(onClick = { if (commentText.isNotBlank()) { viewModel.addComment(commentText, user.displayName, true); commentText = "" } }, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) { Icon(CorporateIcons.Send, null, tint = Color.White) }
            }
        }
    }

    if (showAttachmentDialog) {
        com.example.pxrioverde.ui.admin.components.AvatarFullscreenDialog(url = ticket.imageUrl!!, name = "Anexo", onDismiss = { showAttachmentDialog = false })
    }
}

@Composable
fun StatusFilterChip(status: TicketStatus, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.padding(end = 8.dp).clickable { onClick() }, shape = RoundedCornerShape(24.dp), color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray)) {
        Text(text = status.name.replace("_", " "), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (isSelected) Color.White else Color.Gray)
    }
}

@Composable
fun AdminChatBubble(message: TicketMessage) {
    val alignment = if (message.isAdmin) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isAdmin) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
    val contentColor = if (message.isAdmin) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
    val label = if (message.isAdmin) "Suporte TI (${message.authorName})" else message.authorName
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = bubbleColor, modifier = Modifier.widthIn(max = 290.dp)) {
            Text(text = message.text, modifier = Modifier.padding(12.dp), color = contentColor, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AbsenceManagementSection(
    selectedAbsence: ComunicadoState?,
    onSelectAbsence: (ComunicadoState?) -> Unit,
    viewModel: AbsenceViewModel,
    isDesktop: Boolean = false
) {
    val absences by viewModel.absences.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAbsences()
        viewModel.observeAbsences()
    }

    if (selectedAbsence == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Comunicados de Ausência",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            if (absences.isEmpty() && !isLoading) {
                AdminEmptyState(
                    title = "Nenhum comunicado",
                    description = "Ainda não há solicitações de ausência.",
                    icon = rememberVectorPainter(CorporateIcons.User),
                    actionLabel = "Atualizar",
                    onActionClick = { viewModel.loadAbsences() }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(absences) { absence ->
                        AbsenceItemCard(absence, onClick = { onSelectAbsence(absence) })
                    }
                }
            }
        }
    } else {
        com.example.pxrioverde.ui.hr.ComunicadoAusenciaScreen(
            state = selectedAbsence,
            onStateChange = { onSelectAbsence(it) },
            onSave = { updatedState ->
                updatedState.adminFeedback?.let { feedback ->
                    viewModel.updateFeedback(updatedState.id ?: "", feedback) {
                        onSelectAbsence(null)
                    }
                } ?: onSelectAbsence(null)
            },
            onBack = { onSelectAbsence(null) },
            supervisors = emptyList(), // Admin doesn't need to select an approver during review
            isReadOnly = true,
            isAdminMode = true
        )
    }
}

@Composable
fun AbsenceItemCard(absence: ComunicadoState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val avatarUrl = absence.userAvatarUrl
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar de ${absence.userName}",
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B4332).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        CorporateIcons.User, 
                        contentDescription = null, 
                        tint = Color(0xFF1B4332),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(absence.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("${absence.type.label} • ${absence.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            if (absence.adminFeedback != null) {
                Icon(Icons.Default.CheckCircle, "Avaliado", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            } else {
                Surface(
                    color = Color(0xFFFFA000).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "PENDENTE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseManagementSection(
    user: User,
    viewModel: PurchaseViewModel,
    isDesktop: Boolean = false
) {
    val requests by viewModel.userRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    var selectedRequest by remember { mutableStateOf<PurchaseRequest?>(null) }
    var showAll by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Solicitações de Compra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (showAll) "Exibindo Histórico Completo" else "Exibindo Apenas Pendentes",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "VER PENDENTES" else "VER HISTÓRICO", fontWeight = FontWeight.Bold)
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }

        val filteredRequests = if (showAll) requests else requests.filter { it.status == PurchaseStatus.PENDENTE }

        if (filteredRequests.isEmpty() && !isLoading) {
            AdminEmptyState(
                title = if (showAll) "Nenhuma solicitação" else "Tudo em dia",
                description = if (showAll) "Ainda não há pedidos no sistema." else "Não há novas solicitações para aprovar.",
                icon = rememberVectorPainter(if (showAll) CorporateIcons.Receipt else CorporateIcons.Check),
                actionLabel = "Atualizar",
                onActionClick = { viewModel.loadAllRequests() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRequests) { request ->
                    com.example.pxrioverde.ui.purchase.PurchaseRequestItemCard(request) {
                        selectedRequest = request
                    }
                }
            }
        }
    }

    if (selectedRequest != null) {
        com.example.pxrioverde.ui.purchase.PurchaseEvaluationDialog(
            request = selectedRequest!!,
            isUploading = isUploading,
            onDismiss = { selectedRequest = null },
            onConfirm = { status, comment, approvedValue ->
                viewModel.updateRequestStatus(
                    requestId = selectedRequest!!.id!!, 
                    status = status, 
                    comment = comment, 
                    approvedValue = approvedValue,
                    approverName = user.displayName
                ) {
                    selectedRequest = null
                    viewModel.loadAllRequests()
                }
            }
        )
    }
}
