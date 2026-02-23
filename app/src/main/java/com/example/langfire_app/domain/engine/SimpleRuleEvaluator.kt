package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule
import com.example.langfire_app.domain.model.RuleConditions

/**
 * Evaluator for SIMPLE rules.
 *
 * Simple rules perform an immediate check against the current behavior's attributes.
 * They do not require any historical data — only the incoming behavior is considered.
 *
 * Flow:
 * 1. Check if the behavior type matches the rule's expected behavior type
 * 2. Extract the specified attribute value from the behavior
 * 3. Compare the attribute value against the rule's threshold using the specified operator
 *
 * Example Rule:
 *   type = SIMPLE
 *   conditions = {
 *     behaviorType: "session_complete",
 *     attribute: "correct_count",
 *     operator: ">=",
 *     value: "10"
 *   }
 *
 * This rule triggers when a session ends with 10+ correct answers.
 */
object SimpleRuleEvaluator {

    /**
     * Evaluate whether the given behavior satisfies the simple rule.
     *
     * @param rule The rule to evaluate
     * @param behavior The current behavior to check
     * @return true if the behavior satisfies the rule conditions
     */
    fun evaluate(rule: Rule, behavior: Behavior): Boolean {
        val conditions = rule.conditions

        // Step 1: Check behavior type match
        if (behavior.type != conditions.behaviorType) return false

        // Step 2: Extract attribute value
        val attribute = conditions.attribute ?: return false
        val operator = conditions.operator ?: return false
        val thresholdStr = conditions.value ?: return false

        val actualValueStr = behavior.attributes[attribute] ?: return false

        // Step 3: Compare using the operator
        return compareValues(actualValueStr, operator, thresholdStr)
    }

    /**
     * Compare two string values using the specified operator.
     * Values are first attempted to be parsed as numbers (Double).
     * If parsing fails, string comparison is used.
     */
    private fun compareValues(actual: String, operator: String, threshold: String): Boolean {
        val actualNum = actual.toDoubleOrNull()
        val thresholdNum = threshold.toDoubleOrNull()

        // Numeric comparison
        if (actualNum != null && thresholdNum != null) {
            return when (operator) {
                ">=" -> actualNum >= thresholdNum
                "<=" -> actualNum <= thresholdNum
                ">"  -> actualNum > thresholdNum
                "<"  -> actualNum < thresholdNum
                "==" -> actualNum == thresholdNum
                "!=" -> actualNum != thresholdNum
                else -> false
            }
        }

        // String comparison (fallback)
        return when (operator) {
            "==" -> actual == threshold
            "!=" -> actual != threshold
            else -> false
        }
    }
}
