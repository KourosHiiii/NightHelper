package com.nighthelper.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "night_helper")

class NightRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private companion object {
        val KEY_ITEMS = stringPreferencesKey("items_json")
        val KEY_STATES = stringPreferencesKey("states_json")
        val KEY_HISTORY = stringPreferencesKey("history_json")
        val KEY_SETTINGS = stringPreferencesKey("settings_json")
        val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
    }

    val items: Flow<List<ChecklistItem>> = context.dataStore.data.map { prefs ->
        decodeItems(prefs[KEY_ITEMS])
    }

    val states: Flow<Map<String, ItemState>> = context.dataStore.data.map { prefs ->
        decodeStates(prefs[KEY_STATES])
    }

    val history: Flow<List<DayRecord>> = context.dataStore.data.map { prefs ->
        prefs[KEY_HISTORY]?.let { raw ->
            runCatching { json.decodeFromString<List<DayRecord>>(raw) }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    val settings: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        prefs[KEY_SETTINGS]?.let { raw ->
            runCatching { json.decodeFromString<SettingsState>(raw) }.getOrDefault(SettingsState())
        } ?: SettingsState()
    }

    fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun decodeItems(raw: String?): List<ChecklistItem> {
        if (raw.isNullOrBlank()) return DefaultItems.create()
        return runCatching { json.decodeFromString<List<ChecklistItem>>(raw) }
            .getOrDefault(DefaultItems.create())
            .ifEmpty { DefaultItems.create() }
    }

    private fun decodeStates(raw: String?): Map<String, ItemState> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { json.decodeFromString<Map<String, ItemState>>(raw) }
            .getOrDefault(emptyMap())
    }

    suspend fun saveItems(items: List<ChecklistItem>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ITEMS] = json.encodeToString(items)
        }
    }

    suspend fun setItemState(id: String, state: ItemState) {
        context.dataStore.edit { prefs ->
            val current = decodeStates(prefs[KEY_STATES]).toMutableMap()
            current[id] = state
            prefs[KEY_STATES] = json.encodeToString(current)
        }
    }

    suspend fun resetStates() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_STATES)
        }
    }

    suspend fun saveSettings(settings: SettingsState) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SETTINGS] = json.encodeToString(settings)
        }
    }

    suspend fun addHistory(record: DayRecord) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.let { raw ->
                runCatching { json.decodeFromString<List<DayRecord>>(raw) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = (listOf(record) + current.filter { it.date != record.date })
                .sortedByDescending { it.date }
                .take(90)
            prefs[KEY_HISTORY] = json.encodeToString(updated)
        }
    }

    /**
     * Daily reset: called on app open / widget update.
     * Archives the previous day's leftover state as a DayRecord, then resets item states.
     * Returns true when a rollover happened.
     */
    suspend fun checkDailyReset(): Boolean {
        val today = today()
        val prefs = context.dataStore.data.first()
        val last = prefs[KEY_LAST_ACTIVE_DATE]
        if (last == today) return false

        val items = decodeItems(prefs[KEY_ITEMS])
        val states = decodeStates(prefs[KEY_STATES])
        if (states.isNotEmpty() && items.isNotEmpty()) {
            val completed = items.filter { states[it.id]?.done == true }
            val missed = items.filter { states[it.id]?.done != true }
            val gratitude = items
                .firstOrNull { it.type == ItemType.TEXT }
                ?.let { states[it.id]?.textValue?.takeIf { text -> text.isNotBlank() } }
            val record = DayRecord(
                date = last ?: today,
                totalItems = items.size,
                completedItems = completed.size,
                completedTitles = completed.map { it.title },
                missedTitles = missed.map { it.title },
                gratitude = gratitude,
                closedAt = null
            )
            context.dataStore.edit { p ->
                val current = p[KEY_HISTORY]?.let { raw ->
                    runCatching { json.decodeFromString<List<DayRecord>>(raw) }
                        .getOrDefault(emptyList())
                } ?: emptyList()
                val updated = (listOf(record) + current.filter { it.date != record.date })
                    .sortedByDescending { it.date }
                    .take(90)
                p[KEY_HISTORY] = json.encodeToString(updated)
            }
        }
        context.dataStore.edit { p ->
            p.remove(KEY_STATES)
            p[KEY_LAST_ACTIVE_DATE] = today
        }
        return true
    }
}
