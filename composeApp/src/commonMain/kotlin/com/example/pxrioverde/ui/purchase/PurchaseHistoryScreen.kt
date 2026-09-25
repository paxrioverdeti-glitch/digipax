package com.example.pxrioverde.ui.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.PurchaseRequest
import com.example.pxrioverde.model.PurchaseStatus
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.viewmodel.PurchaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(
    userId: String,
    viewModel: PurchaseViewModel,
    onBack: () -> Unit
) {
    val requests by viewModel.userRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserRequests(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Compras", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(CorporateIcons.Back, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B4332))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F7F6))) {
            if (isLoading && requests.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF1B4332))
            } else if (requests.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(CorporateIcons.Receipt, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhuma solicitação encontrada", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        PurchaseRequestItemCard(request)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseRequestItemCard(request: PurchaseRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                StatusBadge(request.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Qtd: ${request.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(request.type.label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF1B4332))
            }

            if (request.status != PurchaseStatus.PENDENTE) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "RETORNO DA APROVAÇÃO:", 
                    style = MaterialTheme.typography.labelSmall, 
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                if (request.approvedValue != null) {
                    Text(
                        "Valor Aprovado: R$ ${request.approvedValue}", 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                
                if (!request.managerComment.isNullOrBlank()) {
                    Text(
                        request.managerComment, 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (!request.approverName.isNullOrBlank()) {
                    Text(
                        "Avaliado por: ${request.approverName}", 
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PurchaseStatus) {
    val color = when(status) {
        PurchaseStatus.PENDENTE -> Color(0xFFFFA000)
        PurchaseStatus.APROVADO, PurchaseStatus.COMPRADO -> Color(0xFF2E7D32)
        PurchaseStatus.REJEITADO -> Color(0xFFD32F2F)
        PurchaseStatus.PROGRAMADO -> Color(0xFF1976D2)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
