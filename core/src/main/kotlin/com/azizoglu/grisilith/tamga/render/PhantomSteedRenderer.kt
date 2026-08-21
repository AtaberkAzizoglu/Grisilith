package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.player.Player
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/*
 * Deprecated compatibility shell.
 *
 * Phantom Steed visual is now implemented by swapping the player character
 * sprite to a mounted character sprite while the Tamga is ACTIVE.
 */
class PhantomSteedRenderer {
    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player,
        activeProgress: Float
    ) {
        // Intentionally empty.
    }
}
