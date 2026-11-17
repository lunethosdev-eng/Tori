package com.pokke.music.data.remote

/**
 * Normaliza covers de Ukiyo / Server 2 / paths relativos de Supabase Storage
 * a URL absolutas usables por Coil y BitChord.
 * También ofrece fallback a YouTube y a búsqueda externa (Deezer / iTunes).
 */
object CoverUrls {
    fun absolute(raw: String?, sourceBaseUrl: String? = null): String? {
        val v = raw?.trim()?.takeIf { it.isNotBlank() && it != "null" } ?: return null
        if (v.startsWith("http://", true) || v.startsWith("https://", true) ||
            v.startsWith("content://") || v.startsWith("file://")) {
            return v
        }
        val base = (sourceBaseUrl ?: UkiyoConfig.SUPABASE_URL).trimEnd('/')
        return when {
            v.startsWith("/storage/v1/") -> "$base$v"
            v.startsWith("storage/v1/") -> "$base/$v"
            v.startsWith("/object/") -> "$base/storage/v1$v"
            v.startsWith("object/") -> "$base/storage/v1/$v"
            // path tipo "covers/xyz.jpg" en bucket público
            '/' in v || v.contains('.') -> "$base/storage/v1/object/public/$v"
            else -> v
        }
    }

    fun fromYoutubeId(youtubeId: String?): String? =
        youtubeId?.takeIf { it.isNotBlank() }?.let { "https://i.ytimg.com/vi/$it/hqdefault.jpg" }

    fun best(coverUrl: String?, youtubeId: String?, sourceBaseUrl: String? = null): String? =
        absolute(coverUrl, sourceBaseUrl) ?: fromYoutubeId(youtubeId)

    /**
     * Fallback síncrono ligero usando la API pública de Deezer.
     * Se llama solo cuando el cover local y el de YouTube fallan.
     * Timeout corto para no bloquear la UI.
     */
    fun fromDeezer(title: String?, artist: String?): String? {
        if (title.isNullOrBlank() || artist.isNullOrBlank()) return null
        return runCatching {
            val q = java.net.URLEncoder.encode("$artist $title", "UTF-8")
            val url = "https://api.deezer.com/search/track?q=$q&limit=1"
            val connection = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Pokke/1.0")
            }
            try {
                if (connection.responseCode !in 200..299) return null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val data = org.json.JSONObject(body).optJSONArray("data") ?: return null
                if (data.length() == 0) return null
                val album = data.getJSONObject(0).optJSONObject("album") ?: return null
                album.optString("cover_xl")
                    .takeIf { it.isNotBlank() && it != "null" }
                    ?: album.optString("cover_big").takeIf { it.isNotBlank() && it != "null" }
                    ?: album.optString("cover_medium").takeIf { it.isNotBlank() && it != "null" }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    /**
     * Fallback iTunes Search API (público, sin key).
     */
    fun fromItunes(title: String?, artist: String?): String? {
        if (title.isNullOrBlank() || artist.isNullOrBlank()) return null
        return runCatching {
            val term = java.net.URLEncoder.encode("$artist $title", "UTF-8")
            val url = "https://itunes.apple.com/search?term=$term&entity=song&limit=1"
            val connection = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Pokke/1.0")
            }
            try {
                if (connection.responseCode !in 200..299) return null
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val results = org.json.JSONObject(body).optJSONArray("results") ?: return null
                if (results.length() == 0) return null
                val artwork = results.getJSONObject(0).optString("artworkUrl100")
                    .takeIf { it.isNotBlank() && it != "null" } ?: return null
                // Subir resolución
                artwork.replace("100x100bb", "600x600bb")
                    .replace("100x100", "600x600")
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    /**
     * Mejor cover posible: local → YT → Deezer → iTunes.
     * Usar en background cuando displayCoverUrl sea null.
     */
    fun resolveBest(coverUrl: String?, youtubeId: String?, title: String?, artist: String?, sourceBaseUrl: String? = null): String? {
        best(coverUrl, youtubeId, sourceBaseUrl)?.let { return it }
        fromDeezer(title, artist)?.let { return it }
        return fromItunes(title, artist)
    }
}
