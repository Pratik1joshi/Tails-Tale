package com.example.tailstale.repo

import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineModel

interface VaccineRepository {
    suspend fun getAllVaccines(): Result<List<VaccineModel>>
    suspend fun getVaccinesByPetType(petType: PetType): Result<List<VaccineModel>>
    suspend fun getVaccineById(vaccineId: String): Result<VaccineModel?>
    suspend fun getVaccinesByAgeRequirement(petAge: Int): Result<List<VaccineModel>>
    suspend fun getRequiredVaccines(petType: PetType, petAge: Int): Result<List<VaccineModel>>
}
