package com.pokke.music.data.youtube

import com.pokke.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Catálogo YouTube Music vía InnerTube (cliente ANDROID_MUSIC).
 * OFF por defecto — activar en Settings. Sin PoToken: sirve para búsqueda;
 * streams de alta calidad pueden requerir poToken más adelante.
 */
class YouTubeCatalogRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    suspend fun search(query: String, limit: Int = 30): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            if (query.isBlank()) return@runCatching emptyList()
            val body = JSONObject()
                .put("context", JSONObject()
                    .put("client", JSONObject()
                        .put("clientName", "ANDROID_MUSIC")
                        .put("clientVersion", "7.27.52")
                        .put("hl", "es")
                        .put("gl", "US")
                    )
                )
                .put("query", query)
                .put("params", "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D") // songs filter when accepted
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val req = Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/search?prettyPrint=false")
                .header("User-Agent", "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 14)")
                .header("X-YouTube-Client-Name", "21")
                .header("X-YouTube-Client-Version", "7.27.52")
                .post(body)
                .build()

            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) error("YT search HTTP ${resp.code}")
                parseSongs(text, limit)
            }
        }
    }

    private fun parseSongs(json: String, limit: Int): List<Song> {
        val root = JSONObject(json)
        val out = ArrayList<Song>()
        fun walk(node: Any?) {
            if (out.size >= limit || node == null) return
            when (node) {
                is JSONObject -> {
                    if (node.has("musicResponsiveListItemRenderer")) {
                        parseItem(node.getJSONObject("musicResponsiveListItemRenderer"))?.let { out.add(it) }
                    }
                    node.keys().forEach { k -> walk(node.opt(k)) }
                }
                is JSONArray -> {
                    for (i in 0 until node.length()) walk(node.opt(i))
                }
            }
        }
        walk(root)
        return out.distinctBy { it.id }.take(limit)
    }

    private fun parseItem(item: JSONObject): Song? {
        return runCatching {
            val videoId = item.optJSONObject("playlistItemData")?.optString("videoId")
                ?: findVideoId(item)
                ?: return null
            val flex = item.optJSONArray("flexColumns") ?: return null
            val title = flex.optJSONObject(0)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
                ?.optJSONObject(0)
                ?.optString("text")
                ?: "YouTube"
            val artist = flex.optJSONObject(1)
                ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")
                ?.optJSONArray("runs")
                ?.optJSONObject(0)
                ?.optString("text")
                ?: "YouTube Music"
            val thumb = item.optJSONObject("thumbnail")
                ?.optJSONObject("musicThumbnailRenderer")
                ?.optJSONObject("thumbnail")
                ?.optJSONArray("thumbnails")
            val cover = thumb?.optJSONObject(thumb.length() - 1)?.optString("url")
            Song(
                id = "yt_$videoId",
                title = title,
                artist = artist,
                album = "YouTube Music",
                coverUrl = cover,
                audioUrl = null, // stream resolve later / PoToken
                durationSeconds = 0,
                youtubeId = videoId
            )
        }.getOrNull()
    }

    private fun findVideoId(o: JSONObject): String? {
        val s = o.toString()
        val m = Regex("\"videoId\"\\s*:\\s*\"([a-zA-Z0-9_-]{11})\"").find(s)
        return m?.groupValues?.getOrNull(1)
    }
}
