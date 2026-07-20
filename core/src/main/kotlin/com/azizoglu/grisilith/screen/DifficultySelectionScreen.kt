package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class DifficultySelectionScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f
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

    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )

        camera.update()
    }

    override fun render(delta: Float) {
        val screenChanged =
            handleInput()

        if (screenChanged) {
            return
        }

        renderScreen()
    }

    private fun handleInput(): Boolean {
        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> {
                game.showMapSelection(
                    GameDifficulty.EASY
                )

                return true
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                game.showMapSelection(
                    GameDifficulty.NORMAL
                )

                return true
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                game.showMapSelection(
                    GameDifficulty.HARD
                )

                return true
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            ) -> {
                game.showMainMenu()
                return true
            }
        }

        return false
    }

    private fun renderScreen() {
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

        font.color = Color.WHITE

        font.draw(
            batch,
            "SELECT DIFFICULTY",
            545f,
            520f
        )

        font.draw(
            batch,
            "1 - EASY",
            570f,
            410f
        )

        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            "20 Stages",
            590f,
            380f
        )

        font.color = Color.WHITE

        font.draw(
            batch,
            "2 - NORMAL",
            570f,
            320f
        )

        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            "30 Stages",
            590f,
            290f
        )

        font.color = Color.WHITE

        font.draw(
            batch,
            "3 - HARD",
            570f,
            230f
        )

        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            "40 Stages",
            590f,
            200f
        )

        font.color = Color.GRAY

        font.draw(
            batch,
            "Difficulty changes run length, not enemy scaling.",
            460f,
            125f
        )

        font.draw(
            batch,
            "ESC - Back",
            585f,
            80f
        )

        batch.end()
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
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
