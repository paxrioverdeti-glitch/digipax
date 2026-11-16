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

    private val _onlyDamagedFilter = MutableStateFlow(false)
    val onlyDamagedFilter = _onlyDamagedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /** Foto ampliada: título, link e os danos para desenhar por cima. */
    data class ModalImage(val title: String, val url: String, val danos: List<com.example.pxrioverde.model.DanoEncontradoDto> = emptyList())

    private val _selectedImageForModal = MutableStateFlow<ModalImage?>(null)
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

    fun openImageModal(title: String, imageUrl: String, danos: List<com.example.pxrioverde.model.DanoEncontradoDto> = emptyList()) {
        _selectedImageForModal.value = ModalImage(title, imageUrl, danos)
    }

    fun closeImageModal() {
        _selectedImageForModal.value = null
    }

    private val _reviewError = MutableStateFlow<String?>(null)
    val reviewError = _reviewError.asStateFlow()

    /** Atualiza o cartão na hora e grava no banco; se falhar, volta como estava. */
    fun reviewInspection(tripId: String, status: String?) {
        val before = _reports.value
        _reports.value = before.map { if (it.tripId == tripId) it.copy(reviewStatus = status) else it }
        viewModelScope.launch {
            repository.reviewInspection(tripId, status).onFailure { e ->
                _reports.value = before
                _reviewError.value = "Não foi possível salvar a revisão: ${e.message}"
            }
        }
    }

    fun clearReviewError() { _reviewError.value = null }

    // ---------- Análise pela IA pedida pelo gestor ----------

    /** Viagens sendo analisadas agora (para mostrar "Analisando..." no cartão). */
    private val _analyzingTripIds = MutableStateFlow<Set<String>>(emptySet())
    val analyzingTripIds = _analyzingTripIds.asStateFlow()

    /** Progresso do "Analisar todas": (feitas, total). Nulo quando não está rodando. */
    private val _batchProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val batchProgress = _batchProgress.asStateFlow()

    private var batchJob: kotlinx.coroutines.Job? = null

    /** Analisa uma vistoria e espera o resultado. */
    fun analyzeNow(tripId: String) {
        if (tripId in _analyzingTripIds.value) return
        viewModelScope.launch {
            _analyzingTripIds.value = _analyzingTripIds.value + tripId
            repository.requestServerAnalysis(tripId, waitForResult = true).onFailure { e ->
                _reviewError.value = "Não foi possível analisar: ${e.message}"
            }
            _analyzingTripIds.value = _analyzingTripIds.value - tripId
            refreshQuietly()
        }
    }

    /**
     * Analisa todas as vistorias sem análise, UMA POR VEZ (para não estourar o limite
     * por minuto do Gemini). O gestor pode parar no meio.
     */
    fun analyzeAllPending() {
        if (batchJob?.isActive == true) return
        val pending = _reports.value.filter { it.isAnalysisPending }.map { it.tripId }
        if (pending.isEmpty()) return
        batchJob = viewModelScope.launch {
            var failures = 0
            var firstError: String? = null
            try {
                var quotaReached = false
                for ((index, tripId) in pending.withIndex()) {
                    _batchProgress.value = index to pending.size
                    _analyzingTripIds.value = _analyzingTripIds.value + tripId
                    repository.requestServerAnalysis(tripId, waitForResult = true).onFailure { e ->
                        failures++
                        if (firstError == null) firstError = e.message
                        if (e.message?.contains("limite diário") == true) quotaReached = true
                    }
                    _analyzingTripIds.value = _analyzingTripIds.value - tripId
                    refreshQuietly()
                    // Limite diário do Gemini acabou: parar, as próximas falhariam igual.
                    if (quotaReached) break
                }
                if (quotaReached) {
                    _reviewError.value = "O limite diário gratuito do Gemini acabou. As vistorias que faltaram continuam " +
                        "pendentes: rode \"Analisar todas\" de novo amanhã."
                } else if (failures > 0) {
                    _reviewError.value = "$failures de ${pending.size} vistorias não puderam ser analisadas. " +
                        "Motivo: ${firstError ?: "desconhecido"}"
                }
            } finally {
                _batchProgress.value = null
                _analyzingTripIds.value = emptySet()
            }
        }
    }

    fun stopBatch() {
        batchJob?.cancel()
    }

    /** Recarrega a lista sem mostrar o carregamento por cima de tudo. */
    private suspend fun refreshQuietly() {
        try {
            _reports.value = repository.getDamagedReportsForAdmin(_onlyDamagedFilter.value)
        } catch (e: Exception) {
            println("Erro ao atualizar vistorias: ${e.message}")
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            println("[AVARIAS] Carregando vistorias (somente com dano = ${_onlyDamagedFilter.value})...")
            try {
                val fetchedReports = repository.getDamagedReportsForAdmin(_onlyDamagedFilter.value)
                println("[AVARIAS] ${fetchedReports.size} vistorias carregadas para a tela")
                _reports.value = fetchedReports
            } catch (e: Exception) {
                println("Erro ao carregar relatórios na ViewModel: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
