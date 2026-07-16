package com.example.simplenofap.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.simplenofap.counter.CounterPresentation
import com.example.simplenofap.counter.CounterVisualTier
import com.example.simplenofap.counter.DAY_MILLIS
import com.example.simplenofap.counter.HOUR_MILLIS
import com.example.simplenofap.counter.MINUTE_MILLIS
import com.example.simplenofap.counter.SECOND_MILLIS
import com.example.simplenofap.counter.calculateCounter
import com.example.simplenofap.localization.LocalAppStrings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
internal fun CounterScreen(
    startedAtEpochMillis: Long?,
    onStartTimeChanged: (Long) -> Unit,
    onResetToNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var validationError by remember { mutableStateOf<String?>(null) }
    val strings = LocalAppStrings.current

    CounterContent(
        startedAtEpochMillis = startedAtEpochMillis,
        onChangeStartTime = {
            showCurrentDateTimePicker { selectedEpochMillis ->
                if (selectedEpochMillis <= System.currentTimeMillis()) {
                    validationError = null
                    onStartTimeChanged(selectedEpochMillis)
                } else {
                    validationError = strings.startTimeFutureError
                }
            }(context)
        },
        onResetToNow = onResetToNow,
        validationError = validationError,
        modifier = modifier
    )
}

@Composable
internal fun CounterContent(
    startedAtEpochMillis: Long?,
    onChangeStartTime: () -> Unit,
    onResetToNow: () -> Unit,
    validationError: String? = null,
    nowProvider: () -> Long = System::currentTimeMillis,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var nowEpochMillis by remember(startedAtEpochMillis) { mutableLongStateOf(nowProvider()) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(startedAtEpochMillis) {
        while (true) {
            nowEpochMillis = nowProvider()
            delay(SECOND_MILLIS - (nowEpochMillis % SECOND_MILLIS))
        }
    }

    val presentation = startedAtEpochMillis?.let {
        calculateCounter(it, nowEpochMillis, strings.counterUnitLabels)
    } ?: calculateCounter(nowEpochMillis, nowEpochMillis, strings.counterUnitLabels)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = strings.myStreak,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(20.dp))
        CounterCard(presentation)
        if (startedAtEpochMillis != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${strings.counterStartedAt}: ${formatStartTime(startedAtEpochMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (validationError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onChangeStartTime,
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "counter_change_start" }
            ) {
                Text(strings.changeStartTime)
            }
            Button(
                onClick = { showResetConfirmation = true },
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "counter_reset" }
            ) {
                Text(strings.resetToNow)
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text(strings.resetCounterTitle) },
            text = { Text(strings.resetCounterBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmation = false
                        onResetToNow()
                    },
                    modifier = Modifier.semantics { testTag = "counter_confirm_reset" }
                ) {
                    Text(strings.confirmReset)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun CounterCard(presentation: CounterPresentation) {
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val targetColors = counterGradient(presentation.visualTier, darkTheme)
    val firstColor by animateColorAsState(targetColors[0], tween(700), label = "counterGradientStart")
    val middleColor by animateColorAsState(targetColors[1], tween(700), label = "counterGradientMiddle")
    val lastColor by animateColorAsState(targetColors[2], tween(700), label = "counterGradientEnd")
    val textColor = counterTextColor(presentation.visualTier, darkTheme)
    val strings = LocalAppStrings.current
    val wholeSeconds = presentation.elapsedWholeSeconds
    val days = wholeSeconds / 86_400L
    val hours = (wholeSeconds / 3_600L) % 24L
    val minutes = (wholeSeconds / 60L) % 60L
    val seconds = wholeSeconds % 60L
    val description = strings.counterAccessibility(days, hours, minutes, seconds)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(firstColor, middleColor, lastColor)))
            .semantics(mergeDescendants = true) {
                contentDescription = description
                testTag = "counter_value"
            }
            .padding(horizontal = 18.dp, vertical = 38.dp)
    ) {
        if (presentation.usesStackedLayout) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(
                    targetState = presentation.durationSummaryText,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "counterSummary"
                ) { summary ->
                    Text(
                        text = summary,
                        color = textColor,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AnimatedCounterText(
                        text = presentation.liveTimeText,
                        color = textColor
                    )
                }
            }
        } else {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                AnimatedCounterText(
                    text = presentation.formattedText,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun AnimatedCounterText(text: String, color: Color) {
    AnimatedContent(
        targetState = text,
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
        label = "counterText"
    ) { displayedText ->
        Text(
            text = displayedText,
            color = color,
            maxLines = 1,
            style = MaterialTheme.typography.headlineLarge.merge(
                TextStyle(
                    fontFeatureSettings = "tnum",
                    fontWeight = FontWeight.Bold
                )
            )
        )
    }
}

@Composable
private fun counterGradient(tier: CounterVisualTier, darkTheme: Boolean): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return when (tier) {
        CounterVisualTier.Neutral -> listOf(scheme.surfaceVariant, scheme.surface, scheme.surfaceVariant)
        CounterVisualTier.Warm -> if (darkTheme) {
            listOf(Color(0xFFC69200), Color(0xFFF2C94C), Color(0xFFA97800))
        } else {
            listOf(Color(0xFFFFE082), Color(0xFFFFF3B0), Color(0xFFFFD54F))
        }
        CounterVisualTier.Forest -> listOf(Color(0xFF075E36), Color(0xFF12A05C), Color(0xFF034A29))
        CounterVisualTier.BlueSoft -> listOf(Color(0xFF447DA5), Color(0xFF6097BD), Color(0xFF326B94))
        CounterVisualTier.BlueMedium -> listOf(Color(0xFF286B9B), Color(0xFF3987BC), Color(0xFF17547F))
        CounterVisualTier.BlueStrong -> listOf(Color(0xFF155887), Color(0xFF2479AF), Color(0xFF0E4268))
        CounterVisualTier.BlueSolid -> listOf(Color(0xFF073C67), Color(0xFF0D5D91), Color(0xFF052F52))
        CounterVisualTier.Rainbow -> listOf(Color(0xFFB3261E), Color(0xFF287A52), Color(0xFF155887))
    }
}

@Composable
private fun counterTextColor(tier: CounterVisualTier, darkTheme: Boolean): Color {
    return when (tier) {
        CounterVisualTier.Neutral -> MaterialTheme.colorScheme.onSurface
        CounterVisualTier.Warm -> Color(0xFF2E2300)
        else -> Color.White
    }
}

private fun formatStartTime(epochMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date(epochMillis))
}

private fun showCurrentDateTimePicker(
    onSelected: (Long) -> Unit
): (android.content.Context) -> Unit = { context ->
    val now = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val selected = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onSelected(selected.timeInMillis)
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(context)
            ).show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = now.timeInMillis
    }.show()
}
