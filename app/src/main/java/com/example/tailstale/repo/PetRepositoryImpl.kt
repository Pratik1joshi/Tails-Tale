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
            println("DEBUG: Starting getPetsByUserId for user: $userId")

            // First, try to get pets from userPets collection
            val userPetsSnapshot = userPetsDatabase.child(userId).get().await()
            println("DEBUG: Got userPets snapshot, children count: ${userPetsSnapshot.childrenCount}")

            val petIds = userPetsSnapshot.children.mapNotNull { it.key }
            println("DEBUG: Pet IDs found: $petIds")

            val pets = mutableListOf<PetModel>()

            if (petIds.isNotEmpty()) {
                // Get pets by their IDs from userPets collection
                for (petId in petIds) {
                    val petSnapshot = database.child(petId).get().await()
                    petSnapshot.getValue<PetModel>()?.let {
                        pets.add(it)
                        println("DEBUG: Added pet: ${it.name}")
                    }
                }
            } else {
                println("DEBUG: No pet IDs found in userPets, checking all pets...")
                // Fallback: Search all pets and find ones that belong to this user
                val allPetsSnapshot = database.get().await()
                println("DEBUG: All pets snapshot children count: ${allPetsSnapshot.childrenCount}")

                for (petSnapshot in allPetsSnapshot.children) {
                    val pet = petSnapshot.getValue<PetModel>()
                    pet?.let {
                        println("DEBUG: Found pet in all pets: ${it.name}")
                        pets.add(it)
                    }
                }

                // If we found pets this way, link them to the user for future queries
                if (pets.isNotEmpty()) {
                    for (pet in pets) {
                        linkPetToUser(userId, pet.id)
                        println("DEBUG: Linked pet ${pet.name} to user $userId")
                    }
                }
            }

            println("DEBUG: Final pets count: ${pets.size}")
            Result.success(pets)
        } catch (e: Exception) {
            println("DEBUG: Error in getPetsByUserId: ${e.message}")
            println("DEBUG: Error stack trace: ${e.stackTrace.joinToString("\n")}")
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