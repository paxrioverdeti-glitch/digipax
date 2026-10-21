package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO que representa o resultado da análise de vistoria veicular retornado pela API do Gemini.
 */
@Serializable
data class VehicleInspectionResultDto(
    @SerialName("houve_novo_dano")
    val houveNovoDano: Boolean = false,

    @SerialName("resumo_geral")
    val resumoGeral: String = "",

    @SerialName("danos_encontrados")
    val danosEncontrados: List<DanoEncontradoDto> = emptyList(),
)

/**
 * DTO que detalha cada dano individual identificado na lataria do veículo.
 */
@Serializable
data class DanoEncontradoDto(
    @SerialName("angulo")
    val angulo: String = "",

    @SerialName("tipo_dano")
    val tipoDano: String = "",

    @SerialName("descricao")
    val descricao: String = "",

    @SerialName("gravidade")
    val gravidade: String = "Leve", // "Leve" | "Moderado" | "Grave"
)

/**
 * Item consolidado exibido na Aba Administrativa de Relatório de Avarias do Gestor de Frota.
 */
@Serializable
data class VehicleDamageReportItem(
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("car_id")
    val carId: String = "",
    @SerialName("car_name")
    val carName: String = "Veículo de Frota",
    @SerialName("car_license_plate")
    val carLicensePlate: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("driver_name")
    val driverName: String = "Motorista",
    @SerialName("driver_avatar_url")
    val driverAvatarUrl: String? = null,
    @SerialName("houve_novo_dano")
    val houveNovoDano: Boolean = true,
    @SerialName("resumo_geral")
    val resumoGeral: String = "",
    @SerialName("danos_encontrados")
    val danosEncontrados: List<DanoEncontradoDto> = emptyList(),
    @SerialName("initial_photo_urls")
    val initialPhotoUrls: List<String> = emptyList(),
    @SerialName("final_photo_urls")
    val finalPhotoUrls: List<String> = emptyList(),
    @SerialName("analyzed_at")
    val analyzedAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
)

/**
 * Ângulos das fotos obrigatórias exigidos na vistoria de veículos de frota.
 */
enum class InspectionAngle(val label: String) {
    FRONT("Frente"),
    LEFT_SIDE("Lateral Esquerda"),
    REAR("Traseira"),
    RIGHT_SIDE("Lateral Direita"),
}

/**
 * Encapsula os dados de uma imagem de vistoria.
 * Aceita ByteArray bruto, String Base64 pronta, ou URL.
 */
data class InspectionImage(
    val byteArray: ByteArray? = null,
    val base64Data: String? = null,
    val mimeType: String = "image/jpeg",
    val url: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InspectionImage) return false
        if (byteArray != null) {
            if (other.byteArray == null) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
        } else if (other.byteArray != null) return false
        if (base64Data != other.base64Data) return false
        if (mimeType != other.mimeType) return false
        return url == other.url
    }

    override fun hashCode(): Int {
        var result = byteArray?.contentHashCode() ?: 0
        result = 31 * result + (base64Data?.hashCode() ?: 0)
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        return result
    }
}

/**
 * Agrupa as 8 fotos necessárias para a análise comparativa:
 * 4 da vistoria inicial e 4 da vistoria final (Frente, Lateral Esquerda, Traseira, Lateral Direita).
 */
data class VehicleInspectionPhotos(
    val initialFront: InspectionImage,
    val initialLeftSide: InspectionImage,
    val initialRear: InspectionImage,
    val initialRightSide: InspectionImage,
    val finalFront: InspectionImage,
    val finalLeftSide: InspectionImage,
    val finalRear: InspectionImage,
    val finalRightSide: InspectionImage,
) {
    fun toInitialList(): List<Pair<InspectionAngle, InspectionImage>> = listOf(
        InspectionAngle.FRONT to initialFront,
        InspectionAngle.LEFT_SIDE to initialLeftSide,
        InspectionAngle.REAR to initialRear,
        InspectionAngle.RIGHT_SIDE to initialRightSide,
    )

    fun toFinalList(): List<Pair<InspectionAngle, InspectionImage>> = listOf(
        InspectionAngle.FRONT to initialFront,
        InspectionAngle.LEFT_SIDE to initialLeftSide,
        InspectionAngle.REAR to initialRear,
        InspectionAngle.RIGHT_SIDE to initialRightSide,
    )

    fun toOrderedList(): List<Pair<String, InspectionImage>> = listOf(
        "Vistoria Inicial - Frente" to initialFront,
        "Vistoria Inicial - Lateral Esquerda" to initialLeftSide,
        "Vistoria Inicial - Traseira" to initialRear,
        "Vistoria Inicial - Lateral Direita" to initialRightSide,
        "Vistoria Final - Frente" to finalFront,
        "Vistoria Final - Lateral Esquerda" to finalLeftSide,
        "Vistoria Final - Traseira" to finalRear,
        "Vistoria Final - Lateral Direita" to finalRightSide,
    )
}
