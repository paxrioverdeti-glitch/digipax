package com.example.pxrioverde.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pxrioverde.model.FeedPost
import com.example.pxrioverde.model.Idea
import com.example.pxrioverde.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: FeedRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _ideas = MutableStateFlow<List<Idea>>(emptyList())
    val ideas = _ideas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 = Mural, 1 = Caixa de Ideias
    val selectedTab = _selectedTab.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    private val likedPostIds = mutableSetOf<String>()
    private val votedIdeaIds = mutableSetOf<String>()

    init {
        loadData()
    }

    fun setTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val fetchedPosts = repository.getFeedPosts()
            val fetchedIdeas = repository.getIdeas()

            _posts.value = fetchedPosts.map { post ->
                post.copy(isLiked = post.id != null && likedPostIds.contains(post.id))
            }

            _ideas.value = fetchedIdeas.map { idea ->
                idea.copy(hasVoted = idea.id != null && votedIdeaIds.contains(idea.id))
            }

            _isLoading.value = false
        }
    }

    fun createPost(
        title: String,
        content: String,
        category: String,
        eventDate: String?,
        authorName: String,
        imageBytes: ByteArray?,
        fileName: String?,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || content.isBlank()) {
            _userMessage.value = "Preencha o título e o conteúdo do comunicado."
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            val success = repository.createFeedPost(
                title = title,
                content = content,
                category = category,
                eventDate = eventDate,
                authorName = authorName,
                imageBytes = imageBytes,
                imageFileName = fileName
            )
            _isUploading.value = false

            if (success) {
                _userMessage.value = "Publicado com sucesso no Mural!"
                loadData()
                onSuccess()
            } else {
                _userMessage.value = "Erro ao publicar no Mural. Tente novamente."
            }
        }
    }

    fun toggleLikePost(post: FeedPost) {
        val postId = post.id ?: return
        val currentLiked = likedPostIds.contains(postId)
        val newCount = if (currentLiked) (post.likesCount - 1).coerceAtLeast(0) else post.likesCount + 1

        if (currentLiked) {
            likedPostIds.remove(postId)
        } else {
            likedPostIds.add(postId)
        }

        _posts.value = _posts.value.map {
            if (it.id == postId) {
                it.copy(likesCount = newCount, isLiked = !currentLiked)
            } else it
        }

        viewModelScope.launch {
            repository.toggleLikePost(postId, newCount)
        }
    }

    fun submitIdea(
        title: String,
        description: String,
        category: String,
        authorId: String,
        authorName: String,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank() || description.isBlank()) {
            _userMessage.value = "Preencha o título e a descrição da sua ideia."
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            val success = repository.submitIdea(
                title = title,
                description = description,
                category = category,
                authorId = authorId,
                authorName = authorName
            )
            _isUploading.value = false

            if (success) {
                _userMessage.value = "Ideia enviada para análise! Obrigado por contribuir."
                loadData()
                onSuccess()
            } else {
                _userMessage.value = "Erro ao enviar sua ideia. Tente novamente."
            }
        }
    }

    fun voteIdea(idea: Idea) {
        val ideaId = idea.id ?: return
        if (votedIdeaIds.contains(ideaId)) return

        votedIdeaIds.add(ideaId)
        val newCount = idea.votesCount + 1

        _ideas.value = _ideas.value.map {
            if (it.id == ideaId) {
                it.copy(votesCount = newCount, hasVoted = true)
            } else it
        }

        viewModelScope.launch {
            repository.voteIdea(ideaId, newCount)
        }
    }

    fun moderateIdea(ideaId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateIdeaStatus(ideaId, newStatus)
            _isLoading.value = false

            if (success) {
                _userMessage.value = "Status da ideia atualizado."
                loadData()
            } else {
                _userMessage.value = "Erro ao atualizar status da ideia."
            }
        }
    }

    fun publishIdea(
        idea: Idea,
        postCategory: String,
        eventDate: String?,
        reviewerId: String,
        imageBytes: ByteArray?,
        imageFileName: String?
    ) {
        if (idea.id == null) {
            _userMessage.value = "Não foi possível publicar uma ideia sem identificador."
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            val success = repository.publishIdea(
                idea,
                postCategory,
                eventDate,
                reviewerId,
                imageBytes,
                imageFileName
            )
            _isUploading.value = false
            if (success) {
                _userMessage.value = "Ideia publicada no Mural com sucesso."
                loadData()
            } else {
                _userMessage.value = "Erro ao publicar a ideia no Mural."
            }
        }
    }
}
