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
    }
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
}
