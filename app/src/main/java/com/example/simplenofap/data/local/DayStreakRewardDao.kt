package com.example.simplenofap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayStreakRewardDao {
    @Query("SELECT * FROM day_streak_rewards ORDER BY achievedAtEpochMillis DESC")
    fun observeAll(): Flow<List<DayStreakRewardEntity>>

    @Query("SELECT * FROM day_streak_rewards WHERE usedAtEpochMillis IS NULL ORDER BY achievedAtEpochMillis DESC")
    fun observeAvailable(): Flow<List<DayStreakRewardEntity>>

    @Query("SELECT COUNT(*) FROM day_streak_rewards WHERE streakType = :streakType AND usedAtEpochMillis IS NULL")
    fun observeUnusedCountByType(streakType: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM day_streak_rewards WHERE streakType = :streakType AND usedAtEpochMillis IS NULL")
    suspend fun getUnusedCountByType(streakType: String): Int

    @Query("SELECT streakType, COUNT(*) AS count FROM day_streak_rewards WHERE usedAtEpochMillis IS NULL GROUP BY streakType")
    fun observeAvailableCountsByType(): Flow<List<DayStreakRewardTypeCount>>

    @Query("SELECT MAX(usedAtEpochMillis) FROM day_streak_rewards WHERE usedAtEpochMillis IS NOT NULL")
    fun observeLatestUsedAtEpochMillis(): Flow<Long?>

    @Query("SELECT MAX(usedAtEpochMillis) FROM day_streak_rewards WHERE usedAtEpochMillis IS NOT NULL")
    suspend fun getLatestUsedAtEpochMillis(): Long?

    @Query("SELECT * FROM day_streak_rewards ORDER BY achievedAtEpochMillis DESC LIMIT :limit")
    fun observeLastAwarded(limit: Int): Flow<List<DayStreakRewardEntity>>

    @Query("SELECT * FROM day_streak_rewards WHERE usedAtEpochMillis IS NOT NULL ORDER BY usedAtEpochMillis DESC LIMIT :limit")
    fun observeLastUsed(limit: Int): Flow<List<DayStreakRewardEntity>>

    @Query("SELECT * FROM day_streak_rewards WHERE id = :id")
    suspend fun getById(id: Long): DayStreakRewardEntity?

    @Query("SELECT * FROM day_streak_rewards WHERE streakType = :streakType AND usedAtEpochMillis IS NULL ORDER BY achievedAtEpochMillis ASC, id ASC LIMIT 1")
    suspend fun getOldestUnusedByType(streakType: String): DayStreakRewardEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reward: DayStreakRewardEntity): Long

    @Update
    suspend fun update(reward: DayStreakRewardEntity)

    @Query("UPDATE day_streak_rewards SET usedAtEpochMillis = :usedAtEpochMillis, updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :id AND usedAtEpochMillis IS NULL")
    suspend fun markUsed(id: Long, usedAtEpochMillis: Long, updatedAtEpochMillis: Long): Int

    @Query("UPDATE day_streak_rewards SET usedAtEpochMillis = NULL, updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :id")
    suspend fun markUnused(id: Long, updatedAtEpochMillis: Long): Int

    @Query("DELETE FROM day_streak_rewards WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

data class DayStreakRewardTypeCount(
    val streakType: String,
    val count: Int
)
