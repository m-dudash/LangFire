package com.example.langfire_app.domain.model

data class Rule(
    val id: Int = 0,
    val type: RuleType,
    val conditions: RuleConditions,
    val achievementId: Int,
    val xpReward: Int = 0,
    val grantsFreeze: Boolean = false
)

enum class RuleType {
    SIMPLE,
    REPETITIVE,
    INTERVAL_REPETITIVE
}

data class RuleConditions(
    val behaviorType: String,
    val attribute: String? = null,
    val operator: String? = null,
    val value: String? = null,
    val interval: String? = null,
    val repeatCount: Int? = null,
    val consecutive: Boolean = false,
    val xpAttribute: String? = null,
    val repeatableXp: Boolean = false
)
