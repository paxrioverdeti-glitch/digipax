package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.CorporateIcons
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCheckOutScreen(
    carName: String,
    capturedPhotos: List<ByteArray?>,
    isUploading: Boolean,
    onTakePhoto: (Int) -> Unit,
    onSubmit: (Double) -> Unit, // Renomeado de onStartTrip para ser genérico
    onBack: () -> Unit,
    isFinishing: Boolean = false // Novo parâmetro para diferenciar o contexto
) {
    var kmValue by remember { mutableStateOf("") }
    val photoLabels = listOf("Frente", "Traseira", "Lateral Esq.", "Lateral Dir.")
    val allPhotosCaptured = capturedPhotos.all { it != null }
    
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color(0xFFF5F7F6),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                        color = if (isFinishing) Color(0xFF1B4332) else Color(0xFF2D6A4F),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(CorporateIcons.Back, contentDescription = "Voltar", tint = Color.White)
                    }
                    Text(
                        if (isFinishing) "Finalizar: $carName" else "Check-in: $carName",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-24).dp) 
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de Quilometragem
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            if (isFinishing) "Fim de Viagem" else "Início de Viagem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4332)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (isFinishing) "Quilometragem Final" else "Quilometragem Atual",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = kmValue,
                            onValueChange = { if (it.all { char -> char.isDigit() }) kmValue = it },
                            placeholder = { Text(if (isFinishing) "Digite o KM Final" else "Digite o KM Inicial", fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(CorporateIcons.Monitor, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFF2D6A4F)
                            )
                        )
                    }
                }
                
                // Card de Fotos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Fotos de ${if (isFinishing) "Entrega" else "Check-in"} (4)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4332)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Carousel de fotos
                        val pagerState = rememberPagerState(pageCount = { 4 })
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F7F6))
                                    .clickable { onTakePhoto(pagerState.currentPage) },
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                pageSpacing = 12.dp
                            ) { page ->
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (capturedPhotos[page] != null) {
                                        AsyncImage(
                                            model = capturedPhotos[page],
                                            contentDescription = photoLabels[page],
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Label por cima da foto (Opcional, para clareza)
                                        Surface(
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text(
                                                photoLabels[page],
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = CorporateIcons.Camera,
                                                contentDescription = null,
                                                tint = Color(0xFF1B4332),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                photoLabels[page],
                                                fontWeight = FontWeight.Bold,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                "Toque para tirar foto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Indicadores de página
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(4) { iteration ->
                                    val isCaptured = capturedPhotos[iteration] != null
                                    val isCurrent = pagerState.currentPage == iteration
                                    
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(width = if (isCurrent) 24.dp else 8.dp, height = 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> if (isFinishing) Color(0xFF1B4332) else Color(0xFF2D6A4F)
                                                    isCaptured -> Color(0xFF81C784)
                                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                                }
                                            )
                                    )
                                }
                            }
                            
                            if (!allPhotosCaptured) {
                                Text(
                                    "Arraste para o lado para tirar as outras fotos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        val km = kmValue.toDoubleOrNull() ?: 0.0
                        onSubmit(km) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFinishing) Color(0xFF1B4332) else Color(0xFF2D6A4F),
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                        disabledContentColor = Color.Gray
                    ),
                    enabled = kmValue.isNotEmpty() && allPhotosCaptured && !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(if (isFinishing) "FINALIZAR VIAGEM" else "INICIAR VIAGEM", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("CANCELAR")
                }

                Spacer(modifier = Modifier.height(24.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
fun PhotoPlaceholder(
    label: String, 
    hasPhoto: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (hasPhoto) Color(0xFFE8F5E9) else Color(0xFFF5F7F6),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (hasPhoto) Color(0xFF2D6A4F) else Color.LightGray.copy(alpha = 0.5f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (hasPhoto) CorporateIcons.Support else CorporateIcons.Camera, // Usando Support como check por enquanto
                contentDescription = null,
                tint = if (hasPhoto) Color(0xFF2D6A4F) else Color(0xFF1B4332),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
                if (hasPhoto) "Foto capturada" else "Toque para tirar", 
                style = MaterialTheme.typography.labelSmall, 
                color = if (hasPhoto) Color(0xFF2D6A4F) else Color.Gray
            )
        }
    }
}
