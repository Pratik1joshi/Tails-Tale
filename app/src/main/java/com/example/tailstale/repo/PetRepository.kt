package com.example.tailstale.repo

import com.example.tailstale.model.PetModel

interface PetRepository {
    suspend fun getPetById(userId: String, petId: String): PetModel?
    suspend fun updatePet(userId: String, pet: PetModel)
    suspend fun createPet(userId: String, pet: PetModel)
    suspend fun getAllPets(userId: String): List<PetModel>
}