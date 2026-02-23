package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule
import java.util.Calendar
import java.util.TimeZone

/**
 * Evaluator for INTERVAL_REPETITIVE rules.
 *
 * Interval Repetitive rules check behavior patterns within time intervals.
 * They are used for mechanics like daily streaks, weekly goals, and
 * time-based achievements (e.g., "study 10 consecutive days").
 *
 * These rules are typically evaluated on events like app_open, where
 * the system needs to check whether the user has been consistently active.
 *
 * Flow:
 * 1. Verify the current behavior matches the expected type
 * 2. Group historical behaviors by the specified interval (daily/weekly)
 * 3. Count the number of intervals with at least one matching behavior
 * 4. If consecutive is required, find the longest consecutive streak ending at today
 * 5. Compare the count/streak against the required repeat_count
 *
 * Example Rule:
 *   type = INTERVAL_REPETITIVE
 *   conditions = {
 *     behaviorType: "app_open",
 *     interval: "daily",
 *     repeatCount: 10,
 *     consecutive: true
 *   }
 *
 * This rule triggers when the user has opened the app 10 consecutive days.
 */
object IntervalRepetitiveRuleEvaluator {

    /**
     * Evaluate whether the behavior pattern satisfies the interval repetitive rule.
     *
     * @param rule The rule to evaluate
     * @param currentBehavior The behavior just occurred (e.g., app_open today)
     * @param historicalBehaviors Past behaviors from the database
     * @return true if the interval repetition pattern satisfies the rule
     */
    fun evaluate(
        rule: Rule,
        currentBehavior: Behavior,
        historicalBehaviors: List<Behavior>
    ): Boolean {
        val conditions = rule.conditions

        // Step 1: Verify behavior type match
        if (currentBehavior.type != conditions.behaviorType) return false

        val interval = conditions.interval ?: return false
        val requiredCount = conditions.repeatCount ?: return false
        val requireConsecutive = conditions.consecutive

        // Step 2: Combine current + historical
        val allBehaviors = (historicalBehaviors.filter {
            it.type == conditions.behaviorType
        } + currentBehavior).distinctBy { it.id }

        // Step 3: Group by interval
        val intervalKeys = allBehaviors.map { behavior ->
            getIntervalKey(behavior.timestamp, interval)
        }.distinct().sorted()

        if (intervalKeys.isEmpty()) return false

        // Step 4: Evaluate based on consecutive requirement
        return if (requireConsecutive) {
            val currentKey = getIntervalKey(currentBehavior.timestamp, interval)
            val consecutiveCount = countConsecutiveEndingAt(intervalKeys, currentKey, interval)
            consecutiveCount >= requiredCount
        } else {
            // Non-consecutive: just count total unique intervals
            intervalKeys.size >= requiredCount
        }
    }

    /**
     * Get the current consecutive streak count.
     * Useful for updating profile streak_days and for UI display.
     *
     * @param behaviors List of behaviors to analyze
     * @param behaviorType The type of behavior to consider
     * @param interval The interval to group by ("daily" or "weekly")
     * @param currentTimestamp The reference timestamp (usually now)
     * @return The current consecutive streak count
     */
    fun getCurrentStreak(
        behaviors: List<Behavior>,
        behaviorType: String,
        interval: String = "daily",
        currentTimestamp: Long = System.currentTimeMillis()
    ): Int {
        val filteredBehaviors = behaviors.filter { it.type == behaviorType }
        if (filteredBehaviors.isEmpty()) return 0

        val intervalKeys = filteredBehaviors.map { getIntervalKey(it.timestamp, interval) }
            .distinct()
            .sorted()

        val currentKey = getIntervalKey(currentTimestamp, interval)
        return countConsecutiveEndingAt(intervalKeys, currentKey, interval)
    }

    /**
     * Convert a timestamp to an interval key.
     * For "daily" interval: returns day-of-year concatenated with year (e.g., "2024-045")
     * For "weekly" interval: returns week-of-year concatenated with year (e.g., "2024-W07")
     */
    private fun getIntervalKey(timestamp: Long, interval: String): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = timestamp
        }

        return when (interval) {
            "daily" -> {
                val year = calendar.get(Calendar.YEAR)
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                "$year-${dayOfYear.toString().padStart(3, '0')}"
            }
            "weekly" -> {
                val year = calendar.get(Calendar.YEAR)
                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                "$year-W${weekOfYear.toString().padStart(2, '0')}"
            }
            else -> timestamp.toString()
        }
    }

    /**
     * Count the number of consecutive interval keys ending at (or including) the target key.
     *
     * For example, if intervalKeys = ["2024-043", "2024-044", "2024-045"]
     * and targetKey = "2024-045", this returns 3.
     *
     * If there's a gap (e.g., "2024-043", "2024-045"), the consecutive count
     * ending at "2024-045" is 1.
     */
    private fun countConsecutiveEndingAt(
        intervalKeys: List<String>,
        targetKey: String,
        interval: String
    ): Int {
        // Extract numeric representations for consecutive comparison
        val keyValues = intervalKeys.mapNotNull { keyToNumeric(it, interval) }.distinct().sorted()
        val targetValue = keyToNumeric(targetKey, interval) ?: return 0

        // Target must be in the set (or we include it)
        val allValues = (keyValues + targetValue).distinct().sorted()

        // Count consecutive values ending at targetValue
        var count = 0
        var current = targetValue
        for (i in allValues.indices.reversed()) {
            if (allValues[i] == current) {
                count++
                current--
            } else if (allValues[i] < current) {
                // Gap found — break
                break
            }
        }

        return count
    }

    /**
     * Convert an interval key to a numeric value for consecutive comparison.
     * For daily intervals: year * 366 + dayOfYear
     * For weekly intervals: year * 53 + weekOfYear
     */
    private fun keyToNumeric(key: String, interval: String): Int? {
        return try {
            when (interval) {
                "daily" -> {
                    val parts = key.split("-")
                    val year = parts[0].toInt()
                    val day = parts[1].toInt()
                    year * 366 + day
                }
                "weekly" -> {
                    val parts = key.split("-W")
                    val year = parts[0].toInt()
                    val week = parts[1].toInt()
                    year * 53 + week
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
