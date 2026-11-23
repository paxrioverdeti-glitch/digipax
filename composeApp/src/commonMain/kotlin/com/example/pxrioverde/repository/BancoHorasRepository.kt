package com.example.pxrioverde.repository

import com.example.pxrioverde.model.BancoHoras
import com.example.pxrioverde.model.BancoHorasErro
import com.example.pxrioverde.service.getHttpEngine
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * Consulta o saldo do banco de horas na Plataforma DP (Edge Function banco-horas-app).
 * O app nunca fala com o Sólides. Nada é gravado: nem saldo, nem CPF, nem token, nem log.
 */
class BancoHorasRepository(
    private val supabase: SupabaseClient
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    suspend fun consultar(): Result<BancoHoras> {
        val token = supabase.auth.currentSessionOrNull()?.accessToken
            ?: return Result.failure(BancoHorasErro("Sua sessão expirou. Saia e entre de novo no aplicativo."))

        val client = HttpClient(getHttpEngine()) {
            install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        }
        return try {
            val resposta = client.post(URL) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val corpo = resposta.bodyAsText()
            when (resposta.status.value) {
                200 -> try {
                    Result.success(json.decodeFromString<BancoHoras>(corpo))
                } catch (e: kotlinx.serialization.SerializationException) {
                    Result.failure(BancoHorasErro("Recebemos uma resposta inesperada. Tente de novo em instantes."))
                }
                401 -> Result.failure(BancoHorasErro("Sua sessão expirou. Saia e entre de novo no aplicativo."))
                429 -> Result.failure(BancoHorasErro("Muitas consultas seguidas. Espere um pouco e tente de novo."))
                in 500..599 -> Result.failure(BancoHorasErro("O serviço do banco de horas está indisponível no momento. Tente de novo em alguns minutos. Se continuar, avise o DP."))
                else -> Result.failure(BancoHorasErro("Não conseguimos buscar seu saldo agora. Tente de novo em instantes."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sem detalhes da exceção: pode conter dados da resposta.
            Result.failure(BancoHorasErro("Sem conexão com a internet. Confira o sinal e tente de novo."))
        } finally {
            client.close()
        }
    }

    private companion object {
        // Host com https:// obrigatório (sem o esquema o celular chamaria localhost).
        const val URL = "https://efjunpluofjgnkahuvff.supabase.co/functions/v1/banco-horas-app"
    }
}
