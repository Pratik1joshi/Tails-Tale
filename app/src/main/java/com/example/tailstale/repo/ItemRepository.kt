package com.example.tailstale.repo

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.FoodModel
import com.example.tailstale.model.ItemCategory
import com.example.tailstale.model.PetType
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.ToyModel
import com.example.tailstale.model.VaccineModel

interface ItemRepository {
    suspend fun getAllFood(): Result<List<FoodModel>>
    suspend fun getAllToys(): Result<List<ToyModel>>
    suspend fun getAllVaccines(): Result<List<VaccineModel>>
    suspend fun getFoodByCategory(category: FoodCategory): Result<List<FoodModel>>
    suspend fun getToysByCategory(category: ToyCategory): Result<List<ToyModel>>
    suspend fun getItemsByPetType(petType: PetType): Result<Map<ItemCategory, List<Any>>>
    suspend fun getFoodById(foodId: String): Result<FoodModel?>
    suspend fun getToyById(toyId: String): Result<ToyModel?>
    suspend fun getVaccineById(vaccineId: String): Result<VaccineModel?>
}
