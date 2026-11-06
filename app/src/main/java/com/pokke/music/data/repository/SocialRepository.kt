package com.pokke.music.data.repository

import com.pokke.music.data.remote.KokoroConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** REST client for the first Pokke Social alpha endpoints. */
class SocialRepository(private val auth: AuthRepository) {
    data class Friend(val id: String, val username: String, val displayName: String, val avatarUrl: String?)
    data class Conversation(val id: String, val friend: Friend, val lastMessage: String?, val lastMessageAt: String?)
    data class Message(val id: String, val senderId: String, val type: String, val body: String?, val mediaUrl: String?, val createdAt: String)

    private fun request(path: String, method: String = "GET", body: String? = null, prefer: String? = null): String {
        val session = auth.session.value ?: throw IOException("Inicia sesión para usar Pokke Social")
        val url = URL(KokoroConfig.SUPABASE_URL + path)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("apikey", KokoroConfig.SUPABASE_ANON_KEY)
            setRequestProperty("Authorization", "Bearer ${session.accessToken}")
            setRequestProperty("Content-Type", "application/json")
            if (prefer != null) setRequestProperty("Prefer", prefer)
            if (body != null) doOutput = true
        }
        if (body != null) conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
        conn.disconnect()
        if (code !in 200..299) throw IOException("Pokke Social HTTP $code: $text")
        return text
    }

    suspend fun friends(): Result<List<Friend>> = withContext(Dispatchers.IO) { runCatching {
        val rows = JSONArray(request("/rest/v1/rpc/pokke_my_friends", "POST", "{}"))
        (0 until rows.length()).map { i -> rows.getJSONObject(i).let { Friend(it.optString("friend_id"), it.optString("username"), it.optString("display_name"), it.optString("avatar_url").takeIf(String::isNotBlank)) } }
    } }

    suspend fun conversations(): Result<List<Conversation>> = withContext(Dispatchers.IO) { runCatching {
        val rows = JSONArray(request("/rest/v1/rpc/pokke_my_conversations", "POST", "{}"))
        (0 until rows.length()).map { i -> rows.getJSONObject(i).let { row ->
            Conversation(row.optString("conversation_id"), Friend(row.optString("friend_id"), row.optString("friend_username"), row.optString("friend_display_name"), row.optString("friend_avatar_url").takeIf(String::isNotBlank)), row.optString("last_message").takeIf(String::isNotBlank), row.optString("last_message_at").takeIf(String::isNotBlank))
        } }
    } }

    suspend fun openConversation(friendId: String): Result<String> = withContext(Dispatchers.IO) { runCatching {
        val encoded = JSONObject().put("friend_uuid", friendId).toString()
        request("/rest/v1/rpc/pokke_open_direct_conversation", "POST", encoded).trim().removeSurrounding("\"")
    } }

    suspend fun messages(conversationId: String): Result<List<Message>> = withContext(Dispatchers.IO) { runCatching {
        val id = URLEncoder.encode(conversationId, "UTF-8")
        val rows = JSONArray(request("/rest/v1/pokke_messages?select=id,sender_id,message_type,body,media_url,created_at&conversation_id=eq.$id&deleted_at=is.null&order=created_at.asc&limit=200"))
        (0 until rows.length()).map { i -> rows.getJSONObject(i).let { Message(it.optString("id"), it.optString("sender_id"), it.optString("message_type"), it.optString("body").takeIf(String::isNotBlank), it.optString("media_url").takeIf(String::isNotBlank), it.optString("created_at")) } }
    } }

    suspend fun sendText(conversationId: String, text: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        require(text.isNotBlank()) { "Escribe un mensaje" }
        val sender = auth.session.value?.userId ?: throw IOException("Inicia sesión")
        val payload = JSONArray().put(JSONObject().put("conversation_id", conversationId).put("sender_id", sender).put("message_type", "text").put("body", text.trim()))
        request("/rest/v1/pokke_messages", "POST", payload.toString(), "return=minimal")
        Unit
    } }
}
