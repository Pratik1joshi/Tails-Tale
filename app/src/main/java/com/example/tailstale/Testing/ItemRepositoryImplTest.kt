package com.example.tailstale

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.PetType
import com.example.tailstale.model.ItemCategory
import com.example.tailstale.repo.ItemRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ItemRepositoryImplTest {
    private val repo = ItemRepositoryImpl()

    @Test
    fun testGetAllFood_returnsFoods() = runBlocking {
        val result = repo.getAllFood()
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun testGetAllToys_returnsToys() = runBlocking {
        val result = repo.getAllToys()
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
    fun testGetToysByCategory_returnsCorrectCategory() = runBlocking {
        val result = repo.getToysByCategory(ToyCategory.PUZZLE)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertEquals(ToyCategory.PUZZLE, it.category)
        }
    }

    @Test
    fun testGetItemsByPetType_returnsCorrectItems() = runBlocking {
        val result = repo.getItemsByPetType(PetType.DOG)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isNotEmpty() == true)
        assertTrue(result.getOrNull()?.containsKey(ItemCategory.FOOD) == true)
        assertTrue(result.getOrNull()?.containsKey(ItemCategory.TOY) == true)
    }
}
