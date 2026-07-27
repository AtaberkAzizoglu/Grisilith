package com.azizoglu.grisilith.chest

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

class ChestPickupManager {

    private val pickups =
        mutableListOf<ChestPickup>()

    val activeChestCount: Int
        get() =
            pickups.count { pickup ->
                !pickup.isCollected
            }

    fun spawn(
        worldX: Float,
        worldY: Float
    ) {
        pickups.add(
            ChestPickup(
                startX =
                    worldX,

                startY =
                    worldY
            )
        )
    }

    fun update(
        playerBounds:
        Rectangle,

        onChestCollected:
            () -> Unit
    ) {
        pickups.forEach { pickup ->
            pickup.update(
                playerBounds =
                    playerBounds,

                onCollected =
                    onChestCollected
            )
        }

        pickups.removeAll { pickup ->
            pickup.isCollected
        }
    }

    fun render(
        shapeRenderer:
        ShapeRenderer
    ) {
        pickups.forEach { pickup ->
            pickup.render(
                shapeRenderer
            )
        }
    }

    fun clearUncollectedChests() {
        pickups.clear()
    }

    fun clearAll() {
        pickups.clear()
    }
}
