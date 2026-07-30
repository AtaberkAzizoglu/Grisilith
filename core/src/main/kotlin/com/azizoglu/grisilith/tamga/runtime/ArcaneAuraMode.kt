package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.StatType

enum class ArcaneAuraMode(
    val statType: StatType,
    val damageType: DamageType
) {

    SHEPHERD(
        statType =
            StatType.SHEPHERD,

        damageType =
            DamageType.POISON
    ),

    ELEMENTAL_DAMAGE(
        statType =
            StatType.ELEMENTAL_DAMAGE,

        damageType =
            DamageType.WATER
    ),

    MIGHT_OF_NATURE(
        statType =
            StatType.MIGHT_OF_NATURE,

        damageType =
            DamageType.NECROTIC
    );

    companion object {

        fun fromStatType(
            statType: StatType
        ): ArcaneAuraMode? {

            return entries.firstOrNull { mode ->
                mode.statType ==
                    statType
            }
        }
    }
}
