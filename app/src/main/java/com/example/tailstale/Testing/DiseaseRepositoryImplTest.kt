package com.example.tailstale

import com.example.tailstale.model.PetType
import com.example.tailstale.model.DiseaseSeverity
import com.example.tailstale.repo.DiseaseRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DiseaseRepositoryImplTest {
    private val repo = DiseaseRepositoryImpl()

    @Test
    fun testGetAllDiseases_returnsDiseases() = runBlocking {
        val result = repo.getAllDiseases()
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull().isNullOrEmpty())
    }

    @Test
    fun testGetDiseasesByPetType_returnsCorrectPetType() = runBlocking {
        val result = repo.getDiseasesByPetType(PetType.DOG)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertTrue(it.affectedPetTypes.contains(PetType.DOG))
        }
    }

    @Test
    fun testGetDiseasesBySeverity_returnsCorrectSeverity() = runBlocking {
        val result = repo.getDiseasesBySeverity(DiseaseSeverity.HIGH)
        assertTrue(result.isSuccess)
        result.getOrNull()?.forEach {
            assertEquals(DiseaseSeverity.HIGH, it.severity)
        }
    }
}