package com.example.tailstale.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.UserModel
import com.example.tailstale.repo.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
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

    fun clearError() {
        _error.value = null
    }

    fun setError(message: String) {
        _error.value = message
    }
}