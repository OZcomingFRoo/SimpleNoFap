package com.example.simplenofap.ui.main

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.simplenofap.counter.DAY_MILLIS
import com.example.simplenofap.counter.HOUR_MILLIS
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.localization.ResolvedLanguage
import com.example.simplenofap.localization.stringsFor
import com.example.simplenofap.ui.theme.SimpleNoFapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CounterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editButtonInvokesPickerAction() {
        var editClicked = false
        setCounterContent(onChangeStartTime = { editClicked = true })

        composeRule.onNodeWithTag("counter_change_start").performClick()

        composeRule.runOnIdle { assertTrue(editClicked) }
    }

    @Test
    fun resetRequiresConfirmationAndInvokesCallbackOnlyAfterConfirming() {
        var resets = 0
        setCounterContent(onResetToNow = { resets++ })

        composeRule.onNodeWithTag("counter_reset").performClick()
        composeRule.runOnIdle { assertEquals(0, resets) }
        composeRule.onNodeWithText("Reset your streak?").assertExists()
        composeRule.onNodeWithTag("counter_confirm_reset").performClick()

        composeRule.runOnIdle { assertEquals(1, resets) }
    }

    @Test
    fun hebrewKeepsCounterOrderingLtrAndLocalizesSemantics() {
        val now = 10L * DAY_MILLIS
        setCounterContent(
            language = ResolvedLanguage.Hebrew,
            startedAt = now - 2L * DAY_MILLIS - 3L * HOUR_MILLIS,
            now = now
        )

        composeRule.onNodeWithTag("counter_value")
            .assertTextContains("2 ימים")
            .assertTextContains("03:00:00")
            .assertContentDescriptionContains("רצף:")
        composeRule.onNodeWithText("הרצף שלי").assertExists()
        composeRule.onNodeWithText("שינוי זמן ההתחלה").assertExists()
    }

    @Test
    fun validationMessageIsLocalized() {
        setCounterContent(
            language = ResolvedLanguage.Hebrew,
            validationError = stringsFor(ResolvedLanguage.Hebrew).startTimeFutureError
        )

        composeRule.onNodeWithText("יש לבחור זמן שאינו בעתיד.").assertExists()
    }

    private fun setCounterContent(
        language: ResolvedLanguage = ResolvedLanguage.English,
        startedAt: Long = 0L,
        now: Long = 2L * DAY_MILLIS,
        validationError: String? = null,
        onChangeStartTime: () -> Unit = {},
        onResetToNow: () -> Unit = {}
    ) {
        composeRule.setContent {
            SimpleNoFapTheme(dynamicColor = false) {
                CompositionLocalProvider(
                    LocalAppStrings provides stringsFor(language),
                    LocalLayoutDirection provides language.layoutDirection
                ) {
                    CounterContent(
                        startedAtEpochMillis = startedAt,
                        onChangeStartTime = onChangeStartTime,
                        onResetToNow = onResetToNow,
                        validationError = validationError,
                        nowProvider = { now }
                    )
                }
            }
        }
    }
}
