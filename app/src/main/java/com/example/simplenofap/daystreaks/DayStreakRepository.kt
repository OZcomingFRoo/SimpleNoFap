package com.example.simplenofap.daystreaks

import com.example.simplenofap.data.local.DayStreakRewardDao
import com.example.simplenofap.data.local.DayStreakRewardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DayStreakRepository(
    private val dao: DayStreakRewardDao,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    fun observeRewards(): Flow<List<DayStreakReward>> {
        return dao.observeAll().map { rewards -> rewards.map { it.toDomain() } }
    }

    fun observeAvailableRewards(): Flow<List<DayStreakReward>> {
        return dao.observeAvailable().map { rewards -> rewards.map { it.toDomain() } }
    }

    fun observeUnusedCount(type: DayStreakType): Flow<Int> {
        return dao.observeUnusedCountByType(type.name)
    }

    suspend fun getUnusedCount(type: DayStreakType): Int {
        return dao.getUnusedCountByType(type.name)
    }

    fun observeLastAwarded(limit: Int = DefaultRecentRewardLimit): Flow<List<DayStreakReward>> {
        require(limit > 0) { "limit must be greater than 0." }
        return dao.observeLastAwarded(limit).map { rewards -> rewards.map { it.toDomain() } }
    }

    fun observeLastUsed(limit: Int = DefaultRecentRewardLimit): Flow<List<DayStreakReward>> {
        require(limit > 0) { "limit must be greater than 0." }
        return dao.observeLastUsed(limit).map { rewards -> rewards.map { it.toDomain() } }
    }

    suspend fun insertReward(reward: DayStreakReward): Long {
        return dao.insert(reward.toEntity())
    }

    suspend fun grantReward(
        type: DayStreakType,
        achievedAtEpochMillis: Long = currentTimeMillis(),
        sourceStreakStartAtEpochMillis: Long? = null
    ): Long {
        val now = currentTimeMillis()
        return dao.insert(
            DayStreakRewardEntity(
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
                streakType = type.name,
                achievedAtEpochMillis = achievedAtEpochMillis,
                usedAtEpochMillis = null,
                sourceStreakStartAtEpochMillis = sourceStreakStartAtEpochMillis
            )
        )
    }

    suspend fun markRewardUsed(id: Long, usedAtEpochMillis: Long = currentTimeMillis()): Boolean {
        return dao.markUsed(
            id = id,
            usedAtEpochMillis = usedAtEpochMillis,
            updatedAtEpochMillis = currentTimeMillis()
        ) > 0
    }

    suspend fun markRewardUnused(id: Long): Boolean {
        return dao.markUnused(id, currentTimeMillis()) > 0
    }

    suspend fun deleteReward(id: Long): Boolean {
        return dao.deleteById(id) > 0
    }

    companion object {
        const val DefaultRecentRewardLimit = 5
    }
}

private fun DayStreakRewardEntity.toDomain(): DayStreakReward {
    return DayStreakReward(
        id = id,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        streakType = DayStreakType.valueOf(streakType),
        achievedAtEpochMillis = achievedAtEpochMillis,
        usedAtEpochMillis = usedAtEpochMillis,
        sourceStreakStartAtEpochMillis = sourceStreakStartAtEpochMillis
    )
}

private fun DayStreakReward.toEntity(): DayStreakRewardEntity {
    return DayStreakRewardEntity(
        id = id,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
        streakType = streakType.name,
        achievedAtEpochMillis = achievedAtEpochMillis,
        usedAtEpochMillis = usedAtEpochMillis,
        sourceStreakStartAtEpochMillis = sourceStreakStartAtEpochMillis
    )
}
