package com.example.tailstale.model

import java.util.UUID

data class VaccineModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val targetDisease: String,
    val effectiveDurationDays: Int,
    val cost: Int,
    val compatiblePetTypes: List<PetType>
)