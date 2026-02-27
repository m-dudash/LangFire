package com.example.langfire_app.data.local.mappers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.langfire_app.data.local.entities.*
import com.example.langfire_app.domain.model.*

/**
 * Mappers between Room entities and domain models.
 * Centralizes all conversion logic for Clean Architecture boundary crossing.
 */
object EntityMappers {

    private val gson = Gson()

    // ──────────────────────────────────────────────
    // Profile
    // ──────────────────────────────────────────────

    fun ProfileEntity.toDomain(): Profile = Profile(
        id = id,
        name = name,
        xp = xp,
        streakDays = streakDays,
        lastActiveDate = lastActiveDate
    )

    fun Profile.toEntity(): ProfileEntity = ProfileEntity(
        id = id,
        name = name,
        xp = xp,
        streakDays = streakDays,
        lastActiveDate = lastActiveDate
    )

    // ──────────────────────────────────────────────
    // Behavior
    // ──────────────────────────────────────────────

    fun BehaviorEntity.toDomain(): Behavior {
        val attributesMap: Map<String, String> = try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(attributes, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        return Behavior(
            id = id,
            type = type,
            timestamp = timestamp,
            attributes = attributesMap,
            profileId = profileId
        )
    }

    fun Behavior.toEntity(): BehaviorEntity = BehaviorEntity(
        id = id,
        type = type,
        timestamp = timestamp,
        attributes = gson.toJson(attributes),
        profileId = profileId
    )

    // ──────────────────────────────────────────────
    // Achievement
    // ──────────────────────────────────────────────

    fun AchievementEntity.toDomain(): Achievement = Achievement(
        id = id,
        type = type,
        value = value,
        unlocked = unlocked,
        description = description,
        icon = icon,
        title = title,
        profileId = profileId
    )

    fun Achievement.toEntity(): AchievementEntity = AchievementEntity(
        id = id,
        type = type,
        value = value,
        unlocked = unlocked,
        description = description,
        icon = icon,
        title = title,
        profileId = profileId
    )

    // ──────────────────────────────────────────────
    // Rule
    // ──────────────────────────────────────────────

    fun RuleEntity.toDomain(): Rule {
        val parsedConditions = try {
            gson.fromJson(conditions, RuleConditions::class.java)
        } catch (e: Exception) {
            RuleConditions(behaviorType = "unknown")
        }

        val ruleType = try {
            RuleType.valueOf(type.uppercase())
        } catch (e: Exception) {
            RuleType.SIMPLE
        }

        return Rule(
            id = id,
            type = ruleType,
            conditions = parsedConditions,
            achievementId = achievementId
        )
    }

    fun Rule.toEntity(): RuleEntity = RuleEntity(
        id = id,
        type = type.name,
        conditions = gson.toJson(conditions),
        achievementId = achievementId
    )

    // ──────────────────────────────────────────────
    // WordProgress
    // ──────────────────────────────────────────────

    fun WordProgressEntity.toDomain(): WordProgress = WordProgress(
        id = id,
        knowledgeCoeff = knowledgeCoeff,
        lastReviewed = lastReviewed,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        profileId = profileId,
        wordId = wordId
    )

    fun WordProgress.toEntity(): WordProgressEntity = WordProgressEntity(
        id = id,
        knowledgeCoeff = knowledgeCoeff,
        lastReviewed = lastReviewed,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        profileId = profileId,
        wordId = wordId
    )


    // ──────────────────────────────────────────────
    // Course
    // ──────────────────────────────────────────────

    fun CourseEntity.toDomain(): Course = Course(
        id = id,
        name = name,
        targetLang = targetLang,
        icon = icon ?: "🏳️"
    )

    fun Course.toEntity(): CourseEntity = CourseEntity(
        id = id,
        name = name,
        targetLang = targetLang,
        icon = icon
    )
}
