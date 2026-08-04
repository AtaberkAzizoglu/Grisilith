package com.azizoglu.grisilith.combat.interaction

class CombatInteractionRegistry {

    private val handlers =
        mutableListOf<
                (
            CombatInteractionEvent
        ) -> Unit
            >()

    fun register(

        handler:
            (
            CombatInteractionEvent
        ) -> Unit
    ) {

        handlers +=
            handler
    }

    fun dispatch(

        event:
        CombatInteractionEvent
    ) {

        handlers.forEach {

            it(
                event
            )
        }
    }

    fun clear() {

        handlers.clear()
    }
}
