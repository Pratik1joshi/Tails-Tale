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

    fun clearError() {
        _error.value = null
    }
}
