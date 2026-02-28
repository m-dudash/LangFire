package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Gamification Engine's rule evaluators.
 *
 * These tests verify the core logic of each rule type without
 * any Android or database dependencies.
 */
class SimpleRuleEvaluatorTest {

    @Test
    fun `SIMPLE rule - correct count >= 10 should pass`() {
        val rule = Rule(
            id = 1,
            type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count",
                operator = ">=",
                value = "10"
            ),
            achievementId = 1
        )

        val behavior = Behavior(
            id = 1,
            type = "session_complete",
            attributes = mapOf("correct_count" to "12"),
            profileId = 1
        )

        assertTrue(SimpleRuleEvaluator.evaluate(rule, behavior))
    }

    @Test
    fun `SIMPLE rule - correct count >= 10 should fail with 5`() {
        val rule = Rule(
            id = 1,
            type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count",
                operator = ">=",
                value = "10"
            ),
            achievementId = 1
        )

        val behavior = Behavior(
            id = 1,
            type = "session_complete",
            attributes = mapOf("correct_count" to "5"),
            profileId = 1
        )

        assertFalse(SimpleRuleEvaluator.evaluate(rule, behavior))
    }

    @Test
    fun `SIMPLE rule - wrong behavior type should fail`() {
        val rule = Rule(
            id = 1,
            type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count",
                operator = ">=",
                value = "10"
            ),
            achievementId = 1
        )

        val behavior = Behavior(
            id = 1,
            type = "app_open",
            attributes = mapOf("correct_count" to "15"),
            profileId = 1
        )

        assertFalse(SimpleRuleEvaluator.evaluate(rule, behavior))
    }

    @Test
    fun `SIMPLE rule - accuracy == 100 should pass`() {
        val rule = Rule(
            id = 2,
            type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "accuracy",
                operator = "==",
                value = "100"
            ),
            achievementId = 2
        )

        val behavior = Behavior(
            id = 1,
            type = "session_complete",
            attributes = mapOf("accuracy" to "100"),
            profileId = 1
        )

        assertTrue(SimpleRuleEvaluator.evaluate(rule, behavior))
    }

    @Test
    fun `SIMPLE rule - session_time <= 120 should pass for fast session`() {
        val rule = Rule(
            id = 3,
            type = RuleType.SIMPLE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "session_time",
                operator = "<=",
                value = "120"
            ),
            achievementId = 3
        )

        val behavior = Behavior(
            id = 1,
            type = "session_complete",
            attributes = mapOf("session_time" to "95"),
            profileId = 1
        )

        assertTrue(SimpleRuleEvaluator.evaluate(rule, behavior))
    }
}

class RepetitiveRuleEvaluatorTest {

    @Test
    fun `REPETITIVE rule - sum of correct_count >= 50 should pass`() {
        val rule = Rule(
            id = 1,
            type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count",
                operator = "sum_>=",
                value = "50"
            ),
            achievementId = 1
        )

        val currentBehavior = Behavior(
            id = 6,
            type = "session_complete",
            attributes = mapOf("correct_count" to "10"),
            profileId = 1
        )

        val history = listOf(
            Behavior(id = 1, type = "session_complete", attributes = mapOf("correct_count" to "12"), profileId = 1),
            Behavior(id = 2, type = "session_complete", attributes = mapOf("correct_count" to "8"), profileId = 1),
            Behavior(id = 3, type = "session_complete", attributes = mapOf("correct_count" to "15"), profileId = 1),
            Behavior(id = 4, type = "session_complete", attributes = mapOf("correct_count" to "7"), profileId = 1),
            Behavior(id = 5, type = "session_complete", attributes = mapOf("correct_count" to "5"), profileId = 1)
        )
        // Sum: 12 + 8 + 15 + 7 + 5 + 10 = 57 >= 50

        assertTrue(RepetitiveRuleEvaluator.evaluate(rule, currentBehavior, history))
    }

    @Test
    fun `REPETITIVE rule - sum of correct_count >= 50 should fail when not enough`() {
        val rule = Rule(
            id = 1,
            type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                attribute = "correct_count",
                operator = "sum_>=",
                value = "50"
            ),
            achievementId = 1
        )

        val currentBehavior = Behavior(
            id = 3,
            type = "session_complete",
            attributes = mapOf("correct_count" to "5"),
            profileId = 1
        )

        val history = listOf(
            Behavior(id = 1, type = "session_complete", attributes = mapOf("correct_count" to "10"), profileId = 1),
            Behavior(id = 2, type = "session_complete", attributes = mapOf("correct_count" to "8"), profileId = 1)
        )
        // Sum: 10 + 8 + 5 = 23 < 50

        assertFalse(RepetitiveRuleEvaluator.evaluate(rule, currentBehavior, history))
    }

    @Test
    fun `REPETITIVE rule - count of sessions >= 10 should pass`() {
        val rule = Rule(
            id = 2,
            type = RuleType.REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "session_complete",
                operator = "count_>=",
                value = "10"
            ),
            achievementId = 2
        )

        val currentBehavior = Behavior(
            id = 11,
            type = "session_complete",
            attributes = emptyMap(),
            profileId = 1
        )

        val history = (1..10).map {
            Behavior(id = it, type = "session_complete", attributes = emptyMap(), profileId = 1)
        }

        assertTrue(RepetitiveRuleEvaluator.evaluate(rule, currentBehavior, history))
    }
}

class IntervalRepetitiveRuleEvaluatorTest {

    @Test
    fun `INTERVAL rule - 3-day consecutive streak should pass`() {
        val rule = Rule(
            id = 1,
            type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily",
                repeatCount = 3,
                consecutive = true
            ),
            achievementId = 1
        )

        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val currentBehavior = Behavior(
            id = 3,
            type = "daily_activity",
            timestamp = now,
            profileId = 1
        )

        val history = listOf(
            Behavior(id = 1, type = "daily_activity", timestamp = now - 2 * dayMs, profileId = 1),
            Behavior(id = 2, type = "daily_activity", timestamp = now - dayMs, profileId = 1)
        )

        assertTrue(IntervalRepetitiveRuleEvaluator.evaluate(rule, currentBehavior, history))
    }

    @Test
    fun `INTERVAL rule - non-consecutive days should fail consecutive check`() {
        val rule = Rule(
            id = 1,
            type = RuleType.INTERVAL_REPETITIVE,
            conditions = RuleConditions(
                behaviorType = "daily_activity",
                interval = "daily",
                repeatCount = 3,
                consecutive = true
            ),
            achievementId = 1
        )

        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val currentBehavior = Behavior(
            id = 3,
            type = "daily_activity",
            timestamp = now,
            profileId = 1
        )

        // Gap: day 1, skip day 2, day 3 (today)
        val history = listOf(
            Behavior(id = 1, type = "daily_activity", timestamp = now - 3 * dayMs, profileId = 1)
        )

        assertFalse(IntervalRepetitiveRuleEvaluator.evaluate(rule, currentBehavior, history))
    }

    @Test
    fun `getCurrentStreak returns correct streak count`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val behaviors = listOf(
            Behavior(id = 1, type = "daily_activity", timestamp = now - 4 * dayMs, profileId = 1),
            Behavior(id = 2, type = "daily_activity", timestamp = now - 3 * dayMs, profileId = 1),
            Behavior(id = 3, type = "daily_activity", timestamp = now - 2 * dayMs, profileId = 1),
            Behavior(id = 4, type = "daily_activity", timestamp = now - dayMs, profileId = 1),
            Behavior(id = 5, type = "daily_activity", timestamp = now, profileId = 1)
        )

        val streak = IntervalRepetitiveRuleEvaluator.getCurrentStreak(
            behaviors = behaviors,
            behaviorType = "daily_activity",
            interval = "daily",
            currentTimestamp = now
        )

        assertEquals(5, streak)
    }
}
