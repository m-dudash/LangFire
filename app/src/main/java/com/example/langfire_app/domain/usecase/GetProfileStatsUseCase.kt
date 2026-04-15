package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.ProfileStats
import com.example.langfire_app.domain.repository.StatsRepository
import javax.inject.Inject

class GetProfileStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository
){
    suspend operator fun invoke(profileId: Int): ProfileStats {
        return statsRepository.getProfileStats(profileId)
    }
}

