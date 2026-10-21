package com.example.pxrioverde.repository

import com.example.pxrioverde.model.*
import com.example.pxrioverde.service.GeminiInspectionService
import com.example.pxrioverde.service.getHttpEngine
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Entidade para persistência do relatório de vistoria no Supabase/Banco de Dados para acesso dos gestores.
 */
@Serializable
data class VehicleInspectionReportPayload(
    @SerialName("id")
    val id: String? = null,
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("car_id")
    val carId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("houve_novo_dano")
    val houveNovoDano: Boolean,
    @SerialName("resumo_geral")
    val resumoGeral: String,
    @SerialName("danos_encontrados")
    val danosEncontrados: List<DanoEncontradoDto> = emptyList(),
    @SerialName("analyzed_at")
    val analyzedAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
)

@Serializable
private data class ProfileSummaryDto(
    val id: String,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

/**
 * Repository de Vistorias de Veículos.
 * O aplicativo do motorista submete as fotos e o gestor da frota visualiza a análise gerada pelo Gemini.
 */
class VehicleInspectionRepository(
    private val geminiService: GeminiInspectionService,
    private val supabase: SupabaseClient? = null
) {

    /**
     * Solicita a análise automatizada das 8 fotos da vistoria usando o Gemini.
     */
    suspend fun analyzeInspection(photos: VehicleInspectionPhotos): Result<VehicleInspectionResultDto> {
        return geminiService.analyzeInspection(photos)
    }

    /**
     * Sobrecarga para receber listas de foto em ByteArray (4 iniciais + 4 finais).
     */
    suspend fun analyzeInspection(
        initialPhotos: List<ByteArray>,
        finalPhotos: List<ByteArray>
    ): Result<VehicleInspectionResultDto> {
        return geminiService.analyzeInspection(initialPhotos, finalPhotos)
    }

    /**
     * Sobrecarga para receber listas de foto em Base64 (4 iniciais + 4 finais).
     */
    suspend fun analyzeInspectionBase64(
        initialPhotosBase64: List<String>,
        finalPhotosBase64: List<String>
    ): Result<VehicleInspectionResultDto> {
        return geminiService.analyzeInspectionBase64(initialPhotosBase64, finalPhotosBase64)
    }

    suspend fun analyzeAndSaveReport(
        tripId: String,
        carId: String,
        userId: String,
        initialPhotoUrls: List<String>,
        finalPhotoUrls: List<String>
    ): Result<Boolean> {
        return try {
            require(initialPhotoUrls.size == 4 && finalPhotoUrls.size == 4) {
                "A análise exige 4 fotos iniciais e 4 fotos finais."
            }

            val photoClient = HttpClient(getHttpEngine())
            val initialPhotos: List<ByteArray>
            try {
                initialPhotos = downloadPhotos(photoClient, initialPhotoUrls)
                val finalPhotos = downloadPhotos(photoClient, finalPhotoUrls)

                // A captura do app está na ordem Frente, Traseira, Esquerda, Direita.
                val geminiOrder = listOf(0, 2, 1, 3)
                val analysis = geminiService.analyzeInspection(
                    geminiOrder.map(initialPhotos::get),
                    geminiOrder.map(finalPhotos::get)
                ).getOrThrow()

                Result.success(saveReportForAdmin(tripId, carId, userId, analysis).getOrThrow())
            } finally {
                photoClient.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun downloadPhotos(client: HttpClient, urls: List<String>): List<ByteArray> {
        return coroutineScope {
            urls.map { url ->
                async {
                    val response = client.get(url)
                    if (!response.status.isSuccess()) {
                        val details = response.bodyAsText()
                        throw IllegalStateException(
                            "Não foi possível baixar uma foto da vistoria (${response.status.value}): $details"
                        )
                    }

                    response.bodyAsBytes().also { bytes ->
                        require(bytes.isNotEmpty()) { "Uma foto da vistoria foi baixada vazia." }
                        require(bytes.size <= 10 * 1024 * 1024) {
                            "Uma foto da vistoria excede o limite de 10 MB."
                        }
                    }
                }
            }.awaitAll()
        }
    }

    /**
     * Salva o relatório de vistoria para acesso exclusivo da área administrativa / gestores de frota.
     */
    suspend fun saveReportForAdmin(
        tripId: String,
        carId: String,
        userId: String,
        result: VehicleInspectionResultDto
    ): Result<Boolean> {
        return try {
            val client = supabase ?: error("Supabase indisponível para salvar o relatório.")
            val payload = VehicleInspectionReportPayload(
                tripId = tripId,
                carId = carId,
                userId = userId,
                houveNovoDano = result.houveNovoDano,
                resumoGeral = result.resumoGeral,
                danosEncontrados = result.danosEncontrados
            )
            client.from("inspection_reports").upsert(payload) {
                onConflict = "trip_id"
                ignoreDuplicates = true
            }
            Result.success(true)
        } catch (e: Exception) {
            println("Erro ao salvar relatório de vistoria: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Busca relatórios de vistoria cadastrados para visualização do gestor (Admin).
     */
    suspend fun getReportForAdmin(tripId: String): VehicleInspectionReportPayload? {
        return try {
            val client = supabase ?: return null
            client.from("inspection_reports")
                .select {
                    filter {
                        eq("trip_id", tripId)
                    }
                }
                .decodeSingleOrNull<VehicleInspectionReportPayload>()
        } catch (e: Exception) {
            println("Erro ao buscar relatório de vistoria para o admin: ${e.message}")
            null
        }
    }

    /**
     * Busca a lista de vistorias com relatórios de avaria para o Painel Administrativo.
     * Se [onlyDamaged] for verdadeiro, filtra estritamente por `houve_novo_dano == true`.
     */
    suspend fun getDamagedReportsForAdmin(onlyDamaged: Boolean = true): List<VehicleDamageReportItem> {
        return try {
            val client = supabase ?: return emptyList()
            
            val payloads = client.from("inspection_reports")
                .select {
                    filter {
                        if (onlyDamaged) {
                            eq("houve_novo_dano", true)
                        }
                    }
                    order("analyzed_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<VehicleInspectionReportPayload>()

            payloads.map { payload ->
                // Busca perfil do motorista para enriquecer o relatório
                var driverName = "Motorista da Frota"
                var driverAvatar: String? = null
                try {
                    val profile = client.from("profiles")
                        .select { filter { eq("id", payload.userId) } }
                        .decodeSingleOrNull<ProfileSummaryDto>()
                    if (profile != null) {
                        driverName = profile.name ?: "Motorista da Frota"
                        driverAvatar = profile.avatarUrl
                    }
                } catch (_: Exception) {}

                // Busca dados do veículo
                var carName = "Veículo Corporativo"
                var carPlate = ""
                try {
                    val car = client.from("cars")
                        .select { filter { eq("id", payload.carId) } }
                        .decodeSingleOrNull<Car>()
                    if (car != null) {
                        carName = "${car.name} ${car.model}".trim()
                        carPlate = car.licensePlate
                    }
                } catch (_: Exception) {}

                // Busca fotos da viagem
                var initialPhotos: List<String> = emptyList()
                var finalPhotos: List<String> = emptyList()
                try {
                    val trip = client.from("trips")
                        .select { filter { eq("id", payload.tripId) } }
                        .decodeSingleOrNull<Trip>()
                    if (trip != null) {
                        initialPhotos = trip.photoUrls
                        finalPhotos = trip.endPhotoUrls
                    }
                } catch (_: Exception) {}

                VehicleDamageReportItem(
                    id = payload.id ?: payload.tripId,
                    tripId = payload.tripId,
                    carId = payload.carId,
                    carName = carName,
                    carLicensePlate = carPlate,
                    userId = payload.userId,
                    driverName = driverName,
                    driverAvatarUrl = driverAvatar,
                    houveNovoDano = payload.houveNovoDano,
                    resumoGeral = payload.resumoGeral,
                    danosEncontrados = payload.danosEncontrados,
                    initialPhotoUrls = initialPhotos,
                    finalPhotoUrls = finalPhotos,
                    analyzedAt = payload.analyzedAt
                )
            }
        } catch (e: Exception) {
            println("Erro ao buscar relatórios de avarias para o admin: ${e.message}")
            emptyList()
        }
    }
}
