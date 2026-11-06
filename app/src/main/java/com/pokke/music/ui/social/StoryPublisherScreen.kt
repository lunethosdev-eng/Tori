package com.pokke.music.ui.social

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.data.repository.SocialRepository
import kotlinx.coroutines.launch

@Composable
fun StoryPublisherScreen(authRepository: AuthRepository, onBack: () -> Unit, onPublished: () -> Unit = {}) {
    val context = LocalContext.current
    val social = remember(authRepository) { SocialRepository(authRepository) }
    val scope = rememberCoroutineScope()
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaType by remember { mutableStateOf("image") }
    var caption by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { mediaUri = uri; mediaType = "image" }
    }
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { mediaUri = uri; mediaType = "video" }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Text("Nueva historia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Text("Foto o vídeo ≤ 60s · expira en 24h", color = Color.White.copy(.5f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { pickImage.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2F42))) {
                Text("Foto")
            }
            Button(onClick = { pickVideo.launch("video/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2F42))) {
                Text("Vídeo")
            }
        }

        Spacer(Modifier.height(12.dp))
        mediaUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFF16161A), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Text("Tipo: $mediaType", color = Color.White.copy(.5f), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it.take(120) },
            label = { Text("Texto en la historia") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(.3f),
                focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(.5f)
            )
        )

        message?.let { Text(it, color = if (it.startsWith("OK")) Color(0xFF9BE7B0) else Color(0xFFFF8A80), fontSize = 13.sp) }

        Button(
            enabled = !busy && mediaUri != null,
            onClick = {
                val uri = mediaUri ?: return@Button
                busy = true; message = null
                scope.launch {
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("No se pudo leer el archivo")
                        if (mediaType == "video" && bytes.size > 40 * 1024 * 1024) error("Vídeo demasiado grande")
                        val mime = context.contentResolver.getType(uri) ?: if (mediaType == "video") "video/mp4" else "image/jpeg"
                        val url = authRepository.uploadPublicImage(
                            "stories",
                            if (mediaType == "video") "story.mp4" else "story.jpg",
                            bytes,
                            mime
                        ).getOrThrow()
                        social.publishStory(url, mediaType, caption.ifBlank { null }).getOrThrow()
                    }.onSuccess {
                        message = "OK · Historia publicada (24h)"
                        onPublished()
                    }.onFailure { message = it.message ?: "Error al publicar" }
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C17A), contentColor = Color.Black)
        ) {
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
            else Text("Publicar historia", fontWeight = FontWeight.Bold)
        }
    }
}
