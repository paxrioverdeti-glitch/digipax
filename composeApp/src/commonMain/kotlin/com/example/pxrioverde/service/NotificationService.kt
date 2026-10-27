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
        subscribeToInspectionUpdates(user)
    }

    fun subscribeToTicketUpdates(user: User) {
        subscriptionScope.launch {
            try {
                val channel = supabase.realtime.channel("ticket_updates_${user.id}")
                
                // 1. Notificar ADMIN quando houver um NOVO chamado ou nova mensagem de usuário
                if (user.role == UserRole.ADMIN) {
                    // Novo Chamado
                    launch {
                        try {
                            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                                table = "tickets"
                            }.collect { action ->
                                try {
                                    val ticket = action.decodeRecord<Ticket>()
                                    val title = "Novo Chamado Aberto"
                                    val text = "${ticket.userName} abriu um chamado: ${ticket.title}"
                                    showNotification(title, text)
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar novo chamado: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de tickets: ${e.message}")
                        }
                    }

                    // Nova Mensagem de Usuário
                    launch {
                        try {
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
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar mensagem no chamado: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de comentarios: ${e.message}")
                        }
                    }
                } else {
                    // 2. Notificar USUÁRIO COMUM quando o Admin responde
                    launch {
                        try {
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
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar resposta: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de respostas de suporte: ${e.message}")
                        }
                    }
                }
                
                channel.subscribe()
            } catch (e: Throwable) {
                println("DEBUG: Erro ao se inscrever em chamados: ${e.message}")
            }
        }
    }

    fun notifyUserAbsenceSubmitted(typeLabel: String) {
        val title = "Comunicado Enviado"
        val text = "Seu comunicado de $typeLabel foi registrado e enviado ao supervisor."
        showNotification(title, text)
    }

    fun subscribeToAbsenceUpdates(user: User) {
        subscriptionScope.launch {
            try {
                val channel = supabase.realtime.channel("absence_updates_${user.id}")

                // 1. Notificar USUÁRIO quando o Supervisor/RH der um feedback
                launch {
                    try {
                        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                            table = "absences"
                        }.collect { action ->
                            try {
                                val absence = action.decodeRecord<RealtimeAbsenceRow>()
                                if (absence.userId == user.id && absence.adminFeedback != null) {
                                    val title = "Ausência Avaliada"
                                    val text = "Seu comunicado de ${absence.type.label} foi avaliado."
                                    showNotification(title, text)
                                }
                            } catch (e: Throwable) {
                                println("DEBUG: Erro ao processar notificação de ausência: ${e.message}")
                            }
                        }
                    } catch (e: Throwable) {
                        println("DEBUG: Erro na escuta de ausencias: ${e.message}")
                    }
                }

                // 2. Notificar SUPERVISOR / ENCARREGADO / ADMIN quando houver uma nova solicitação para ele
                if (user.role == UserRole.SUPERVISOR || user.role == UserRole.ENCARREGADO || user.role == UserRole.ADMIN) {
                    launch {
                        try {
                            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                                table = "absences"
                            }.collect { action ->
                                try {
                                    val absence = action.decodeRecord<RealtimeAbsenceRow>()
                                    if (absence.targetApproverId == user.id || absence.targetApproverId.isNullOrBlank() || user.role == UserRole.ADMIN) {
                                        val title = "Nova Solicitação de Ausência"
                                        val name = if (absence.userName.isNotBlank()) absence.userName else "Um colaborador"
                                        val text = "$name enviou um comunicado de ${absence.type.label}."
                                        showNotification(title, text)
                                    }
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar nova solicitação: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de novas solicitacoes de ausencia: ${e.message}")
                        }
                    }
                }

                channel.subscribe()
            } catch (e: Throwable) {
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
                        try {
                            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                                table = "purchase_requests"
                            }.collect { action ->
                                try {
                                    val request = action.decodeRecord<PurchaseRequest>()
                                    val title = "Nova Solicitação de Compra"
                                    val text = "${request.requesterName} solicitou: ${request.itemName}"
                                    showNotification(title, text)
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar notificação de compra: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de compras: ${e.message}")
                        }
                    }
                }

                // 2. Notificar o SOLICITANTE quando o status mudar
                launch {
                    try {
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
                            } catch (e: Throwable) {
                                println("DEBUG: Erro ao processar feedback de compra: ${e.message}")
                            }
                        }
                    } catch (e: Throwable) {
                        println("DEBUG: Erro na escuta de atualizacoes de compra: ${e.message}")
                    }
                }

                channel.subscribe()
            } catch (e: Throwable) {
                println("DEBUG: Erro ao se inscrever em compras: ${e.message}")
            }
        }
    }

    /**
     * Inscreve o Admin/Gestor para receber alertas de vistoria veicular em tempo real.
     */
    fun subscribeToInspectionUpdates(user: User) {
        subscriptionScope.launch {
            try {
                if (user.role == UserRole.ADMIN || user.role == UserRole.SUPERVISOR || user.role == UserRole.ENCARREGADO) {
                    val channel = supabase.realtime.channel("inspection_updates_${user.id}")

                    launch {
                        try {
                            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                                table = "inspection_reports"
                            }.collect { action ->
                                try {
                                    val report = action.decodeRecord<RealtimeInspectionReportRow>()
                                    if (report.houveNovoDano) {
                                        val title = "⚠️ Dano na Lataria Detectado"
                                        val text = "Novo dano em veículo da frota: ${report.resumoGeral.take(80)}"
                                        showNotification(title, text)
                                    } else {
                                        val title = "🚗 Vistoria de Frota Concluída"
                                        val text = "Vistoria concluída sem novos danos identificados."
                                        showNotification(title, text)
                                    }
                                } catch (e: Throwable) {
                                    println("DEBUG: Erro ao processar notificação de vistoria: ${e.message}")
                                }
                            }
                        } catch (e: Throwable) {
                            println("DEBUG: Erro na escuta de vistorias: ${e.message}")
                        }
                    }

                    channel.subscribe()
                }
            } catch (e: Throwable) {
                println("DEBUG: Erro ao se inscrever em vistorias: ${e.message}")
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
        
        // Mostra notificação nativa do sistema operacional do dispositivo mobile (Android/iOS)
        notificationManager?.showNotification(title, text)
    }

    fun clearUnreadCount() {
        _unreadCount.value = 0
    }
}

@kotlinx.serialization.Serializable
private data class RealtimeAbsenceRow(
    val id: String? = null,
    @kotlinx.serialization.SerialName("user_id") val userIdSnake: String? = null,
    @kotlinx.serialization.SerialName("userId") val userIdCamel: String? = null,
    @kotlinx.serialization.SerialName("user_name") val userNameSnake: String? = null,
    @kotlinx.serialization.SerialName("userName") val userNameCamel: String? = null,
    @kotlinx.serialization.SerialName("target_approver_id") val targetApproverIdSnake: String? = null,
    @kotlinx.serialization.SerialName("targetApproverId") val targetApproverIdCamel: String? = null,
    val type: AbsenceType = AbsenceType.ATRASO,
    @kotlinx.serialization.SerialName("admin_feedback") val adminFeedbackSnake: AdminFeedback? = null,
    @kotlinx.serialization.SerialName("adminFeedback") val adminFeedbackCamel: AdminFeedback? = null
) {
    val userId: String get() = userIdSnake ?: userIdCamel ?: ""
    val userName: String get() = userNameSnake ?: userNameCamel ?: ""
    val targetApproverId: String? get() = targetApproverIdSnake ?: targetApproverIdCamel
    val adminFeedback: AdminFeedback? get() = adminFeedbackSnake ?: adminFeedbackCamel
}

@kotlinx.serialization.Serializable
private data class RealtimeInspectionReportRow(
    val id: String? = null,
    @kotlinx.serialization.SerialName("houve_novo_dano") val houveNovoDano: Boolean = false,
    @kotlinx.serialization.SerialName("resumo_geral") val resumoGeral: String = ""
)
