package com.example.tailstale.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tailstale.repo.AuthRepository
import com.example.tailstale.repo.AuthRepositoryImpl
import com.example.tailstale.repo.PetRepository
import com.example.tailstale.repo.PetRepositoryImpl
import com.example.tailstale.repo.UserRepository
import com.example.tailstale.repo.UserRepositoryImpl
import com.example.tailstale.viewmodel.AuthViewModel
import com.example.tailstale.viewmodel.PetViewModel
import com.example.tailstale.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

object AppModule {
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun provideUserRepository(): UserRepository = UserRepositoryImpl()

    fun providePetRepository(): PetRepository = PetRepositoryImpl()

    fun provideAuthRepository(
        auth: FirebaseAuth = provideFirebaseAuth(),
        userRepository: UserRepository = provideUserRepository()
    ): AuthRepository = AuthRepositoryImpl(auth, userRepository)

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        AuthViewModel(
                            provideAuthRepository(),
                            providePetRepository()
                        ) as T
                    }
                    modelClass.isAssignableFrom(PetViewModel::class.java) -> {
                        PetViewModel(
                            providePetRepository(),
                            provideUserRepository()
                        ) as T
                    }
                    modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                        UserViewModel(provideUserRepository()) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }

    fun providePetViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(PetViewModel::class.java) -> {
                        PetViewModel(
                            providePetRepository(),
                            provideUserRepository()
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}