package com.fitnex.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fitnex.app.domain.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.playerDataStore: DataStore<Preferences> by preferencesDataStore(name = "fitnex_player")

/**
 * Local, offline player progress.
 *
 * DataStore performs each write to a temporary file and atomically renames it,
 * so a crash or force-close either keeps the previous save or the new one, never
 * a half-written file. Fields are additive with defaults, so app updates do not
 * wipe progress.
 */
class GameRepository(private val context: Context) {

    val playerState: Flow<PlayerState> = context.playerDataStore.data.map { prefs ->
        decode(prefs[STATE_KEY])
    }

    /** Applies [transform] atomically and returns the persisted result. */
    suspend fun update(transform: (PlayerState) -> PlayerState): PlayerState {
        var updated = PlayerState()
        context.playerDataStore.edit { prefs ->
            updated = transform(decode(prefs[STATE_KEY]))
            prefs[STATE_KEY] = json.encodeToString(PlayerState.serializer(), updated)
        }
        return updated
    }

    suspend fun addPushUp(): PlayerState =
        update { it.copy(lifetimePushUps = it.lifetimePushUps + 1) }

    /** Wipes all progress. Settings live in their own store and are untouched. */
    suspend fun reset(): PlayerState = update { PlayerState() }

    private fun decode(raw: String?): PlayerState {
        if (raw == null) return PlayerState()
        return runCatching { json.decodeFromString(PlayerState.serializer(), raw) }
            .getOrElse { PlayerState() }
    }

    private companion object {
        val STATE_KEY = stringPreferencesKey("player_state_v1")
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
