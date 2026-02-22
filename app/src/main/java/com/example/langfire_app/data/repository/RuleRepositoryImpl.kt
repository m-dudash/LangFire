package com.example.langfire_app.data.repository

import com.example.langfire_app.data.local.dao.RuleDao
import com.example.langfire_app.data.local.mappers.EntityMappers.toDomain
import com.example.langfire_app.data.local.mappers.EntityMappers.toEntity
import com.example.langfire_app.domain.model.Rule
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleRepositoryImpl @Inject constructor(
    private val ruleDao: RuleDao
) : RuleRepository {

    override suspend fun getAllRules(): List<Rule> {
        return ruleDao.getAll().map { it.toDomain() }
    }

    override suspend fun getRuleById(id: Int): Rule? {
        return ruleDao.getById(id)?.toDomain()
    }

    override suspend fun getRulesByType(type: String): List<Rule> {
        return ruleDao.getByType(type).map { it.toDomain() }
    }

    override suspend fun getRulesByAchievementId(achievementId: Int): List<Rule> {
        return ruleDao.getByAchievementId(achievementId).map { it.toDomain() }
    }

    override suspend fun saveRule(rule: Rule): Long {
        return ruleDao.insert(rule.toEntity())
    }

    override suspend fun saveAllRules(rules: List<Rule>) {
        ruleDao.insertAll(rules.map { it.toEntity() })
    }

    override suspend fun deleteAllRules() {
        ruleDao.deleteAll()
    }
}
