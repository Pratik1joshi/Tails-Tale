package com.example.tailstale.model

import java.util.UUID

data class ToyModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: ToyCategory,
    val funFactor: Int,
    val energyConsumption: Int,
    val durability: Int,
    val cost: Int,
    val compatiblePetTypes: Set<PetType>,
    val skillsImproved: List<String> = emptyList()
)

enum class ToyCategory(val displayName: String) {
    INTERACTIVE("Interactive"),
    CHEW("Chew Toy"),
    PUZZLE("Puzzle"),
    EXERCISE("Exercise"),
    COMFORT("Comfort")
}
