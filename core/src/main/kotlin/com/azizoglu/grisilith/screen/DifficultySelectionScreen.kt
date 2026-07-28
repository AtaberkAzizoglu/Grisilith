package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.config.GameDifficulty
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
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.config.GameMap

class DifficultySelectionScreen(
    private val game:
    GrisilithGame,

    private val gameMap:
    GameMap,

    private val selectedCharacter:
    CharacterDefinition
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
                    "EASY — 20 STAGES",

                x =
                    440f,

                y =
                    400f,

                width =
                    400f,

                height =
                    75f
            ) {
                game.startRun(
                    difficulty =
                        GameDifficulty.EASY,

                    gameMap =
                        gameMap,

                    character =
                        selectedCharacter
                )
            },

            MenuButton(
                text =
                    "NORMAL — 30 STAGES",

                x =
                    440f,

                y =
                    295f,

                width =
                    400f,

                height =
                    75f
            ) {
                game.startRun(
                    difficulty =
                        GameDifficulty.NORMAL,

                    gameMap =
                        gameMap,

                    character =
                        selectedCharacter
                )
            },

            MenuButton(
                text =
                    "HARD — 40 STAGES",

                x =
                    440f,

                y =
                    190f,

                width =
                    400f,

                height =
                    75f
            ) {
                game.startRun(
                    difficulty =
                        GameDifficulty.HARD,

                    gameMap =
                        gameMap,

                    character =
                        selectedCharacter
                )
            },

            MenuButton(
                text =
                    "BACK",

                x =
                    490f,

                y =
                    95f,

                width =
                    300f,

                height =
                    65f
            ) {
                game.showCharacterSelection(
                    gameMap =
                        gameMap
                )
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
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "SELECT DIFFICULTY",
            0f,
            620f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            0.95f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Character: " +
                selectedCharacter.displayName,
            0f,
            565f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.draw(
            batch,
            "Map: " +
                gameMap.displayName,
            0f,
            535f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        font.data.setScale(
            0.85f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "Difficulty changes run length, not enemy scaling.",
            0f,
            500f,
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
