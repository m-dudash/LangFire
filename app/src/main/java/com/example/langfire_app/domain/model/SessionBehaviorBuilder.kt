package com.example.langfire_app.domain.model

/**
 * Builds a domain-agnostic [Behavior] from session metrics.
 *
 * The caller (ViewModel / use-case) provides raw session data;
 * the builder packs it into the generic attribute map expected
 * by the Gamification Engine.
 */
object SessionBehaviorBuilder {

    /**
     * @param profileId            Active user profile
     * @param correctCount         Number of exercises answered correctly
     * @param forgotCount          Number of exercises answered incorrectly
     * @param totalExercises       Total exercises in the session
     * @param sessionDurationSec   Wall-clock duration of the session in seconds
     */
    fun build(
        profileId: Int,
        correctCount: Int,
        forgotCount: Int,
        totalExercises: Int,
        sessionDurationSec: Long
    ): Behavior {
        val accuracy = if (totalExercises > 0)
            ((correctCount.toDouble() / totalExercises) * 100).toInt()
        else 0

        return Behavior(
            type = "session_complete",
            profileId = profileId,
            attributes = mapOf(
                "correct_count"    to correctCount.toString(),
                "forgot_count"     to forgotCount.toString(),
                "total_exercises"  to totalExercises.toString(),
                "accuracy"         to accuracy.toString(),
                "session_time"     to sessionDurationSec.toString()
            )
        )
    }
}
