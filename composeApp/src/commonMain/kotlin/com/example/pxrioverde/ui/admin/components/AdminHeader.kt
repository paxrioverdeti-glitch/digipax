package com.example.pxrioverde.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.service.NotificationService
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car

@Composable
fun AdminHeader(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    notificationService: NotificationService,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    isDesktop: Boolean = false
) {
    val unreadCount by notificationService.unreadCount.collectAsState()

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = if (!isDesktop) Modifier.windowInsetsPadding(WindowInsets.statusBars) else Modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = if (isDesktop) 12.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Seção Esquerda: Título (Mobile) ou Seção Atual (Desktop)
                if (!isDesktop) {
                    Text(
                        "PXR Admin",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B4332)
                    )
                } else {
                    val sectionTitle = when(selectedTabIndex) {
                        0 -> "Gestão de Chamados"
                        1 -> "Controle de Frota"
                        2 -> "Análise de Dados"
                        3 -> "Gestão de Ausências"
                        5 -> "Mural e Ideias"
                        else -> "Painel Administrativo"
                    }
                    Text(
                        sectionTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B4332)
                    )
                }

                // Seção Direita: Notificações e Perfil
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = CorporateIcons.Notifications,
                                contentDescription = "Notificações",
                                tint = Color(0xFF1B4332)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() },
                        color = Color(0xFF1B4332).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = CorporateIcons.User,
                                contentDescription = "Perfil",
                                tint = Color(0xFF1B4332),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // No mobile as abas foram removidas daqui para irem para o BottomNavigation
        }
    }
}
