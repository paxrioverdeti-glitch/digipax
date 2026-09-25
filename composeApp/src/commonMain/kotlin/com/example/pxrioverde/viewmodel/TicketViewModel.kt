package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketComment
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.model.TicketMessage
import com.example.pxrioverde.repository.TicketRepository
import com.example.pxrioverde.domain.usecase.SubmitTicketUseCase
import com.example.pxrioverde.generateUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TicketViewModel(
    private val repository: TicketRepository,
    private val submitTicketUseCase: SubmitTicketUseCase
) : ViewModel() {

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets = _tickets.asStateFlow()

    private val _selectedTicket = MutableStateFlow<Ticket?>(null)
    val selectedTicket = _selectedTicket.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _messages = MutableStateFlow<List<TicketMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages = _isLoadingMessages.asStateFlow()

    private var attachmentBytes: ByteArray? = null
    private var attachmentName: String? = null

    fun setAttachment(bytes: ByteArray?, fileName: String) {
        attachmentBytes = bytes
        attachmentName = fileName
    }

    private var messagesJob: kotlinx.coroutines.Job? = null

    fun selectTicket(ticket: Ticket?) {
        _selectedTicket.value = ticket
        messagesJob?.cancel()
        if (ticket != null) {
            loadTicketMessages(ticket.id ?: "")
        } else {
            _messages.value = emptyList()
        }
    }

    private var currentTicketsJob: kotlinx.coroutines.Job? = null

    fun observeTickets() {
        currentTicketsJob?.cancel()
        currentTicketsJob = viewModelScope.launch {
            repository.observeTickets().collect {
                _tickets.value = it
            }
        }
    }

    fun loadTickets(userId: String, page: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sincroniza com o banco local
                repository.syncTickets(userId)
                
                // Reforço: Busca do banco local e atualiza o estado imediatamente
                // Isso garante que a lista apareça mesmo se a observação demorar a iniciar
                repository.observeTickets().first().let { 
                    _tickets.value = it 
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllTickets(page: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tickets = repository.getAllTickets(page = page ?: 0)
                _tickets.value = tickets 
                
                // Sincroniza o chamado selecionado com a versão mais recente da lista
                _selectedTicket.value?.let { selected ->
                    tickets.find { it.id == selected.id }?.let { updated ->
                        _selectedTicket.value = updated
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observeAllTicketsRealtime() {
        viewModelScope.launch {
            repository.subscribeToAllTickets {
                loadAllTickets() // Recarrega quando houver mudança no banco (incluindo notas)
            }
        }
    }

    fun loadTicketMessages(ticketId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            _isLoadingMessages.value = true
            
            // 1. Inicia observação local imediatamente
            launch {
                repository.getMessagesFlow(ticketId).collect {
                    _messages.value = it.sortedBy { m -> m.timestamp }
                }
            }

            // 2. Tenta sincronizar com o servidor em background
            try {
                repository.fetchMessagesPage(ticketId, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingMessages.value = false
            }

            // 3. Tenta assinar Realtime para novas mensagens
            try {
                repository.subscribeToMessages(ticketId) { newMessage ->
                    // A própria função subscribeToMessages já insere no banco local, 
                    // o que fará o flow do item 1 disparar.
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadNextMessages(ticketId: String) {
        loadTicketMessages(ticketId)
    }

    fun submitTicket(
        userId: String,
        userName: String,
        title: String,
        computer: String,
        sector: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                submitTicketUseCase(
                    userId = userId,
                    userName = userName,
                    title = title,
                    computer = computer,
                    sector = sector,
                    description = description,
                    attachmentBytes = attachmentBytes,
                    attachmentName = attachmentName,
                    onSuccess = {
                        onSuccess()
                        attachmentBytes = null
                        attachmentName = null
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateStatus(status: TicketStatus) {
        val ticketId = _selectedTicket.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.updateStatus(ticketId, status)
                _selectedTicket.value = _selectedTicket.value?.copy(status = status)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rateTicket(rating: Int, comment: String?) {
        val ticketId = _selectedTicket.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.rateTicket(ticketId, rating, comment)
                _selectedTicket.value = _selectedTicket.value?.copy(
                    rating = rating,
                    ratingComment = comment
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addComment(
        text: String,
        authorName: String,
        isAdmin: Boolean,
        onResult: (Boolean) -> Unit = {}
    ) {
        sendMessage(text, authorName, isAdmin, onResult)
    }

    fun sendMessage(
        text: String,
        authorName: String,
        isAdmin: Boolean,
        onResult: (Boolean) -> Unit = {}
    ) {
        val ticketId = _selectedTicket.value?.id ?: return
        viewModelScope.launch {
            try {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val messageId = generateUUID()
                
                // 1. Cria a mensagem com ID real gerado no cliente
                val message = TicketMessage(
                    id = messageId, 
                    ticketId = ticketId,
                    authorName = authorName,
                    text = text,
                    timestamp = timestamp,
                    isAdmin = isAdmin
                )

                // 2. Salva localmente IMEDIATAMENTE (UI atualiza pelo Flow)
                repository.saveMessageLocally(message, "SENDING")
                
                // 3. Envia para o Supabase com o MESMO ID
                // Assim o Realtime quando retornar terá o mesmo ID e o Room fará REPLACE (evita duplicidade)
                repository.addMessage(message)
                
                // 4. Marca como enviado localmente
                repository.saveMessageLocally(message, "SENT")
                
                onResult(true)
            } catch (e: Exception) {
                println("DEBUG: Erro ao enviar mensagem chat: ${e.message}")
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    fun clearData() {
        _tickets.value = emptyList()
        _selectedTicket.value = null
        _messages.value = emptyList()
        attachmentBytes = null
        attachmentName = null
    }
}
