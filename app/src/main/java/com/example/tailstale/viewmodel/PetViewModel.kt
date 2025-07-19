package com.example.tailstale.viewmodel

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

    fun createPet(name: String, petType: PetType, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            val pet = PetModel(
                name = name,
                type = petType.name,
                age = 1, // Start at 1 month old
                ageInRealDays = 0,
                lastAgeUpdate = System.currentTimeMillis()
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
                    // Age up all pets and check for health issues
                    val updatedPets = petList.map { pet ->
                        val agedPet = petHealthService.ageUpPet(pet)

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

                        // Update pet age in repository if changed
                        if (agedPet.age != pet.age) {
                            petRepository.updatePet(agedPet)
                        }

                        agedPet
                    }

                    _pets.value = updatedPets
                    // Set current pet to first pet if none selected and pets exist
                    if (_currentPet.value == null && updatedPets.isNotEmpty()) {
                        _currentPet.value = updatedPets.first()
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
}
