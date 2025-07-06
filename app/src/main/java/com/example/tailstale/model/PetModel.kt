package com.example.tailstale.model

import java.util.UUID

enum class PetType { DOG, CAT, RABBIT, BIRD, HAMSTER }
enum class GrowthStage { BABY, YOUNG, ADULT, SENIOR }


data class PetModel(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var type: String = PetType.DOG.name, // Store as String instead of enum
    var age: Int = 0,
    var happiness: Int = 100,
    var health: Int = 100,
    var hunger: Int = 0,
    var energy: Int = 100,
    var weight: Double = 1.0,
    var cleanliness: Int = 100,
    var lastFed: Long = System.currentTimeMillis(),
    var lastPlayed: Long = System.currentTimeMillis(),
    var lastCleaned: Long = System.currentTimeMillis(),
    val vaccineHistory: List<Map<String, Any>> = emptyList(), // Simple list of maps
    val careLog: List<Map<String, Any>> = emptyList(), // Simple list of maps
    var creationDate: Long = System.currentTimeMillis()
) {
    val growthStage: GrowthStage
        get() = when (age) {
            in 0..6 -> GrowthStage.BABY
            in 7..24 -> GrowthStage.YOUNG
            in 25..84 -> GrowthStage.ADULT
            else -> GrowthStage.SENIOR
        }

    val needsAttention: Boolean
        get() = hunger > 80 || energy < 20 || happiness < 30 || health < 50 || cleanliness < 30
}