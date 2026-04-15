package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule
import com.example.langfire_app.domain.model.RuleConditions

object SimpleRuleEvaluator {

    fun evaluate(rule: Rule, behavior: Behavior): Boolean {
        val conditions = rule.conditions
        if (behavior.type != conditions.behaviorType) return false
        val attribute = conditions.attribute ?: return false
        val operator = conditions.operator ?: return false
        val thresholdStr = conditions.value ?: return false
        val actualValueStr = behavior.attributes[attribute] ?: return false
        return compareValues(actualValueStr, operator, thresholdStr)
    }

    private fun compareValues(actual: String, operator: String, threshold: String): Boolean {
        val actualNum = actual.toDoubleOrNull()
        val thresholdNum = threshold.toDoubleOrNull()
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
        return when (operator) {
            "==" -> actual == threshold
            "!=" -> actual != threshold
            else -> false
        }
    }
}
