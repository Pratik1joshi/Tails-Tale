package com.example.tailstale.repo

import com.example.tailstale.model.DiseaseCategory
import com.example.tailstale.model.DiseaseModel
import com.example.tailstale.model.DiseaseSeverity
import com.example.tailstale.model.PetType

class DiseaseRepositoryImpl : DiseaseRepository {
    private val diseases = mutableListOf<DiseaseModel>()

    init {
        initializeSampleDiseases()
    }

    override suspend fun getAllDiseases(): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseasesByPetType(petType: PetType): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.filter { it.affectedPetTypes.contains(petType) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseasesBySeverity(severity: DiseaseSeverity): Result<List<DiseaseModel>> {
        return try {
            Result.success(diseases.filter { it.severity == severity })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiseaseById(diseaseId: String): Result<DiseaseModel?> {
        return try {
            Result.success(diseases.find { it.id == diseaseId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomDisease(petType: PetType): Result<DiseaseModel?> {
        return try {
            val compatibleDiseases = diseases.filter { it.affectedPetTypes.contains(petType) }
            Result.success(compatibleDiseases.randomOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeSampleDiseases() {
        diseases.addAll(listOf(
            // Puppy diseases (0-6 months) - High risk without vaccination
            DiseaseModel(
                name = "Parvovirus",
                description = "A highly contagious viral infection that affects the digestive system",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.VIRAL,
                healthImpact = 40,
                happinessImpact = 30,
                durationDays = 7..14,
                treatmentCost = 500,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Severe vomiting", "Bloody diarrhea", "Lethargy", "Loss of appetite", "Dehydration"),
                preventionTips = listOf("DHPP vaccination series", "Avoid unvaccinated dogs", "Keep puppy indoors until fully vaccinated"),
                treatmentSteps = listOf("Emergency vet care", "IV fluids", "Anti-nausea medication", "Isolation"),
                vulnerableAgeRange = 6..16, // 6-16 weeks most vulnerable
                riskFactorByAge = mapOf(
                    (6..8) to 0.15, // 15% risk at 6-8 weeks without vaccine
                    (9..12) to 0.25, // 25% risk at 9-12 weeks
                    (13..16) to 0.10 // 10% risk at 13-16 weeks
                ),
                preventableByVaccines = setOf("DHPP First Dose", "DHPP Second Dose", "DHPP Final Booster"),
                requiredVaccineForPrevention = "DHPP First Dose"
            ),

            DiseaseModel(
                name = "Distemper",
                description = "A serious viral disease affecting respiratory, digestive and nervous systems",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.VIRAL,
                healthImpact = 45,
                happinessImpact = 35,
                durationDays = 14..21,
                treatmentCost = 600,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Fever", "Nasal discharge", "Coughing", "Vomiting", "Seizures"),
                preventionTips = listOf("DHPP vaccination series", "Avoid contact with wild animals", "Keep vaccination schedule"),
                treatmentSteps = listOf("Supportive care", "Antibiotics for secondary infections", "Fluid therapy", "Anti-seizure medication"),
                vulnerableAgeRange = 6..24, // 6 weeks to 2 years
                riskFactorByAge = mapOf(
                    (6..12) to 0.20, // 20% risk in puppies
                    (13..24) to 0.08 // 8% risk in young dogs
                ),
                preventableByVaccines = setOf("DHPP First Dose", "DHPP Second Dose", "DHPP Final Booster"),
                requiredVaccineForPrevention = "DHPP First Dose"
            ),

            DiseaseModel(
                name = "Rabies",
                description = "Fatal viral disease affecting the nervous system - legally required vaccination",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.VIRAL,
                healthImpact = 100, // Fatal if contracted
                happinessImpact = 100,
                durationDays = 7..10,
                treatmentCost = 0, // No treatment - prevention only
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Aggression", "Excessive drooling", "Difficulty swallowing", "Paralysis"),
                preventionTips = listOf("Rabies vaccination", "Avoid wild animals", "Report animal bites"),
                treatmentSteps = listOf("No treatment available - prevention through vaccination only"),
                vulnerableAgeRange = 14..Int.MAX_VALUE, // From 14 weeks onward
                riskFactorByAge = mapOf(
                    (14..52) to 0.02, // 2% risk in first year without vaccine
                    (53..Int.MAX_VALUE) to 0.01 // 1% ongoing risk
                ),
                preventableByVaccines = setOf("Rabies First Dose", "Rabies Annual Booster"),
                requiredVaccineForPrevention = "Rabies First Dose"
            ),

            // Young dog diseases (6 months - 2 years)
            DiseaseModel(
                name = "Kennel Cough",
                description = "A highly contagious respiratory infection common in social environments",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.BACTERIAL,
                healthImpact = 15,
                happinessImpact = 10,
                durationDays = 7..21,
                treatmentCost = 100,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Dry hacking cough", "Retching", "Mild fever", "Runny nose"),
                preventionTips = listOf("Bordetella vaccination", "Avoid crowded dog areas when sick", "Good ventilation"),
                treatmentSteps = listOf("Rest", "Cough suppressants", "Antibiotics if severe", "Isolation from other dogs"),
                vulnerableAgeRange = 12..48, // 3 months to 4 years
                riskFactorByAge = mapOf(
                    (12..24) to 0.12, // 12% risk in young dogs in social settings
                    (25..48) to 0.08 // 8% risk in adult dogs
                ),
                preventableByVaccines = setOf("Bordetella (Kennel Cough)"),
                requiredVaccineForPrevention = "Bordetella (Kennel Cough)"
            ),

            DiseaseModel(
                name = "Lyme Disease",
                description = "Tick-borne bacterial infection affecting joints and organs",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.BACTERIAL,
                healthImpact = 25,
                happinessImpact = 20,
                durationDays = 14..30,
                treatmentCost = 200,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Lameness", "Joint swelling", "Fever", "Loss of appetite", "Lethargy"),
                preventionTips = listOf("Lyme vaccination", "Tick prevention", "Regular tick checks", "Avoid tick-infested areas"),
                treatmentSteps = listOf("Antibiotics", "Anti-inflammatory drugs", "Rest", "Physical therapy"),
                vulnerableAgeRange = 12..Int.MAX_VALUE, // From 3 months onward
                riskFactorByAge = mapOf(
                    (12..60) to 0.05, // 5% risk in active young dogs in tick areas
                    (61..Int.MAX_VALUE) to 0.03 // 3% risk in older dogs
                ),
                preventableByVaccines = setOf("Lyme Disease"),
                requiredVaccineForPrevention = "Lyme Disease"
            ),

            // Parasitic diseases - all ages
            DiseaseModel(
                name = "Intestinal Worms",
                description = "Internal parasites that affect digestion and nutrient absorption",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.PARASITIC,
                healthImpact = 10,
                happinessImpact = 15,
                durationDays = 7..14,
                treatmentCost = 50,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Pot-bellied appearance", "Diarrhea", "Vomiting", "Weight loss", "Visible worms in stool"),
                preventionTips = listOf("Regular deworming", "Clean environment", "Proper waste disposal"),
                treatmentSteps = listOf("Deworming medication", "Follow-up treatment", "Environmental cleaning"),
                vulnerableAgeRange = 2..Int.MAX_VALUE, // From 2 weeks onward
                riskFactorByAge = mapOf(
                    (2..12) to 0.30, // 30% risk in puppies
                    (13..Int.MAX_VALUE) to 0.15 // 15% ongoing risk without prevention
                ),
                preventableByVaccines = setOf("Deworming Treatment"),
                requiredVaccineForPrevention = "Deworming Treatment"
            ),

            DiseaseModel(
                name = "Heartworm Disease",
                description = "Serious parasitic infection transmitted by mosquitoes affecting heart and lungs",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.PARASITIC,
                healthImpact = 35,
                happinessImpact = 25,
                durationDays = 30..180, // Long-term condition
                treatmentCost = 800,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Coughing", "Exercise intolerance", "Weight loss", "Difficulty breathing"),
                preventionTips = listOf("Monthly heartworm prevention", "Mosquito control", "Regular testing"),
                treatmentSteps = listOf("Heartworm treatment protocol", "Exercise restriction", "Follow-up testing"),
                vulnerableAgeRange = 8..Int.MAX_VALUE, // From 8 weeks onward
                riskFactorByAge = mapOf(
                    (8..Int.MAX_VALUE) to 0.08 // 8% risk in areas with mosquitoes without prevention
                ),
                preventableByVaccines = setOf("Heartworm Prevention"),
                requiredVaccineForPrevention = "Heartworm Prevention"
            ),

            // Senior dog diseases (7+ years / 84+ months)
            DiseaseModel(
                name = "Hip Dysplasia",
                description = "Genetic condition affecting hip joints, more common with age",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 20,
                happinessImpact = 30,
                durationDays = 30..Int.MAX_VALUE, // Chronic condition
                treatmentCost = 300,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Limping", "Difficulty standing", "Reduced activity", "Joint stiffness"),
                preventionTips = listOf("Weight management", "Regular exercise", "Joint supplements"),
                treatmentSteps = listOf("Pain management", "Physical therapy", "Weight control", "Surgery if severe"),
                vulnerableAgeRange = 84..Int.MAX_VALUE, // 7+ years
                riskFactorByAge = mapOf(
                    (84..120) to 0.10, // 10% risk in senior years
                    (121..Int.MAX_VALUE) to 0.15 // 15% risk in very senior years
                ),
                preventableByVaccines = setOf(), // No vaccine prevention
                requiredVaccineForPrevention = null
            )
        ))
    }

    // NEW: Get diseases that pet is vulnerable to at current age
    override suspend fun getVulnerableDiseasesForAge(petType: PetType, petAge: Int): Result<List<DiseaseModel>> {
        return try {
            val vulnerableDiseases = diseases.filter { disease ->
                disease.affectedPetTypes.contains(petType) && petAge in disease.vulnerableAgeRange
            }
            Result.success(vulnerableDiseases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: Get diseases that can be prevented by vaccines
    override suspend fun getPreventableDiseasesForAge(petType: PetType, petAge: Int): Result<List<DiseaseModel>> {
        return try {
            val preventableDiseases = diseases.filter { disease ->
                disease.affectedPetTypes.contains(petType) &&
                petAge in disease.vulnerableAgeRange &&
                disease.preventableByVaccines.isNotEmpty()
            }
            Result.success(preventableDiseases)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: Check if pet is protected from disease through vaccination
    override suspend fun isPetProtectedFromDisease(diseaseId: String, petVaccineHistory: Map<String, Any>): Result<Boolean> {
        return try {
            val disease = diseases.find { it.id == diseaseId }
            if (disease == null) {
                Result.success(false)
            } else {
                // Check if pet has any of the protective vaccines
                val isProtected = disease.preventableByVaccines.any { vaccineName ->
                    petVaccineHistory.values.any { vaccineRecord ->
                        vaccineRecord.toString().contains(vaccineName, ignoreCase = true)
                    }
                }
                Result.success(isProtected)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
