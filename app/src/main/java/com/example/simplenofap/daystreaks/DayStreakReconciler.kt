package com.example.simplenofap.daystreaks

import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.SettingsRepository

class DayStreakReconciler(
    private val settingsRepository: SettingsRepository,
    private val rewardRepository: DayStreakRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    suspend fun reconcile(
        nowEpochMillis: Long = currentTimeMillis(),
        expectedAttemptId: Long? = null
    ): DayStreakReconciliationResult {
        settingsRepository.initializeStartTimeIfAbsent(nowEpochMillis)
        val settings = settingsRepository.currentSettings()
        val startedAt = settings.startNoFapAtEpochMillis
            ?: return DayStreakReconciliationResult.NoActiveAttempt
        val attemptId = settings.dayStreakAttemptId ?: startedAt
        if (expectedAttemptId != null && expectedAttemptId != attemptId) {
            return DayStreakReconciliationResult.StaleAttempt
        }

        val crossed = DayStreakMilestones.unprocessedCrossedTypes(
            startedAtEpochMillis = startedAt,
            nowEpochMillis = nowEpochMillis,
            processedMask = settings.processedDayStreakMilestonesMask
        )
        if (crossed.isEmpty()) {
            return DayStreakReconciliationResult.NoReward(
                nextMilestone = nextMilestone(settings, nowEpochMillis)
            )
        }

        val grantedType = crossed.last()
        val newMask = DayStreakMilestones.markProcessed(
            settings.processedDayStreakMilestonesMask,
            crossed
        )
        val rewardId = rewardRepository.grantReward(
            type = grantedType,
            achievedAtEpochMillis = nowEpochMillis,
            sourceStreakStartAtEpochMillis = startedAt
        )
        settingsRepository.setProcessedDayStreakMilestonesMask(newMask)
        if (rewardId > 0L) {
            settingsRepository.setLastCelebratedDayStreakRewardId(rewardId)
        }
        return DayStreakReconciliationResult.Granted(
            rewardId = rewardId.takeIf { it > 0L },
            type = grantedType,
            processedMask = newMask,
            nextMilestone = nextMilestone(
                settings.copy(processedDayStreakMilestonesMask = newMask),
                nowEpochMillis
            )
        )
    }

    private fun nextMilestone(
        settings: AppSettings,
        nowEpochMillis: Long
    ): DayStreakNextMilestone? {
        val startedAt = settings.startNoFapAtEpochMillis ?: return null
        return DayStreakMilestones.nextUnprocessedFutureMilestone(
            startedAtEpochMillis = startedAt,
            nowEpochMillis = nowEpochMillis,
            processedMask = settings.processedDayStreakMilestonesMask
        )
    }
}

sealed interface DayStreakReconciliationResult {
    data class Granted(
        val rewardId: Long?,
        val type: DayStreakType,
        val processedMask: Int,
        val nextMilestone: DayStreakNextMilestone?
    ) : DayStreakReconciliationResult

    data class NoReward(
        val nextMilestone: DayStreakNextMilestone?
    ) : DayStreakReconciliationResult

    data object NoActiveAttempt : DayStreakReconciliationResult
    data object StaleAttempt : DayStreakReconciliationResult
}
