package com.pokke.music.ui.social

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.TextFields
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class GalleryItem(val uri: Uri, val isVideo: Boolean)

@Composable
fun StoryPublisherScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onPublished: () -> Unit = {}
) {
    val context = LocalContext.current
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var selected by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var caption by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showPublishSheet by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("grid") } // grid | preview | collage

    fun loadGallery() {
        scope.launch {
            items = withContext(Dispatchers.IO) {
                val out = mutableListOf<GalleryItem>()
                val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE)
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
                    var n = 0
                    while (c.moveToNext() && n < 200) {
                        val id = c.getLong(idCol)
                        val type = c.getInt(typeCol)
                        val contentUri = ContentUris.withAppendedId(uri, id)
                        out.add(GalleryItem(contentUri, type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO))
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
        if (hasPermission) loadGallery()
        else message = "Permiso de galería necesario"
    }

    fun requestPerms() {
        val perms = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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
            selected.size >= 3 -> selected.drop(1) + uri // max 3 slides
            else -> selected + uri
        }
    }

    suspend fun publish(audience: String) {
        if (selected.isEmpty()) return
        busy = true
        message = null
        runCatching {
            for ((index, uri) in selected.withIndex()) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("No se pudo leer el archivo")
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val isVideo = mime.startsWith("video")
                if (isVideo && bytes.size > 40 * 1024 * 1024) error("Vídeo demasiado grande")
                val url = authRepository.uploadPublicImage(
                    "stories",
                    "story_${System.currentTimeMillis()}_$index.${if (isVideo) "mp4" else "jpg"}",
                    bytes,
                    mime
                ).getOrThrow()
                // audience stored in caption prefix for now until schema has audience column used
                val cap = buildString {
                    if (audience == "friends") append("[friends] ")
                    if (selected.size > 1) append("[slide ${index + 1}/${selected.size}] ")
                    append(caption)
                }.trim().ifBlank { null }
                social.publishStory(url, if (isVideo) "video" else "image", cap).getOrThrow()
            }
        }.onSuccess {
            message = "Historia publicada"
            onPublished()
        }.onFailure { message = it.message ?: "Error al publicar" }
        busy = false
        showPublishSheet = false
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0C))) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text("Nueva historia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        if (selected.isEmpty()) "Elige hasta 3 fotos/vídeos"
                        else "${selected.size} seleccionada(s) · slide al terminar cada una",
                        color = Color.White.copy(.5f), fontSize = 12.sp
                    )
                }
                TextButton(
                    onClick = { if (selected.isNotEmpty()) showPublishSheet = true },
                    enabled = selected.isNotEmpty() && !busy
                ) {
                    Text("Siguiente", color = if (selected.isEmpty()) Color.White.copy(.3f) else Color(0xFFE5C17A), fontWeight = FontWeight.Bold)
                }
            }

            // Preview strip
            if (selected.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selected.forEach { uri ->
                        AsyncImage(
                            uri, null,
                            Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A1E)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                // Tools row (text / emoji placeholders for editor tools)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistChip(
                        onClick = { mode = "preview" },
                        label = { Text("Texto") },
                        leadingIcon = { Icon(Icons.Default.TextFields, null, Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("Emoji") },
                        leadingIcon = { Icon(Icons.Default.EmojiEmotions, null, Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { mode = if (selected.size >= 2) "collage" else mode },
                        label = { Text("Collage") },
                        leadingIcon = { Icon(Icons.Default.Collections, null, Modifier.size(16.dp)) },
                        enabled = selected.size >= 2
                    )
                }
                if (mode == "preview" || mode == "collage") {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it.take(120) },
                        placeholder = { Text("Escribe en la historia…", color = Color.White.copy(.4f)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE5C17A),
                            unfocusedBorderColor = Color.White.copy(.2f)
                        )
                    )
                }
            }

            if (!hasPermission) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pokke necesita acceso a tu galería", color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text("Para elegir fotos y vídeos de historias", color = Color.White.copy(.5f), fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { requestPerms() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C17A), contentColor = Color.Black)
                    ) { Text("Permitir acceso") }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(items, key = { it.uri.toString() }) { item ->
                        val isSelected = selected.contains(item.uri)
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .clickable { toggle(item.uri) }
                        ) {
                            AsyncImage(
                                item.uri, null,
                                Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (item.isVideo) {
                                Text(
                                    "VIDEO",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(.55f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            if (isSelected) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .border(2.dp, Color(0xFFE5C17A))
                                        .background(Color(0x33E5C17A))
                                )
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE5C17A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPublishSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPublishSheet = false },
                containerColor = Color(0xFF16161A)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("¿Quién puede verla?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { scope.launch { publish("public") } },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C17A), contentColor = Color.Black)
                    ) {
                        Text(if (busy) "Publicando…" else "Publicar historia pública", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { scope.launch { publish("friends") } },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Solo amigos", color = Color.White)
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        message?.let {
            Text(
                it,
                color = if (it.startsWith("Historia")) Color(0xFF9BE7B0) else Color(0xFFFF8A80),
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
            )
        }
    }
}
