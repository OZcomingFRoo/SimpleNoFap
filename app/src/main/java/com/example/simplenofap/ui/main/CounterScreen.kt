package com.example.simplenofap.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.ui.components.PlaceholderContent

@Composable
internal fun CounterScreen(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current

    PlaceholderContent(
        title = strings.counter,
        body = strings.counterPlaceholder,
        modifier = modifier
    )
}
