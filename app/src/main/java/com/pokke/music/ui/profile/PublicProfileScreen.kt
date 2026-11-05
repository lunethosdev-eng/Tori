package com.pokke.music.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.UserProfile
import com.pokke.music.data.repository.AuthRepository
import com.pokke.music.ui.profile.LinkFavicon
import com.pokke.music.ui.profile.hostLabel
import com.pokke.music.ui.profile.platformLabel
import com.pokke.music.ui.profile.MangaFavoritesDisplay
import com.pokke.music.ui.profile.ListeningCard
import kotlinx.coroutines.launch

private val Ink = Color(0xFFF3F2FA)
private val Muted = Color(0xFFA5A4B5)

@Composable
fun PublicProfileScreen(
    username: String,
    repository: AuthRepository,
    onBack: () -> Unit
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(username) {
        loading = true
        error = null
        repository.getProfileByUsername(username)
            .onSuccess { profile = it; if (it == null) error = "Perfil no encontrado o privado" }
            .onFailure { error = it.localizedMessage ?: "No se pudo cargar el perfil" }
        loading = false
    }

    val p = profile
    val accent = when (p?.accentColor) {
        "blue" -> Color(0xFF8CB7FF); "pink" -> Color(0xFFFF9FCB); "cyan" -> Color(0xFF7EDCEB)
        "green" -> Color(0xFF8ED9B0); "gold" -> Color(0xFFE5C17A); else -> Color(0xFFAAA0FF)
    }
    val pageBg = when (p?.theme) {
        "ocean" -> Color(0xFF101B2C); "violet" -> Color(0xFF1B142A); "rose" -> Color(0xFF291824)
        "amoled" -> Color.Black; else -> Color(0xFF171A28)
    }
    val cardBg = when (p?.cardStyle) {
        "solid" -> Color(0xFF252A3D); "outline" -> pageBg.copy(alpha = .55f); else -> Color(0xCC252A40)
    }

    Box(Modifier.fillMaxSize().background(pageBg)) {
        when {
            loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = accent)
            error != null || p == null -> Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error ?: "Perfil no disponible", color = Ink, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Ink)
                }
            }
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Banner
                Box(Modifier.fillMaxWidth().height(180.dp)) {
                    if (!p.bannerUrl.isNullOrBlank()) {
                        AsyncImage(p.bannerUrl, "Banner", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.linearGradient(listOf(accent.copy(.75f), Color(0xFF20243B), pageBg))
                            )
                        )
                    }
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, pageBg.copy(.85f)))))
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.statusBarsPadding().padding(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                }

                // Avatar overlapping banner
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    FramedAvatar(
                        avatarUrl = p.avatarUrl,
                        displayName = p.displayName ?: p.username ?: "?",
                        frameId = p.frame,
                        size = 104.dp,
                        borderColor = accent,
                        borderWidth = 3.dp,
                        modifier = Modifier.offset(y = (-42).dp)
                    )
                }

                Column(Modifier.padding(horizontal = 22.dp).offset(y = (-28).dp)) {
                    Text(
                        p.displayName?.ifBlank { null } ?: p.username ?: "Usuario",
                        color = Ink,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("@${p.username ?: "user"}", color = Muted, fontSize = 14.sp)
                        if (!p.pronouns.isNullOrBlank()) {
                            Text("  ·  ${p.pronouns}", color = Muted, fontSize = 13.sp)
                        }
                    }

                    // Status bubble
                    if (!p.statusText.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent.copy(.16f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(p.statusText!!, color = accent, fontSize = 13.sp)
                        }
                    }

                    // Badges
                    val badgeList = p.badges?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
                    if (badgeList.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            badgeList.take(6).forEach { label ->
                                Row(
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accent.copy(.13f))
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, null, tint = accent, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(label, color = Ink, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Listening (only if they allow it – live data only available for self for now)
                    if (p.showListening) {
                        Spacer(Modifier.height(16.dp))
                        // For other users we don't have their live player; placeholder until presence is wired.
                        // Own profile uses live PlayerController via ProfileScreen.
                    }

                    // Bio
                    if (!p.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text("About Me", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(p.bio!!, color = Ink.copy(.9f), fontSize = 14.sp, lineHeight = 20.sp)
                    }

                    // Primary link with site icon
                    if (!p.linkUrl.isNullOrBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinkFavicon(p.linkUrl!!, size = 28.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    p.linkLabel?.ifBlank { null } ?: platformLabel(p.linkUrl!!) ?: hostLabel(p.linkUrl!!),
                                    color = Ink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(hostLabel(p.linkUrl!!), color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    MangaFavoritesDisplay(p.favoriteMangas, accent, Ink, Muted, cardBg)

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

private fun hostOf(url: String): String = try {
    java.net.URI(url).host?.removePrefix("www.") ?: url
} catch (_: Exception) {
    url
}
