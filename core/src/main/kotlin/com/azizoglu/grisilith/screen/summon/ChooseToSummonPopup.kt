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

        shapeRenderer.color =
            Color(
                0.08f,
                0.055f,
                0.11f,
                0.98f
            )

        shapeRenderer.rect(
            left,
            bottom,
            PANEL_WIDTH,
            PANEL_HEIGHT
        )

        shapeRenderer.color =
            Color(
                0.46f,
                0.76f,
                0.26f,
                1f
            )

        shapeRenderer.rectLine(
            left,
            bottom,
            left +
                PANEL_WIDTH,
            bottom,
            4f
        )

        optionBounds.forEach {
                bounds ->

            shapeRenderer.color =
                if (
                    bounds.contains(
                        pointer
                    )
                ) {
                    Color(
                        0.30f,
                        0.42f,
                        0.20f,
                        1f
                    )
                } else {
                    Color(
                        0.16f,
                        0.12f,
                        0.19f,
                        1f
                    )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        shapeRenderer.end()

        batch.projectionMatrix =
            camera.combined

        batch.begin()

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

        font.data.setScale(
            0.80f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            weapon.displayName +
                " reached Level V. This choice belongs only to this weapon.",

            left +
                42f,
            bottom +
                PANEL_HEIGHT -
                82f,
            PANEL_WIDTH -
                84f,
            Align.center,
            true
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
                    "${index + 1}. ${choice.displayName}",
                    bounds.x +
                        18f,
                    bounds.y +
                        bounds.height -
                        22f
                )

                font.data.setScale(
                    0.74f
                )

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    choice.description,
                    bounds.x +
                        18f,
                    bounds.y +
                        bounds.height -
                        49f,
                    bounds.width -
                        36f,
                    Align.left,
                    true
                )
            }

        font.data.setScale(
            0.72f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "The stage will begin after a choice is made.",
            left,
            bottom +
                24f,
            PANEL_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            1f
        )

        batch.end()

        Gdx.gl.glDisable(
            com.badlogic.gdx.graphics.GL20.GL_BLEND
        )
    }

    private fun commit(
        weapon: WeaponInstance,
        choice: IvySummonChoice
    ) {

        weapon.chooseIvySummon(
            choice
        )

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
                            150f -
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
