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
    val treatmentSteps: List<String> = emptyList(),
    // NEW: Age-based vulnerability
    val vulnerableAgeRange: IntRange = 1..Int.MAX_VALUE, // Age in months when pet is vulnerable
    val riskFactorByAge: Map<IntRange, Double> = emptyMap(), // Risk probability by age ranges
    val preventableByVaccines: Set<String> = emptySet(), // Vaccine names that prevent this disease
    val requiredVaccineForPrevention: String? = null // Primary vaccine that prevents this
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
