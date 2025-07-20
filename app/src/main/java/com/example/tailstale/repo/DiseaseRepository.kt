package com.example.tailstale.repo

import com.example.tailstale.model.DiseaseModel
import com.example.tailstale.model.DiseaseSeverity
import com.example.tailstale.model.PetType

interface DiseaseRepository {
    suspend fun getAllDiseases(): Result<List<DiseaseModel>>
    suspend fun getDiseasesByPetType(petType: PetType): Result<List<DiseaseModel>>
    suspend fun getDiseasesBySeverity(severity: DiseaseSeverity): Result<List<DiseaseModel>>
    suspend fun getDiseaseById(diseaseId: String): Result<DiseaseModel?>
    suspend fun getRandomDisease(petType: PetType): Result<DiseaseModel?>
}