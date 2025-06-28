package com.example.tailstale.repo

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.FoodModel
import com.example.tailstale.model.PetType


class FoodRepositoryImpl : FoodRepository {
    private val foods = mutableListOf<FoodModel>()

    init {
        initializeFoodData()
    }

    override suspend fun getAllFood(): Result<List<FoodModel>> {
        return try {
            Result.success(foods.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodByCategory(category: FoodCategory): Result<List<FoodModel>> {
        return try {
            Result.success(foods.filter { it.category == category })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodByPetType(petType: PetType): Result<List<FoodModel>> {
        return try {
            Result.success(foods.filter { it.compatiblePetTypes.contains(petType) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodById(foodId: String): Result<FoodModel?> {
        return try {
            Result.success(foods.find { it.id == foodId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFood(query: String): Result<List<FoodModel>> {
        return try {
            val searchResults = foods.filter { food ->
                food.name.contains(query, ignoreCase = true) ||
                        food.description.contains(query, ignoreCase = true) ||
                        food.ingredients.any { it.contains(query, ignoreCase = true) }
            }
            Result.success(searchResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodByNutritionRange(minNutrition: Int, maxNutrition: Int): Result<List<FoodModel>> {
        return try {
            Result.success(foods.filter { it.nutritionValue in minNutrition..maxNutrition })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoodByCostRange(minCost: Int, maxCost: Int): Result<List<FoodModel>> {
        return try {
            Result.success(foods.filter { it.cost in minCost..maxCost })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeFoodData() {
        foods.addAll(listOf(
            FoodModel(
                name = "Premium Dog Food",
                description = "High-quality dry food for adult dogs",
                category = FoodCategory.DRY_FOOD,
                nutritionValue = 85,
                healthBoost = 10,
                happinessBoost = 5,
                cost = 25,
                compatiblePetTypes = setOf(PetType.DOG),
                ingredients = listOf("Chicken", "Rice", "Vegetables", "Vitamins")
            ),
            FoodModel(
                name = "Cat Treats",
                description = "Delicious treats that cats love",
                category = FoodCategory.TREATS,
                nutritionValue = 30,
                healthBoost = 5,
                happinessBoost = 15,
                cost = 10,
                compatiblePetTypes = setOf(PetType.CAT),
                ingredients = listOf("Tuna", "Salmon", "Catnip")
            ),
            FoodModel(
                name = "Puppy Formula",
                description = "Special nutrition for growing puppies",
                category = FoodCategory.WET_FOOD,
                nutritionValue = 90,
                healthBoost = 15,
                happinessBoost = 8,
                cost = 30,
                compatiblePetTypes = setOf(PetType.DOG),
                ingredients = listOf("Lamb", "Sweet Potato", "Calcium", "DHA")
            ),
            FoodModel(
                name = "Bird Seeds Mix",
                description = "Nutritious seed mix for birds",
                category = FoodCategory.DRY_FOOD,
                nutritionValue = 70,
                healthBoost = 8,
                happinessBoost = 12,
                cost = 15,
                compatiblePetTypes = setOf(PetType.BIRD),
                ingredients = listOf("Sunflower Seeds", "Millet", "Nyjer Seeds")
            )
        ))
    }
}
