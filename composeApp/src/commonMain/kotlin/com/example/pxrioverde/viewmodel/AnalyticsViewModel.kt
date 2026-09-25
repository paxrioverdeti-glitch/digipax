package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.analytics.DashboardData
import com.example.pxrioverde.domain.usecase.GetAnalyticsDashboardUseCase
import com.example.pxrioverde.util.export.FileSharer
import com.example.pxrioverde.util.export.ReportGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AnalyticsViewModel(
    private val getAnalyticsDashboardUseCase: GetAnalyticsDashboardUseCase,
    private val fileSharer: FileSharer
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    private val _selectedMonth = MutableStateFlow(now.month.ordinal + 1)
    val selectedMonth = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(now.year)
    val selectedYear = _selectedYear.asStateFlow()

    fun setPeriod(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AnalyticsUiState.Loading
            try {
                val data = getAnalyticsDashboardUseCase(_selectedMonth.value, _selectedYear.value)
                _uiState.value = AnalyticsUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(e.message ?: "Erro ao carregar métricas")
            }
        }
    }

    fun exportTicketsReport() {
        val state = _uiState.value
        if (state is AnalyticsUiState.Success) {
            val csv = ReportGenerator.generateTicketsCsv(state.data.allTickets)
            fileSharer.shareCsv(csv, "relatorio_chamados")
        }
    }

    fun exportTripsReport() {
        val state = _uiState.value
        if (state is AnalyticsUiState.Success) {
            val csv = ReportGenerator.generateTripsCsv(state.data.appointments)
            fileSharer.shareCsv(csv, "relatorio_viagens")
        }
    }

    fun clearData() {
        _uiState.value = AnalyticsUiState.Loading
    }
}

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: DashboardData) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}
