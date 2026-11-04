package com.pokke.music.data.remote

/**
 * Kokoro Supabase – accounts / profiles / playlists
 * Keys pulled from connected project.
 */
object KokoroConfig {
    const val SUPABASE_URL = "https://ajbmpgnzkgtcmulocftd.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_idn1WATcO2bx-YAEc2kWVA_YFF-HBcF"

    const val PROFILES = "/rest/v1/profiles"
    const val PLAYLISTS = "/rest/v1/playlists"
    const val FAVORITES = "/rest/v1/favorites"
    const val USER_SETTINGS = "/rest/v1/user_settings"
    const val AUTH_SIGNUP = "/auth/v1/signup"
    const val AUTH_TOKEN = "/auth/v1/token?grant_type=password"
    const val AUTH_REFRESH = "/auth/v1/token?grant_type=refresh_token"
    const val AUTH_USER = "/auth/v1/user"
    const val AUTH_LOGOUT = "/auth/v1/logout"
    const val PLAYLIST_SONGS = "/rest/v1/playlist_songs"
}
