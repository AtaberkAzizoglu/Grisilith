package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.player.Player
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils

class PhantomSteedRenderer {
    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player,
        activeProgress: Float
    ) {
        val pulse =
            0.82f +
                MathUtils.sin(activeProgress * MathUtils.PI2 * 4f) * 0.08f

        val centerX = player.centerX
        val centerY = player.centerY - 18f

        shapeRenderer.color = Color(0.35f, 0.78f, 1f, 0.25f)
        shapeRenderer.ellipse(
            centerX - 39f * pulse,
            centerY - 17f,
            78f * pulse,
            34f
        )

        shapeRenderer.color = Color(0.60f, 0.88f, 1f, 0.48f)
        shapeRenderer.ellipse(
            centerX - 31f * pulse,
            centerY - 13f,
            62f * pulse,
            26f
        )

        shapeRenderer.rect(
            centerX - 25f,
            centerY - 29f,
            8f,
            25f
        )
        shapeRenderer.rect(
            centerX + 17f,
            centerY - 29f,
            8f,
            25f
        )

        shapeRenderer.triangle(
            centerX + 24f, centerY + 4f,
            centerX + 46f, centerY + 17f,
            centerX + 35f, centerY - 2f
        )
    }
}
