package com.azizoglu.grisilith.chest

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class ChestPickup(
    startX: Float,
    startY: Float
) {

    companion object {
        private const val CHEST_WIDTH =
            34f

        private const val CHEST_HEIGHT =
            26f

        private const val LID_HEIGHT =
            8f
    }

    private val position =
        Vector2(
            startX -
                CHEST_WIDTH / 2f,

            startY -
                CHEST_HEIGHT / 2f
        )

    private val collisionBounds =
        Rectangle()

    var isCollected:
        Boolean = false
        private set

    val centerX: Float
        get() =
            position.x +
                CHEST_WIDTH / 2f

    val centerY: Float
        get() =
            position.y +
                CHEST_HEIGHT / 2f

    val bounds: Rectangle
        get() {
            collisionBounds.set(
                position.x,
                position.y,
                CHEST_WIDTH,
                CHEST_HEIGHT
            )

            return collisionBounds
        }

    fun update(
        playerBounds:
        Rectangle,

        onCollected:
            () -> Unit
    ) {
        if (isCollected) {
            return
        }

        if (
            !bounds.overlaps(
                playerBounds
            )
        ) {
            return
        }

        isCollected = true

        onCollected()
    }

    fun render(
        shapeRenderer:
        ShapeRenderer
    ) {
        if (isCollected) {
            return
        }

        /*
         * Ana gövde.
         */
        shapeRenderer.color =
            Color(
                0.42f,
                0.22f,
                0.07f,
                1f
            )

        shapeRenderer.rect(
            position.x,
            position.y,
            CHEST_WIDTH,
            CHEST_HEIGHT -
                LID_HEIGHT
        )

        /*
         * Kapak.
         */
        shapeRenderer.color =
            Color(
                0.58f,
                0.32f,
                0.09f,
                1f
            )

        shapeRenderer.rect(
            position.x -
                2f,

            position.y +
                CHEST_HEIGHT -
                LID_HEIGHT,

            CHEST_WIDTH +
                4f,

            LID_HEIGHT
        )

        /*
         * Altın renkli kilit.
         */
        shapeRenderer.color =
            Color.GOLD

        shapeRenderer.rect(
            centerX -
                3f,

            position.y +
                7f,

            6f,
            8f
        )
    }
}
