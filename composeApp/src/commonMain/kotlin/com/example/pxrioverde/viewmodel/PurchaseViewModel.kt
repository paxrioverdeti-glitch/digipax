package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.PurchaseRequest
import com.example.pxrioverde.model.PurchaseStatus
import com.example.pxrioverde.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PurchaseUiState {
    object Loading : PurchaseUiState()
    data class Success(val requests: List<PurchaseRequest>) : PurchaseUiState()
    data class Error(val message: String) : PurchaseUiState()
    object Empty : PurchaseUiState()
}

class PurchaseViewModel(
    private val repository: PurchaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PurchaseUiState>(PurchaseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _userRequests = MutableStateFlow<List<PurchaseRequest>>(emptyList())
    val userRequests = _userRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private var attachmentBytes: ByteArray? = null
    private var attachmentName: String? = null

    fun setAttachment(bytes: ByteArray?, fileName: String?) {
        attachmentBytes = bytes
        attachmentName = fileName
    }

    fun loadUserRequests(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userRequests.value = repository.getRequestsByRequester(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRequest(request: PurchaseRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                var finalRequest = request
                
                // Upload anexo se existir
                attachmentBytes?.let { bytes ->
                    attachmentName?.let { name ->
                        val url = repository.uploadAttachment(bytes, name)
                        finalRequest = finalRequest.copy(attachmentUrl = url)
                    }
                }

                repository.submitPurchaseRequest(finalRequest)
                attachmentBytes = null
                attachmentName = null
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateRequestStatus(
        requestId: String, 
        status: PurchaseStatus, 
        comment: String?, 
        approvedValue: Double? = null,
        approverName: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.updateRequestStatus(requestId, status, comment, approvedValue, approverName)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun loadAllRequests() {
        viewModelScope.launch {
            _uiState.value = PurchaseUiState.Loading
            try {
                val requests = repository.getAllRequests()
                _userRequests.value = requests
                
                val pending = requests.filter { it.status == PurchaseStatus.PENDENTE }
                if (pending.isEmpty()) {
                    _uiState.value = PurchaseUiState.Empty
                } else {
                    _uiState.value = PurchaseUiState.Success(pending)
                }
            } catch (e: Exception) {
                _uiState.value = PurchaseUiState.Error(e.message ?: "Erro desconhecido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _approvedRequests = MutableStateFlow<List<PurchaseRequest>>(emptyList())
    val approvedRequests = _approvedRequests.asStateFlow()

    fun loadApprovedRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val all = repository.getAllRequests()
                _approvedRequests.value = all.filter { it.status == PurchaseStatus.APROVADO }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsPurchased(requestId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                repository.updateRequestStatus(requestId, PurchaseStatus.COMPRADO, "Item adquirido pelo setor de compras.")
                loadApprovedRequests()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearData() {
        _userRequests.value = emptyList()
        attachmentBytes = null
        attachmentName = null
    }
}
