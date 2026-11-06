package com.pokke.music.ui.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    authRepository: AuthRepository,
    onBack: (() -> Unit)? = null,
    onOpenStoryPublisher: (() -> Unit)? = null,
    onOpenMaps: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val storiesOn by prefs.experimentalStoriesEnabled.collectAsState(false)
    val mapsOn by prefs.experimentalMapsEnabled.collectAsState(false)
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()
    var friends by remember { mutableStateOf<List<SocialRepository.Friend>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<SocialRepository.Conversation>>(emptyList()) }
    var stories by remember { mutableStateOf<List<SocialRepository.Story>>(emptyList()) }
    var selected by remember { mutableStateOf<SocialRepository.Conversation?>(null) }
    var messages by remember { mutableStateOf<List<SocialRepository.Message>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val myId = authRepository.session.collectAsState().value?.userId

    fun refreshLists() {
        scope.launch {
            loading = true
            social.friends().onSuccess { friends = it }.onFailure { error = it.message }
            social.conversations().onSuccess { conversations = it }.onFailure { error = it.message }
            if (storiesOn) social.activeStories().onSuccess { stories = it }
            loading = false
        }
    }

    fun loadMessages(id: String) {
        scope.launch {
            social.messages(id).onSuccess { messages = it }.onFailure { error = it.message }
        }
    }

    LaunchedEffect(Unit) { refreshLists() }

    // Simple realtime: poll open conversation every 3s
    LaunchedEffect(selected?.id) {
        val id = selected?.id ?: return@LaunchedEffect
        while (true) {
            social.messages(id).onSuccess { messages = it }
            delay(3000)
        }
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val convo = selected ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("No se pudo leer la imagen")
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val url = authRepository.uploadPublicImage("chat", "img_${System.currentTimeMillis()}.jpg", bytes, mime).getOrThrow()
                social.sendImage(convo.id, url).getOrThrow()
                loadMessages(convo.id)
            }.onFailure { error = it.message }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .statusBarsPadding()
    ) {
        if (selected == null) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                }
                Text("Chats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.weight(1f))
                if (mapsOn && onOpenMaps != null) {
                    TextButton(onClick = onOpenMaps) { Text("Mapas", color = Color(0xFFE5C17A)) }
                }
                IconButton(onClick = { refreshLists() }) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
            }

            if (storiesOn) {
                LazyRow(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onOpenStoryPublisher?.invoke() }) {
                            Box(
                                Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2A2F42)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                            Text("Tu historia", color = Color.White.copy(.7f), fontSize = 11.sp)
                        }
                    }
                    items(stories, key = { it.id }) { s ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                            scope.launch { social.markStoryViewed(s.id) }
                        }) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (s.viewed) Color(0xFF333338) else Color(0xFFE5C17A))
                                    .padding(2.dp)
                            ) {
                                AsyncImage(
                                    model = s.avatarUrl ?: s.mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF1A1A1E)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text((s.displayName ?: s.username ?: "?").take(8), color = Color.White.copy(.7f), fontSize = 11.sp)
                        }
                    }
                }
            }

            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Color.White)
            error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(12.dp)) }

            LazyColumn(Modifier.fillMaxSize()) {
                items(conversations, key = { it.id }) { c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selected = c; loadMessages(c.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF29292E))) {
                            c.friend.avatarUrl?.let {
                                AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.nickname ?: c.friend.displayName.ifBlank { c.friend.username }, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(c.lastMessage ?: "Sin mensajes", color = Color.White.copy(.5f), fontSize = 13.sp, maxLines = 1)
                        }
                        if (c.unread > 0) {
                            Badge { Text("${c.unread}") }
                        }
                    }
                }
                if (conversations.isEmpty() && friends.isNotEmpty()) {
                    item {
                        Text("Amigos — toca para abrir chat", color = Color.White.copy(.5f), modifier = Modifier.padding(16.dp))
                    }
                    items(friends, key = { it.id }) { f ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                scope.launch {
                                    social.openConversation(f.id).onSuccess { id ->
                                        selected = SocialRepository.Conversation(id, f, null, null)
                                        loadMessages(id)
                                    }.onFailure { error = it.message }
                                }
                            }.padding(16.dp)
                        ) {
                            Text(f.displayName.ifBlank { f.username }, color = Color.White)
                        }
                    }
                }
            }
        } else {
            val convo = selected!!
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text(convo.nickname ?: convo.friend.displayName.ifBlank { convo.friend.username }, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 12.dp),
                reverseLayout = false
            ) {
                items(messages, key = { it.id }) { m ->
                    val mine = m.senderId == myId
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = if (mine) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            Modifier
                                .widthIn(max = 280.dp)
                                .background(
                                    if (mine) Color(0xFFE5C17A) else Color(0xFF2A2F42),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(10.dp)
                        ) {
                            when (m.type) {
                                "image", "gif" -> if (!m.mediaUrl.isNullOrBlank()) {
                                    AsyncImage(m.mediaUrl, null, Modifier.width(200.dp).height(160.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                } else Text(m.body ?: "", color = if (mine) Color.Black else Color.White)
                                else -> Text(m.body ?: "", color = if (mine) Color.Black else Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { pickImage.launch("image/*") }) {
                    Icon(Icons.Default.Image, null, tint = Color.White)
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mensaje") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(.4f), unfocusedBorderColor = Color.White.copy(.2f)
                    )
                )
                IconButton(onClick = {
                    val text = draft.trim(); if (text.isEmpty()) return@IconButton
                    scope.launch {
                        social.sendText(convo.id, text).onSuccess {
                            draft = ""
                            loadMessages(convo.id)
                        }.onFailure { error = it.message }
                    }
                }) {
                    Icon(Icons.Default.Send, null, tint = Color(0xFFE5C17A))
                }
            }
        }
    }
}
