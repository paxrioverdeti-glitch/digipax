package com.example.pxrioverde.service

import com.example.pxrioverde.model.User
import com.example.pxrioverde.model.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

class AuthService {
    val sessionStatus = supabase.auth.sessionStatus

    suspend fun login(email: String, password: String): User? {
        return try {
            println("Iniciando login para: $email")

            // 1. Faz o login no Supabase Auth
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            // CORREÇÃO CRUCIAL: Aguarda o estado mudar para Authenticated antes de prosseguir
            println("Aguardando sincronização do estado de autenticação...")
            try {
                withTimeout(5000) { // Timeout de 5 segundos para não travar a UI infinitamente se falhar
                    supabase.auth.sessionStatus.first { status ->
                        status is SessionStatus.Authenticated
                    }
                }
                println("Estado sincronizado! Usuário autenticado com sucesso.")
            } catch (e: Exception) {
                println("Aviso/Erro: Timeout ao aguardar SessionStatus.Authenticated: ${e.message}")
                // Se der timeout mas o ID existir na linha abaixo, ele ainda tenta prosseguir
            }

            // 2. Busca os detalhes do perfil na tabela 'profiles'
            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                println("Erro: Usuário nulo após signIn e sincronização de sessão")
                return null
            }

            println("Login Auth ok, buscando perfil para ID: $userId")

            val profile = try {
                println("Consultando tabela 'profiles' para ID: $userId")
                val response = supabase.from("profiles")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }

                println("Resposta bruta recebida. Decodificando...")
                response.decodeSingle<UserProfileDto>()
            } catch (e: Exception) {
                println("Erro detalhado na busca do perfil: ${e.message}")
                e.printStackTrace()
                throw Exception("Erro ao buscar perfil no banco: ${e.message}")
            }

            User(
                id = profile.id,
                name = profile.name,
                email = profile.email,
                role = try { UserRole.valueOf(profile.role.uppercase()) } catch(e: Exception) { UserRole.CLIENT },
                avatarUrl = profile.avatarUrl
            )
        } catch (e: Exception) {
            println("Falha no processo de login: ${e.message}")
            throw e
        }
    }

    /**
     * Primeiro acesso (Caminho 2 - somente convidado).
     * Cria o usuario no Supabase Auth. O gatilho handle_first_access() no banco
     * confere se o e-mail esta na lista de convidados: se nao estiver, o cadastro
     * e recusado pelo servidor e a excecao sobe com a mensagem apropriada.
     */
    suspend fun signUp(email: String, password: String): User? {
        return try {
            println("Iniciando primeiro acesso para: $email")

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            // Aguarda a sessao sincronizar (quando a confirmacao de e-mail esta desligada,
            // o usuario ja fica autenticado logo apos o signUp).
            try {
                withTimeout(5000) {
                    supabase.auth.sessionStatus.first { status ->
                        status is SessionStatus.Authenticated
                    }
                }
            } catch (e: Exception) {
                println("Aviso: sessao nao autenticou automaticamente apos signUp: ${e.message}")
            }

            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                // Sem sessao ativa (ex.: confirmacao de e-mail ligada). O cadastro
                // foi criado; o usuario deve confirmar o e-mail e depois logar.
                println("Cadastro criado, aguardando confirmacao de e-mail / login.")
                return null
            }

            // Busca o perfil criado automaticamente pelo gatilho.
            val profile = supabase.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingle<UserProfileDto>()

            User(
                id = profile.id,
                name = profile.name,
                email = profile.email,
                role = try { UserRole.valueOf(profile.role.uppercase()) } catch (e: Exception) { UserRole.CLIENT },
                avatarUrl = profile.avatarUrl
            )
        } catch (e: Exception) {
            println("Falha no primeiro acesso: ${e.message}")
            throw e
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            // Aguarda a inicialização do status da sessão
            println("Aguardando inicialização da sessão Supabase...")
            supabase.auth.sessionStatus.filter { it !is SessionStatus.Initializing }.first()
            println("Sessão inicializada. Status: ${supabase.auth.sessionStatus.value}")

            val userId = supabase.auth.currentUserOrNull()?.id ?: run {
                println("Nenhum usuário logado após inicialização.")
                return null
            }
            val profile = try {
                supabase.from("profiles")
                    .select { filter { eq("id", userId) } }
                    .decodeSingle<UserProfileDto>()
            } catch (e: Exception) {
                println("Erro ao buscar perfil atual: ${e.message}")
                return null
            }

            User(
                id = profile.id,
                name = profile.name,
                email = profile.email,
                role = try { UserRole.valueOf(profile.role.uppercase()) } catch(e: Exception) { UserRole.CLIENT },
                avatarUrl = profile.avatarUrl
            )
        } catch (e: Exception) {
            println("Erro em getCurrentUser: ${e.message}")
            null
        }
    }

    fun isUserLoggedIn(): Boolean = supabase.auth.currentSessionOrNull() != null

    suspend fun getApprovers(): List<User> {
        return try {
            val response = supabase.from("profiles")
                .select {
                    filter {
                        eq("role", "ADMIN")
                    }
                }
            
            response.decodeList<UserProfileDto>()
                .filter { profile ->
                    val name = (profile.name ?: "").lowercase()
                    val email = profile.email.lowercase()
                    !name.contains("luiza") && !email.contains("luiza")
                }
                .map { profile ->
                    User(
                        id = profile.id,
                        name = profile.name,
                        email = profile.email,
                        role = try { UserRole.valueOf(profile.role.uppercase()) } catch(e: Exception) { UserRole.CLIENT },
                        avatarUrl = profile.avatarUrl
                    )
                }
        } catch (e: Exception) {
            println("Erro ao buscar aprovadores: ${e.message}")
            emptyList()
        }
    }

    suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            println("Erro ao realizar logout no Supabase: ${e.message}")
        }
    }

    // ---------------------------------------------------------------------
    // GESTAO DE CONVITES (Primeiro acesso - Caminho 2)
    // ---------------------------------------------------------------------

    /** Lista todos os convites (somente ADMIN consegue, por RLS). */
    suspend fun getInvites(): List<AllowedEmailDto> {
        return try {
            supabase.from("allowed_emails")
                .select()
                .decodeList<AllowedEmailDto>()
        } catch (e: Exception) {
            println("Erro ao listar convites: ${e.message}")
            emptyList()
        }
    }

    /** Convida um e-mail (chama a funcao invite_email no banco, que valida se quem chama e ADMIN). */
    suspend fun inviteEmail(email: String, name: String?, role: String = "CLIENT") {
        supabase.postgrest.rpc(
            "invite_email",
            Json.encodeToJsonElement(
                InviteParams.serializer(),
                InviteParams(
                    email = email.trim().lowercase(),
                    name = name?.trim(),
                    role = role
                )
            ).jsonObject
        )
    }

    /** Remove um convite da lista. */
    suspend fun removeInvite(email: String) {
        supabase.from("allowed_emails").delete {
            filter { eq("email", email.lowercase()) }
        }
    }
}

@kotlinx.serialization.Serializable
data class AllowedEmailDto(
    val email: String,
    val name: String? = null,
    val role: String = "CLIENT",
    val used: Boolean = false
)

@kotlinx.serialization.Serializable
data class UserProfileDto(
    val id: String,
    val name: String? = null,
    val email: String,
    val role: String,
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null,
    @kotlinx.serialization.SerialName("createdAt") val createdAt: String? = null
)

@kotlinx.serialization.Serializable
data class InviteParams(
    @kotlinx.serialization.SerialName("p_email") val email: String,
    @kotlinx.serialization.SerialName("p_name") val name: String?,
    @kotlinx.serialization.SerialName("p_role") val role: String
)