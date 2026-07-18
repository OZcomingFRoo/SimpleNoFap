package com.example.simplenofap

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.simplenofap.daystreaks.AndroidDayStreakAlarmScheduler
import com.example.simplenofap.daystreaks.DayStreakReconciler
import com.example.simplenofap.daystreaks.DayStreakType
import com.example.simplenofap.daystreaks.dayStreakRepository
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.ResolvedLanguage
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.LanguagePreference
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.settings.ThemePreference
import com.example.simplenofap.ui.SimpleNoFapApp
import com.example.simplenofap.ui.theme.SimpleNoFapTheme
import com.example.simplenofap.widget.CounterWidgetUpdater
import com.example.simplenofap.widget.EXTRA_OPEN_COUNTER
import kotlinx.coroutines.launch
import com.example.simplenofap.notifications.AndroidNotificationScheduler
import com.example.simplenofap.notifications.notificationRepository
import java.util.Locale

@Suppress("DEPRECATION")
private val HebrewLocale = Locale("iw")

class MainActivity : ComponentActivity() {
    private var openCounterRequest by mutableIntStateOf(0)
    private var openDayStreaksRequest by mutableIntStateOf(0)
    private var highlightedDayStreakType: DayStreakType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val settingsRepository = SettingsRepository(applicationContext)
            settingsRepository.initializeStartTimeIfAbsent()
            DayStreakReconciler(
                settingsRepository,
                applicationContext.dayStreakRepository()
            ).reconcile()
            AndroidDayStreakAlarmScheduler(applicationContext).reconcile(settingsRepository)
            AndroidNotificationScheduler(applicationContext)
                .reconcile(applicationContext.notificationRepository())
            CounterWidgetUpdater.refreshAll(applicationContext)
        }
        handleIntent(intent)

        setContent {
            val settingsRepository = remember {
                SettingsRepository(applicationContext)
            }
            val scope = rememberCoroutineScope()
            val settings by settingsRepository.settings.collectAsState(
                initial = AppSettings()
            )
            val resolvedLanguage = resolveLanguagePreference(settings.languagePreference)
            val strings = stringsFor(resolvedLanguage)
            val darkTheme = shouldUseDarkTheme(
                themePreference = settings.themePreference,
                systemDarkTheme = isSystemInDarkTheme()
            )

            SimpleNoFapTheme(darkTheme = darkTheme) {
                LocalizedApp(
                    languagePreference = settings.languagePreference,
                    resolvedLanguage = resolvedLanguage
                ) {
                    CompositionLocalProvider(LocalAppStrings provides strings) {
                        SimpleNoFapApp(
                            userName = settings.userName,
                            startedAtEpochMillis = settings.startNoFapAtEpochMillis,
                            languagePreference = settings.languagePreference,
                            themePreference = settings.themePreference,
                            openCounterRequest = openCounterRequest,
                            openDayStreaksRequest = openDayStreaksRequest,
                            highlightedDayStreakType = highlightedDayStreakType,
                            onUserNameSaved = { userName ->
                                scope.launch {
                                    settingsRepository.setUserName(userName)
                                }
                            },
                            onStartTimeChanged = { startedAtEpochMillis ->
                                scope.launch {
                                    settingsRepository.setStartNoFapAtEpochMillis(startedAtEpochMillis)
                                    DayStreakReconciler(
                                        settingsRepository,
                                        applicationContext.dayStreakRepository()
                                    ).reconcile()
                                    AndroidDayStreakAlarmScheduler(applicationContext).reconcile(settingsRepository)
                                    CounterWidgetUpdater.refreshAll(applicationContext)
                                }
                            },
                            onLanguagePreferenceChanged = { languagePreference ->
                                scope.launch {
                                    settingsRepository.setLanguagePreference(languagePreference)
                                }
                            },
                            onThemePreferenceChanged = { themePreference ->
                                scope.launch {
                                    settingsRepository.setThemePreference(themePreference)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        CounterWidgetUpdater.refreshAll(applicationContext)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_COUNTER, false) == true) {
            openCounterRequest++
            intent.removeExtra(EXTRA_OPEN_COUNTER)
        }
        if (intent?.getBooleanExtra(EXTRA_OPEN_DAY_STREAKS, false) == true) {
            highlightedDayStreakType = intent.getStringExtra(EXTRA_DAY_STREAK_HIGHLIGHT_TYPE)
                ?.let { runCatching { DayStreakType.valueOf(it) }.getOrNull() }
            openDayStreaksRequest++
            intent.removeExtra(EXTRA_OPEN_DAY_STREAKS)
            intent.removeExtra(EXTRA_DAY_STREAK_HIGHLIGHT_TYPE)
        }
    }

    companion object {
        const val EXTRA_OPEN_DAY_STREAKS = "open_day_streaks"
        const val EXTRA_DAY_STREAK_HIGHLIGHT_TYPE = "day_streak_highlight_type"
    }
}

@Composable
private fun LocalizedApp(
    languagePreference: LanguagePreference,
    resolvedLanguage: ResolvedLanguage,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val activityResultRegistryOwner = checkNotNull(LocalActivityResultRegistryOwner.current) {
        "ActivityResultRegistryOwner must be available before localizing the app context."
    }
    val onBackPressedDispatcherOwner = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "OnBackPressedDispatcherOwner must be available before localizing the app context."
    }
    val systemConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(systemConfiguration, languagePreference) {
        when (languagePreference) {
            LanguagePreference.System -> systemConfiguration
            LanguagePreference.English -> systemConfiguration.withLocale(Locale.US)
            LanguagePreference.Hebrew -> systemConfiguration.withLocale(HebrewLocale)
        }
    }
    val localizedContext = remember(baseContext, localizedConfiguration, languagePreference) {
        if (languagePreference == LanguagePreference.System) {
            baseContext
        } else {
            baseContext.createConfigurationContext(localizedConfiguration)
        }
    }

    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
        LocalOnBackPressedDispatcherOwner provides onBackPressedDispatcherOwner,
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        LocalLayoutDirection provides resolvedLanguage.layoutDirection,
        content = content
    )
}

private fun Configuration.withLocale(locale: Locale): Configuration {
    return Configuration(this).apply { setLocale(locale) }
}

private fun shouldUseDarkTheme(
    themePreference: ThemePreference,
    systemDarkTheme: Boolean
): Boolean {
    return when (themePreference) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> systemDarkTheme
    }
}
