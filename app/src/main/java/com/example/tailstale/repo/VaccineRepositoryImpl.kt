package com.example.tailstale.repo

import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineModel

class VaccineRepositoryImpl : VaccineRepository {
    private val vaccines = mutableListOf<VaccineModel>()

    init {
        initializeVaccineData()
    }

    override suspend fun getAllVaccines(): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccinesByPetType(petType: PetType): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter { it.compatiblePetTypes.contains(petType) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccineById(vaccineId: String): Result<VaccineModel?> {
        return try {
            Result.success(vaccines.find { it.id == vaccineId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccinesByAgeRequirement(petAge: Int): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter { petAge in it.ageRequirement })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRequiredVaccines(petType: PetType, petAge: Int): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter {
                it.compatiblePetTypes.contains(petType) && petAge in it.ageRequirement
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeVaccineData() {
        vaccines.addAll(listOf(
            VaccineModel(
                name = "Rabies Vaccine",
                description = "Essential vaccine to prevent rabies",
                targetDisease = "Rabies",
                effectiveDurationDays = 365,
                cost = 50,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 12..Int.MAX_VALUE,
                sideEffects = listOf("Mild fatigue", "Slight fever")
            ),
            VaccineModel(
                name = "DHPP Vaccine",
                description = "Protects against Distemper, Hepatitis, Parvovirus, and Parainfluenza",
                targetDisease = "Multiple",
                effectiveDurationDays = 365,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 6..Int.MAX_VALUE,
                sideEffects = listOf("Mild swelling at injection site")
            )
        ))
    }
}
