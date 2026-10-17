package com.azizoglu.grisilith.combat.interaction

class CombatInteractionManager {

    private val rules =
        mutableListOf<CombatInteractionRule>()

    init {
        activeManager =
            this
    }

    fun register(
        rule: CombatInteractionRule
    ) {
        rules +=
            rule
    }

    fun clearRules() {
        rules.clear()
    }

    fun unregister(rule: CombatInteractionRule) { rules.remove(rule) }

    fun notify(
        event: CombatInteractionEvent
    ) {
        rules
            .toList()
            .forEach {
                it.handle(
                    event
                )
            }
    }

    companion object {

        private var activeManager:
            CombatInteractionManager? =
            null

        fun notifyGlobal(
            event: CombatInteractionEvent
        ) {
            activeManager
                ?.notify(
                    event
                )
        }
    }
}
