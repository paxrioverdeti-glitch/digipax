package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload completo enviado para o endpoint generateContent da API do Gemini.
 */
@Serializable
data class GeminiRequestPayload(
    @SerialName("contents")
    val contents: List<GeminiContent>,
    @SerialName("generationConfig")
    val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig(),
)

/**
 * Conteúdo da requisição multimodal (role user + lista de partes com texto e imagens).
 */
@Serializable
data class GeminiContent(
    @SerialName("role")
    val role: String = "user",
    @SerialName("parts")
    val parts: List<GeminiPart>,
)

/**
 * Parte individual de um conteúdo (pode ser texto ou dados inline/imagem Base64).
 */
@Serializable
data class GeminiPart(
    @SerialName("text")
    val text: String? = null,
    @SerialName("inlineData")
    val inlineData: GeminiInlineData? = null,
)

/**
 * Objeto de dados de mídia inline (imagem codificada em Base64 + MIME type).
 */
@Serializable
data class GeminiInlineData(
    @SerialName("mimeType")
    val mimeType: String = "image/jpeg",
    @SerialName("data")
    val data: String,
)

/**
 * Configurações de geração do Gemini para forçar saída estrita em JSON.
 */
@Serializable
data class GeminiGenerationConfig(
    @SerialName("responseMimeType")
    val responseMimeType: String? = "application/json",
    @SerialName("temperature")
    val temperature: Double? = 0.2,
)

/**
 * Resposta padrão retornada pela API REST do Gemini.
 */
@Serializable
data class GeminiResponse(
    @SerialName("candidates")
    val candidates: List<GeminiCandidate>? = null,
    @SerialName("error")
    val error: GeminiError? = null,
)

/**
 * Candidato de resposta gerado pelo modelo.
 */
@Serializable
data class GeminiCandidate(
    @SerialName("content")
    val content: GeminiResponseContent? = null,
    @SerialName("finishReason")
    val finishReason: String? = null,
)

/**
 * Conteúdo da resposta contendo as partes de texto.
 */
@Serializable
data class GeminiResponseContent(
    @SerialName("parts")
    val parts: List<GeminiResponsePart>? = null,
)

/**
 * Parte de texto na resposta do Gemini.
 */
@Serializable
data class GeminiResponsePart(
    @SerialName("text")
    val text: String? = null,
)

/**
 * Representação de erro retornado pela API do Gemini.
 */
@Serializable
data class GeminiError(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("status")
    val status: String? = null,
)
