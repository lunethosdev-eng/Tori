package com.pokke.music.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokke.music.data.model.LyricLine
import com.pokke.music.data.preferences.UserPreferences
import com.pokke.music.player.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

@Composable
fun LyricsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val current by PlayerController.currentSong.collectAsState()
    val playing by PlayerController.isPlaying.collectAsState()
    val position by PlayerController.positionMs.collectAsState()
    val duration by PlayerController.durationMs.collectAsState()
    val animIntensity by prefs.lyricsAnimIntensity.collectAsState(1f)
    val colorMode by prefs.lyricsColorMode.collectAsState("classic") // classic | emotion | mono
    val targetLang by prefs.lyricsTargetLang.collectAsState("es")

    var lines by remember { mutableStateOf(current?.lyrics.orEmpty()) }
    var plain by remember { mutableStateOf(current?.lyricsText) }
    var loading by remember { mutableStateOf(false) }
    var translated by remember { mutableStateOf(false) }
    var originalLines by remember { mutableStateOf<List<LyricLine>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(current?.id) {
        lines = current?.lyrics.orEmpty()
        plain = current?.lyricsText
        translated = false
        originalLines = emptyList()
    }

    val active = remember(position, lines) {
        lines.indexOfLast { it.timeMs <= position }.coerceAtLeast(0)
    }
    LaunchedEffect(active) {
        if (lines.isNotEmpty()) runCatching { listState.animateScrollToItem(active.coerceAtMost(lines.lastIndex)) }
    }

    fun emotionColor(text: String, selected: Boolean): Color {
        if (!selected) return Color.White.copy(0.32f)
        val t = text.lowercase()
        return when {
            anyWord(t, listOf("love", "amor", "corazón", "heart", "kiss")) -> Color(0xFFFF6B9D)
            anyWord(t, listOf("sad", "llorar", "cry", "pain", "dolor", "alone")) -> Color(0xFF7EA8FF)
            anyWord(t, listOf("fire", "fuego", "rage", "ira", "hot")) -> Color(0xFFFF8A4C)
            anyWord(t, listOf("night", "noche", "moon", "luna", "dark")) -> Color(0xFFB39DFF)
            anyWord(t, listOf("happy", "feliz", "dance", "bailar", "party")) -> Color(0xFFFFD56A)
            else -> Color.White
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0B0B0E))) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(current?.title ?: "Letras", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text(current?.artist ?: "", color = Color.White.copy(0.55f), fontSize = 12.sp, maxLines = 1)
                }
                IconButton(onClick = {
                    scope.launch {
                        if (translated && originalLines.isNotEmpty()) {
                            lines = originalLines
                            translated = false
                            return@launch
                        }
                        val src = lines.ifEmpty {
                            plain?.lines()?.mapIndexed { i, t -> LyricLine(i * 2000L, t) }.orEmpty()
                        }
                        if (src.isEmpty()) return@launch
                        loading = true
                        originalLines = src
                        val out = translateLines(src, targetLang)
                        if (out.isNotEmpty()) {
                            lines = out
                            translated = true
                        }
                        loading = false
                    }
                }) {
                    Icon(Icons.Default.Translate, "Traducir", tint = if (translated) Color(0xFF8BE9A0) else Color.White)
                }
            }

            // Language chips
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("es" to "Español", "en" to "English", "pt" to "Português", "ja" to "日本語", "ko" to "한국어").forEach { (code, label) ->
                    val sel = targetLang == code
                    Text(
                        label,
                        color = if (sel) Color.Black else Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) Color.White else Color.White.copy(0.12f))
                            .clickable {
                                scope.launch { prefs.setLyricsTargetLang(code) }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (lines.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 60.dp),
                    verticalArrangement = Arrangement.spacedBy((14 + 10 * animIntensity).dp)
                ) {
                    itemsIndexed(lines) { index, line ->
                        val selected = index == active
                        val targetColor = when (colorMode) {
                            "emotion" -> emotionColor(line.text, selected)
                            "mono" -> if (selected) Color(0xFFE8E8E8) else Color.White.copy(0.28f)
                            else -> if (selected) Color.White else Color.White.copy(0.35f)
                        }
                        val color by animateColorAsState(targetColor, tween((180 / animIntensity.coerceAtLeast(0.3f)).toInt()), label = "lyricColor")
                        val scale by animateFloatAsState(
                            if (selected) 1f + 0.06f * animIntensity else 0.96f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "lyricScale"
                        )
                        Text(
                            line.text,
                            color = color,
                            fontSize = if (selected) (25 + 4 * animIntensity).sp else 22.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            lineHeight = 34.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .clickable { PlayerController.seekTo(line.timeMs) }
                        )
                    }
                }
            } else {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        if (loading) "Traduciendo…" else plain ?: "No se encontraron letras sincronizadas para esta canción.",
                        color = Color.White.copy(0.7f),
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    )
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                Slider(
                    value = if (duration > 0) position.toFloat().div(duration).coerceIn(0f, 1f) else 0f,
                    onValueChange = { PlayerController.seekTo((it * duration).toLong()) },
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(0.3f))
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { PlayerController.seekTo((position - 10000).coerceAtLeast(0)) }) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    IconButton(onClick = { PlayerController.togglePlayPause() }) {
                        Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                    IconButton(onClick = { PlayerController.seekTo((position + 10000).coerceAtMost(duration)) }) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }
}

private fun anyWord(text: String, words: List<String>) = words.any { it in text }

private suspend fun translateLines(src: List<LyricLine>, targetLang: String): List<LyricLine> =
    withContext(Dispatchers.IO) {
        runCatching {
            val client = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
            val joined = src.joinToString("\n") { it.text }
            // LibreTranslate public instance (best-effort; may rate-limit)
            val url = "https://libretranslate.com/translate"
            val body = okhttp3.FormBody.Builder()
                .add("q", joined.take(4500))
                .add("source", "auto")
                .add("target", targetLang)
                .add("format", "text")
                .build()
            val req = Request.Builder().url(url).post(body).header("Accept", "application/json").build()
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) return@runCatching emptyList()
                val translated = org.json.JSONObject(text).optString("translatedText")
                if (translated.isBlank()) return@runCatching emptyList()
                val parts = translated.split("\n")
                src.mapIndexed { i, line ->
                    line.copy(text = parts.getOrNull(i)?.takeIf { it.isNotBlank() } ?: line.text)
                }
            }
        }.getOrDefault(emptyList())
    }
