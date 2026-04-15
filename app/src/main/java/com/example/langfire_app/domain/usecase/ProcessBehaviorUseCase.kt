package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.EngineResult
import javax.inject.Inject

class ProcessBehaviorUseCase @Inject constructor(
    private val gamificationEngine: GamificationEngine
) {
    suspend operator fun invoke(behavior: Behavior): EngineResult {
        return gamificationEngine.processBehavior(behavior)
    }
}
