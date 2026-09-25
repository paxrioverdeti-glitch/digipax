package com.example.pxrioverde.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.pxrioverde.model.FeedConstants
import com.example.pxrioverde.model.FeedPost
import com.example.pxrioverde.model.Idea
import com.example.pxrioverde.model.User
import com.example.pxrioverde.model.UserRole
import com.example.pxrioverde.ui.components.CorporateIcons
import com.example.pxrioverde.ui.components.StrategicEmptyState
import com.example.pxrioverde.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuralScreen(
    user: User,
    viewModel: FeedViewModel,
    onBack: () -> Unit = {}
) {
    val posts by viewModel.posts.collectAsState()
    val ideas by viewModel.ideas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val isAdmin = user.role == UserRole.ADMIN

    var selectedPostCategory by remember { mutableStateOf("TODOS") }
    var selectedIdeaCategory by remember { mutableStateOf("TODAS") }
    var selectedIdeaSort by remember { mutableStateOf("RECENTES") }

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showSubmitIdeaDialog by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<FeedPost?>(null) }
    var ideaToPublish by remember { mutableStateOf<Idea?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7F6),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = Color(0xFF1B4332), shadowElevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Mural Pax Rio Verde",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Eventos, Comunicados & Ideias",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    CorporateIcons.Back,
                                    contentDescription = "Voltar",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Tab Selector
                    SecondaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1B4332),
                        contentColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setTab(0) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        CorporateIcons.Mural,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mural", fontWeight = FontWeight.Bold)
                                }
                            },
                            selectedContentColor = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.6f)
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setTab(1) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        CorporateIcons.Idea,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Caixa de Ideias", fontWeight = FontWeight.Bold)
                                }
                            },
                            selectedContentColor = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = Color(0xFF2D6A4F),
                    contentColor = Color.White,
                    icon = { Icon(CorporateIcons.Mural, null) },
                    text = { Text("Novo Comunicado", fontWeight = FontWeight.Bold) }
                )
            } else if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showSubmitIdeaDialog = true },
                    containerColor = Color(0xFF2D6A4F),
                    contentColor = Color.White,
                    icon = { Icon(CorporateIcons.Idea, null) },
                    text = { Text("+ Enviar Ideia", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1B4332))
                }
            } else {
                if (selectedTab == 0) {
                    // TAB 0: MURAL
                    val filteredPosts = remember(posts, selectedPostCategory) {
                        if (selectedPostCategory == "TODOS") posts
                        else posts.filter { it.category.equals(selectedPostCategory, ignoreCase = true) }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Filtros de Categoria
                        val postFilterColors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2D6A4F),
                            selectedLabelColor = Color.White
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedPostCategory == "TODOS",
                                    onClick = { selectedPostCategory = "TODOS" },
                                    label = { Text("Todos") },
                                    colors = postFilterColors
                                )
                            }
                            items(FeedConstants.POST_CATEGORIES) { cat ->
                                FilterChip(
                                    selected = selectedPostCategory == cat,
                                    onClick = { selectedPostCategory = cat },
                                    label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }) },
                                    colors = postFilterColors
                                )
                            }
                        }

                        if (filteredPosts.isEmpty()) {
                            StrategicEmptyState(
                                title = "Nenhum comunicado encontrado",
                                description = "Fique atento! Em breve novos avisos e eventos serão publicados aqui.",
                                icon = CorporateIcons.Mural,
                                buttonText = "Atualizar",
                                onButtonClick = { viewModel.loadData() }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredPosts) { post ->
                                    FeedPostCard(
                                        post = post,
                                        onLikeClick = { viewModel.toggleLikePost(post) },
                                        onClick = { selectedPostForDetail = post }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: CAIXA DE IDEIAS
                    val filteredIdeas = remember(ideas, selectedIdeaCategory, selectedIdeaSort, user.id) {
                        val categorizedIdeas = if (selectedIdeaCategory == "TODAS") ideas
                        else if (selectedIdeaCategory == "MINHAS") ideas.filter { it.authorId == user.id }
                        else ideas.filter { it.category.equals(selectedIdeaCategory, ignoreCase = true) }

                        when (selectedIdeaSort) {
                            "VOTADAS" -> categorizedIdeas.sortedWith(
                                compareByDescending<Idea> { it.votesCount }
                                    .thenByDescending { it.createdAt ?: "" }
                            )
                            else -> categorizedIdeas.sortedByDescending { it.createdAt ?: "" }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Banner Incentivo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            CorporateIcons.Idea,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "Sua ideia transforma a Pax Rio Verde!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Compartilhe sugestões de melhorias, inovação e bem-estar para nossa equipe.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Filtros de Categoria de Ideias
                        val ideaFilterColors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2D6A4F),
                            selectedLabelColor = Color.White
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedIdeaCategory == "TODAS",
                                    onClick = { selectedIdeaCategory = "TODAS" },
                                    label = { Text("Todas") },
                                    colors = ideaFilterColors
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedIdeaCategory == "MINHAS",
                                    onClick = { selectedIdeaCategory = "MINHAS" },
                                    label = { Text("Minhas Ideias") },
                                    colors = ideaFilterColors
                                )
                            }
                            items(FeedConstants.IDEA_CATEGORIES) { cat ->
                                FilterChip(
                                    selected = selectedIdeaCategory == cat,
                                    onClick = { selectedIdeaCategory = cat },
                                    label = {
                                        Text(
                                            when (cat) {
                                                "BEM_ESTAR" -> "Bem Estar"
                                                "INOVACAO" -> "Inovação"
                                                else -> cat.lowercase().replaceFirstChar { it.uppercase() }
                                            }
                                        )
                                    },
                                    colors = ideaFilterColors
                                )
                            }
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedIdeaSort == "RECENTES",
                                    onClick = { selectedIdeaSort = "RECENTES" },
                                    label = { Text("Mais recentes") },
                                    colors = ideaFilterColors
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedIdeaSort == "VOTADAS",
                                    onClick = { selectedIdeaSort = "VOTADAS" },
                                    label = { Text("Mais votadas") },
                                    colors = ideaFilterColors
                                )
                            }
                        }

                        if (filteredIdeas.isEmpty()) {
                            StrategicEmptyState(
                                title = "Nenhuma ideia enviada ainda",
                                description = "Seja o primeiro a enviar uma ideia e contribuir com o ambiente de trabalho!",
                                icon = CorporateIcons.Idea,
                                buttonText = "Enviar Ideia",
                                onButtonClick = { showSubmitIdeaDialog = true },
                                showButton = false
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredIdeas) { idea ->
                                    IdeaCard(
                                        idea = idea,
                                        isAdmin = isAdmin,
                                        onVoteClick = { viewModel.voteIdea(idea) },
                                        onModerate = { status -> idea.id?.let { viewModel.moderateIdea(it, status) } },
                                        onPublish = { ideaToPublish = idea }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalhes da Notícia
    selectedPostForDetail?.let { post ->
        FeedPostDetailModal(
            post = post,
            onDismiss = { selectedPostForDetail = null }
        )
    }

    // Modal Criar Post (Admin)
    if (showCreatePostDialog) {
        CreateFeedPostDialog(
            isUploading = isUploading,
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { title, content, category, eventDate, authorName, imageBytes, fileName ->
                viewModel.createPost(
                    title = title,
                    content = content,
                    category = category,
                    eventDate = eventDate,
                    authorName = authorName,
                    imageBytes = imageBytes,
                    fileName = fileName,
                    onSuccess = { showCreatePostDialog = false }
                )
            }
        )
    }

    // Modal Enviar Ideia (Colaborador)
    if (showSubmitIdeaDialog) {
        SubmitIdeaDialog(
            isUploading = isUploading,
            onDismiss = { showSubmitIdeaDialog = false },
            onSubmit = { title, description, category ->
                viewModel.submitIdea(
                    title = title,
                    description = description,
                    category = category,
                    authorId = user.id,
                    authorName = user.displayName,
                    onSuccess = { showSubmitIdeaDialog = false }
                )
            }
        )
    }

    ideaToPublish?.let { idea ->
        PublishIdeaDialog(
            isUploading = isUploading,
            onDismiss = { ideaToPublish = null },
            onSubmit = { category, eventDate, imageBytes, fileName ->
                viewModel.publishIdea(idea, category, eventDate, user.id, imageBytes, fileName)
                ideaToPublish = null
            }
        )
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    onLikeClick: () -> Unit,
    onClick: () -> Unit
) {
    val categoryBgColor = when (post.category.uppercase()) {
        "EVENTO" -> Color(0xFF2D6A4F)
        "COMUNICADO" -> Color(0xFF1E3A8A)
        else -> Color(0xFFD97706)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagem do Banner Dinâmica
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = categoryBgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = post.category,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (!post.eventDate.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                CorporateIcons.Calendar,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = post.eventDate,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1B4332)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Por: ${post.authorName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    IconButton(onClick = onLikeClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                CorporateIcons.Heart,
                                contentDescription = "Curtir",
                                tint = if (post.isLiked) Color.Red else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            if (post.likesCount > 0) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = post.likesCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (post.isLiked) Color.Red else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IdeaCard(
    idea: Idea,
    isAdmin: Boolean,
    onVoteClick: () -> Unit,
    onModerate: (String) -> Unit,
    onPublish: () -> Unit
) {
    val statusColor = when (idea.status.uppercase()) {
        "APROVADO" -> Color(0xFF2D6A4F)
        "REJEITADO" -> Color(0xFFDC2626)
        else -> Color(0xFFD97706) // PENDENTE
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF1B4332).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = idea.category,
                        color = Color(0xFF1B4332),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = idea.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = idea.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B4332)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = idea.description,
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sugerido por: ${idea.authorName}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Button(
                    onClick = onVoteClick,
                    enabled = !idea.hasVoted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (idea.hasVoted) Color.LightGray else Color(0xFF2D6A4F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (idea.hasVoted) "Apoiado (${idea.votesCount})" else "👍 Apoiar (${idea.votesCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isAdmin && idea.publishedPostId == null && idea.status.uppercase() == "PENDENTE") {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onModerate("REJEITADO") }) {
                        Text("Rejeitar", color = Color.Red, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onModerate("APROVADO") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
                    ) {
                        Text("Aprovar Ideia", fontSize = 12.sp)
                    }
                }
            }

            if (isAdmin && idea.publishedPostId == null && idea.status.uppercase() == "APROVADO") {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onPublish,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publicar no Mural", fontSize = 12.sp)
                }
            } else if (idea.publishedPostId != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Publicado no Mural",
                    color = Color(0xFF2D6A4F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPostDetailModal(
    post: FeedPost,
    onDismiss: () -> Unit
) {
    var showFullscreenImage by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showFullscreenImage = true },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
            }

            Surface(
                color = Color(0xFF1B4332),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = post.category,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1B4332)
            )

            if (!post.eventDate.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "🗓️ Data do Evento: ${post.eventDate}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color(0xFF2D6A4F)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }

        if (showFullscreenImage && !post.imageUrl.isNullOrBlank()) {
            Dialog(
                onDismissRequest = { showFullscreenImage = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showFullscreenImage = false },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Imagem ampliada: ${post.title}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateFeedPostDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, content: String, category: String, eventDate: String?, authorName: String, imageBytes: ByteArray?, fileName: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("COMUNICADO") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var eventDate by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Comunicação Interna") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageFileName by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Novo Comunicado ou Evento", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo / Descrição") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = postCategoryLabel(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de publicação") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        FeedConstants.POST_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(postCategoryLabel(option)) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Data do Evento (Opcional - ex: 25/10/2025)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Autor / Departamento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                FeedImagePicker { bytes, fileName ->
                    imageBytes = bytes
                    imageFileName = fileName
                }
                if (imageFileName != null) {
                    Text(
                        text = "Imagem selecionada: $imageFileName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2D6A4F)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        title,
                        content,
                        category,
                        eventDate.ifBlank { null },
                        authorName,
                        imageBytes,
                        imageFileName
                    )
                },
                enabled = !isUploading && title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SubmitIdeaDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MELHORIA") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Nova Sugestão / Ideia", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Ideia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descreva sua sugestão") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = ideaCategoryLabel(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria da ideia") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        FeedConstants.IDEA_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(ideaCategoryLabel(option)) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title, description, category) },
                enabled = !isUploading && title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Enviar Ideia")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PublishIdeaDialog(
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (category: String, eventDate: String?, imageBytes: ByteArray?, fileName: String?) -> Unit
) {
    var category by remember { mutableStateOf("COMUNICADO") }
    var eventDate by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageFileName by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Publicar ideia no Mural", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = postCategoryLabel(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de publicação") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        FeedConstants.POST_CATEGORIES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(postCategoryLabel(option)) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Data do Evento (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FeedImagePicker { bytes, fileName ->
                    imageBytes = bytes
                    imageFileName = fileName
                }
                if (imageFileName != null) {
                    Text(
                        text = "Imagem selecionada: $imageFileName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2D6A4F)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(category, eventDate.ifBlank { null }, imageBytes, imageFileName)
                },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancelar")
            }
        }
    )
}

private fun postCategoryLabel(category: String): String = when (category) {
    "COMUNICADO" -> "Comunicado"
    "EVENTO" -> "Evento"
    "NOTICIA" -> "Notícia"
    else -> category
}

private fun ideaCategoryLabel(category: String): String = when (category) {
    "MELHORIA" -> "Melhoria"
    "INOVACAO" -> "Inovação"
    "BEM_ESTAR" -> "Bem Estar"
    "OUTROS" -> "Outros"
    else -> category
}
