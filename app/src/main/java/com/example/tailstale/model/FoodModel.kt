package com.example.tailstale.model

import java.util.UUID

data class FoodModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: FoodCategory,
    val nutritionValue: Int,
    val healthBoost: Int,
    val happinessBoost: Int = 0,
    val cost: Int,
    val compatiblePetTypes: Set<PetType>,
    val imageResource: String = "",
    val ingredients: List<String> = emptyList()
)

enum class FoodCategory(val displayName: String) {
    DRY_FOOD("Dry Food"),
    WET_FOOD("Wet Food"),
    TREATS("Treats"),
    SUPPLEMENTS("Supplements"),
    RAW_FOOD("Raw Food")
}
