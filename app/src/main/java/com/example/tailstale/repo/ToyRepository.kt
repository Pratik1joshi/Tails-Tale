package com.example.tailstale.repo

import com.example.tailstale.model.PetType
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.ToyModel

interface ToyRepository {
    suspend fun getAllToys(): Result<List<ToyModel>>
    suspend fun getToysByCategory(category: ToyCategory): Result<List<ToyModel>>
    suspend fun getToysByPetType(petType: PetType): Result<List<ToyModel>>
    suspend fun getToyById(toyId: String): Result<ToyModel?>
    suspend fun searchToys(query: String): Result<List<ToyModel>>
    suspend fun getToysByCostRange(minCost: Int, maxCost: Int): Result<List<ToyModel>>
}