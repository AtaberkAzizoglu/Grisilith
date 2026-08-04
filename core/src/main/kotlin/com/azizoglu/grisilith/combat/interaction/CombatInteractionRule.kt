package com.azizoglu.grisilith.combat.interaction

fun interface CombatInteractionRule {
    fun handle(
        event: CombatInteractionEvent
    )
}
