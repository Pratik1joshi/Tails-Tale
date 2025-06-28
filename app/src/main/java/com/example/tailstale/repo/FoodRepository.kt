package com.example.tailstale.repo

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.FoodModel
import com.example.tailstale.model.PetType

interface FoodRepository {
    suspend fun getAllFood(): Result<List<FoodModel>>
    suspend fun getFoodByCategory(category: FoodCategory): Result<List<FoodModel>>
    suspend fun getFoodByPetType(petType: PetType): Result<List<FoodModel>>
    suspend fun getFoodById(foodId: String): Result<FoodModel?>
    suspend fun searchFood(query: String): Result<List<FoodModel>>
    suspend fun getFoodByNutritionRange(minNutrition: Int, maxNutrition: Int): Result<List<FoodModel>>
    suspend fun getFoodByCostRange(minCost: Int, maxCost: Int): Result<List<FoodModel>>
}