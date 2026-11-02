package com.pokke.music.data.repository

import com.pokke.music.data.model.Song
import com.pokke.music.data.remote.UkiyoConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Talks to Ukiyo Server (songs catalog).
 * Uses simple HttpURLConnection so we don't force Retrofit yet.
 * Replace the URL/key in UkiyoConfig with your real values.
 */
class MusicRepository {

    suspend fun searchSongs(query: String, limit: Int = 30): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode("%$query%", "UTF-8")
            val url = URL(
                "${UkiyoConfig.SUPABASE_URL}${UkiyoConfig.SONGS}" +
                    "?or=(title.ilike.$encoded,artist.ilike.$encoded)" +
                    "&limit=$limit" +
                    "&select=id,title,artist,album,cover_url,animated_cover_url,audio_url,duration_seconds,lyrics_text,lyrics_url,genre,youtube_id"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", UkiyoConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${UkiyoConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Accept", "application/json")
            }
            val body = conn.inputStream.bufferedReader().readText()
            parseSongs(JSONArray(body))
        }
    }

    suspend fun getRecentSongs(limit: Int = 40): Result<List<Song>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "${UkiyoConfig.SUPABASE_URL}${UkiyoConfig.SONGS}" +
                    "?order=created_at.desc&limit=$limit" +
                    "&select=id,title,artist,album,cover_url,animated_cover_url,audio_url,duration_seconds,lyrics_text,lyrics_url,genre,youtube_id"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", UkiyoConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${UkiyoConfig.SUPABASE_ANON_KEY}")
                setRequestProperty("Accept", "application/json")
            }
            val body = conn.inputStream.bufferedReader().readText()
            parseSongs(JSONArray(body))
        }
    }

    suspend fun getSongById(id: String): Result<Song?> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "${UkiyoConfig.SUPABASE_URL}${UkiyoConfig.SONGS}?id=eq.$id&select=*"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", UkiyoConfig.SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer ${UkiyoConfig.SUPABASE_ANON_KEY}")
            }
            val body = conn.inputStream.bufferedReader().readText()
            val arr = JSONArray(body)
            if (arr.length() == 0) null else parseSong(arr.getJSONObject(0))
        }
    }

    private fun parseSongs(arr: JSONArray): List<Song> {
        val list = mutableListOf<Song>()
        for (i in 0 until arr.length()) {
            list.add(parseSong(arr.getJSONObject(i)))
        }
        return list
    }

    private fun parseSong(o: JSONObject): Song = Song(
        id = o.optString("id"),
        title = o.optString("title"),
        artist = o.optString("artist"),
        album = o.optString("album").takeIf { it.isNotBlank() },
        coverUrl = o.optString("cover_url").takeIf { it.isNotBlank() },
        animatedCoverUrl = o.optString("animated_cover_url").takeIf { it.isNotBlank() },
        audioUrl = o.optString("audio_url").takeIf { it.isNotBlank() },
        durationSeconds = o.optInt("duration_seconds", 0),
        lyricsText = o.optString("lyrics_text").takeIf { it.isNotBlank() },
        lyricsUrl = o.optString("lyrics_url").takeIf { it.isNotBlank() },
        genre = o.optString("genre").takeIf { it.isNotBlank() },
        youtubeId = o.optString("youtube_id").takeIf { it.isNotBlank() }
    )
}
