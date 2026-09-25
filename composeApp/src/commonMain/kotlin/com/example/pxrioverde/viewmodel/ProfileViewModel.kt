package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.repository.UserPreferencesRepository
import com.example.pxrioverde.service.supabase
import com.example.pxrioverde.util.LocalStorage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock

class ProfileViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _profileImageUrl = MutableStateFlow(LocalStorage.getString("profile_image_url", ""))
    val profileImageUrl = _profileImageUrl.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    val userSector = preferencesRepository.userSector
    val machineName = preferencesRepository.machineName

    fun saveProfileSettings(sector: String, machine: String) {
        preferencesRepository.updatePreferences(sector, machine)
    }

    fun uploadProfilePicture(userId: String, bytes: ByteArray) {
        viewModelScope.launch {
            // Validação de sessão antes de iniciar o upload
            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                println("ERRO: Tentativa de upload sem sessão ativa. O usuário precisa estar logado.")
                return@launch
            }

            _isUploading.value = true
            try {
                // Garantimos que o path seja exatamente o UUID.jpg para bater com a Policy
                val path = "${userId}.jpg"
                val bucket = supabase.storage.from("profiles")
                
                println("DEBUG: Iniciando upload para bucket 'profiles' | Path: $path | Usuário: ${session.user?.id}")
                
                bucket.upload(path, bytes) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }
                
                // Adicionamos um timestamp como parâmetro apenas na URL para "quebrar" o cache da UI
                // sem criar arquivos duplicados no storage
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val url = "${bucket.publicUrl(path)}?t=$timestamp"
                
                // Validação básica da URL gerada
                if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                    // Atualizar tabela profiles
                    supabase.from("profiles").update(
                        mapOf("avatar_url" to url)
                    ) {
                        filter { eq("id", userId) }
                    }

                    // Salvar localmente e atualizar estado
                    LocalStorage.putString("profile_image_url", url)
                    _profileImageUrl.value = url
                } else {
                    println("ERRO: URL pública gerada é inválida: $url")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
    
    fun updateLocalPhoto(url: String) {
        _profileImageUrl.value = url
        LocalStorage.putString("profile_image_url", url)
    }

    fun clearData() {
        _profileImageUrl.value = ""
    }
}
