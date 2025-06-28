package com.example.tailstale.repo

import com.example.tailstale.model.Achievement
import com.example.tailstale.model.AchievementCategory
import com.example.tailstale.model.AchievementDifficulty
import com.example.tailstale.model.CareActionType

class AchievementRepositoryImpl : AchievementRepository {
    private val achievements = mutableListOf<Achievement>()

    init {
        initializeSampleAchievements()
    }

    override suspend fun getAllAchievements(): Result<List<Achievement>> {
        return try {
            Result.success(achievements.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAchievementsByCategory(category: AchievementCategory): Result<List<Achievement>> {
        return try {
            Result.success(achievements.filter { it.category == category })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAchievementById(achievementId: String): Result<Achievement?> {
        return try {
            Result.success(achievements.find { it.id == achievementId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAchievements(userId: String, actionType: CareActionType): Result<List<Achievement>> {
        return try {
            // This would implement logic to check if any achievements are unlocked
            // based on the user's actions
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeSampleAchievements() {
        achievements.addAll(listOf(
            Achievement(
                id = "first_pet",
                name = "First Pet Parent",
                description = "Adopt your first virtual pet",
                category = AchievementCategory.CARETAKER,
                difficulty = AchievementDifficulty.EASY,
                rewardCoins = 50,
                rewardGems = 2,
                rewardExperience = 100,
                requirements = mapOf("petsAdopted" to 1)
            ),
            Achievement(
                id = "feeding_master",
                name = "Feeding Master",
                description = "Feed pets 100 times",
                category = AchievementCategory.CARETAKER,
                difficulty = AchievementDifficulty.MEDIUM,
                rewardCoins = 200,
                rewardGems = 5,
                rewardExperience = 500,
                requirements = mapOf("feedCount" to 100)
            )
        ))
    }
}
