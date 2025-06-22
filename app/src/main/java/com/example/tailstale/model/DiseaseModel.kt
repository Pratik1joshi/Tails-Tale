package com.example.tailstale.model

import java.util.UUID

data class DiseaseModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val severity: String,
    val healthImpact: Int,
    val happinessImpact: Int,
    val durationDays: Int,
    val treatmentCost: Int,
    val affectedPetTypes: List<PetType>,
    val symptoms: List<String>
)