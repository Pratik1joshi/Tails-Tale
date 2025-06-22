package com.example.tailstale.repo

import com.example.tailstale.model.FoodModel

interface FoodRepository {
    suspend fun getFoodById(foodId: String): FoodModel?
    suspend fun updateFood(food: FoodModel)
    suspend fun createFood(food: FoodModel)
    suspend fun getAllFood(): List<FoodModel>
}