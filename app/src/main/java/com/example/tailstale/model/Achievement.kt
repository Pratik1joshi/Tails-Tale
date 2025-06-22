package com.example.tailstale.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val rewardCoins: Int = 0,
    val rewardGems: Int = 0,
    var isUnlocked: Boolean = false,
    var dateUnlocked: Long? = null
)