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
    val openMenu: String,
    val addNotification: String,
    val noNotifications: String,
    val noNotificationsBody: String,
    val editNotification: String,
    val newNotification: String,
    val cancel: String,
    val delete: String,
    val deleteNotificationTitle: String,
    val deleteNotificationBody: String,
    val discardChangesTitle: String,
    val discardChangesBody: String,
    val discard: String,
    val keepEditing: String,
    val message: String,
    val messageOptional: String,
    val sound: String,
    val defaultSound: String,
    val silent: String,
    val chooseSound: String,
    val weekdaysRequired: String,
    val notificationPermissionWarning: String,
    val grantPermission: String,
    val exactAlarmWarning: String,
    val allowExactAlarms: String,
    val notificationError: String,
    val notificationDefaultBody: String,
    val weekdayInitials: List<String>,
    val weekdayNames: List<String>
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
    openMenu = "Open menu",
    addNotification = "Add notification",
    noNotifications = "No reminders yet",
    noNotificationsBody = "Add a weekly reminder to keep your streak in focus.",
    editNotification = "Edit reminder",
    newNotification = "New reminder",
    cancel = "Cancel",
    delete = "Delete",
    deleteNotificationTitle = "Delete this reminder?",
    deleteNotificationBody = "This reminder and its scheduled alarm will be removed.",
    discardChangesTitle = "Discard changes?",
    discardChangesBody = "Your unsaved changes will be lost.",
    discard = "Discard",
    keepEditing = "Keep editing",
    message = "Message",
    messageOptional = "Optional encouragement",
    sound = "Sound",
    defaultSound = "System default",
    silent = "Silent",
    chooseSound = "Choose sound",
    weekdaysRequired = "Choose at least one day.",
    notificationPermissionWarning = "Notifications are blocked. Active reminders are saved but cannot be delivered.",
    grantPermission = "Allow notifications",
    exactAlarmWarning = "Exact alarm access is off. Reminders may arrive a little late.",
    allowExactAlarms = "Alarms & reminders",
    notificationError = "Something went wrong. Please try again.",
    notificationDefaultBody = "Keep going!",
    weekdayInitials = listOf("S", "M", "T", "W", "T", "F", "S"),
    weekdayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
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
    openMenu = "פתח תפריט",
    addNotification = "הוספת התראה",
    noNotifications = "עדיין אין תזכורות",
    noNotificationsBody = "הוסיפו תזכורת שבועית כדי לשמור על המיקוד ברצף.",
    editNotification = "עריכת תזכורת",
    newNotification = "תזכורת חדשה",
    cancel = "ביטול",
    delete = "מחיקה",
    deleteNotificationTitle = "למחוק את התזכורת?",
    deleteNotificationBody = "התזכורת והשעון המתוזמן שלה יימחקו.",
    discardChangesTitle = "לבטל את השינויים?",
    discardChangesBody = "השינויים שלא נשמרו יאבדו.",
    discard = "ביטול שינויים",
    keepEditing = "המשך עריכה",
    message = "הודעה",
    messageOptional = "עידוד אופציונלי",
    sound = "צליל",
    defaultSound = "ברירת המחדל של המערכת",
    silent = "שקט",
    chooseSound = "בחירת צליל",
    weekdaysRequired = "יש לבחור לפחות יום אחד.",
    notificationPermissionWarning = "ההתראות חסומות. תזכורות פעילות נשמרות אך לא ניתן להציג אותן.",
    grantPermission = "אישור התראות",
    exactAlarmWarning = "הגישה להתראות מדויקות כבויה. ייתכן עיכוב קל בתזכורות.",
    allowExactAlarms = "התראות ותזכורות",
    notificationError = "משהו השתבש. נסו שוב.",
    notificationDefaultBody = "ממשיכים קדימה!",
    weekdayInitials = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש"),
    weekdayNames = listOf("יום ראשון", "יום שני", "יום שלישי", "יום רביעי", "יום חמישי", "יום שישי", "שבת")
)
