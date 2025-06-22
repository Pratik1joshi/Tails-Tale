package com.example.tailstale.model

import java.util.UUID

data class FoodModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: String,
    val nutritionValue: Int,
    val healthBoost: Int,
    val cost: Int,
    val compatiblePetTypes: List<PetType>,
    val imageResource: String = ""
)