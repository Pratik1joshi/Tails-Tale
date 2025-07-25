package com.example.tailstale.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// Theme colors
object AppColors {
    // Light theme colors
    val LightPrimary = Color(0xFF007AFF)
    val LightSecondary = Color(0xFFFF9500)
    val LightBackground = Color(0xFFF5F5F5)
    val LightSurface = Color.White
    val LightOnSurface = Color.Black
    val LightOnBackground = Color.Black
    val LightOutline = Color.Gray

    // Dark theme colors
    val DarkPrimary = Color(0xFF0A84FF)
    val DarkSecondary = Color(0xFFFF9F0A)
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkOnSurface = Color.White
    val DarkOnBackground = Color.White
    val DarkOutline = Color(0xFF787878)
}

// Theme preference state
object ThemeState {
    private var _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
}

@Composable
fun VirtualPetTheme(
    darkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = AppColors.DarkPrimary,
            secondary = AppColors.DarkSecondary,
            background = AppColors.DarkBackground,
            surface = AppColors.DarkSurface,
            onSurface = AppColors.DarkOnSurface,
            onBackground = AppColors.DarkOnBackground,
            outline = AppColors.DarkOutline,
            surfaceVariant = Color(0xFF2D2D2D),
            onSurfaceVariant = Color(0xFFE0E0E0)
        )
    } else {
        lightColorScheme(
            primary = AppColors.LightPrimary,
            secondary = AppColors.LightSecondary,
            background = AppColors.LightBackground,
            surface = AppColors.LightSurface,
            onSurface = AppColors.LightOnSurface,
            onBackground = AppColors.LightOnBackground,
            outline = AppColors.LightOutline,
            surfaceVariant = Color(0xFFF8F8F8),
            onSurfaceVariant = Color(0xFF484848)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
