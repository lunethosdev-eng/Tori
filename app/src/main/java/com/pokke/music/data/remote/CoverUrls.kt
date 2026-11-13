package com.pokke.music.data.remote

/**
 * Normaliza covers de Ukiyo / Server 2 / paths relativos de Supabase Storage
 * a URL absolutas usables por Coil y BitChord.
 */
object CoverUrls {
    fun absolute(raw: String?, sourceBaseUrl: String? = null): String? {
        val v = raw?.trim()?.takeIf { it.isNotBlank() && it != "null" } ?: return null
        if (v.startsWith("http://", true) || v.startsWith("https://", true) || v.startsWith("content://") || v.startsWith("file://")) {
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
}
