package com.example.tailstale.repo

import com.example.tailstale.model.CareAction
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineRecord
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await

class PetRepositoryImpl : PetRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("pets")
    private val userPetsDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("userPets")

    override suspend fun createPet(pet: PetModel): Result<PetModel> {
        return try {
            // Save pet to pets collection
            database.child(pet.id).setValue(pet).await()
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPetById(petId: String): Result<PetModel?> {
        return try {
            val snapshot = database.child(petId).get().await()
            val pet = snapshot.getValue<PetModel>()
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPetsByUserId(userId: String): Result<List<PetModel>> {
        return try {
            // Get user's pet IDs
            val userPetsSnapshot = userPetsDatabase.child(userId).get().await()
            val petIds = userPetsSnapshot.children.mapNotNull { it.key }

            // Get all pets for this user
            val pets = mutableListOf<PetModel>()
            for (petId in petIds) {
                val petSnapshot = database.child(petId).get().await()
                petSnapshot.getValue<PetModel>()?.let { pets.add(it) }
            }

            Result.success(pets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePet(pet: PetModel): Result<PetModel> {
        return try {
            database.child(pet.id).setValue(pet).await()
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePet(petId: String): Result<Boolean> {
        return try {
            database.child(petId).removeValue().await()
            // Remove from all user's pet lists
            userPetsDatabase.get().await().children.forEach { userSnapshot ->
                userSnapshot.child(petId).ref.removeValue()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCareAction(petId: String, action: CareAction): Result<Boolean> {
        return try {
            val careActionRef = database.child(petId).child("careLog").push()
            careActionRef.setValue(action).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePetStats(petId: String, statsUpdate: Map<String, Any>): Result<Boolean> {
        return try {
            database.child(petId).updateChildren(statsUpdate).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addVaccineRecord(petId: String, record: VaccineRecord): Result<Boolean> {
        return try {
            val vaccineRef = database.child(petId).child("vaccineHistory").push()
            vaccineRef.setValue(record).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper method to link pet to user
    suspend fun linkPetToUser(userId: String, petId: String): Result<Boolean> {
        return try {
            userPetsDatabase.child(userId).child(petId).setValue(true).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}