package com.example.tailstale.repo

import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.FoodModel
import com.example.tailstale.model.ItemCategory
import com.example.tailstale.model.PetType
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.ToyModel
import com.example.tailstale.model.VaccineModel

class ItemRepositoryImpl : ItemRepository {
    private val foods = mutableListOf<FoodModel>()
    private val toys = mutableListOf<ToyModel>()
    private val vaccines = mutableListOf<VaccineModel>()

    init {
        // Initialize with some sample data
        initializeSampleData()
    }

    override suspend fun getAllFood(): Result<List<FoodModel>> {
        return try {
            Result.success(foods.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllToys(): Result<List<ToyModel>> {
        return try {
            Result.success(toys.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllVaccines(): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.toList())
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

    override suspend fun getToysByCategory(category: ToyCategory): Result<List<ToyModel>> {
        return try {
            Result.success(toys.filter { it.category == category })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItemsByPetType(petType: PetType): Result<Map<ItemCategory, List<Any>>> {
        return try {
            val result = mapOf(
                ItemCategory.FOOD to foods.filter { it.compatiblePetTypes.contains(petType) },
                ItemCategory.TOY to toys.filter { it.compatiblePetTypes.contains(petType) },
                ItemCategory.VACCINE to vaccines.filter { it.compatiblePetTypes.contains(petType) }
            )
            Result.success(result)
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

    override suspend fun getToyById(toyId: String): Result<ToyModel?> {
        return try {
            Result.success(toys.find { it.id == toyId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccineById(vaccineId: String): Result<VaccineModel?> {
        return try {
            Result.success(vaccines.find { it.id == vaccineId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeSampleData() {
        // Add sample foods
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
                ingredients = listOf("Chicken", "Rice", "Vegetables")
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
                ingredients = listOf("Tuna", "Salmon")
            )
        ))

        // Add sample toys
        toys.addAll(listOf(
            ToyModel(
                name = "Rope Toy",
                description = "Durable rope toy for dogs",
                category = ToyCategory.CHEW,
                funFactor = 70,
                energyConsumption = 15,
                durability = 85,
                cost = 15,
                compatiblePetTypes = setOf(PetType.DOG),
                skillsImproved = listOf("Dental Health", "Jaw Strength")
            ),
            ToyModel(
                name = "Feather Wand",
                description = "Interactive toy for cats",
                category = ToyCategory.INTERACTIVE,
                funFactor = 90,
                energyConsumption = 25,
                durability = 60,
                cost = 12,
                compatiblePetTypes = setOf(PetType.CAT),
                skillsImproved = listOf("Agility", "Hunting Instincts")
            )
        ))

        // Add sample vaccines
        vaccines.addAll(listOf(
            VaccineModel(
                name = "Rabies Vaccine",
                description = "Essential vaccine to prevent rabies",
                targetDisease = "Rabies",
                effectiveDurationDays = 365,
                cost = 50,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 12..Int.MAX_VALUE,
                sideEffects = listOf("Mild fatigue", "Slight fever")
            )
        ))
    }
}
