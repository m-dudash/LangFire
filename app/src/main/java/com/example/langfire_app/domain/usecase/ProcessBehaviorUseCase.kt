package com.example.langfire_app.domain.usecase

import com.example.langfire_app.domain.engine.GamificationEngine
import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.EngineResult
import javax.inject.Inject

/**
 * Use case: Process a behavior through the Gamification Engine.
 *
 * This is the primary entry point from the Presentation Layer to the
 * gamification system. It is called after relevant user interactions:
 *
 * - After a learning session completes
 * - When the app is opened (for streak tracking)
 * - When a word is marked as learned
 *
 * Usage example from ViewModel:
 * ```
 * val behavior = Behavior(
 *     type = "session_complete",
 *     attributes = mapOf(
 *         "correct_count" to "8",
 *         "incorrect_count" to "2",
 *         "session_time" to "300"
 *     ),
 *     profileId = currentProfileId
 * )
 * val result = processBehaviorUseCase(behavior)
 * // result.newAchievements → show achievement popup
 * // result.xpGranted → animate XP bar
 * // result.streakUpdated → update fire animation
 * ```
 */
class ProcessBehaviorUseCase @Inject constructor(
    private val gamificationEngine: GamificationEngine
) {
    /**
     * Process the behavior and return the gamification result.
     */
    suspend operator fun invoke(behavior: Behavior): EngineResult {
        return gamificationEngine.processBehavior(behavior)
    }
}
