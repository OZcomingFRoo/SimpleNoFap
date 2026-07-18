package com.example.simplenofap.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenofap.daystreaks.AndroidDayStreakAlarmScheduler
import com.example.simplenofap.daystreaks.DayStreakConsumeResult
import com.example.simplenofap.daystreaks.DayStreakMilestones
import com.example.simplenofap.daystreaks.DayStreakNextMilestone
import com.example.simplenofap.daystreaks.DayStreakReconciler
import com.example.simplenofap.daystreaks.DayStreakRepository
import com.example.simplenofap.daystreaks.DayStreakReward
import com.example.simplenofap.daystreaks.DayStreakType
import com.example.simplenofap.daystreaks.dayStreakRepository
import com.example.simplenofap.settings.AppSettings
import com.example.simplenofap.settings.SettingsRepository
import com.example.simplenofap.widget.CounterWidgetUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class DayStreaksUiState(
    val achievements: List<DayStreakAchievementUiState> = emptyList(),
    val history: List<DayStreakReward> = emptyList(),
    val nextMilestone: DayStreakNextMilestone? = null,
    val remainingMillis: Long? = null,
    val cooldownAvailableAtEpochMillis: Long? = null,
    val nowEpochMillis: Long = 0L,
    val celebrationReward: DayStreakReward? = null,
    val highlightedType: DayStreakType? = null,
    val errorMessageVisible: Boolean = false
) {
    val availableRewards: List<DayStreakAchievementUiState>
        get() = achievements.filter { it.availableCount > 0 }

    val isInCooldown: Boolean
        get() = cooldownAvailableAtEpochMillis?.let { nowEpochMillis < it } == true
}

internal data class DayStreakAchievementUiState(
    val type: DayStreakType,
    val requiredDayCount: Int,
    val availableCount: Int,
    val earnedInCurrentAttempt: Boolean,
    val processedInCurrentAttempt: Boolean
)

private data class RewardSnapshot(
    val rewards: List<DayStreakReward>,
    val counts: Map<DayStreakType, Int>,
    val history: List<DayStreakReward>,
    val latestUsedAtEpochMillis: Long?
)

internal class DayStreaksViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val rewardRepository = application.dayStreakRepository()
    private val alarmScheduler = AndroidDayStreakAlarmScheduler(application)
    private val now = MutableStateFlow(System.currentTimeMillis())
    private val highlightedType = MutableStateFlow<DayStreakType?>(null)
    private val errorVisible = MutableStateFlow(false)

    private val rewardSnapshot = combine(
        rewardRepository.observeRewards(),
        rewardRepository.observeAvailableCountsByType(),
        rewardRepository.observeLastAwarded(),
        rewardRepository.observeLatestUsedAtEpochMillis()
    ) { rewards, counts, history, latestUsedAt ->
        RewardSnapshot(rewards, counts, history, latestUsedAt)
    }

    val uiState: StateFlow<DayStreaksUiState> = combine(
        settingsRepository.settings,
        rewardSnapshot,
        now,
        highlightedType,
        errorVisible
    ) { settings, rewards, nowEpochMillis, highlighted, error ->
        settings.toUiState(rewards, nowEpochMillis, highlighted, error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayStreaksUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                now.value = System.currentTimeMillis()
                delay(60_000L)
            }
        }
        viewModelScope.launch {
            DayStreakReconciler(settingsRepository, rewardRepository).reconcile()
            alarmScheduler.reconcile(settingsRepository)
        }
    }

    fun highlight(type: DayStreakType?) {
        highlightedType.value = type
    }

    fun dismissCelebration() {
        viewModelScope.launch {
            settingsRepository.setLastCelebratedDayStreakRewardId(null)
        }
    }

    fun consumeReward(type: DayStreakType, onConsumed: () -> Unit = {}) {
        viewModelScope.launch {
            when (rewardRepository.consumeOldestAvailableReward(type)) {
                is DayStreakConsumeResult.Consumed -> {
                    errorVisible.value = false
                    onConsumed()
                }
                DayStreakConsumeResult.NoRewardAvailable,
                is DayStreakConsumeResult.Cooldown -> {
                    errorVisible.value = true
                }
            }
        }
    }

    fun resetToNow() {
        viewModelScope.launch {
            settingsRepository.resetStartNoFapToNow()
            CounterWidgetUpdater.refreshAll(getApplication())
            alarmScheduler.reconcile(settingsRepository)
        }
    }

    private fun AppSettings.toUiState(
        rewards: RewardSnapshot,
        nowEpochMillis: Long,
        highlighted: DayStreakType?,
        error: Boolean
    ): DayStreaksUiState {
        val startedAt = startNoFapAtEpochMillis
        val next = if (startedAt == null) {
            null
        } else {
            DayStreakMilestones.nextUnprocessedFutureMilestone(
                startedAtEpochMillis = startedAt,
                nowEpochMillis = nowEpochMillis,
                processedMask = processedDayStreakMilestonesMask
            )
        }
        val currentRewardSourceId = startedAt
        val achievements = DayStreakMilestones.all.map { metadata ->
            val bit = DayStreakMilestones.bitFor(metadata.type)
            DayStreakAchievementUiState(
                type = metadata.type,
                requiredDayCount = metadata.requiredDayCount,
                availableCount = rewards.counts[metadata.type] ?: 0,
                earnedInCurrentAttempt = rewards.rewards.any {
                    it.streakType == metadata.type &&
                        it.sourceStreakStartAtEpochMillis == currentRewardSourceId
                },
                processedInCurrentAttempt = processedDayStreakMilestonesMask and bit != 0
            )
        }
        val celebration = lastCelebratedDayStreakRewardId?.let { id ->
            rewards.rewards.firstOrNull { it.id == id }
        }
        val remaining = next?.triggerAtEpochMillis?.minus(nowEpochMillis)?.coerceAtLeast(0L)
        val cooldown = rewardRepository.cooldownAvailableAt(rewards.latestUsedAtEpochMillis)
        return DayStreaksUiState(
            achievements = achievements,
            history = rewards.history,
            nextMilestone = next,
            remainingMillis = remaining,
            cooldownAvailableAtEpochMillis = cooldown,
            nowEpochMillis = nowEpochMillis,
            celebrationReward = celebration,
            highlightedType = highlighted,
            errorMessageVisible = error
        )
    }
}
