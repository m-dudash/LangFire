package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule

/**
 * Evaluator for REPETITIVE rules.
 *
 * Repetitive rules check conditions that require accumulated historical data.
 * They aggregate attribute values across multiple past behaviors to determine
 * if a threshold has been reached.
 *
 * Supported aggregate operators:
 * - "sum_>=" : Sum of attribute values across all matching behaviors >= threshold
 * - "sum_<=" : Sum of attribute values <= threshold
 * - "count_>=" : Count of matching behaviors >= threshold
 * - "count_==" : Count of matching behaviors == threshold
 * - "avg_>=" : Average of attribute values >= threshold
 *
 * Flow:
 * 1. Filter historical behaviors by type matching the rule
 * 2. Include the current behavior being processed
 * 3. Aggregate the specified attribute values using the specified operation
 * 4. Compare the aggregate against the threshold
 *
 * Example Rule:
 *   type = REPETITIVE
 *   conditions = {
 *     behaviorType: "session_complete",
 *     attribute: "correct_count",
 *     operator: "sum_>=",
 *     value: "100"
 *   }
 *
 * This rule triggers when the total correct answers across ALL sessions reaches 100.
 */
object RepetitiveRuleEvaluator {

    /**
     * Evaluate whether the current behavior + historical behaviors satisfy the repetitive rule.
     *
     * @param rule The rule to evaluate
     * @param currentBehavior The behavior just occurred
     * @param historicalBehaviors Past behaviors of the same profile (already filtered or full list)
     * @return true if the accumulated data satisfies the rule conditions
     */
    fun evaluate(
        rule: Rule,
        currentBehavior: Behavior,
        historicalBehaviors: List<Behavior>
    ): Boolean {
        val conditions = rule.conditions

        // Step 1: Check that current behavior matches the rule type
        if (currentBehavior.type != conditions.behaviorType) return false

        // Step 2: Combine current + historical behaviors of the same type
        val allRelevantBehaviors = (historicalBehaviors.filter {
            it.type == conditions.behaviorType
        } + currentBehavior).distinctBy { it.id }

        val operator = conditions.operator ?: return false
        val thresholdStr = conditions.value ?: return false
        val threshold = thresholdStr.toDoubleOrNull() ?: return false

        // Step 3: Determine which aggregate operation to perform
        return when {
            operator.startsWith("sum_") -> {
                val attribute = conditions.attribute ?: return false
                val sum = allRelevantBehaviors.sumOf { behavior ->
                    behavior.attributes[attribute]?.toDoubleOrNull() ?: 0.0
                }
                evaluateComparison(sum, operator.removePrefix("sum_"), threshold)
            }

            operator.startsWith("count_") -> {
                val count = allRelevantBehaviors.size.toDouble()
                evaluateComparison(count, operator.removePrefix("count_"), threshold)
            }

            operator.startsWith("avg_") -> {
                val attribute = conditions.attribute ?: return false
                val values = allRelevantBehaviors.mapNotNull { behavior ->
                    behavior.attributes[attribute]?.toDoubleOrNull()
                }
                if (values.isEmpty()) return false
                val avg = values.average()
                evaluateComparison(avg, operator.removePrefix("avg_"), threshold)
            }

            else -> false
        }
    }

    private fun evaluateComparison(value: Double, operator: String, threshold: Double): Boolean {
        return when (operator) {
            ">=" -> value >= threshold
            "<=" -> value <= threshold
            ">"  -> value > threshold
            "<"  -> value < threshold
            "==" -> value == threshold
            "!=" -> value != threshold
            else -> false
        }
    }
}
