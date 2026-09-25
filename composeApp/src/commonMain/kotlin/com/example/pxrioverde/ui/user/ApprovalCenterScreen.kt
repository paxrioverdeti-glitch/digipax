package com.example.pxrioverde.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pxrioverde.model.User
import com.example.pxrioverde.viewmodel.AbsenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalCenterScreen(
    absenceViewModel: AbsenceViewModel,
    user: User
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ausências")
    val selectedAbsence by absenceViewModel.selectedAbsence.collectAsState()

    Scaffold(
        topBar = {
            if (selectedAbsence == null) {
                Surface(color = Color.White, shadowElevation = 4.dp) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Central de Aprovações",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B4332)
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.White
                        ),
                        windowInsets = WindowInsets.statusBars
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F7F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (selectedAbsence == null) Modifier.padding(padding) else Modifier)
        ) {
            if (selectedAbsence == null) {
                TabRow(
                    selectedTabIndex = selectedTab.coerceAtMost(tabs.size - 1),
                    containerColor = Color.White,
                    contentColor = Color(0xFF1B4332),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.coerceAtMost(tabs.size - 1)]),
                            color = Color(0xFF1B4332)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title.uppercase(), 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                                    letterSpacing = 1.sp
                                ) 
                            }
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> AbsenceApprovalTab(absenceViewModel, user.id)
            }
        }
    }
}

@Composable
fun AbsenceApprovalTab(viewModel: AbsenceViewModel, userId: String) {
    ApproverPortal(approverId = userId, viewModel = viewModel)
}
