package com.pokke.music.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pokke_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val LIQUID_GLASS = booleanPreferencesKey("liquid_glass_enabled")
        private val AUTO_ANIMATED_COVERS = booleanPreferencesKey("auto_animated_covers")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val liquidGlassEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LIQUID_GLASS] ?: false          // OFF by default (experimental)
    }

    val autoAnimatedCovers: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_ANIMATED_COVERS] ?: true   // ON by default
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    suspend fun setLiquidGlass(enabled: Boolean) {
        context.dataStore.edit { it[LIQUID_GLASS] = enabled }
    }

    suspend fun setAutoAnimatedCovers(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_ANIMATED_COVERS] = enabled }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN] = loggedIn }
    }
}
