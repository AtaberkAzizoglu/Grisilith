package com.azizoglu.grisilith.weapon.visual

enum class ProjectileVisualKind(
    val rowIndex: Int,
    val baseWorldHeight: Float
) {
    DAGGER(
        rowIndex = -1,
        baseWorldHeight = 34f
    ),

    ARROW(
        rowIndex = 0,
        baseWorldHeight = 30f
    ),

    AIR(
        rowIndex = 1,
        baseWorldHeight = 32f
    ),

    WATER(
        rowIndex = 2,
        baseWorldHeight = 32f
    ),

    FIRE(
        rowIndex = 3,
        baseWorldHeight = 32f
    ),

    POISON(
        rowIndex = 4,
        baseWorldHeight = 32f
    ),

    ACID(
        rowIndex = 5,
        baseWorldHeight = 32f
    ),

    LIGHTNING(
        rowIndex = 6,
        baseWorldHeight = 34f
    ),

    NECROTIC(
        rowIndex = 7,
        baseWorldHeight = 34f
    )
}
