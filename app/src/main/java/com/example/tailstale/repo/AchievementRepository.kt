package com.example.tailstale.repo

import com.example.tailstale.model.Achievement
import com.example.tailstale.model.AchievementCategory
import com.example.tailstale.model.CareActionType

interface AchievementRepository {
    suspend fun getAllAchievements(): Result<List<Achievement>>
    suspend fun getAchievementsByCategory(category: AchievementCategory): Result<List<Achievement>>
    suspend fun getAchievementById(achievementId: String): Result<Achievement?>
    suspend fun checkAchievements(userId: String, actionType: CareActionType): Result<List<Achievement>>
}