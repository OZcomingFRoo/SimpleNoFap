package com.example.simplenofap.localization

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import com.example.simplenofap.settings.LanguagePreference
import java.util.Locale

enum class ResolvedLanguage(
    val layoutDirection: LayoutDirection
) {
    English(LayoutDirection.Ltr),
    Hebrew(LayoutDirection.Rtl)
}

data class AppStrings(
    val appName: String,
    val main: String,
    val notifications: String,
    val settings: String,
    val counter: String,
    val dayStreaks: String,
    val language: String,
    val languageEnglish: String,
    val languageHebrew: String,
    val languageSystem: String,
    val theme: String,
    val themeLight: String,
    val themeDark: String,
    val themeSystem: String,
    val profile: String,
    val name: String,
    val namePlaceholder: String,
    val nameSupportingText: String,
    val nameRequired: String,
    val nameSaved: String,
    val save: String,
    val appearance: String,
    val appearanceDescription: String,
    val languageDescription: String,
    val about: String,
    val version: String,
    val versionUnavailable: String,
    val aboutPurpose: String,
    val unitedStatesFlag: String,
    val israelFlag: String,
    val mainPlaceholder: String,
    val counterPlaceholder: String,
    val dayStreaksPlaceholder: String,
    val notificationsPlaceholder: String,
    val settingsPlaceholder: String,
    val openMenu: String
)

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }

fun resolveLanguagePreference(languagePreference: LanguagePreference): ResolvedLanguage {
    return when (languagePreference) {
        LanguagePreference.English -> ResolvedLanguage.English
        LanguagePreference.Hebrew -> ResolvedLanguage.Hebrew
        LanguagePreference.System -> {
            if (Locale.getDefault().language.equals("he", ignoreCase = true)) {
                ResolvedLanguage.Hebrew
            } else {
                ResolvedLanguage.English
            }
        }
    }
}

fun stringsFor(language: ResolvedLanguage): AppStrings {
    return when (language) {
        ResolvedLanguage.English -> EnglishStrings
        ResolvedLanguage.Hebrew -> HebrewStrings
    }
}

private val EnglishStrings = AppStrings(
    appName = "SimpleNoFap",
    main = "Main",
    notifications = "Notification Manager",
    settings = "Settings",
    counter = "Counter",
    dayStreaks = "Day-Streaks",
    language = "Language",
    languageEnglish = "English",
    languageHebrew = "Hebrew",
    languageSystem = "System",
    theme = "Theme",
    themeLight = "Light",
    themeDark = "Dark",
    themeSystem = "System",
    profile = "Profile",
    name = "Name",
    namePlaceholder = "How should we call you?",
    nameSupportingText = "Used to make encouragement feel personal.",
    nameRequired = "Name is required.",
    nameSaved = "Name saved.",
    save = "Save",
    appearance = "Appearance",
    appearanceDescription = "Choose how SimpleNoFap looks on this device.",
    languageDescription = "Choose the language and reading direction.",
    about = "About",
    version = "Version",
    versionUnavailable = "Unavailable",
    aboutPurpose = "A simple, private companion for staying focused on your streak.",
    unitedStatesFlag = "United States flag",
    israelFlag = "Israel flag",
    mainPlaceholder = "Main screen placeholder",
    counterPlaceholder = "The streak counter will live here.",
    dayStreaksPlaceholder = "Recent and available Day-Streak badges will live here.",
    notificationsPlaceholder = "Scheduled notification management will live here.",
    settingsPlaceholder = "App preferences for the skeleton.",
    openMenu = "Open menu"
)

private val HebrewStrings = AppStrings(
    appName = "SimpleNoFap",
    main = "ראשי",
    notifications = "ניהול התראות",
    settings = "הגדרות",
    counter = "מונה",
    dayStreaks = "רצפי ימים",
    language = "שפה",
    languageEnglish = "אנגלית",
    languageHebrew = "עברית",
    languageSystem = "לפי המערכת",
    theme = "ערכת נושא",
    themeLight = "בהיר",
    themeDark = "כהה",
    themeSystem = "לפי המערכת",
    profile = "פרופיל",
    name = "שם",
    namePlaceholder = "איך לפנות אליך?",
    nameSupportingText = "השם משמש לעידוד אישי יותר.",
    nameRequired = "יש להזין שם.",
    nameSaved = "השם נשמר.",
    save = "שמירה",
    appearance = "מראה",
    appearanceDescription = "בחרו כיצד SimpleNoFap תיראה במכשיר הזה.",
    languageDescription = "בחרו את שפת האפליקציה ואת כיוון הקריאה.",
    about = "אודות",
    version = "גרסה",
    versionUnavailable = "לא זמינה",
    aboutPurpose = "שותף פשוט ופרטי שעוזר לשמור על המיקוד ברצף שלך.",
    unitedStatesFlag = "דגל ארצות הברית",
    israelFlag = "דגל ישראל",
    mainPlaceholder = "מסך ראשי זמני",
    counterPlaceholder = "כאן יופיע מונה הרצף.",
    dayStreaksPlaceholder = "כאן יופיעו תגי רצפי הימים האחרונים והזמינים.",
    notificationsPlaceholder = "כאן יופיע ניהול ההתראות המתוזמנות.",
    settingsPlaceholder = "העדפות האפליקציה עבור שלד הממשק.",
    openMenu = "פתח תפריט"
)
