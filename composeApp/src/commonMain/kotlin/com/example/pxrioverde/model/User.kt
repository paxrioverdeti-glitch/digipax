package com.example.pxrioverde.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class UserRole {
    @SerialName("ADMIN") ADMIN,
    @SerialName("SUPERVISOR") SUPERVISOR,
    @SerialName("ENCARREGADO") ENCARREGADO,
    @SerialName("CLIENT") CLIENT
}

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val email: String,
    val role: UserRole = UserRole.CLIENT,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("createdAt") val createdAt: String? = null
) {
    val displayName: String get() = name ?: email.substringBefore("@")
}

@Serializable
data class Profiles(
    @SerialName("avatar_url") val avatarUrl: String? = null
)
