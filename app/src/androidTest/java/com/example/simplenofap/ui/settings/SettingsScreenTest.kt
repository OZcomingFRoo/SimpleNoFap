package com.example.simplenofap.ui.settings

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.ResolvedLanguage
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.LanguagePreference
import com.example.simplenofap.settings.ThemePreference
import com.example.simplenofap.ui.theme.SimpleNoFapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun name_requiresAChangedNonBlankValueAndSavesTrimmedText() {
        var savedName: String? = null
        setEnglishContent(onUserNameSaved = { savedName = it })

        composeRule.onNodeWithTag("settings_name_save").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings_name_input").performTextClearance()
        composeRule.onNodeWithTag("settings_name_input").performTextInput("   ")
        composeRule.onNodeWithTag("settings_name_save").assertIsNotEnabled()
        composeRule.onNodeWithText("Name is required.").assertExists()

        composeRule.onNodeWithTag("settings_name_input").performTextClearance()
        composeRule.onNodeWithTag("settings_name_input").performTextInput("  Omri  ")
        composeRule.onNodeWithTag("settings_name_save")
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle { assertEquals("Omri", savedName) }
        composeRule.onNodeWithTag("settings_name_input").assertTextContains("Omri")
        composeRule.onNodeWithText("Name saved.").assertExists()
    }

    @Test
    fun selectorsShowSelectionAndInvokeCallbacks() {
        var selectedTheme: ThemePreference? = null
        var selectedLanguage: LanguagePreference? = null
        setEnglishContent(
            themePreference = ThemePreference.Light,
            languagePreference = LanguagePreference.English,
            onThemePreferenceChanged = { selectedTheme = it },
            onLanguagePreferenceChanged = { selectedLanguage = it }
        )

        composeRule.onNodeWithTag("theme_light")
            .performScrollTo()
            .assertIsSelected()
        composeRule.onNodeWithTag("theme_dark").performClick()
        composeRule.runOnIdle { assertEquals(ThemePreference.Dark, selectedTheme) }
        composeRule.onNodeWithTag("theme_system").performClick()
        composeRule.runOnIdle { assertEquals(ThemePreference.System, selectedTheme) }

        composeRule.onNodeWithTag("language_english")
            .performScrollTo()
            .assertIsSelected()
        composeRule.onNodeWithTag("language_hebrew").performClick()
        composeRule.runOnIdle { assertEquals(LanguagePreference.Hebrew, selectedLanguage) }
        composeRule.onNodeWithTag("language_system").performClick()
        composeRule.runOnIdle { assertEquals(LanguagePreference.System, selectedLanguage) }
    }

    @Test
    fun aboutUsesLocalizedVersionFallback() {
        setEnglishContent(appVersionName = null)

        composeRule.onNodeWithText("Version: Unavailable")
            .performScrollTo()
            .assertExists()
        composeRule.onNodeWithText("SimpleNoFap").assertExists()
    }

    @Test
    fun hebrewContentUsesRtlTileOrdering() {
        val language = ResolvedLanguage.Hebrew
        composeRule.setContent {
            SimpleNoFapTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppStrings provides stringsFor(language),
                    LocalLayoutDirection provides language.layoutDirection
                ) {
                    SettingsContent(
                        userName = "עומרי",
                        appVersionName = "1.0",
                        languagePreference = LanguagePreference.Hebrew,
                        themePreference = ThemePreference.System,
                        onUserNameSaved = {},
                        onLanguagePreferenceChanged = {},
                        onThemePreferenceChanged = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("language_english").performScrollTo()
        val englishLeft = composeRule.onNodeWithTag("language_english")
            .fetchSemanticsNode().boundsInRoot.left
        val hebrewLeft = composeRule.onNodeWithTag("language_hebrew")
            .fetchSemanticsNode().boundsInRoot.left

        assertTrue(englishLeft > hebrewLeft)
        composeRule.onNodeWithText("עברית").assertExists()
    }

    private fun setEnglishContent(
        appVersionName: String? = "1.0",
        themePreference: ThemePreference = ThemePreference.System,
        languagePreference: LanguagePreference = LanguagePreference.System,
        onUserNameSaved: (String) -> Unit = {},
        onLanguagePreferenceChanged: (LanguagePreference) -> Unit = {},
        onThemePreferenceChanged: (ThemePreference) -> Unit = {}
    ) {
        val language = ResolvedLanguage.English
        composeRule.setContent {
            SimpleNoFapTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppStrings provides stringsFor(language),
                    LocalLayoutDirection provides language.layoutDirection
                ) {
                    SettingsContent(
                        userName = "Omri",
                        appVersionName = appVersionName,
                        languagePreference = languagePreference,
                        themePreference = themePreference,
                        onUserNameSaved = onUserNameSaved,
                        onLanguagePreferenceChanged = onLanguagePreferenceChanged,
                        onThemePreferenceChanged = onThemePreferenceChanged
                    )
                }
            }
        }
    }
}
