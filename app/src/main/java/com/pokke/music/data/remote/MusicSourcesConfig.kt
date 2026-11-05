package com.pokke.music.data.remote

/**
 * Multiple music catalog backends. App merges search/results from all enabled sources.
 * Owner can add more via Admin panel (persisted in pokke users.music_sources).
 */
data class MusicSource(
    val id: String,
    val name: String,
    val label: String,
    val supabaseUrl: String,
    val publishableKey: String,
    val isEnabled: Boolean = true,
    val isPrimary: Boolean = false
)

object MusicSourcesConfig {
    /** Built-in defaults; runtime list can be loaded from pokke users.music_sources */
    val defaults: List<MusicSource> = listOf(
        MusicSource(
            id = "ukiyo",
            name = "ukiyo",
            label = "Ukiyo (principal)",
            supabaseUrl = UkiyoConfig.SUPABASE_URL,
            publishableKey = UkiyoConfig.SUPABASE_PUBLISHABLE_KEY,
            isPrimary = true
        ),
        MusicSource(
            id = "pokke_server_2",
            name = "pokke_server_2",
            label = "Pokke Server 2",
            supabaseUrl = "https://zcsbmighlvwefvafpecm.supabase.co",
            publishableKey = UkiyoConfig.SUPABASE_PUBLISHABLE_KEY, // replace with real key in Admin
            isPrimary = false
        )
    )

    @Volatile var runtime: List<MusicSource> = defaults

    fun enabled(): List<MusicSource> = runtime.filter { it.isEnabled && it.supabaseUrl.isNotBlank() && !it.supabaseUrl.contains("placeholder") }
}
