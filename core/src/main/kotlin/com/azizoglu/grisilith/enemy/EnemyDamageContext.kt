package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType

data class EnemyDamageContext(
    val damageType: DamageType =
        DamageType.NONE,

    val sourceWeaponRuntimeId: String? =
        null,

    val sourceWeaponDefinitionId: String? =
        null,

    val sourceName: String? =
        null,

    val sourceWeaponTags: Set<ContentTag> =
        emptySet(),

    val attackerPiercingIgnoreRatio: Float =
        0f,

    val sourceIsSummon: Boolean = false,

    val projectileTravelProgress: Float = 0f,

    val conditionStatScore: Int? = null,
    val appliesAutomaticCondition: Boolean = true,
    val projectileAttack: Boolean = false,
    val defenseTimeScale: Float = 1f
) {

    val isPhysicalWeaponDamage: Boolean
        get() =
            damageType in PHYSICAL_DAMAGE_TYPES &&
                sourceWeaponTags.any { tag ->
                    tag == ContentTag.MELEE_WEAPON ||
                        tag == ContentTag.RANGED_WEAPON
                }

    val isProjectileAttack: Boolean
        get() =
            projectileAttack || projectileTravelProgress > 0f ||
                ContentTag.RANGED_WEAPON in sourceWeaponTags

    companion object {
        private val PHYSICAL_DAMAGE_TYPES =
            setOf(
                DamageType.SLASHING,
                DamageType.PIERCING,
                DamageType.BLUDGEONING
            )

        val NONE =
            EnemyDamageContext()
    }
}
