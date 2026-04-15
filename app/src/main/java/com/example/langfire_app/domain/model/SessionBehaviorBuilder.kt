package com.example.langfire_app.domain.model

object SessionBehaviorBuilder {

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
                "session_time"     to sessionDurationSec.toString(),
                "cumulative_already_tracked" to "true"
            )
        )
    }
}
