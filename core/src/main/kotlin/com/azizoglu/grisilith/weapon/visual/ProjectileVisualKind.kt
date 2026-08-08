package com.azizoglu.grisilith.weapon.visual

enum class ProjectileVisualKind(
    val rowIndex: Int,
    val baseWorldSize: Float
) {
    ARROW(
        rowIndex = 0,
        baseWorldSize = 28f
    ),

    DAGGER(
        rowIndex = 1,
        baseWorldSize = 28f
    ),

    AIR(
        rowIndex = 2,
        baseWorldSize = 32f
    ),

    WATER(
        rowIndex = 3,
        baseWorldSize = 32f
    ),

    FIRE(
        rowIndex = 4,
        baseWorldSize = 32f
    ),

    POISON(
        rowIndex = 5,
        baseWorldSize = 32f
    ),

    ACID(
        rowIndex = 6,
        baseWorldSize = 32f
    ),

    LIGHTNING(
        rowIndex = 7,
        baseWorldSize = 32f
    ),

    NECROTIC(
        rowIndex = 8,
        baseWorldSize = 32f
    )
}
