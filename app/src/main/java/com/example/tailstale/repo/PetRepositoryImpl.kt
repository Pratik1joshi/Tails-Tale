package com.example.tailstale.repo

import com.example.tailstale.model.PetModel

class PetRepositoryImpl(private val db: FirebaseFirestore) : PetRepository {
    override suspend fun getPetById(userId: String, petId: String): PetModel? {
        val snapshot = db.collection("users").document(userId).collection("pets").document(petId).get().await()
        return snapshot.toObject(PetModel::class.java)
    }

    override suspend fun updatePet(userId: String, pet: PetModel) {
        db.collection("users").document(userId).collection("pets").document(pet.id).set(pet).await()
    }

    override suspend fun createPet(userId: String, pet: PetModel) {
        db.collection("users").document(userId).collection("pets").document(pet.id).set(pet).await()
    }

    override suspend fun getAllPets(userId: String): List<PetModel> {
        val snapshot = db.collection("users").document(userId).collection("pets").get().await()
        return snapshot.toObjects(PetModel::class.java)
    }
}