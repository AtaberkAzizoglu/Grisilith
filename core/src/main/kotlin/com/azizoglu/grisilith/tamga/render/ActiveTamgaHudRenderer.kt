package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.tamga.runtime.active.ActiveTamgaState
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.ceil

class ActiveTamgaHudRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {

    companion object {
        private const val BUTTON_SIZE = 72f
        private const val BUTTON_GAP = 16f
        private const val BOTTOM_MARGIN = 24f
        private const val BORDER_WIDTH = 3f
    }

    private val weaponButtonBounds =
        Rectangle()

    private val armorButtonBounds =
        Rectangle()

    fun render(
        showWeaponButton: Boolean,
        weaponState: ActiveTamgaState?,
        weaponRemainingActiveSeconds: Float,
        weaponRemainingCooldownSeconds: Float,
        showArmorButton: Boolean,
        armorState: ActiveTamgaState?,
        armorRemainingActiveSeconds: Float,
        armorRemainingCooldownSeconds: Float
    ) {
        updateButtonBounds(
            showWeaponButton = showWeaponButton,
            showArmorButton = showArmorButton
        )

        if (!showWeaponButton && !showArmorButton) {
            return
        }

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        if (showWeaponButton) {
            drawButtonBackground(
                bounds = weaponButtonBounds,
                state = weaponState
            )
        }

        if (showArmorButton) {
            drawButtonBackground(
                bounds = armorButtonBounds,
                state = armorState
            )
        }

        shapeRenderer.end()

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        GdxLineWidth.set(BORDER_WIDTH)

        if (showWeaponButton) {
            drawButtonBorder(
                bounds = weaponButtonBounds,
                state = weaponState
            )
        }

        if (showArmorButton) {
            drawButtonBorder(
                bounds = armorButtonBounds,
                state = armorState
            )
        }

        shapeRenderer.end()

        GdxLineWidth.reset()

        batch.projectionMatrix =
            camera.combined

        batch.begin()

        if (showWeaponButton) {
            drawButtonText(
                bounds = weaponButtonBounds,
                shortcut = "Z",
                symbol = "CH",
                state = weaponState,
                remainingActiveSeconds =
                    weaponRemainingActiveSeconds,
                remainingCooldownSeconds =
                    weaponRemainingCooldownSeconds
            )
        }

        if (showArmorButton) {
            drawButtonText(
                bounds = armorButtonBounds,
                shortcut = "X",
                symbol = "PS",
                state = armorState,
                remainingActiveSeconds =
                    armorRemainingActiveSeconds,
                remainingCooldownSeconds =
                    armorRemainingCooldownSeconds
            )
        }

        batch.end()
    }

    fun isWeaponButtonHit(
        worldX: Float,
        worldY: Float
    ): Boolean {
        return weaponButtonBounds
            .contains(
                worldX,
                worldY
            )
    }

    fun isArmorButtonHit(
        worldX: Float,
        worldY: Float
    ): Boolean {
        return armorButtonBounds
            .contains(
                worldX,
                worldY
            )
    }

    private fun updateButtonBounds(
        showWeaponButton: Boolean,
        showArmorButton: Boolean
    ) {
        weaponButtonBounds.set(
            0f,
            0f,
            0f,
            0f
        )

        armorButtonBounds.set(
            0f,
            0f,
            0f,
            0f
        )

        val visibleCount =
            listOf(
                showWeaponButton,
                showArmorButton
            )
                .count { visible ->
                    visible
                }

        if (visibleCount <= 0) {
            return
        }

        val totalWidth =
            visibleCount * BUTTON_SIZE +
                (visibleCount - 1) * BUTTON_GAP

        var x =
            camera.position.x -
                totalWidth / 2f

        val y =
            camera.position.y -
                cameraHeight / 2f +
                BOTTOM_MARGIN

        if (showWeaponButton) {
            weaponButtonBounds.set(
                x,
                y,
                BUTTON_SIZE,
                BUTTON_SIZE
            )

            x +=
                BUTTON_SIZE +
                    BUTTON_GAP
        }

        if (showArmorButton) {
            armorButtonBounds.set(
                x,
                y,
                BUTTON_SIZE,
                BUTTON_SIZE
            )
        }
    }

    private fun drawButtonBackground(
        bounds: Rectangle,
        state: ActiveTamgaState?
    ) {
        shapeRenderer.color =
            when (state) {
                ActiveTamgaState.READY ->
                    Color(
                        0.12f,
                        0.30f,
                        0.16f,
                        0.92f
                    )

                ActiveTamgaState.ACTIVE ->
                    Color(
                        0.20f,
                        0.42f,
                        0.68f,
                        0.95f
                    )

                ActiveTamgaState.COOLDOWN ->
                    Color(
                        0.16f,
                        0.16f,
                        0.19f,
                        0.90f
                    )

                null ->
                    Color.DARK_GRAY
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )
    }

    private fun drawButtonBorder(
        bounds: Rectangle,
        state: ActiveTamgaState?
    ) {
        shapeRenderer.color =
            when (state) {
                ActiveTamgaState.READY ->
                    Color.LIME

                ActiveTamgaState.ACTIVE ->
                    Color.CYAN

                ActiveTamgaState.COOLDOWN ->
                    Color.GRAY

                null ->
                    Color.DARK_GRAY
            }

        shapeRenderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        )
    }

    private fun drawButtonText(
        bounds: Rectangle,
        shortcut: String,
        symbol: String,
        state: ActiveTamgaState?,
        remainingActiveSeconds: Float,
        remainingCooldownSeconds: Float
    ) {
        font.color =
            Color.WHITE

        font.draw(
            batch,
            symbol,
            bounds.x + 9f,
            bounds.y + bounds.height - 13f
        )

        font.draw(
            batch,
            "[$shortcut]",
            bounds.x + bounds.width - 32f,
            bounds.y + bounds.height - 13f
        )

        val statusText =
            when (state) {
                ActiveTamgaState.READY ->
                    "READY"

                ActiveTamgaState.ACTIVE ->
                    "${ceil(remainingActiveSeconds).toInt()}s"

                ActiveTamgaState.COOLDOWN ->
                    "${ceil(remainingCooldownSeconds).toInt()}s"

                null ->
                    ""
            }

        font.draw(
            batch,
            statusText,
            bounds.x + 9f,
            bounds.y + 20f
        )
    }
}

private object GdxLineWidth {
    fun set(width: Float) {
        com.badlogic.gdx.Gdx.gl.glLineWidth(width)
    }

    fun reset() {
        com.badlogic.gdx.Gdx.gl.glLineWidth(1f)
    }
}
