package com.example.pxrioverde.service

import com.example.pxrioverde.model.*
import com.example.pxrioverde.util.NotificationManager
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

class NotificationService(private val notificationManager: NotificationManager? = null) {
    private val subscriptionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _notifications = MutableStateFlow<List<CorporateNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val NATALIA_EMAIL = "natalia@pax.com"
    private val NATALIA_ID = "d1e74a8a-1896-41e1-9e9a-36d05bef4d48"

    fun subscribeAll(user: User) {
        subscribeToTicketUpdates(user)
        subscribeToAbsenceUpdates(user)
        subscribeToPurchaseUpdates(user)
    }

    fun subscribeToTicketUpdates(user: User) {
        subscriptionScope.launch {
            try {
                val channel = supabase.realtime.channel("ticket_updates_${user.id}")
                
                // 1. Notificar ADMIN quando houver um NOVO chamado ou nova mensagem de usuário
                if (user.role == UserRole.ADMIN) {
                    // Novo Chamado
                    launch {
                        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                            table = "tickets"
                        }.collect { action ->
                            try {
                                val ticket = action.decodeRecord<Ticket>()
                                val title = "Novo Chamado Aberto"
                                val text = "${ticket.userName} abriu um chamado: ${ticket.title}"
                                showNotification(title, text)
                            } catch (e: Exception) {
                                println("DEBUG: Erro ao processar novo chamado: ${e.message}")
                            }
                        }
                    }

                    // Nova Mensagem de Usuário
                    launch {
                        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                            table = "ticket_comments"
                        }.collect { action ->
                            try {
                                val message = action.decodeRecord<TicketMessage>()
                                if (!message.isAdmin) {
                                    val title = "Nova Mensagem de Usuário"
                                    val text = "${message.authorName}: ${message.text.take(50)}"
                                    showNotification(title, text)
                                }
                            } catch (e: Exception) {
                                println("DEBUG: Erro ao processar mensagem no chamado: ${e.message}")
                            }
                        }
                    }
                } else {
                    // 2. Notificar USUÁRIO COMUM quando o Admin responde
                    launch {
                        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                            table = "ticket_comments"
                        }.collect { action ->
                            try {
                                val message = action.decodeRecord<TicketMessage>()
                                if (message.isAdmin) {
                                    val title = "Resposta do Suporte"
                                    val text = message.text.take(50) + if (message.text.length > 50) "..." else ""
                                    showNotification(title, text)
                                }
                            } catch (e: Exception) {
                                println("DEBUG: Erro ao processar resposta: ${e.message}")
                            }
                        }
                    }
                }
                
                channel.subscribe()
            } catch (e: Exception) {
                println("DEBUG: Erro ao se inscrever em chamados: ${e.message}")
            }
        }
    }

    fun subscribeToAbsenceUpdates(user: User) {
        subscriptionScope.launch {
            try {
                val channel = supabase.realtime.channel("absence_updates_${user.id}")

                // 1. Notificar USUÁRIO quando o Supervisor/RH der um feedback
                launch {
                    channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                        table = "absences"
                    }.collect { action ->
                        try {
                            val absence = action.decodeRecord<ComunicadoState>()
                            if (absence.userId == user.id && absence.adminFeedback != null) {
                                val title = "Ausência Avaliada"
                                val text = "Seu comunicado de ${absence.type.label} foi avaliado."
                                showNotification(title, text)
                            }
                        } catch (e: Exception) {
                            println("DEBUG: Erro ao processar notificação de ausência: ${e.message}")
                        }
                    }
                }

                // 2. Notificar SUPERVISOR / ENCARREGADO / ADMIN quando houver uma nova solicitação para ele
                if (user.role == UserRole.SUPERVISOR || user.role == UserRole.ENCARREGADO || user.role == UserRole.ADMIN) {
                    launch {
                        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                            table = "absences"
                        }.collect { action ->
                            try {
                                val absence = action.decodeRecord<ComunicadoState>()
                                if (absence.targetApproverId == user.id || user.role == UserRole.ADMIN) {
                                    val title = "Nova Solicitação de Ausência"
                                    val text = "${absence.userName} enviou um comunicado de ${absence.type.label}."
                                    showNotification(title, text)
                                }
                            } catch (e: Exception) {
                                println("DEBUG: Erro ao processar nova solicitação: ${e.message}")
                            }
                        }
                    }
                }

                channel.subscribe()
            } catch (e: Exception) {
                println("DEBUG: Erro ao se inscrever em ausências: ${e.message}")
            }
        }
    }

    fun subscribeToPurchaseUpdates(user: User) {
        subscriptionScope.launch {
            try {
                val channel = supabase.realtime.channel("purchase_updates_${user.id}")

                // 1. Notificar a NATÁLIA (ou Admin) sobre novas solicitações
                val isNatalia = user.id == NATALIA_ID || user.email == NATALIA_EMAIL
                if (isNatalia || user.role == UserRole.ADMIN) {
                    launch {
                        channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                            table = "purchase_requests"
                        }.collect { action ->
                            try {
                                val request = action.decodeRecord<PurchaseRequest>()
                                val title = "Nova Solicitação de Compra"
                                val text = "${request.requesterName} solicitou: ${request.itemName}"
                                showNotification(title, text)
                            } catch (e: Exception) {
                                println("DEBUG: Erro ao processar notificação de compra: ${e.message}")
                            }
                        }
                    }
                }

                // 2. Notificar o SOLICITANTE quando o status mudar
                launch {
                    channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                        table = "purchase_requests"
                    }.collect { action ->
                        try {
                            val request = action.decodeRecord<PurchaseRequest>()
                            if (request.requesterId == user.id) {
                                val title = "Atualização de Compra"
                                val text = "Sua solicitação de ${request.itemName} está: ${request.status.label}."
                                showNotification(title, text)
                            }
                        } catch (e: Exception) {
                            println("DEBUG: Erro ao processar feedback de compra: ${e.message}")
                        }
                    }
                }

                channel.subscribe()
            } catch (e: Exception) {
                println("DEBUG: Erro ao se inscrever em compras: ${e.message}")
            }
        }
    }

    private fun showNotification(title: String, text: String) {
        val newNotif = CorporateNotification(
            id = Clock.System.now().toEpochMilliseconds().toString(),
            title = title,
            message = text
        )
        
        _notifications.value = listOf(newNotif) + _notifications.value
        _unreadCount.value += 1
        
        // Mostra notificação do sistema
        notificationManager?.showNotification(title, text)
    }

    fun clearUnreadCount() {
        _unreadCount.value = 0
    }
}
