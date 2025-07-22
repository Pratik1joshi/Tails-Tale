package com.example.tailstale.repo

import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineModel

class VaccineRepositoryImpl : VaccineRepository {
    private val vaccines = mutableListOf<VaccineModel>()

    init {
        initializeVaccineData()
    }

    override suspend fun getAllVaccines(): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccinesByPetType(petType: PetType): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter { it.compatiblePetTypes.contains(petType) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccineById(vaccineId: String): Result<VaccineModel?> {
        return try {
            Result.success(vaccines.find { it.id == vaccineId })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVaccinesByAgeRequirement(petAge: Int): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter { petAge in it.ageRequirement })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRequiredVaccines(petType: PetType, petAge: Int): Result<List<VaccineModel>> {
        return try {
            Result.success(vaccines.filter {
                it.compatiblePetTypes.contains(petType) && petAge in it.ageRequirement
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun initializeVaccineData() {
        vaccines.addAll(listOf(
            // Core Vaccines (Mandatory)
            VaccineModel(
                name = "DHPP First Dose",
                description = "First dose of DHPP vaccine protecting against Distemper, Hepatitis, Parvovirus, and Parainfluenza",
                targetDisease = "Distemper, Hepatitis, Parvovirus, Parainfluenza",
                effectiveDurationDays = 30,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 6..8, // 6-8 weeks
                sideEffects = listOf("Mild swelling at injection site", "Slight fatigue")
            ),
            VaccineModel(
                name = "DHPP Second Dose",
                description = "Second booster of DHPP vaccine for enhanced protection",
                targetDisease = "Distemper, Hepatitis, Parvovirus, Parainfluenza",
                effectiveDurationDays = 30,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 10..12, // 10-12 weeks
                sideEffects = listOf("Mild swelling at injection site")
            ),
            VaccineModel(
                name = "DHPP Final Booster",
                description = "Final puppy series DHPP vaccine for full immunity",
                targetDisease = "Distemper, Hepatitis, Parvovirus, Parainfluenza",
                effectiveDurationDays = 365,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 14..16, // 14-16 weeks
                sideEffects = listOf("Mild swelling at injection site")
            ),
            VaccineModel(
                name = "Rabies First Dose",
                description = "Essential first rabies vaccine - legally required",
                targetDisease = "Rabies",
                effectiveDurationDays = 365,
                cost = 50,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 14..16, // 14-16 weeks
                sideEffects = listOf("Mild fatigue", "Slight fever", "Injection site soreness")
            ),
            VaccineModel(
                name = "DHPP Yearly Booster",
                description = "Annual DHPP booster for adult dogs",
                targetDisease = "Distemper, Hepatitis, Parvovirus, Parainfluenza",
                effectiveDurationDays = 365,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 12..Int.MAX_VALUE, // 1 year and older
                sideEffects = listOf("Mild swelling at injection site")
            ),
            VaccineModel(
                name = "Rabies Annual Booster",
                description = "Annual rabies booster for adult dogs",
                targetDisease = "Rabies",
                effectiveDurationDays = 365,
                cost = 50,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 12..Int.MAX_VALUE, // 1 year and older
                sideEffects = listOf("Mild fatigue", "Injection site soreness")
            ),

            // Non-Core Vaccines (Lifestyle-based)
            VaccineModel(
                name = "Bordetella (Kennel Cough)",
                description = "Protects against kennel cough - recommended for dogs around other dogs",
                targetDisease = "Kennel Cough",
                effectiveDurationDays = 365,
                cost = 35,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 12..Int.MAX_VALUE, // 12 weeks and older
                sideEffects = listOf("Mild cough", "Nasal discharge for 1-2 days")
            ),
            VaccineModel(
                name = "Leptospirosis",
                description = "Protects against bacterial infection from contaminated water",
                targetDisease = "Leptospirosis",
                effectiveDurationDays = 365,
                cost = 40,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 12..Int.MAX_VALUE,
                sideEffects = listOf("Mild allergic reaction possible", "Injection site swelling")
            ),
            VaccineModel(
                name = "Lyme Disease",
                description = "Protects against tick-borne Lyme disease",
                targetDisease = "Lyme Disease",
                effectiveDurationDays = 365,
                cost = 45,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 12..Int.MAX_VALUE,
                sideEffects = listOf("Mild lethargy", "Injection site soreness")
            ),
            VaccineModel(
                name = "Canine Influenza",
                description = "Protects against dog flu - recommended for social dogs",
                targetDisease = "Canine Influenza",
                effectiveDurationDays = 365,
                cost = 55,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 12..Int.MAX_VALUE,
                sideEffects = listOf("Mild respiratory symptoms", "Low-grade fever")
            ),

            // Preventive Treatments
            VaccineModel(
                name = "Deworming Treatment",
                description = "Monthly deworming treatment to prevent intestinal parasites",
                targetDisease = "Intestinal Worms",
                effectiveDurationDays = 30,
                cost = 25,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 2..Int.MAX_VALUE, // From 2 weeks old
                sideEffects = listOf("Mild stomach upset", "Temporary loose stool")
            ),
            VaccineModel(
                name = "Flea & Tick Prevention",
                description = "Monthly flea and tick prevention treatment",
                targetDisease = "Fleas and Ticks",
                effectiveDurationDays = 30,
                cost = 30,
                compatiblePetTypes = setOf(PetType.DOG, PetType.CAT),
                ageRequirement = 8..Int.MAX_VALUE, // From 8 weeks old
                sideEffects = listOf("Mild skin irritation at application site")
            ),
            VaccineModel(
                name = "Heartworm Prevention",
                description = "Monthly heartworm prevention - essential in warm climates",
                targetDisease = "Heartworm",
                effectiveDurationDays = 30,
                cost = 35,
                compatiblePetTypes = setOf(PetType.DOG),
                ageRequirement = 8..Int.MAX_VALUE, // From 8 weeks old
                sideEffects = listOf("Rare: mild digestive upset")
            )
        ))
    }
}
