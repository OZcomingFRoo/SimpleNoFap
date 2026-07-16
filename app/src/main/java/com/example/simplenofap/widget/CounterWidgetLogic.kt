package com.example.simplenofap.widget

import com.example.simplenofap.counter.CounterPresentation
import com.example.simplenofap.counter.DAY_MILLIS
import com.example.simplenofap.counter.HOUR_MILLIS

data class WidgetChronometerState(
    val baseElapsedRealtimeMillis: Long,
    val format: String,
    val nextRefreshEpochMillis: Long?
)

fun calculateWidgetChronometerState(
    presentation: CounterPresentation,
    elapsedRealtimeMillis: Long,
    nowEpochMillis: Long
): WidgetChronometerState {
    val liveRemainder = presentation.liveRemainderMillis
    val paddingBoundary = when {
        presentation.elapsedMillis < HOUR_MILLIS ->
            nowEpochMillis + (HOUR_MILLIS - presentation.elapsedMillis)
        presentation.elapsedMillis < DAY_MILLIS && presentation.elapsedMillis < 10L * HOUR_MILLIS ->
            nowEpochMillis + (10L * HOUR_MILLIS - presentation.elapsedMillis)
        presentation.elapsedMillis >= DAY_MILLIS && liveRemainder < HOUR_MILLIS ->
            nowEpochMillis + (HOUR_MILLIS - liveRemainder)
        presentation.elapsedMillis >= DAY_MILLIS && liveRemainder < 10L * HOUR_MILLIS ->
            nowEpochMillis + (10L * HOUR_MILLIS - liveRemainder)
        else -> null
    }
    val nextRefresh = listOfNotNull(
        presentation.nextVisualBoundaryEpochMillis,
        presentation.nextStaticRefreshEpochMillis,
        paddingBoundary
    ).filter { it > nowEpochMillis }.minOrNull()
    val hourPadding = if (
        liveRemainder >= HOUR_MILLIS &&
        liveRemainder < 10L * HOUR_MILLIS
    ) {
        "0"
    } else {
        ""
    }

    return WidgetChronometerState(
        baseElapsedRealtimeMillis = elapsedRealtimeMillis - presentation.liveRemainderMillis,
        format = presentation.staticPrefix + hourPadding + "%s",
        nextRefreshEpochMillis = nextRefresh
    )
}
