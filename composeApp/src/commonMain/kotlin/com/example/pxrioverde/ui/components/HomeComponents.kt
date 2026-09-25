package com.example.pxrioverde.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import com.example.pxrioverde.util.AdaptiveUtils
import com.example.pxrioverde.util.WindowSizeClass
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car
import pxrioverde.composeapp.generated.resources.megafone
import com.example.pxrioverde.model.User
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.viewmodel.ProfileViewModel
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.viewmodel.TripViewModel
import com.example.pxrioverde.viewmodel.PurchaseViewModel
import com.example.pxrioverde.service.NotificationService
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.util.DateUtils
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import androidx.compose.ui.layout.ContentScale

// Estrutura de dados para os itens da navegação
sealed class BottomNavIcon {
    data class Vector(val imageVector: ImageVector) : BottomNavIcon()
    data class Resource(val painter: androidx.compose.ui.graphics.painter.Painter) : BottomNavIcon()
}

data class BottomNavItem(
    val title: String,
    val icon: BottomNavIcon,
    val route: String
)

@Composable
fun SupportBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    userRole: com.example.pxrioverde.model.UserRole = com.example.pxrioverde.model.UserRole.CLIENT
) {
    val carIcon = painterResource(Res.drawable.car)
    val items = remember(userRole, carIcon) {
        mutableListOf(
            BottomNavItem("Início", BottomNavIcon.Vector(CorporateIcons.Home), "home"),
            BottomNavItem("Chamados", BottomNavIcon.Vector(CorporateIcons.Receipt), "tickets"),
            BottomNavItem("Viagens", BottomNavIcon.Resource(carIcon), "viagem")
        ).apply {
            if (userRole == com.example.pxrioverde.model.UserRole.ADMIN || 
                userRole == com.example.pxrioverde.model.UserRole.SUPERVISOR || 
                userRole == com.example.pxrioverde.model.UserRole.ENCARREGADO) {
                add(BottomNavItem("Aprovar", BottomNavIcon.Vector(CorporateIcons.Analytics), "approvals"))
            }
            add(BottomNavItem("Perfil", BottomNavIcon.Vector(CorporateIcons.User), "profile"))
        }
    }

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color(0xFFF5F7F6), // Fundo cinza super claro
        tonalElevation = 8.dp, // Dá uma leve sombra para separar do conteúdo da tela
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index
            
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    when (val icon = item.icon) {
                        is BottomNavIcon.Vector -> Icon(
                            imageVector = icon.imageVector,
                            contentDescription = item.title
                        )
                        is BottomNavIcon.Resource -> Icon(
                            painter = icon.painter,
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    // Cor do ícone e texto quando NÃO está selecionado (cinza discreto)
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray,

                    // Cor do ícone e texto quando ESTÁ selecionado (seu verde principal)
                    selectedIconColor = Color(0xFF1B4332),
                    selectedTextColor = Color(0xFF2D6A4F),

                    // A cor da "pílula" no fundo do ícone ativo (verde translúcido)
                    indicatorColor = Color(0xFF2D6A4F).copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.painter.Painter? = null,
    imageVector: ImageVector? = null,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        if (icon != null) {
            Icon(painter = icon, contentDescription = null, tint = Color(0xFF1B4332), modifier = Modifier.size(18.dp))
        } else if (imageVector != null) {
            Icon(imageVector = imageVector, contentDescription = null, tint = Color(0xFF1B4332), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = Color(0xFF1B4332),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

// --- TELA PRINCIPAL (END-TO-END) ---
@Composable
fun UserSupportDashboardScreen(
    user: User,
    onNavigateToTicketCreate: (String?) -> Unit = {},
    onNavigateToCarBooking: () -> Unit = {},
    onNavigateToComunicadoRH: () -> Unit = {},
    onNavigateToMural: () -> Unit = {},
    onNavigateToPurchaseRequest: () -> Unit = {},
    onNavigateToPurchaseHistory: () -> Unit = {},
    onNavigateToTickets: () -> Unit = {},
    onNavigateToTrips: () -> Unit = {},
    onSelectTicket: (Ticket) -> Unit = {},
    onNavigateToCheckOut: (Booking) -> Unit = {},
    profileViewModel: ProfileViewModel,
    ticketViewModel: TicketViewModel,
    tripViewModel: TripViewModel,
    purchaseViewModel: PurchaseViewModel,
    notificationService: NotificationService,
    hasPendingResponse: Boolean = false,
    onNotificationsClick: () -> Unit = {}
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthSizeClass = AdaptiveUtils.calculateWindowSizeClass(maxWidth)
        val isDesktop = widthSizeClass != WindowSizeClass.COMPACT
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F6))
                .verticalScroll(rememberScrollState())
        ) {
            if (!isDesktop) {
                SupportHeader(
                    user = user, 
                    profileViewModel = profileViewModel,
                    notificationService = notificationService,
                    onNotificationsClick = onNotificationsClick
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (isDesktop) {
                // LAYOUT DESKTOP: Organizado em colunas e grades
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Coluna Esquerda: Card de Ação + Mural + Categorias
                    Column(modifier = Modifier.weight(1.2f)) {
                        NewTicketActionCard(
                            onNavigateToTicketCreate = { onNavigateToTicketCreate(null) },
                            onNavigateToCarBooking = onNavigateToCarBooking,
                            onNavigateToComunicadoRH = onNavigateToComunicadoRH,
                            hasPendingResponse = hasPendingResponse,
                            isDesktop = true
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        MuralManagementCard(onNavigateToMural = onNavigateToMural, isDesktop = true)

                        QuickCategoriesSection(onNavigateToTicketCreate = onNavigateToTicketCreate, isDesktop = true)
                    }

                    // Coluna Direita: Chamados Recentes
                    Column(modifier = Modifier.weight(1f)) {
                        RecentTicketsSection(
                            viewModel = ticketViewModel,
                            onSeeAll = onNavigateToTickets,
                            onSelectTicket = onSelectTicket,
                            isDesktop = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Viagens Recentes em Largura Total no Desktop
                RecentTripsSection(
                    user = user,
                    viewModel = tripViewModel,
                    onSeeAll = onNavigateToTrips,
                    onNavigateToCheckOut = onNavigateToCheckOut,
                    isDesktop = true
                )
            } else {
                // LAYOUT MOBILE: Vertical
                NewTicketActionCard(
                    onNavigateToTicketCreate = { onNavigateToTicketCreate(null) },
                    onNavigateToCarBooking = onNavigateToCarBooking,
                    onNavigateToComunicadoRH = onNavigateToComunicadoRH,
                    hasPendingResponse = hasPendingResponse
                )

                MuralManagementCard(onNavigateToMural = onNavigateToMural)

                QuickCategoriesSection(onNavigateToTicketCreate = onNavigateToTicketCreate)
                RecentTicketsSection(
                    viewModel = ticketViewModel,
                    onSeeAll = onNavigateToTickets,
                    onSelectTicket = onSelectTicket
                )
                RecentTripsSection(
                    user = user,
                    viewModel = tripViewModel,
                    onSeeAll = onNavigateToTrips,
                    onNavigateToCheckOut = onNavigateToCheckOut
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ManagementActionsSection(
    onNavigateToPurchaseRequest: () -> Unit,
    onNavigateToPurchaseHistory: () -> Unit,
    isDesktop: Boolean = false
) {
    Column(modifier = Modifier.padding(horizontal = if (isDesktop) 0.dp else 24.dp).padding(bottom = if (isDesktop) 0.dp else 24.dp)) {
        Text(
            text = "CENTRAL DE GESTÃO",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1B4332).copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Solicitações de Compra",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gerencie pedidos de suprimentos e serviços.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onNavigateToPurchaseRequest,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(CorporateIcons.Receipt, null, tint = Color(0xFF1B4332), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("NOVO PEDIDO", color = Color(0xFF1B4332), fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                            
                            OutlinedButton(
                                onClick = onNavigateToPurchaseHistory,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("HISTÓRICO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = CorporateIcons.Receipt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 1. CABEÇALHO ---
@Composable
fun SupportHeader(
    user: User, 
    profileViewModel: ProfileViewModel,
    notificationService: NotificationService,
    onNotificationsClick: () -> Unit = {}
) {
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
    val unreadNotifs by notificationService.unreadCount.collectAsState()

    // Sincronizar URL inicial se o ViewModel estiver vazio
    LaunchedEffect(user.avatarUrl) {
        if (profileImageUrl.isBlank() && !user.avatarUrl.isNullOrBlank()) {
            profileViewModel.updateLocalPhoto(user.avatarUrl)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .padding(bottom = 48.dp), // Aumentado para chegar na metade do card sobreposto
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            val isValidUrl = !profileImageUrl.isNullOrBlank() && 
                           (profileImageUrl!!.startsWith("http://") || profileImageUrl!!.startsWith("https://"))

            if (isValidUrl) {
                println("DEBUG: Loading Profile Header Photo URL: $profileImageUrl")
                KamelImage(
                    resource = asyncPainterResource(profileImageUrl!!),
                    contentDescription = "Perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        }
                    },
                    onFailure = { error ->
                        println("DEBUG: Image Load Failure (SupportHeader): ${error.message}")
                        Icon(
                            imageVector = CorporateIcons.User,
                            contentDescription = "Perfil",
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                )
            } else {
                Icon(
                    imageVector = CorporateIcons.User,
                    contentDescription = "Perfil",
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Olá, ${user.displayName}!",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Portal Integrado Pax Rio Verde.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        NotificationBell(
            unreadCount = unreadNotifs,
            onClick = onNotificationsClick,
            tint = Color.White
        )
    }
}

// --- CARD ELEGANTE PARA O MURAL & IDEIAS ---
@Composable
fun MuralManagementCard(
    onNavigateToMural: () -> Unit,
    isDesktop: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(horizontal = if (isDesktop) 0.dp else 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "COMUNICADOS & IDEIAS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1B4332).copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mural Pax Rio Verde",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Fique por dentro de comunicados, eventos e compartilhe suas ideias com a equipe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToMural,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.megafone),
                                contentDescription = null,
                                tint = Color(0xFF1B4332),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("VER MURAL", color = Color(0xFF1B4332), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(Res.drawable.megafone),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 2. CHAMADA PRINCIPAL (CARD SOBREPOSTO) ---
@Composable
fun NewTicketActionCard(
    onNavigateToTicketCreate: () -> Unit = {},
    onNavigateToCarBooking: () -> Unit = {},
    onNavigateToComunicadoRH: () -> Unit = {},
    hasPendingResponse: Boolean = false,
    isDesktop: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isDesktop) 0.dp else 24.dp)
            .offset(y = if (isDesktop) 0.dp else (-30).dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "O que deseja fazer hoje?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opção Reservar
                ActionButton(
                    icon = painterResource(Res.drawable.car),
                    label = "Reservar",
                    onClick = onNavigateToCarBooking,
                    modifier = Modifier.weight(1f)
                )

                // Opção Abrir Chamado
                ActionButton(
                    imageVector = CorporateIcons.Receipt,
                    label = "Chamado",
                    onClick = onNavigateToTicketCreate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link elegante para RH
            TextButton(
                onClick = onNavigateToComunicadoRH,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = CorporateIcons.Exit,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (hasPendingResponse) "Verificar Resposta / Aprovação" else "Vai se ausentar? Clique aqui",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// --- 3. ACESSO RÁPIDO (CATEGORIAS) ---
@Composable
fun QuickCategoriesSection(onNavigateToTicketCreate: (String?) -> Unit, isDesktop: Boolean = false) {
    Column(modifier = Modifier.padding(horizontal = if (isDesktop) 0.dp else 24.dp)) {
        Text(
            text = "Atendimento Rápido",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (isDesktop) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { QuickCategoryButtonDesktop(icon = CorporateIcons.Monitor, label = "Equipamentos", onClick = { onNavigateToTicketCreate("Equipamentos") }) }
                item { QuickCategoryButtonDesktop(icon = CorporateIcons.Tickets, label = "Sistemas", onClick = { onNavigateToTicketCreate("Sistemas") }) }
                item { QuickCategoryButtonDesktop(icon = CorporateIcons.Wifi, label = "Rede", onClick = { onNavigateToTicketCreate("Rede") }) }
                item { QuickCategoryButtonDesktop(icon = CorporateIcons.Support, label = "Outros", onClick = { onNavigateToTicketCreate("Outros") }) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickCategoryButton(
                    icon = BottomNavIcon.Vector(CorporateIcons.Monitor),
                    label = "Equipamentos",
                    onClick = { onNavigateToTicketCreate("Equipamentos") }
                )
                QuickCategoryButton(
                    icon = BottomNavIcon.Vector(CorporateIcons.Tickets),
                    label = "Sistemas",
                    onClick = { onNavigateToTicketCreate("Sistemas") }
                )
                QuickCategoryButton(
                    icon = BottomNavIcon.Vector(CorporateIcons.Wifi),
                    label = "Rede",
                    onClick = { onNavigateToTicketCreate("Rede") }
                )
                QuickCategoryButton(
                    icon = BottomNavIcon.Vector(CorporateIcons.Support),
                    label = "Outros",
                    onClick = { onNavigateToTicketCreate("Outros") }
                )
            }
        }
    }
}

@Composable
fun QuickCategoryButtonDesktop(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        }
    }
}

@Composable
fun QuickCategoryButton(icon: BottomNavIcon, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            when (icon) {
                is BottomNavIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = label,
                    modifier = Modifier.padding(18.dp),
                    tint = Color(0xFF2D6A4F)
                )
                is BottomNavIcon.Resource -> Icon(
                    painter = icon.painter,
                    contentDescription = label,
                    modifier = Modifier.padding(18.dp),
                    tint = Color(0xFF2D6A4F)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- 4. LISTA DE CHAMADOS RECENTES ---
@Composable
fun RecentTicketsSection(
    viewModel: TicketViewModel,
    onSeeAll: () -> Unit,
    onSelectTicket: (Ticket) -> Unit,
    isDesktop: Boolean = false
) {
    val tickets by viewModel.tickets.collectAsState()
    val recentTickets = tickets.take(if (isDesktop) 5 else 3)

    Column(modifier = Modifier.padding(top = if (isDesktop) 0.dp else 32.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isDesktop) 0.dp else 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meus Chamados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            if (tickets.isNotEmpty()) {
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2D6A4F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSeeAll() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (tickets.isEmpty()) {
            EmptyStateCard(
                message = "Sua fila está vazia! Nenhuma pendência técnica por enquanto. 🚀",
                icon = CorporateIcons.Receipt
            )
        } else {
            recentTickets.forEach { ticket ->
                val statusColor = when (ticket.status) {
                    TicketStatus.NA_FILA -> Color(0xFFE07A5F)
                    TicketStatus.EM_ANALISE -> Color(0xFF3498DB)
                    TicketStatus.RESOLVIDO -> Color(0xFF2D6A4F)
                    else -> Color.Gray
                }

                TicketItemCard(
                    title = ticket.title,
                    id = "#${ticket.id?.takeLast(4) ?: "---"}",
                    status = when(ticket.status) {
                        TicketStatus.NA_FILA -> "NA FILA"
                        TicketStatus.EM_ANALISE -> "EM ANÁLISE"
                        TicketStatus.AGUARDANDO_PECA -> "AGUARDANDO PEÇA"
                        TicketStatus.EM_ATENDIMENTO -> "EM ATENDIMENTO"
                        TicketStatus.RESOLVIDO -> "RESOLVIDO"
                        TicketStatus.CANCELADO -> "CANCELADO"
                    },
                    statusColor = statusColor,
                    date = DateUtils.formatIsoDate(ticket.createdAt).split(" às").firstOrNull() ?: "",
                    isDesktop = isDesktop,
                    onClick = { 
                        viewModel.selectTicket(ticket)
                        onSelectTicket(ticket)
                    }
                )
            }
        }
    }
}

// --- 5. LISTA DE VIAGENS RECENTES ---
@Composable
fun RecentTripsSection(
    user: User,
    viewModel: TripViewModel,
    onSeeAll: () -> Unit,
    onNavigateToCheckOut: (Booking) -> Unit,
    isDesktop: Boolean = false
) {
    val bookings by viewModel.bookings.collectAsState()
    val recentBookings = bookings.take(if (isDesktop) 4 else 3)

    LaunchedEffect(user.id) {
        viewModel.loadBookings(user.id)
    }

    Column(modifier = Modifier.padding(top = 32.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isDesktop) 24.dp else 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minhas viagens",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            if (bookings.isNotEmpty()) {
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2D6A4F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSeeAll() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            EmptyStateCard(
                message = "O asfalto está te esperando! Nenhuma viagem registrada recentemente. 🛣️",
                icon = CorporateIcons.MapPin
            )
        } else {
            if (isDesktop) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    recentBookings.forEach { booking ->
                        Box(modifier = Modifier.weight(1f)) {
                            TripItemCardDesktop(booking, onNavigateToCheckOut)
                        }
                    }
                }
            } else {
                recentBookings.forEach { booking ->
                    val statusText = when (booking.status.uppercase()) {
                        "FINALIZADO" -> "Concluída"
                        "CANCELADO" -> "Cancelado"
                        "EM_ANDAMENTO", "EM_USO" -> "Em Uso"
                        else -> if (booking.isEmergency) "Emergência" else "Agendado"
                    }

                    val statusColor = when (booking.status.uppercase()) {
                        "FINALIZADO", "CANCELADO" -> Color.Gray
                        "EM_ANDAMENTO", "EM_USO" -> Color(0xFFE07A5F)
                        else -> if (booking.isEmergency) Color(0xFFE07A5F) else Color(0xFF2D6A4F)
                    }

                    TripItemCard(
                        destination = booking.destination ?: "Destino não informado",
                        date = booking.date,
                        status = statusText,
                        statusColor = statusColor,
                        onClick = {
                            if (booking.status != "finalizado" && booking.status != "cancelado") {
                                onNavigateToCheckOut(booking)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PurchasingAgentSection(
    viewModel: PurchaseViewModel,
    modifier: Modifier = Modifier,
    isDesktop: Boolean = false
) {
    val approvedRequests by viewModel.approvedRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    var requestToMark by remember { mutableStateOf<com.example.pxrioverde.model.PurchaseRequest?>(null) }

    val horizontalPadding = if (isDesktop) 24.dp else 16.dp

    LaunchedEffect(Unit) {
        viewModel.loadApprovedRequests()
    }

    if (requestToMark != null) {
        AlertDialog(
            onDismissRequest = { if (!isUploading) requestToMark = null },
            title = { Text("Finalizar Compra", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja confirmar que o item '${requestToMark!!.itemName}' foi comprado e o processo foi finalizado?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAsPurchased(requestToMark!!.id!!) {
                            requestToMark = null
                        }
                    },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("CONFIRMAR")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToMark = null }, enabled = !isUploading) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    Column(modifier = modifier) {
        // Título Estilizado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "CENTRAL DE COMPRAS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B4332).copy(alpha = 0.6f),
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Itens aprovados para aquisição",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B4332)
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF1B4332))
                } else {
                    Surface(
                        color = Color(0xFF1B4332).copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = CorporateIcons.Receipt, 
                            contentDescription = null, 
                            tint = Color(0xFF1B4332),
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                }
            }
        }
        
        if (approvedRequests.isEmpty() && !isLoading) {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                EmptyStateCard(
                    message = "Tudo em ordem por aqui! Nenhuma compra aguardando aquisição no momento. ✅",
                    icon = CorporateIcons.Check
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { approvedRequests.size })
            
            Column {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val request = approvedRequests[page]
                    PurchasingAgentCard(
                        request = request,
                        onMarkPurchased = { requestToMark = it }
                    )
                }
                
                if (approvedRequests.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        Modifier
                            .height(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(approvedRequests.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color(0xFF1B4332) else Color(0xFF1B4332).copy(alpha = 0.2f)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchasingAgentCard(
    request: com.example.pxrioverde.model.PurchaseRequest,
    onMarkPurchased: (com.example.pxrioverde.model.PurchaseRequest) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFD8F3DC) // Verde menta claro
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CorporateIcons.Check, 
                            contentDescription = null, 
                            tint = Color(0xFF1B4332), 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.itemName, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B4332),
                        lineHeight = 24.sp
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(CorporateIcons.User, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${request.requesterName} • ${request.department}", 
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Badge de "Aprovado por Natália"
                Surface(
                    color = Color(0xFF2D6A4F).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Aprovado por Natália",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1B4332),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Área de Valor e Ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF8F9FA), Color(0xFFF1F3F5))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "VALOR PARA COMPRA", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "R$ ${request.approvedValue ?: 0.0}", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D6A4F)
                    )
                }
                
                Button(
                    onClick = { onMarkPurchased(request) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B4332)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(CorporateIcons.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("COMPRADO", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
            
            if (!request.managerComment.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFBEB)) // Amarelo bem clarinho para nota
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(CorporateIcons.Support, null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = request.managerComment!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TripItemCardDesktop(booking: Booking, onNavigateToCheckOut: (Booking) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            if (booking.status != "finalizado" && booking.status != "cancelado") onNavigateToCheckOut(booking)
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = booking.destination ?: "Destino",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(text = booking.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Surface(
                color = if (booking.status == "finalizado") Color.Gray.copy(alpha = 0.1f) else Color(0xFF2D6A4F).copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = booking.status.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (booking.status == "finalizado") Color.Gray else Color(0xFF2D6A4F),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, icon: ImageVector? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TripItemCard(destination: String, date: String, status: String, statusColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = CorporateIcons.MapPin,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = " • ",
                        color = Color.Gray
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = CorporateIcons.ChevronRight,
                contentDescription = "Detalhes",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TicketItemCard(
    title: String,
    id: String,
    status: String,
    statusColor: Color,
    date: String = "",
    isDesktop: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isDesktop) 0.dp else 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de status
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = CorporateIcons.Receipt,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B4332),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = id,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = " • ",
                        color = Color.Gray
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (date.isNotBlank()) {
                        Text(
                            text = " • ",
                            color = Color.Gray
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Icon(
                imageVector = CorporateIcons.ChevronRight,
                contentDescription = "Detalhes",
                tint = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animação de balanço (wiggle) suave quando há notificações não lidas
    val rotation by infiniteTransition.animateFloat(
        initialValue = if (unreadCount > 0) -10f else 0f,
        targetValue = if (unreadCount > 0) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Animação de pulso para o badge
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (unreadCount > 0) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    ) {
                        Text(unreadCount.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = CorporateIcons.Bell,
                contentDescription = "Notificações",
                tint = tint,
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (unreadCount > 0) rotation else 0f
                }
            )
        }
    }
}
