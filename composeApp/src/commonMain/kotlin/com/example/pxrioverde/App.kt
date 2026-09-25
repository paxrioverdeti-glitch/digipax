package com.example.pxrioverde

import com.example.pxrioverde.ui.user.ProfileScreenWrapper
import com.example.pxrioverde.viewmodel.ProfileViewModel


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.pxrioverde.util.CommonBackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.example.pxrioverde.model.User
import com.example.pxrioverde.model.UserRole
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.Booking
import com.example.pxrioverde.ui.user.UserPortal
import com.example.pxrioverde.viewmodel.TripViewModel
import com.example.pxrioverde.viewmodel.TicketViewModel
import com.example.pxrioverde.viewmodel.AnalyticsViewModel
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import com.example.pxrioverde.ui.ticket.TicketDetailScreen
import com.example.pxrioverde.viewmodel.AppScreen
import com.example.pxrioverde.viewmodel.MainViewModel
import com.example.pxrioverde.viewmodel.PurchaseViewModel
import com.example.pxrioverde.viewmodel.FeedViewModel
import com.example.pxrioverde.ui.feed.MuralScreen
import com.example.pxrioverde.ui.ticket.TicketCreateWrapper
import com.example.pxrioverde.ui.purchase.PurchaseRequestScreen
import com.example.pxrioverde.ui.purchase.PurchaseHistoryScreen
import com.example.pxrioverde.ui.admin.AdminDashboard
import com.example.pxrioverde.ui.user.CarBookingScreenWrapper
import com.example.pxrioverde.ui.user.VehiclePortal
import com.example.pxrioverde.ui.user.VehicleCheckOutWrapper
import com.example.pxrioverde.ui.user.UserSideBar
import com.example.pxrioverde.ui.components.CorporateIcons
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.pxrioverde.service.rememberHapticService
import com.example.pxrioverde.ui.components.UserSupportDashboardScreen
import com.example.pxrioverde.ui.components.SupportBottomNavigation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.pxrioverde.service.AuthService
import com.example.pxrioverde.service.NotificationService
import com.example.pxrioverde.viewmodel.*
import io.github.jan.supabase.auth.status.SessionStatus

import com.example.pxrioverde.ui.theme.PxrioverdeTheme
import kotlinx.coroutines.launch

import com.example.pxrioverde.ui.components.LottieAnimation
import com.example.pxrioverde.ui.components.GlassCard
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

import androidx.compose.animation.core.tween
import com.example.pxrioverde.util.AdaptiveUtils
import com.example.pxrioverde.util.WindowSizeClass
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car
import pxrioverde.composeapp.generated.resources.logo_login
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.input.ImeAction
import com.example.pxrioverde.util.LocalStorage
import com.example.pxrioverde.database.getDatabase
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.*
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.pxrioverde.ui.components.NotificationBell
import com.example.pxrioverde.ui.user.ApprovalCenterScreen
import com.example.pxrioverde.ui.user.UserAbsencePortal
import com.example.pxrioverde.ui.user.UserNotificationsScreen
import com.example.pxrioverde.viewmodel.AbsenceViewModel

import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import io.kamel.image.config.LocalKamelConfig

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    val kamelConfig = remember {
        KamelConfig {
            takeFrom(KamelConfig.Default)
            imageBitmapDecoder()
        }
    }

    CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
        PxrioverdeTheme {
            var showSplash by remember { mutableStateOf(true) }
            var initialUser by remember { mutableStateOf<User?>(null) }

            if (showSplash) {
                AnimatedSplashScreen(onAnimationFinished = { showSplash = false })
            } else {
                MainAppContent(initialUser = initialUser)
            }
        }
    }
}

@Composable
fun MainAppContent(initialUser: User? = null) {
    val authService: AuthService = koinInject()
    val sessionStatus by authService.sessionStatus.collectAsState()
    var currentUser by remember { mutableStateOf<User?>(initialUser) }

    val mainViewModel: MainViewModel = koinViewModel()
    val adminViewModel: AdminViewModel = koinViewModel()
    val currentTab by mainViewModel.currentTab.collectAsState()
    val currentScreen by mainViewModel.currentScreen.collectAsState()
    val userSector by mainViewModel.userSector.collectAsState()
    val machineName by mainViewModel.machineName.collectAsState()
    
    val hapticService = rememberHapticService()
    val scope = rememberCoroutineScope()
    
    // Injetando ViewModels via Koin
    val ticketViewModel: TicketViewModel = koinViewModel()
    val tripViewModel: TripViewModel = koinViewModel()
    val analyticsViewModel: AnalyticsViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val absenceViewModel: AbsenceViewModel = koinViewModel()
    val purchaseViewModel: PurchaseViewModel = koinViewModel()
    val feedViewModel: FeedViewModel = koinViewModel()
    val notificationService: NotificationService = koinInject()

    // Sincroniza o usuário local e recarrega dados em mudanças de sessão (incluindo refresh de token)
    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            val user = authService.getCurrentUser()
            if (user != null) {
                currentUser = user
                
                // Inicia a observação contínua do banco de dados local
                ticketViewModel.observeTickets()
                
                // Dispara o carregamento/recarga de dados
                ticketViewModel.loadTickets(user.id)
                tripViewModel.loadBookings(user.id)
                notificationService.subscribeAll(user)
            }
        } else if (sessionStatus is SessionStatus.NotAuthenticated) {
            currentUser = null
            mainViewModel.clearData()
            adminViewModel.clearData()
        }
    }

    CommonBackHandler(enabled = currentScreen != AppScreen.Main) {
        mainViewModel.goBack()
    }

    if (currentUser == null) {
        LoginScreen(onLogin = { user -> 
            currentUser = user
        })
    } else {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)) togetherWith
                fadeOut(animationSpec = tween(300))
            }
        ) { targetScreen ->
            when (targetScreen) {
                is AppScreen.TicketCreate -> TicketCreateWrapper(
                    user = currentUser!!,
                    viewModel = ticketViewModel,
                    onBack = { mainViewModel.goBack() },
                    preselectedSector = targetScreen.preselectedSector,
                    defaultSector = userSector,
                    defaultComputer = machineName
                )
                is AppScreen.VehicleCheckOut -> {
                    VehicleCheckOutWrapper(
                        booking = targetScreen.booking,
                        viewModel = tripViewModel,
                        onBack = { mainViewModel.goBack() },
                        onSuccess = { 
                            mainViewModel.goBack()
                            mainViewModel.setTab(0)
                        }
                    )
                }
                is AppScreen.CarBooking -> CarBookingScreenWrapper(
                    user = currentUser!!,
                    viewModel = tripViewModel,
                    onBack = { mainViewModel.goBack() },
                    defaultDepartment = userSector
                )
                is AppScreen.ComunicadoRH -> UserAbsencePortal(
                    userId = currentUser!!.id,
                    userName = currentUser!!.displayName,
                    avatarUrl = currentUser!!.avatarUrl,
                    sector = userSector,
                    viewModel = absenceViewModel,
                    onBack = { mainViewModel.goBack() }
                )
                is AppScreen.PurchaseRequest -> PurchaseRequestScreen(
                    user = currentUser!!,
                    sector = userSector,
                    onSave = { request ->
                        purchaseViewModel.submitRequest(request) {
                            mainViewModel.goBack()
                        }
                    },
                    onBack = { mainViewModel.goBack() },
                    isUploading = purchaseViewModel.isUploading.collectAsState().value
                )
                is AppScreen.PurchaseHistory -> PurchaseHistoryScreen(
                    userId = currentUser!!.id,
                    viewModel = purchaseViewModel,
                    onBack = { mainViewModel.goBack() }
                )
                is AppScreen.Mural -> MuralScreen(
                    user = currentUser!!,
                    viewModel = feedViewModel,
                    onBack = { mainViewModel.goBack() }
                )
                else -> MainContainer(
                    user = currentUser!!,
                    currentTab = currentTab,
                    onTabChange = { 
                        mainViewModel.setTab(it)
                        hapticService.success()
                    },
                    ticketViewModel = ticketViewModel,
                    tripViewModel = tripViewModel,
                    analyticsViewModel = analyticsViewModel,
                    profileViewModel = profileViewModel,
                    absenceViewModel = absenceViewModel,
                    adminViewModel = adminViewModel,
                    purchaseViewModel = purchaseViewModel,
                    notificationService = notificationService,
                    onNavigateToTicketCreate = { sector -> 
                        mainViewModel.navigateTo(AppScreen.TicketCreate(sector))
                    },
                    onNavigateToCarBooking = { mainViewModel.navigateTo(AppScreen.CarBooking) },
                    onNavigateToComunicadoRH = { mainViewModel.navigateTo(AppScreen.ComunicadoRH) },
                    onNavigateToMural = { mainViewModel.navigateTo(AppScreen.Mural) },
                    onNavigateToCheckOut = { booking ->
                        mainViewModel.navigateTo(AppScreen.VehicleCheckOut(booking))
                    },
                    onNavigateToPurchaseRequest = { mainViewModel.navigateTo(AppScreen.PurchaseRequest) },
                    onNavigateToPurchaseHistory = { mainViewModel.navigateTo(AppScreen.PurchaseHistory) },
                    onLogout = {
                        scope.launch {
                            // 1. Logout no Supabase (limpa sessão)
                            authService.logout()
                            
                            // 2. Limpa o banco de dados local (Room) através dos DAOs
                            try {
                                val db = getDatabase()
                                db.ticketDao().clearAll()
                                db.messageDao().clearAll()
                                db.tripDao().clearAll()
                            } catch (e: Exception) {
                                println("Erro ao limpar banco: ${e.message}")
                            }

                            // 3. Reseta o estado dos ViewModels para não sobrar dados em memória
                            ticketViewModel.clearData()
                            tripViewModel.clearData()
                            analyticsViewModel.clearData()
                            profileViewModel.clearData()
                            absenceViewModel.clearData()
                            purchaseViewModel.clearData()
                            mainViewModel.clearData()
                            adminViewModel.clearData()

                            // 4. Limpa preferências locais (configurações, cache de nomes, etc)
                            LocalStorage.clear()
                            
                            // 5. Volta para tela de login
                            currentUser = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLogin: (User) -> Unit) {
    val authService: AuthService = koinInject()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(LocalStorage.getBoolean("rememberMe", true)) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (rememberMe) {
            email = LocalStorage.getString("savedEmail", "")
            password = LocalStorage.getString("savedPassword", "")
        }
    }

    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Camada 1: Background Estático Fullscreen
        Image(
            painter = painterResource(Res.drawable.logo_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Camada 2: Conteúdo Inicial (Botão + Atalhos)
        AnimatedVisibility(
            visible = !showSheet,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 24.dp, end = 24.dp, bottom = 80.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Botão de Entrada Premium (Gradient)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1B4332), 
                                    Color(0xFF2D6A4F)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSheet = true 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ENTRAR NO SISTEMA",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // ATALHOS PREMIUM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoginShortcutItem("Frota", painterResource(Res.drawable.car))
                    LoginShortcutItem("Chamados", rememberVectorPainter(CorporateIcons.Support))
                    LoginShortcutItem("Ausência", rememberVectorPainter(CorporateIcons.Calendar))
                }
            }
        }

        // Camada 3: Overlay clicável para fechar o Bottom Sheet
        if (showSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSheet = false }
            )
        }

        // Camada 4: Bottom Sheet de Login (Formulário)
        AnimatedVisibility(
            visible = showSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(), // Faz o card subir com o teclado
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.Transparent
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Handle visual
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 20.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        )
                        
                        Text(
                            "Bem-vindo",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Identifique-se para continuar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Campo de E-mail
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            placeholder = { Text("E-mail corporativo", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = CorporateIcons.Home, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Senha
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            placeholder = { Text("Sua senha", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        isLoading = true
                                        scope.launch {
                                            try {
                                                LocalStorage.putBoolean("rememberMe", rememberMe)
                                                if (rememberMe) {
                                                    LocalStorage.putString("savedEmail", email)
                                                    LocalStorage.putString("savedPassword", password)
                                                } else {
                                                    LocalStorage.putString("savedEmail", "")
                                                    LocalStorage.putString("savedPassword", "")
                                                }
                                                val user = authService.login(email, password)
                                                if (user != null) onLogin(user) else errorMessage = "E-mail ou senha incorretos."
                                            } catch (e: Exception) {
                                                errorMessage = e.message ?: "Erro ao realizar login."
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = CorporateIcons.Support, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) CorporateIcons.VisibilityOff else CorporateIcons.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                "Manter conectado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        if (errorMessage != null) {
                            Surface(
                                modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isLoading = true
                                scope.launch {
                                    try {
                                        LocalStorage.putBoolean("rememberMe", rememberMe)
                                        if (rememberMe) {
                                            LocalStorage.putString("savedEmail", email)
                                            LocalStorage.putString("savedPassword", password)
                                        } else {
                                            LocalStorage.putString("savedEmail", "")
                                            LocalStorage.putString("savedPassword", "")
                                        }
                                        val user = authService.login(email, password)
                                        if (user != null) {
                                            onLogin(user)
                                        } else {
                                            errorMessage = "E-mail ou senha incorretos."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Erro ao realizar login."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                                disabledContentColor = Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    "ENTRAR",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Autenticando...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "DESENVOLVIMENTO T.I • v2.4.0",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginShortcutItem(label: String, icon: Painter) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = Color(0xFF1B4332), // Verde Pax Premium
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    user: User, 
    currentTab: Int, 
    onTabChange: (Int) -> Unit, 
    ticketViewModel: TicketViewModel,
    tripViewModel: TripViewModel,
    analyticsViewModel: AnalyticsViewModel,
    profileViewModel: ProfileViewModel,
    absenceViewModel: AbsenceViewModel,
    adminViewModel: AdminViewModel,
    purchaseViewModel: PurchaseViewModel,
    notificationService: NotificationService,
    onNavigateToTicketCreate: (String?) -> Unit = {},
    onNavigateToCarBooking: () -> Unit = {},
    onNavigateToComunicadoRH: () -> Unit = {},
    onNavigateToMural: () -> Unit = {},
    onNavigateToPurchaseRequest: () -> Unit = {},
    onNavigateToPurchaseHistory: () -> Unit = {},
    onNavigateToCheckOut: (Booking) -> Unit = {},
    onLogout: () -> Unit
) {
    var showTicketDetail by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    val selectedAbsence by absenceViewModel.selectedAbsence.collectAsState()

    CommonBackHandler(enabled = user.role != UserRole.ADMIN && (showTicketDetail || selectedAbsence != null || showNotifications || currentTab != 0)) {
        if (showNotifications) {
            showNotifications = false
        } else if (showTicketDetail) {
            showTicketDetail = false
        } else if (selectedAbsence != null) {
            absenceViewModel.selectAbsence(null)
        } else {
            onTabChange(0)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthSizeClass = AdaptiveUtils.calculateWindowSizeClass(maxWidth)
        val isDesktop = widthSizeClass != WindowSizeClass.COMPACT
        
        if (user.role == UserRole.ADMIN) {
            AdminView(
                user = user, 
                viewModel = ticketViewModel,
                tripViewModel = tripViewModel,
                analyticsViewModel = analyticsViewModel,
                notificationService = notificationService,
                absenceViewModel = absenceViewModel,
                adminViewModel = adminViewModel,
                onLogout = onLogout
            )
        } else if (isDesktop) {
            // LAYOUT DESKTOP PARA USUÁRIO COMUM
            Row(modifier = Modifier.fillMaxSize()) {
                UserSideBar(
                    selectedSection = currentTab,
                    onSectionSelected = { 
                        onTabChange(it)
                        showNotifications = false
                        showTicketDetail = false
                    },
                    modifier = Modifier.width(85.dp)
                )
                
                Column(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.background)) {
                    // Header Superior Simplificado para Usuário Desktop
                    Surface(color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                when(currentTab) {
                                    0 -> "Início"
                                    1 -> "Meus Chamados"
                                    2 -> "Meus Agendamentos"
                                    3 -> "Meu Perfil"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B4332)
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val unreadNotifs by notificationService.unreadCount.collectAsState()
                                NotificationBell(
                                    unreadCount = unreadNotifs,
                                    onClick = { showNotifications = true },
                                    tint = Color(0xFF1B4332)
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable { onTabChange(3) },
                                    color = Color(0xFF1B4332).copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(CorporateIcons.User, null, tint = Color(0xFF1B4332), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        UserContentArea(
                            user = user,
                            currentTab = currentTab,
                            showNotifications = showNotifications,
                            showTicketDetail = showTicketDetail,
                            ticketViewModel = ticketViewModel,
                            tripViewModel = tripViewModel,
                            profileViewModel = profileViewModel,
                            absenceViewModel = absenceViewModel,
                            purchaseViewModel = purchaseViewModel,
                            notificationService = notificationService,
                            onNavigateToTicketCreate = onNavigateToTicketCreate,
                            onNavigateToCarBooking = onNavigateToCarBooking,
                            onNavigateToComunicadoRH = onNavigateToComunicadoRH,
                            onNavigateToMural = onNavigateToMural,
                            onNavigateToPurchaseRequest = onNavigateToPurchaseRequest,
                            onNavigateToPurchaseHistory = onNavigateToPurchaseHistory,
                            onNavigateToTickets = { onTabChange(1) },
                            onNavigateToTrips = { onTabChange(2) },
                            onSelectTicket = { showTicketDetail = true },
                            onNavigateToCheckOut = onNavigateToCheckOut,
                            onLogout = onLogout,
                            onCloseNotifications = { showNotifications = false },
                            onCloseTicketDetail = { showTicketDetail = false },
                            isDesktop = true
                        )
                    }
                }
            }
        } else {
            // LAYOUT MOBILE COM BOTTOM NAVIGATION
            val selectedAbsence by absenceViewModel.selectedAbsence.collectAsState()
            
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    // Oculta a barra de navegação se estiver visualizando um detalhe (Ticket ou Ausência)
                    if (!showTicketDetail && selectedAbsence == null) {
                        SupportBottomNavigation(
                            selectedItem = currentTab,
                            onItemSelected = onTabChange,
                            userRole = user.role
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    UserContentArea(
                        user = user,
                        currentTab = currentTab,
                        showNotifications = showNotifications,
                        showTicketDetail = showTicketDetail,
                        ticketViewModel = ticketViewModel,
                        tripViewModel = tripViewModel,
                        profileViewModel = profileViewModel,
                        absenceViewModel = absenceViewModel,
                        purchaseViewModel = purchaseViewModel,
                        notificationService = notificationService,
                        onNavigateToTicketCreate = onNavigateToTicketCreate,
                        onNavigateToCarBooking = onNavigateToCarBooking,
                        onNavigateToComunicadoRH = onNavigateToComunicadoRH,
                        onNavigateToMural = onNavigateToMural,
                        onNavigateToPurchaseRequest = onNavigateToPurchaseRequest,
                        onNavigateToPurchaseHistory = onNavigateToPurchaseHistory,
                        onNavigateToTickets = { onTabChange(1) },
                        onNavigateToTrips = { onTabChange(2) },
                        onSelectTicket = { showTicketDetail = true },
                        onNavigateToCheckOut = onNavigateToCheckOut,
                        onLogout = onLogout,
                        onCloseNotifications = { showNotifications = false },
                        onCloseTicketDetail = { showTicketDetail = false },
                        isDesktop = false
                    )
                }
            }
        }
    }
}

@Composable
fun UserContentArea(
    user: User,
    currentTab: Int,
    showNotifications: Boolean,
    showTicketDetail: Boolean,
    ticketViewModel: TicketViewModel,
    tripViewModel: TripViewModel,
    profileViewModel: ProfileViewModel,
    absenceViewModel: AbsenceViewModel,
    purchaseViewModel: PurchaseViewModel,
    notificationService: NotificationService,
    onNavigateToTicketCreate: (String?) -> Unit,
    onNavigateToCarBooking: () -> Unit,
    onNavigateToComunicadoRH: () -> Unit,
    onNavigateToMural: () -> Unit = {},
    onNavigateToPurchaseRequest: () -> Unit,
    onNavigateToPurchaseHistory: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onSelectTicket: (Ticket) -> Unit,
    onNavigateToCheckOut: (Booking) -> Unit,
    onLogout: () -> Unit,
    onCloseNotifications: () -> Unit,
    onCloseTicketDetail: () -> Unit,
    isDesktop: Boolean
) {
    if (showNotifications) {
        UserNotificationsScreen(
            notificationService = notificationService,
            onBack = onCloseNotifications
        )
    } else if (showTicketDetail) {
        TicketDetailScreen(
            viewModel = ticketViewModel,
            userName = user.displayName,
            onBack = onCloseTicketDetail
        )
    } else {
        val hasApprovalTab = user.role == UserRole.ADMIN || user.role == UserRole.SUPERVISOR || user.role == UserRole.ENCARREGADO
        
        when (currentTab) {
            0 -> UserHome(
                user = user,
                onNavigateToTicketCreate = onNavigateToTicketCreate,
                onNavigateToCarBooking = onNavigateToCarBooking,
                onNavigateToComunicadoRH = onNavigateToComunicadoRH,
                onNavigateToMural = onNavigateToMural,
                onNavigateToPurchaseRequest = onNavigateToPurchaseRequest,
                onNavigateToPurchaseHistory = onNavigateToPurchaseHistory,
                onNavigateToTickets = onNavigateToTickets,
                onNavigateToTrips = onNavigateToTrips,
                onSelectTicket = onSelectTicket,
                onNavigateToCheckOut = onNavigateToCheckOut,
                profileViewModel = profileViewModel,
                ticketViewModel = ticketViewModel,
                tripViewModel = tripViewModel,
                absenceViewModel = absenceViewModel,
                purchaseViewModel = purchaseViewModel,
                notificationService = notificationService,
                onNavigateToNotifications = { /* No Desktop já é gerenciado */ }
            )
            1 -> UserPortal(
                user = user,
                viewModel = ticketViewModel,
                onSelectTicket = onSelectTicket,
                onNavigateToTicketCreate = onNavigateToTicketCreate,
                isDesktop = isDesktop
            )
            2 -> VehiclePortal(
                user = user,
                viewModel = tripViewModel,
                onNavigateToCheckOut = onNavigateToCheckOut,
                onNavigateToReserve = onNavigateToCarBooking,
                isDesktop = isDesktop
            )
            3 -> {
                if (hasApprovalTab) {
                    ApprovalCenterScreen(
                        absenceViewModel = absenceViewModel,
                        user = user
                    )
                } else {
                    ProfileScreenWrapper(
                        user = user, 
                        viewModel = profileViewModel,
                        onLogout = onLogout
                    )
                }
            }
            4 -> {
                if (hasApprovalTab) {
                    ProfileScreenWrapper(
                        user = user, 
                        viewModel = profileViewModel,
                        onLogout = onLogout
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }
            else -> Box(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun UserHome(
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
    absenceViewModel: AbsenceViewModel,
    purchaseViewModel: PurchaseViewModel,
    notificationService: NotificationService,
    onNavigateToNotifications: () -> Unit = {}
) {
    val userAbsences by absenceViewModel.userAbsences.collectAsState()
    val lastViewedId by absenceViewModel.lastViewedAbsenceId.collectAsState()
    
    // Consideramos que há resposta pendente se o último comunicado tiver adminFeedback preenchido
    // E se o ID desse comunicado for diferente do último que o usuário visualizou
    val hasPendingResponse = remember(userAbsences, lastViewedId) {
        val latestResponded = userAbsences.firstOrNull { it.adminFeedback != null }
        latestResponded != null && latestResponded.id != lastViewedId
    }

    LaunchedEffect(user.id) {
        absenceViewModel.loadUserAbsences(user.id)
    }

    UserSupportDashboardScreen(
        user = user,
        onNavigateToTicketCreate = onNavigateToTicketCreate,
        onNavigateToCarBooking = onNavigateToCarBooking,
        onNavigateToComunicadoRH = onNavigateToComunicadoRH,
        onNavigateToMural = onNavigateToMural,
        onNavigateToPurchaseRequest = onNavigateToPurchaseRequest,
        onNavigateToPurchaseHistory = onNavigateToPurchaseHistory,
        onNavigateToTickets = onNavigateToTickets,
        onNavigateToTrips = onNavigateToTrips,
        onSelectTicket = onSelectTicket,
        onNavigateToCheckOut = onNavigateToCheckOut,
        profileViewModel = profileViewModel,
        ticketViewModel = ticketViewModel,
        tripViewModel = tripViewModel,
        notificationService = notificationService,
        hasPendingResponse = hasPendingResponse,
        onNotificationsClick = onNavigateToNotifications,
        purchaseViewModel = purchaseViewModel
    )
}

@Composable
fun AdminView(
    user: User, 
    viewModel: TicketViewModel,
    tripViewModel: TripViewModel,
    analyticsViewModel: AnalyticsViewModel,
    notificationService: NotificationService,
    absenceViewModel: AbsenceViewModel,
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    AdminDashboard(
        user = user, 
        viewModel = viewModel,
        tripViewModel = tripViewModel,
        analyticsViewModel = analyticsViewModel,
        notificationService = notificationService,
        onLogout = onLogout,
        absenceViewModel = absenceViewModel,
        adminViewModel = adminViewModel
    )
}

@Composable
fun AnimatedSplashScreen(onAnimationFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000) // Duração da animação
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            resName = "splash_animation",
            modifier = Modifier.fillMaxSize()
        )
    }
}
