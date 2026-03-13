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
 * - Session-based XP rewards
 *
 * XP values are stored in [Rule.xpReward] — the engine reads them from the
 * database at evaluation time rather than using hardcoded constants.
 */
@Singleton
class GamificationSeeder @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val achievementRepository: AchievementRepository,
    private val profileDao: com.example.langfire_app.data.local.dao.ProfileDao
) {

    /**
     * Test utility: Force-sets a profile into a state perfect for testing streak freezes.
     * Profile will have:
     * - 10 day streak
     * - 1 freeze
     * - Last activity 2 days ago (Gap found!)
     */
    suspend fun setupFreezeTest(profileId: Int) {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        
        val profile = profileDao.getById(profileId) ?: return
        
        profileDao.update(profile.copy(
            streakDays = 10,
            lastActiveDate = now - 2 * dayMs,
            streakFreezes = 1
        ))
    }

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

        // Setup test state for Freeze Bonus testing
        setupFreezeTest(profileId)
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
                id = 44,
                type = "unique_fortune_reward",
                value = null,
                unlocked = true,
                description = "TST",
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
                description = "Nehasiací oheň: Denná séria 14 dní",
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
            ),

            // ══════════════════════════════════════
            // SESSION XP ACHIEVEMENTS
            // ══════════════════════════════════════

            Achievement(
                id = 17,
                type = "session_xp",
                value = null,
                unlocked = false,
                description = "Dokončená relácia: Získaj XP za dokončenie relácie",
                profileId = profileId
            ),
            Achievement(
                id = 18,
                type = "session_xp",
                value = null,
                unlocked = false,
                description = "Správne odpovede: Získaj XP za každú správnu odpoveď",
                profileId = profileId
            ),
            Achievement(
                id = 19,
                type = "accuracy",
                value = null,
                unlocked = false,
                description = "Prvé správne: Odpovedz správne aspoň na 5 slov v jednej relácii",
                profileId = profileId
            ),
            Achievement(
                id = 20,
                type = "accuracy",
                value = null,
                unlocked = false,
                description = "Trojka: Dosiahni 80%+ správnosť v jednej relácii",
                profileId = profileId
            ),
            Achievement(
                id = 21,
                type = "word_count",
                value = null,
                unlocked = false,
                description = "Slovíčkový rekordér: Celkovo 2000 správnych odpovedí",
                profileId = profileId
            )
        )
    }

    private fun createDefaultRules(): List<Rule> {
        return listOf(
            // ══════════════════════════════════════
            // SIMPLE RULES
            // ══════════════════════════════════════

            // Rule 1: Perfect session (100% accuracy) → 30 XP
            Rule(
                id = 1,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "accuracy",
                    operator = "==",
                    value = "100"
                ),
                achievementId = 1,
                xpReward = 30
            ),

            // Rule 2: Speed session (under 120 seconds) → 25 XP
            Rule(
                id = 2,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "session_time",
                    operator = "<=",
                    value = "120"
                ),
                achievementId = 2,
                xpReward = 25
            ),

            // Rule 3: 10 correct in one session → 40 XP
            Rule(
                id = 3,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = ">=",
                    value = "10"
                ),
                achievementId = 3,
                xpReward = 40
            ),

            // ══════════════════════════════════════
            // REPETITIVE RULES
            // ══════════════════════════════════════

            // Rule 4: Total 50 correct answers → 40 XP
            Rule(
                id = 4,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "50"
                ),
                achievementId = 4,
                xpReward = 40
            ),

            // Rule 5: Total 200 correct answers → 40 XP
            Rule(
                id = 5,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "200"
                ),
                achievementId = 5,
                xpReward = 40
            ),

            // Rule 6: Total 500 correct answers → 40 XP
            Rule(
                id = 6,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "500"
                ),
                achievementId = 6,
                xpReward = 40
            ),

            // Rule 7: Total 1000 correct answers → 40 XP
            Rule(
                id = 7,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "1000"
                ),
                achievementId = 7,
                xpReward = 40
            ),

            // Rule 8: 10 sessions completed → 20 XP
            Rule(
                id = 8,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    operator = "count_>=",
                    value = "10"
                ),
                achievementId = 8,
                xpReward = 20
            ),

            // Rule 9: 50 sessions completed → 20 XP
            Rule(
                id = 9,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    operator = "count_>=",
                    value = "50"
                ),
                achievementId = 9,
                xpReward = 20
            ),

            // ══════════════════════════════════════
            // INTERVAL REPETITIVE RULES
            // ══════════════════════════════════════

            // Rule 10: 3-day streak → 50 XP
            Rule(
                id = 10,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 3,
                    consecutive = true
                ),
                achievementId = 10,
                xpReward = 50
            ),

            // Rule 11: 7-day streak → 50 XP
            Rule(
                id = 11,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 7,
                    consecutive = true
                ),
                achievementId = 11,
                xpReward = 50
            ),

            // Rule 12: 14-day streak → 50 XP
            Rule(
                id = 12,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 14,
                    consecutive = true
                ),
                achievementId = 12,
                xpReward = 50
            ),

            // Rule 13: 30-day streak → 50 XP
            Rule(
                id = 13,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 30,
                    consecutive = true
                ),
                achievementId = 13,
                xpReward = 50
            ),

            // Rule 14: 100-day streak → 100 XP
            Rule(
                id = 14,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "daily_activity",
                    interval = "daily",
                    repeatCount = 100,
                    consecutive = true
                ),
                achievementId = 14,
                xpReward = 100
            ),

            // Rule 15: Early bird — 5 consecutive days with morning study → 50 XP
            Rule(
                id = 15,
                type = RuleType.INTERVAL_REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "morning_study",
                    interval = "daily",
                    repeatCount = 5,
                    consecutive = true
                ),
                achievementId = 15,
                xpReward = 50
            ),

            // Rule 16: Rare wheel of fortune achievement → 100 XP
            Rule(
                id = 16,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "wheel_rare_win",
                    attribute = "won",
                    operator = "==",
                    value = "true"
                ),
                achievementId = 16,
                xpReward = 100
            ),

            // ══════════════════════════════════════
            // SESSION XP RULES (new)
            // ══════════════════════════════════════

            // Rule 17: Base session XP — every completed session → 50 XP
            Rule(
                id = 17,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "total_exercises",
                    operator = ">=",
                    value = "1"
                ),
                achievementId = 17,
                xpReward = 50
            ),

            // Rule 18: Per-correct-answer XP — each correct → 5 XP (multiplied by correct_count)
            Rule(
                id = 18,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = ">=",
                    value = "1",
                    xpAttribute = "correct_count"
                ),
                achievementId = 18,
                xpReward = 5
            ),

            // Rule 19: At least 5 correct in one session → 20 XP
            Rule(
                id = 19,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = ">=",
                    value = "5"
                ),
                achievementId = 19,
                xpReward = 20
            ),

            // Rule 20: 80%+ accuracy in one session → 30 XP
            Rule(
                id = 20,
                type = RuleType.SIMPLE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "accuracy",
                    operator = ">=",
                    value = "80"
                ),
                achievementId = 20,
                xpReward = 30
            ),

            // Rule 21: Total 2000 correct answers → 40 XP
            Rule(
                id = 21,
                type = RuleType.REPETITIVE,
                conditions = RuleConditions(
                    behaviorType = "session_complete",
                    attribute = "correct_count",
                    operator = "sum_>=",
                    value = "2000"
                ),
                achievementId = 21,
                xpReward = 40
            )
        )
    }
}
