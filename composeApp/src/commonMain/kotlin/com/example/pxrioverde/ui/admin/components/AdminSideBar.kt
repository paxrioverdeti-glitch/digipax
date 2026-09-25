package com.example.pxrioverde.ui.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.CorporateIcons
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car

@Composable
fun AdminSideBar(
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color(0xFF1B4332), // Seu verde escuro característico
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                // Espaço para um Logo ou Ícone Principal do App
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CorporateIcons.Support,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "PXR",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        contentColor = Color.White
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AdminNavItem(
            selected = selectedSection == 0,
            onClick = { onSectionSelected(0) },
            icon = CorporateIcons.Tickets,
            label = "Chamados"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdminNavItem(
            selected = selectedSection == 1,
            onClick = { onSectionSelected(1) },
            icon = null, // Usaremos o painterResource abaixo
            label = "Veículos",
            customIcon = {
                Icon(
                    painter = painterResource(Res.drawable.car),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdminNavItem(
            selected = selectedSection == 2,
            onClick = { onSectionSelected(2) },
            icon = CorporateIcons.Analytics,
            label = "Métricas"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdminNavItem(
            selected = selectedSection == 3,
            onClick = { onSectionSelected(3) },
            icon = CorporateIcons.User,
            label = "Ausências"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdminNavItem(
            selected = selectedSection == 5,
            onClick = { onSectionSelected(5) },
            icon = CorporateIcons.Mural,
            label = "Mural"
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AdminNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    label: String,
    customIcon: @Composable (() -> Unit)? = null
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
            }
        },
        label = {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = Color(0xFF1B4332),
            selectedTextColor = Color.White,
            indicatorColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f)
        )
    )
}
