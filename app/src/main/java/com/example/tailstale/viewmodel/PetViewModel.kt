package com.example.tailstale.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _pets = MutableLiveData<List<PetModel>>()
    val pets: LiveData<List<PetModel>> = _pets

    private val _currentPet = MutableLiveData<PetModel?>()
    val currentPet: LiveData<PetModel?> = _currentPet

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createPet(name: String, petType: PetType, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            val pet = PetModel(
                name = name,
                type = petType.name
            )

            petRepository.createPet(pet).fold(
                onSuccess = { createdPet ->
                    // Add pet to user's pet list
                    userRepository.getUserById(userId).fold(
                        onSuccess = { user ->
                            user?.let {
                                // Create a new user with updated pets list
                                val updatedUser = it.copy(
                                    pets = it.pets + createdPet.id
                                )
                                userRepository.updateUser(updatedUser)
                            }
                        },
                        onFailure = { }
                    )
                    _currentPet.value = createdPet
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
                onSuccess = {
                    _pets.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun selectPet(pet: PetModel) {
        _currentPet.value = pet
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

    fun vaccinatePet(vaccineId: String) {
        _currentPet.value?.let { pet ->
            viewModelScope.launch {
                val vaccineRecord = VaccineRecord(
                    vaccine = VaccineModel(
                        name = "Sample Vaccine",
                        description = "Sample Description",
                        targetDisease = "Sample Disease",
                        effectiveDurationDays = 365,
                        cost = 50,
                        compatiblePetTypes = setOf(PetType.valueOf(pet.type))
                    )
                )

                val vaccineRecordMap: Map<String, Any> = mapOf(
                    "vaccineName" to vaccineRecord.vaccine.name,
                    "description" to vaccineRecord.vaccine.description,
                    "targetDisease" to vaccineRecord.vaccine.targetDisease,
                    "effectiveDurationDays" to vaccineRecord.vaccine.effectiveDurationDays,
                    "cost" to vaccineRecord.vaccine.cost,
                    "dateAdministered" to vaccineRecord.dateAdministered
                )

                petRepository.addVaccineRecord(pet.id, vaccineRecord).fold(
                    onSuccess = {
                        val updatedPet = pet.copy(
                            vaccineHistory = pet.vaccineHistory + vaccineRecordMap
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
