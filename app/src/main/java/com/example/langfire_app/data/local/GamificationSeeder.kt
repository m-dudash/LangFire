package com.example.langfire_app.data.local

import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the database with default gamification rules and achievements.
 *
 * This class is called on first app launch to populate the rule system
 * with predefined gamification mechanics:
 * - Points for correct answers
 * - Daily streak achievements
 * - Word count milestones
 * - Accuracy achievements
 * - Speed achievements
 * - Rare wheel-of-fortune achievements
 *
 * All rules correspond to the gamification elements described in the thesis:
 * Body (points), Achievementy, Denná séria (daily streak), etc.
 */
@Singleton
class GamificationSeeder @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val achievementRepository: AchievementRepository
) {

    /**
     * Seed the database with default achievements and rules.
     * Should be called once on first app launch.
     *
     * @param profileId The ID of the user profile to create achievements for
     */
    suspend fun seedIfNeeded(profileId: Int) {
        val existingRules = ruleRepository.getAllRules()
        if (existingRules.isNotEmpty()) return // Already seeded

        val achievements = createDefaultAchievements(profileId)
        achievementRepository.saveAllAchievements(achievements)

        val rules = createDefaultRules()
        ruleRepository.saveAllRules(rules)
    }

    private fun createDefaultAchievements(profileId: Int): List<Achievement> {
        return listOf(
            // ══════════════════════════════════════
            // SESSION-BASED ACHIEVEMENTS (SIMPLE)
            // ══════════════════════════════════════

            Achievement(
                id = 1,
                type = "accuracy",
                value = null,
                unlocked = false,
                description = "Perfekcionista: Dosiahni 100% správnosť v jednej relácii",
                profileId = profileId
            ),
            Achievement(
                id = 2,
                type = "speed",
                value = null,
                unlocked = false,
                description = "Rýchly študent: Ukonči reláciu za menej ako 2 minúty",
                profileId = profileId
            ),
            Achievement(
                id = 3,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Prvý krok: Odpovedz správne na 10 slov v jednej relácii",
                profileId = profileId
            ),

            // ══════════════════════════════════════
            // CUMULATIVE ACHIEVEMENTS (REPETITIVE)
            // ══════════════════════════════════════

            Achievement(
                id = 4,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Začiatočník: Celkovo 50 správnych odpovedí",
                profileId = profileId
            ),
            Achievement(
                id = 5,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Pokročilý: Celkovo 200 správnych odpovedí",
                profileId = profileId
            ),
            Achievement(
                id = 6,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Expert: Celkovo 500 správnych odpovedí",
                profileId = profileId
            ),
            Achievement(
                id = 7,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Majster slov: Celkovo 1000 správnych odpovedí",
                profileId = profileId
            ),
            Achievement(
                id = 8,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Maratónec: Ukonči 10 učebných relácií",
                profileId = profileId
            ),
            Achievement(
                id = 9,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Vytrvalec: Ukonči 50 učebných relácií",
                profileId = profileId
            ),

            // ══════════════════════════════════════
            // STREAK ACHIEVEMENTS (INTERVAL_REPETITIVE)
            // ══════════════════════════════════════

            Achievement(
                id = 10,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Prvý plameň: Denná séria 3 dni",
                profileId = profileId
            ),
            Achievement(
                id = 11,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Horí to!: Denná séria 7 dní",
                profileId = profileId
            ),
            Achievement(
                id = 12,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Nehasiaci oheň: Denná séria 14 dní",
                profileId = profileId
            ),
            Achievement(
                id = 13,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Mesačný plameň: Denná séria 30 dní",
                profileId = profileId
            ),
            Achievement(
                id = 14,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Neuhasiteľný: Denná séria 100 dní",
                profileId = profileId
            ),
            Achievement(
                id = 15,
                type = "streak",
                value = null,
                unlocked = false,
                description = "Ranný vtáčik: Študuj 5 po sebe idúcich dní pred 8:00",
                profileId = profileId
            ),

            // ══════════════════════════════════════
            // RARE ACHIEVEMENTS (from wheel of fortune)
            // ══════════════════════════════════════

            Achievement(
                id = 16,
                type = "rare",
                value = null,
                unlocked = false,
                description = "Šťastlivec: Vyhraj zriedkavú odmenu na kolese šťastia",
                profileId = profileId
            )
        )
    }

    private fun createDefaultRules(): List<Rule> {
        return listOf(
            // ══════════════════════════════════════
            // SIMPLE RULES
            // ══════════════════════════════════════

            // Rule 1: Perfect session (100% accuracy)
            Rule(
                id = 1,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "accuracy",
                    operator = "==",
                    value = "100"
                ),
                achievementId = 1
            ),

            // Rule 2: Speed session (under 120 seconds)
            Rule(
                id = 2,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "session_time",
                    operator = "<=",
                    value = "120"
                ),
                achievementId = 2
            ),

            // Rule 3: 10 correct in one session
            Rule(
                id = 3,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = ">=",
                    value = "10"
                ),
                achievementId = 3
            ),

            // ══════════════════════════════════════
            // REPETITIVE RULES
            // ══════════════════════════════════════

            // Rule 4: Total 50 correct answers
            Rule(
                id = 4,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "50"
                ),
                achievementId = 4
            ),

            // Rule 5: Total 200 correct answers
            Rule(
                id = 5,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "200"
                ),
                achievementId = 5
            ),

            // Rule 6: Total 500 correct answers
            Rule(
                id = 6,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "500"
                ),
                achievementId = 6
            ),

            // Rule 7: Total 1000 correct answers
            Rule(
                id = 7,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "1000"
                ),
                achievementId = 7
            ),

            // Rule 8: 10 sessions completed
            Rule(
                id = 8,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    operator = "count_>=",
                    value = "10"
                ),
                achievementId = 8
            ),

            // Rule 9: 50 sessions completed
            Rule(
                id = 9,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    operator = "count_>=",
                    value = "50"
                ),
                achievementId = 9
            ),

            // ══════════════════════════════════════
            // INTERVAL REPETITIVE RULES
            // ══════════════════════════════════════

            // Rule 10: 3-day streak
            Rule(
                id = 10,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 3,
                    consecutive = true
                ),
                achievementId = 10
            ),

            // Rule 11: 7-day streak
            Rule(
                id = 11,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 7,
                    consecutive = true
                ),
                achievementId = 11
            ),

            // Rule 12: 14-day streak
            Rule(
                id = 12,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 14,
                    consecutive = true
                ),
                achievementId = 12
            ),

            // Rule 13: 30-day streak
            Rule(
                id = 13,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 30,
                    consecutive = true
                ),
                achievementId = 13
            ),

            // Rule 14: 100-day streak
            Rule(
                id = 14,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 100,
                    consecutive = true
                ),
                achievementId = 14
            ),

            // Rule 15: Early bird — 5 consecutive days with morning study (before 8:00)
            Rule(
                id = 15,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "morning_study",
                    interval = "daily",
                    repeatCount = 5,
                    consecutive = true
                ),
                achievementId = 15
            ),

            // Rule 16: Rare wheel of fortune achievement (triggered by type match)
            Rule(
                id = 16,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "wheel_rare_win",
                    attribute = "won",
                    operator = "==",
                    value = "true"
                ),
                achievementId = 16
            )
        )
    }
}
