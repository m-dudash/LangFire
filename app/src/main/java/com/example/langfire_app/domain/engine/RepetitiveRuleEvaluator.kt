package com.example.langfire_app.domain.engine

import com.example.langfire_app.domain.model.Behavior
import com.example.langfire_app.domain.model.Rule

object RepetitiveRuleEvaluator {

    fun evaluate(
        rule: Rule,
        currentBehavior: Behavior,
        historicalBehaviors: List<Behavior>
    ): Boolean {
        val conditions = rule.conditions

        if (currentBehavior.type != conditions.behaviorType) return false

        val allRelevantBehaviors = (historicalBehaviors.filter {
            it.type == conditions.behaviorType
        } + currentBehavior).distinctBy { it.id }

        val operator = conditions.operator ?: return false
        val thresholdStr = conditions.value ?: return false
        val threshold = thresholdStr.toDoubleOrNull() ?: return false

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
