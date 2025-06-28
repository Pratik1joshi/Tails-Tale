package com.example.tailstale.repo

import com.example.tailstale.model.CareAction
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.VaccineRecord

class PetRepositoryImpl : PetRepository {
    private val pets = mutableMapOf<String, PetModel>()
    private val userPets = mutableMapOf<String, MutableList<String>>()

    override suspend fun createPet(pet: PetModel): Result<PetModel> {
        return try {
            pets[pet.id] = pet
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPetById(petId: String): Result<PetModel?> {
        return try {
            Result.success(pets[petId])
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPetsByUserId(userId: String): Result<List<PetModel>> {
        return try {
            val petIds = userPets[userId] ?: emptyList()
            val userPetsList = petIds.mapNotNull { pets[it] }
            Result.success(userPetsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePet(pet: PetModel): Result<PetModel> {
        return try {
            pets[pet.id] = pet
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePet(petId: String): Result<Boolean> {
        return try {
            pets.remove(petId)
            // Remove from user's pet list
            userPets.values.forEach { it.remove(petId) }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCareAction(petId: String, action: CareAction): Result<Boolean> {
        return try {
            pets[petId]?.let { pet ->
                pet.careLog.add(action)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePetStats(petId: String, statsUpdate: Map<String, Any>): Result<Boolean> {
        return try {
            pets[petId]?.let { pet ->
                var updatedPet = pet
                statsUpdate.forEach { (stat, value) ->
                    updatedPet = when (stat) {
                        "happiness" -> updatedPet.copy(happiness = (value as Int).coerceIn(0, 100))
                        "health" -> updatedPet.copy(health = (value as Int).coerceIn(0, 100))
                        "hunger" -> updatedPet.copy(hunger = (value as Int).coerceIn(0, 100))
                        "energy" -> updatedPet.copy(energy = (value as Int).coerceIn(0, 100))
                        "cleanliness" -> updatedPet.copy(cleanliness = (value as Int).coerceIn(0, 100))
                        "weight" -> updatedPet.copy(weight = value as Double)
                        "age" -> updatedPet.copy(age = value as Int)
                        else -> updatedPet
                    }
                }
                pets[petId] = updatedPet
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addVaccineRecord(petId: String, record: VaccineRecord): Result<Boolean> {
        return try {
            pets[petId]?.let { pet ->
                pet.vaccineHistory.add(record)
                Result.success(true)
            } ?: Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
