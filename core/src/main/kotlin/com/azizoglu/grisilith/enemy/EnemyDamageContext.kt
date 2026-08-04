package com.azizoglu.grisilith.enemy

import com.azizoglu.grisilith.content.DamageType

data class EnemyDamageContext(
    val damageType: DamageType =
        DamageType.NONE,

    val sourceWeaponRuntimeId: String? =
        null,

    val sourceWeaponDefinitionId: String? =
        null,

    val sourceName: String? =
        null
) {

    companion object {

        val NONE =
            EnemyDamageContext()
    }
}
