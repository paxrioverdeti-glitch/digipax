package com.example.pxrioverde.service

import com.example.pxrioverde.model.*
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.encoding.Base64

/**
 * Serviço responsável pela integração REST com a API do Gemini para
 * análise comparativa multimodal de vistorias de lataria de veículos de frota.
 */
class GeminiInspectionService(
    private val httpClient: HttpClient = HttpClient(getHttpEngine()),
    private val apiKey: String = DEFAULT_API_KEY,
    private val modelName: String = DEFAULT_MODEL
) {

    companion object {
        const val DEFAULT_API_KEY = ""
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val ALTERNATIVE_MODEL = "gemini-1.5-pro"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        /**
         * Prompt estrito contratual conforme especificação do negócio.
         */
        val SYSTEM_PROMPT = """
            Você é um sistema especialista em vistoria veicular automatizada e análise de danos de lataria para frotas corporativas. Compare as fotos iniciais e finais pelos mesmos ângulos, nesta ordem: Frente, Lateral Esquerda, Traseira, Lateral Direita.
            Compare cada foto inicial somente com a foto final do mesmo ângulo. Identifique exclusivamente novos danos visíveis na lataria (amassados, arranhões profundos, trincas ou peças danificadas). Ignore diferenças causadas por iluminação, reflexos, sujeira ou perspectiva; quando não houver evidência visual clara de um dano novo, não o classifique como avaria.
            Retorne ESTRITAMENTE um JSON válido com esta estrutura:
            {
              "houve_novo_dano": boolean,
              "resumo_geral": "string",
              "danos_encontrados": [
                {
                  "angulo": "string",
                  "tipo_dano": "string",
                  "descricao": "string",
                  "gravidade": "Leve | Moderado | Grave"
                }
              ]
            }
        """.trimIndent()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Analisa as vistorias inicial e final recebendo o objeto estruturado [VehicleInspectionPhotos].
     */
    suspend fun analyzeInspection(photos: VehicleInspectionPhotos): Result<VehicleInspectionResultDto> {
        return try {
            val orderedPhotos = photos.toOrderedList()
            if (orderedPhotos.size != 8) {
                return Result.failure(
                    IllegalArgumentException("São necessárias exatamente 8 fotos (4 da vistoria inicial e 4 da vistoria final).")
                )
            }
            executeGeminiAnalysis(orderedPhotos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sobrecarga para receber duas listas de bytes de imagem (4 fotos iniciais e 4 fotos finais).
     */
    suspend fun analyzeInspection(
        initialPhotos: List<ByteArray>,
        finalPhotos: List<ByteArray>
    ): Result<VehicleInspectionResultDto> {
        return try {
            if (initialPhotos.size != 4 || finalPhotos.size != 4) {
                return Result.failure(
                    IllegalArgumentException("Devem ser enviadas exatamente 4 fotos iniciais e 4 fotos finais (Frente, Lateral Esquerda, Traseira, Lateral Direita).")
                )
            }

            val angles = listOf("Frente", "Lateral Esquerda", "Traseira", "Lateral Direita")
            val orderedList = mutableListOf<Pair<String, InspectionImage>>()

            for (i in 0..3) {
                orderedList.add("Vistoria Inicial - ${angles[i]}" to InspectionImage(byteArray = initialPhotos[i]))
            }
            for (i in 0..3) {
                orderedList.add("Vistoria Final - ${angles[i]}" to InspectionImage(byteArray = finalPhotos[i]))
            }

            executeGeminiAnalysis(orderedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sobrecarga para receber duas listas de Strings Base64 (4 fotos iniciais e 4 fotos finais).
     */
    suspend fun analyzeInspectionBase64(
        initialPhotosBase64: List<String>,
        finalPhotosBase64: List<String>
    ): Result<VehicleInspectionResultDto> {
        return try {
            if (initialPhotosBase64.size != 4 || finalPhotosBase64.size != 4) {
                return Result.failure(
                    IllegalArgumentException("Devem ser enviadas exatamente 4 fotos iniciais e 4 fotos finais em Base64.")
                )
            }

            val angles = listOf("Frente", "Lateral Esquerda", "Traseira", "Lateral Direita")
            val orderedList = mutableListOf<Pair<String, InspectionImage>>()

            for (i in 0..3) {
                orderedList.add("Vistoria Inicial - ${angles[i]}" to InspectionImage(base64Data = initialPhotosBase64[i]))
            }
            for (i in 0..3) {
                orderedList.add("Vistoria Final - ${angles[i]}" to InspectionImage(base64Data = finalPhotosBase64[i]))
            }

            executeGeminiAnalysis(orderedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Constrói o payload multimodal Ktor e executa a chamada REST para o Gemini.
     */
    private suspend fun executeGeminiAnalysis(
        orderedPhotos: List<Pair<String, InspectionImage>>
    ): Result<VehicleInspectionResultDto> {
        return try {
            val parts = mutableListOf<GeminiPart>()

            // 1. Inclui o Prompt do Sistema no início do payload
            parts.add(GeminiPart(text = SYSTEM_PROMPT))

            // 2. Anexa cada foto com sua respectiva descrição textual no payload multimodal
            orderedPhotos.forEach { (description, image) ->
                val base64Data = convertImageToBase64(image)
                if (base64Data.isNotBlank()) {
                    parts.add(GeminiPart(text = "--- Foto: $description ---"))
                    parts.add(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = image.mimeType,
                                data = base64Data
                            )
                        )
                    )
                }
            }

            val requestPayload = GeminiRequestPayload(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = parts
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.1
                )
            )

            val endpointUrl = "$BASE_URL/$modelName:generateContent?key=$apiKey"
            val requestBodyJson = json.encodeToString(GeminiRequestPayload.serializer(), requestPayload)

            val httpResponse = httpClient.post(endpointUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBodyJson)
            }

            val responseBodyText = httpResponse.bodyAsText()

            if (!httpResponse.status.isSuccess()) {
                val errorDetails = parseGeminiError(responseBodyText)
                return Result.failure(
                    Exception("Falha na chamada Gemini API (${httpResponse.status.value}): $errorDetails")
                )
            }

            // 3. Processa e decodifica a resposta JSON do Gemini
            val geminiResponse = json.decodeFromString(GeminiResponse.serializer(), responseBodyText)

            if (geminiResponse.error != null) {
                return Result.failure(
                    Exception("Erro retornado pela Gemini API: ${geminiResponse.error.message}")
                )
            }

            val rawJsonText = geminiResponse.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()
                ?.text

            if (rawJsonText.isNullOrBlank()) {
                return Result.failure(
                    Exception("A resposta da API do Gemini veio vazia ou em formato inesperado.")
                )
            }

            val cleanedJsonText = cleanJsonMarkdown(rawJsonText)
            val inspectionResult = json.decodeFromString(
                VehicleInspectionResultDto.serializer(),
                cleanedJsonText
            )

            Result.success(inspectionResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Utilitário para converter a imagem em String Base64 limpa.
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun convertImageToBase64(image: InspectionImage): String {
        return when {
            !image.base64Data.isNullOrBlank() -> sanitizeBase64(image.base64Data)
            image.byteArray != null -> Base64.encode(image.byteArray)
            else -> ""
        }
    }

    /**
     * Remove prefixos data:image/...;base64, se existirem na String Base64.
     */
    private fun sanitizeBase64(rawBase64: String): String {
        return if (rawBase64.contains(",")) {
            rawBase64.substringAfter(",")
        } else {
            rawBase64
        }
    }

    /**
     * Remove marcações Markdown tipo ```json ... ``` se o Gemini as incluir no retorno.
     */
    private fun cleanJsonMarkdown(rawJson: String): String {
        var cleaned = rawJson.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("\n")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substringBeforeLast("```")
        }
        return cleaned.trim()
    }

    /**
     * Tenta realizar o parse da resposta de erro do Gemini.
     */
    private fun parseGeminiError(responseBody: String): String {
        return try {
            val errorResponse = json.decodeFromString(GeminiResponse.serializer(), responseBody)
            errorResponse.error?.message ?: responseBody
        } catch (_: Exception) {
            responseBody
        }
    }
}
