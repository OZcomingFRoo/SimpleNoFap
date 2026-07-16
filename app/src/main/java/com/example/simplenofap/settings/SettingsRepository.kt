package com.example.simplenofap.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SettingsDataStoreName = "settings"

private val Context.settingsDataStore by preferencesDataStore(name = SettingsDataStoreName)

data class AppSettings(
    val userName: String = "",
    val languagePreference: LanguagePreference = LanguagePreference.System,
    val themePreference: ThemePreference = ThemePreference.System,
    val startNoFapAtEpochMillis: Long? = null
)

class SettingsRepository(
    private val context: Context,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    private object Keys {
        val UserName = stringPreferencesKey("user_name")
        val Language = stringPreferencesKey("language")
        val Theme = stringPreferencesKey("theme")
        val StartNoFapAtEpochMillis = longPreferencesKey("start_no_fap_at_epoch_millis")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val language = preferences[Keys.Language]
                ?.let { stored -> LanguagePreference.entries.find { it.name == stored } }
                ?: LanguagePreference.System
            val theme = preferences[Keys.Theme]
                ?.let { stored -> ThemePreference.entries.find { it.name == stored } }
                ?: ThemePreference.System

            AppSettings(
                userName = preferences[Keys.UserName].orEmpty(),
                languagePreference = language,
                themePreference = theme,
                startNoFapAtEpochMillis = preferences[Keys.StartNoFapAtEpochMillis]
            )
        }

    suspend fun initializeStartTimeIfAbsent(nowEpochMillis: Long = currentTimeMillis()): Long {
        var storedValue = nowEpochMillis
        context.settingsDataStore.edit { preferences ->
            storedValue = preferences[Keys.StartNoFapAtEpochMillis] ?: nowEpochMillis.also {
                preferences[Keys.StartNoFapAtEpochMillis] = it
            }
        }
        return storedValue
    }

    suspend fun setStartNoFapAtEpochMillis(
        startedAtEpochMillis: Long,
        nowEpochMillis: Long = currentTimeMillis()
    ) {
        require(startedAtEpochMillis <= nowEpochMillis) {
            "The streak start time cannot be in the future."
        }
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.StartNoFapAtEpochMillis] = startedAtEpochMillis
        }
    }

    suspend fun resetStartNoFapToNow(nowEpochMillis: Long = currentTimeMillis()) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.StartNoFapAtEpochMillis] = nowEpochMillis
        }
    }

    suspend fun setUserName(userName: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.UserName] = userName
        }
    }

    suspend fun setLanguagePreference(languagePreference: LanguagePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.Language] = languagePreference.name
        }
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.Theme] = themePreference.name
        }
    }
}
