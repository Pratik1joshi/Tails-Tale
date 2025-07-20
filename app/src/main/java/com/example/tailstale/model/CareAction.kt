package com.example.tailstale.model

import java.util.UUID

data class CareAction(
    val id: String = UUID.randomUUID().toString(),
    val action: CareActionType,
    val timestamp: Long = System.currentTimeMillis(),
    val itemUsed: String? = null,
    val effectOnPet: Map<String, Int> = emptyMap() // e.g., "happiness" to +10
)

enum class CareActionType(val displayName: String) {
    FEED("Feed"),
    PLAY("Play"),
    CLEAN("Clean"),
    EXERCISE("Exercise"),
    TREAT("Treat Disease"),
    VACCINATE("Vaccinate"),
    VET_VISIT("Vet Visit")
}