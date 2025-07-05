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
                type = petType
            )

            petRepository.createPet(pet).fold(
                onSuccess = { createdPet ->
                    // Add pet to user's pet list
                    userRepository.getUserById(userId).fold(
                        onSuccess = { user ->
                            user?.let {
                                it.pets.add(createdPet.id)
                                userRepository.updateUser(it)
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
                // Create care action
                val careAction = CareAction(
                    action = CareActionType.FEED,
                    itemUsed = foodId,
                    effectOnPet = mapOf("hunger" to -20, "happiness" to 5)
                )

                // Update pet stats
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
                            lastFed = System.currentTimeMillis()
                        )
                        updatedPet.careLog.add(careAction)
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
                            lastPlayed = System.currentTimeMillis()
                        )
                        updatedPet.careLog.add(careAction)
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
                            lastCleaned = System.currentTimeMillis()
                        )
                        updatedPet.careLog.add(careAction)
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
                // Here you would get vaccine details and create record
                val vaccineRecord = VaccineRecord(
                    vaccine = VaccineModel(
                        name = "Sample Vaccine",
                        description = "Sample Description",
                        targetDisease = "Sample Disease",
                        effectiveDurationDays = 365,
                        cost = 50,
                        compatiblePetTypes = setOf(pet.type)
                    )
                )

                petRepository.addVaccineRecord(pet.id, vaccineRecord).fold(
                    onSuccess = {
                        pet.vaccineHistory.add(vaccineRecord)
                        _currentPet.value = pet
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
