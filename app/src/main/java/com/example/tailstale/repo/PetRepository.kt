package com.example.tailstale.repo

import com.example.tailstale.model.CareAction
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.VaccineRecord

interface PetRepository {
    suspend fun createPet(pet: PetModel): Result<PetModel>
    suspend fun getPetById(petId: String): Result<PetModel?>
    suspend fun getPetsByUserId(userId: String): Result<List<PetModel>>
    suspend fun updatePet(pet: PetModel): Result<PetModel>
    suspend fun deletePet(petId: String): Result<Boolean>
    suspend fun addCareAction(petId: String, action: CareAction): Result<Boolean>
    suspend fun updatePetStats(petId: String, statsUpdate: Map<String, Any>): Result<Boolean>
    suspend fun addVaccineRecord(petId: String, record: VaccineRecord): Result<Boolean>
}
