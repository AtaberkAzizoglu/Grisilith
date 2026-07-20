package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class MainMenuScreen(
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
        val screenChanged = handleInput()

        if (screenChanged) {
            return
        }

        renderMenu()
    }

    private fun handleInput(): Boolean {
        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            )
        ) {
            game.showDifficultySelection()
            return true
        }

        if (
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            )
        ) {
            // Save sistemi henüz yok.
            return false
        }

        return false
    }

    private fun renderMenu() {
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
            "GRISILITH",
            570f,
            500f
        )

        font.draw(
            batch,
            "1 - NEW GAME",
            550f,
            390f
        )

        font.color = Color.GRAY

        font.draw(
            batch,
            "2 - LOAD GAME (NO SAVE)",
            510f,
            340f
        )

        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            "Desktop Prototype Controls",
            520f,
            210f
        )

        font.draw(
            batch,
            "Move: WASD / Arrow Keys",
            520f,
            175f
        )

        font.draw(
            batch,
            "Pause: Space",
            520f,
            140f
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
