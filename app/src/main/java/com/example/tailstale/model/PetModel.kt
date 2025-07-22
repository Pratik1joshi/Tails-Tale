package com.example.tailstale.model

import java.util.UUID

enum class PetType { DOG, CAT, RABBIT, BIRD, HAMSTER }
enum class GrowthStage { BABY, YOUNG, ADULT, SENIOR }

data class PetModel(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var type: String = PetType.DOG.name, // Store as String instead of enum
    var age: Int = 1, // Age in months
    var ageInRealDays: Int = 0, // Track real days since creation for aging
    var happiness: Int = 100,
    var health: Int = 100,
    var hunger: Int = 0,
    var energy: Int = 100,
    var weight: Double = 1.0,
    var cleanliness: Int = 100,
    var lastFed: Long = System.currentTimeMillis(),
    var lastPlayed: Long = System.currentTimeMillis(),
    var lastCleaned: Long = System.currentTimeMillis(),
    var lastAgeUpdate: Long = System.currentTimeMillis(), // Track last age update
    var lastStatsDecay: Long = System.currentTimeMillis(), // Track when stats last decayed
    val vaccineHistory: Map<String, Any> = emptyMap(), // Change to Map for Firebase compatibility
    val diseaseHistory: Map<String, Any> = emptyMap(), // Change to Map for Firebase compatibility
    val careLog: Map<String, Any> = emptyMap(), // Change to Map for Firebase compatibility
    var creationDate: Long = System.currentTimeMillis()
) {
    val growthStage: GrowthStage
        get() = when (age) {
            in 1..6 -> GrowthStage.BABY    // 1-6 months = puppy
            in 7..24 -> GrowthStage.YOUNG  // 7-24 months = young dog
            in 25..84 -> GrowthStage.ADULT // 25-84 months = adult dog
            else -> GrowthStage.SENIOR     // 85+ months = senior dog
        }

    val needsAttention: Boolean
        get() = hunger > 80 || energy < 20 || happiness < 30 || health < 50 || cleanliness < 30

    val ageInYears: Double
        get() = age / 12.0

    val isVaccineOverdue: Boolean
        get() {
            // Check if core vaccines are overdue based on age
            return when (age) {
                in 6..8 -> !hasVaccine("DHPP_first")
                in 10..12 -> !hasVaccine("DHPP_second")
                in 14..16 -> !hasVaccine("DHPP_third") || !hasVaccine("Rabies_first")
                12 -> !hasVaccine("DHPP_yearly") // 1 year booster
                else -> false
            }
        }

    // Helper to check if pet has received a specific vaccine
    private fun hasVaccine(vaccineType: String): Boolean {
        // Since vaccineHistory is now a Map<String, Any>, we need to check if the vaccine type exists as a key
        // or if it's stored in a different way. For now, let's check if the vaccineType exists as a key or value
        return vaccineHistory.containsKey(vaccineType) ||
               vaccineHistory.values.any { it.toString().contains(vaccineType, ignoreCase = true) }
    }

    // Real-time aging: 1 real hour = 1 pet month
    fun shouldAgeUp(): Boolean {
        val hoursSinceLastUpdate = (System.currentTimeMillis() - lastAgeUpdate) / (60 * 60 * 1000)
        return hoursSinceLastUpdate >= 1 // Age up every real hour
    }

    // Calculate how many months the pet should age based on time passed
    fun calculateAgeIncrease(): Int {
        val hoursSinceLastUpdate = (System.currentTimeMillis() - lastAgeUpdate) / (60 * 60 * 1000)
        return hoursSinceLastUpdate.toInt()
    }

    // Stats decay over time - pets get hungrier, less clean, etc.
    fun shouldDecayStats(): Boolean {
        val minutesSinceLastDecay = (System.currentTimeMillis() - lastStatsDecay) / (60 * 1000)
        return minutesSinceLastDecay >= 30 // Decay stats every 30 minutes
    }

    // Calculate stats decay based on time passed - ENHANCED for background decay
    fun calculateStatsDecay(): Map<String, Int> {
        val minutesSinceLastDecay = (System.currentTimeMillis() - lastStatsDecay) / (60 * 1000)
        val decayIntervals = (minutesSinceLastDecay / 30).toInt() // Every 30 minutes

        return if (decayIntervals > 0) {
            mapOf(
                "hunger" to minOf(100, hunger + (decayIntervals * 5)), // Gets hungrier
                "energy" to maxOf(0, energy - (decayIntervals * 3)), // Gets tired
                "cleanliness" to maxOf(0, cleanliness - (decayIntervals * 2)), // Gets dirty
                "happiness" to maxOf(0, happiness - (decayIntervals * 1)) // Slightly less happy
            )
        } else {
            emptyMap()
        }
    }

    // NEW: Calculate background decay - what should have happened while app was closed
    fun calculateBackgroundDecay(): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val minutesSinceLastDecay = (currentTime - lastStatsDecay) / (60 * 1000)

        // Only apply background decay if more than 30 minutes have passed
        if (minutesSinceLastDecay < 30) {
            return emptyMap()
        }

        val decayIntervals = (minutesSinceLastDecay / 30).toInt() // Every 30 minutes

        // More aggressive decay for background (pet was neglected while app was closed)
        val hungerIncrease = minOf(100 - hunger, decayIntervals * 6) // Faster hunger
        val energyDecrease = minOf(energy, decayIntervals * 4) // Faster energy loss
        val cleanlinessDecrease = minOf(cleanliness, decayIntervals * 3) // Gets dirtier
        val happinessDecrease = minOf(happiness, decayIntervals * 2) // Gets sadder

        return mapOf(
            "hunger" to hunger + hungerIncrease,
            "energy" to energy - energyDecrease,
            "cleanliness" to cleanliness - cleanlinessDecrease,
            "happiness" to happiness - happinessDecrease,
            "lastStatsDecay" to currentTime
        )
    }

    // NEW: Calculate background aging - aging that happened while app was closed
    fun calculateBackgroundAging(): Map<String, Any> {
        val currentTime = System.currentTimeMillis()
        val hoursSinceLastUpdate = (currentTime - lastAgeUpdate) / (60 * 60 * 1000)

        if (hoursSinceLastUpdate < 1) {
            return emptyMap()
        }

        val ageIncrease = hoursSinceLastUpdate.toInt()
        val newAge = age + ageIncrease
        val newWeight = calculateNewBackgroundWeight(newAge)

        return mapOf(
            "age" to newAge,
            "weight" to newWeight,
            "lastAgeUpdate" to currentTime
        )
    }

    // Helper to calculate weight after background aging
    private fun calculateNewBackgroundWeight(newAge: Int): Double {
        val baseWeight = when (type.uppercase()) {
            "DOG" -> 0.5
            "CAT" -> 0.3
            "RABBIT" -> 0.2
            "BIRD" -> 0.05
            "HAMSTER" -> 0.02
            else -> 0.5
        }

        val maxWeight = when (type.uppercase()) {
            "DOG" -> 25.0
            "CAT" -> 5.0
            "RABBIT" -> 2.5
            "BIRD" -> 0.5
            "HAMSTER" -> 0.15
            else -> 25.0
        }

        return when (newAge) {
            in 1..6 -> baseWeight + (newAge * 0.1) // Baby growth
            in 7..24 -> baseWeight + (6 * 0.1) + ((newAge - 6) * 0.05) // Young growth
            else -> maxWeight // Adult weight
        }
    }

    // Get time since last care action in hours
    fun getTimeSinceLastFed(): Long {
        return (System.currentTimeMillis() - lastFed) / (60 * 60 * 1000)
    }

    fun getTimeSinceLastPlayed(): Long {
        return (System.currentTimeMillis() - lastPlayed) / (60 * 60 * 1000)
    }

    fun getTimeSinceLastCleaned(): Long {
        return (System.currentTimeMillis() - lastCleaned) / (60 * 60 * 1000)
    }

    // Get pet's current life stage description
    fun getLifeStageDescription(): String {
        return when (growthStage) {
            GrowthStage.BABY -> "Playful and curious baby ${type.lowercase()}"
            GrowthStage.YOUNG -> "Energetic young ${type.lowercase()}"
            GrowthStage.ADULT -> "Mature and wise ${type.lowercase()}"
            GrowthStage.SENIOR -> "Senior ${type.lowercase()} with lots of experience"
        }
    }

    // Calculate overall health score (0-100)
    fun getOverallHealthScore(): Int {
        val weights = mapOf(
            "health" to 0.4,
            "happiness" to 0.25,
            "hunger" to 0.15, // Lower hunger is better
            "energy" to 0.1,
            "cleanliness" to 0.1
        )

        val hungerScore = 100 - hunger // Invert hunger (less hunger = better score)

        return ((health * weights["health"]!!) +
                (happiness * weights["happiness"]!!) +
                (hungerScore * weights["hunger"]!!) +
                (energy * weights["energy"]!!) +
                (cleanliness * weights["cleanliness"]!!)).toInt()
    }
}