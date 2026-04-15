package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.model.UnitWordItem
import com.example.langfire_app.domain.repository.ProfileRepository
import com.example.langfire_app.domain.repository.UnitDetailsRepository
import javax.inject.Inject

data class UnitDetailsResult(
    val unitName: String,
    val words: List<UnitWordItem>
)

class GetUnitDetailsUseCase @Inject constructor(
    private val repository: UnitDetailsRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(unitId: Int): UnitDetailsResult? {
        val profile = profileRepository.getActiveProfile() ?: return null
        val unitName = repository.getUnitName(unitId) ?: "Unit Details"
        val words = repository.getWordsForUnit(unitId, profile.id)
            .sortedBy { it.knowledgeCoeff != null }
        return UnitDetailsResult(unitName = unitName, words = words)
    }
}
