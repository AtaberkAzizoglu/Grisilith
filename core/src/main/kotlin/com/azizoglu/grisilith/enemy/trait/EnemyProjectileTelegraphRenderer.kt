package com.azizoglu.grisilith.enemy.trait

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

/** Shared trajectory style for every enemy projectile telegraph. */
internal fun drawEnemyProjectileDottedLine(
    renderer: ShapeRenderer,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    color: Color
) {
    val direction = Vector2(endX - startX, endY - startY)
    val length = direction.len()
    if (length <= 1f) return
    direction.nor()
    renderer.color = color
    var distance = 14f
    while (distance < length) {
        renderer.circle(
            startX + direction.x * distance,
            startY + direction.y * distance,
            3.2f,
            8
        )
        distance += 22f
    }
}
