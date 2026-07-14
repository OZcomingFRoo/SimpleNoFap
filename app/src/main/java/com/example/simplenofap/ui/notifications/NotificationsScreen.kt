package com.example.simplenofap.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.ui.components.PlaceholderContent

@Composable
internal fun NotificationsScreen(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current

    PlaceholderContent(
        title = strings.notifications,
        body = strings.notificationsPlaceholder,
        modifier = modifier
    )
}
