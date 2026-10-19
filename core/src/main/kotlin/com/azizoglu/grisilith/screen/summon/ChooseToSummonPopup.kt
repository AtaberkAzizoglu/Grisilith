package com.azizoglu.grisilith.screen.summon

import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer

class ChooseToSummonPopup(
    private val camera: OrthographicCamera,
    private val viewport: Viewport,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    private val pointer =
        Vector2()

    private val optionBounds =
        Array(
            IvySummonChoice.entries.size
        ) {
            Rectangle()
        }

    private var pendingWeapon:
        WeaponInstance? =
        null

    val isOpen: Boolean
        get() =
            pendingWeapon !=
                null

    fun open(
        weapon: WeaponInstance
    ): Boolean {

        if (
            isOpen ||
            weapon.ivySummonChoice !=
            null
        ) {
            return false
        }

        pendingWeapon =
            weapon

        return true
    }

    fun handleInput() {

        val weapon =
            pendingWeapon
                ?: return

        when {
            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1
            ) -> {
                commit(
                    weapon,
                    IvySummonChoice.SUMMON_ANIMALS
                )
                return
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_2
            ) -> {
                commit(
                    weapon,
                    IvySummonChoice.SUMMON_DEMONS
                )
                return
            }

            Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_3
            ) -> {
                commit(
                    weapon,
                    IvySummonChoice.CALL_THE_KNIGHT
                )
                return
            }
        }

        if (
            !Gdx.input.isButtonJustPressed(
                Input.Buttons.LEFT
            )
        ) {
            return
        }

        updatePointer()

        IvySummonChoice.entries
            .forEachIndexed {
                    index,
                    choice ->

                if (
                    optionBounds[index]
                        .contains(
                            pointer
                        )
                ) {
                    commit(
                        weapon,
                        choice
                    )

                    return
                }
            }
    }

    fun render() {

        val weapon =
            pendingWeapon
                ?: return

        updateLayout()

        Gdx.gl.glEnable(
            com.badlogic.gdx.graphics.GL20.GL_BLEND
        )

        Gdx.gl.glBlendFunc(
            com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
            com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        val left =
            camera.position.x -
                PANEL_WIDTH /
                2f

        val bottom =
            camera.position.y -
                PANEL_HEIGHT /
                2f

        shapeRenderer.color =
            Color(
                0f,
                0f,
                0f,
                0.72f
            )

        shapeRenderer.rect(
            camera.position.x -
                viewport.worldWidth /
                2f,
            camera.position.y -
                viewport.worldHeight /
                2f,
            viewport.worldWidth,
            viewport.worldHeight
        )

        shapeRenderer.end()

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        UiComponentRenderer.drawPanel(
            batch,
            Rectangle(left, bottom, PANEL_WIDTH, PANEL_HEIGHT),
            PanelVisualStyle.MODAL
        )

        optionBounds.forEach { bounds ->
            UiComponentRenderer.drawButton(
                batch,
                bounds,
                if (bounds.contains(pointer)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
            )
        }

        font.color =
            Color.WHITE

        font.data.setScale(
            1.55f
        )

        font.draw(
            batch,
            "CHOOSE TO SUMMON",
            left,
            bottom +
                PANEL_HEIGHT -
                42f,
            PANEL_WIDTH,
            Align.center,
            false
        )

        IvySummonChoice.entries
            .forEachIndexed {
                    index,
                    choice ->

                val bounds =
                    optionBounds[index]

                font.data.setScale(
                    1f
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    choice.displayName,
                    bounds.x,
                    bounds.y + (bounds.height + font.capHeight) / 2f,
                    bounds.width,
                    Align.center,
                    false
                )
            }

        font.data.setScale(
            1f
        )

        optionBounds.indexOfFirst { it.contains(pointer) }.takeIf { it >= 0 }?.let { index ->
            val choice = IvySummonChoice.entries[index]
            com.azizoglu.grisilith.screen.render.AdaptiveTooltipRenderer.draw(
                batch, font,
                com.badlogic.gdx.math.Rectangle(camera.position.x-viewport.worldWidth/2f,
                    camera.position.y-viewport.worldHeight/2f,viewport.worldWidth,viewport.worldHeight),
                pointer, choice.displayName,
                com.azizoglu.grisilith.summon.SummonPresentation.choice(choice),
                490f, .88f, .73f
            )
        }
        batch.end()

        Gdx.gl.glDisable(
            com.badlogic.gdx.graphics.GL20.GL_BLEND
        )
    }

    private fun commit(
        weapon: WeaponInstance,
        choice: IvySummonChoice
    ) {

        if(weapon.chooseIvySummon(choice)) com.azizoglu.grisilith.audio.GameAudio.summonChoice(choice)

        pendingWeapon =
            null
    }

    private fun updatePointer() {

        pointer.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat()
        )

        viewport.unproject(
            pointer
        )
    }

    private fun updateLayout() {

        updatePointer()

        val left =
            camera.position.x -
                PANEL_WIDTH /
                2f

        val bottom =
            camera.position.y -
                PANEL_HEIGHT /
                2f

        val optionWidth =
            PANEL_WIDTH -
                84f

        IvySummonChoice.entries
            .forEachIndexed {
                    index,
                    _ ->

                optionBounds[index]
                    .set(
                        left +
                            42f,

                        bottom +
                            PANEL_HEIGHT -
                            175f -
                            index *
                            (
                                OPTION_HEIGHT +
                                    OPTION_GAP
                                ),

                        optionWidth,
                        OPTION_HEIGHT
                    )
            }
    }

    companion object {

        private const val PANEL_WIDTH =
            760f

        private const val PANEL_HEIGHT =
            520f

        private const val OPTION_HEIGHT =
            92f

        private const val OPTION_GAP =
            18f
    }
}
