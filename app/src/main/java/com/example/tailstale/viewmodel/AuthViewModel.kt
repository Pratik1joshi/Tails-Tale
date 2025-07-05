package com.example.tailstale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.PetModel
import com.example.tailstale.model.PetType
import com.example.tailstale.model.UserModel
import com.example.tailstale.repo.AuthRepository
import com.example.tailstale.repo.PetRepository
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _loading.value = true
            authRepository.getCurrentUser().fold(
                onSuccess = {
                    _currentUser.value = it
                    _isSignedIn.value = it != null
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.signInWithEmail(email, password).fold(
                onSuccess = {
                    _currentUser.value = it
                    _isSignedIn.value = true
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.signUpWithEmail(email, password, displayName).fold(
                onSuccess = {
                    _currentUser.value = it
                    _isSignedIn.value = true
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.signInWithGoogle(credential).fold(
                onSuccess = {
                    _currentUser.value = it
                    _isSignedIn.value = true
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    fun signInWithGithub(credential: AuthCredential) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.signInWithGithub(credential).fold(
                onSuccess = {
                    _currentUser.value = it
                    _isSignedIn.value = true
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.resetPassword(email).fold(
                onSuccess = {
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
            _loading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().fold(
                onSuccess = {
                    _currentUser.value = null
                    _isSignedIn.value = false
                    _error.value = null
                },
                onFailure = {
                    _error.value = it.message
                }
            )
        }
    }

    fun signUpWithEmailAndPet(
        email: String,
        password: String,
        displayName: String,
        petType: PetType?,
        petName: String?
    ) {
        viewModelScope.launch {
            _loading.value = true
            authRepository.signUpWithEmail(email, password, displayName).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _isSignedIn.value = true
                    _error.value = null

                    // Create initial pet if provided
                    if (petType != null && petName != null && petName.isNotBlank()) {
                        createInitialPet(user.id, petType, petName)
                    }
                },
                onFailure = {
                    _error.value = it.message
                    _isSignedIn.value = false
                }
            )
            _loading.value = false
        }
    }

    private suspend fun createInitialPet(userId: String, petType: PetType, petName: String) {
        try {
            val pet = PetModel(
                name = petName,
                type = petType
            )

            petRepository.createPet(pet).fold(
                onSuccess = { createdPet ->
                    // Pet created successfully, it will be automatically linked to user
                },
                onFailure = { exception ->
                    // Log error but don't fail the signup process
                    _error.value = "Account created but failed to create pet: ${exception.message}"
                }
            )
        } catch (e: Exception) {
            // Log error but don't fail signup
            _error.value = "Account created but failed to create pet: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setError(message: String) {
        _error.value = message
    }
}