package com.example.simplenofap.daystreaks

import com.example.simplenofap.data.local.DayStreakRewardDao
import com.example.simplenofap.data.local.DayStreakRewardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DayStreakRepositoryTest {
    @Test
    fun unusedCountByType_countsOnlyRewardsWhereUsedAtIsNull() = runBlocking {
        val dao = FakeDayStreakRewardDao()
        val repository = DayStreakRepository(dao)

        repository.insertReward(reward(type = DayStreakType.OneWeek, usedAtEpochMillis = null))
        repository.insertReward(reward(type = DayStreakType.OneWeek, usedAtEpochMillis = 2_000))
        repository.insertReward(reward(type = DayStreakType.OneWeek, usedAtEpochMillis = null))
        repository.insertReward(reward(type = DayStreakType.TwoWeeks, usedAtEpochMillis = null))

        assertEquals(2, repository.getUnusedCount(DayStreakType.OneWeek))
        assertEquals(1, repository.observeUnusedCount(DayStreakType.TwoWeeks).first())
    }

    @Test
    fun observeLastAwarded_returnsFiveMostRecentByAchievedAt() = runBlocking {
        val dao = FakeDayStreakRewardDao()
        val repository = DayStreakRepository(dao)

        listOf(10L, 50L, 20L, 70L, 30L, 60L).forEach { achievedAt ->
            repository.insertReward(reward(achievedAtEpochMillis = achievedAt))
        }

        val achievedTimes = repository.observeLastAwarded().first()
            .map { it.achievedAtEpochMillis }

        assertEquals(listOf(70L, 60L, 50L, 30L, 20L), achievedTimes)
    }

    @Test
    fun observeLastUsed_returnsFiveMostRecentUsedRewards() = runBlocking {
        val dao = FakeDayStreakRewardDao()
        val repository = DayStreakRepository(dao)

        listOf(null, 10L, 50L, 20L, 70L, 30L, 60L).forEach { usedAt ->
            repository.insertReward(reward(usedAtEpochMillis = usedAt))
        }

        val usedTimes = repository.observeLastUsed().first()
            .map { it.usedAtEpochMillis }

        assertEquals(listOf(70L, 60L, 50L, 30L, 20L), usedTimes)
    }

    @Test
    fun dayStreakReward_isAvailableWhenUsedAtIsNull() {
        assertTrue(reward(usedAtEpochMillis = null).isAvailable)
    }

    private fun reward(
        type: DayStreakType = DayStreakType.ThreeDays,
        achievedAtEpochMillis: Long = 1_000,
        usedAtEpochMillis: Long? = null
    ): DayStreakReward {
        return DayStreakReward(
            createdAtEpochMillis = 1,
            updatedAtEpochMillis = 1,
            streakType = type,
            achievedAtEpochMillis = achievedAtEpochMillis,
            usedAtEpochMillis = usedAtEpochMillis,
            sourceStreakStartAtEpochMillis = null
        )
    }
}

private class FakeDayStreakRewardDao : DayStreakRewardDao {
    private val rewards = MutableStateFlow<List<DayStreakRewardEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<DayStreakRewardEntity>> {
        return rewards.map { rewardList ->
            rewardList.sortedByDescending { it.achievedAtEpochMillis }
        }
    }

    override fun observeAvailable(): Flow<List<DayStreakRewardEntity>> {
        return rewards.map { rewardList ->
            rewardList
                .filter { it.usedAtEpochMillis == null }
                .sortedByDescending { it.achievedAtEpochMillis }
        }
    }

    override fun observeUnusedCountByType(streakType: String): Flow<Int> {
        return rewards.map { rewardList ->
            rewardList.count { it.streakType == streakType && it.usedAtEpochMillis == null }
        }
    }

    override suspend fun getUnusedCountByType(streakType: String): Int {
        return rewards.value.count { it.streakType == streakType && it.usedAtEpochMillis == null }
    }

    override fun observeLastAwarded(limit: Int): Flow<List<DayStreakRewardEntity>> {
        return rewards.map { rewardList ->
            rewardList
                .sortedByDescending { it.achievedAtEpochMillis }
                .take(limit)
        }
    }

    override fun observeLastUsed(limit: Int): Flow<List<DayStreakRewardEntity>> {
        return rewards.map { rewardList ->
            rewardList
                .filter { it.usedAtEpochMillis != null }
                .sortedByDescending { it.usedAtEpochMillis }
                .take(limit)
        }
    }

    override suspend fun getById(id: Long): DayStreakRewardEntity? {
        return rewards.value.firstOrNull { it.id == id }
    }

    override suspend fun insert(reward: DayStreakRewardEntity): Long {
        val assignedId = if (reward.id == 0L) nextId++ else reward.id
        rewards.value += reward.copy(id = assignedId)
        return assignedId
    }

    override suspend fun update(reward: DayStreakRewardEntity) {
        rewards.value = rewards.value.map { existing ->
            if (existing.id == reward.id) reward else existing
        }
    }

    override suspend fun markUsed(
        id: Long,
        usedAtEpochMillis: Long,
        updatedAtEpochMillis: Long
    ): Int {
        var changed = false
        rewards.value = rewards.value.map { reward ->
            if (reward.id == id) {
                changed = true
                reward.copy(
                    usedAtEpochMillis = usedAtEpochMillis,
                    updatedAtEpochMillis = updatedAtEpochMillis
                )
            } else {
                reward
            }
        }
        return if (changed) 1 else 0
    }

    override suspend fun markUnused(id: Long, updatedAtEpochMillis: Long): Int {
        var changed = false
        rewards.value = rewards.value.map { reward ->
            if (reward.id == id) {
                changed = true
                reward.copy(
                    usedAtEpochMillis = null,
                    updatedAtEpochMillis = updatedAtEpochMillis
                )
            } else {
                reward
            }
        }
        return if (changed) 1 else 0
    }

    override suspend fun deleteById(id: Long): Int {
        val beforeSize = rewards.value.size
        rewards.value = rewards.value.filterNot { it.id == id }
        return beforeSize - rewards.value.size
    }
}
