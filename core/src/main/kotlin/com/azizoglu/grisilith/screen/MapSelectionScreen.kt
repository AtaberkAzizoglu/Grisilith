package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport

class MapSelectionScreen(
    private val game: GrisilithGame,
    private val difficulty: GameDifficulty
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
                game.startRun(
                    difficulty = difficulty,
                    gameMap = GameMap.ROAD_TO_HOME
                )

                return true
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                if (
                    GameMap.TROUBLE_AT_HOME
                        .isInitiallyUnlocked
                ) {
                    game.startRun(
                        difficulty = difficulty,
                        gameMap =
                            GameMap.TROUBLE_AT_HOME
                    )

                    return true
                }
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                if (
                    GameMap
                        .FOLLOWING_THE_DUNGEON_DELVER
                        .isInitiallyUnlocked
                ) {
                    game.startRun(
                        difficulty = difficulty,
                        gameMap =
                            GameMap
                                .FOLLOWING_THE_DUNGEON_DELVER
                    )

                    return true
                }
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.ESCAPE
            ) -> {
                game.showDifficultySelection()
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
            "SELECT MAP",
            580f,
            560f
        )

        font.color = Color.LIGHT_GRAY

        font.draw(
            batch,
            "Difficulty: ${difficulty.displayName}",
            545f,
            520f
        )

        renderMapOption(
            number = 1,
            gameMap = GameMap.ROAD_TO_HOME,
            y = 430f
        )

        renderMapOption(
            number = 2,
            gameMap = GameMap.TROUBLE_AT_HOME,
            y = 310f
        )

        renderMapOption(
            number = 3,
            gameMap =
                GameMap.FOLLOWING_THE_DUNGEON_DELVER,
            y = 190f
        )

        font.color = Color.GRAY

        font.draw(
            batch,
            "ESC - Back",
            585f,
            80f
        )

        batch.end()
    }

    private fun renderMapOption(
        number: Int,
        gameMap: GameMap,
        y: Float
    ) {
        val unlocked =
            gameMap.isInitiallyUnlocked

        font.color =
            if (unlocked) {
                Color.WHITE
            } else {
                Color.DARK_GRAY
            }

        val lockText =
            if (unlocked) {
                ""
            } else {
                " [LOCKED]"
            }

        font.draw(
            batch,
            "$number - ${gameMap.displayName}$lockText",
            445f,
            y
        )

        font.color =
            if (unlocked) {
                Color.LIGHT_GRAY
            } else {
                Color.DARK_GRAY
            }

        font.draw(
            batch,
            gameMap.description,
            470f,
            y - 30f
        )

        font.draw(
            batch,
            "${gameMap.worldWidth.toInt()} x " +
                "${gameMap.worldHeight.toInt()}",
            470f,
            y - 58f
        )
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
