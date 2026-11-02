package com.pokke.music.data.remote

/**
 * Kokoro Supabase – accounts / profiles / playlists
 * Keys pulled from connected project.
 */
object KokoroConfig {
    const val SUPABASE_URL = "https://ajbmpgnzkgtcmulocftd.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFqYm1wZ256a2d0Y211bG9jZnRkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzNDU2MjksImV4cCI6MjEwNTkyMTYyOX0.twsBnT9WBBILyXFuoQh7viI578yoNbeQztKHsr8DB8k"

    const val PROFILES = "/rest/v1/profiles"
    const val PLAYLISTS = "/rest/v1/playlists"
    const val FAVORITES = "/rest/v1/favorites"
    const val USER_SETTINGS = "/rest/v1/user_settings"
    const val AUTH_SIGNUP = "/auth/v1/signup"
    const val AUTH_TOKEN = "/auth/v1/token?grant_type=password"
    const val AUTH_USER = "/auth/v1/user"
    const val AUTH_LOGOUT = "/auth/v1/logout"
}
