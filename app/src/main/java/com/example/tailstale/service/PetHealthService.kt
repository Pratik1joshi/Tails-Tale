package com.example.tailstale.service

import com.example.tailstale.model.DiseaseModel
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineModel
import com.example.tailstale.repo.DiseaseRepository
import com.example.tailstale.repo.VaccineRepository
import kotlin.random.Random

class PetHealthService(
    private val diseaseRepository: DiseaseRepository,
    private val vaccineRepository: VaccineRepository
) {

    /**
     * Ages up the pet by checking if enough real-world time has passed
     * 1 real day = 1 pet week (accelerated aging)
     */
    fun ageUpPet(pet: PetModel): PetModel {
        if (!pet.shouldAgeUp()) return pet

        val daysSinceLastUpdate = (System.currentTimeMillis() - pet.lastAgeUpdate) / (24 * 60 * 60 * 1000)
        val weeksToAdd = daysSinceLastUpdate.toInt()

        // Convert weeks to months (approximately 4 weeks = 1 month)
        val monthsToAdd = weeksToAdd / 4

        return if (monthsToAdd > 0) {
            pet.copy(
                age = pet.age + monthsToAdd,
                ageInRealDays = pet.ageInRealDays + daysSinceLastUpdate.toInt(),
                lastAgeUpdate = System.currentTimeMillis()
            )
        } else {
            pet.copy(
                ageInRealDays = pet.ageInRealDays + daysSinceLastUpdate.toInt(),
                lastAgeUpdate = System.currentTimeMillis()
            )
        }
    }

    /**
     * Checks if pet needs vaccines based on current age
     */
    suspend fun getRequiredVaccines(pet: PetModel): List<VaccineModel> {
        val petType = PetType.valueOf(pet.type)
        return vaccineRepository.getRequiredVaccines(petType, pet.age).getOrElse { emptyList() }
            .filter { vaccine ->
                // Don't suggest vaccines already received
                !pet.hasReceivedVaccine(vaccine.name)
            }
    }

    /**
     * Checks if pet needs overdue vaccines
     */
    suspend fun getOverdueVaccines(pet: PetModel): List<VaccineModel> {
        val petType = PetType.valueOf(pet.type)
        val requiredVaccines = vaccineRepository.getRequiredVaccines(petType, pet.age).getOrElse { emptyList() }

        return requiredVaccines.filter { vaccine ->
            when {
                // Core vaccines that are overdue
                vaccine.name.contains("DHPP First") && pet.age >= 8 && !pet.hasReceivedVaccine(vaccine.name) -> true
                vaccine.name.contains("DHPP Second") && pet.age >= 12 && !pet.hasReceivedVaccine(vaccine.name) -> true
                vaccine.name.contains("DHPP Final") && pet.age >= 16 && !pet.hasReceivedVaccine(vaccine.name) -> true
                vaccine.name.contains("Rabies First") && pet.age >= 16 && !pet.hasReceivedVaccine(vaccine.name) -> true
                vaccine.name.contains("DHPP Yearly") && pet.age >= 12 && shouldGetYearlyBooster(pet, "DHPP") -> true
                vaccine.name.contains("Rabies Annual") && pet.age >= 12 && shouldGetYearlyBooster(pet, "Rabies") -> true
                else -> false
            }
        }
    }

    /**
     * Calculates disease risk based on age and vaccination status
     */
    suspend fun calculateDiseaseRisk(pet: PetModel): List<DiseaseRiskAssessment> {
        val petType = PetType.valueOf(pet.type)
        val allDiseases = diseaseRepository.getDiseasesByPetType(petType).getOrElse { emptyList() }

        return allDiseases.map { disease ->
            val baseRisk = calculateBaseRiskForAge(pet.age, disease)
            val vaccinationModifier = calculateVaccinationProtection(pet, disease)
            val finalRisk = (baseRisk * vaccinationModifier).coerceIn(0.0, 1.0)

            DiseaseRiskAssessment(
                disease = disease,
                riskPercentage = (finalRisk * 100).toInt(),
                isHighRisk = finalRisk > 0.3,
                riskFactors = getRiskFactors(pet, disease),
                preventionTips = disease.preventionTips
            )
        }.sortedByDescending { it.riskPercentage }
    }

    /**
     * NEW: Get age-based disease warnings for the pet
     */
    suspend fun getAgeBasedDiseaseWarnings(pet: PetModel): List<DiseaseWarning> {
        val petType = PetType.valueOf(pet.type)
        val warnings = mutableListOf<DiseaseWarning>()

        // Get diseases the pet is vulnerable to at current age
        val vulnerableDiseases = diseaseRepository.getVulnerableDiseasesForAge(petType, pet.age)
            .getOrElse { emptyList() }

        for (disease in vulnerableDiseases) {
            // Check if pet is protected by vaccination
            val isProtected = diseaseRepository.isPetProtectedFromDisease(disease.id, pet.vaccineHistory)
                .getOrElse { false }

            if (!isProtected) {
                // Calculate risk level based on age
                val riskLevel = calculateRiskLevel(disease, pet.age)
                val warningLevel = when {
                    riskLevel >= 0.20 -> WarningLevel.HIGH
                    riskLevel >= 0.10 -> WarningLevel.MEDIUM
                    riskLevel >= 0.05 -> WarningLevel.LOW
                    else -> WarningLevel.INFO
                }

                warnings.add(
                    DiseaseWarning(
                        disease = disease,
                        warningLevel = warningLevel,
                        riskPercentage = (riskLevel * 100).toInt(),
                        recommendedAction = getRecommendedAction(disease),
                        urgency = getUrgency(disease, pet.age)
                    )
                )
            }
        }

        return warnings.sortedByDescending { it.warningLevel.priority }
    }

    /**
     * NEW: Get vaccination recommendations based on upcoming disease risks
     */
    suspend fun getVaccinationRecommendations(pet: PetModel): List<VaccinationRecommendation> {
        val petType = PetType.valueOf(pet.type)
        val recommendations = mutableListOf<VaccinationRecommendation>()

        // Get preventable diseases for current age
        val preventableDiseases = diseaseRepository.getPreventableDiseasesForAge(petType, pet.age)
            .getOrElse { emptyList() }

        // Get upcoming vaccines
        val upcomingVaccines = vaccineRepository.getRequiredVaccines(petType, pet.age)
            .getOrElse { emptyList() }
            .filter { vaccine ->
                !pet.hasReceivedVaccine(vaccine.name)
            }

        for (vaccine in upcomingVaccines) {
            val protectedDiseases = preventableDiseases.filter { disease ->
                disease.preventableByVaccines.contains(vaccine.name)
            }

            val urgency = if (protectedDiseases.any { it.severity == com.example.tailstale.model.DiseaseSeverity.SEVERE }) {
                UrgencyLevel.HIGH
            } else {
                UrgencyLevel.MEDIUM
            }

            recommendations.add(
                VaccinationRecommendation(
                    vaccine = vaccine,
                    protectedDiseases = protectedDiseases,
                    urgency = urgency,
                    reason = "Prevents: ${protectedDiseases.joinToString(", ") { it.name }}"
                )
            )
        }

        return recommendations.sortedByDescending { it.urgency.priority }
    }

    /**
     * ENHANCED: Check for random disease with vaccination protection
     */
    suspend fun checkForRandomDiseaseWithProtection(pet: PetModel): DiseaseModel? {
        return try {
            val petType = PetType.valueOf(pet.type)

            // Get diseases that the pet is vulnerable to at current age
            val vulnerableDiseases = diseaseRepository.getVulnerableDiseasesForAge(petType, pet.age)
                .getOrElse { emptyList() }

            if (vulnerableDiseases.isEmpty()) {
                return null
            }

            // Calculate weighted random selection based on age-specific risk factors
            val diseaseWithRisks = vulnerableDiseases.mapNotNull { disease ->
                val riskLevel = calculateAgeSpecificRisk(disease, pet.age, pet)
                if (riskLevel > 0) {
                    disease to riskLevel
                } else {
                    null
                }
            }

            if (diseaseWithRisks.isEmpty()) {
                return null
            }

            // Weighted random selection - diseases with higher risk are more likely
            val totalRisk = diseaseWithRisks.sumOf { it.second }
            val randomValue = kotlin.random.Random.nextDouble() * totalRisk

            var cumulativeRisk = 0.0
            for ((disease, risk) in diseaseWithRisks) {
                cumulativeRisk += risk
                if (randomValue <= cumulativeRisk) {
                    return disease
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate age-specific risk for a disease considering vaccination status
     */
    private fun calculateAgeSpecificRisk(disease: DiseaseModel, petAge: Int, pet: PetModel): Double {
        // Get base risk for current age
        val baseRisk = disease.riskFactorByAge.entries.find { (ageRange, _) ->
            petAge in ageRange
        }?.value ?: 0.0

        if (baseRisk == 0.0) return 0.0

        // Check vaccination protection
        val isProtected = disease.preventableByVaccines.any { vaccineName ->
            pet.vaccineHistory.values.any { vaccineRecord ->
                when (vaccineRecord) {
                    is Map<*, *> -> {
                        vaccineRecord["vaccineName"]?.toString()?.contains(vaccineName, ignoreCase = true) ?: false
                    }
                    else -> vaccineRecord.toString().contains(vaccineName, ignoreCase = true)
                }
            }
        }

        // Reduce risk significantly if protected by vaccine
        val protectionMultiplier = if (isProtected) 0.1 else 1.0 // 90% protection from vaccines

        // Additional risk factors based on pet's current health status
        val healthMultiplier = when {
            pet.health < 30 -> 1.5 // Sick pets more vulnerable
            pet.health < 60 -> 1.2 // Moderately healthy pets slightly more vulnerable
            else -> 1.0
        }

        val stressMultiplier = when {
            pet.happiness < 30 -> 1.3 // Stressed pets more vulnerable
            pet.happiness < 60 -> 1.1
            else -> 1.0
        }

        val hygieneMultiplier = when {
            pet.cleanliness < 30 -> 1.4 // Dirty pets more vulnerable to disease
            pet.cleanliness < 60 -> 1.1
            else -> 1.0
        }

        return baseRisk * protectionMultiplier * healthMultiplier * stressMultiplier * hygieneMultiplier
    }

    /**
     * Get comprehensive disease warnings (wrapper method for getAgeBasedDiseaseWarnings)
     */
    suspend fun getDiseaseWarnings(pet: PetModel): List<DiseaseWarning> {
        return getAgeBasedDiseaseWarnings(pet)
    }

    // Helper methods
    private fun calculateRiskLevel(disease: DiseaseModel, petAge: Int): Double {
        return disease.riskFactorByAge.entries.find { (ageRange, _) ->
            petAge in ageRange
        }?.value ?: 0.0
    }

    private fun getRecommendedAction(disease: DiseaseModel): String {
        return when {
            disease.requiredVaccineForPrevention != null ->
                "Get ${disease.requiredVaccineForPrevention} vaccination"
            disease.preventableByVaccines.isNotEmpty() ->
                "Consider vaccination: ${disease.preventableByVaccines.first()}"
            else -> "Follow prevention tips: ${disease.preventionTips.joinToString(", ")}"
        }
    }

    private fun getUrgency(disease: DiseaseModel, petAge: Int): UrgencyLevel {
        val riskLevel = calculateRiskLevel(disease, petAge)

        return when {
            disease.severity == com.example.tailstale.model.DiseaseSeverity.SEVERE && riskLevel >= 0.15 -> UrgencyLevel.IMMEDIATE
            disease.severity == com.example.tailstale.model.DiseaseSeverity.SEVERE && riskLevel >= 0.05 -> UrgencyLevel.HIGH
            disease.severity == com.example.tailstale.model.DiseaseSeverity.MODERATE && riskLevel >= 0.10 -> UrgencyLevel.MEDIUM
            else -> UrgencyLevel.LOW
        }
    }

    private fun calculateBaseRiskForAge(age: Int, disease: DiseaseModel): Double {
        return when {
            // Puppy diseases (1-6 months)
            age in 1..6 && disease.name in listOf("Parvovirus", "Distemper", "Intestinal Worms") -> 0.8

            // Young dog diseases (6 months - 2 years)
            age in 6..24 && disease.name in listOf("Kennel Cough", "Intestinal Worms", "Allergies") -> 0.4

            // Adult dog diseases (2-7 years)
            age in 24..84 && disease.name in listOf("Hip Dysplasia", "Allergies", "Ear Infections", "Dental Disease") -> 0.3

            // Senior dog diseases (7+ years)
            age > 84 && disease.name in listOf("Arthritis", "Heart Disease", "Kidney Disease", "Diabetes") -> 0.6

            // Common year-round diseases
            disease.name in listOf("Fleas and Ticks", "Upset Stomach") -> 0.2

            else -> 0.1
        }
    }

    private fun calculateVaccinationProtection(pet: PetModel, disease: DiseaseModel): Double {
        return when (disease.name) {
            "Parvovirus" -> if (pet.hasReceivedVaccine("DHPP")) 0.05 else 1.0
            "Distemper" -> if (pet.hasReceivedVaccine("DHPP")) 0.03 else 1.0
            "Kennel Cough" -> if (pet.hasReceivedVaccine("Bordetella")) 0.1 else 1.0
            "Intestinal Worms" -> if (pet.hasReceivedVaccine("Deworming")) 0.2 else 1.0
            "Fleas and Ticks" -> if (pet.hasReceivedVaccine("Flea & Tick Prevention")) 0.1 else 1.0
            else -> 1.0 // No vaccine protection
        }
    }

    private fun getRiskFactors(pet: PetModel, disease: DiseaseModel): List<String> {
        val factors = mutableListOf<String>()

        when (disease.name) {
            "Parvovirus", "Distemper" -> {
                if (pet.age < 6) factors.add("Young age (vulnerable immune system)")
                if (!pet.hasReceivedVaccine("DHPP")) factors.add("Missing core vaccines")
            }
            "Hip Dysplasia" -> {
                if (pet.type == "DOG" && pet.age > 24) factors.add("Breed predisposition")
            }
            "Arthritis" -> {
                if (pet.age > 84) factors.add("Advanced age")
                if (pet.weight > 30) factors.add("Overweight")
            }
            "Dental Disease" -> {
                if (pet.age > 36) factors.add("Age-related plaque buildup")
            }
        }

        return factors
    }

    private fun shouldGetYearlyBooster(pet: PetModel, vaccineType: String): Boolean {
        // Since vaccineHistory is now a Map<String, Any>, we need to check differently
        // For now, let's implement a simple check based on the map structure

        // Check if the vaccine type exists in the history and when it was last administered
        val lastVaccineKey = pet.vaccineHistory.keys.find { key ->
            key.contains(vaccineType, ignoreCase = true)
        }

        if (lastVaccineKey != null) {
            // Try to extract the date from the vaccine record
            val vaccineData = pet.vaccineHistory[lastVaccineKey]
            val lastVaccineDate = when (vaccineData) {
                is Map<*, *> -> (vaccineData["dateAdministered"] as? Long) ?: 0L
                is Long -> vaccineData // If the value itself is a timestamp
                else -> 0L
            }

            val monthsSinceLastVaccine = (System.currentTimeMillis() - lastVaccineDate) / (30L * 24 * 60 * 60 * 1000)
            return monthsSinceLastVaccine >= 12 // Need yearly booster
        }

        return true // If no vaccine found, booster is needed
    }

    private fun PetModel.hasReceivedVaccine(vaccineName: String): Boolean {
        // Check if the vaccine name exists in the vaccineHistory map
        return vaccineHistory.keys.any { key ->
            key.contains(vaccineName.split(" ").first(), ignoreCase = true)
        } || vaccineHistory.values.any { value ->
            value.toString().contains(vaccineName.split(" ").first(), ignoreCase = true)
        }
    }
}

data class DiseaseRiskAssessment(
    val disease: DiseaseModel,
    val riskPercentage: Int,
    val isHighRisk: Boolean,
    val riskFactors: List<String>,
    val preventionTips: List<String>
)

data class DiseaseWarning(
    val disease: DiseaseModel,
    val warningLevel: WarningLevel,
    val riskPercentage: Int,
    val recommendedAction: String,
    val urgency: UrgencyLevel
)

enum class WarningLevel(val priority: Int) {
    HIGH(3),
    MEDIUM(2),
    LOW(1),
    INFO(0)
}

enum class UrgencyLevel(val priority: Int) {
    IMMEDIATE(3),
    HIGH(2),
    MEDIUM(1),
    LOW(0)
}

data class VaccinationRecommendation(
    val vaccine: VaccineModel,
    val protectedDiseases: List<DiseaseModel>,
    val urgency: UrgencyLevel,
    val reason: String
)
