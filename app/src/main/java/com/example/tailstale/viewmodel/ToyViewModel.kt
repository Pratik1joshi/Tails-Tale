package com.example.tailstale.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.PetType
import com.example.tailstale.model.ToyCategory
import com.example.tailstale.model.ToyModel
import com.example.tailstale.repo.ToyRepository
import kotlinx.coroutines.launch

class ToyViewModel(
    private val toyRepository: ToyRepository
) : ViewModel() {

    private val _toys = MutableLiveData<List<ToyModel>>()
    val toys: LiveData<List<ToyModel>> = _toys

    private val _filteredToys = MutableLiveData<List<ToyModel>>()
    val filteredToys: LiveData<List<ToyModel>> = _filteredToys

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllToys()
    }

    private fun loadAllToys() {
        viewModelScope.launch {
            _loading.value = true
            toyRepository.getAllToys().fold(
                onSuccess = {
                    _toys.value = it
                    _filteredToys.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun filterByCategory(category: ToyCategory) {
        viewModelScope.launch {
            toyRepository.getToysByCategory(category).fold(
                onSuccess = {
                    _filteredToys.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun filterByPetType(petType: PetType) {
        viewModelScope.launch {
            toyRepository.getToysByPetType(petType).fold(
                onSuccess = {
                    _filteredToys.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun searchToys(query: String) {
        if (query.isBlank()) {
            _filteredToys.value = _toys.value
            return
        }

        viewModelScope.launch {
            toyRepository.searchToys(query).fold(
                onSuccess = {
                    _filteredToys.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}
