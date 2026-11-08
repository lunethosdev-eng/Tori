package com.pokke.music.ui.social

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import com.pokke.music.player.PlayerController
import com.pokke.music.data.remote.GiphyClient
import com.pokke.music.data.remote.GiphyGif
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private data class GalleryItem(val uri: Uri, val isVideo: Boolean, val durationSec: Int = 0)
private data class StoryText(
    val text: String,
    val offset: Offset = Offset.Zero,
    val style: Int = 0, // 0 classic bold, 1 classic, 2 typewriter
    val isGif: Boolean = false,
    val gifUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryPublisherScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onPublished: () -> Unit = {}
) {
    val context = LocalContext.current
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()
    val currentSong by PlayerController.currentSong.collectAsState()

    var hasPermission by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var selected by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var phase by remember { mutableStateOf("gallery") } // gallery | editor | music
    var caption by remember { mutableStateOf("") }
    var storyTexts by remember { mutableStateOf<List<StoryText>>(emptyList()) }
    var draftText by remember { mutableStateOf("") }
    var showTextEditor by remember { mutableStateOf(false) }
    var textStyle by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showPublishSheet by remember { mutableStateOf(false) }
    var attachMusic by remember { mutableStateOf(true) }
    var musicOnly by remember { mutableStateOf(false) }
    var stickerOpen by remember { mutableStateOf(false) }
    var giphyQuery by remember { mutableStateOf("") }
    var giphyResults by remember { mutableStateOf<List<GiphyGif>>(emptyList()) }
    var giphyLoading by remember { mutableStateOf(false) }
    var selectedGif by remember { mutableStateOf<GiphyGif?>(null) }

    fun loadGallery() {
        scope.launch {
            items = withContext(Dispatchers.IO) {
                val out = mutableListOf<GalleryItem>()
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.DURATION
                )
                val selection = (
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
                    )
                val selectionArgs = arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
                val uri = MediaStore.Files.getContentUri("external")
                context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
                )?.use { c ->
                    val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val typeCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    val durCol = c.getColumnIndex(MediaStore.Files.FileColumns.DURATION)
                    var n = 0
                    while (c.moveToNext() && n < 240) {
                        val id = c.getLong(idCol)
                        val type = c.getInt(typeCol)
                        val dur = if (durCol >= 0) (c.getLong(durCol) / 1000).toInt() else 0
                        out.add(
                            GalleryItem(
                                ContentUris.withAppendedId(uri, id),
                                type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                                dur
                            )
                        )
                        n++
                    }
                }
                out
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result.values.any { it }
        if (hasPermission) loadGallery() else message = "Permiso de galería necesario"
    }

    fun requestPerms() {
        val perms = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        val granted = perms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) {
            hasPermission = true
            loadGallery()
        } else permissionLauncher.launch(perms)
    }

    LaunchedEffect(Unit) { requestPerms() }

    fun toggle(uri: Uri) {
        selected = when {
            selected.contains(uri) -> selected - uri
            selected.size >= 3 -> selected.drop(1) + uri
            else -> selected + uri
        }
    }

    suspend fun publish(audience: String) {
        if (selected.isEmpty() && !musicOnly) return
        busy = true
        message = null
        runCatching {
            val musicMeta = if (attachMusic && currentSong != null) {
                " ♪ ${currentSong!!.title} – ${currentSong!!.artist}"
            } else ""
            if (musicOnly && currentSong != null) {
                // music-only story card as image-less entry with caption
                val cap = buildString {
                    if (audience == "friends") append("[friends] ")
                    append("[music_only] ")
                    append("${currentSong!!.title} – ${currentSong!!.artist}")
                    if (caption.isNotBlank()) append("\n$caption")
                    storyTexts.filter { !it.isGif }.forEach { append("\n${it.text}") }
                }
                // Use cover as media if available
                val cover = currentSong!!.coverUrl
                if (!cover.isNullOrBlank()) {
                    social.publishStory(cover, "image", cap).getOrThrow()
                } else error("Sin carátula para historia de solo música")
            } else {
                for ((index, uri) in selected.withIndex()) {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("No se pudo leer el archivo")
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val isVideo = mime.startsWith("video")
                    if (isVideo && bytes.size > 40 * 1024 * 1024) error("Vídeo demasiado grande (máx ~40MB)")
                    val url = authRepository.uploadPublicImage(
                        "stories",
                        "story_${System.currentTimeMillis()}_$index.${if (isVideo) "mp4" else "jpg"}",
                        bytes,
                        mime
                    ).getOrThrow()
                    val cap = buildString {
                        if (audience == "friends") append("[friends] ")
                        if (selected.size > 1) append("[slide ${index + 1}/${selected.size}] ")
                        append(caption)
                        storyTexts.filter { !it.isGif }.forEach { append("\n${it.text}") }
                        append(musicMeta)
                    }.trim().ifBlank { null }
                    social.publishStory(url, if (isVideo) "video" else "image", cap).getOrThrow()
                }
            }
        }.onSuccess {
            message = "Historia publicada"
            onPublished()
        }.onFailure { message = it.message ?: "Error al publicar" }
        busy = false
        showPublishSheet = false
    }

    when (phase) {
        "gallery" -> {
            // Instagram-like gallery picker
            Column(Modifier.fillMaxSize().background(Color(0xFF000000)).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                    Text(
                        "Agregar a historia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, null, tint = Color.White.copy(.7f))
                    }
                }

                // Tool chips like IG: Plantillas, Efectos, Música, Collage
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Plantillas", Icons.Default.AutoAwesome, {}),
                        Triple("Efectos", Icons.Default.FilterVintage, {}),
                        Triple("Música", Icons.Default.MusicNote, { phase = "music" }),
                        Triple("Collage", Icons.Default.Collections, {
                            if (selected.size >= 2) phase = "editor"
                        })
                    ).forEach { (label, icon, act) ->
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1C1C1E))
                                .clickable { act() }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(label, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recientes", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(
                        if (selected.isEmpty()) "Seleccionar" else "${selected.size}/3",
                        color = Color(0xFF3797F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!hasPermission) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Acceso a galería", color = Color.White, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { requestPerms() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3797F0))) {
                            Text("Permitir")
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f).padding(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // Camera tile
                        item {
                            Box(
                                Modifier
                                    .aspectRatio(1f)
                                    .background(Color(0xFF1C1C1E))
                                    .clickable { message = "Cámara próximamente" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                        items(items, key = { it.uri.toString() }) { item ->
                            val isSelected = selected.contains(item.uri)
                            Box(Modifier.aspectRatio(1f).clickable { toggle(item.uri) }) {
                                AsyncImage(item.uri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                if (item.isVideo && item.durationSec > 0) {
                                    Text(
                                        "%d:%02d".format(item.durationSec / 60, item.durationSec % 60),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(.55f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(Modifier.fillMaxSize().border(2.dp, Color(0xFF3797F0)).background(Color(0x333797F0)))
                                    Box(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3797F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${selected.indexOf(item.uri) + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom next bar
                if (selected.isNotEmpty()) {
                    Button(
                        onClick = { phase = "editor" },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3797F0)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Siguiente", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        "music" -> {
            Column(Modifier.fillMaxSize().background(Color(0xFF0B0B0D)).statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { phase = "gallery" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text("Música", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Text("Audio sugerido", color = Color.White.copy(.5f), modifier = Modifier.padding(horizontal = 16.dp))
                currentSong?.let { song ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C1C1E))
                            .clickable { attachMusic = true; if (selected.isEmpty()) { musicOnly = true; phase = "editor" } else phase = "editor" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(song.coverUrl, null, Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(song.artist, color = Color.White.copy(.6f), fontSize = 13.sp)
                        }
                        Icon(Icons.Default.GraphicEq, null, tint = Color(0xFF3797F0))
                    }
                } ?: Text("Reproduce una canción en Pokke para usarla", color = Color.White.copy(.5f), modifier = Modifier.padding(16.dp))
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Switch(attachMusic, { attachMusic = it })
                    Text("Adjuntar a la historia", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                }
                TextButton(onClick = { musicOnly = true; attachMusic = true; phase = "editor" }) {
                    Text("Solo música (sin foto)", color = Color(0xFF3797F0))
                }
            }
        }

        else -> {
            // EDITOR canvas Instagram-like
            val previewUri = selected.firstOrNull()
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (previewUri != null) {
                    AsyncImage(previewUri, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (musicOnly && currentSong != null) {
                    // music-only visual
                    Box(Modifier.fillMaxSize().background(Color(0xFF121218)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                currentSong!!.coverUrl, null,
                                Modifier.size(200.dp).clip(CircleShape).border(6.dp, Color.White.copy(.15f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(currentSong!!.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(currentSong!!.artist, color = Color.White.copy(.7f))
                        }
                    }
                }

                // Draggable texts
                storyTexts.forEachIndexed { idx, st ->
                    key(st.gifUrl ?: st.text, idx) {
                        var offset by remember(st) { mutableStateOf(st.offset) }
                        var dragY by remember { mutableStateOf(0f) }
                        val alpha by animateFloatAsState(if (dragY > 180f) 0.35f else 1f, label = "delA")
                        Box(
                            Modifier
                                .offset { IntOffset(offset.x.roundToInt(), (offset.y + dragY).roundToInt()) }
                                .graphicsLayer { this.alpha = alpha }
                                .pointerInput(idx) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            if (dragY > 220f) {
                                                storyTexts = storyTexts.filterIndexed { i, _ -> i != idx }
                                            }
                                            dragY = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            offset = Offset(offset.x + amount.x, offset.y + amount.y)
                                            dragY += amount.y
                                            // persist soft
                                            storyTexts = storyTexts.mapIndexed { i, s ->
                                                if (i == idx) s.copy(offset = offset) else s
                                            }
                                        }
                                    )
                                }
                        ) {
                            if (st.isGif && !st.gifUrl.isNullOrBlank()) {
                                AsyncImage(
                                    st.gifUrl, "GIF",
                                    Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    st.text,
                                    color = Color.White,
                                    fontSize = if (st.style == 0) 28.sp else 22.sp,
                                    fontWeight = if (st.style == 0) FontWeight.Black else FontWeight.Medium,
                                    modifier = Modifier
                                        .background(Color.Black.copy(0.35f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (showTextEditor) {
                ModalBottomSheet(onDismissRequest = { showTextEditor = false }, containerColor = Color(0xFF1C1C1E)) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = draftText,
                            onValueChange = { draftText = it.take(80) },
                            placeholder = { Text("Escribe…", color = Color.White.copy(.4f)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3797F0),
                                unfocusedBorderColor = Color.White.copy(.2f)
                            )
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Classic", "Modern", "Signature").forEachIndexed { i, label ->
                                FilterChip(
                                    selected = textStyle == i,
                                    onClick = { textStyle = i },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (draftText.isNotBlank()) {
                                    storyTexts = storyTexts + StoryText(draftText.trim(), Offset(80f, 320f), textStyle)
                                    draftText = ""
                                    showTextEditor = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3797F0))
                        ) { Text("Agregar texto") }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            LaunchedEffect(stickerOpen, giphyQuery) {
        if (!stickerOpen) return@LaunchedEffect
        giphyLoading = true
        runCatching {
            giphyResults = if (giphyQuery.isBlank()) GiphyClient.trending(30) else GiphyClient.search(giphyQuery, 30)
        }.onFailure { message = it.message }
        giphyLoading = false
    }
    if (stickerOpen) {
                ModalBottomSheet(onDismissRequest = { stickerOpen = false }, containerColor = Color(0xFF1C1C1E)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Stickers", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        // Quick sticker chips (IG-like labels)
                        val stickers = listOf(
                            "📍 UBICACIÓN", "👤 MENCIÓN", "🎵 MÚSICA", "GIF", "📷 FOTO",
                            "❓ PREGUNTAS", "📊 ENCUESTA", "🔗 ENLACE", "# HASHTAG"
                        )
                        stickers.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { s ->
                                    AssistChip(
                                        onClick = {
                                            storyTexts = storyTexts + StoryText(s, Offset(60f, 280f), 1)
                                            stickerOpen = false
                                        },
                                        label = { Text(s, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text("GIFs", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        OutlinedTextField(
                            value = giphyQuery,
                            onValueChange = { giphyQuery = it },
                            placeholder = { Text("Buscar Giphy…", color = Color.White.copy(.4f)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF3797F0), unfocusedBorderColor = Color.White.copy(.2f))
                        )
                        if (giphyLoading) {
                            Text("Cargando GIFs…", color = Color.White.copy(.5f))
                        } else {
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(giphyResults.size) { i ->
                                    val g = giphyResults[i]
                                    AsyncImage(
                                        g.previewUrl, null,
                                        Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).clickable {
                                            selectedGif = g
                                            storyTexts = storyTexts + StoryText(text = "GIF", offset = Offset(40f, 200f), style = 1, isGif = true, gifUrl = g.fullUrl)
                                            stickerOpen = false
                                        },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            if (showPublishSheet) {
                ModalBottomSheet(onDismissRequest = { showPublishSheet = false }, containerColor = Color(0xFF1C1C1E)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Compartir historia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { scope.launch { publish("public") } },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3797F0))
                        ) { Text(if (busy) "Publicando…" else "Tu historia (pública)", fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { scope.launch { publish("friends") } },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Mejores amigos", color = Color.White) }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    message?.let {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Text(
                it,
                color = if (it.startsWith("Historia")) Color(0xFF9BE7B0) else Color(0xFFFF8A80),
                modifier = Modifier.padding(24.dp).background(Color.Black.copy(.7f), RoundedCornerShape(8.dp)).padding(12.dp)
            )
        }
    }
}
}
