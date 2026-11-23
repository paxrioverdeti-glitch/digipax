package com.example.pxrioverde.repository

import com.example.pxrioverde.model.*
import com.example.pxrioverde.service.GeminiInspectionService
import com.example.pxrioverde.service.getHttpEngine
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.github.jan.supabase.auth.auth
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
    val analyzedAt: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    @SerialName("analysis_failed")
    val analysisFailed: Boolean = false,
    @SerialName("review_status")
    val reviewStatus: String? = null
)

/** Linha de viagem lida pelo admin: tudo opcional, para nenhuma viagem incompleta quebrar a lista. */
@Serializable
private data class AdminTripRow(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("car_id") val carId: String? = null,
    @SerialName("start_time") val startTime: Long? = null,
    @SerialName("end_time") val endTime: Long? = null,
    @SerialName("photo_urls") val photoUrls: List<String>? = null,
    @SerialName("end_photo_urls") val endPhotoUrls: List<String>? = null
)

@Serializable
private data class ProfileSummaryDto(
    val id: String,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

/**
 * Repository de Vistorias de Veículos.
 * O aplicativo do motorista submete as fotos e o gestor da frota visualiza a análise automática.
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

            // Registra a vistoria na hora como "em análise", para o admin já ver as fotos.
            saveReportForAdmin(
                tripId, carId, userId,
                VehicleInspectionResultDto(
                    houveNovoDano = false,
                    resumoGeral = "Análise automática em andamento. Revise as fotos se necessário."
                ),
                analysisFailed = true
            )

            val photoClient = HttpClient(getHttpEngine())
            val initialPhotos: List<ByteArray>
            try {
                initialPhotos = downloadPhotos(photoClient, initialPhotoUrls)
                val finalPhotos = downloadPhotos(photoClient, finalPhotoUrls)

                // A captura do app está na ordem Frente, Traseira, Esquerda, Direita.
                val geminiOrder = listOf(0, 2, 1, 3)
                val analysisResult = geminiService.analyzeInspection(
                    geminiOrder.map(initialPhotos::get),
                    geminiOrder.map(finalPhotos::get)
                )
                val analysis = analysisResult.getOrNull()
                if (analysis == null) {
                    // A IA falhou: registra mesmo assim para o admin ver as fotos e revisar na mao.
                    saveReportForAdmin(
                        tripId, carId, userId,
                        VehicleInspectionResultDto(
                            houveNovoDano = false,
                            resumoGeral = "Análise automática indisponível. Revise as fotos manualmente."
                        ),
                        analysisFailed = true
                    )
                    return Result.failure(analysisResult.exceptionOrNull() ?: Exception("Falha na análise"))
                }

                Result.success(saveReportForAdmin(tripId, carId, userId, analysis).getOrThrow())
            } finally {
                photoClient.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pede ao servidor (Edge Function "analyze-inspection") para analisar a vistoria.
     * O servidor baixa as fotos, chama o serviço configurado e grava o relatório. O app só envia o id
     * da viagem, então as credenciais do serviço não ficam no celular e a análise continua
     * mesmo que o motorista feche o app.
     */
    suspend fun requestServerAnalysis(tripId: String, waitForResult: Boolean = false): Result<Unit> {
        val client = supabase ?: return Result.failure(IllegalStateException("Supabase indisponível."))
        // Nova tentativa automática: se a internet falhar, tenta mais 2 vezes (após 3s e 10s).
        // Erros "definitivos" (sem permissão, viagem sem fotos) não adiantam repetir.
        val retryDelays = listOf(3_000L, 10_000L)
        var lastError: Exception? = null
        for (attempt in 0..retryDelays.size) {
            if (attempt > 0) kotlinx.coroutines.delay(retryDelays[attempt - 1])
            try {
                val token = client.auth.currentAccessTokenOrNull() ?: error("Sessão expirada.")
                val http = HttpClient(getHttpEngine()) {
                    install(io.ktor.client.plugins.HttpTimeout) {
                        // Esperando o resultado, a análise leva até ~2 minutos
                        requestTimeoutMillis = if (waitForResult) 170_000 else 30_000
                        socketTimeoutMillis = if (waitForResult) 170_000 else 30_000
                        connectTimeoutMillis = 15_000
                    }
                }
                try {
                    // O cliente do Supabase guarda o endereço SEM "https://". Sem isso,
                    // o pedido ia para "localhost" (o próprio celular) e nunca chegava ao servidor.
                    val baseUrl = client.supabaseUrl.let { if (it.startsWith("http")) it else "https://$it" }
                    val response = http.post("$baseUrl/functions/v1/analyze-inspection") {
                        header("Authorization", "Bearer $token")
                        header("apikey", client.supabaseKey)
                        contentType(ContentType.Application.Json)
                        setBody(
                            buildJsonObject {
                                put("trip_id", tripId)
                                put("wait", waitForResult)
                            }.toString()
                        )
                    }
                    val code = response.status.value
                    if (response.status.isSuccess()) return Result.success(Unit)
                    val message = "Servidor recusou a análise ($code): ${response.bodyAsText()}"
                    // 502 no modo "esperar" = a IA já tentou e falhou; repetir só gastaria crédito
                    if (code in listOf(400, 401, 403, 404, 422) || (waitForResult && code == 502)) {
                        println("Erro ao pedir análise ao servidor: $message")
                        return Result.failure(IllegalStateException(message))
                    }
                    lastError = IllegalStateException(message)
                } finally {
                    http.close()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
            println("Pedido de análise falhou (tentativa ${attempt + 1}): ${lastError?.message}")
        }
        return Result.failure(lastError ?: IllegalStateException("Falha ao pedir análise."))
    }
    /** Gestor confirma a avaria ("confirmado"), marca falso alarme ("falso_alarme") ou desfaz (null). */
    suspend fun reviewInspection(tripId: String, status: String?): Result<Unit> {
        return try {
            val client = supabase ?: error("Supabase indisponível.")
            client.postgrest.rpc(
                "review_inspection",
                buildJsonObject {
                    put("p_trip_id", tripId)
                    put("p_status", status)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            println("Erro ao revisar vistoria: ${e.message}")
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
        result: VehicleInspectionResultDto,
        analysisFailed: Boolean = false
    ): Result<Boolean> {
        return try {
            val client = supabase ?: error("Supabase indisponível para salvar o relatório.")
            // Função segura no banco: grava como o usuário logado, sem depender das regras de linha.
            client.postgrest.rpc(
                "save_inspection_report",
                buildJsonObject {
                    put("p_trip_id", tripId)
                    put("p_car_id", carId)
                    put("p_houve_novo_dano", result.houveNovoDano)
                    put("p_resumo_geral", result.resumoGeral)
                    put("p_danos", Json.encodeToJsonElement(
                        kotlinx.serialization.builtins.ListSerializer(DanoEncontradoDto.serializer()),
                        result.danosEncontrados
                    ))
                    put("p_analysis_failed", analysisFailed)
                }
            )
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
     * Lista as vistorias para o Painel Administrativo a partir das VIAGENS finalizadas
     * (que guardam as fotos), juntando o relatório da IA quando ele existir.
     * Assim as fotos aparecem mesmo se a análise automática falhar ou ainda não tiver rodado.
     * Se [onlyDamaged] for verdadeiro, mostra só o que precisa de atenção:
     * dano detectado, análise que falhou ou viagem ainda sem análise.
     */
    suspend fun getDamagedReportsForAdmin(onlyDamaged: Boolean = true): List<VehicleDamageReportItem> {
        return try {
            val client = supabase ?: return emptyList()

            // 1. Viagens finalizadas (as fotos ficam aqui)
            // Leitura tolerante: viagens antigas podem ter KM, hora, carro ou motorista vazios.
            // Com o modelo rígido (Trip), UMA viagem assim derrubava a lista inteira.
            val trips = client.from("trips")
                .select(io.github.jan.supabase.postgrest.query.Columns.raw(
                    "id, user_id, car_id, start_time, end_time, photo_urls, end_photo_urls"
                )) {
                    order("end_time", io.github.jan.supabase.postgrest.query.Order.DESCENDING, nullsFirst = false)
                    limit(200)
                }
                .decodeList<AdminTripRow>()
                .filter { it.id != null && (it.photoUrls.orEmpty().isNotEmpty() || it.endPhotoUrls.orEmpty().isNotEmpty()) }
            println("Vistorias para o admin: ${trips.size} viagens com fotos")
            if (trips.isEmpty()) return emptyList()

            val tripIds = trips.mapNotNull { it.id }
            val userIds = trips.mapNotNull { it.userId }.distinct()
            val carIds = trips.mapNotNull { it.carId }.distinct()

            // 2. Busca em lote (3 consultas no total, em vez de 3 por viagem)
            val reportsByTrip = try {
                client.from("inspection_reports")
                    .select { filter { isIn("trip_id", tripIds) } }
                    .decodeList<VehicleInspectionReportPayload>()
                    .associateBy { it.tripId }
            } catch (e: Exception) {
                println("Relatórios de IA indisponíveis, mostrando só as fotos: ${e.message}")
                emptyMap()
            }
            val profilesById = try {
                client.from("profiles")
                    .select { filter { isIn("id", userIds) } }
                    .decodeList<ProfileSummaryDto>()
                    .associateBy { it.id }
            } catch (_: Exception) { emptyMap() }
            val carsById = try {
                client.from("cars")
                    .select { filter { isIn("id", carIds) } }
                    .decodeList<Car>()
                    .associateBy { it.id }
            } catch (_: Exception) { emptyMap() }

            trips.mapNotNull { trip ->
                val report = reportsByTrip[trip.id]
                // Já revisada pelo gestor não precisa mais de atenção
                val needsAttention = report?.reviewStatus == null &&
                    (report == null || report.houveNovoDano || report.analysisFailed)
                if (onlyDamaged && !needsAttention) return@mapNotNull null

                val profile = profilesById[trip.userId]
                val car = carsById[trip.carId]
                VehicleDamageReportItem(
                    id = report?.id ?: trip.id!!,
                    tripId = trip.id!!,
                    carId = trip.carId.orEmpty(),
                    carName = car?.let { "${it.name} ${it.model}".trim() } ?: "Veículo Corporativo",
                    carLicensePlate = car?.licensePlate ?: "",
                    userId = trip.userId.orEmpty(),
                    driverName = profile?.name ?: "Motorista da Frota",
                    driverAvatarUrl = profile?.avatarUrl,
                    houveNovoDano = report?.houveNovoDano ?: false,
                    resumoGeral = report?.resumoGeral
                        ?: "Análise automática ainda não realizada. Revise as fotos manualmente.",
                    danosEncontrados = report?.danosEncontrados ?: emptyList(),
                    initialPhotoUrls = trip.photoUrls.orEmpty().map { toPublicPhotoUrl(client, it) },
                    finalPhotoUrls = trip.endPhotoUrls.orEmpty().map { toPublicPhotoUrl(client, it) },
                    analyzedAt = report?.analyzedAt ?: trip.endTime ?: trip.startTime ?: 0L,
                    needsReview = report == null || report.analysisFailed,
                    reviewStatus = report?.reviewStatus
                )
            }
        } catch (e: Exception) {
            println("Erro ao buscar vistorias para o admin: ${e.message}")
            emptyList()
        }
    }
    /** Viagens antigas podem ter salvo só o caminho do arquivo; transforma em link completo. */
    private fun toPublicPhotoUrl(client: SupabaseClient, raw: String): String {
        val value = raw.trim()
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        return client.storage.from("trip_photos").publicUrl(value.removePrefix("trip_photos/"))
    }
}