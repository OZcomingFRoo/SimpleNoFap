package com.example.simplenofap.daystreaks

import com.example.simplenofap.data.local.DayStreakRewardDao
import com.example.simplenofap.data.local.DayStreakRewardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val RewardCooldownMillis = 72L * 60L * 60L * 1_000L

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

    fun observeAvailableCountsByType(): Flow<Map<DayStreakType, Int>> {
        return dao.observeAvailableCountsByType().map { counts ->
            counts.associate { DayStreakType.valueOf(it.streakType) to it.count }
        }
    }

    fun observeLatestUsedAtEpochMillis(): Flow<Long?> {
        return dao.observeLatestUsedAtEpochMillis()
    }

    suspend fun getLatestUsedAtEpochMillis(): Long? {
        return dao.getLatestUsedAtEpochMillis()
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

    suspend fun consumeOldestAvailableReward(
        type: DayStreakType,
        usedAtEpochMillis: Long = currentTimeMillis()
    ): DayStreakConsumeResult {
        val latestUsedAt = dao.getLatestUsedAtEpochMillis()
        if (latestUsedAt != null) {
            val allowedAt = latestUsedAt + RewardCooldownMillis
            if (usedAtEpochMillis < allowedAt) {
                return DayStreakConsumeResult.Cooldown(
                    latestUsedAtEpochMillis = latestUsedAt,
                    availableAtEpochMillis = allowedAt
                )
            }
        }
        val reward = dao.getOldestUnusedByType(type.name)
            ?: return DayStreakConsumeResult.NoRewardAvailable
        val changed = dao.markUsed(
            id = reward.id,
            usedAtEpochMillis = usedAtEpochMillis,
            updatedAtEpochMillis = currentTimeMillis()
        )
        return if (changed > 0) {
            DayStreakConsumeResult.Consumed(reward.toDomain().copy(usedAtEpochMillis = usedAtEpochMillis))
        } else {
            DayStreakConsumeResult.NoRewardAvailable
        }
    }

    fun cooldownAvailableAt(latestUsedAtEpochMillis: Long?): Long? {
        return latestUsedAtEpochMillis?.let { it + RewardCooldownMillis }
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
        const val CooldownMillis = RewardCooldownMillis
    }
}

sealed interface DayStreakConsumeResult {
    data class Consumed(val reward: DayStreakReward) : DayStreakConsumeResult
    data object NoRewardAvailable : DayStreakConsumeResult
    data class Cooldown(
        val latestUsedAtEpochMillis: Long,
        val availableAtEpochMillis: Long
    ) : DayStreakConsumeResult
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
