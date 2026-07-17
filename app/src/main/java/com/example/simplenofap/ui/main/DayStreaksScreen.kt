package com.example.simplenofap.ui.main

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simplenofap.daystreaks.DayStreakReward
import com.example.simplenofap.daystreaks.DayStreakType
import com.example.simplenofap.daystreaks.dayStreakName
import com.example.simplenofap.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DayStreaksScreen(
    uiState: DayStreaksUiState,
    onDismissCelebration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { testTag = "day_streaks_screen" }
    ) {
        uiState.celebrationReward?.let { reward ->
            item {
                CelebrationCard(
                    reward = reward,
                    onDismiss = onDismissCelebration
                )
            }
        }
        item {
            SummaryCard(uiState)
        }
        item {
            Text(
                text = strings.dayStreakOverview,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        items(uiState.achievements, key = { it.type.name }) { achievement ->
            AchievementRow(
                achievement = achievement,
                highlighted = uiState.highlightedType == achievement.type
            )
        }
        item {
            HistorySection(uiState.history)
        }
    }
}

@Composable
private fun CelebrationCard(
    reward: DayStreakReward,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(strings.dayStreakCelebrationTitle, style = MaterialTheme.typography.titleMedium)
            Text(strings.dayStreakCelebrationBody(strings.dayStreakName(reward.streakType)))
            Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(strings.dismiss)
            }
        }
    }
}

@Composable
private fun SummaryCard(uiState: DayStreaksUiState) {
    val strings = LocalAppStrings.current
    val nextText = uiState.nextMilestone?.let {
        "${strings.dayStreakName(it.type)} - ${formatRemaining(strings, uiState.remainingMillis ?: 0L)}"
    } ?: strings.dayStreakAllAchievementsEarned
    val cooldownText = uiState.cooldownAvailableAtEpochMillis?.let { availableAt ->
        if (uiState.nowEpochMillis >= availableAt) {
            strings.dayStreakCooldownReady
        } else {
            strings.dayStreakCooldownWaiting(formatRemaining(strings, availableAt - uiState.nowEpochMillis))
        }
    } ?: strings.dayStreakCooldownReady

    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(strings.dayStreakNextAchievement, style = MaterialTheme.typography.labelLarge)
            Text(nextText, style = MaterialTheme.typography.titleMedium)
            Text(cooldownText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: DayStreakAchievementUiState,
    highlighted: Boolean
) {
    val strings = LocalAppStrings.current
    val alpha = if (achievement.processedInCurrentAttempt || achievement.availableCount > 0) 1f else 0.68f
    val container = if (highlighted) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .semantics { testTag = "day_streak_${achievement.type.name}" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Badge(type = achievement.type)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.dayStreakName(achievement.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = strings.dayStreakThresholdDays(achievement.requiredDayCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(
                    onClick = {},
                    label = { Text(strings.dayStreakAvailableCount(achievement.availableCount)) }
                )
                Text(
                    text = if (achievement.earnedInCurrentAttempt) {
                        strings.dayStreakEarned
                    } else {
                        strings.dayStreakLocked
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Badge(type: DayStreakType) {
    val accent = when (type) {
        DayStreakType.ThreeDays -> Color(0xFF2E7D32)
        DayStreakType.OneWeek -> Color(0xFFFFA000)
        DayStreakType.ThreeWeeks -> Color(0xFF5E5CE6)
        DayStreakType.OneMonth -> Color(0xFFE65100)
        DayStreakType.TwoMonths -> Color(0xFFFFC107)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(54.dp)
            .background(accent.copy(alpha = 0.16f), CircleShape)
    ) {
        Canvas(Modifier.size(34.dp)) {
            when (type) {
                DayStreakType.ThreeDays -> {
                    drawLine(accent, Offset(size.width / 2, size.height), Offset(size.width / 2, size.height * 0.25f), strokeWidth = 4.dp.toPx())
                    drawOval(accent, topLeft = Offset(size.width * 0.15f, size.height * 0.25f), size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.28f))
                    drawOval(accent, topLeft = Offset(size.width * 0.50f, size.height * 0.12f), size = androidx.compose.ui.geometry.Size(size.width * 0.35f, size.height * 0.28f))
                }
                DayStreakType.OneWeek -> {
                    val path = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width * 0.62f, size.height * 0.38f)
                        lineTo(size.width, size.height / 2)
                        lineTo(size.width * 0.62f, size.height * 0.62f)
                        lineTo(size.width / 2, size.height)
                        lineTo(size.width * 0.38f, size.height * 0.62f)
                        lineTo(0f, size.height / 2)
                        lineTo(size.width * 0.38f, size.height * 0.38f)
                        close()
                    }
                    drawPath(path, accent)
                }
                DayStreakType.ThreeWeeks -> {
                    val shield = Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height * 0.22f)
                        lineTo(size.width * 0.82f, size.height * 0.78f)
                        lineTo(size.width / 2, size.height)
                        lineTo(size.width * 0.18f, size.height * 0.78f)
                        lineTo(0f, size.height * 0.22f)
                        close()
                    }
                    drawPath(shield, accent, style = Stroke(width = 3.dp.toPx()))
                    drawCircle(accent, radius = 4.dp.toPx(), center = Offset(size.width / 2, size.height * 0.45f))
                }
                DayStreakType.OneMonth -> {
                    val flame = Path().apply {
                        moveTo(size.width / 2, 0f)
                        cubicTo(size.width, size.height * 0.35f, size.width * 0.82f, size.height, size.width / 2, size.height)
                        cubicTo(size.width * 0.12f, size.height, 0f, size.height * 0.45f, size.width / 2, 0f)
                    }
                    drawPath(flame, accent)
                }
                DayStreakType.TwoMonths -> {
                    val crown = Path().apply {
                        moveTo(0f, size.height * 0.75f)
                        lineTo(size.width * 0.16f, size.height * 0.25f)
                        lineTo(size.width * 0.38f, size.height * 0.55f)
                        lineTo(size.width / 2, 0f)
                        lineTo(size.width * 0.62f, size.height * 0.55f)
                        lineTo(size.width * 0.84f, size.height * 0.25f)
                        lineTo(size.width, size.height * 0.75f)
                        close()
                    }
                    drawPath(crown, accent)
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<DayStreakReward>) {
    val strings = LocalAppStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = strings.dayStreakHistory,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (history.isEmpty()) {
            Text(strings.dayStreakNoHistory, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            history.forEach { reward ->
                val date = formatDate(reward.usedAtEpochMillis ?: reward.achievedAtEpochMillis)
                val line = if (reward.usedAtEpochMillis == null) {
                    strings.dayStreakEarnedAt(date)
                } else {
                    strings.dayStreakUsedAt(date)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (reward.usedAtEpochMillis == null) 1f else 0.62f)
                        .padding(vertical = 4.dp)
                ) {
                    Badge(reward.streakType)
                    Column {
                        Text(strings.dayStreakName(reward.streakType), fontWeight = FontWeight.Medium)
                        Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

internal fun formatRemaining(strings: com.example.simplenofap.localization.AppStrings, millis: Long): String {
    val totalMinutes = (millis.coerceAtLeast(0L) + 59_999L) / 60_000L
    val days = totalMinutes / (24L * 60L)
    val hours = (totalMinutes / 60L) % 24L
    val minutes = totalMinutes % 60L
    return strings.dayStreakRemainingTime(days, hours, minutes)
}

private fun formatDate(epochMillis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).format(Date(epochMillis))
}
