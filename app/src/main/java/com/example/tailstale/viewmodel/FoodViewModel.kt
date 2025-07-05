package com.example.tailstale.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.FoodCategory
import com.example.tailstale.model.FoodModel
import com.example.tailstale.model.PetType
import com.example.tailstale.repo.FoodRepository
import kotlinx.coroutines.launch

class FoodViewModel(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _foods = MutableLiveData<List<FoodModel>>()
    val foods: LiveData<List<FoodModel>> = _foods

    private val _filteredFoods = MutableLiveData<List<FoodModel>>()
    val filteredFoods: LiveData<List<FoodModel>> = _filteredFoods

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadAllFood()
    }

    private fun loadAllFood() {
        viewModelScope.launch {
            _loading.value = true
            foodRepository.getAllFood().fold(
                onSuccess = {
                    _foods.value = it
                    _filteredFoods.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun filterByCategory(category: FoodCategory) {
        viewModelScope.launch {
            foodRepository.getFoodByCategory(category).fold(
                onSuccess = {
                    _filteredFoods.value = it
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
            foodRepository.getFoodByPetType(petType).fold(
                onSuccess = {
                    _filteredFoods.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun searchFood(query: String) {
        if (query.isBlank()) {
            _filteredFoods.value = _foods.value
            return
        }

        viewModelScope.launch {
            foodRepository.searchFood(query).fold(
                onSuccess = {
                    _filteredFoods.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun filterByCostRange(minCost: Int, maxCost: Int) {
        viewModelScope.launch {
            foodRepository.getFoodByCostRange(minCost, maxCost).fold(
                onSuccess = {
                    _filteredFoods.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun resetFilters() {
        _filteredFoods.value = _foods.value
    }

    fun clearError() {
        _error.value = null
    }
}
