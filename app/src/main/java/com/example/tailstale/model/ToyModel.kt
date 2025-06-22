package com.example.tailstale.model

import java.util.UUID

// ToyModel.kt
data class ToyModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: String,
    val funFactor: Int,
    val energyConsumption: Int,
    val durability: Int,
    val cost: Int,
    val compatiblePetTypes: List<PetType>
)