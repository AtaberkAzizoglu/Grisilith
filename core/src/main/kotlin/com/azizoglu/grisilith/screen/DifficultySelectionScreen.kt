package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.preset.PresetRepository
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
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

class DifficultySelectionScreen(
    private val game:
    GrisilithGame,

    private val gameMap:
    GameMap,

    private val selectedCharacter:
    CharacterDefinition? = null,

    private val presetId:
    String? = null
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH =
            1280f

        private const val VIEWPORT_HEIGHT =
            720f
    }

    init {
        require(
            selectedCharacter != null ||
                presetId != null
        ) {
            "DifficultySelectionScreen requires a Character or Preset."
        }
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

    private fun start(
        difficulty:
        GameDifficulty
    ) {
        val selectedPresetId =
            presetId

        if (
            selectedPresetId != null
        ) {
            game.startPresetRun(
                difficulty =
                    difficulty,

                gameMap =
                    gameMap,

                presetId =
                    selectedPresetId
            )

            return
        }

        val character =
            selectedCharacter
                ?: return

        game.startRun(
            difficulty =
                difficulty,

            gameMap =
                gameMap,

            character =
                character
        )
    }

    private val buttons =
        listOf(
            MenuButton(
                text =
                    "${GameDifficulty.EASY.displayName.uppercase()} — " +
                        "${GameDifficulty.EASY.maximumStage} STAGES",

                x =
                    440f,

                y =
                    400f,

                width =
                    400f,

                height =
                    75f
            ) {
                start(
                    GameDifficulty.EASY
                )
            },

            MenuButton(
                text =
                    "${GameDifficulty.NORMAL.displayName.uppercase()} — " +
                        "${GameDifficulty.NORMAL.maximumStage} STAGES",

                x =
                    440f,

                y =
                    295f,

                width =
                    400f,

                height =
                    75f
            ) {
                start(
                    GameDifficulty.NORMAL
                )
            },

            MenuButton(
                text =
                    "${GameDifficulty.HARD.displayName.uppercase()} — " +
                        "${GameDifficulty.HARD.maximumStage} STAGES",

                x =
                    440f,

                y =
                    190f,

                width =
                    400f,

                height =
                    75f
            ) {
                start(
                    GameDifficulty.HARD
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
                if (
                    presetId != null
                ) {
                    game.showPresetMenu(
                        selectedMap =
                            gameMap
                    )
                } else {
                    game.showCharacterSelection(
                        gameMap =
                            gameMap
                    )
                }
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
            Align.center,
            false
        )

        font.data.setScale(
            0.90f
        )

        font.color =
            Color.LIGHT_GRAY

        val selectionText =
            presetId
                ?.let { id ->
                    val presetName =
                        PresetRepository
                            .findById(
                                id
                            )
                            ?.presetName
                            ?: "Preset"

                    "$presetName  •  ${gameMap.displayName}"
                }
                ?: (
                    selectedCharacter
                        ?.displayName
                        ?: "Character"
                    ) +
                "  •  ${gameMap.displayName}"

        font.draw(
            batch,
            selectionText,
            0f,
            555f,
            VIEWPORT_WIDTH,
            Align.center,
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
