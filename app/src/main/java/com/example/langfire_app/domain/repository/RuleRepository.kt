package com.example.langfire_app.domain.repository

import com.example.langfire_app.domain.model.Rule

interface RuleRepository {
    suspend fun getAllRules(): List<Rule>
    suspend fun getRuleById(id: Int): Rule?
    suspend fun getRulesByType(type: String): List<Rule>
    suspend fun getRulesByAchievementId(achievementId: Int): List<Rule>
    suspend fun saveRule(rule: Rule): Long
    suspend fun saveAllRules(rules: List<Rule>)
    suspend fun deleteAllRules()
}
