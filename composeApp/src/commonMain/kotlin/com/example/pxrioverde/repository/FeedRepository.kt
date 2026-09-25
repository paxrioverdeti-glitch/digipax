package com.example.pxrioverde.repository

import com.example.pxrioverde.model.FeedPost
import com.example.pxrioverde.model.Idea
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class FeedRepository(
    private val supabase: SupabaseClient
) {
    suspend fun getFeedPosts(): List<FeedPost> = withContext(Dispatchers.Default) {
        try {
            supabase.from("feed_posts")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FeedPost>()
        } catch (e: Exception) {
            println("FeedRepository: Erro ao buscar posts: ${e.message}")
            emptyList()
        }
    }

    suspend fun createFeedPost(
        title: String,
        content: String,
        category: String,
        eventDate: String?,
        authorName: String,
        imageBytes: ByteArray?,
        imageFileName: String?
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            var imageUrl: String? = null
            if (imageBytes != null && !imageFileName.isNullOrBlank()) {
                val timestamp = Clock.System.now().toEpochMilliseconds()
                val path = "post_${timestamp}_$imageFileName"
                val bucket = supabase.storage.from("feed_images")
                bucket.upload(path, imageBytes)
                imageUrl = bucket.publicUrl(path)
            }

            val post = FeedPost(
                title = title,
                content = content,
                category = category,
                eventDate = eventDate,
                authorName = authorName,
                imageUrl = imageUrl
            )

            supabase.from("feed_posts").insert(post)
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao criar post: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleLikePost(postId: String, newLikesCount: Int): Boolean = withContext(Dispatchers.Default) {
        try {
            supabase.from("feed_posts").update(
                mapOf("likes_count" to newLikesCount)
            ) {
                filter {
                    eq("id", postId)
                }
            }
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao curtir post: ${e.message}")
            false
        }
    }

    suspend fun getIdeas(): List<Idea> = withContext(Dispatchers.Default) {
        try {
            supabase.from("ideas")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Idea>()
        } catch (e: Exception) {
            println("FeedRepository: Erro ao buscar ideias: ${e.message}")
            emptyList()
        }
    }

    suspend fun submitIdea(
        title: String,
        description: String,
        category: String,
        authorId: String,
        authorName: String
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            val idea = Idea(
                title = title,
                description = description,
                category = category,
                authorId = authorId,
                authorName = authorName,
                status = "PENDENTE"
            )
            supabase.from("ideas").insert(idea)
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao enviar ideia: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun voteIdea(ideaId: String, newVotesCount: Int): Boolean = withContext(Dispatchers.Default) {
        try {
            supabase.from("ideas").update(
                mapOf("votes_count" to newVotesCount)
            ) {
                filter {
                    eq("id", ideaId)
                }
            }
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao votar na ideia: ${e.message}")
            false
        }
    }

    suspend fun updateIdeaStatus(ideaId: String, status: String): Boolean = withContext(Dispatchers.Default) {
        try {
            supabase.from("ideas").update(
                mapOf("status" to status)
            ) {
                filter {
                    eq("id", ideaId)
                }
            }
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao atualizar status da ideia: ${e.message}")
            false
        }
    }

    suspend fun publishIdea(
        idea: Idea,
        postCategory: String,
        eventDate: String?,
        reviewerId: String,
        imageBytes: ByteArray?,
        imageFileName: String?
    ): Boolean = withContext(Dispatchers.Default) {
        try {
            val imageUrl = if (imageBytes != null && !imageFileName.isNullOrBlank()) {
                val path = "post_${Clock.System.now().toEpochMilliseconds()}_$imageFileName"
                val bucket = supabase.storage.from("feed_images")
                bucket.upload(path, imageBytes)
                bucket.publicUrl(path)
            } else {
                null
            }

            val post = supabase.from("feed_posts").insert(
                FeedPost(
                    title = idea.title,
                    content = idea.description,
                    category = postCategory,
                    eventDate = eventDate,
                    authorName = idea.authorName,
                    imageUrl = imageUrl
                )
            ) {
                select()
            }.decodeSingle<FeedPost>()

            supabase.from("ideas").update(
                mapOf(
                    "status" to "APROVADO",
                    "published_post_id" to post.id,
                    "published_at" to Clock.System.now().toString(),
                    "reviewed_by" to reviewerId,
                    "reviewed_at" to Clock.System.now().toString()
                )
            ) {
                filter { eq("id", idea.id ?: error("A ideia não possui ID")) }
            }
            true
        } catch (e: Exception) {
            println("FeedRepository: Erro ao publicar ideia no mural: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
