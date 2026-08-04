package com.azizoglu.grisilith.combat.reaction

import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.SpellReactionType

data class SpellReactionResult(
    val resolvedDamage: Int,
    val reactionType: SpellReactionType? = null,
    val consumedCondition: ConditionType? = null
) {

    init {
        require(
            resolvedDamage >= 0
        ) {
            "Resolved reaction damage cannot be negative."
        }
    }

    val wasTriggered: Boolean
        get() =
            reactionType !=
                null
}
