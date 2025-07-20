package com.example.tailstale.repo

import com.example.tailstale.model.PetType
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.ToyModel

class ToyRepositoryImpl : ToyRepository {
    private val toys = mutableListOf<ToyModel>()

    init {
        initializeToyData()
    }

    override suspend fun getAllToys(): Result<List<ToyModel>> {
        return try {
            Result.success(toys.toList())
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

    override suspend fun getToysByPetType(petType: PetType): Result<List<ToyModel>> {
        return try {
            Result.success(toys.filter { it.compatiblePetTypes.contains(petType) })
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

    override suspend fun searchToys(query: String): Result<List<ToyModel>> {
        return try {
            val searchResults = toys.filter { toy ->
                toy.name.contains(query, ignoreCase = true) ||
                        toy.description.contains(query, ignoreCase = true)
            }
            Result.success(searchResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getToysByCostRange(minCost: Int, maxCost: Int): Result<List<ToyModel>> {
        return try {
            Result.success(toys.filter { it.cost in minCost..maxCost })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeToyData() {
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
            ),
            ToyModel(
                name = "Puzzle Ball",
                description = "Mental stimulation puzzle for smart pets",
                category = ToyCategory.PUZZLE,
                funFactor = 80,
                energyConsumption = 20,
                durability = 90,
                cost = 25,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                skillsImproved = listOf("Problem Solving", "Mental Stimulation")
            )
        ))
    }
}