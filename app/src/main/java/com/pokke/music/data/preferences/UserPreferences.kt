package com.pokke.music.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pokke_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        private val EXTERNAL_ACCOUNTS = booleanPreferencesKey("external_accounts_enabled")
        private val SPOTIFY_LOGIN = booleanPreferencesKey("spotify_login_enabled")
        private val DISCORD_LOGIN = booleanPreferencesKey("discord_login_enabled")
        private val YTMUSIC_LOGIN = booleanPreferencesKey("ytmusic_login_enabled")
        private val VENDORED_GLASS = booleanPreferencesKey("vendored_backdrop_glass")

        private val LIQUID_GLASS = booleanPreferencesKey("liquid_glass_enabled")
        private val LIQUID_SLIDER = booleanPreferencesKey("liquid_slider_enabled")
        private val LIQUID_PERFORMANCE = booleanPreferencesKey("liquid_glass_performance_mode")
        private val EXPERIMENTAL_CHATS = booleanPreferencesKey("experimental_chats_alpha_1")
        private val EXPERIMENTAL_MAPS = booleanPreferencesKey("experimental_friend_maps_alpha_1")
        private val AUTO_ANIMATED_COVERS = booleanPreferencesKey("auto_animated_covers")
        private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        private val BLUR_PLAYER = booleanPreferencesKey("blur_player_background")
        private val GAPLESS = booleanPreferencesKey("gapless_playback")
        private val CROSSFADE = floatPreferencesKey("crossfade_seconds")
        private val ACCENT = stringPreferencesKey("accent_color")
        private val THEME = stringPreferencesKey("app_theme")
        private val SHOW_LISTENING = booleanPreferencesKey("show_listening_on_profile")
        private val SHOW_LINK_EMBEDS = booleanPreferencesKey("show_link_embeds")
        private val PLAYER_THEME = stringPreferencesKey("player_theme") // minimal | sakura | midnight | aurora

        // YouTube / external catalog (separate from login)
        private val YOUTUBE_CATALOG = booleanPreferencesKey("youtube_catalog_enabled")
        private val LYRICS_TARGET_LANG = stringPreferencesKey("lyrics_target_lang")
        private val LYRICS_COLOR_MODE = stringPreferencesKey("lyrics_color_mode")
        private val LYRICS_ANIM_INTENSITY = floatPreferencesKey("lyrics_anim_intensity")
        private val HOME_CUSTOM_BG = stringPreferencesKey("home_custom_bg_url")
        private val APP_ACCENT = stringPreferencesKey("app_accent_theme")
        private val SPOTIFY_CANVAS = booleanPreferencesKey("spotify_canvas_enabled")
        private val APPLE_CANVAS = booleanPreferencesKey("apple_canvas_enabled")
        private val POKKE_ANIMATED_COVERS = booleanPreferencesKey("pokke_animated_covers")
        private val EXTERNAL_CANVAS = booleanPreferencesKey("external_canvas_enabled")

        // Home personalization
        private val HOME_SHOW_RECENT = booleanPreferencesKey("home_show_recent")
        private val HOME_SHOW_NOW_PLAYING = booleanPreferencesKey("home_show_now_playing")
        private val HOME_SHOW_QUICK_ACCESS = booleanPreferencesKey("home_show_quick_access")
        private val HOME_SHOW_RECOMMENDATIONS = booleanPreferencesKey("home_show_recommendations")
        private val HOME_SHOW_LIBRARY_SHORTCUTS = booleanPreferencesKey("home_show_library_shortcuts")
        private val HOME_SECTION_ORDER = stringPreferencesKey("home_section_order")
        private val HOME_LAYOUT = stringPreferencesKey("home_layout") // compact | comfortable | dense
        private val HOME_DENSITY = stringPreferencesKey("home_density") // small | medium | large
    }

    val externalAccountsEnabled: Flow<Boolean> = context.dataStore.data.map { it[EXTERNAL_ACCOUNTS] ?: false }
    val spotifyLoginEnabled: Flow<Boolean> = context.dataStore.data.map { it[SPOTIFY_LOGIN] ?: false }
    val discordLoginEnabled: Flow<Boolean> = context.dataStore.data.map { it[DISCORD_LOGIN] ?: false }
    val ytMusicLoginEnabled: Flow<Boolean> = context.dataStore.data.map { it[YTMUSIC_LOGIN] ?: false }
    val vendoredBackdropGlass: Flow<Boolean> = context.dataStore.data.map { it[VENDORED_GLASS] ?: true }

    suspend fun setExternalAccountsEnabled(v: Boolean) { context.dataStore.edit { it[EXTERNAL_ACCOUNTS] = v } }
    suspend fun setSpotifyLoginEnabled(v: Boolean) { context.dataStore.edit { it[SPOTIFY_LOGIN] = v } }
    suspend fun setDiscordLoginEnabled(v: Boolean) { context.dataStore.edit { it[DISCORD_LOGIN] = v } }
    suspend fun setYtMusicLoginEnabled(v: Boolean) { context.dataStore.edit { it[YTMUSIC_LOGIN] = v } }
    suspend fun setVendoredBackdropGlass(v: Boolean) { context.dataStore.edit { it[VENDORED_GLASS] = v } }

    val youtubeCatalogEnabled: Flow<Boolean> = context.dataStore.data.map { it[YOUTUBE_CATALOG] ?: false }
    val lyricsTargetLang: Flow<String> = context.dataStore.data.map { it[LYRICS_TARGET_LANG] ?: "es" }
    val lyricsColorMode: Flow<String> = context.dataStore.data.map { it[LYRICS_COLOR_MODE] ?: "classic" }
    val lyricsAnimIntensity: Flow<Float> = context.dataStore.data.map { it[LYRICS_ANIM_INTENSITY] ?: 1f }
    val homeCustomBgUrl: Flow<String> = context.dataStore.data.map { it[HOME_CUSTOM_BG] ?: "" }
    val appAccentTheme: Flow<String> = context.dataStore.data.map { it[APP_ACCENT] ?: "violet" }
    val spotifyCanvasEnabled: Flow<Boolean> = context.dataStore.data.map { it[SPOTIFY_CANVAS] ?: false }
    val appleCanvasEnabled: Flow<Boolean> = context.dataStore.data.map { it[APPLE_CANVAS] ?: false }
    val pokkeAnimatedCoversEnabled: Flow<Boolean> = context.dataStore.data.map { it[POKKE_ANIMATED_COVERS] ?: true }
    val externalCanvasEnabled: Flow<Boolean> = context.dataStore.data.map { it[EXTERNAL_CANVAS] ?: true }

    suspend fun setYoutubeCatalogEnabled(v: Boolean) { context.dataStore.edit { it[YOUTUBE_CATALOG] = v } }
    suspend fun setLyricsTargetLang(v: String) { context.dataStore.edit { it[LYRICS_TARGET_LANG] = v } }
    suspend fun setLyricsColorMode(v: String) { context.dataStore.edit { it[LYRICS_COLOR_MODE] = v } }
    suspend fun setLyricsAnimIntensity(v: Float) { context.dataStore.edit { it[LYRICS_ANIM_INTENSITY] = v.coerceIn(0.3f, 1.8f) } }
    suspend fun setHomeCustomBgUrl(v: String) { context.dataStore.edit { it[HOME_CUSTOM_BG] = v } }
    suspend fun setAppAccentTheme(v: String) { context.dataStore.edit { it[APP_ACCENT] = v } }
    suspend fun setSpotifyCanvasEnabled(v: Boolean) { context.dataStore.edit { it[SPOTIFY_CANVAS] = v } }
    suspend fun setAppleCanvasEnabled(v: Boolean) { context.dataStore.edit { it[APPLE_CANVAS] = v } }
    suspend fun setPokkeAnimatedCoversEnabled(v: Boolean) { context.dataStore.edit { it[POKKE_ANIMATED_COVERS] = v } }
    suspend fun setExternalCanvasEnabled(v: Boolean) { context.dataStore.edit { it[EXTERNAL_CANVAS] = v } }

    val liquidGlassEnabled: Flow<Boolean> = context.dataStore.data.map { it[LIQUID_GLASS] ?: false }
    val liquidSliderEnabled: Flow<Boolean> = context.dataStore.data.map { it[LIQUID_SLIDER] ?: true }
    val liquidPerformanceMode: Flow<Boolean> = context.dataStore.data.map { it[LIQUID_PERFORMANCE] ?: true }
    val experimentalChatsEnabled: Flow<Boolean> = context.dataStore.data.map { it[EXPERIMENTAL_CHATS] ?: false }
    val experimentalMapsEnabled: Flow<Boolean> = context.dataStore.data.map { it[EXPERIMENTAL_MAPS] ?: false }
    val autoAnimatedCovers: Flow<Boolean> = context.dataStore.data.map { it[AUTO_ANIMATED_COVERS] ?: true }
    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLORS] ?: true }
    val blurPlayerBackground: Flow<Boolean> = context.dataStore.data.map { it[BLUR_PLAYER] ?: false }
    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { it[GAPLESS] ?: true }
    val crossfadeSeconds: Flow<Float> = context.dataStore.data.map { it[CROSSFADE] ?: 0f }
    val accentColor: Flow<String> = context.dataStore.data.map { it[ACCENT] ?: "white" }
    val appTheme: Flow<String> = context.dataStore.data.map { it[THEME] ?: "dark" }
    /** Default ON: others can see what you are listening to on your public profile. */
    val showListeningOnProfile: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LISTENING] ?: true }
    /** Default ON: link cards expand / show rich preview when possible. */
    val showLinkEmbeds: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LINK_EMBEDS] ?: true }
    val playerTheme: Flow<String> = context.dataStore.data.map { it[PLAYER_THEME] ?: "minimal" }

    // Home personalization flows
    val homeShowRecent: Flow<Boolean> = context.dataStore.data.map { it[HOME_SHOW_RECENT] ?: true }
    val homeShowNowPlaying: Flow<Boolean> = context.dataStore.data.map { it[HOME_SHOW_NOW_PLAYING] ?: true }
    val homeShowQuickAccess: Flow<Boolean> = context.dataStore.data.map { it[HOME_SHOW_QUICK_ACCESS] ?: true }
    val homeShowRecommendations: Flow<Boolean> = context.dataStore.data.map { it[HOME_SHOW_RECOMMENDATIONS] ?: true }
    val homeShowLibraryShortcuts: Flow<Boolean> = context.dataStore.data.map { it[HOME_SHOW_LIBRARY_SHORTCUTS] ?: true }
    val homeSectionOrder: Flow<String> = context.dataStore.data.map {
        it[HOME_SECTION_ORDER] ?: "now_playing,quick_access,recent,recommendations,library"
    }
    val homeLayout: Flow<String> = context.dataStore.data.map { it[HOME_LAYOUT] ?: "comfortable" }
    val homeDensity: Flow<String> = context.dataStore.data.map { it[HOME_DENSITY] ?: "medium" }

    suspend fun setLiquidGlass(v: Boolean) = context.dataStore.edit { it[LIQUID_GLASS] = v }
    suspend fun setLiquidSlider(v: Boolean) = context.dataStore.edit { it[LIQUID_SLIDER] = v }
    suspend fun setLiquidPerformanceMode(v: Boolean) = context.dataStore.edit { it[LIQUID_PERFORMANCE] = v }
    suspend fun setExperimentalChats(v: Boolean) = context.dataStore.edit { it[EXPERIMENTAL_CHATS] = v; if (!v) it[EXPERIMENTAL_MAPS] = false }
    suspend fun setExperimentalMaps(v: Boolean) = context.dataStore.edit { it[EXPERIMENTAL_MAPS] = v }
    suspend fun setAutoAnimatedCovers(v: Boolean) = context.dataStore.edit { it[AUTO_ANIMATED_COVERS] = v }
    suspend fun setDynamicColors(v: Boolean) = context.dataStore.edit { it[DYNAMIC_COLORS] = v }
    suspend fun setBlurPlayerBackground(v: Boolean) = context.dataStore.edit { it[BLUR_PLAYER] = v }
    suspend fun setGaplessPlayback(v: Boolean) = context.dataStore.edit { it[GAPLESS] = v }
    suspend fun setCrossfadeSeconds(v: Float) = context.dataStore.edit { it[CROSSFADE] = v.coerceIn(0f, 12f) }
    suspend fun setAccentColor(v: String) = context.dataStore.edit { it[ACCENT] = v }
    suspend fun setAppTheme(v: String) = context.dataStore.edit { it[THEME] = v }
    suspend fun setShowListeningOnProfile(v: Boolean) = context.dataStore.edit { it[SHOW_LISTENING] = v }
    suspend fun setShowLinkEmbeds(v: Boolean) = context.dataStore.edit { it[SHOW_LINK_EMBEDS] = v }
    suspend fun setPlayerTheme(v: String) = context.dataStore.edit { it[PLAYER_THEME] = v }

    suspend fun setHomeShowRecent(v: Boolean) = context.dataStore.edit { it[HOME_SHOW_RECENT] = v }
    suspend fun setHomeShowNowPlaying(v: Boolean) = context.dataStore.edit { it[HOME_SHOW_NOW_PLAYING] = v }
    suspend fun setHomeShowQuickAccess(v: Boolean) = context.dataStore.edit { it[HOME_SHOW_QUICK_ACCESS] = v }
    suspend fun setHomeShowRecommendations(v: Boolean) = context.dataStore.edit { it[HOME_SHOW_RECOMMENDATIONS] = v }
    suspend fun setHomeShowLibraryShortcuts(v: Boolean) = context.dataStore.edit { it[HOME_SHOW_LIBRARY_SHORTCUTS] = v }
    suspend fun setHomeSectionOrder(v: String) = context.dataStore.edit { it[HOME_SECTION_ORDER] = v }
    suspend fun setHomeLayout(v: String) = context.dataStore.edit { it[HOME_LAYOUT] = v }
    suspend fun setHomeDensity(v: String) = context.dataStore.edit { it[HOME_DENSITY] = v }

    /** Restore default Home layout and visibility. */
    suspend fun resetHomeCustomization() = context.dataStore.edit {
        it[HOME_SHOW_RECENT] = true
        it[HOME_SHOW_NOW_PLAYING] = true
        it[HOME_SHOW_QUICK_ACCESS] = true
        it[HOME_SHOW_RECOMMENDATIONS] = true
        it[HOME_SHOW_LIBRARY_SHORTCUTS] = true
        it[HOME_SECTION_ORDER] = "now_playing,quick_access,recent,recommendations,library"
        it[HOME_LAYOUT] = "comfortable"
        it[HOME_DENSITY] = "medium"
    }
}
