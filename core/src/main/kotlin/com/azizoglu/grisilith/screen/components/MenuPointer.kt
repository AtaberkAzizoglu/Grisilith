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

    val justPressed: Boolean
        get() =
            Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )

    /*
     * LibGDX'de doğrudan justReleased olmadığı
     * için önceki frame durumu tutulur.
     */
    private var wasHeld =
        false

    var justReleased:
        Boolean = false
        private set

    val isHeld: Boolean
        get() =
            Gdx.input.isButtonPressed(
                Input.Buttons.LEFT
            )

    fun update() {
        position.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            position
        )

        val heldNow =
            isHeld

        justReleased =
            wasHeld &&
                !heldNow

        wasHeld =
            heldNow
    }

    fun reset() {
        wasHeld = false
        justReleased = false
    }
}
