package com.example.pxrioverde.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.LottieAnimation
import com.example.pxrioverde.ui.components.CorporateIcons
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun BookingSuccessScreen(
    onDismiss: () -> Unit,
    isEmergency: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animação de Sucesso
        Box(
            modifier = Modifier.heightIn(min = 200.dp, max = 320.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                resName = "carregamento",
                modifier = Modifier.size(320.dp)
            )
        }

        if (isEmergency) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CorporateIcons.Alert,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Viagem emergencial iniciada com sucesso. Boa viagem!",
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Texto de Sucesso
            Text(
                text = if (isEmergency) "Viagem Iniciada!" else "Agendamento Realizado!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1B4332), 
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEmergency) 
                    "Sua viagem foi registrada no sistema." 
                else 
                    "Sua reserva foi confirmada com sucesso.\nO veículo estará disponível no horário agendado.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botão Concluir
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B4332)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    "CONCLUIR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }

    // Auto-dismiss após um tempo
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }
}
