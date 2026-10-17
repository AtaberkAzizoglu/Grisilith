package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

/*
 * Altyapısı hazır fakat içeriği daha sonra
 * eklenecek ekranlar için ortak placeholder.
 */
class PlaceholderMenuScreen(
    private val title:
    String,

    private val description:
    String,

    private val onBack:
        () -> Unit
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f
    }

    private val camera =
        OrthographicCamera()

    private val viewport =
        FitViewport(
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            camera
        )

    private val batch =
        SpriteBatch()

    private val font =
        UiFontFactory.createDisplay()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                490f,

            y =
                90f,

            width =
                300f,

            height =
                70f,

            onClick =
                onBack
        )

    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()

        pointer.reset()
    }

    override fun render(
        delta:
        Float
    ) {
        pointer.update()

        val screenChanged =
            backButton.update(
                pointerPosition =
                    pointer.position,

                pointerJustPressed =
                    pointer.justPressed,

                pointerJustReleased =
                    pointer.justReleased
            )

        if (screenChanged) {
            return
        }

        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        )

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        font.data.setScale(
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            title,
            100f,
            610f,
            1080f,
            1,
            false
        )

        font.data.setScale(
            1.1f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            description,
            250f,
            430f,
            780f,
            1,
            true
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        backButton.render(
            batch =
                batch,

            font =
                font,

            pointerPosition =
                pointer.position,

            pointerHeld =
                pointer.isHeld
        )

        batch.end()
    }

    override fun resize(
        width:
        Int,

        height:
        Int
    ) {
        if (
            width <= 0 ||
            height <= 0
        ) {
            return
        }

        viewport.update(
            width,
            height,
            true
        )
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
    }
}
