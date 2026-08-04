package com.azizoglu.grisilith.weapon.skill.area

import com.badlogic.gdx.math.Vector2

data class PersistentArea(
    val id: String,

    val center:
    Vector2,

    val radius:
    Float,

    val durationSeconds:
    Float,

    val tickIntervalSeconds:
    Float,

    val anchor:
    PersistentAreaAnchor
) {
    init {
        require(
            id.isNotBlank()
        )

        require(
            radius >
                0f
        )

        require(
            durationSeconds >
                0f
        )

        require(
            tickIntervalSeconds >
                0f
        )
    }

    fun contains(
        worldX: Float,
        worldY: Float
    ): Boolean {
        return Vector2.dst2(
            center.x,
            center.y,
            worldX,
            worldY
        ) <=
            radius *
            radius
    }
}
