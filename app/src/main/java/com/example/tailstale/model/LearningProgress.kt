package com.example.tailstale.model

data class LearningProgress(
    val completedLessons: MutableSet<String> = mutableSetOf(),
    val careSkillLevels: MutableMap<CareActionType, Int> = mutableMapOf(),
    val totalCareActions: Int = 0,
    val petTypesExperienced: MutableSet<PetType> = mutableSetOf()
)