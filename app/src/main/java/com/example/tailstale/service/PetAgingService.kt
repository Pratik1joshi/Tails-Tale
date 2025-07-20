package com.example.tailstale.service

import android.util.Log
import com.example.tailstale.model.PetModel
import com.example.tailstale.repo.PetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetAgingService(
    private val petRepository: PetRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var agingJob: Job? = null

    companion object {
        private const val TAG = "PetAgingService"
        private const val UPDATE_INTERVAL = 60 * 1000L // Check every minute
    }

    /**
     * Start the real-time aging system
     */
    fun startRealTimeAging(userId: String) {
        Log.d(TAG, "Starting real-time aging for user: $userId")

        agingJob?.cancel() // Cancel any existing job

        agingJob = scope.launch {
            while (true) {
                try {
                    updateAllPetsForUser(userId)
                    delay(UPDATE_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in aging loop: ${e.message}", e)
                    delay(UPDATE_INTERVAL) // Continue even after errors
                }
            }
        }
    }

    /**
     * Stop the real-time aging system
     */
    fun stopRealTimeAging() {
        Log.d(TAG, "Stopping real-time aging")
        agingJob?.cancel()
        agingJob = null
    }

    /**
     * Update all pets for a specific user
     */
    private suspend fun updateAllPetsForUser(userId: String) {
        try {
            petRepository.getPetsByUserId(userId).fold(
                onSuccess = { pets ->
                    pets.forEach { pet ->
                        val updatedPet = processPetAging(pet)
                        if (updatedPet != pet) { // Only update if changes occurred
                            petRepository.updatePet(updatedPet)
                            Log.d(TAG, "Updated pet ${pet.name}: Age ${pet.age} -> ${updatedPet.age}")
                        }
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to get pets for user $userId: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pets for user $userId", e)
        }
    }

    /**
     * Process aging and stats decay for a single pet
     */
    fun processPetAging(pet: PetModel): PetModel {
        var updatedPet = pet
        val currentTime = System.currentTimeMillis()

        // NEW: First, apply any background decay that happened while app was closed
        val backgroundDecay = pet.calculateBackgroundDecay()
        if (backgroundDecay.isNotEmpty()) {
            updatedPet = updatedPet.copy(
                hunger = (backgroundDecay["hunger"] as? Int) ?: pet.hunger,
                energy = (backgroundDecay["energy"] as? Int) ?: pet.energy,
                cleanliness = (backgroundDecay["cleanliness"] as? Int) ?: pet.cleanliness,
                happiness = (backgroundDecay["happiness"] as? Int) ?: pet.happiness,
                lastStatsDecay = backgroundDecay["lastStatsDecay"] as? Long ?: pet.lastStatsDecay
            )
            Log.d(TAG, "Applied background decay to ${pet.name}: H:${updatedPet.hunger} E:${updatedPet.energy} C:${updatedPet.cleanliness} Ha:${updatedPet.happiness}")
        }

        // NEW: Apply background aging that happened while app was closed
        val backgroundAging = pet.calculateBackgroundAging()
        if (backgroundAging.isNotEmpty()) {
            updatedPet = updatedPet.copy(
                age = (backgroundAging["age"] as? Int) ?: pet.age,
                weight = (backgroundAging["weight"] as? Double) ?: pet.weight,
                lastAgeUpdate = backgroundAging["lastAgeUpdate"] as? Long ?: pet.lastAgeUpdate
            )
            Log.d(TAG, "Applied background aging to ${pet.name}: Age ${pet.age} -> ${updatedPet.age}, Weight: ${updatedPet.weight}kg")
        }

        // Process normal aging (for any additional time since background calculation)
        if (updatedPet.shouldAgeUp()) {
            val ageIncrease = updatedPet.calculateAgeIncrease()
            updatedPet = updatedPet.copy(
                age = updatedPet.age + ageIncrease,
                lastAgeUpdate = currentTime,
                weight = calculateNewWeight(updatedPet, ageIncrease)
            )
            Log.d(TAG, "Pet ${pet.name} aged by $ageIncrease months (now ${updatedPet.age} months)")
        }

        // Process normal stats decay (for any additional time since background calculation)
        if (updatedPet.shouldDecayStats()) {
            val statsDecay = updatedPet.calculateStatsDecay()
            if (statsDecay.isNotEmpty()) {
                updatedPet = updatedPet.copy(
                    hunger = statsDecay["hunger"] ?: updatedPet.hunger,
                    energy = statsDecay["energy"] ?: updatedPet.energy,
                    cleanliness = statsDecay["cleanliness"] ?: updatedPet.cleanliness,
                    happiness = statsDecay["happiness"] ?: updatedPet.happiness,
                    lastStatsDecay = currentTime
                )
                Log.d(TAG, "Pet ${pet.name} normal stats decay: H:${updatedPet.hunger} E:${updatedPet.energy} C:${updatedPet.cleanliness} Ha:${updatedPet.happiness}")
            }
        }

        // Additional aging effects
        updatedPet = applyAgingEffects(updatedPet)

        return updatedPet
    }

    /**
     * Calculate new weight based on age and growth stage
     */
    private fun calculateNewWeight(pet: PetModel, ageIncrease: Int): Double {
        val baseWeight = when (pet.type.uppercase()) {
            "DOG" -> 0.5 // Start at 0.5kg for puppy
            "CAT" -> 0.3 // Start at 0.3kg for kitten
            "RABBIT" -> 0.2
            "BIRD" -> 0.05
            "HAMSTER" -> 0.02
            else -> 0.5
        }

        val maxWeight = when (pet.type.uppercase()) {
            "DOG" -> 25.0 // Max adult dog weight
            "CAT" -> 5.0  // Max adult cat weight
            "RABBIT" -> 2.5
            "BIRD" -> 0.5
            "HAMSTER" -> 0.15
            else -> 25.0
        }

        // Weight grows with age but plateaus at adult stage
        val ageBasedWeight = when (pet.age + ageIncrease) {
            in 1..6 -> baseWeight + (pet.age * 0.1) // Baby growth
            in 7..24 -> baseWeight + (6 * 0.1) + ((pet.age - 6) * 0.05) // Young growth
            else -> maxWeight // Adult weight
        }

        return minOf(maxWeight, ageBasedWeight)
    }

    /**
     * Apply additional effects based on pet's age and life stage
     */
    private fun applyAgingEffects(pet: PetModel): PetModel {
        var updatedPet = pet

        // Senior pets have slightly reduced max health and energy
        if (pet.growthStage == com.example.tailstale.model.GrowthStage.SENIOR) {
            updatedPet = updatedPet.copy(
                health = minOf(pet.health, 90), // Senior pets max 90% health
                energy = if (pet.energy > 80) 80 else pet.energy // Senior pets get tired easier
            )
        }

        // Baby pets get hungry faster
        if (pet.growthStage == com.example.tailstale.model.GrowthStage.BABY) {
            if (pet.getTimeSinceLastFed() > 2) { // Haven't been fed in 2+ hours
                updatedPet = updatedPet.copy(
                    hunger = minOf(100, pet.hunger + 10),
                    happiness = maxOf(0, pet.happiness - 5)
                )
            }
        }

        return updatedPet
    }

    /**
     * Force update a specific pet (useful for manual refresh)
     */
    suspend fun forceUpdatePet(petId: String): Result<PetModel> {
        return try {
            petRepository.getPetById(petId).fold(
                onSuccess = { pet ->
                    if (pet != null) {
                        val updatedPet = processPetAging(pet)
                        petRepository.updatePet(updatedPet).fold(
                            onSuccess = { Result.success(updatedPet) },
                            onFailure = { Result.failure(it) }
                        )
                    } else {
                        Result.failure(Exception("Pet not found"))
                    }
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get aging statistics for a pet
     */
    fun getPetAgingStats(pet: PetModel): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val ageInRealDays = (currentTime - pet.creationDate) / (24 * 60 * 60 * 1000)
        val timeSinceLastAgeUpdate = (currentTime - pet.lastAgeUpdate) / (60 * 60 * 1000) // in hours
        val timeSinceLastStatsDecay = (currentTime - pet.lastStatsDecay) / (60 * 1000) // in minutes

        return mapOf(
            "ageInMonths" to pet.age,
            "ageInYears" to pet.ageInYears,
            "ageInRealDays" to ageInRealDays,
            "growthStage" to pet.growthStage.name,
            "weight" to pet.weight,
            "timeSinceLastAgeUpdate" to timeSinceLastAgeUpdate,
            "timeSinceLastStatsDecay" to timeSinceLastStatsDecay,
            "nextAgeUpIn" to (60 - timeSinceLastAgeUpdate), // Hours until next age up
            "nextStatsDecayIn" to (30 - timeSinceLastStatsDecay), // Minutes until next stats decay
            "overallHealthScore" to pet.getOverallHealthScore(),
            "lifeStageDescription" to pet.getLifeStageDescription()
        )
    }
}
