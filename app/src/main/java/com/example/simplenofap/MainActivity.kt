package com.example.simplenofap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.settings.ThemePreference
import com.example.simplenofap.ui.SimpleNoFapApp
import com.example.simplenofap.ui.theme.SimpleNoFapTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                CompositionLocalProvider(
                    LocalAppStrings provides strings,
                    LocalLayoutDirection provides resolvedLanguage.layoutDirection
                ) {
                    SimpleNoFapApp(
                        languagePreference = settings.languagePreference,
                        themePreference = settings.themePreference,
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
