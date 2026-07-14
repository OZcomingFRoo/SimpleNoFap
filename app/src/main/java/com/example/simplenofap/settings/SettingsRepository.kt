package com.example.simplenofap.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
    val themePreference: ThemePreference = ThemePreference.System
)

class SettingsRepository(
    private val context: Context
) {
    private object Keys {
        val UserName = stringPreferencesKey("user_name")
        val Language = stringPreferencesKey("language")
        val Theme = stringPreferencesKey("theme")
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
                themePreference = theme
            )
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
