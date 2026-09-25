package com.example.pxrioverde.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedPost(
    val id: String? = null,
    val title: String,
    val content: String,
    val category: String = "COMUNICADO", // "EVENTO", "COMUNICADO", "NOTICIA"
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("author_name") val authorName: String = "Comunicação Interna",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val isLiked: Boolean = false
)

@Serializable
data class Idea(
    val id: String? = null,
    val title: String,
    val description: String,
    val category: String = "MELHORIA", // "MELHORIA", "INOVACAO", "BEM_ESTAR", "OUTROS"
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String,
    val status: String = "PENDENTE", // "PENDENTE", "APROVADO", "REJEITADO"
    @SerialName("votes_count") val votesCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("published_post_id") val publishedPostId: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val hasVoted: Boolean = false
)

object FeedConstants {
    val POST_CATEGORIES = listOf("COMUNICADO", "EVENTO", "NOTICIA")
    val IDEA_CATEGORIES = listOf("MELHORIA", "INOVACAO", "BEM_ESTAR", "OUTROS")
}
