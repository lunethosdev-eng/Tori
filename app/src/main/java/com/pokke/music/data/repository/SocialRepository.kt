package com.pokke.music.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SocialRepository(private val auth: AuthRepository) {

    data class Friend(val id: String, val username: String, val displayName: String, val avatarUrl: String?)
    data class Conversation(
        val id: String,
        val friend: Friend,
        val lastMessage: String?,
        val lastMessageAt: String?,
        val unread: Int = 0,
        val nickname: String? = null
    )
    data class Message(
        val id: String,
        val senderId: String,
        val type: String,
        val body: String?,
        val mediaUrl: String?,
        val createdAt: String
    )
    data class Story(
        val id: String,
        val userId: String,
        val username: String?,
        val displayName: String?,
        val avatarUrl: String?,
        val mediaUrl: String,
        val mediaType: String,
        val caption: String?,
        val createdAt: String,
        val viewed: Boolean
    )
    data class FriendLocation(
        val userId: String,
        val username: String?,
        val displayName: String?,
        val lat: Double,
        val lng: Double,
        val shareMusic: Boolean,
        val shareBattery: Boolean,
        val batteryPct: Int?,
        val currentTrack: String?,
        val updatedAt: String
    )

    private fun session() = auth.session.value ?: throw IOException("Inicia sesión")

    private fun request(
        path: String,
        method: String = "GET",
        body: String? = null,
        prefer: String? = null
    ): String {
        val s = session()
        val base = com.pokke.music.data.remote.KokoroConfig.SUPABASE_URL
        val key = com.pokke.music.data.remote.KokoroConfig.SUPABASE_PUBLISHABLE_KEY
        val url = if (path.startsWith("http")) path else "$base$path"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("apikey", key)
            setRequestProperty("Authorization", "Bearer ${s.accessToken}")
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            if (prefer != null) setRequestProperty("Prefer", prefer)
        }
        try {
            if (body != null) conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream?.bufferedReader()?.readText().orEmpty()
            if (code !in 200..299) throw IOException("HTTP $code: ${resp.take(300)}")
            return resp
        } finally {
            conn.disconnect()
        }
    }

    suspend fun friends(): Result<List<Friend>> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = JSONArray(request("/rest/v1/rpc/pokke_my_friends", "POST", "{}"))
            (0 until rows.length()).map { i ->
                rows.getJSONObject(i).let {
                    Friend(it.optString("friend_id"), it.optString("username"), it.optString("display_name"), it.optString("avatar_url").takeIf(String::isNotBlank))
                }
            }
        }
    }

    suspend fun conversations(): Result<List<Conversation>> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = JSONArray(request("/rest/v1/rpc/pokke_my_conversations", "POST", "{}"))
            (0 until rows.length()).map { i ->
                val row = rows.getJSONObject(i)
                Conversation(
                    id = row.optString("conversation_id"),
                    friend = Friend(row.optString("friend_id"), row.optString("friend_username"), row.optString("friend_display_name"), row.optString("friend_avatar_url").takeIf(String::isNotBlank)),
                    lastMessage = row.optString("last_message").takeIf(String::isNotBlank),
                    lastMessageAt = row.optString("last_message_at").takeIf(String::isNotBlank),
                    unread = row.optInt("unread_count", 0),
                    nickname = row.optString("nickname").takeIf(String::isNotBlank)
                )
            }
        }
    }

    suspend fun openConversation(friendId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            request("/rest/v1/rpc/pokke_open_direct_conversation", "POST", JSONObject().put("friend_uuid", friendId).toString())
                .trim().removeSurrounding("\"")
        }
    }

    suspend fun messages(conversationId: String): Result<List<Message>> = withContext(Dispatchers.IO) {
        runCatching {
            val id = URLEncoder.encode(conversationId, "UTF-8")
            val rows = JSONArray(
                request("/rest/v1/pokke_messages?select=id,sender_id,message_type,body,media_url,created_at&conversation_id=eq.$id&deleted_at=is.null&order=created_at.asc&limit=200")
            )
            (0 until rows.length()).map { i ->
                rows.getJSONObject(i).let {
                    Message(it.optString("id"), it.optString("sender_id"), it.optString("message_type"), it.optString("body").takeIf(String::isNotBlank), it.optString("media_url").takeIf(String::isNotBlank), it.optString("created_at"))
                }
            }
        }
    }

    suspend fun sendText(conversationId: String, text: String): Result<Unit> =
        sendMessage(conversationId, "text", text.trim(), null)

    suspend fun sendImage(conversationId: String, mediaUrl: String, caption: String? = null): Result<Unit> =
        sendMessage(conversationId, "image", caption, mediaUrl)

    suspend fun sendGif(conversationId: String, mediaUrl: String): Result<Unit> =
        sendMessage(conversationId, "gif", null, mediaUrl)

    suspend fun sendAudio(conversationId: String, mediaUrl: String): Result<Unit> =
        sendMessage(conversationId, "audio", null, mediaUrl)

    private suspend fun sendMessage(conversationId: String, type: String, body: String?, mediaUrl: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (type == "text") require(!body.isNullOrBlank()) { "Escribe un mensaje" }
                val obj = JSONObject()
                    .put("conversation_id", conversationId)
                    .put("sender_id", session().userId)
                    .put("message_type", type)
                if (!body.isNullOrBlank()) obj.put("body", body)
                if (!mediaUrl.isNullOrBlank()) obj.put("media_url", mediaUrl)
                request("/rest/v1/pokke_messages", "POST", JSONArray().put(obj).toString(), "return=minimal")
                Unit
            }
        }

    suspend fun activeStories(): Result<List<Story>> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = JSONArray(request("/rest/v1/rpc/pokke_active_stories", "POST", "{}"))
            (0 until rows.length()).map { i ->
                rows.getJSONObject(i).let {
                    Story(
                        it.optString("id"), it.optString("user_id"),
                        it.optString("username").takeIf(String::isNotBlank),
                        it.optString("display_name").takeIf(String::isNotBlank),
                        it.optString("avatar_url").takeIf(String::isNotBlank),
                        it.optString("media_url"), it.optString("media_type", "image"),
                        it.optString("caption").takeIf(String::isNotBlank),
                        it.optString("created_at"), it.optBoolean("viewed", false)
                    )
                }
            }
        }
    }

    suspend fun publishStory(mediaPath: String, mediaType: String, caption: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val obj = JSONObject()
                    .put("user_id", session().userId)
                    .put("media_path", mediaPath)
                    .put("media_type", mediaType)
                    .put("expires_at", java.time.Instant.now().plusSeconds(24 * 3600).toString())
                if (!caption.isNullOrBlank()) obj.put("caption", caption)
                request("/rest/v1/pokke_stories", "POST", JSONArray().put(obj).toString(), "return=minimal")
                Unit
            }
        }

    suspend fun markStoryViewed(storyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request(
                "/rest/v1/pokke_story_views",
                "POST",
                JSONArray().put(JSONObject().put("story_id", storyId).put("viewer_id", session().userId)).toString(),
                "return=minimal"
            )
            Unit
        }
    }

    suspend fun updateMyLocation(
        lat: Double, lng: Double, shareWith: String,
        shareMusic: Boolean, shareBattery: Boolean, batteryPct: Int?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val obj = JSONObject()
                .put("user_id", session().userId)
                .put("latitude", lat)
                .put("longitude", lng)
                .put("is_sharing", shareWith != "none")
                .put("share_music", shareMusic)
                .put("share_battery", shareBattery)
                .put("updated_at", java.time.Instant.now().toString())
            if (batteryPct != null) obj.put("battery_percent", batteryPct)
            request(
                "/rest/v1/pokke_user_locations?on_conflict=user_id",
                "POST",
                JSONArray().put(obj).toString(),
                "resolution=merge-duplicates,return=minimal"
            )
            Unit
        }
    }

    suspend fun stopSharingLocation(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            request(
                "/rest/v1/pokke_user_locations?user_id=eq.${session().userId}",
                "PATCH",
                JSONObject().put("is_sharing", false).toString(),
                "return=minimal"
            )
            Unit
        }
    }

    suspend fun friendLocations(): Result<List<FriendLocation>> = withContext(Dispatchers.IO) {
        runCatching {
            val rows = JSONArray(request("/rest/v1/rpc/pokke_friend_locations", "POST", "{}"))
            (0 until rows.length()).map { i ->
                rows.getJSONObject(i).let {
                    FriendLocation(
                        it.optString("user_id"),
                        it.optString("username").takeIf(String::isNotBlank),
                        it.optString("display_name").takeIf(String::isNotBlank),
                        it.optDouble("lat"), it.optDouble("lng"),
                        it.optBoolean("share_music", false),
                        it.optBoolean("share_battery", false),
                        if (it.has("battery_pct") && !it.isNull("battery_pct")) it.optInt("battery_pct") else null,
                        it.optString("current_track").takeIf(String::isNotBlank),
                        it.optString("updated_at")
                    )
                }
            }
        }
    }
}
