package com.example.simplenofap.counter

import java.util.Locale

const val SECOND_MILLIS = 1_000L
const val MINUTE_MILLIS = 60L * SECOND_MILLIS
const val HOUR_MILLIS = 60L * MINUTE_MILLIS
const val DAY_MILLIS = 24L * HOUR_MILLIS
const val WEEK_MILLIS = 7L * DAY_MILLIS
const val MONTH_MILLIS = 30L * DAY_MILLIS
const val YEAR_MILLIS = 360L * DAY_MILLIS

enum class CounterVisualTier {
    Neutral,
    Warm,
    Forest,
    BlueSoft,
    BlueMedium,
    BlueStrong,
    BlueSolid,
    Rainbow
}

data class CounterUnitLabels(
    val year: String,
    val years: String,
    val month: String,
    val months: String,
    val week: String,
    val weeks: String,
    val day: String,
    val days: String
) {
    fun year(value: Long) = unit(value, year, years)
    fun month(value: Long) = unit(value, month, months)
    fun week(value: Long) = unit(value, week, weeks)
    fun day(value: Long) = unit(value, day, days)

    private fun unit(value: Long, singular: String, plural: String): String {
        return "$value ${if (value == 1L) singular else plural}"
    }
}

val EnglishCounterUnitLabels = CounterUnitLabels(
    year = "year",
    years = "years",
    month = "month",
    months = "months",
    week = "week",
    weeks = "weeks",
    day = "day",
    days = "days"
)

data class CounterPresentation(
    val formattedText: String,
    val durationSummaryText: String,
    val usesStackedLayout: Boolean,
    val staticPrefix: String,
    val liveTimeText: String,
    val liveRemainderMillis: Long,
    val elapsedMillis: Long,
    val elapsedWholeSeconds: Long,
    val elapsedWholeDays: Long,
    val nextVisualBoundaryEpochMillis: Long?,
    val nextStaticRefreshEpochMillis: Long?,
    val visualTier: CounterVisualTier
)

private val visualBoundaries = listOf(
    HOUR_MILLIS to CounterVisualTier.Warm,
    DAY_MILLIS to CounterVisualTier.Forest,
    3L * DAY_MILLIS to CounterVisualTier.BlueMedium,
    WEEK_MILLIS to CounterVisualTier.BlueStrong,
    MONTH_MILLIS to CounterVisualTier.BlueSolid,
    YEAR_MILLIS to CounterVisualTier.Rainbow
)

fun calculateCounter(
    startedAtEpochMillis: Long,
    nowEpochMillis: Long,
    unitLabels: CounterUnitLabels = EnglishCounterUnitLabels
): CounterPresentation {
    val elapsedMillis = (nowEpochMillis - startedAtEpochMillis).coerceAtLeast(0L)
    val elapsedSeconds = elapsedMillis / SECOND_MILLIS
    val elapsedDays = elapsedMillis / DAY_MILLIS
    val liveRemainderMillis = if (elapsedMillis >= DAY_MILLIS) {
        elapsedMillis % DAY_MILLIS
    } else {
        elapsedMillis
    }
    val liveText = formatLiveTime(elapsedMillis, liveRemainderMillis)
    val durationSummary = formatDurationSummary(elapsedMillis, unitLabels)
    val usesStackedLayout = elapsedMillis >= WEEK_MILLIS
    val staticPrefix = when {
        durationSummary.isEmpty() || usesStackedLayout -> ""
        else -> "$durationSummary · "
    }
    val tier = visualBoundaries.lastOrNull { elapsedMillis >= it.first }?.second
        ?: CounterVisualTier.Neutral
    val nextVisualBoundary = visualBoundaries.firstOrNull { elapsedMillis < it.first }
        ?.first
        ?.let { safeAdd(startedAtEpochMillis, it) }
    val nextStaticRefresh = if (elapsedMillis >= DAY_MILLIS) {
        val nextDayElapsed = safeMultiply(elapsedDays + 1L, DAY_MILLIS)
        safeAdd(startedAtEpochMillis, nextDayElapsed)
    } else {
        null
    }

    return CounterPresentation(
        formattedText = if (usesStackedLayout) {
            "$durationSummary\n$liveText"
        } else {
            staticPrefix + liveText
        },
        durationSummaryText = durationSummary,
        usesStackedLayout = usesStackedLayout,
        staticPrefix = staticPrefix,
        liveTimeText = liveText,
        liveRemainderMillis = liveRemainderMillis,
        elapsedMillis = elapsedMillis,
        elapsedWholeSeconds = elapsedSeconds,
        elapsedWholeDays = elapsedDays,
        nextVisualBoundaryEpochMillis = nextVisualBoundary,
        nextStaticRefreshEpochMillis = nextStaticRefresh,
        visualTier = tier
    )
}

private fun formatLiveTime(elapsedMillis: Long, remainderMillis: Long): String {
    val totalSeconds = remainderMillis / SECOND_MILLIS
    val seconds = totalSeconds % 60L
    val minutes = (totalSeconds / 60L) % 60L
    val hours = totalSeconds / 3_600L

    return when {
        elapsedMillis < HOUR_MILLIS -> String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        else -> String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
    }
}

private fun formatDurationSummary(
    elapsedMillis: Long,
    labels: CounterUnitLabels
): String {
    if (elapsedMillis < DAY_MILLIS) return ""

    val totalDays = elapsedMillis / DAY_MILLIS
    val units = when {
        elapsedMillis < WEEK_MILLIS -> listOf(labels.day(totalDays))
        elapsedMillis < MONTH_MILLIS -> {
            val weeks = totalDays / 7L
            val days = totalDays % 7L
            buildList {
                if (weeks > 0L) add(labels.week(weeks))
                if (days > 0L) add(labels.day(days))
            }
        }

        elapsedMillis < YEAR_MILLIS -> {
            val months = totalDays / 30L
            val daysAfterMonths = totalDays % 30L
            val weeks = daysAfterMonths / 7L
            val days = daysAfterMonths % 7L
            buildList {
                if (months > 0L) add(labels.month(months))
                if (weeks > 0L) add(labels.week(weeks))
                if (days > 0L) add(labels.day(days))
            }
        }

        else -> {
            val years = totalDays / 360L
            val daysAfterYears = totalDays % 360L
            val months = daysAfterYears / 30L
            val remainingDays = (daysAfterYears % 30L) % 7L
            buildList {
                if (years > 0L) add(labels.year(years))
                if (months > 0L) add(labels.month(months))
                if (remainingDays > 0L) add(labels.day(remainingDays))
            }
        }
    }
    return units.joinToString(separator = ", ")
}

private fun safeAdd(left: Long, right: Long): Long? {
    if (right > 0L && left > Long.MAX_VALUE - right) return null
    if (right < 0L && left < Long.MIN_VALUE - right) return null
    return left + right
}

private fun safeMultiply(left: Long, right: Long): Long {
    if (left <= 0L || right <= 0L) return 0L
    return if (left > Long.MAX_VALUE / right) Long.MAX_VALUE else left * right
}
