package com.example.tailstale

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.PetType
import com.example.tailstale.repo.FoodRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FoodRepositoryImplTest {
    private val repo = FoodRepositoryImpl()

    @Test
    fun testGetAllFood_returnsFoods() = runBlocking {
        val result = repo.getAllFood()
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun testGetFoodByCategory_returnsCorrectCategory() = runBlocking {
        val result = repo.getFoodByCategory(FoodCategory.DRY_FOOD)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertEquals(FoodCategory.DRY_FOOD, it.category)
        }
    }

    @Test
    fun testGetFoodByPetType_returnsCorrectPetType() = runBlocking {
        val result = repo.getFoodByPetType(PetType.DOG)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertTrue(it.compatiblePetTypes.contains(PetType.DOG))
        }
    }
}