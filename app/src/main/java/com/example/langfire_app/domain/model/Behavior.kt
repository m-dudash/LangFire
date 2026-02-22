package com.example.langfire_app.domain.model

/**
 * Domain model representing a user behavior (interaction event).
 *
 * After each relevant interaction (e.g., session end, app open),
 * a Behavior is created with typed attributes and sent to the
 * Gamification Engine for rule evaluation.
 *
 * @param type The type of behavior, e.g. "session_complete", "app_open", "word_learned"
 * @param attributes Map of key-value pairs describing the behavior context,
 *        e.g. ["correct_count" = "8", "session_time" = "300"]
 */
data class Behavior(
    val id: Int = 0,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attributes: Map<String, String> = emptyMap(),
    val profileId: Int
)
