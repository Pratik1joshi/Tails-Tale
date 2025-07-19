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
            // Puppy diseases (0-6 months)
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
                preventionTips = listOf("Vaccination", "Avoid unvaccinated dogs", "Keep puppy indoors until fully vaccinated"),
                treatmentSteps = listOf("Emergency vet care", "IV fluids", "Anti-nausea medication", "Isolation")
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
                preventionTips = listOf("DHPP vaccination", "Avoid contact with wild animals", "Keep vaccination schedule"),
                treatmentSteps = listOf("Supportive care", "Antibiotics for secondary infections", "Fluid therapy", "Anti-seizure medication")
            ),

            // Young dog diseases (6 months - 2 years)
            DiseaseModel(
                name = "Kennel Cough",
                description = "A highly contagious respiratory infection",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.BACTERIAL,
                healthImpact = 15,
                happinessImpact = 10,
                durationDays = 7..21,
                treatmentCost = 100,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Dry hacking cough", "Retching", "Mild fever", "Runny nose"),
                preventionTips = listOf("Vaccination", "Avoid crowded dog areas when sick", "Good ventilation"),
                treatmentSteps = listOf("Rest", "Cough suppressants", "Antibiotics if severe", "Isolation from other dogs")
            ),

            DiseaseModel(
                name = "Intestinal Worms",
                description = "Parasitic worms living in the digestive tract",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.PARASITIC,
                healthImpact = 20,
                happinessImpact = 15,
                durationDays = 14..30,
                treatmentCost = 80,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Visible worms in stool", "Vomiting", "Diarrhea", "Weight loss", "Bloated belly"),
                preventionTips = listOf("Regular deworming", "Clean up feces promptly", "Prevent eating contaminated food"),
                treatmentSteps = listOf("Deworming medication", "Follow-up treatment", "Fecal examination", "Environmental cleaning")
            ),

            // Adult dog diseases (2-7 years)
            DiseaseModel(
                name = "Hip Dysplasia",
                description = "A genetic condition affecting hip joint development",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 25,
                happinessImpact = 30,
                durationDays = 365..1825, // 1-5 years chronic condition
                treatmentCost = 800,
                affectedPetTypes = setOf(PetType.DOG),
                symptoms = listOf("Limping", "Difficulty rising", "Reduced activity", "Pain when touched", "Bunny hopping gait"),
                preventionTips = listOf("Proper nutrition during growth", "Avoid overexercise in puppies", "Maintain healthy weight"),
                treatmentSteps = listOf("Weight management", "Physical therapy", "Pain medication", "Joint supplements", "Surgery in severe cases")
            ),

            DiseaseModel(
                name = "Allergies",
                description = "Immune system reaction to environmental or food allergens",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.BEHAVIORAL,
                healthImpact = 10,
                happinessImpact = 20,
                durationDays = 30..365, // Seasonal or chronic
                treatmentCost = 200,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Itchy skin", "Red eyes", "Sneezing", "Ear infections", "Excessive licking"),
                preventionTips = listOf("Identify and avoid triggers", "Regular grooming", "Hypoallergenic diet"),
                treatmentSteps = listOf("Antihistamines", "Medicated baths", "Dietary changes", "Allergy testing")
            ),

            DiseaseModel(
                name = "Ear Infections",
                description = "Bacterial or yeast infection in the ear canal",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.BACTERIAL,
                healthImpact = 15,
                happinessImpact = 25,
                durationDays = 7..14,
                treatmentCost = 120,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Head shaking", "Ear scratching", "Bad odor", "Dark discharge", "Red ear canal"),
                preventionTips = listOf("Regular ear cleaning", "Keep ears dry", "Trim ear hair"),
                treatmentSteps = listOf("Ear cleaning", "Antibiotic drops", "Anti-inflammatory medication", "Follow-up examination")
            ),

            // Senior dog diseases (7+ years)
            DiseaseModel(
                name = "Arthritis",
                description = "Joint inflammation causing pain and stiffness",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 30,
                happinessImpact = 35,
                durationDays = 365..3650, // Chronic, lifelong
                treatmentCost = 400,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Stiffness", "Reluctance to move", "Difficulty climbing stairs", "Limping", "Reduced activity"),
                preventionTips = listOf("Maintain healthy weight", "Regular gentle exercise", "Joint supplements"),
                treatmentSteps = listOf("Pain medication", "Weight management", "Physical therapy", "Joint supplements", "Comfortable bedding")
            ),

            DiseaseModel(
                name = "Heart Disease",
                description = "Cardiovascular problems affecting heart function",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 40,
                happinessImpact = 30,
                durationDays = 180..1825, // 6 months to 5 years
                treatmentCost = 1000,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Coughing", "Difficulty breathing", "Fatigue", "Reduced appetite", "Fainting"),
                preventionTips = listOf("Regular check-ups", "Healthy diet", "Regular exercise", "Weight management"),
                treatmentSteps = listOf("Cardiac medication", "Dietary changes", "Exercise restriction", "Regular monitoring")
            ),

            DiseaseModel(
                name = "Kidney Disease",
                description = "Progressive decline in kidney function",
                severity = DiseaseSeverity.SEVERE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 35,
                happinessImpact = 25,
                durationDays = 365..1825, // 1-5 years chronic
                treatmentCost = 800,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Increased thirst", "Frequent urination", "Weight loss", "Vomiting", "Loss of appetite"),
                preventionTips = listOf("Fresh water always available", "Regular blood tests", "Kidney-friendly diet"),
                treatmentSteps = listOf("Special diet", "Fluid therapy", "Medication", "Regular blood monitoring")
            ),

            DiseaseModel(
                name = "Diabetes",
                description = "Inability to properly regulate blood sugar levels",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.GENETIC,
                healthImpact = 30,
                happinessImpact = 20,
                durationDays = 365..3650, // Lifelong management
                treatmentCost = 600,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Excessive thirst", "Frequent urination", "Weight loss", "Increased appetite", "Lethargy"),
                preventionTips = listOf("Maintain healthy weight", "Regular exercise", "Consistent feeding schedule"),
                treatmentSteps = listOf("Insulin injections", "Dietary management", "Regular glucose monitoring", "Exercise routine")
            ),

            // Common year-round diseases
            DiseaseModel(
                name = "Fleas and Ticks",
                description = "External parasites that feed on blood",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.PARASITIC,
                healthImpact = 10,
                happinessImpact = 25,
                durationDays = 7..30,
                treatmentCost = 60,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Excessive scratching", "Red bumps on skin", "Visible fleas", "Hair loss", "Skin irritation"),
                preventionTips = listOf("Monthly flea prevention", "Regular grooming", "Vacuum regularly", "Treat all pets"),
                treatmentSteps = listOf("Flea shampoo", "Topical treatment", "Environmental treatment", "Prevention medication")
            ),

            DiseaseModel(
                name = "Dental Disease",
                description = "Plaque and tartar buildup leading to gum disease",
                severity = DiseaseSeverity.MODERATE,
                category = DiseaseCategory.BACTERIAL,
                healthImpact = 20,
                happinessImpact = 15,
                durationDays = 90..365,
                treatmentCost = 300,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Bad breath", "Yellow tartar", "Red gums", "Difficulty eating", "Loose teeth"),
                preventionTips = listOf("Daily teeth brushing", "Dental chews", "Regular dental cleanings"),
                treatmentSteps = listOf("Professional cleaning", "Tooth extraction if needed", "Antibiotics", "Pain medication")
            ),

            DiseaseModel(
                name = "Upset Stomach",
                description = "Digestive upset from dietary indiscretion or stress",
                severity = DiseaseSeverity.MILD,
                category = DiseaseCategory.NUTRITIONAL,
                healthImpact = 10,
                happinessImpact = 15,
                durationDays = 1..5,
                treatmentCost = 40,
                affectedPetTypes = setOf(PetType.DOG, PetType.CAT),
                symptoms = listOf("Vomiting", "Diarrhea", "Loss of appetite", "Lethargy", "Gas"),
                preventionTips = listOf("Consistent diet", "Avoid table scraps", "Slow feeding", "Stress management"),
                treatmentSteps = listOf("Bland diet", "Fasting for 12-24 hours", "Probiotics", "Plenty of water")
            )
        ))
    }
}
