package com.example.pxrioverde.service

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import com.example.pxrioverde.util.LocalStorage
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.isSuccess
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.seconds

/**
 * Fábrica de Engine HTTP para cada plataforma.
 */
expect fun getHttpEngine(): HttpClientEngine

/**
 * Configuração otimizada do Cliente Supabase.
 */
val supabase = createSupabaseClient(
    supabaseUrl = "https://yfkpqeeoticvajgitkrw.supabase.co",
    supabaseKey = "sb_publishable_TZWG8dXcxpUty4steup3jQ_UfQeYqr4"
) {
    httpEngine = getHttpEngine()
    requestTimeout = 120.seconds

    @OptIn(SupabaseInternal::class)
    httpConfig {
        install(WebSockets)
        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, response -> !response.status.isSuccess() }
            retryOnExceptionIf { _, cause ->
                cause is ConnectTimeoutException || cause.message?.contains("host") == true
            }
            delayMillis { retry -> retry * 2000L }
        }
    }
    
    // Otimização do Serializador JSON global
    val optimizedJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        useAlternativeNames = false // Melhora performance de parsing
        coerceInputValues = true
    }

    install(Postgrest)
    install(Realtime) {
        heartbeatInterval = 15.seconds
    }
    install(Auth) {
        sessionManager = LocalStorageSessionManager(optimizedJson)
        autoLoadFromStorage = true
        autoSaveToStorage = true
        alwaysAutoRefresh = true
    }
    install(Storage)
}

/**
 * Gerenciador de Sessão Customizado.
 */
class LocalStorageSessionManager(private val json: Json) : SessionManager {
    private val key = "supabase_session"
    private val rememberMeKey = "rememberMe"
    
    // Fallback em memória caso o usuário não queira persistir
    private var memorySession: UserSession? = null

    override suspend fun saveSession(session: UserSession) {
        val shouldRemember = LocalStorage.getBoolean(rememberMeKey, false)
        
        if (shouldRemember) {
            try {
                val sessionJson = json.encodeToString(session)
                LocalStorage.putString(key, sessionJson)
            } catch (e: Exception) {
                println("Erro ao persistir sessão: ${e.message}")
            }
        } else {
            // Se não é para lembrar, limpamos o disco por segurança e guardamos só em memória
            LocalStorage.putString(key, "")
            memorySession = session
        }
    }

    override suspend fun loadSession(): UserSession? {
        val shouldRemember = LocalStorage.getBoolean(rememberMeKey, false)
        
        if (!shouldRemember) return memorySession

        val sessionStr = LocalStorage.getString(key, "")
        return if (sessionStr.isNotEmpty()) {
            try {
                json.decodeFromString<UserSession>(sessionStr)
            } catch (e: Exception) {
                println("Erro ao carregar sessão persistida: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    override suspend fun deleteSession() {
        LocalStorage.putString(key, "")
        memorySession = null
    }
}
