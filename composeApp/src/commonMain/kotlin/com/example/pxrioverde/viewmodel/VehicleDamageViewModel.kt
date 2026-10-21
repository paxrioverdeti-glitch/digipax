package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.VehicleDamageReportItem
import com.example.pxrioverde.repository.VehicleInspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDamageViewModel(
    private val repository: VehicleInspectionRepository
) : ViewModel() {

    private val _reports = MutableStateFlow<List<VehicleDamageReportItem>>(emptyList())
    val reports = _reports.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _onlyDamagedFilter = MutableStateFlow(true)
    val onlyDamagedFilter = _onlyDamagedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedImageForModal = MutableStateFlow<Pair<String, String>?>(null) // Title to URL
    val selectedImageForModal = _selectedImageForModal.asStateFlow()

    init {
        loadReports()
    }

    fun setOnlyDamagedFilter(onlyDamaged: Boolean) {
        _onlyDamagedFilter.value = onlyDamaged
        loadReports()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openImageModal(title: String, imageUrl: String) {
        _selectedImageForModal.value = title to imageUrl
    }

    fun closeImageModal() {
        _selectedImageForModal.value = null
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedReports = repository.getDamagedReportsForAdmin(_onlyDamagedFilter.value)
                _reports.value = fetchedReports
            } catch (e: Exception) {
                println("Erro ao carregar relatórios na ViewModel: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
