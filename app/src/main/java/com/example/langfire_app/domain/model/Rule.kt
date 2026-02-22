package com.example.langfire_app.domain.model

/**
 * Domain model representing a gamification rule.
 *
 * Rules define the conditions under which achievements are granted.
 * They are stored with JSON conditions and evaluated by the Gamification Engine.
 *
 * Three types of rules are supported:
 *
 * 1. **SIMPLE** — Immediate check against current behavior attributes.
 *    Example: "If correct_count >= 10 in a single session → unlock achievement"
 *    Conditions JSON: {"behavior_type": "session_complete", "attribute": "correct_count", "operator": ">=", "value": "10"}
 *
 * 2. **REPETITIVE** — Check against accumulated history of behaviors.
 *    Example: "If total correct answers across all sessions >= 100 → unlock achievement"
 *    Conditions JSON: {"behavior_type": "session_complete", "attribute": "correct_count", "operator": "sum_>=", "value": "100"}
 *
 * 3. **INTERVAL_REPETITIVE** — Check behavior patterns within time intervals.
 *    Example: "If user has been active 10 consecutive days → unlock achievement"
 *    Conditions JSON: {"behavior_type": "app_open", "interval": "daily", "repeat_count": "10", "consecutive": "true"}
 *
 * @param type One of: "SIMPLE", "REPETITIVE", "INTERVAL_REPETITIVE"
 * @param conditions Parsed conditions from JSON defining the rule logic
 * @param achievementId The ID of the achievement to unlock/update when this rule is satisfied
 */
data class Rule(
    val id: Int = 0,
    val type: RuleType,
    val conditions: RuleConditions,
    val achievementId: Int
)

/**
 * Enum of supported rule types in the Gamification Engine.
 */
enum class RuleType {
    /** Immediate check against current behavior attributes */
    SIMPLE,
    /** Check against accumulated history of behaviors */
    REPETITIVE,
    /** Check behavior patterns within time intervals (e.g., daily streak) */
    INTERVAL_REPETITIVE
}

/**
 * Parsed conditions for a gamification rule.
 *
 * This data class represents the structured form of the JSON conditions
 * stored in the database. The Gamification Engine uses these to evaluate
 * whether a behavior (or set of behaviors) satisfies the rule.
 */
data class RuleConditions(
    /** The type of behavior this rule applies to (e.g., "session_complete", "app_open") */
    val behaviorType: String,

    /** The attribute key to check in the behavior's attributes map (for SIMPLE and REPETITIVE) */
    val attribute: String? = null,

    /** The comparison operator: ">=", "<=", ">", "<", "==", "sum_>=", "sum_<=", "count_>=" */
    val operator: String? = null,

    /** The threshold value to compare against */
    val value: String? = null,

    /** Time interval for INTERVAL_REPETITIVE rules: "daily", "weekly" */
    val interval: String? = null,

    /** Number of repetitions required for INTERVAL_REPETITIVE rules */
    val repeatCount: Int? = null,

    /** Whether repetitions must be consecutive (for INTERVAL_REPETITIVE) */
    val consecutive: Boolean = false
)
