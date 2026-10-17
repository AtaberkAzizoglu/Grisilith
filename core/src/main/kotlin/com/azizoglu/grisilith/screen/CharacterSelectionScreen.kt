package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatPresentationGroup
import com.azizoglu.grisilith.ui.stat.StatSymbolRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.badlogic.gdx.math.Rectangle

class CharacterSelectionScreen(
    private val game:
    GrisilithGame,

    private val gameMap:
    GameMap
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

    private val characters =
        DefaultCharacters.all

    private val pointer =
        MenuPointer(
            viewport
        )

    private val characterButtons =
        characters.mapIndexed {
                index,
                character ->

            MenuButton(
                text =
                    "PLAY AS " +
                        character.displayName,

                x =
                    28f +
                        index * 245f,

                y =
                    75f,

                width =
                    228f,

                height =
                    65f
            ) {
                startCharacter(
                    index
                )
            }
        }

    private val backButton =
        MenuButton(
            text =
                "BACK",

            x =
                490f,

            y =
                10f,

            width =
                300f,

            height =
                50f
        ) {
            game.showMapSelection()
        }

    private val buttons =
        characterButtons +
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

        renderScreen()
    }



    private fun startCharacter(
        index:
        Int
    ) {
        val character =
            characters.getOrNull(
                index
            )
                ?: return

        game.showDifficultySelection(
            gameMap =
                gameMap,

            character =
                character
        )
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

        font.color =
            Color.WHITE

        font.draw(
            batch,
            "SELECT CHARACTER",
            545f,
            665f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Map: ${gameMap.displayName}",
            0f,
            625f,
            VIEWPORT_WIDTH,
            1,
            false
        )

        characters.forEachIndexed {
                index,
                character ->

            val startX =
                28f +
                    index * 245f

            renderCharacter(
                number = index + 1,
                character = character,
                startX = startX,
                startY = 555f
            )
        }

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

    private fun renderCharacter(
        number: Int,
        character: CharacterDefinition,
        startX: Float,
        startY: Float
    ) {
        font.data.setScale(0.70f)

        var currentY =
            startY

        font.color =
            Color.WHITE

        font.draw(
            batch,
            character.displayName,
            startX,
            currentY + 42f,
            228f,
            1,
            false
        )

        val portraitSize =
            108f

        ContentIconRenderer.draw(
            batch = batch,
            definition = character,
            bounds = Rectangle(
                startX + (228f - portraitSize) / 2f,
                currentY - 78f,
                portraitSize,
                portraitSize
            )
        )

        currentY -= 118f

        font.color =
            Color.GOLD

        font.draw(
            batch,
            character.trait.displayName +
                ": " +
            character.trait.description,
            startX,
            currentY,
            228f,
            -1,
            true
        )

        val statStartY =
            currentY - 78f

        val leftColumnX =
            startX

        val rightColumnX =
            startX + 114f

        fun drawGroupedColumn(
            groups: List<StatPresentationGroup>,
            x: Float
        ) {
            var y = statStartY
            groups.forEach { group ->
                font.data.setScale(0.42f)
                font.color = Color(0.72f, 0.78f, 0.90f, 1f)
                font.draw(batch, group.title, x, y, 108f, com.badlogic.gdx.utils.Align.center, false)
                y -= 16f
                font.data.setScale(0.52f)
                group.stats.forEach { statEntry ->
                    drawStat(statEntry, character.baseStat(statEntry), x, y)
                    y -= 18f
                }
                y -= 4f
            }
        }

        drawGroupedColumn(
            listOf(StatPresentationGroup.magicalDamage, StatPresentationGroup.defensive),
            leftColumnX
        )
        drawGroupedColumn(
            listOf(StatPresentationGroup.physicalDamage, StatPresentationGroup.utility),
            rightColumnX
        )

        font.data.setScale(1f)
    }

    private fun drawStat(
        statType: StatType,
        value: Int,
        x: Float,
        y: Float
    ) {
        StatSymbolRenderer.drawLabel(
            batch = batch,
            font = font,
            statType = statType,
            x = x,
            baselineY = y,
            iconSize = 13f,
            width = 92f
        )

        font.color = when {
            value >= 12 -> Color.GREEN
            value <= 3 -> Color.SCARLET
            else -> Color.WHITE
        }
        font.draw(batch, value.toString(), x + 96f, y)
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
