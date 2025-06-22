package com.example.tailstale.viewmodel

@HiltViewModel
class PetViewModel @Inject constructor(
    private val repository: PetRepository
) : ViewModel() {
    private val _pets = MutableLiveData<List<PetModel>>()
    val pets: LiveData<List<PetModel>> = _pets

    fun loadPets(userId: String) {
        viewModelScope.launch {
            _pets.value = repository.getAllPets(userId)
        }
    }

    fun addPet(userId: String, pet: PetModel) {
        viewModelScope.launch {
            repository.createPet(userId, pet)
            loadPets(userId) // Refresh
        }
    }

    fun updatePet(userId: String, pet: PetModel) {
        viewModelScope.launch {
            repository.updatePet(userId, pet)
            loadPets(userId) // Refresh
        }
    }
}