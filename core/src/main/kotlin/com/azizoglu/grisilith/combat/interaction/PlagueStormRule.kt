package com.azizoglu.grisilith.combat.interaction

import com.azizoglu.grisilith.content.DamageType

class PlagueStormRule(
    private val isInsideVortex: (
        worldX: Float,
        worldY: Float
    ) -> Boolean,
    private val activatePlagueStorm: () -> Boolean
) : CombatInteractionRule {

    override fun handle(
        event: CombatInteractionEvent
    ) {
        /*
         * Plague Storm:
         * Active Vortex Pull içindeki bir Enemy başarılı NECROTIC
         * damage aldığında tetiklenir.
         *
         * Kaynağın ne olduğu önemli değildir. Bu nedenle hem Vortex
         * alanındaki hedefe başka bir Necrotic saldırı verilmesi hem de
         * Vortex ile ilişkili bir Necrotic damage event'i aynı pipeline'ı
         * kullanır.
         */
        if (
            event.trigger !=
            CombatInteractionTrigger.DAMAGE_APPLIED ||
            event.damageType !=
            DamageType.NECROTIC ||
            event.appliedDamage <=
            0 ||
            !isInsideVortex(
                event.worldX,
                event.worldY
            )
        ) {
            return
        }

        activatePlagueStorm()
    }
}
