package com.example.tailstale.model

import java.util.UUID

data class DiseaseModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val severity: DiseaseSeverity,
    val category: DiseaseCategory,
    val healthImpact: Int,
    val happinessImpact: Int,
    val durationDays: IntRange,
    val treatmentCost: Int,
    val affectedPetTypes: Set<PetType>,
    val symptoms: List<String>,
    val preventionTips: List<String> = emptyList(),
    val treatmentSteps: List<String> = emptyList()
)

enum class DiseaseCategory(val displayName: String) {
    PARASITIC("Parasitic"),
    VIRAL("Viral"),
    BACTERIAL("Bacterial"),
    GENETIC("Genetic"),
    NUTRITIONAL("Nutritional"),
    BEHAVIORAL("Behavioral")
}
enum class DiseaseSeverity { MILD, MODERATE, SEVERE }

