package com.example.tailstale.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.CareAction
import com.example.tailstale.model.CareActionType
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
import com.example.tailstale.repo.PetRepository
import com.example.tailstale.repo.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val activityRepository: com.example.tailstale.repo.ActivityRepository = com.example.tailstale.repo.ActivityRepositoryImpl(),
    private val achievementManager: com.example.tailstale.service.AchievementManager = com.example.tailstale.service.AchievementManager()
) : ViewModel() {

    private val _pets = MutableStateFlow<List<PetModel>>(emptyList())
    val pets: StateFlow<List<PetModel>> = _pets

    private val _currentPet = MutableStateFlow<PetModel?>(null)
    val currentPet: StateFlow<PetModel?> = _currentPet

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Add real-time aging service
    private val petAgingService = com.example.tailstale.service.PetAgingService(petRepository)

    // Add aging stats state
    private val _petAgingStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val petAgingStats: StateFlow<Map<String, Any>> = _petAgingStats

    // Add health service and new states
    private val petHealthService = com.example.tailstale.service.PetHealthService(
        com.example.tailstale.repo.DiseaseRepositoryImpl(),
        com.example.tailstale.repo.VaccineRepositoryImpl()
    )

    private val _requiredVaccines = MutableStateFlow<List<com.example.tailstale.model.VaccineModel>>(emptyList())
    val requiredVaccines: StateFlow<List<com.example.tailstale.model.VaccineModel>> = _requiredVaccines

    private val _overdueVaccines = MutableStateFlow<List<com.example.tailstale.model.VaccineModel>>(emptyList())
    val overdueVaccines: StateFlow<List<com.example.tailstale.model.VaccineModel>> = _overdueVaccines

    private val _diseaseRisks = MutableStateFlow<List<com.example.tailstale.service.DiseaseRiskAssessment>>(emptyList())
    val diseaseRisks: StateFlow<List<com.example.tailstale.service.DiseaseRiskAssessment>> = _diseaseRisks

    // NEW: Disease warnings and vaccination recommendations
    private val _diseaseWarnings = MutableStateFlow<List<com.example.tailstale.service.DiseaseWarning>>(emptyList())
    val diseaseWarnings: StateFlow<List<com.example.tailstale.service.DiseaseWarning>> = _diseaseWarnings

    private val _vaccinationRecommendations = MutableStateFlow<List<com.example.tailstale.service.VaccinationRecommendation>>(emptyList())
    val vaccinationRecommendations: StateFlow<List<com.example.tailstale.service.VaccinationRecommendation>> = _vaccinationRecommendations

    // Add achievement-related states
    private val _newlyUnlockedAchievements = MutableStateFlow<List<com.example.tailstale.model.Achievement>>(emptyList())
    val newlyUnlockedAchievements: StateFlow<List<com.example.tailstale.model.Achievement>> = _newlyUnlockedAchievements

    private val _userStats = MutableStateFlow<com.example.tailstale.model.UserStats?>(null)
    val userStats: StateFlow<com.example.tailstale.model.UserStats?> = _userStats

    /**
     * Start real-time aging for a user's pets
     */
    fun startRealTimeAging(userId: String) {
        petAgingService.startRealTimeAging(userId)

        // Also start a periodic refresh to update UI
        viewModelScope.launch {
            while (true) {
                delay(60000) // Refresh every minute
                refreshPetsData(userId)
            }
        }
    }

    /**
     * Stop real-time aging
     */
    fun stopRealTimeAging() {
        petAgingService.stopRealTimeAging()
    }

    /**
     * Refresh pets data without full reload
     */
    private suspend fun refreshPetsData(userId: String) {
        try {
            petRepository.getPetsByUserId(userId).fold(
                onSuccess = { petList ->
                    _pets.value = petList
                    // Update current pet if it's in the list
                    _currentPet.value?.let { currentPet ->
                        val updatedCurrentPet = petList.find { it.id == currentPet.id }
                        if (updatedCurrentPet != null) {
                            _currentPet.value = updatedCurrentPet
                            updatePetAgingStats(updatedCurrentPet)
                        }
                    }
                },
                onFailure = { /* Silently fail to avoid spamming errors */ }
            )
        } catch (e: Exception) {
            // Silently handle refresh errors
        }
    }

    /**
     * Update aging statistics for current pet
     */
    private fun updatePetAgingStats(pet: PetModel) {
        _petAgingStats.value = petAgingService.getPetAgingStats(pet)
    }

//    fun createPet(name: String, petType: PetType, userId: String) {
//        viewModelScope.launch {
//            _loading.value = true
//            _error.value = null // Clear any previous errors
//
//            try {
//                val pet = PetModel(
//                    name = name,
//                    type = petType.name,
//                    age = 1, // Start at 1 month old
//                    ageInRealDays = 0,
//                    lastAgeUpdate = System.currentTimeMillis(),
//                    lastStatsDecay = System.currentTimeMillis()
//                )
//
//                // Create the pet first
//                petRepository.createPet(pet).fold(
//                    onSuccess = { createdPet ->
//                        // Link the pet to the user using the linkPetToUser method
//                        viewModelScope.launch {
//                            (petRepository as? com.example.tailstale.repo.PetRepositoryImpl)?.linkPetToUser(userId, createdPet.id)?.fold(
//                                onSuccess = {
//                                    // Successfully linked, now update UI state
//                                    _currentPet.value = createdPet
//                                    updatePetHealth(createdPet)
//                                    updatePetAgingStats(createdPet)
//
//                                    // Track pet creation achievement
//                                    trackAchievementAction("addPet", createdPet)
//
//                                    // Reload the user's pets to refresh the list
//                                    loadUserPets(userId)
//
//                                    _error.value = "Pet '${createdPet.name}' created successfully!"
//                                    _loading.value = false
//                                },
//                                onFailure = { linkError ->
//                                    _error.value = "Pet created but failed to link to user: ${linkError.message}"
//                                    _loading.value = false
//                                }
//                            )
//                        }
//                    },
//                    onFailure = { createError ->
//                        _error.value = "Failed to create pet: ${createError.message}"
//                        _loading.value = false
//                    }
//                )
//            } catch (e: Exception) {
//                _error.value = "Error creating pet: ${e.message}"
//                _loading.value = false
//            }
//        }
//    }

    fun loadUserPets(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            petRepository.getPetsByUserId(userId).fold(
                onSuccess = { petList ->
                    // Process each pet through the aging service - NOW INCLUDES BACKGROUND DECAY
                    val updatedPets = petList.map { pet ->
                        // Apply background decay and aging that happened while app was closed
                        val agedPet = petAgingService.processPetAging(pet)

                        // Update pet in repository if changed (background decay applied)
                        if (agedPet != pet) {
                            petRepository.updatePet(agedPet)
                            Log.d("PetViewModel", "Applied background updates to ${pet.name}")
                            Log.d("PetViewModel", "Before: H:${pet.hunger} E:${pet.energy} C:${pet.cleanliness} Ha:${pet.happiness} Age:${pet.age}")
                            Log.d("PetViewModel", "After: H:${agedPet.hunger} E:${agedPet.energy} C:${agedPet.cleanliness} Ha:${agedPet.happiness} Age:${agedPet.age}")
                        }

                        agedPet
                    }

                    // Process disease checks for aged pets separately
                    val finalUpdatedPets = mutableListOf<PetModel>()
                    for (pet in updatedPets) {
                        val originalPet = petList.find { it.id == pet.id }
                        if (originalPet != null && pet.age > originalPet.age) {
                            // Pet aged up, check for random disease
                            val randomDisease = petHealthService.checkForRandomDiseaseWithProtection(pet)
                            if (randomDisease != null) {
                                // Apply disease effects
                                val affectedPet = pet.copy(
                                    health = maxOf(0, pet.health - randomDisease.healthImpact),
                                    happiness = maxOf(0, pet.happiness - randomDisease.happinessImpact),
                                    diseaseHistory = pet.diseaseHistory + mapOf(
                                        "diseaseName" to randomDisease.name,
                                        "severity" to randomDisease.severity.name,
                                        "diagnosedDate" to System.currentTimeMillis(),
                                        "treatmentCost" to randomDisease.treatmentCost,
                                        "symptoms" to randomDisease.symptoms
                                    )
                                )
                                petRepository.updatePet(affectedPet)
                                finalUpdatedPets.add(affectedPet)
                            } else {
                                finalUpdatedPets.add(pet)
                            }
                        } else {
                            finalUpdatedPets.add(pet)
                        }
                    }

                    _pets.value = finalUpdatedPets
                    // Set current pet to first pet if none selected and pets exist
                    if (_currentPet.value == null && finalUpdatedPets.isNotEmpty()) {
                        _currentPet.value = finalUpdatedPets.first()
                        updatePetAgingStats(finalUpdatedPets.first())
                    }
                    _error.value = null
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
            _loading.value = false
        }
    }

    fun selectPet(pet: PetModel) {
        _currentPet.value = pet
        updatePetHealth(pet)
        updatePetAgingStats(pet)
    }

    /**
     * Force update a specific pet's aging
     */
    fun forceAgePet(petId: String) {
        viewModelScope.launch {
            petAgingService.forceUpdatePet(petId).fold(
                onSuccess = { updatedPet ->
                    // Update the pet in our local state
                    _pets.value = _pets.value.map {
                        if (it.id == petId) updatedPet else it
                    }

                    // Update current pet if it's the one being aged
                    if (_currentPet.value?.id == petId) {
                        _currentPet.value = updatedPet
                        updatePetAgingStats(updatedPet)
                    }

                    _error.value = "Pet ${updatedPet.name} aged successfully!"
                },
                onFailure = {
                    _error.value = "Failed to age pet: ${it.message}"
                }
            )
        }
    }

    /**
     * Update pet health information including vaccines and disease risks
     */
    private fun updatePetHealth(pet: PetModel) {
        viewModelScope.launch {
            // Get required vaccines for current age
            val required = petHealthService.getRequiredVaccines(pet)
            _requiredVaccines.value = required

            // Get overdue vaccines
            val overdue = petHealthService.getOverdueVaccines(pet)
            _overdueVaccines.value = overdue

            // Calculate disease risks
            val risks = petHealthService.calculateDiseaseRisk(pet)
            _diseaseRisks.value = risks

            // Use available methods for disease warnings and vaccination recommendations
            val warnings = petHealthService.getAgeBasedDiseaseWarnings(pet)
            _diseaseWarnings.value = warnings

            val recommendations = petHealthService.getVaccinationRecommendations(pet)
            _vaccinationRecommendations.value = recommendations
        }
    }

    /**
     * Administer vaccine to pet
     */
    fun vaccinatePet(vaccineId: String, vaccineName: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                val vaccineRecord = mapOf(
                    "vaccineId" to vaccineId,
                    "vaccineName" to vaccineName,
                    "vaccineType" to vaccineName.split(" ").first(), // e.g., "DHPP" from "DHPP First Dose"
                    "dateAdministered" to System.currentTimeMillis(),
                    "nextDueDate" to System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year later
                )

                val updatedPet = pet.copy(
                    vaccineHistory = pet.vaccineHistory + vaccineRecord,
                    health = minOf(100, pet.health + 5) // Small health boost from vaccination
                )

                petRepository.updatePet(updatedPet).fold(
                    onSuccess = {
                        _currentPet.value = updatedPet
                        updatePetHealth(updatedPet) // Refresh health status
                        _error.value = null
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            }
        }
    }

    /**
     * NEW: Enhanced vaccination method with disease prevention check
     */
    fun administerVaccine(vaccineId: String, vaccineName: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                _loading.value = true

                val vaccineRecord = mapOf(
                    "vaccineId" to vaccineId,
                    "vaccineName" to vaccineName,
                    "vaccineType" to vaccineName.split(" ").first(),
                    "dateAdministered" to System.currentTimeMillis(),
                    "effectiveUntil" to System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
                    "administrationReason" to "Preventive healthcare"
                )

                val healthBoost = 10 // Enhanced health boost for serious vaccination

                val updatedPet = pet.copy(
                    vaccineHistory = pet.vaccineHistory + (System.currentTimeMillis().toString() to vaccineRecord),
                    health = minOf(100, pet.health + healthBoost)
                )

                petRepository.updatePet(updatedPet).fold(
                    onSuccess = {
                        _currentPet.value = updatedPet
                        updatePetHealth(updatedPet)
                        _error.value = "✅ ${pet.name} vaccinated with $vaccineName successfully!"
                        _loading.value = false
                    },
                    onFailure = {
                        _error.value = "❌ Failed to vaccinate: ${it.message}"
                        _loading.value = false
                    }
                )
            }
        }
    }

    /**
     * NEW: Treat an active disease and remove it from pet's conditions
     */
    fun treatDisease(diseaseName: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                _loading.value = true

                // Find the disease in the pet's history
                var diseaseFound = false
                var treatmentCost = 0
                var healthRecovered = 0

                val updatedDiseaseHistory = pet.diseaseHistory.toMutableMap()

                // Look for the active disease
                updatedDiseaseHistory.entries.forEach { (key, value) ->
                    val diseaseData = value as? Map<String, Any>
                    if (diseaseData?.get("diseaseName")?.toString() == diseaseName) {
                        val diagnosedDate = diseaseData["diagnosedDate"] as? Long ?: 0L
                        val daysSince = (System.currentTimeMillis() - diagnosedDate) / (1000 * 60 * 60 * 24)

                        // Only treat if disease is still active (within 30 days)
                        if (daysSince <= 30) {
                            diseaseFound = true
                            treatmentCost = diseaseData["treatmentCost"] as? Int ?: 100

                            // Calculate health recovery based on severity
                            val severity = diseaseData["severity"]?.toString() ?: "MILD"
                            healthRecovered = when (severity) {
                                "SEVERE" -> 30
                                "MODERATE" -> 20
                                "MILD" -> 15
                                else -> 10
                            }

                            // Mark disease as treated
                            updatedDiseaseHistory[key] = diseaseData + mapOf(
                                "treatmentDate" to System.currentTimeMillis(),
                                "status" to "TREATED",
                                "treatmentCost" to treatmentCost
                            )
                        }
                    }
                }

                if (diseaseFound) {
                    val updatedPet = pet.copy(
                        health = minOf(100, pet.health + healthRecovered),
                        diseaseHistory = updatedDiseaseHistory
                    )

                    petRepository.updatePet(updatedPet).fold(
                        onSuccess = {
                            _currentPet.value = updatedPet
                            updatePetHealth(updatedPet)
                            _error.value = "✅ $diseaseName treated successfully! Health recovered: +$healthRecovered. Treatment cost: $$treatmentCost"
                            _loading.value = false
                        },
                        onFailure = {
                            _error.value = "❌ Treatment completed but failed to update pet: ${it.message}"
                            _loading.value = false
                        }
                    )
                } else {
                    _error.value = "❌ No active $diseaseName found to treat"
                    _loading.value = false
                }
            }
        }
    }

    /**
     * NEW: Emergency health check - forces immediate health assessment
     */
    fun performEmergencyHealthCheck() {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                _loading.value = true

                // Check for active diseases (diagnosed within last 30 days)
                val activeDiseases = mutableListOf<String>()
                pet.diseaseHistory.forEach { (_, diseaseData) ->
                    val diseaseMap = diseaseData as? Map<String, Any>
                    if (diseaseMap != null) {
                        val diagnosedDate = diseaseMap["diagnosedDate"] as? Long ?: 0L
                        val daysSince = (System.currentTimeMillis() - diagnosedDate) / (1000 * 60 * 60 * 24)
                        val status = diseaseMap["status"]?.toString()

                        if (daysSince <= 30 && status != "TREATED") {
                            val diseaseName = diseaseMap["diseaseName"]?.toString() ?: "Unknown"
                            val severity = diseaseMap["severity"]?.toString() ?: "MILD"
                            activeDiseases.add("$diseaseName ($severity)")
                        }
                    }
                }

                // Check vaccination status
                val missedVaccines = mutableListOf<String>()
                val petAge = pet.age

                // Check for core vaccines based on age
                when {
                    petAge >= 6 && petAge <= 8 -> {
                        if (!hasVaccine(pet, "DHPP First Dose")) {
                            missedVaccines.add("DHPP First Dose (CRITICAL)")
                        }
                    }
                    petAge >= 10 && petAge <= 12 -> {
                        if (!hasVaccine(pet, "DHPP Second Dose")) {
                            missedVaccines.add("DHPP Second Dose (CRITICAL)")
                        }
                    }
                    petAge >= 14 && petAge <= 16 -> {
                        if (!hasVaccine(pet, "DHPP Final Booster")) {
                            missedVaccines.add("DHPP Final Booster (CRITICAL)")
                        }
                        if (!hasVaccine(pet, "Rabies First Dose")) {
                            missedVaccines.add("Rabies First Dose (LEGALLY REQUIRED)")
                        }
                    }
                }

                val emergencyActions = mutableListOf<String>()

                if (activeDiseases.isNotEmpty()) {
                    emergencyActions.add("🚨 URGENT: ${activeDiseases.size} active disease(s): ${activeDiseases.joinToString(", ")}")
                }

                if (missedVaccines.isNotEmpty()) {
                    emergencyActions.add("⚠️ CRITICAL: Missing vaccines: ${missedVaccines.joinToString(", ")}")
                }

                if (pet.health < 30) {
                    emergencyActions.add("💔 CRITICAL: Health is dangerously low (${pet.health}%)")
                }

                _error.value = if (emergencyActions.isNotEmpty()) {
                    "🏥 HEALTH ALERT: ${emergencyActions.joinToString(" | ")}"
                } else {
                    "✅ Emergency health check complete - ${pet.name} is healthy!"
                }

                _loading.value = false
            }
        }
    }

    /**
     * Helper function to check if pet has received a specific vaccine
     */
    private fun hasVaccine(pet: PetModel, vaccineName: String): Boolean {
        return pet.vaccineHistory.values.any { vaccineData ->
            val vaccineMap = vaccineData as? Map<String, Any>
            val name = vaccineMap?.get("vaccineName")?.toString() ?: ""
            name.contains(vaccineName, ignoreCase = true) || vaccineName.contains(name, ignoreCase = true)
        }
    }

    /**
     * NEW: Get active diseases that need treatment
     */
    fun getActiveDiseases(): List<Map<String, Any>> {
        val pet = _currentPet.value ?: return emptyList()
        val activeDiseases = mutableListOf<Map<String, Any>>()

        pet.diseaseHistory.forEach { (_, diseaseData) ->
            val diseaseMap = diseaseData as? Map<String, Any>
            if (diseaseMap != null) {
                val diagnosedDate = diseaseMap["diagnosedDate"] as? Long ?: 0L
                val daysSince = (System.currentTimeMillis() - diagnosedDate) / (1000 * 60 * 60 * 24)
                val status = diseaseMap["status"]?.toString()

                if (daysSince <= 30 && status != "TREATED") {
                    activeDiseases.add(diseaseMap + mapOf("daysSinceDiagnosis" to daysSince.toInt()))
                }
            }
        }

        return activeDiseases.sortedByDescending {
            val severity = it["severity"]?.toString() ?: "MILD"
            when (severity) {
                "SEVERE" -> 3
                "MODERATE" -> 2
                "MILD" -> 1
                else -> 0
            }
        }
    }

    /**
     * Get activities for current user's pets
     */
    suspend fun getUserActivities(userId: String): Result<List<com.example.tailstale.model.ActivityRecord>> {
        return activityRepository.getActivitiesByUserId(userId)
    }

    /**
     * Get recent activities with limit
     */
    suspend fun getRecentActivities(userId: String, limit: Int = 50): Result<List<com.example.tailstale.model.ActivityRecord>> {
        return activityRepository.getRecentActivities(userId, limit)
    }

    /**
     * Get activity statistics for a pet
     */
    suspend fun getActivityStats(petId: String, days: Int = 7): Result<Map<String, Int>> {
        return activityRepository.getActivityStats(petId, days)
    }

    /**
     * Clear the error state
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Direct stats update method for immediate UI feedback with activity tracking and achievement tracking
     */
    fun updatePetStatsDirect(petId: String, statsUpdate: Map<String, Any>, activityType: String = "", videoRes: String? = null) {
        viewModelScope.launch {
            try {
                // Update in repository first
                petRepository.updatePetStats(petId, statsUpdate).fold(
                    onSuccess = {
                        // Update local state immediately for responsive UI
                        _currentPet.value?.let { currentPet ->
                            if (currentPet.id == petId) {
                                val updatedPet = currentPet.copy(
                                    health = (statsUpdate["health"] as? Int) ?: currentPet.health,
                                    hunger = (statsUpdate["hunger"] as? Int) ?: currentPet.hunger,
                                    happiness = (statsUpdate["happiness"] as? Int) ?: currentPet.happiness,
                                    energy = (statsUpdate["energy"] as? Int) ?: currentPet.energy,
                                    cleanliness = (statsUpdate["cleanliness"] as? Int) ?: currentPet.cleanliness,
                                    lastFed = (statsUpdate["lastFed"] as? Long) ?: currentPet.lastFed,
                                    lastPlayed = (statsUpdate["lastPlayed"] as? Long) ?: currentPet.lastPlayed,
                                    lastCleaned = (statsUpdate["lastCleaned"] as? Long) ?: currentPet.lastCleaned,
                                    lastStatsDecay = (statsUpdate["lastStatsDecay"] as? Long) ?: currentPet.lastStatsDecay
                                )
                                _currentPet.value = updatedPet

                                // Also update in pets list
                                _pets.value = _pets.value.map { pet ->
                                    if (pet.id == petId) updatedPet else pet
                                }

                                // Update aging stats
                                updatePetAgingStats(updatedPet)

                                // Track achievement progress
                                trackAchievementAction(activityType, updatedPet)

                                // Record activity based on action type
                                val activityTypeEnum = when (activityType.lowercase()) {
                                    "feed" -> com.example.tailstale.model.ActivityType.FEEDING
                                    "play" -> com.example.tailstale.model.ActivityType.PLAYING
                                    "clean" -> com.example.tailstale.model.ActivityType.CLEANING
                                    "sleep" -> com.example.tailstale.model.ActivityType.SLEEPING
                                    "walk" -> com.example.tailstale.model.ActivityType.WALKING
                                    "sit" -> com.example.tailstale.model.ActivityType.SITTING
                                    "bath" -> com.example.tailstale.model.ActivityType.BATHING
                                    "health" -> com.example.tailstale.model.ActivityType.HEALTH_CHECK
                                    else -> null
                                }

                                // Record activity if type is recognized
                                activityTypeEnum?.let { type ->
                                    val statsChanged = mutableMapOf<String, Int>()
                                    statsUpdate.forEach { (key, value) ->
                                        if (value is Int && key in listOf("health", "hunger", "happiness", "energy", "cleanliness")) {
                                            val oldValue = when (key) {
                                                "health" -> currentPet.health
                                                "hunger" -> currentPet.hunger
                                                "happiness" -> currentPet.happiness
                                                "energy" -> currentPet.energy
                                                "cleanliness" -> currentPet.cleanliness
                                                else -> 0
                                            }
                                            val change = value - oldValue
                                            if (change != 0) statsChanged[key] = change
                                        }
                                    }

                                    // Create ActivityRecord and record it
                                    val activityRecord = com.example.tailstale.model.ActivityRecord(
                                        petId = updatedPet.id,
                                        petName = updatedPet.name,
                                        activityType = type,
                                        activityName = activityType,
                                        duration = 10000, // 10 seconds for video activities
                                        statsChanged = statsChanged,
                                        videoPlayed = videoRes,
                                        details = mapOf<String, Any>(
                                            "timestamp" to System.currentTimeMillis(),
                                            "actionSource" to "userInteraction"
                                        )
                                    )

                                    // Record activity using the repository
                                    viewModelScope.launch {
                                        activityRepository.recordActivity(activityRecord)
                                    }
                                }
                            }
                        }
                        _error.value = null
                    },
                    onFailure = { exception ->
                        _error.value = "Failed to update pet stats: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error updating pet stats: ${e.message}"
            }
        }
    }

    /**
     * Track achievement progress based on user actions
     */
    private fun trackAchievementAction(actionType: String, pet: PetModel) {
        viewModelScope.launch {
            try {
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                // Add debug logging
                Log.d("PetViewModel", "🎯 Tracking achievement action: $actionType for pet: ${pet.name}")

                val additionalData: Map<String, Any> = mapOf(
                    "petHealth" to pet.health,
                    "petId" to pet.id,
                    "petName" to pet.name
                )

                val newAchievements = achievementManager.updateUserStats(userId, actionType, additionalData)

                // Log the result
                Log.d("PetViewModel", "🏆 New achievements unlocked: ${newAchievements.size}")
                if (newAchievements.isNotEmpty()) {
                    Log.d("PetViewModel", "🎉 Achievements: ${newAchievements.map { it.name }}")
                }

                if (newAchievements.isNotEmpty()) {
                    _newlyUnlockedAchievements.value = newAchievements
                    // Show achievement notification
                    _error.value = "🎉 Achievement${if (newAchievements.size > 1) "s" else ""} Unlocked: ${newAchievements.joinToString(", ") { it.name }}!"
                }

                // Load updated user stats and log them
                val updatedStats = achievementManager.getUserStats(userId)
                Log.d("PetViewModel", "📊 Updated stats - Walk count: ${updatedStats.walkCount}, Feed count: ${updatedStats.feedCount}, Play count: ${updatedStats.playCount}")
                _userStats.value = updatedStats

            } catch (e: Exception) {
                Log.e("PetViewModel", "Failed to track achievement: ${e.message}", e)
            }
        }
    }

    /**
     * Load user achievement data
     */
    fun loadUserAchievements(userId: String) {
        viewModelScope.launch {
            try {
                val stats = achievementManager.getUserStats(userId)
                _userStats.value = stats
            } catch (e: Exception) {
                Log.e("PetViewModel", "Failed to load user achievements: ${e.message}")
            }
        }
    }

    /**
     * Clear newly unlocked achievements (after user has seen them)
     */
    fun clearNewlyUnlockedAchievements() {
        _newlyUnlockedAchievements.value = emptyList()
    }

    /**
     * Enhanced createPet method with achievement tracking
     */
    fun createPet(name: String, petType: PetType, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val pet = PetModel(
                    name = name,
                    type = petType.name,
                    age = 1,
                    ageInRealDays = 0,
                    lastAgeUpdate = System.currentTimeMillis(),
                    lastStatsDecay = System.currentTimeMillis()
                )

                petRepository.createPet(pet).fold(
                    onSuccess = { createdPet ->
                        viewModelScope.launch {
                            (petRepository as? com.example.tailstale.repo.PetRepositoryImpl)?.linkPetToUser(userId, createdPet.id)?.fold(
                                onSuccess = {
                                    _currentPet.value = createdPet
                                    updatePetHealth(createdPet)
                                    updatePetAgingStats(createdPet)

                                    // Track pet creation achievement
                                    trackAchievementAction("addPet", createdPet)

                                    loadUserPets(userId)
                                    _error.value = "Pet '${createdPet.name}' created successfully!"
                                    _loading.value = false
                                },
                                onFailure = { linkError ->
                                    _error.value = "Pet created but failed to link to user: ${linkError.message}"
                                    _loading.value = false
                                }
                            )
                        }
                    },
                    onFailure = { createError ->
                        _error.value = "Failed to create pet: ${createError.message}"
                        _loading.value = false
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error creating pet: ${e.message}"
                _loading.value = false
            }
        }
    }
}
