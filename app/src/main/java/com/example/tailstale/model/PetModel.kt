package com.example.tailstale.model

import java.util.UUID

data class PetModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: String = "Dog",
    var age: Int = 0,
    var growthStage: String = "Baby",
    var happiness: Int = 100,
    var health: Int = 100,
    var hunger: Int = 0,
    var energy: Int = 100,
    var weight: Double = 1.0,
    var activeDisease: DiseaseModel? = null,
    val vaccineHistory: MutableList<VaccineRecord> = mutableListOf()
)