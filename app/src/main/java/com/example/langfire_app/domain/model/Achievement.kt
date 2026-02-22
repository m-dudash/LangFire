package com.example.langfire_app.domain.model

/**
 * Domain model representing a gamification achievement (reward).
 *
 * Achievements are unlocked by the Gamification Engine when
 * rule conditions are met. They can represent medals, trophies,
 * badges, or any other form of reward.
 *
 * @param type The category of achievement, e.g. "streak", "accuracy", "word_count", "speed", "rare"
 * @param value Numeric value associated with the achievement (e.g., streak count, word count)
 * @param unlocked Whether the achievement has been unlocked
 * @param description Human-readable description of the achievement
 */
data class Achievement(
    val id: Int = 0,
    val type: String,
    val value: Int? = null,
    val unlocked: Boolean = false,
    val description: String? = null,
    val profileId: Int
)
