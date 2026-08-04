package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

class ConditionContainer {

    private val conditions =
        mutableMapOf<ConditionType, ActiveCondition>()

    fun update(
        delta: Float,
        onTick: (ConditionTick) -> Unit = {}
    ) {
        if (delta <= 0f) {
            return
        }

        val iterator = conditions.iterator()

        while (iterator.hasNext()) {
            val condition = iterator.next().value

            condition.update(
                delta = delta,
                onTick = onTick
            )

            if (condition.isExpired) {
                iterator.remove()
            }
        }
    }

    fun apply(
        application: ConditionApplication
    ) {
        val existing = conditions[application.type]

        if (existing == null) {
            conditions[application.type] =
                ActiveCondition(application)
            return
        }

        existing.refresh(application)
    }

    /*
     * Eski çağrılarla geriye uyumludur.
     * Bu sürüm kaynak snapshot'ı taşımadığı için DoT hasarı üretmez.
     * Weapon ve Skill bağlantıları ConditionApplication kullanacaktır.
     */
    fun apply(
        type: ConditionType,
        durationSeconds: Float? = null
    ) {
        apply(
            ConditionApplication(
                type = type,
                durationSeconds = durationSeconds,
                source = ConditionSource.NONE
            )
        )
    }

    fun contains(type: ConditionType): Boolean {
        return conditions.containsKey(type)
    }

    fun activeCondition(type: ConditionType): ActiveCondition? {
        return conditions[type]
    }

    fun remove(type: ConditionType): Boolean {
        return conditions.remove(type) != null
    }

    fun clear() {
        conditions.clear()
    }

    val activeTypes: Set<ConditionType>
        get() = conditions.keys.toSet()
}
