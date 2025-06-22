package com.example.tailstale.model

data class VaccineRecord(
    val vaccine: VaccineModel,
    val dateAdministered: Long = System.currentTimeMillis()
)