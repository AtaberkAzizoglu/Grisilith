package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer

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
        BitmapFont()

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
                    60f +
                        index * 405f,

                y =
                    75f,

                width =
                    350f,

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
                60f +
                    index * 405f

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
        var currentY =
            startY

        font.color =
            Color.WHITE

        font.draw(
            batch,
            character.displayName,
            startX,
            currentY
        )

        currentY -= 38f

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            character.description,
            startX,
            currentY,
            350f,
            -1,
            true
        )

        font.color =
            Color.GOLD

        font.draw(
            batch,
            character.trait.displayName +
                ": " +
                character.trait.description,
            startX,
            currentY - 62f,
            350f,
            -1,
            true
        )

        val statStartY =
            currentY - 130f

        val leftColumnX =
            startX

        val rightColumnX =
            startX + 180f

        val rowSpacing =
            25f

        val leftColumnStats =
            listOf(
                "Max HP" to
                    StatType.MAX_HP,

                "Armor Class" to
                    StatType.ARMOR_CLASS,

                "Strength" to
                    StatType.STRENGTH,

                "Dexterity" to
                    StatType.DEXTERITY,

                "Wisdom" to
                    StatType.WISDOM,

                "Haste" to
                    StatType.HASTE,

                "Attack Speed" to
                    StatType.ATTACK_SPEED,

                "Crit Rate" to
                    StatType.CRIT_RATE,

                "Crit Damage" to
                    StatType.CRIT_DAMAGE,

                "Movement" to
                    StatType.MOVEMENT_SPEED
            )

        val rightColumnStats =
            listOf(
                "Luck" to
                    StatType.LUCK,

                "Harvest" to
                    StatType.HARVEST,

                "Elemental" to
                    StatType.ELEMENTAL_DAMAGE,

                "Nature" to
                    StatType.MIGHT_OF_NATURE,

                "Shepherd" to
                    StatType.SHEPHERD,

                "Piercing" to
                    StatType.PIERCING,

                "Enemy Count" to
                    StatType.ENEMY_COUNT,

                "HP Regen" to
                    StatType.HP_REGENERATION,

                "Life Steal" to
                    StatType.LIFE_STEAL
            )

        leftColumnStats.forEachIndexed {
                index,
                statEntry ->

            drawStat(
                label = statEntry.first,
                value =
                    character.baseStat(
                        statEntry.second
                    ),
                x = leftColumnX,
                y =
                    statStartY -
                        index * rowSpacing
            )
        }

        rightColumnStats.forEachIndexed {
                index,
                statEntry ->

            drawStat(
                label = statEntry.first,
                value =
                    character.baseStat(
                        statEntry.second
                    ),
                x = rightColumnX,
                y =
                    statStartY -
                        index * rowSpacing
            )
        }
    }

    private fun drawStat(
        label: String,
        value: Int,
        x: Float,
        y: Float
    ) {
        font.color =
            when {
                value >= 12 ->
                    Color.GREEN

                value <= 3 ->
                    Color.SCARLET

                else ->
                    Color.LIGHT_GRAY
            }

        font.draw(
            batch,
            "$label: $value",
            x,
            y
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
