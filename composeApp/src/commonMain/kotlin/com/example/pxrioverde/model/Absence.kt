package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AbsenceType(val label: String) {
    ATRASO("Atraso"),
    SAIDA_ANTECIPADA("Saída Antecipada"),
    RETIRADA("Retirada"),
    FALTA("Falta"),
    TROCA_HORARIO("Troca de Horário")
}

@Serializable
data class AdminFeedback(
    @SerialName("is_authorized") val isAuthorized: Boolean? = null,
    @SerialName("is_proven") val isProven: Boolean? = null,
    val instructions: List<String> = emptyList() // "Descontar", "Abonar", "Compensar"
)

@Serializable
data class ComunicadoState(
    val id: String? = null,
    @SerialName("userId") val userId: String = "",
    @SerialName("userName") val userName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val sector: String = "",
    @SerialName("targetApproverId") val targetApproverId: String? = null,
    @SerialName("targetApproverName") val targetApproverName: String? = null,
    val type: AbsenceType = AbsenceType.ATRASO,
    val date: String = "",
    @SerialName("expectedTime") val expectedTime: String = "",
    @SerialName("effectiveTime") val effectiveTime: String = "",
    @SerialName("exitTime") val exitTime: String = "",
    @SerialName("returnTime") val returnTime: String = "",
    @SerialName("missingHours") val missingHours: String = "",
    @SerialName("missingDays") val missingDays: String = "",
    @SerialName("originalTime") val originalTime: String = "",
    @SerialName("newTime") val newTime: String = "",
    val reason: String = "",
    @SerialName("adminFeedback") val adminFeedback: AdminFeedback? = null,
    @SerialName("createdAt") val createdAt: Long = 0L
) {
    val userAvatarUrl: String? get() = avatarUrl
}
