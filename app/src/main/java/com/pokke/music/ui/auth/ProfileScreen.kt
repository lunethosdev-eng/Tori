package com.pokke.music.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.data.model.UserProfile
import com.pokke.music.data.repository.AuthRepository
import kotlinx.coroutines.launch

private val ProfileInk = Color(0xFFF3F2FA)
private val ProfileMuted = Color(0xFFA5A4B5)

@Composable
fun ProfileScreen(repository: AuthRepository, onBack: () -> Unit, onSignedOut: () -> Unit) {
    val session by repository.session.collectAsState()
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("") }
    var banner by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("midnight") }
    var frame by remember { mutableStateOf("none") }
    var badge by remember { mutableStateOf("none") }
    var pronouns by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var accent by remember { mutableStateOf("violet") }
    var cardStyle by remember { mutableStateOf("glass") }
    var badges by remember { mutableStateOf("Pokke listener,Music lover") }
    var isPublic by remember { mutableStateOf(true) }
    var linkLabel by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(session?.userId) {
        loading = true
        repository.getProfile().onSuccess { p ->
            profile = p
            displayName = p?.displayName.orEmpty(); username = p?.username.orEmpty(); bio = p?.bio.orEmpty()
            avatar = p?.avatarUrl.orEmpty(); banner = p?.bannerUrl.orEmpty(); theme = p?.theme ?: "midnight"
            frame = p?.frame ?: "none"; badge = p?.badge ?: "none"; pronouns = p?.pronouns.orEmpty()
            status = p?.statusText.orEmpty(); accent = p?.accentColor ?: "violet"; cardStyle = p?.cardStyle ?: "glass"
            badges = p?.badges ?: "Pokke listener,Music lover"; isPublic = p?.isPublic ?: true
            linkLabel = p?.linkLabel.orEmpty(); linkUrl = p?.linkUrl.orEmpty()
        }.onFailure { message = it.localizedMessage }
        loading = false
    }

    val accentColor = when (accent) {
        "blue" -> Color(0xFF8CB7FF); "pink" -> Color(0xFFFF9FCB); "cyan" -> Color(0xFF7EDCEB)
        "green" -> Color(0xFF8ED9B0); "gold" -> Color(0xFFE5C17A); else -> Color(0xFFAAA0FF)
    }
    val pageBackground = when (theme) {
        "ocean" -> Color(0xFF101B2C); "violet" -> Color(0xFF1B142A); "rose" -> Color(0xFF291824)
        "amoled" -> Color.Black; else -> Color(0xFF171A28)
    }
    val cardBackground = when (cardStyle) {
        "solid" -> Color(0xFF252A3D); "outline" -> pageBackground.copy(alpha = .55f); else -> Color(0xCC252A40)
    }

    Column(Modifier.fillMaxSize().background(pageBackground).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(236.dp)) {
            if (banner.isNotBlank()) AsyncImage(banner, "Banner del perfil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(accentColor.copy(.78f), Color(0xFF20243B), pageBackground))))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(.10f), Color.Black.copy(.55f)))))
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ProfileTopButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White) }
                ProfileTopButton(onClick = { scope.launch { repository.signOut(); onSignedOut() } }) { Icon(Icons.Default.Logout, "Cerrar sesión", tint = Color.White) }
            }
            Box(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 0.dp).size(104.dp).clip(CircleShape)
                .background(Color(0xFF30364D)).border(3.dp, accentColor, CircleShape), contentAlignment = Alignment.Center) {
                if (avatar.isNotBlank()) AsyncImage(avatar, "Avatar", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text((displayName.firstOrNull() ?: 'P').uppercase(), color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                if (frame != "none") Box(Modifier.fillMaxSize().border(3.dp, frameColor(frame), CircleShape))
            }
            if (badge != "none") Surface(Modifier.align(Alignment.BottomStart).padding(start = 96.dp, bottom = 4.dp), shape = CircleShape, color = accentColor) {
                Icon(Icons.Default.Badge, "Emblema destacado", tint = Color(0xFF171A28), modifier = Modifier.padding(7.dp).size(18.dp))
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(displayName.ifBlank { "Tu perfil" }, color = ProfileInk, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("@${username.ifBlank { "usuario" }}", color = ProfileMuted, fontSize = 14.sp)
                if (pronouns.isNotBlank()) Text("  ·  $pronouns", color = ProfileMuted, fontSize = 13.sp)
            }
            if (status.isNotBlank()) {
                Surface(color = accentColor.copy(.14f), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Text(status, color = accentColor, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
            if (badges.isNotBlank()) Row(Modifier.padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                badges.split(',').map { it.trim() }.filter { it.isNotBlank() }.take(5).forEach { label ->
                    Surface(color = accentColor.copy(.13f), shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, null, tint = accentColor, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp)); Text(label, color = ProfileInk, fontSize = 11.sp)
                        }
                    }
                }
            }
            if (bio.isNotBlank()) Text(bio, color = ProfileInk.copy(.88f), fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 17.dp))
            if (linkUrl.isNotBlank()) Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, null, tint = accentColor, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp))
                Text(linkLabel.ifBlank { linkUrl }, color = accentColor, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(onClick = { editing = !editing }, modifier = Modifier.fillMaxWidth().padding(top = 22.dp).height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF171827))) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(9.dp)); Text(if (editing) "Cerrar editor" else "Editar perfil principal", fontWeight = FontWeight.Bold)
            }
            AnimatedVisibility(editing, enter = fadeIn(), exit = fadeOut()) {
                Column(Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(22.dp)).background(cardBackground).padding(16.dp)) {
                    Text("PERSONALIZACIÓN DEL PERFIL", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(12.dp))
                    ProfileField("Nombre visible", displayName) { displayName = it }
                    ProfileField("Nombre de usuario (3–24 caracteres)", username) { username = it }
                    ProfileField("Pronombres", pronouns) { pronouns = it }
                    ProfileField("Estado personalizado", status) { status = it }
                    ProfileField("Biografía", bio, false) { bio = it }
                    ProfileField("URL pública del avatar", avatar) { avatar = it }
                    ProfileField("URL pública del banner", banner) { banner = it }
                    ProfileField("Texto del enlace", linkLabel) { linkLabel = it }
                    ProfileField("URL del enlace", linkUrl) { linkUrl = it }
                    ProfileField("Emblemas (separados por coma)", badges) { badges = it }
                    ProfileChoice("Tema", theme, listOf("midnight", "ocean", "violet", "rose", "amoled"), listOf("Noche", "Océano", "Violeta", "Rosa", "AMOLED"), accentColor) { theme = it }
                    ProfileChoice("Color de acento", accent, listOf("violet", "blue", "pink", "cyan", "green", "gold"), listOf("Violeta", "Azul", "Rosa", "Cian", "Verde", "Dorado"), accentColor) { accent = it }
                    ProfileChoice("Marco del avatar", frame, listOf("none", "violet", "blue", "pink", "gold"), listOf("Ninguno", "Violeta", "Azul", "Rosa", "Dorado"), accentColor) { frame = it }
                    ProfileChoice("Emblema principal", badge, listOf("none", "music", "star", "crown", "verified"), listOf("Ninguno", "Música", "Estrella", "Corona", "Verificado"), accentColor) { badge = it }
                    ProfileChoice("Estilo de tarjeta", cardStyle, listOf("glass", "solid", "outline"), listOf("Cristal", "Sólido", "Contorno"), accentColor) { cardStyle = it }
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Perfil público", color = ProfileInk, fontWeight = FontWeight.SemiBold); Text("Permite mostrar tu perfil en espacios públicos", color = ProfileMuted, fontSize = 11.sp) }
                        Switch(isPublic, { isPublic = it })
                    }
                    if (message != null) Text(message!!, color = if (message!!.contains("correctamente")) Color(0xFF8FE0B0) else Color(0xFFFF9C9C), fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    Button(onClick = {
                        saving = true; message = null
                        scope.launch {
                            repository.updateProfile(displayName, username, bio, avatar, banner, theme, frame, badge, pronouns, status, accent, cardStyle, badges, isPublic, linkLabel, linkUrl)
                                .onSuccess { profile = it; message = "Cambios guardados correctamente" }
                                .onFailure { message = it.localizedMessage ?: "No se pudo guardar el perfil" }
                            saving = false
                        }
                    }, enabled = !loading && !saving, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF171827))) {
                        if (saving) CircularProgressIndicator(Modifier.size(18.dp), color = Color(0xFF171827), strokeWidth = 2.dp) else Text("Guardar personalización", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 25.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileTab("Principal", tab == 0, accentColor, Modifier.weight(1f)) { tab = 0 }
                ProfileTab("Favoritos", tab == 1, accentColor, Modifier.weight(1f)) { tab = 1 }
                ProfileTab("Acerca de", tab == 2, accentColor, Modifier.weight(1f)) { tab = 2 }
            }
            when (tab) {
                0 -> {
                    ProfilePanel(cardBackground, accentColor, "Actividad", "Tu espacio musical", "Cuando reproduzcas música, podrás compartir aquí lo que estás escuchando.", Icons.Default.MusicNote)
                    ProfilePanel(cardBackground, accentColor, "Emblemas", "Colección del perfil", badges.ifBlank { "Todavía no has añadido emblemas." }, Icons.Default.Badge)
                }
                1 -> ProfilePanel(cardBackground, accentColor, "Favoritos", "Tu colección", "Tus canciones favoritas sincronizadas con Kokoro aparecen en tu biblioteca.", Icons.Default.Favorite)
                else -> ProfilePanel(cardBackground, accentColor, "Información", if (isPublic) "Perfil público" else "Perfil privado", "Miembro de Pokke · ${session?.email.orEmpty()}", Icons.Default.Public)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable private fun ProfileTopButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(Color.Black.copy(.34f)).clickable(onClick = onClick), contentAlignment = Alignment.Center, content = { content() })
}

@Composable private fun ProfileField(label: String, value: String, multiline: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = !multiline, minLines = if (multiline) 3 else 1,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ProfileInk, unfocusedTextColor = ProfileInk,
            focusedBorderColor = Color(0xFFAAA0FF), unfocusedBorderColor = Color.White.copy(.16f),
            focusedLabelColor = Color(0xFFAAA0FF), unfocusedLabelColor = ProfileMuted, cursorColor = Color(0xFFAAA0FF)))
}

@Composable private fun ProfileChoice(title: String, selected: String, values: List<String>, labels: List<String>, accent: Color, onSelect: (String) -> Unit) {
    Text(title, color = ProfileMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 7.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEachIndexed { index, value ->
            val active = selected == value
            Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (active) accent.copy(.25f) else Color.White.copy(.055f))
                .border(if (active) 1.dp else 0.dp, if (active) accent else Color.Transparent, RoundedCornerShape(9.dp))
                .clickable { onSelect(value) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(labels[index], color = if (active) accent else ProfileMuted, fontSize = 9.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}

@Composable private fun ProfileTab(label: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(onClick = onClick).padding(vertical = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (selected) accent else ProfileMuted, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        Spacer(Modifier.height(8.dp)); Box(Modifier.fillMaxWidth().height(if (selected) 3.dp else 1.dp).clip(CircleShape).background(if (selected) accent else Color.White.copy(.10f)))
    }
}

@Composable private fun ProfilePanel(background: Color, accent: Color, title: String, heading: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(Modifier.fillMaxWidth().padding(top = 15.dp).clip(RoundedCornerShape(20.dp)).background(background).padding(17.dp)) {
        Text(title, color = ProfileInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(.15f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent) }
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text(heading, color = ProfileInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(body, color = ProfileMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

private fun frameColor(frame: String) = when (frame) {
    "blue" -> Color(0xFF8CB7FF); "pink" -> Color(0xFFFF9FCB); "gold" -> Color(0xFFE5C17A); else -> Color(0xFFAAA0FF)
}
