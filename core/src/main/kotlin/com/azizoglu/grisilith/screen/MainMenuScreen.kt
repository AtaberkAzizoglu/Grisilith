package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class MainMenuScreen(
    private val game:
    GrisilithGame
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
        BitmapFont()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val buttons =
        listOf(
            MenuButton(
                text =
                    "PLAY",

                x =
                    440f,

                y =
                    455f,

                width =
                    400f,

                height =
                    65f
            ) {
                game.showPlayMenu()
            },

            MenuButton(
                text =
                    "COLLECTION",

                x =
                    440f,

                y =
                    370f,

                width =
                    400f,

                height =
                    65f
            ) {
                game.showCollection()
            },

            MenuButton(
                text =
                    "MERCHANT FROM HALATHA",

                x =
                    440f,

                y =
                    285f,

                width =
                    400f,

                height =
                    65f
            ) {
                game.showMerchant()
            },

            MenuButton(
                text =
                    "HIGH SCORES",

                x =
                    440f,

                y =
                    200f,

                width =
                    400f,

                height =
                    65f
            ) {
                game.showHighScores()
            },

            MenuButton(
                text =
                    "SETTINGS",

                x =
                    440f,

                y =
                    115f,

                width =
                    400f,

                height =
                    65f
            ) {
                game.showSettings()
            }
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

        buttons.forEach { button ->
            val screenChanged =
                button.update(
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
            2.5f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "GRISILITH",
            0f,
            630f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Desktop Prototype",
            0f,
            570f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        buttons.forEach { button ->
            button.render(
                batch =
                    batch,

                font =
                    font,

                pointerPosition =
                    pointer.position,

                pointerHeld =
                    pointer.isHeld
            )
        }

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
