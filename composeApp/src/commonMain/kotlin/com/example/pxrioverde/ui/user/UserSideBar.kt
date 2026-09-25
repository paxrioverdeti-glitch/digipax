package com.example.pxrioverde.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.ui.components.CorporateIcons
import org.jetbrains.compose.resources.painterResource
import pxrioverde.composeapp.generated.resources.Res
import pxrioverde.composeapp.generated.resources.car

import com.example.pxrioverde.model.UserRole

@Composable
fun UserSideBar(
    selectedSection: Int,
    onSectionSelected: (Int) -> Unit,
    userRole: UserRole = UserRole.CLIENT,
    modifier: Modifier = Modifier
) {
    val hasApprovalTab = userRole == UserRole.ADMIN || userRole == UserRole.SUPERVISOR || userRole == UserRole.ENCARREGADO

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = Color(0xFF1B4332), // Verde Pax
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = CorporateIcons.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        contentColor = Color.White
    ) {
        Spacer(modifier = Modifier.weight(1f))

        UserNavItem(
            selected = selectedSection == 0,
            onClick = { onSectionSelected(0) },
            icon = CorporateIcons.Home,
            label = "Início"
        )

        Spacer(modifier = Modifier.height(16.dp))

        UserNavItem(
            selected = selectedSection == 1,
            onClick = { onSectionSelected(1) },
            icon = CorporateIcons.Receipt,
            label = "Chamados"
        )

        Spacer(modifier = Modifier.height(16.dp))

        UserNavItem(
            selected = selectedSection == 2,
            onClick = { onSectionSelected(2) },
            icon = null,
            label = "Viagens",
            customIcon = {
                Icon(
                    painter = painterResource(Res.drawable.car),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        if (hasApprovalTab) {
            Spacer(modifier = Modifier.height(16.dp))

            UserNavItem(
                selected = selectedSection == 3,
                onClick = { onSectionSelected(3) },
                icon = CorporateIcons.Check,
                label = "Aprovar"
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            UserNavItem(
                selected = selectedSection == 4,
                onClick = { onSectionSelected(4) },
                icon = CorporateIcons.User,
                label = "Perfil"
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            UserNavItem(
                selected = selectedSection == 3,
                onClick = { onSectionSelected(3) },
                icon = CorporateIcons.User,
                label = "Perfil"
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UserNavItem(
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
