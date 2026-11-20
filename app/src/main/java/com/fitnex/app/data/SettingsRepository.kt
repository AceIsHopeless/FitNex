package com.fitnex.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class CameraFacing { FRONT, BACK }

@Serializable
data class GameSettings(
    val cameraFacing: CameraFacing = CameraFacing.FRONT,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
)

private val Context.settingsDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "fitnex_settings")

/**
 * Player preferences. Kept in their own DataStore so resetting progress does
 * not silently change the camera or feedback behaviour the player chose.
 */
class SettingsRepository(private val context: Context) {

    val settings: Flow<GameSettings> = context.settingsDataStore.data.map { prefs ->
        decode(prefs[SETTINGS_KEY])
    }

    suspend fun update(transform: (GameSettings) -> GameSettings): GameSettings {
        var updated = GameSettings()
        context.settingsDataStore.edit { prefs ->
            updated = transform(decode(prefs[SETTINGS_KEY]))
            prefs[SETTINGS_KEY] = json.encodeToString(GameSettings.serializer(), updated)
        }
        return updated
    }

    private fun decode(raw: String?): GameSettings {
        if (raw == null) return GameSettings()
        return runCatching { json.decodeFromString(GameSettings.serializer(), raw) }
            .getOrElse { GameSettings() }
    }

    private companion object {
        val SETTINGS_KEY = stringPreferencesKey("settings_v1")
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
