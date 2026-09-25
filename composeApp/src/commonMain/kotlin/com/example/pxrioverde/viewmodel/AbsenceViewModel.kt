package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.ComunicadoState
import com.example.pxrioverde.model.AdminFeedback
import com.example.pxrioverde.model.User
import com.example.pxrioverde.model.Supervisor
import com.example.pxrioverde.model.SupervisorData
import com.example.pxrioverde.repository.AbsenceRepository
import com.example.pxrioverde.service.AuthService
import com.example.pxrioverde.util.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AbsenceViewModel(
    private val repository: AbsenceRepository,
    private val authService: AuthService
) : ViewModel() {

    private val _absences = MutableStateFlow<List<ComunicadoState>>(emptyList())
    val absences = _absences.asStateFlow()

    private val _userAbsences = MutableStateFlow<List<ComunicadoState>>(emptyList())
    val userAbsences = _userAbsences.asStateFlow()

    private val _approverAbsences = MutableStateFlow<List<ComunicadoState>>(emptyList())
    val approverAbsences = _approverAbsences.asStateFlow()

    private val _approvers = MutableStateFlow<List<User>>(emptyList())
    val approvers = _approvers.asStateFlow()

    private val _fixedSupervisors = MutableStateFlow<List<Supervisor>>(SupervisorData.list)
    val fixedSupervisors = _fixedSupervisors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedAbsence = MutableStateFlow<ComunicadoState?>(null)
    val selectedAbsence = _selectedAbsence.asStateFlow()

    private val _lastViewedAbsenceId = MutableStateFlow(LocalStorage.getString("last_viewed_absence_id", ""))
    val lastViewedAbsenceId = _lastViewedAbsenceId.asStateFlow()

    fun selectAbsence(absence: ComunicadoState?) {
        _selectedAbsence.value = absence
    }

    fun markAbsenceAsViewed(id: String) {
        if (id.isBlank()) return
        LocalStorage.putString("last_viewed_absence_id", id)
        _lastViewedAbsenceId.value = id
    }

    fun loadAbsences() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _absences.value = repository.getAllAbsences()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserAbsences(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userAbsences.value = repository.getAbsencesByUser(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadApproverAbsences(approverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _approverAbsences.value = repository.getAbsencesForApprover(approverId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadApprovers() {
        viewModelScope.launch {
            _approvers.value = authService.getApprovers()
        }
    }

    fun observeAbsences() {
        viewModelScope.launch {
            repository.subscribeToAbsences {
                loadAbsences()
            }
        }
    }

    fun submitAbsence(absence: ComunicadoState, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.submitAbsence(absence)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFeedback(absenceId: String, feedback: AdminFeedback, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateFeedback(absenceId, feedback)
                onSuccess()
                loadAbsences() // Recarrega para refletir a mudança
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearData() {
        _absences.value = emptyList()
        _userAbsences.value = emptyList()
        _approverAbsences.value = emptyList()
        _selectedAbsence.value = null
    }
}
