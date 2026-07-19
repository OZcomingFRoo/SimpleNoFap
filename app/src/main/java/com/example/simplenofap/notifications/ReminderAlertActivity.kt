package com.example.simplenofap.notifications

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simplenofap.LocalizedApp
import com.example.simplenofap.MainActivity
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.resolveLanguagePreference
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.shouldUseDarkTheme
import com.example.simplenofap.ui.theme.SimpleNoFapTheme

class ReminderAlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val title = intent.getStringExtra(ExtraTitle).orEmpty()
        val body = intent.getStringExtra(ExtraBody).orEmpty()

        setContent {
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val settings by settingsRepository.settings.collectAsState(initial = AppSettings())
            val resolvedLanguage = resolveLanguagePreference(settings.languagePreference)
            val strings = stringsFor(resolvedLanguage)
            val darkTheme = shouldUseDarkTheme(
                themePreference = settings.themePreference,
                systemDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            )

            SimpleNoFapTheme(darkTheme = darkTheme) {
                LocalizedApp(
                    languagePreference = settings.languagePreference,
                    resolvedLanguage = resolvedLanguage
                ) {
                    CompositionLocalProvider(LocalAppStrings provides strings) {
                        ReminderAlertContent(
                            title = title.ifBlank { strings.appName },
                            body = body.ifBlank { strings.notificationDefaultBody },
                            onDismiss = ::finish,
                            onOpenApp = {
                                startActivity(
                                    Intent(this, MainActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                )
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val ExtraTitle = "reminder_alert_title"
        const val ExtraBody = "reminder_alert_body"
    }
}

@Composable
private fun ReminderAlertContent(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.fullScreenAlertDismiss)
                }
                Button(
                    onClick = onOpenApp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.fullScreenAlertOpenApp)
                }
            }
        }
    }
}
