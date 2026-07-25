package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.statistics.StatType

object WeaponModifierFactory {

    fun create(
        statType: StatType,
        amount: Int
    ): StatModifier {
        return StatModifier(
            statType = statType,
            amount = amount
        )
    }
}
