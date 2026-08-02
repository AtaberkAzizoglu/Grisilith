package com.azizoglu.grisilith.summary

import com.azizoglu.grisilith.statistics.StatType

data class RunSummaryStatEntry(
    val statType: StatType,
    val label: String,
    val totalValue: Int,
    val contributions: List<StatSourceContribution>
) {

    init {
        require(
            contributions.sumOf {
                it.amount
            } ==
                totalValue
        ) {
            "Summary stat contribution total must equal totalValue."
        }
    }

    val nonZeroContributions:
        List<StatSourceContribution>
        get() =
            contributions.filter {
                it.amount != 0
            }
}
