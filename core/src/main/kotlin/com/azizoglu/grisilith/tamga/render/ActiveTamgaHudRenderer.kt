package com.azizoglu.grisilith.tamga.render

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.input.RunInputBindings
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.tamga.runtime.active.ActiveTamgaState
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import kotlin.math.ceil

class ActiveTamgaHudRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    @Suppress("UNUSED_PARAMETER") shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont
) {
    companion object {
        private const val ICON_SIZE = 48f
        private const val ICON_GAP = 8f
        private const val GROUP_GAP = 30f
        private const val BOTTOM_MARGIN = 18f
        private const val WEAPON_GROUP_WIDTH = 338f
    }

    private val weaponButtonBounds = Rectangle()
    private val armorButtonBounds = Rectangle()

    fun render(
        showWeaponButton: Boolean,
        weaponContentId: String?,
        weaponState: ActiveTamgaState?,
        weaponRemainingActiveSeconds: Float,
        weaponRemainingCooldownSeconds: Float,
        showArmorButton: Boolean,
        armorContentId: String?,
        armorState: ActiveTamgaState?,
        armorRemainingActiveSeconds: Float,
        armorRemainingCooldownSeconds: Float
    ) {
        updateButtonBounds(showWeaponButton, showArmorButton)
        if (!showWeaponButton && !showArmorButton) return

        batch.projectionMatrix = camera.combined
        batch.begin()
        if (showWeaponButton && weaponContentId != null) {
            drawButton(weaponButtonBounds, weaponContentId, RunInputBindings.weaponTamgaLabel(),
                weaponState, weaponRemainingActiveSeconds, weaponRemainingCooldownSeconds)
        }
        if (showArmorButton && armorContentId != null) {
            drawButton(armorButtonBounds, armorContentId, RunInputBindings.armorTamgaLabel(),
                armorState, armorRemainingActiveSeconds, armorRemainingCooldownSeconds)
        }
        batch.end()
    }

    fun isWeaponButtonHit(worldX: Float, worldY: Float): Boolean =
        weaponButtonBounds.contains(worldX, worldY)

    fun isArmorButtonHit(worldX: Float, worldY: Float): Boolean =
        armorButtonBounds.contains(worldX, worldY)

    private fun drawButton(
        bounds: Rectangle,
        contentId: String,
        shortcut: String,
        state: ActiveTamgaState?,
        remainingActiveSeconds: Float,
        remainingCooldownSeconds: Float
    ) {
        ContentIconRenderer.draw(
            batch = batch,
            contentId = contentId,
            category = ContentCategory.TAMGA,
            bounds = bounds,
            alpha = if (state == ActiveTamgaState.COOLDOWN) 0.62f else 1f,
            framed = false
        )

        when (state) {
            ActiveTamgaState.READY -> UiComponentRenderer.drawActiveAbilityFrame(
                batch, bounds, Color(0.32f, 0.78f, 1f, 1f))
            ActiveTamgaState.ACTIVE -> UiComponentRenderer.drawActiveAbilityFrame(
                batch, bounds, Color(0.42f, 1f, 0.66f, 1f))
            ActiveTamgaState.COOLDOWN -> UiComponentRenderer.drawCooldownOverlay(batch, bounds, 1f)
            null -> Unit
        }

        val oldX = font.data.scaleX
        val oldY = font.data.scaleY
        font.data.setScale(0.58f)
        if (GameSettings.showAbilityShortcuts) {
            font.color = Color(0.80f, 0.92f, 1f, 1f)
            font.draw(batch, "[$shortcut]", bounds.x + 3f, bounds.y + bounds.height - 4f,
                bounds.width - 6f, Align.right, false)
        }
        if (GameSettings.showAbilityCooldowns && state == ActiveTamgaState.COOLDOWN) {
            val remaining = remainingCooldownSeconds
            font.color = Color(1f, 0.86f, 0.62f, 1f)
            font.draw(batch, ceil(remaining.coerceAtLeast(0f)).toInt().toString(),
                bounds.x + 4f, bounds.y + 15f)
        }
        font.data.setScale(oldX, oldY)
    }

    private fun updateButtonBounds(showWeaponButton: Boolean, showArmorButton: Boolean) {
        weaponButtonBounds.set(0f, 0f, 0f, 0f)
        armorButtonBounds.set(0f, 0f, 0f, 0f)
        val count = listOf(showWeaponButton, showArmorButton).count { it }
        if (count == 0) return

        val weaponGroupLeft = camera.position.x - WEAPON_GROUP_WIDTH / 2f
        val totalWidth = count * ICON_SIZE + (count - 1) * ICON_GAP
        var x = weaponGroupLeft - GROUP_GAP - totalWidth
        val y = camera.position.y - cameraHeight / 2f + BOTTOM_MARGIN
        if (showWeaponButton) {
            weaponButtonBounds.set(x, y, ICON_SIZE, ICON_SIZE)
            x += ICON_SIZE + ICON_GAP
        }
        if (showArmorButton) armorButtonBounds.set(x, y, ICON_SIZE, ICON_SIZE)
    }
}
