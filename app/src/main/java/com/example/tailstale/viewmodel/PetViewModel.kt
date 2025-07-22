package com.example.tailstale.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.CareAction
import com.example.tailstale.model.CareActionType
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
import com.example.tailstale.model.VaccineModel
import com.example.tailstale.model.VaccineRecord
import com.example.tailstale.repo.PetRepository
import com.example.tailstale.repo.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository
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

    fun createPet(name: String, petType: PetType, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            val pet = PetModel(
                name = name,
                type = petType.name,
                age = 1, // Start at 1 month old
                ageInRealDays = 0,
                lastAgeUpdate = System.currentTimeMillis(),
                lastStatsDecay = System.currentTimeMillis()
            )

            petRepository.createPet(pet).fold(
                onSuccess = { createdPet ->
                    // Add pet to user's pet list
                    userRepository.getUserById(userId).fold(
                        onSuccess = { user ->
                            user?.let {
                                val updatedUser = it.copy(
                                    pets = it.pets + mapOf(createdPet.id to true)
                                )
                                userRepository.updateUser(updatedUser)
                            }
                        },
                        onFailure = { }
                    )
                    _currentPet.value = createdPet
                    updatePetHealth(createdPet) // Check initial health status
                    updatePetAgingStats(createdPet) // Initialize aging stats
                    loadUserPets(userId)
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

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

                        // Check for random disease if pet aged up
                        if (agedPet.age > pet.age) {
                            val randomDisease = petHealthService.checkForRandomDisease(agedPet)
                            randomDisease?.let { disease ->
                                // Apply disease effects
                                val affectedPet = agedPet.copy(
                                    health = maxOf(0, agedPet.health - disease.healthImpact),
                                    happiness = maxOf(0, agedPet.happiness - disease.happinessImpact),
                                    diseaseHistory = agedPet.diseaseHistory + mapOf(
                                        "diseaseName" to disease.name,
                                        "severity" to disease.severity.name,
                                        "diagnosedDate" to System.currentTimeMillis(),
                                        "treatmentCost" to disease.treatmentCost,
                                        "symptoms" to disease.symptoms
                                    )
                                )
                                petRepository.updatePet(affectedPet)
                                return@map affectedPet
                            }
                        }

                        agedPet
                    }

                    _pets.value = updatedPets
                    // Set current pet to first pet if none selected and pets exist
                    if (_currentPet.value == null && updatedPets.isNotEmpty()) {
                        _currentPet.value = updatedPets.first()
                        updatePetAgingStats(updatedPets.first())
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

    fun feedPet(foodId: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                val careAction = CareAction(
                    action = CareActionType.FEED,
                    itemUsed = foodId,
                    effectOnPet = mapOf("hunger" to -20, "happiness" to 5)
                )

                val careActionMap: Map<String, Any> = mapOf(
                    "action" to careAction.action.name,
                    "itemUsed" to (careAction.itemUsed ?: ""),
                    "effectOnPet" to careAction.effectOnPet,
                    "timestamp" to careAction.timestamp
                )

                val statsUpdate = mapOf(
                    "hunger" to maxOf(0, pet.hunger - 20),
                    "happiness" to minOf(100, pet.happiness + 5),
                    "lastFed" to System.currentTimeMillis()
                )

                petRepository.addCareAction(pet.id, careAction)
                petRepository.updatePetStats(pet.id, statsUpdate).fold(
                    onSuccess = {
                        val updatedPet = pet.copy(
                            hunger = maxOf(0, pet.hunger - 20),
                            happiness = minOf(100, pet.happiness + 5),
                            lastFed = System.currentTimeMillis(),
                            careLog = pet.careLog + careActionMap
                        )
                        _currentPet.value = updatedPet
                        _error.value = null
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            }
        }
    }

    fun playWithPet(toyId: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                val careAction = CareAction(
                    action = CareActionType.PLAY,
                    itemUsed = toyId,
                    effectOnPet = mapOf("happiness" to 15, "energy" to -10)
                )

                val careActionMap: Map<String, Any> = mapOf(
                    "action" to careAction.action.name,
                    "itemUsed" to (careAction.itemUsed ?: ""),
                    "effectOnPet" to careAction.effectOnPet,
                    "timestamp" to careAction.timestamp
                )

                val statsUpdate = mapOf(
                    "happiness" to minOf(100, pet.happiness + 15),
                    "energy" to maxOf(0, pet.energy - 10),
                    "lastPlayed" to System.currentTimeMillis()
                )

                petRepository.addCareAction(pet.id, careAction)
                petRepository.updatePetStats(pet.id, statsUpdate).fold(
                    onSuccess = {
                        val updatedPet = pet.copy(
                            happiness = minOf(100, pet.happiness + 15),
                            energy = maxOf(0, pet.energy - 10),
                            lastPlayed = System.currentTimeMillis(),
                            careLog = pet.careLog + careActionMap
                        )
                        _currentPet.value = updatedPet
                        _error.value = null
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            }
        }
    }

    fun cleanPet() {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                val careAction = CareAction(
                    action = CareActionType.CLEAN,
                    effectOnPet = mapOf("cleanliness" to 25, "happiness" to 5)
                )

                val careActionMap: Map<String, Any> = mapOf(
                    "action" to careAction.action.name,
                    "effectOnPet" to careAction.effectOnPet,
                    "timestamp" to careAction.timestamp
                )

                val statsUpdate = mapOf(
                    "cleanliness" to minOf(100, pet.cleanliness + 25),
                    "happiness" to minOf(100, pet.happiness + 5),
                    "lastCleaned" to System.currentTimeMillis()
                )

                petRepository.addCareAction(pet.id, careAction)
                petRepository.updatePetStats(pet.id, statsUpdate).fold(
                    onSuccess = {
                        val updatedPet = pet.copy(
                            cleanliness = minOf(100, pet.cleanliness + 25),
                            happiness = minOf(100, pet.happiness + 5),
                            lastCleaned = System.currentTimeMillis(),
                            careLog = pet.careLog + careActionMap
                        )
                        _currentPet.value = updatedPet
                        _error.value = null
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
    /**
     * Direct stats update method for immediate UI feedback
     */
    fun updatePetStatsDirect(petId: String, statsUpdate: Map<String, Any>) {
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
}
