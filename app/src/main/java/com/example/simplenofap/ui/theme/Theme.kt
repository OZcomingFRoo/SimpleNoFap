package com.example.simplenofap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CalmLightColorScheme = lightColorScheme(
    primary = CalmBlue,
    onPrimary = Color.White,
    primaryContainer = CalmBlueContainer,
    onPrimaryContainer = CalmOnBlueContainer,
    secondary = SoftTeal,
    onSecondary = Color.White,
    secondaryContainer = SoftTealContainer,
    onSecondaryContainer = CalmOnTealContainer,
    tertiary = CalmBlueDark,
    onTertiary = Color.White,
    background = MorningBackground,
    onBackground = SoftCharcoal,
    surface = MorningSurface,
    onSurface = SoftCharcoal,
    surfaceVariant = MorningSurfaceVariant,
    onSurfaceVariant = CalmOnBlueContainer,
    outline = MistOutline
)

private val CalmDarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = CalmOnBlueContainer,
    primaryContainer = NightPrimaryContainer,
    onPrimaryContainer = NightText,
    secondary = NightSecondary,
    onSecondary = CalmOnTealContainer,
    secondaryContainer = NightSecondaryContainer,
    onSecondaryContainer = NightText,
    tertiary = NightPrimary,
    onTertiary = CalmOnBlueContainer,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMutedText,
    outline = NightOutline
)

@Composable
fun SimpleNoFapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context).withCalmDarkIdentity()
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context).withCalmLightIdentity()
        }

        darkTheme -> CalmDarkColorScheme
        else -> CalmLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun ColorScheme.withCalmLightIdentity(): ColorScheme {
    return copy(
        primary = CalmBlue,
        onPrimary = Color.White,
        primaryContainer = CalmBlueContainer,
        onPrimaryContainer = CalmOnBlueContainer,
        secondary = SoftTeal,
        onSecondary = Color.White,
        secondaryContainer = SoftTealContainer,
        onSecondaryContainer = CalmOnTealContainer,
        background = MorningBackground,
        onBackground = SoftCharcoal,
        surface = MorningSurface,
        onSurface = SoftCharcoal,
        surfaceVariant = MorningSurfaceVariant,
        onSurfaceVariant = CalmOnBlueContainer,
        outline = MistOutline
    )
}

private fun ColorScheme.withCalmDarkIdentity(): ColorScheme {
    return copy(
        primary = NightPrimary,
        onPrimary = CalmOnBlueContainer,
        primaryContainer = NightPrimaryContainer,
        onPrimaryContainer = NightText,
        secondary = NightSecondary,
        onSecondary = CalmOnTealContainer,
        secondaryContainer = NightSecondaryContainer,
        onSecondaryContainer = NightText,
        background = NightBackground,
        onBackground = NightText,
        surface = NightSurface,
        onSurface = NightText,
        surfaceVariant = NightSurfaceVariant,
        onSurfaceVariant = NightMutedText,
        outline = NightOutline
    )
}
