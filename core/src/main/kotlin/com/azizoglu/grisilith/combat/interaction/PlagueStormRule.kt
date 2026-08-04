package com.azizoglu.grisilith.combat.interaction

import com.badlogic.gdx.math.MathUtils

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

        if (
            event.trigger !=
            CombatInteractionTrigger.ERASING_SOULS_EXPLOSION ||
            !isInsideVortex(
                event.worldX,
                event.worldY
            )
        ) {
            return
        }

        if (
            MathUtils.random() <=
            TRIGGER_CHANCE
        ) {
            activatePlagueStorm()
        }
    }

    companion object {

        const val TRIGGER_CHANCE =
            0.33f
    }
}
