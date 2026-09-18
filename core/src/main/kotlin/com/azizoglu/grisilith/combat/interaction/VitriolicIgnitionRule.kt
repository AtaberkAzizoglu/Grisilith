package com.azizoglu.grisilith.combat.interaction

import com.azizoglu.grisilith.content.DamageType

class VitriolicIgnitionRule(
    private val tryIgnitePool: (
        worldX: Float,
        worldY: Float
    ) -> Boolean,
    private val onTriggered: (
        worldX: Float,
        worldY: Float
    ) -> Unit
) : CombatInteractionRule {

    override fun handle(
        event: CombatInteractionEvent
    ) {
        /*
         * Vitriolic Ignition:
         * Active Acidic Pool içindeki bir Enemy başarılı FIRE damage
         * aldığında deterministik olarak tetiklenir.
         */
        if (
            (event.trigger != CombatInteractionTrigger.DAMAGE_APPLIED &&
                event.trigger != CombatInteractionTrigger.PROJECTILE_PASSED) ||
            event.damageType != DamageType.FIRE
        ) {
            return
        }

        if (
            tryIgnitePool(
                event.worldX,
                event.worldY
            )
        ) {
            onTriggered(
                event.worldX,
                event.worldY
            )
        }
    }
}
