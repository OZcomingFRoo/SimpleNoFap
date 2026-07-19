package com.example.simplenofap.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.simplenofap.R
import com.example.simplenofap.localization.AppStrings
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.settings.LanguagePreference
import com.example.simplenofap.settings.ThemePreference
import com.example.simplenofap.ui.theme.CalmBlue
import com.example.simplenofap.ui.theme.CalmBlueDark
import com.example.simplenofap.ui.theme.MorningSurface
import com.example.simplenofap.ui.theme.NightPrimary
import com.example.simplenofap.ui.theme.NightSurface

@Composable
fun SettingsScreen(
    userName: String,
    languagePreference: LanguagePreference,
    themePreference: ThemePreference,
    fullScreenReminderNotificationsEnabled: Boolean,
    fullScreenReminderNotificationsAllowed: Boolean,
    onUserNameSaved: (String) -> Unit,
    onLanguagePreferenceChanged: (LanguagePreference) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    onFullScreenReminderNotificationsChanged: (Boolean) -> Unit,
    onOpenFullScreenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsContent(
        userName = userName,
        appVersionName = rememberAppVersionName(),
        languagePreference = languagePreference,
        themePreference = themePreference,
        fullScreenReminderNotificationsEnabled = fullScreenReminderNotificationsEnabled,
        fullScreenReminderNotificationsAllowed = fullScreenReminderNotificationsAllowed,
        onUserNameSaved = onUserNameSaved,
        onLanguagePreferenceChanged = onLanguagePreferenceChanged,
        onThemePreferenceChanged = onThemePreferenceChanged,
        onFullScreenReminderNotificationsChanged = onFullScreenReminderNotificationsChanged,
        onOpenFullScreenNotificationSettings = onOpenFullScreenNotificationSettings,
        modifier = modifier
    )
}

@Composable
internal fun SettingsContent(
    userName: String,
    appVersionName: String?,
    languagePreference: LanguagePreference,
    themePreference: ThemePreference,
    fullScreenReminderNotificationsEnabled: Boolean,
    fullScreenReminderNotificationsAllowed: Boolean,
    onUserNameSaved: (String) -> Unit,
    onLanguagePreferenceChanged: (LanguagePreference) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    onFullScreenReminderNotificationsChanged: (Boolean) -> Unit,
    onOpenFullScreenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var nameDraft by rememberSaveable(userName) { mutableStateOf(userName) }
    var nameEdited by rememberSaveable(userName) { mutableStateOf(false) }
    var lastSavedName by rememberSaveable { mutableStateOf<String?>(null) }
    val trimmedName = nameDraft.trim()
    val nameIsBlank = trimmedName.isBlank()
    val canSaveName = !nameIsBlank && trimmedName != userName

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(
                title = strings.profile,
                supportingText = strings.nameSupportingText
            ) {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = {
                        nameDraft = it
                        nameEdited = true
                        lastSavedName = null
                    },
                    label = { Text(strings.name) },
                    placeholder = { Text(strings.namePlaceholder) },
                    supportingText = {
                        when {
                            nameEdited && nameIsBlank -> Text(strings.nameRequired)
                            lastSavedName == trimmedName -> Text(strings.nameSaved)
                        }
                    },
                    isError = nameEdited && nameIsBlank,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("settings_name_input")
                )
                Button(
                    onClick = {
                        nameDraft = trimmedName
                        nameEdited = false
                        lastSavedName = trimmedName
                        onUserNameSaved(trimmedName)
                    },
                    enabled = canSaveName,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag("settings_name_save")
                ) {
                    Text(strings.save)
                }
            }
        }

        item {
            SettingsSection(
                title = strings.appearance,
                supportingText = strings.appearanceDescription
            ) {
                ThemeSelector(
                    selected = themePreference,
                    onSelected = onThemePreferenceChanged
                )
            }
        }

        item {
            SettingsSection(
                title = strings.language,
                supportingText = strings.languageDescription
            ) {
                LanguageSelector(
                    selected = languagePreference,
                    onSelected = onLanguagePreferenceChanged
                )
            }
        }

        item {
            SettingsSection(
                title = strings.notificationBehavior,
                supportingText = strings.notificationBehaviorDescription
            ) {
                ReminderNotificationBehaviorSelector(
                    fullScreenEnabled = fullScreenReminderNotificationsEnabled,
                    fullScreenAllowed = fullScreenReminderNotificationsAllowed,
                    onFullScreenChanged = onFullScreenReminderNotificationsChanged,
                    onOpenFullScreenNotificationSettings = onOpenFullScreenNotificationSettings
                )
            }
        }

        item {
            SettingsSection(title = strings.about) {
                SettingsInfoText(
                    text = strings.appName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                SettingsInfoText(
                    text = "${strings.version}: ${appVersionName ?: strings.versionUnavailable}"
                )
                SettingsInfoText(text = strings.aboutPurpose)
            }
        }
    }
}

@Composable
private fun ReminderNotificationBehaviorSelector(
    fullScreenEnabled: Boolean,
    fullScreenAllowed: Boolean,
    onFullScreenChanged: (Boolean) -> Unit,
    onOpenFullScreenNotificationSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    RadioSettingRow(
        label = strings.normalReminderNotifications,
        selected = !fullScreenEnabled,
        testTag = "reminder_notifications_normal",
        onClick = { onFullScreenChanged(false) }
    )
    RadioSettingRow(
        label = strings.fullScreenReminderAlerts,
        selected = fullScreenEnabled,
        testTag = "reminder_notifications_full_screen",
        onClick = { onFullScreenChanged(true) }
    )
    if (fullScreenEnabled && !fullScreenAllowed) {
        Text(
            text = strings.fullScreenPermissionUnavailableWarning,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("full_screen_notifications_warning")
        )
    }
    if (fullScreenEnabled) {
        TextButton(
            onClick = onOpenFullScreenNotificationSettings,
            modifier = Modifier
                .padding(top = 4.dp)
                .testTag("full_screen_notifications_settings")
        ) {
            Text(strings.openAndroidSettings)
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (supportingText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun ThemeSelector(
    selected: ThemePreference,
    onSelected: (ThemePreference) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeTile(
            preference = ThemePreference.Light,
            label = ThemePreference.Light.label(LocalAppStrings.current),
            previewBackground = MorningSurface,
            previewAccent = CalmBlue,
            previewLabel = CalmBlueDark,
            selected = selected == ThemePreference.Light,
            onClick = { onSelected(ThemePreference.Light) },
            modifier = Modifier.weight(1f)
        )
        ThemeTile(
            preference = ThemePreference.Dark,
            label = ThemePreference.Dark.label(LocalAppStrings.current),
            previewBackground = NightSurface,
            previewAccent = NightPrimary,
            previewLabel = NightPrimary,
            selected = selected == ThemePreference.Dark,
            onClick = { onSelected(ThemePreference.Dark) },
            modifier = Modifier.weight(1f)
        )
    }
    RadioSettingRow(
        label = ThemePreference.System.label(LocalAppStrings.current),
        selected = selected == ThemePreference.System,
        testTag = "theme_system",
        onClick = { onSelected(ThemePreference.System) }
    )
}

@Composable
private fun ThemeTile(
    preference: ThemePreference,
    label: String,
    previewBackground: Color,
    previewAccent: Color,
    previewLabel: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        modifier = modifier
            .heightIn(min = 120.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .testTag("theme_${preference.name.lowercase()}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = previewBackground),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(previewAccent)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = previewLabel,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selected: LanguagePreference,
    onSelected: (LanguagePreference) -> Unit
) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LanguageTile(
            preference = LanguagePreference.English,
            label = LanguagePreference.English.label(strings),
            flagResId = R.drawable.flag_us,
            flagDescription = strings.unitedStatesFlag,
            selected = selected == LanguagePreference.English,
            onClick = { onSelected(LanguagePreference.English) },
            modifier = Modifier.weight(1f)
        )
        LanguageTile(
            preference = LanguagePreference.Hebrew,
            label = LanguagePreference.Hebrew.label(strings),
            flagResId = R.drawable.flag_israel,
            flagDescription = strings.israelFlag,
            selected = selected == LanguagePreference.Hebrew,
            onClick = { onSelected(LanguagePreference.Hebrew) },
            modifier = Modifier.weight(1f)
        )
    }
    RadioSettingRow(
        label = LanguagePreference.System.label(strings),
        selected = selected == LanguagePreference.System,
        testTag = "language_system",
        onClick = { onSelected(LanguagePreference.System) }
    )
}

@Composable
private fun LanguageTile(
    preference: LanguagePreference,
    label: String,
    flagResId: Int,
    flagDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileShape = RoundedCornerShape(12.dp)
    val flagShape = RoundedCornerShape(8.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .heightIn(min = 120.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .testTag("language_${preference.name.lowercase()}"),
        shape = tileShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(flagResId),
                    contentDescription = flagDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(64.dp)
                        .height(48.dp)
                        .clip(flagShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, flagShape)
                )
                RadioButton(
                    selected = selected,
                    onClick = null,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RadioSettingRow(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .testTag(testTag)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsInfoText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        fontWeight = fontWeight,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
private fun rememberAppVersionName(): String? {
    val context = LocalContext.current
    return remember(context) { context.packageVersionName() }
}

private fun Context.packageVersionName(): String? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        ).versionName
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0).versionName
    }
}.getOrNull()?.takeIf { it.isNotBlank() }

private fun LanguagePreference.label(strings: AppStrings): String = when (this) {
    LanguagePreference.English -> strings.languageEnglish
    LanguagePreference.Hebrew -> strings.languageHebrew
    LanguagePreference.System -> strings.languageSystem
}

private fun ThemePreference.label(strings: AppStrings): String = when (this) {
    ThemePreference.Light -> strings.themeLight
    ThemePreference.Dark -> strings.themeDark
    ThemePreference.System -> strings.themeSystem
}
