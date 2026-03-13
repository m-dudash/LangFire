package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.FortuneReward
import javax.inject.Inject

class GetFortuneRewardsUseCase @Inject constructor(
    private val gamificationEngine: GamificationEngine
) {
    suspend operator fun invoke(profileId: Int): List<FortuneReward> {
        return gamificationEngine.getFortuneRewards(profileId)
    }
}
