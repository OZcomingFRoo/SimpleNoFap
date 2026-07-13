package com.example.simplenofap.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.simplenofap.localization.AppStrings
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.settings.LanguagePreference
import com.example.simplenofap.settings.ThemePreference
import kotlinx.coroutines.launch

private enum class DrawerDestination {
    Main,
    Notifications,
    Settings
}

private enum class MainTab {
    Counter,
    DayStreaks
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleNoFapApp(
    languagePreference: LanguagePreference,
    themePreference: ThemePreference,
    onLanguagePreferenceChanged: (LanguagePreference) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(DrawerDestination.Main) }
    var currentMainTab by remember { mutableStateOf(MainTab.Counter) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    scope.launch { drawerState.close() }
                }
            )
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.title(strings)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.semantics {
                                contentDescription = strings.openMenu
                            }
                        ) {
                            MenuIcon()
                        }
                    }
                )
            },
            bottomBar = {
                if (currentDestination == DrawerDestination.Main) {
                    MainBottomBar(
                        currentTab = currentMainTab,
                        onTabSelected = { currentMainTab = it }
                    )
                }
            }
        ) { innerPadding ->
            when (currentDestination) {
                DrawerDestination.Main -> MainScreen(
                    currentTab = currentMainTab,
                    modifier = Modifier.padding(innerPadding)
                )

                DrawerDestination.Notifications -> PlaceholderScreen(
                    title = strings.notifications,
                    body = strings.notificationsPlaceholder,
                    modifier = Modifier.padding(innerPadding)
                )

                DrawerDestination.Settings -> SettingsScreen(
                    languagePreference = languagePreference,
                    themePreference = themePreference,
                    onLanguagePreferenceChanged = onLanguagePreferenceChanged,
                    onThemePreferenceChanged = onThemePreferenceChanged,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun MenuIcon() {
    val color = MaterialTheme.colorScheme.onPrimaryContainer

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        val strokeWidth = 2.dp.toPx()
        val startX = 1.dp.toPx()
        val endX = size.width - 1.dp.toPx()
        listOf(
            size.height * 0.3f,
            size.height * 0.5f,
            size.height * 0.7f
        ).forEach { y ->
            drawLine(
                color = color,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun AppDrawer(
    currentDestination: DrawerDestination,
    onDestinationSelected: (DrawerDestination) -> Unit
) {
    val strings = LocalAppStrings.current

    ModalDrawerSheet {
        Text(
            text = strings.appName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(24.dp)
        )
        HorizontalDivider()
        DrawerDestination.entries.forEach { destination ->
            NavigationDrawerItem(
                label = { Text(destination.title(strings)) },
                selected = destination == currentDestination,
                onClick = { onDestinationSelected(destination) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MainBottomBar(
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
private fun MainScreen(
    currentTab: MainTab,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    when (currentTab) {
        MainTab.Counter -> PlaceholderScreen(
            title = strings.counter,
            body = strings.counterPlaceholder,
            modifier = modifier
        )

        MainTab.DayStreaks -> PlaceholderScreen(
            title = strings.dayStreaks,
            body = strings.dayStreaksPlaceholder,
            modifier = modifier
        )
    }
}

@Composable
private fun SettingsScreen(
    languagePreference: LanguagePreference,
    themePreference: ThemePreference,
    onLanguagePreferenceChanged: (LanguagePreference) -> Unit,
    onThemePreferenceChanged: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = strings.settings,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = strings.settingsPlaceholder,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = strings.theme,
            style = MaterialTheme.typography.titleMedium
        )
        ThemePreference.entries.forEach { option ->
            OptionRow(
                label = option.label(strings),
                selected = option == themePreference,
                onClick = { onThemePreferenceChanged(option) }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = strings.language,
            style = MaterialTheme.typography.titleMedium
        )
        LanguagePreference.entries.forEach { option ->
            OptionRow(
                label = option.label(strings),
                selected = option == languagePreference,
                onClick = { onLanguagePreferenceChanged(option) }
            )
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun DrawerDestination.title(strings: AppStrings): String {
    return when (this) {
        DrawerDestination.Main -> strings.main
        DrawerDestination.Notifications -> strings.notifications
        DrawerDestination.Settings -> strings.settings
    }
}

private fun MainTab.title(strings: AppStrings): String {
    return when (this) {
        MainTab.Counter -> strings.counter
        MainTab.DayStreaks -> strings.dayStreaks
    }
}

private fun LanguagePreference.label(strings: AppStrings): String {
    return when (this) {
        LanguagePreference.English -> strings.languageEnglish
        LanguagePreference.Hebrew -> strings.languageHebrew
        LanguagePreference.System -> strings.languageSystem
    }
}

private fun ThemePreference.label(strings: AppStrings): String {
    return when (this) {
        ThemePreference.Light -> strings.themeLight
        ThemePreference.Dark -> strings.themeDark
        ThemePreference.System -> strings.themeSystem
    }
}
