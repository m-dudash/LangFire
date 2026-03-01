package com.example.langfire_app.domain.model

sealed class FortuneReward {
    data class Xp(val amount: Int) : FortuneReward()
    data class Multiplier(val multiplier: Int) : FortuneReward()
    data object UniqueAchievement : FortuneReward()
}