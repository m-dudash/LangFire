package com.example.langfire_app.data.local

import android.util.Log
import com.example.langfire_app.domain.model.*
import com.example.langfire_app.domain.repository.AchievementRepository
import com.example.langfire_app.domain.repository.AppSeeder
import com.example.langfire_app.domain.repository.RuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationSeeder @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val achievementRepository: AchievementRepository
) : AppSeeder {

    override suspend fun seedForProfile(profileId: Int) {
        val existing = achievementRepository.getAchievementsByProfile(profileId)
        if (existing.isNotEmpty()) {
            Log.d("GamificationSeeder", "Already seeded for profileId=$profileId, skipping.")
            return
        }

        Log.d("GamificationSeeder", "Seeding gamification for profileId=$profileId ...")

        val achievements = buildAchievements(profileId)
        achievementRepository.saveAllAchievements(achievements)
        Log.d("GamificationSeeder", "Saved ${achievements.size} achievements.")

        val rules = buildRules()
        ruleRepository.saveAllRules(rules)
        Log.d("GamificationSeeder", "Saved ${rules.size} rules.")
    }

    override suspend fun seedRulesIfMissing() {
        val existingRules = ruleRepository.getAllRules()
        if (existingRules.isNotEmpty()) {
            Log.d("GamificationSeeder", "Rules already present (${existingRules.size}), skipping rule seed.")
            return
        }
        val rules = buildRules()
        ruleRepository.saveAllRules(rules)
        Log.d("GamificationSeeder", "Seeded ${rules.size} rules after cloud restore.")
    }

    private fun buildAchievements(profileId: Int): List<Achievement> = listOf(
        Achievement(
            id = 1, type = "accuracy", unlocked = false,
            title = "Perfectionist",
            description = "Achieve 100% accuracy in a single session",
            icon = "🎯",
            profileId = profileId
        ),
        Achievement(
            id = 2, type = "speed", unlocked = false,
            title = "Fast Learner",
            description = "Complete a session in 60 seconds or less",
            icon = "⚡",
            profileId = profileId
        ),
        Achievement(
            id = 3, type = "word_count", unlocked = false,
            title = "First Step",
            description = "Answer 20 words correctly in a single session",
            icon = "👣",
            profileId = profileId
        ),
        Achievement(
            id = 4, type = "word_count", unlocked = false,
            title = "Beginner",
            description = "Reach 75 total correct answers",
            icon = "🌱",
            profileId = profileId
        ),
        Achievement(
            id = 5, type = "word_count", unlocked = false,
            title = "Advanced",
            description = "Reach 300 total correct answers",
            icon = "📈",
            profileId = profileId
        ),
        Achievement(
            id = 6, type = "word_count", unlocked = false,
            title = "Expert",
            description = "Reach 700 total correct answers",
            icon = "🏆",
            profileId = profileId
        ),
        Achievement(
            id = 7, type = "word_count", unlocked = false,
            title = "Word Master",
            description = "Reach 1,500 total correct answers",
            icon = "👑",
            profileId = profileId
        ),
        Achievement(
            id = 8, type = "word_count", unlocked = false,
            title = "Marathoner",
            description = "Complete 15 learning sessions",
            icon = "🏃",
            profileId = profileId
        ),
        Achievement(
            id = 9, type = "word_count", unlocked = false,
            title = "Persistent",
            description = "Complete 75 learning sessions",
            icon = "🦾",
            profileId = profileId
        ),
        Achievement(
            id = 10, type = "streak", unlocked = false,
            title = "First Spark",
            description = "Maintain a 5-day streak",
            icon = "🔥",
            profileId = profileId
        ),
        Achievement(
            id = 11, type = "streak", unlocked = false,
            title = "On Fire!",
            description = "Maintain a 10-day streak",
            icon = "🔥",
            profileId = profileId
        ),
        Achievement(
            id = 12, type = "streak", unlocked = false,
            title = "Unstoppable Flame",
            description = "Maintain a 21-day streak",
            icon = "🔥",
            profileId = profileId
        ),
        Achievement(
            id = 13, type = "streak", unlocked = false,
            title = "Fire Blaze",
            description = "Maintain a 45-day streak",
            icon = "🔥",
            profileId = profileId
        ),
        Achievement(
            id = 14, type = "streak", unlocked = false,
            title = "Extinguisher Proof",
            description = "Maintain a 120-day streak",
            icon = "💎",
            profileId = profileId
        ),
        Achievement(
            id = 15, type = "streak", unlocked = false,
            title = "Early Bird",
            description = "Study before 8:00 AM for 7 consecutive days",
            icon = "🐦",
            profileId = profileId
        ),
        Achievement(
            id = 16, type = "rare", unlocked = false,
            title = "Lucky One",
            description = "Win a rare reward from the wheel of fortune",
            icon = "🎰",
            profileId = profileId
        ),
        Achievement(
            id = 17, type = "xp_only", unlocked = false,
            title = "Session Complete",
            description = "Complete a full session of at least 8 exercises",
            icon = "✅",
            profileId = profileId
        ),
        Achievement(
            id = 18, type = "xp_only", unlocked = false,
            title = "Correct Answers",
            description = "Score at least 3 correct answers in a session",
            icon = "⭐",
            profileId = profileId
        ),
        Achievement(
            id = 19, type = "accuracy", unlocked = false,
            title = "First Success",
            description = "Answer at least 10 words correctly in a single session",
            icon = "✔️",
            profileId = profileId
        ),
        Achievement(
            id = 20, type = "accuracy", unlocked = false,
            title = "Accurate Mind",
            description = "Achieve 90%+ accuracy in a single session",
            icon = "🎖️",
            profileId = profileId
        ),
        Achievement(
            id = 21, type = "word_count", unlocked = false,
            title = "Vocabulary Legend",
            description = "Reach 3,000 total correct answers",
            icon = "📚",
            profileId = profileId
        ),
        Achievement(
            id = 22, type = "streak_freeze", unlocked = false,
            title = "Keep Your Cool",
            description = "Earn a Streak Freeze for reaching a 10-day streak",
            icon = "🧊",
            profileId = profileId
        )
    )


    private fun buildRules(): List<Rule> = listOf(

        // Rule 1: 100% accuracy in a session = 30 XP
        Rule(
            id = 1, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "accuracy", operator = "==", value = "100",
                repeatableXp = true
            ),
            achievementId = 1, xpReward = 30
        ),

        // Rule 2: Session completed in ≤60 s = 20 XP
        Rule(
            id = 2, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "session_time", operator = "<=", value = "60",
                repeatableXp = true
            ),
            achievementId = 2, xpReward = 20
        ),

        // Rule 3: ≥20 correct in one session = 35 XP (one-time unlock)
        Rule(
            id = 3, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = ">=", value = "20"
            ),
            achievementId = 3, xpReward = 35
        ),

        // Rule 4: Cumulative 75 correct = 30 XP
        Rule(
            id = 4, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = "sum_>=", value = "75"
            ),
            achievementId = 4, xpReward = 30
        ),

        // Rule 5: Cumulative 300 correct = 40 XP
        Rule(
            id = 5, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = "sum_>=", value = "300"
            ),
            achievementId = 5, xpReward = 40
        ),

        // Rule 6: Cumulative 700 correct = 50 XP
        Rule(
            id = 6, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = "sum_>=", value = "700"
            ),
            achievementId = 6, xpReward = 50
        ),

        // Rule 7: Cumulative 1 500 correct = 60 XP
        Rule(
            id = 7, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = "sum_>=", value = "1500"
            ),
            achievementId = 7, xpReward = 60
        ),

        // Rule 8: 15 sessions completed = 25 XP
        Rule(
            id = 8, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                operator = "count_>=", value = "15"
            ),
            achievementId = 8, xpReward = 25
        ),

        // Rule 9: 75 sessions completed = 40 XP
        Rule(
            id = 9, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                operator = "count_>=", value = "75"
            ),
            achievementId = 9, xpReward = 40
        ),

        // Rule 10: 5-day streak = 35 XP
        Rule(
            id = 10, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 5, consecutive = true
            ),
            achievementId = 10, xpReward = 35
        ),

        // Rule 11: 10-day streak = 45 XP
        Rule(
            id = 11, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 10, consecutive = true
            ),
            achievementId = 11, xpReward = 45
        ),

        // Rule 12: 21-day streak = 55 XP
        Rule(
            id = 12, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 21, consecutive = true
            ),
            achievementId = 12, xpReward = 55
        ),

        // Rule 13: 45-day streak = 70 XP
        Rule(
            id = 13, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 45, consecutive = true
            ),
            achievementId = 13, xpReward = 70
        ),

        // Rule 14: 120-day streak = 120 XP
        Rule(
            id = 14, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 120, consecutive = true
            ),
            achievementId = 14, xpReward = 120
        ),

        // Rule 15: Early bird - 7 consecutive morning sessions = 67 XP
        Rule(
            id = 15, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "morning_study",
                interval = "daily", repeatCount = 7, consecutive = true
            ),
            achievementId = 15, xpReward = 67
        ),

        // Rule 16: Rare fortune wheel win = 100 XP
        Rule(
            id = 16, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "wheel_rare_win",
                attribute = "won", operator = "==", value = "true"
            ),
            achievementId = 16, xpReward = 100
        ),

        // Rule 17: Base session XP - session with at least 8 exercises = 10 XP
        Rule(
            id = 17, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "total_exercises", operator = ">=", value = "8",
                repeatableXp = true
            ),
            achievementId = 17, xpReward = 10
        ),

        // Rule 18: Per-correct-answer XP - each correct answer (from 3+) = 2 XP
        Rule(
            id = 18, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = ">=", value = "3",
                xpAttribute = "correct_count", repeatableXp = true
            ),
            achievementId = 18, xpReward = 2
        ),

        // Rule 19: ≥8 correct in one session = 10 XP
        Rule(
            id = 19, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = ">=", value = "10",
                repeatableXp = true
            ),
            achievementId = 19, xpReward = 10
        ),

        // Rule 20: 90%+ accuracy in one session = 15 XP
        Rule(
            id = 20, type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "accuracy", operator = ">=", value = "90",
                repeatableXp = true
            ),
            achievementId = 20, xpReward = 15
        ),

        // Rule 21: Cumulative 3 000 correct = 80 XP
        Rule(
            id = 21, type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count", operator = "sum_>=", value = "3000"
            ),
            achievementId = 21, xpReward = 80
        ),

        // Rule 22: 10-day streak = grants 1 Streak Freeze (repeatable every 10 days)
        Rule(
            id = 22, type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily", repeatCount = 10, consecutive = true,
                repeatableXp = true
            ),
            achievementId = 22, xpReward = 0, grantsFreeze = true
        )
    )
}
