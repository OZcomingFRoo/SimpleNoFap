package com.example.simplenofap.daystreaks

data class DayStreakReward(
    val id: Long = 0,
    val createdAtEpochMillis: Long = 0,
    val updatedAtEpochMillis: Long = 0,
    val streakType: DayStreakType,
    val achievedAtEpochMillis: Long,
    val usedAtEpochMillis: Long?,
    val sourceStreakStartAtEpochMillis: Long?
) {
    val isAvailable: Boolean
        get() = usedAtEpochMillis == null
}
