package com.azizoglu.grisilith.weapon.skill.area

class PersistentAreaManager {

    private val runtimesById =
        mutableMapOf<
            String,
            PersistentAreaRuntime
            >()

    fun runtimeFor(
        areaId: String
    ): PersistentAreaRuntime {
        return runtimesById
            .getOrPut(
                areaId
            ) {
                PersistentAreaRuntime()
            }
    }

    fun clear(
        areaId: String
    ) {
        runtimesById[
            areaId
        ]
            ?.clear()
    }

    fun clearAll() {
        runtimesById.values
            .forEach {
                    runtime ->

                runtime.clear()
            }
    }

    val activeAreaCount:
        Int
        get() =
            runtimesById.values
                .count {
                        runtime ->

                    runtime.isActive
                }
}
