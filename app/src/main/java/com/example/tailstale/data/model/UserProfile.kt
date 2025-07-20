package com.example.tailstale.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val pets: List<Pet> = emptyList(),
    val stats: Stats = Stats(),
    val achievements: List<Achievement> = emptyList(),
    val settings: Settings = Settings(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    data class Pet(
        val id: String = "",
        val name: String = "",
        val type: String = "", // Dog, Cat, etc.
        val breed: String = "",
        val age: String = "",
        val gender: String = "",
        val weight: String = "",
        val birthday: String = "",
        val microchipId: String = "",
        val imageUrl: String = ""
    )

    data class Stats(
        val daysActive: Int = 0,
        val totalCare: Int = 0,
        val level: Int = 1
    )

    data class Achievement(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val icon: String = "",
        val unlockedAt: Long = System.currentTimeMillis()
    )

    data class Settings(
        val notifications: Boolean = true,
        val darkMode: Boolean = false,
        val locationServices: Boolean = false,
        val autoUpdates: Boolean = true,
        val soundEnabled: Boolean = true
    )
}
