package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pxrioverde.model.ComunicadoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {
    private val _selectedSection = MutableStateFlow(0)
    val selectedSection = _selectedSection.asStateFlow()

    private val _showProfile = MutableStateFlow(false)
    val showProfile = _showProfile.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications = _showNotifications.asStateFlow()

    private val _selectedAbsence = MutableStateFlow<ComunicadoState?>(null)
    val selectedAbsence = _selectedAbsence.asStateFlow()

    fun selectSection(section: Int) {
        _selectedSection.value = section
        _showProfile.value = false
        _showNotifications.value = false
    }

    fun setShowProfile(show: Boolean) {
        _showProfile.value = show
        if (show) _showNotifications.value = false
    }

    fun setShowNotifications(show: Boolean) {
        _showNotifications.value = show
        if (show) _showProfile.value = false
    }

    fun selectAbsence(absence: ComunicadoState?) {
        _selectedAbsence.value = absence
    }

    fun goBack(): Boolean {
        return when {
            _selectedAbsence.value != null -> {
                _selectedAbsence.value = null
                true
            }
            _showProfile.value -> {
                _showProfile.value = false
                true
            }
            _showNotifications.value -> {
                _showNotifications.value = false
                true
            }
            _selectedSection.value != 0 -> {
                _selectedSection.value = 0
                true
            }
            else -> false
        }
    }

    fun clearData() {
        _selectedSection.value = 0
        _showProfile.value = false
        _showNotifications.value = false
        _selectedAbsence.value = null
    }
}
