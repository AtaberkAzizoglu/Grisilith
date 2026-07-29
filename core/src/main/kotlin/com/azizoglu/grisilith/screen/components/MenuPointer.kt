package com.azizoglu.grisilith.screen.components

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/*
 * Menü ekranlarındaki fare ve dokunma
 * koordinatlarını ortak şekilde yönetir.
 */
class MenuPointer(
    private val viewport:
    Viewport
) {

    val position =
        Vector2()

    val justPressed:
        Boolean
        get() =
            Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )

    private var wasHeld =
        false

    var justReleased:
        Boolean = false
        private set

    val isHeld:
        Boolean
        get() =
            Gdx.input.isButtonPressed(
                Input.Buttons.LEFT
            )

    /*
     * Yalnızca fare konumunu günceller.
     *
     * Scroll eventleri render döngüsünün dışında
     * gelebileceği için InputAdapter.scrolled()
     * içinde de bu fonksiyon çağrılmalıdır.
     */
    fun updatePosition() {
        position.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            position
        )
    }

    fun update() {
        updatePosition()

        val heldNow =
            isHeld

        justReleased =
            wasHeld &&
                !heldNow

        wasHeld =
            heldNow
    }

    fun reset() {
        updatePosition()

        wasHeld =
            false

        justReleased =
            false
    }
}
