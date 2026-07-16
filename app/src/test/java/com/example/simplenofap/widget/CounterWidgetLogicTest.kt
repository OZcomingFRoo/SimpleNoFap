package com.example.simplenofap.widget

import com.example.simplenofap.counter.DAY_MILLIS
import com.example.simplenofap.counter.HOUR_MILLIS
import com.example.simplenofap.counter.MINUTE_MILLIS
import com.example.simplenofap.counter.calculateCounter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CounterWidgetLogicTest {
    private val start = 10_000L
    private val elapsedRealtime = 5L * DAY_MILLIS

    @Test
    fun underDayUsesWholeElapsedDurationAndNoPrefix() {
        val now = start + 2L * HOUR_MILLIS
        val state = stateAt(now)

        assertEquals(elapsedRealtime - 2L * HOUR_MILLIS, state.baseElapsedRealtimeMillis)
        assertEquals("0%s", state.format)
        assertEquals(start + 10L * HOUR_MILLIS, state.nextRefreshEpochMillis)
    }

    @Test
    fun afterDayUsesDayRemainderAndExactStaticPrefix() {
        val now = start + 9L * DAY_MILLIS + 2L * HOUR_MILLIS
        val state = stateAt(now)

        assertEquals(elapsedRealtime - 2L * HOUR_MILLIS, state.baseElapsedRealtimeMillis)
        assertEquals("0%s", state.format)
        assertEquals(
            start + 9L * DAY_MILLIS + 10L * HOUR_MILLIS,
            state.nextRefreshEpochMillis
        )
    }

    @Test
    fun firstVisualMilestoneIsOneHour() {
        val now = start + 2L * MINUTE_MILLIS

        assertEquals(start + HOUR_MILLIS, stateAt(now).nextRefreshEpochMillis)
    }

    @Test
    fun hourPaddingTransitionsAtOneAndTenHours() {
        val beforeHour = stateAt(start + HOUR_MILLIS - 1L)
        val atHour = stateAt(start + HOUR_MILLIS)
        val atTenHours = stateAt(start + 10L * HOUR_MILLIS)

        assertEquals("%s", beforeHour.format)
        assertEquals("0%s", atHour.format)
        assertEquals("%s", atTenHours.format)
        assertEquals(start + 10L * HOUR_MILLIS, atHour.nextRefreshEpochMillis)
    }

    @Test
    fun noRefreshRemainsWhenEpochArithmeticCannotProduceOne() {
        val presentation = calculateCounter(0L, Long.MAX_VALUE)
        val state = calculateWidgetChronometerState(
            presentation = presentation,
            elapsedRealtimeMillis = elapsedRealtime,
            nowEpochMillis = Long.MAX_VALUE
        )

        assertNull(state.nextRefreshEpochMillis)
    }

    private fun stateAt(now: Long): WidgetChronometerState {
        return calculateWidgetChronometerState(
            presentation = calculateCounter(start, now),
            elapsedRealtimeMillis = elapsedRealtime,
            nowEpochMillis = now
        )
    }
}
