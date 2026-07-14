package com.example.simplenofap.ui.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.simplenofap.localization.AppStrings
import com.example.simplenofap.localization.LocalAppStrings

internal enum class MainTab {
    Counter,
    DayStreaks
}

@Composable
internal fun MainBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    val strings = LocalAppStrings.current

    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.title(strings)) },
                icon = {}
            )
        }
    }
}

@Composable
internal fun MainScreen(
    currentTab: MainTab,
    modifier: Modifier = Modifier
) {
    when (currentTab) {
        MainTab.Counter -> CounterScreen(modifier = modifier)
        MainTab.DayStreaks -> DayStreaksScreen(modifier = modifier)
    }
}

private fun MainTab.title(strings: AppStrings): String {
    return when (this) {
        MainTab.Counter -> strings.counter
        MainTab.DayStreaks -> strings.dayStreaks
    }
}
