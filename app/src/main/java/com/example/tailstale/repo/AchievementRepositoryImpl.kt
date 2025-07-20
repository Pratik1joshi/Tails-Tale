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
                name = "Pet Parent",
                description = "Add your first pet to start your journey",
                category = AchievementCategory.CARE,
                difficulty = AchievementDifficulty.BEGINNER,
                requirements = mapOf("petsAdded" to 1)
            ),
            Achievement(
                id = "pro_feeder",
                name = "Pro Feeder",
                description = "Feed your pet 15 or more times",
                category = AchievementCategory.FEEDING,
                difficulty = AchievementDifficulty.INTERMEDIATE,
                requirements = mapOf("feedCount" to 15)
            ),
            Achievement(
                id = "dedicated_caregiver",
                name = "Dedicated Caregiver",
                description = "Feed your pet 50 times - you're truly dedicated!",
                category = AchievementCategory.FEEDING,
                difficulty = AchievementDifficulty.ADVANCED,
                requirements = mapOf("feedCount" to 50)
            ),
            Achievement(
                id = "playtime_buddy",
                name = "Playtime Buddy",
                description = "Play with your pet 20 times",
                category = AchievementCategory.BONDING,
                difficulty = AchievementDifficulty.INTERMEDIATE,
                requirements = mapOf("playCount" to 20)
            ),
            Achievement(
                id = "health_conscious",
                name = "Health Conscious",
                description = "Give your pet 10 vaccines to keep them healthy",
                category = AchievementCategory.HEALTH,
                difficulty = AchievementDifficulty.INTERMEDIATE,
                requirements = mapOf("vaccineCount" to 10)
            ),
            Achievement(
                id = "training_master",
                name = "Training Master",
                description = "Complete 25 training sessions with your pet",
                category = AchievementCategory.TRAINING,
                difficulty = AchievementDifficulty.ADVANCED,
                requirements = mapOf("trainingCount" to 25)
            ),
            Achievement(
                id = "daily_care_streak",
                name = "Daily Care Champion",
                description = "Take care of your pet for 7 consecutive days",
                category = AchievementCategory.CARE,
                difficulty = AchievementDifficulty.ADVANCED,
                requirements = mapOf("dailyCareStreak" to 7)
            ),
            Achievement(
                id = "pet_whisperer",
                name = "Pet Whisperer",
                description = "Interact with your pet 100 times",
                category = AchievementCategory.BONDING,
                difficulty = AchievementDifficulty.EXPERT,
                requirements = mapOf("totalInteractions" to 100)
            )
        ))
    }
}
