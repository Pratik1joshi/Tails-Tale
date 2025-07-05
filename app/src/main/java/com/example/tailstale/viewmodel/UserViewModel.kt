package com.example.tailstale.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.Achievement
import com.example.tailstale.model.UserModel
import com.example.tailstale.repo.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableLiveData<UserModel?>()
    val currentUser: LiveData<UserModel?> = _currentUser

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _achievements = MutableLiveData<List<Achievement>>()
    val achievements: LiveData<List<Achievement>> = _achievements

    fun createUser(username: String, email: String, displayName: String) {
        viewModelScope.launch {
            _loading.value = true
            val user = UserModel(
                username = username,
                email = email,
                displayName = displayName
            )

            userRepository.createUser(user).fold(
                onSuccess = {
                    _currentUser.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            userRepository.getUserById(userId).fold(
                onSuccess = {
                    _currentUser.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            userRepository.updateUser(user).fold(
                onSuccess = {
                    _currentUser.value = it
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun addCoins(amount: Int) {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                val newCoins = user.coins + amount
                userRepository.updateCoins(user.id, newCoins).fold(
                    onSuccess = {
                        _currentUser.value = user.copy(coins = newCoins)
                        _error.value = null
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            }
        }
    }

    fun spendCoins(amount: Int): Boolean {
        return _currentUser.value?.let { user ->
            if (user.coins >= amount) {
                viewModelScope.launch {
                    val newCoins = user.coins - amount
                    userRepository.updateCoins(user.id, newCoins).fold(
                        onSuccess = {
                            _currentUser.value = user.copy(coins = newCoins)
                            _error.value = null
                        },
                        onFailure = {
                            _error.value = it.message
                        }
                    )
                }
                true
            } else {
                _error.value = "Not enough coins!"
                false
            }
        } ?: false
    }

    fun addExperience(amount: Int) {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                val newExp = user.experience + amount
                userRepository.updateExperience(user.id, newExp).fold(
                    onSuccess = {
                        val newLevel = (newExp / 100) + 1
                        _currentUser.value = user.copy(experience = newExp, level = newLevel)
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
