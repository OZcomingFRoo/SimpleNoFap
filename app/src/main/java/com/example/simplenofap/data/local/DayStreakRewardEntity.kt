package com.example.simplenofap.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_streak_rewards",
    indices = [
        Index(value = ["streakType"]),
        Index(value = ["achievedAtEpochMillis"]),
        Index(value = ["usedAtEpochMillis"])
    ]
)
data class DayStreakRewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val streakType: String,
    val achievedAtEpochMillis: Long,
    val usedAtEpochMillis: Long?,
    val sourceStreakStartAtEpochMillis: Long?
)
