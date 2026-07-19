package com.example.simplenofap.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.simplenofap.daystreaks.DayStreakMilestones
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SettingsDataStoreName = "settings"

private val Context.settingsDataStore by preferencesDataStore(name = SettingsDataStoreName)

data class AppSettings(
    val userName: String = "",
    val languagePreference: LanguagePreference = LanguagePreference.System,
    val themePreference: ThemePreference = ThemePreference.System,
    val startNoFapAtEpochMillis: Long? = null,
    val dayStreakAttemptId: Long? = null,
    val processedDayStreakMilestonesMask: Int = 0,
    val lastCelebratedDayStreakRewardId: Long? = null,
    val fullScreenReminderNotificationsEnabled: Boolean = false
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
        val DayStreakAttemptId = longPreferencesKey("day_streak_attempt_id")
        val ProcessedDayStreakMilestonesMask = intPreferencesKey("processed_day_streak_milestones_mask")
        val LastCelebratedDayStreakRewardId = longPreferencesKey("last_celebrated_day_streak_reward_id")
        val FullScreenReminderNotificationsEnabled =
            booleanPreferencesKey("full_screen_reminder_notifications_enabled")
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
                startNoFapAtEpochMillis = preferences[Keys.StartNoFapAtEpochMillis],
                dayStreakAttemptId = preferences[Keys.DayStreakAttemptId],
                processedDayStreakMilestonesMask =
                    preferences[Keys.ProcessedDayStreakMilestonesMask] ?: 0,
                lastCelebratedDayStreakRewardId =
                    preferences[Keys.LastCelebratedDayStreakRewardId],
                fullScreenReminderNotificationsEnabled =
                    preferences[Keys.FullScreenReminderNotificationsEnabled] ?: false
            )
        }

    suspend fun initializeStartTimeIfAbsent(nowEpochMillis: Long = currentTimeMillis()): Long {
        var storedValue = nowEpochMillis
        context.settingsDataStore.edit { preferences ->
            storedValue = preferences[Keys.StartNoFapAtEpochMillis] ?: nowEpochMillis.also {
                preferences[Keys.StartNoFapAtEpochMillis] = it
            }
            if (preferences[Keys.DayStreakAttemptId] == null) {
                preferences[Keys.DayStreakAttemptId] = storedValue
            }
        }
        return storedValue
    }

    suspend fun currentSettings(): AppSettings = settings.first()

    suspend fun setStartNoFapAtEpochMillis(
        startedAtEpochMillis: Long,
        nowEpochMillis: Long = currentTimeMillis()
    ) {
        require(startedAtEpochMillis <= nowEpochMillis) {
            "The streak start time cannot be in the future."
        }
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.StartNoFapAtEpochMillis] = startedAtEpochMillis
            preferences[Keys.ProcessedDayStreakMilestonesMask] =
                DayStreakMilestones.crossedMask(startedAtEpochMillis, nowEpochMillis)
        }
    }

    suspend fun resetStartNoFapToNow(nowEpochMillis: Long = currentTimeMillis()) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.StartNoFapAtEpochMillis] = nowEpochMillis
            preferences[Keys.DayStreakAttemptId] = nowEpochMillis
            preferences[Keys.ProcessedDayStreakMilestonesMask] = 0
            preferences.remove(Keys.LastCelebratedDayStreakRewardId)
        }
    }

    suspend fun setProcessedDayStreakMilestonesMask(mask: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ProcessedDayStreakMilestonesMask] = mask
        }
    }

    suspend fun setLastCelebratedDayStreakRewardId(rewardId: Long?) {
        context.settingsDataStore.edit { preferences ->
            if (rewardId == null) {
                preferences.remove(Keys.LastCelebratedDayStreakRewardId)
            } else {
                preferences[Keys.LastCelebratedDayStreakRewardId] = rewardId
            }
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

    suspend fun setFullScreenReminderNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.FullScreenReminderNotificationsEnabled] = enabled
        }
    }
}
