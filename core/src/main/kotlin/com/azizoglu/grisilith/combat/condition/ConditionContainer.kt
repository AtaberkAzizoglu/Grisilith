package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.content.ConditionType

class ConditionContainer {

    private val conditions =
        mutableMapOf<
            ConditionType,
            ActiveCondition
            >()

    fun update(
        delta: Float
    ) {
        if (delta <= 0f) {
            return
        }

        val iterator =
            conditions.iterator()

        while (iterator.hasNext()) {
            val entry =
                iterator.next()

            val condition =
                entry.value

            condition.update(
                delta = delta
            )

            if (condition.isExpired) {
                iterator.remove()
            }
        }
    }

    /*
     * durationSeconds null ise condition kalıcıdır.
     *
     * Aynı condition tekrar uygulanırsa süre
     * yenilenir. Kalıcı condition süreliye çevrilmez.
     */
    fun apply(
        type: ConditionType,
        durationSeconds: Float? = null
    ) {
        val existing =
            conditions[type]

        if (existing == null) {
            conditions[type] =
                ActiveCondition(
                    type = type,
                    durationSeconds =
                        durationSeconds
                )

            return
        }

        existing.refresh(
            durationSeconds =
                durationSeconds
        )
    }

    fun contains(
        type: ConditionType
    ): Boolean {

        return conditions.containsKey(
            type
        )
    }

    fun remove(
        type: ConditionType
    ): Boolean {

        return conditions.remove(
            type
        ) != null
    }

    fun clear() {
        conditions.clear()
    }

    val activeTypes:
        Set<ConditionType>
        get() =
            conditions.keys.toSet()
}
