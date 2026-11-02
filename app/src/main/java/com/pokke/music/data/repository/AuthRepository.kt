package com.pokke.music.data.repository

import com.pokke.music.data.model.UserProfile
import com.pokke.music.data.remote.KokoroConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Kokoro Supabase Auth + Profiles
 */
class AuthRepository {

    data class AuthSession(
        val accessToken: String,
        val refreshToken: String?,
        val userId: String,
        val email: String?
    )

    suspend fun signIn(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${KokoroConfig.SUPABASE_URL}${KokoroConfig.AUTH_TOKEN}")
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("apikey", KokoroConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(body.toByteArray()) }

            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            AuthSession(
                accessToken = json.getString("access_token"),
                refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                userId = json.getJSONObject("user").getString("id"),
                email = json.getJSONObject("user").optString("email")
            )
        }
    }

    suspend fun signUp(email: String, password: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${KokoroConfig.SUPABASE_URL}${KokoroConfig.AUTH_SIGNUP}")
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("apikey", KokoroConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(body.toByteArray()) }

            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            // signup may return user without session depending on confirm email settings
            val user = json.optJSONObject("user") ?: json
            AuthSession(
                accessToken = json.optString("access_token"),
                refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() },
                userId = user.getString("id"),
                email = user.optString("email")
            )
        }
    }

    suspend fun getProfile(userId: String, accessToken: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${KokoroConfig.SUPABASE_URL}${KokoroConfig.PROFILES}?id=eq.$userId&select=*")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", KokoroConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
            }
            val body = conn.inputStream.bufferedReader().readText()
            val arr = org.json.JSONArray(body)
            if (arr.length() == 0) null
            else {
                val o = arr.getJSONObject(0)
                UserProfile(
                    id = o.getString("id"),
                    displayName = o.optString("display_name").takeIf { it.isNotBlank() },
                    username = o.optString("username").takeIf { it.isNotBlank() },
                    avatarUrl = o.optString("avatar_url").takeIf { it.isNotBlank() },
                    bannerUrl = o.optString("banner_url").takeIf { it.isNotBlank() },
                    bio = o.optString("bio").takeIf { it.isNotBlank() },
                    theme = o.optString("theme", "default"),
                    frame = o.optString("frame", "none"),
                    isPublic = o.optBoolean("is_public", true),
                    isArtist = o.optBoolean("is_artist", false),
                    badge = o.optString("badge", "none")
                )
            }
        }
    }

    suspend fun signOut(accessToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${KokoroConfig.SUPABASE_URL}${KokoroConfig.AUTH_LOGOUT}")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", KokoroConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
            }
            conn.responseCode // just trigger
            Unit
        }
    }
}
