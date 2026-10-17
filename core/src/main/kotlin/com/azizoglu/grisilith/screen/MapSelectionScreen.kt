package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameMap
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

class MapSelectionScreen(
    private val game:
    GrisilithGame,

    private val presetId:
    String? = null
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
        UiFontFactory.createBody()

    private val pointer =
        MenuPointer(
            viewport
        )

    private val mapButtons =
        GameMap.entries
            .mapIndexed {
                    index,
                    gameMap ->

                MenuButton(
                    text =
                        if (
                            gameMap.isInitiallyUnlocked
                        ) {
                            gameMap.displayName
                        } else {
                            "${gameMap.displayName} — LOCKED"
                        },

                    x =
                        390f,

                    y =
                        470f -
                            index * 90f,

                    width =
                        500f,

                    height =
                        80f,

                    isEnabled =
                        gameMap.isInitiallyUnlocked
                ) {
                    if (
                        presetId == null
                    ) {
                        game.showPresetMenu(
                            selectedMap =
                                gameMap
                        )
                    } else {
                        game.showPresetDifficultySelection(
                            presetId =
                                presetId,

                            gameMap =
                                gameMap
                        )
                    }
                }
            }

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                490f,

            y =
                75f,

            width =
                300f,

            height =
                65f
        ) {
            if (
                presetId == null
            ) {
                game.showPlayMenu()
            } else {
                game.showPresetMenu()
            }
        }

    private val buttons =
        mapButtons +
            backButton

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

        font.data.setScale(
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "SELECT MAP",
            0f,
            625f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            0.82f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            if (
                presetId == null
            ) {
                "Choose a map, then select the Preset for this run."
            } else {
                "Choose the map for the selected Preset."
            },
            0f,
            570f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            1f
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
