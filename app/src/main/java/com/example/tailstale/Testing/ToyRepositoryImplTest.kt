package com.example.tailstale

import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.PetType
import com.example.tailstale.repo.ToyRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ToyRepositoryImplTest {
    private val repo = ToyRepositoryImpl()

    @Test
    fun testGetAllToys_returnsToys() = runBlocking {
        val result = repo.getAllToys()
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
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
    fun testGetToysByPetType_returnsCorrectPetType() = runBlocking {
        val result = repo.getToysByPetType(PetType.CAT)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertTrue(it.compatiblePetTypes.contains(PetType.CAT))
        }
    }
}