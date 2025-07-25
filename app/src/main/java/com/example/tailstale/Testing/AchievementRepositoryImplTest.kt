package com.example.tailstale

import com.example.tailstale.model.AchievementCategory
import com.example.tailstale.model.CareActionType
import com.example.tailstale.repo.AchievementRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AchievementRepositoryImplTest {
    private val repo = AchievementRepositoryImpl()

    @Test
    fun testGetAllAchievements_returnsAchievements() = runBlocking {
        val result = repo.getAllAchievements()
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun testGetAchievementsByCategory_returnsCorrectCategory() = runBlocking {
        val result = repo.getAchievementsByCategory(AchievementCategory.CARE)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertEquals(AchievementCategory.CARE, it.category)
        }
    }

    @Test
    fun testCheckAchievements_returnsAchievementsList() = runBlocking {
        val result = repo.checkAchievements("userId", CareActionType.FEEDING)
        assertTrue(result.isSuccess)
    }
}