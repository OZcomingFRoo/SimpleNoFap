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
    fun reminderNotificationBehaviorDefaultsToNormalAndCanSelectFullScreen() {
        var fullScreenEnabled: Boolean? = null
        setEnglishContent(
            fullScreenReminderNotificationsEnabled = false,
            onFullScreenReminderNotificationsChanged = { fullScreenEnabled = it }
        )

        composeRule.onNodeWithTag("reminder_notifications_normal")
            .performScrollTo()
            .assertIsSelected()
        composeRule.onNodeWithTag("reminder_notifications_full_screen").performClick()

        composeRule.runOnIdle { assertEquals(true, fullScreenEnabled) }
    }

    @Test
    fun fullScreenWarningAndSettingsActionAppearWhenAccessUnavailable() {
        var openedSettings = false
        setEnglishContent(
            fullScreenReminderNotificationsEnabled = true,
            fullScreenReminderNotificationsAllowed = false,
            onOpenFullScreenNotificationSettings = { openedSettings = true }
        )

        composeRule.onNodeWithTag("full_screen_notifications_warning")
            .performScrollTo()
            .assertExists()
        composeRule.onNodeWithText(
            "Android is blocking full-screen alerts for this app. Reminders will still appear as high-priority notifications."
        ).assertExists()
        composeRule.onNodeWithTag("full_screen_notifications_settings").performClick()

        composeRule.runOnIdle { assertTrue(openedSettings) }
    }

    @Test
    fun fullScreenSettingsActionAppearsWhenFullScreenModeIsSelected() {
        var openedSettings = false
        setEnglishContent(
            fullScreenReminderNotificationsEnabled = true,
            fullScreenReminderNotificationsAllowed = true,
            onOpenFullScreenNotificationSettings = { openedSettings = true }
        )

        composeRule.onNodeWithTag("full_screen_notifications_settings")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle { assertTrue(openedSettings) }
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
                        fullScreenReminderNotificationsEnabled = false,
                        fullScreenReminderNotificationsAllowed = true,
                        onUserNameSaved = {},
                        onLanguagePreferenceChanged = {},
                        onThemePreferenceChanged = {},
                        onFullScreenReminderNotificationsChanged = {},
                        onOpenFullScreenNotificationSettings = {}
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
        fullScreenReminderNotificationsEnabled: Boolean = false,
        fullScreenReminderNotificationsAllowed: Boolean = true,
        onUserNameSaved: (String) -> Unit = {},
        onLanguagePreferenceChanged: (LanguagePreference) -> Unit = {},
        onThemePreferenceChanged: (ThemePreference) -> Unit = {},
        onFullScreenReminderNotificationsChanged: (Boolean) -> Unit = {},
        onOpenFullScreenNotificationSettings: () -> Unit = {}
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
                        fullScreenReminderNotificationsEnabled = fullScreenReminderNotificationsEnabled,
                        fullScreenReminderNotificationsAllowed = fullScreenReminderNotificationsAllowed,
                        onUserNameSaved = onUserNameSaved,
                        onLanguagePreferenceChanged = onLanguagePreferenceChanged,
                        onThemePreferenceChanged = onThemePreferenceChanged,
                        onFullScreenReminderNotificationsChanged = onFullScreenReminderNotificationsChanged,
                        onOpenFullScreenNotificationSettings = onOpenFullScreenNotificationSettings
                    )
                }
            }
        }
    }
}
