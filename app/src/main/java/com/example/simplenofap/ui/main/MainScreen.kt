package com.example.simplenofap.ui.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenofap.daystreaks.DayStreakType
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
    startedAtEpochMillis: Long?,
    onStartTimeChanged: (Long) -> Unit,
    highlightedDayStreakType: DayStreakType?,
    dayStreakHighlightRequest: Int,
    modifier: Modifier = Modifier
) {
    val dayStreaksViewModel: DayStreaksViewModel = viewModel()
    val dayStreaksUiState by dayStreaksViewModel.uiState.collectAsState()

    LaunchedEffect(dayStreakHighlightRequest, highlightedDayStreakType) {
        if (dayStreakHighlightRequest > 0) {
            dayStreaksViewModel.highlight(highlightedDayStreakType)
        }
    }

    when (currentTab) {
        MainTab.Counter -> CounterScreen(
            startedAtEpochMillis = startedAtEpochMillis,
            onStartTimeChanged = onStartTimeChanged,
            onResetToNow = dayStreaksViewModel::resetToNow,
            dayStreaksUiState = dayStreaksUiState,
            onUseDayStreakReward = dayStreaksViewModel::consumeReward,
            modifier = modifier
        )
        MainTab.DayStreaks -> DayStreaksScreen(
            uiState = dayStreaksUiState,
            onDismissCelebration = dayStreaksViewModel::dismissCelebration,
            modifier = modifier
        )
    }
}

private fun MainTab.title(strings: AppStrings): String {
    return when (this) {
        MainTab.Counter -> strings.counter
        MainTab.DayStreaks -> strings.dayStreaks
    }
}
