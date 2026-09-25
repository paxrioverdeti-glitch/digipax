package com.example.pxrioverde.repository

import com.example.pxrioverde.model.Ticket
import com.example.pxrioverde.model.TicketComment
import com.example.pxrioverde.model.TicketStatus
import com.example.pxrioverde.model.TicketMessage
import com.example.pxrioverde.model.analytics.TicketMetric
import com.example.pxrioverde.model.toDomain as toMessageDomain
import com.example.pxrioverde.model.toEntity as toMessageEntity
import com.example.pxrioverde.database.MessageDao
import com.example.pxrioverde.database.TicketDao
import com.example.pxrioverde.database.TicketEntity
import com.example.pxrioverde.database.toDomain as toTicketDomain
import com.example.pxrioverde.database.toEntity as toTicketEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.storage.storage
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class TicketRatingUpdate(
    val rating: Int,
    @SerialName("rating_comment") val ratingComment: String? = null
)

@OptIn(ExperimentalTime::class)
class TicketRepository(
    private val supabase: SupabaseClient,
    private val messageDao: MessageDao? = null,
    private val ticketDao: TicketDao? = null
) {
    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeJob: Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var ticketsChannel: RealtimeChannel? = null
    private var ticketsJob: Job? = null

    fun observeTickets(): Flow<List<Ticket>> {
        return ticketDao?.getAllTickets()?.map { entities ->
            entities.map { it.toTicketDomain() }
        } ?: emptyFlow()
    }

    suspend fun syncTickets(userId: String) {
        try {
            // Sincroniza apenas os últimos 50 chamados para evitar OOM e excesso de carga
            val tickets = getTickets(userId, page = 0, pageSize = 50)
            ticketDao?.insertTickets(tickets.map { it.toTicketEntity() })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchMessagesPage(ticketId: String, page: Int, pageSize: Int = 30) {
        val start = page * pageSize
        val end = start + pageSize - 1
        
        try {
            val messages = supabase.from("ticket_comments")
                .select {
                    filter {
                        eq("ticket_id", ticketId)
                    }
                    order("timestamp", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    range(start.toLong(), end.toLong())
                }
                .decodeList<TicketMessage>()
            
            messageDao?.insertMessages(messages.map { it.toMessageEntity() })
        } catch (e: Exception) {
            println("DEBUG: Erro ao buscar mensagens: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getMessagesFlow(ticketId: String): Flow<List<TicketMessage>> {
        return messageDao?.getMessagesForTicket(ticketId)?.map { entities ->
            entities.map { it.toMessageDomain() }
        } ?: kotlinx.coroutines.flow.emptyFlow()
    }

    suspend fun subscribeToMessages(ticketId: String, onNewMessage: (TicketMessage) -> Unit) {
        // Cleanup de conexões anteriores para evitar vazamento de memória e duplicidade
        realtimeJob?.cancel()
        realtimeChannel?.let {
            it.unsubscribe()
            supabase.realtime.removeChannel(it)
        }

        val channel = supabase.realtime.channel("ticket_comments_$ticketId")
        realtimeChannel = channel
        
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "ticket_comments"
        }

        realtimeJob = repositoryScope.launch {
            try {
                flow.collect { action ->
                    try {
                        val message = action.decodeRecord<TicketMessage>()
                        if (message.ticketId == ticketId) {
                            onNewMessage(message)
                            messageDao?.insertMessages(listOf(message.toMessageEntity()))
                        }
                    } catch (e: Exception) {
                        println("DEBUG: Erro ao decodificar registro: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Falha crítica no fluxo Realtime (Socket): ${e.message}")
                // Tenta se inscrever novamente após 5 segundos se a conexão abortar
                delay(5000)
                subscribeToMessages(ticketId, onNewMessage)
            }
        }
        
        channel.subscribe()
    }

    suspend fun saveMessageLocally(message: TicketMessage, syncStatus: String) {
        messageDao?.insertMessages(listOf(message.toMessageEntity(syncStatus)))
    }

    suspend fun createTicket(ticket: Ticket) {
        val payload = mapOf(
            "title" to ticket.title,
            "description" to ticket.description,
            "status" to ticket.status.name.lowercase(), 
            // Converte NA_FILA -> na_fila
            "user_id" to ticket.userId,
            "user_name" to ticket.userName,
            "computer_name" to ticket.computerName,
            "sector" to ticket.sector,
            "image_url" to ticket.imageUrl
        )
        println("DEBUG: Inserindo Ticket com payload manual: $payload")
        supabase.from("tickets").insert(payload)
    }

    suspend fun uploadAttachment(userId: String, bytes: ByteArray, fileName: String): String {
        val path = "$userId/${Clock.System.now().toEpochMilliseconds()}_$fileName"
        supabase.storage.from("attachments").upload(path, bytes)
        return supabase.storage.from("attachments").publicUrl(path)
    }

    suspend fun getTickets(userId: String, page: Int? = null, pageSize: Int = 20): List<Ticket> {
        return supabase.from("tickets_api")
            .select(Columns.raw("id, title, description, status, userId, userName, computer_name, sector, createdAt, image_url, rating, rating_comment")) {
                filter {
                    eq("userId", userId)
                }
                order("createdAt", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                
                // Aplica paginação ou um limite padrão de segurança (100)
                val start = (page ?: 0) * pageSize
                val end = start + pageSize - 1
                range(start.toLong(), end.toLong())
            }
            .decodeList<Ticket>()
    }

    suspend fun getAllTickets(page: Int? = null, pageSize: Int = 50): List<Ticket> {
        return try {
            val response = supabase.from("tickets_api")
                .select(Columns.raw("id, title, description, status, userId, userName, computer_name, sector, createdAt, image_url, rating, rating_comment, avatar_url")) {
                    order("createdAt", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    
                    val start = (page ?: 0) * pageSize
                    val end = start + pageSize - 1
                    range(start.toLong(), end.toLong())
                }
            
            println("DEBUG: RAW RESPONSE: ${response.data}")
            
            val list = response.decodeList<Ticket>()
            if (list.isNotEmpty()) {
                val first = list.first()
                println("DEBUG: FIRST TICKET KEY CHECK: rating=${first.rating}, comment=${first.ratingComment}")
            }
            return list
        } catch (e: Exception) {
            println("DEBUG: Erro ao buscar todos os chamados: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addMessage(message: TicketMessage) {
        supabase.from("ticket_comments").insert(message)
    }

    suspend fun addComment(ticketId: String, comment: TicketComment) {
        // Lógica simplificada: Em uma estrutura real, comentários seriam uma tabela separada
        // Para este MVP, estamos simulando a persistência no Supabase
        supabase.from("ticket_comments").insert(
            comment.copy(id = null) // Deixa o Supabase gerar o ID se configurado
        )
    }

    suspend fun subscribeToAllTickets(onUpdate: () -> Unit) {
        ticketsJob?.cancel()
        ticketsChannel?.let {
            it.unsubscribe()
            supabase.realtime.removeChannel(it)
        }

        val channel = supabase.realtime.channel("all_tickets_changes")
        ticketsChannel = channel

        // Escuta qualquer mudança (UPDATE/INSERT) na tabela tickets
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "tickets"
        }

        ticketsJob = repositoryScope.launch {
            flow.collect {
                onUpdate() // Notifica para recarregar a lista
            }
        }

        channel.subscribe()
    }

    suspend fun getTicketCounts(): Pair<Int, Int> {
        return try {
            val pendingAction = supabase.postgrest["tickets"].select {
                filter {
                    isIn("status", listOf("na_fila", "em_analise", "aguardando_peca", "em_atendimento"))
                }
                count(Count.EXACT)
            }
            val resolvedAction = supabase.postgrest["tickets"].select {
                filter {
                    eq("status", "resolvido")
                }
                count(Count.EXACT)
            }
            
            val pending = pendingAction.countOrNull()?.toInt() ?: 0
            val resolved = resolvedAction.countOrNull()?.toInt() ?: 0
            
            Pair(pending, resolved)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0, 0)
        }
    }

    suspend fun updateStatus(ticketId: String, newStatus: TicketStatus) {
        supabase.from("tickets")
            .update(mapOf("status" to newStatus.name.lowercase())) {
                filter {
                    eq("id", ticketId)
                }
            }
    }

    suspend fun rateTicket(ticketId: String, rating: Int, comment: String?) {
        println("DEBUG: SENDING UPDATE - ID: $ticketId | RATING: $rating | COMMENT: '$comment'")
        supabase.from("tickets").update(
            TicketRatingUpdate(
                rating = rating,
                ratingComment = comment
            )
        ) {
            filter {
                eq("id", ticketId)
            }
        }
    }

    suspend fun getTicketMetrics(): List<TicketMetric> {
        return try {
            supabase.from("ticket_metrics_by_sector")
                .select {
                    range(0, 100) // Limite de segurança para setores
                }
                .decodeList<TicketMetric>()
        } catch (e: Exception) {
            println("DEBUG: Erro ao buscar métricas de chamados: ${e.message}")
            emptyList()
        }
    }
}
