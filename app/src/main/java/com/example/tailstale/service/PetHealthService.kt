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
     * Randomly triggers diseases based on age and risk factors
     */
    suspend fun checkForRandomDisease(pet: PetModel): DiseaseModel? {
        val riskAssessments = calculateDiseaseRisk(pet)

        // Higher chance of disease if pet is very young, old, or unvaccinated
        val diseaseChance = when {
            pet.age < 6 -> 0.15 // Puppies are vulnerable
            pet.age > 84 -> 0.20 // Senior dogs are vulnerable
            pet.isVaccineOverdue -> 0.25 // Unvaccinated pets are at high risk
            pet.health < 70 -> 0.10 // Already sick pets more susceptible
            else -> 0.05 // Healthy adult dogs have low risk
        }

        if (Random.nextFloat() < diseaseChance) {
            // Pick a disease weighted by risk
            val weightedDiseases = riskAssessments.filter { it.riskPercentage > 10 }
            if (weightedDiseases.isNotEmpty()) {
                val randomDisease = weightedDiseases.random()
                return randomDisease.disease
            }
        }

        return null
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
        val lastVaccineDate = pet.vaccineHistory
            .filter { (it["vaccineType"] as? String)?.contains(vaccineType) == true }
            .maxByOrNull { it["dateAdministered"] as? Long ?: 0L }
            ?.get("dateAdministered") as? Long ?: 0L

        val monthsSinceLastVaccine = (System.currentTimeMillis() - lastVaccineDate) / (30L * 24 * 60 * 60 * 1000)
        return monthsSinceLastVaccine >= 12 // Need yearly booster
    }

    private fun PetModel.hasReceivedVaccine(vaccineName: String): Boolean {
        return vaccineHistory.any { vaccine ->
            (vaccine["vaccineName"] as? String)?.contains(vaccineName.split(" ").first()) == true
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
