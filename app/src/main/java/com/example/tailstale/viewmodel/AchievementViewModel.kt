package com.example.tailstale.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tailstale.model.Achievement
import com.example.tailstale.model.UserStats
import com.example.tailstale.service.AchievementManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementViewModel(
    private val achievementManager: AchievementManager
) : ViewModel() {

    private val _userStats = MutableStateFlow<UserStats?>(null)
    val userStats: StateFlow<UserStats?> = _userStats

    private val _unlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val unlockedAchievements: StateFlow<List<Achievement>> = _unlockedAchievements

    private val _allAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val allAchievements: StateFlow<List<Achievement>> = _allAchievements

    private val _achievementProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val achievementProgress: StateFlow<Map<String, Float>> = _achievementProgress

    private val _newlyUnlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val newlyUnlockedAchievements: StateFlow<List<Achievement>> = _newlyUnlockedAchievements

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Load user stats
                val stats = achievementManager.getUserStats(userId)
                _userStats.value = stats

                // Load unlocked achievements
                val unlocked = achievementManager.getUserAchievements(userId)
                _unlockedAchievements.value = unlocked

                // Load all available achievements
                val all = achievementManager.getAllAvailableAchievements()
                _allAchievements.value = all

                // Load progress
                val progress = achievementManager.getAchievementProgress(userId)
                _achievementProgress.value = progress

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load achievements: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun trackAction(userId: String, action: String, additionalData: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            try {
                val newAchievements = achievementManager.updateUserStats(userId, action, additionalData)

                if (newAchievements.isNotEmpty()) {
                    _newlyUnlockedAchievements.value = newAchievements

                    // Refresh data to show updated achievements
                    loadUserData(userId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to track action: ${e.message}"
            }
        }
    }

    fun clearNewlyUnlockedAchievements() {
        _newlyUnlockedAchievements.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    // Get achievements by category
    fun getAchievementsByCategory() = _allAchievements.value.groupBy { it.category }

    // Get locked achievements (not yet unlocked)
    fun getLockedAchievements() = _allAchievements.value.filter { achievement ->
        !(_userStats.value?.unlockedAchievements?.contains(achievement.id) ?: false)
    }

    // Get next achievable achievements (close to being unlocked)
    fun getNextAchievements(limit: Int = 3): List<Pair<Achievement, Float>> {
        val progress = _achievementProgress.value
        return getLockedAchievements()
            .mapNotNull { achievement ->
                val progressValue = progress[achievement.id] ?: 0f
                if (progressValue > 0f) achievement to progressValue else null
            }
            .sortedByDescending { it.second }
            .take(limit)
    }
}

class AchievementViewModelFactory(
    private val achievementManager: AchievementManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementViewModel(achievementManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
