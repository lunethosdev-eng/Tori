package com.pokke.music.data.repository

import android.content.Context
import com.pokke.music.data.model.Playlist
import com.pokke.music.data.model.Song
import com.pokke.music.data.model.UserProfile
import com.pokke.music.data.remote.KokoroConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Kokoro account, profile, favorites and playlist repository. */
class AuthRepository(context: Context) {
    data class AuthSession(
        val accessToken: String,
        val refreshToken: String?,
        val userId: String,
        val email: String?,
        val expiresAt: Long
    )

    private val store = SecureSessionStore(context)
    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session

    suspend fun restoreSession(): Result<AuthSession?> = withContext(Dispatchers.IO) {
        runCatching {
            var saved = store.read()
            if (saved != null && saved.expiresAt <= System.currentTimeMillis() / 1000 + 90) {
                val refreshed = refreshSessionInternal(saved)
                if (refreshed != null) saved = refreshed
                else if (saved.expiresAt <= System.currentTimeMillis() / 1000) {
                    store.clear()
                    saved = null
                }
            }
            _session.value = saved
            saved
        }
    }

    suspend fun signIn(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank() && password.isNotBlank()) { "Completa el correo y la contraseña" }
            val json = authRequest(KokoroConfig.AUTH_TOKEN, JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            })
            val session = parseSession(json)
            store.save(session)
            _session.value = session
            ensureProfile(session, email.substringBefore('@'))
            session
        }
    }

    /** Returns null when email confirmation is required and Supabase did not issue a session. */
    suspend fun signUp(email: String, password: String, displayName: String): Result<AuthSession?> = withContext(Dispatchers.IO) {
        runCatching {
            require(email.isNotBlank() && password.length >= 6) { "Usa un correo válido y una contraseña de al menos 6 caracteres" }
            val json = authRequest(KokoroConfig.AUTH_SIGNUP, JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("data", JSONObject().put("display_name", displayName.trim().ifBlank { email.substringBefore('@') }))
            })
            val token = json.optString("access_token")
            if (token.isBlank()) return@runCatching null
            val session = parseSession(json)
            store.save(session)
            _session.value = session
            ensureProfile(session, displayName.ifBlank { email.substringBefore('@') })
            session
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val current = currentSession()
            if (current != null) runCatching { request(KokoroConfig.AUTH_LOGOUT, "POST", current, body = "{}") }
            store.clear()
            _session.value = null
        }
    }

    suspend fun getProfile(): Result<UserProfile?> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val rows = JSONArray(request(KokoroConfig.PROFILES, "GET", s, params = mapOf("id" to "eq.${s.userId}", "select" to "*", "limit" to "1")))
            if (rows.length() == 0) null else parseProfile(rows.getJSONObject(0))
        }
    }

    suspend fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        avatarUrl: String? = null,
        bannerUrl: String? = null,
        theme: String? = null,
        frame: String? = null,
        badge: String? = null,
        pronouns: String? = null,
        statusText: String? = null,
        accentColor: String? = null,
        cardStyle: String? = null,
        badges: String? = null,
        isPublic: Boolean? = null,
        linkLabel: String? = null,
        linkUrl: String? = null,
        showListening: Boolean? = null,
        extraLinks: String? = null
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            require(username.matches(Regex("[a-zA-Z0-9_.]{3,24}"))) { "El usuario debe tener 3–24 caracteres: letras, números, _ o ." }
            val body = JSONObject().apply {
                put("id", s.userId)
                put("display_name", displayName.trim())
                put("username", username.trim().lowercase())
                put("bio", bio.trim())
                if (avatarUrl != null) { if (avatarUrl.isBlank()) put("avatar_url", JSONObject.NULL) else put("avatar_url", avatarUrl.trim()) }
                if (bannerUrl != null) { if (bannerUrl.isBlank()) put("banner_url", JSONObject.NULL) else put("banner_url", bannerUrl.trim()) }
                if (theme != null) put("theme", theme)
                if (frame != null) put("frame", frame)
                if (badge != null) put("badge", badge)
                if (pronouns != null) put("pronouns", pronouns)
                if (statusText != null) put("status_text", statusText)
                if (accentColor != null) put("accent_color", accentColor)
                if (cardStyle != null) put("card_style", cardStyle)
                if (badges != null) put("badges", badges)
                if (isPublic != null) put("is_public", isPublic)
                if (linkLabel != null) put("link_label", linkLabel)
                if (linkUrl != null) put("link_url", linkUrl)
                if (showListening != null) put("show_listening", showListening)
                if (extraLinks != null) put("extra_links", extraLinks)
            }
            val rows = JSONArray(request(KokoroConfig.PROFILES, "POST", s, params = mapOf("on_conflict" to "id"), body = body.toString(), prefer = "resolution=merge-duplicates,return=representation"))
            if (rows.length() == 0) throw IOException("Kokoro no devolvió el perfil actualizado")
            parseProfile(rows.getJSONObject(0))
        }
    }

    /** Public lookup by username (only returns is_public = true profiles unless it's your own). */
    suspend fun getProfileByUsername(username: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        runCatching {
            val clean = username.trim().lowercase().removePrefix("@")
            require(clean.isNotBlank()) { "Escribe un nombre de usuario" }
            val s = _session.value
            val params = mutableMapOf(
                "username" to "eq.$clean",
                "select" to "*",
                "limit" to "1"
            )
            val body = if (s != null) {
                request(KokoroConfig.PROFILES, "GET", s, params = params)
            } else {
                // Anonymous read (requires RLS allowing public profiles)
                http(KokoroConfig.PROFILES, "GET", params)
            }
            val rows = JSONArray(body)
            if (rows.length() == 0) null else {
                val p = parseProfile(rows.getJSONObject(0))
                if (!p.isPublic && p.id != s?.userId) null else p
            }
        }
    }

    /** Search public profiles by username or display name. */
    suspend fun searchUsers(query: String, limit: Int = 30): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        runCatching {
            require(query.isNotBlank()) { "Escribe algo para buscar" }
            val term = query.trim().lowercase().removePrefix("@")
            val pattern = "*$term*"
            val s = _session.value
            val params = mapOf(
                "select" to "id,display_name,username,avatar_url,banner_url,bio,theme,frame,is_public,badge,pronouns,status_text,accent_color,card_style,badges,link_label,link_url,show_listening",
                "or" to "(username.ilike.$pattern,display_name.ilike.$pattern)",
                "is_public" to "eq.true",
                "order" to "username.asc",
                "limit" to limit.coerceIn(1, 50).toString()
            )
            val body = if (s != null) request(KokoroConfig.PROFILES, "GET", s, params = params)
            else http(KokoroConfig.PROFILES, "GET", params)
            val rows = JSONArray(body)
            (0 until rows.length()).map { parseProfile(rows.getJSONObject(it)) }
        }
    }

    suspend fun getFavorites(): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val rows = JSONArray(request(KokoroConfig.FAVORITES, "GET", s, params = mapOf(
                "user_id" to "eq.${s.userId}", "select" to "song_id,title,artist,cover_url,duration,stream_url", "order" to "created_at.desc"
            )))
            (0 until rows.length()).map { i ->
                val row = rows.getJSONObject(i)
                Song(
                    id = row.optString("song_id"), title = row.optString("title"), artist = row.optString("artist"),
                    coverUrl = row.optString("cover_url").nullable(), audioUrl = row.optString("stream_url").nullable(),
                    durationSeconds = row.optInt("duration", 0)
                )
            }
        }
    }

    suspend fun isFavorite(songId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val rows = JSONArray(request(KokoroConfig.FAVORITES, "GET", s, params = mapOf(
                "user_id" to "eq.${s.userId}", "song_id" to "eq.$songId", "select" to "id", "limit" to "1"
            )))
            rows.length() > 0
        }
    }

    suspend fun setFavorite(song: Song, favorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            if (favorite) {
                val body = JSONObject().apply {
                    put("user_id", s.userId); put("song_id", song.id); put("title", song.title); put("artist", song.artist)
                    put("cover_url", song.displayCoverUrl); put("duration", song.durationSeconds); put("stream_url", song.audioUrl)
                }
                request(KokoroConfig.FAVORITES, "POST", s, params = mapOf("on_conflict" to "user_id,song_id"), body = body.toString(), prefer = "resolution=merge-duplicates,return=minimal")
            } else {
                request(KokoroConfig.FAVORITES, "DELETE", s, params = mapOf("user_id" to "eq.${s.userId}", "song_id" to "eq.${song.id}"))
            }
            Unit
        }
    }

    suspend fun getPlaylists(): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val rows = JSONArray(request(KokoroConfig.PLAYLISTS, "GET", s, params = mapOf(
                "user_id" to "eq.${s.userId}", "select" to "id,name,description,cover_url,is_public,created_at", "order" to "updated_at.desc"
            )))
            (0 until rows.length()).map { i ->
                val row = rows.getJSONObject(i)
                val id = row.getString("id")
                val countRows = JSONArray(request(KokoroConfig.PLAYLIST_SONGS, "GET", s, params = mapOf("playlist_id" to "eq.$id", "select" to "id")))
                Playlist(id, row.optString("name"), row.optString("cover_url").nullable(), row.optString("description").nullable(), row.optBoolean("is_public"), countRows.length())
            }
        }
    }

    suspend fun createPlaylist(name: String, description: String = ""): Result<Playlist> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            require(name.isNotBlank()) { "Escribe un nombre para la playlist" }
            val rows = JSONArray(request(KokoroConfig.PLAYLISTS, "POST", s, body = JSONObject().apply {
                put("user_id", s.userId); put("name", name.trim()); put("description", description.trim()); put("is_public", false)
            }.toString(), prefer = "return=representation"))
            if (rows.length() == 0) throw IOException("No se pudo crear la playlist")
            val row = rows.getJSONObject(0)
            Playlist(row.getString("id"), row.getString("name"), row.optString("cover_url").nullable(), row.optString("description").nullable(), row.optBoolean("is_public"), 0)
        }
    }

    suspend fun getPlaylistSongs(playlistId: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val rows = JSONArray(request(KokoroConfig.PLAYLIST_SONGS, "GET", s, params = mapOf(
                "playlist_id" to "eq.$playlistId", "select" to "song_id,title,artist,cover_url,duration,stream_url", "order" to "position.asc,added_at.asc"
            )))
            (0 until rows.length()).map { i ->
                val row = rows.getJSONObject(i)
                Song(row.optString("song_id"), row.optString("title"), row.optString("artist"), coverUrl = row.optString("cover_url").nullable(), audioUrl = row.optString("stream_url").nullable(), durationSeconds = row.optInt("duration"))
            }
        }
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            val existing = JSONArray(request(KokoroConfig.PLAYLIST_SONGS, "GET", s, params = mapOf(
                "playlist_id" to "eq.$playlistId", "song_id" to "eq.${song.id}", "select" to "id", "limit" to "1"
            )))
            if (existing.length() == 0) {
                val body = JSONObject().apply {
                    put("playlist_id", playlistId); put("song_id", song.id); put("title", song.title); put("artist", song.artist)
                    put("cover_url", song.displayCoverUrl); put("duration", song.durationSeconds); put("stream_url", song.audioUrl)
                }
                request(KokoroConfig.PLAYLIST_SONGS, "POST", s, body = body.toString(), prefer = "return=minimal")
            }
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            request(KokoroConfig.PLAYLIST_SONGS, "DELETE", s, params = mapOf("playlist_id" to "eq.$playlistId", "song_id" to "eq.$songId"))
            Unit
        }
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val s = requireSession()
            request(KokoroConfig.PLAYLIST_SONGS, "DELETE", s, params = mapOf("playlist_id" to "eq.$playlistId"))
            request(KokoroConfig.PLAYLISTS, "DELETE", s, params = mapOf("id" to "eq.$playlistId"))
            Unit
        }
    }

    private suspend fun currentSession(): AuthSession? {
        var s = _session.value ?: store.read()?.also { _session.value = it } ?: return null
        if (s.expiresAt <= System.currentTimeMillis() / 1000 + 90) {
            val refreshed = refreshSessionInternal(s)
            if (refreshed != null) {
                s = refreshed
                _session.value = s
            } else if (s.expiresAt <= System.currentTimeMillis() / 1000) {
                store.clear()
                _session.value = null
                return null
            }
        }
        return s
    }

    private suspend fun requireSession(): AuthSession = currentSession() ?: throw IOException("Inicia sesión para sincronizar tus datos")

    private fun refreshSessionInternal(old: AuthSession): AuthSession? {
        val refresh = old.refreshToken ?: return null
        return runCatching {
            val json = authRequest(KokoroConfig.AUTH_REFRESH, JSONObject().put("refresh_token", refresh))
            parseSession(json).also { store.save(it) }
        }.getOrNull()
    }

    private fun ensureProfile(session: AuthSession, suggestedName: String) {
        runCatching {
            val existing = JSONArray(request(KokoroConfig.PROFILES, "GET", session, params = mapOf("id" to "eq.${session.userId}", "select" to "id", "limit" to "1")))
            if (existing.length() == 0) {
                val body = JSONObject().put("id", session.userId).put("display_name", suggestedName)
                request(KokoroConfig.PROFILES, "POST", session, body = body.toString(), prefer = "return=minimal")
            }
        }
    }

    private fun parseSession(json: JSONObject): AuthSession {
        val user = json.optJSONObject("user") ?: throw IOException("Supabase no devolvió el usuario")
        val access = json.optString("access_token")
        if (access.isBlank()) throw IOException("Supabase no devolvió el token de acceso")
        val expiresAt = json.optLong("expires_at", System.currentTimeMillis() / 1000 + json.optLong("expires_in", 3600))
        return AuthSession(access, json.optString("refresh_token").nullable(), user.getString("id"), user.optString("email").nullable(), expiresAt)
    }

    private fun parseProfile(o: JSONObject) = UserProfile(
        id = o.getString("id"),
        displayName = o.optString("display_name").nullable(),
        username = o.optString("username").nullable(),
        avatarUrl = o.optString("avatar_url").nullable(),
        bannerUrl = o.optString("banner_url").nullable(),
        bio = o.optString("bio").nullable(),
        theme = o.optString("theme", "default"),
        frame = o.optString("frame", "none"),
        isPublic = o.optBoolean("is_public", true),
        isArtist = o.optBoolean("is_artist", false),
        badge = o.optString("badge", "none"),
        pronouns = o.optString("pronouns").nullable(),
        statusText = o.optString("status_text").nullable(),
        accentColor = o.optString("accent_color", "violet"),
        cardStyle = o.optString("card_style", "glass"),
        badges = o.optString("badges").nullable(),
        linkLabel = o.optString("link_label").nullable(),
        linkUrl = o.optString("link_url").nullable(),
        showListening = if (o.has("show_listening")) o.optBoolean("show_listening", true) else true,
        extraLinks = o.optString("extra_links").nullable()
    )

    private fun authRequest(path: String, body: JSONObject): JSONObject = JSONObject(http(path, "POST", body = body.toString(), authToken = null))

    private fun request(
        path: String, method: String, session: AuthSession, params: Map<String, String> = emptyMap(),
        body: String? = null, prefer: String? = null
    ): String = http(path, method, params, body, session.accessToken, prefer)

    private fun http(
        path: String, method: String, params: Map<String, String> = emptyMap(), body: String? = null,
        authToken: String? = null, prefer: String? = null
    ): String {
        val query = if (params.isEmpty()) "" else params.entries.joinToString("&", prefix = "?") { "${encode(it.key)}=${encode(it.value)}" }
        val connection = (URL("${KokoroConfig.SUPABASE_URL}$path$query").openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 15_000; readTimeout = 20_000
            setRequestProperty("apikey", KokoroConfig.SUPABASE_PUBLISHABLE_KEY)
            if (authToken != null) setRequestProperty("Authorization", "Bearer $authToken")
            setRequestProperty("Accept", "application/json")
            if (body != null) { doOutput = true; setRequestProperty("Content-Type", "application/json; charset=utf-8") }
            if (prefer != null) setRequestProperty("Prefer", prefer)
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val detail = runCatching {
                    val e = JSONObject(response)
                    e.optString("msg").ifBlank { e.optString("message") }.ifBlank { e.optString("error_description") }.ifBlank { e.optString("error") }
                }.getOrNull().orEmpty()
                throw IOException("Kokoro respondió HTTP $status${detail.takeIf(String::isNotBlank)?.let { ": $it" } ?: ""}")
            }
            return response
        } finally { connection.disconnect() }
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")
    private fun String?.nullable() = this?.takeIf { it.isNotBlank() && it != "null" }
}
