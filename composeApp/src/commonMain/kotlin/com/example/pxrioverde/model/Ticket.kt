package com.example.pxrioverde.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class TicketStatus {
    @SerialName("na_fila") NA_FILA,
    @SerialName("em_analise") EM_ANALISE,
    @SerialName("aguardando_peca") AGUARDANDO_PECA,
    @SerialName("em_atendimento") EM_ATENDIMENTO,
    @SerialName("resolvido") RESOLVIDO,
    @SerialName("cancelado") CANCELADO
}

@Serializable
data class TicketComment(
    val id: String? = null,
    @SerialName("author_name") val authorName: String,
    val text: String,
    val timestamp: Long,
    @SerialName("is_admin") val isAdmin: Boolean
)

@Serializable
data class Ticket(
    val id: String? = null,
    val title: String,
    val description: String,
    val status: TicketStatus = TicketStatus.NA_FILA,
    @SerialName("userId") val userId: String,
    @SerialName("userName") val userName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("computer_name") val computerName: String = "",
    val sector: String = "",
    @SerialName("createdAt") val createdAt: String? = null,
    val comments: List<TicketComment> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null,
    val rating: Int? = null,
    @SerialName("rating_comment") val ratingComment: String? = null
) {
    val userAvatarUrl: String? get() = avatarUrl
}

object TicketConstants {
    val GOBAH_OPTIONS = listOf(
        "Impressora", "Pasta Corporativa", "Odoo", "E-mail Corporativo", 
        "Acesso ao Computador", "Atualizar/ Instalar programas", 
        "Resetar senha PC", "Computador lento", "Programa do Pc não abre", 
        "Criar/Excluir usuário", "Pacote office/WPS", "Outro"
    )

    val TI_INTERNO_OPTIONS = listOf(
        "Internet", "Dmpax", "Aplicativos internos/Externos", "Teams",
        "Equipamentos com Defeitos", "Celulares", "Linhas Telefonicas",
        "Sala de Servidores", "Sala de Treinamento", "AFA atendimento", "Outro (T.I)"
    )

    fun isGobah(sector: String): Boolean = GOBAH_OPTIONS.contains(sector)
}
