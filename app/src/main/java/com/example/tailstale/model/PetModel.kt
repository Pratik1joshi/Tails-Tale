package com.example.tailstale.model

import java.util.UUID

enum class PetType { DOG, CAT, RABBIT, BIRD, HAMSTER }
enum class GrowthStage { BABY, YOUNG, ADULT, SENIOR }


data class PetModel(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var type: String = PetType.DOG.name, // Store as String instead of enum
    var age: Int = 1, // Start at 1 month old
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

    // Calculate how many real days should equal 1 pet month
    // 1 real day = 1 pet month (so dog ages quickly but realistically)
    fun shouldAgeUp(): Boolean {
        val daysSinceLastUpdate = (System.currentTimeMillis() - lastAgeUpdate) / (24 * 60 * 60 * 1000)
        return daysSinceLastUpdate >= 1 // Age up every real day
    }
}