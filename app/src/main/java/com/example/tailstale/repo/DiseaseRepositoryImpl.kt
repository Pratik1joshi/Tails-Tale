package com.example.tailstale.repo

import com.example.tailstale.model.DiseaseCategory
import com.example.tailstale.model.DiseaseModel
import com.example.tailstale.model.DiseaseSeverity
import com.example.tailstale.model.PetType

class DiseaseRepositoryImpl : DiseaseRepository {
    private val diseases = mutableListOf<DiseaseModel>()

    init {
        initializeSampleDiseases()
    }

    override suspend fun getAllDiseases(): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseasesByPetType(petType: PetType): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.filter { it.affectedPetTypes.contains(petType) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseasesBySeverity(severity: DiseaseSeverity): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.filter { it.severity == severity })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseaseById(diseaseId: String): Result<DiseaseModel?> {
        return try {
            Result.success(diseases.find { it.id == diseaseId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomDisease(petType: PetType): Result<DiseaseModel?> {
        return try {
            val compatibleDiseases = diseases.filter { it.affectedPetTypes.contains(petType) }
            Result.success(compatibleDiseases.randomOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeSampleDiseases() {
        diseases.addAll(listOf(
            DiseaseModel(
                name = "Common Cold",
                description = "A mild respiratory infection",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.VIRAL,
                healthImpact = 10,
                happinessImpact = 5,
                durationDays = 3..7,
                treatmentCost = 20,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Sneezing", "Runny nose", "Mild cough"),
                preventionTips = listOf("Keep pet warm", "Avoid exposure to sick animals"),
                treatmentSteps = listOf("Rest", "Keep warm", "Monitor symptoms")
            )
        ))
    }
}
