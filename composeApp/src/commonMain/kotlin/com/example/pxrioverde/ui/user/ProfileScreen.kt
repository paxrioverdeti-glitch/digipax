package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.pxrioverde.model.User
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.viewmodel.ProfileViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    viewModel: ProfileViewModel,
    onPickPhoto: () -> Unit,
    onLogout: () -> Unit
) {
    val currentSector by viewModel.userSector.collectAsState()
    val currentMachine by viewModel.machineName.collectAsState()

    var machineName by remember(currentMachine) { mutableStateOf(currentMachine) }
    var userSector by remember(currentSector) { mutableStateOf(currentSector) }
    
    val isModified = machineName != currentMachine || userSector != currentSector
    
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val isUploadingPhoto by viewModel.isUploading.collectAsState()

    // Inicializar URL do ViewModel se o usuário já tiver uma e o ViewModel estiver vazio
    LaunchedEffect(user.avatarUrl) {
        if (profileImageUrl.isBlank() && !user.avatarUrl.isNullOrBlank()) {
            viewModel.updateLocalPhoto(user.avatarUrl)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F6))
                .verticalScroll(rememberScrollState())
        ) {
            // Header de Perfil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            onClick = onPickPhoto
                        ) {
                            if (isUploadingPhoto) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                }
                            } else {
                                val isValidUrl = !profileImageUrl.isNullOrBlank() && 
                                               (profileImageUrl!!.startsWith("http://") || profileImageUrl!!.startsWith("https://"))
                                
                                if (isValidUrl) {
                                    println("DEBUG: Loading Profile Screen Photo URL: $profileImageUrl")
                                    KamelImage(
                                        resource = asyncPainterResource(profileImageUrl!!),
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onLoading = {
                                            Box(contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                            }
                                        },
                                        onFailure = { error ->
                                            println("DEBUG: Image Load Failure (ProfileScreen): ${error.message}")
                                            Icon(
                                                imageVector = CorporateIcons.User,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.padding(20.dp)
                                            )
                                        }
                                    )
                                } else {
                                    Icon(
                                        imageVector = CorporateIcons.User,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        }
                        // Badge de Câmera
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            onClick = onPickPhoto
                        ) {
                            Icon(
                                imageVector = CorporateIcons.Camera,
                                contentDescription = "Trocar foto",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Informações da Conta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Nome (Login) - SOMENTE LEITURA
                        ReadOnlyField(label = "Nome / Login", value = user.displayName)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Setor - EDITÁVEL
                        OutlinedTextField(
                            value = userSector,
                            onValueChange = { userSector = it },
                            label = { Text("Setor Padrão") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(CorporateIcons.Home, contentDescription = null, modifier = Modifier.size(20.dp)) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(20.dp))

                        // Máquina - EDITÁVEL
                        OutlinedTextField(
                            value = machineName,
                            onValueChange = { machineName = it },
                            label = { Text("Máquina Padrão") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(CorporateIcons.Monitor, contentDescription = null, modifier = Modifier.size(20.dp)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botão Sair
                OutlinedButton(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Icon(CorporateIcons.Back, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SAIR DA CONTA", fontWeight = FontWeight.Bold)
                }

                if (showSuccessMessage) {
                    Text(
                        text = "Dados salvos com sucesso!",
                        color = Color(0xFF2D6A4F),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Bold
                    )
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(3000)
                        showSuccessMessage = false
                    }
                }
                
                // Espaço para o botão fixo não cobrir o conteúdo ao scrollar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = { Text("Sair da Conta") },
                text = { Text("Tem certeza que deseja encerrar sua sessão?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirm = false
                            onLogout()
                        }
                    ) {
                        Text("SAIR", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Botão Salvar Fixo no Rodapé (Removida a barra branca flutuante feia)
        if (isModified) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveProfileSettings(userSector, machineName)
                        showSuccessMessage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(CorporateIcons.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = CorporateIcons.User,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
