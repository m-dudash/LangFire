package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule
import java.util.Calendar
import java.util.TimeZone

object IntervalRepetitiveRuleEvaluator {

    fun evaluate(
        rule: Rule, currentBehavior: Behavior,  historicalBehaviors: List<Behavior>
    ): Boolean {
        val conditions = rule.conditions
        if (currentBehavior.type != conditions.behaviorType) return false
        val interval = conditions.interval ?: return false
        val requiredCount = conditions.repeatCount ?: return false
        val requireConsecutive = conditions.consecutive
        val allBehaviors = (historicalBehaviors.filter {
            it.type == conditions.behaviorType
        } + currentBehavior).distinctBy { it.id }
        val intervalKeys = allBehaviors.map { behavior ->
            getIntervalKey(behavior.timestamp, interval)
        }.distinct().sorted()
        if (intervalKeys.isEmpty()) return false
        return if (requireConsecutive) {
            val currentKey = getIntervalKey(currentBehavior.timestamp, interval)
            val consecutiveCount = countConsecutiveEndingAt(intervalKeys, currentKey, interval)
            consecutiveCount >= requiredCount
        } else {
            intervalKeys.size >= requiredCount
        }
    }

    fun getCurrentStreak(
        behaviors: List<Behavior>,
        interval: String = "daily",
        currentTimestamp: Long = System.currentTimeMillis()
    ): Int {
        if (behaviors.isEmpty()) return 0

        val intervalKeys = behaviors.map { getIntervalKey(it.timestamp, interval) }
            .distinct()
            .sorted()

        val keyValues = intervalKeys.mapNotNull { keyToNumeric(it, interval) }.distinct().sorted()
        if (keyValues.isEmpty()) return 0

        val targetValue = keyToNumeric(getIntervalKey(currentTimestamp, interval), interval) ?: return 0

        val lastValidDay = keyValues.last()

        if (targetValue - lastValidDay > 1) {
            return 0
        }

        var count = 0
        var current = lastValidDay
        for (i in keyValues.indices.reversed()) {
            if (keyValues[i] == current) {
                count++
                current--
            } else {
                break
            }
        }
        return count
    }

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

    private fun countConsecutiveEndingAt(
        intervalKeys: List<String>,
        targetKey: String,
        interval: String
    ): Int {
        val keyValues = intervalKeys.mapNotNull { keyToNumeric(it, interval) }.distinct().sorted()
        val targetValue = keyToNumeric(targetKey, interval) ?: return 0

        val allValues = (keyValues + targetValue).distinct().sorted()

        var count = 0
        var current = targetValue
        for (i in allValues.indices.reversed()) {
            if (allValues[i] == current) {
                count++
                current--
            } else if (allValues[i] < current) {
                break
            }
        }

        return count
    }

    private fun keyToNumeric(key: String, interval: String): Int? {
        return try {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            when (interval) {
                "daily" -> {
                    val parts = key.split("-")
                    val year = parts[0].toInt()
                    val day = parts[1].toInt()
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.DAY_OF_YEAR, day)

                    val offset = cal.timeZone.getOffset(cal.timeInMillis)
                    ((cal.timeInMillis + offset) / (24 * 60 * 60 * 1000L)).toInt()
                }
                "weekly" -> {
                    val parts = key.split("-W")
                    val year = parts[0].toInt()
                    val week = parts[1].toInt()
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.WEEK_OF_YEAR, week)

                    val offset = cal.timeZone.getOffset(cal.timeInMillis)
                    (((cal.timeInMillis + offset) / (24 * 60 * 60 * 1000L)) / 7).toInt()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
