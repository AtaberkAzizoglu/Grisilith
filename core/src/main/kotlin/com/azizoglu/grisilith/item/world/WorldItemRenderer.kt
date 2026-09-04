package com.azizoglu.grisilith.item.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class WorldItemRenderer {
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val shapes = ShapeRenderer()
    private val textures = mutableMapOf<String, Texture>()

    fun render(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        batch.projectionMatrix = camera.combined
        batch.begin()

        snapshot.traps.filter { it.stateIndex >= 0 }.forEach {
            drawFrame("item_world/pit_trap_sheet.png", 5, 1, it.stateIndex, it.x, it.y, 100f, 102f)
        }
        snapshot.loot.forEach {
            val path = when (it.kind) {
                LootDropKind.BEEF -> "item_world/beef.png"
                LootDropKind.HEAL_POTION -> "item_world/heal_potion.png"
                LootDropKind.SOUL_ORB -> "item_world/soul_orb.png"
            }
            val bob = kotlin.math.sin(it.ageSeconds * 4f) * 2.5f
            val dimensions = when (it.kind) {
                LootDropKind.BEEF -> 45f to 30f
                LootDropKind.HEAL_POTION -> 30f to 35f
                LootDropKind.SOUL_ORB -> 34f to 34f
            }
            drawTexture(path, it.x, it.y + bob, dimensions.first, dimensions.second)
        }

        snapshot.totem?.let { totem ->
            if (snapshot.healingAuraRemainingSeconds > 0f) {
                val frame = (totem.animationSeconds / 0.16f).toInt().mod(4)
                drawFrame("item_world/healing_aura_sheet.png", 2, 2, frame, totem.x, totem.y, 290f, 313f)
            }
            if (snapshot.protectersAreaRemainingSeconds > 0f) {
                val frame = (totem.animationSeconds / 0.13f).toInt().mod(4)
                drawFrame("item_world/protecters_area_sheet.png", 4, 1, frame, totem.x, totem.y, 360f, 321f)
            }
            if (totem.showChargeArea) {
                drawTexture("item_world/bone_totem_charge_area.png", totem.x, totem.y, 310f, 310f, 0.52f)
            }
        }

        snapshot.portal?.let {
            val frame = (it.animationSeconds / 0.12f).toInt().mod(8)
            drawFrame("item_world/portal_sheet.png", 4, 2, frame, it.x, it.y + 54f, 112f, 150f)
        }
        snapshot.merchant?.let {
            val frame = (it.animationSeconds / 0.32f).toInt().mod(6)
            drawFrame("item_world/merchant_sheet.png", 3, 2, frame, it.x, it.y + 42f, 84f, 121f)
        }
        snapshot.brooms.forEach {
            val frame = (it.animationSeconds / 0.14f).toInt().mod(4)
            drawFrame("item_world/animated_broom_sheet.png", 4, 1, frame, it.x, it.y + 20f, 70f, 80f)
        }

        snapshot.totem?.let { totem ->
            drawFrame("item_world/bone_totem_sheet.png", 4, 1, totem.variant, totem.x, totem.y + 62f, 44f, 160f)
        }
        batch.end()

        renderConstructionAndBoundaries(snapshot, camera)
        renderTotemUi(snapshot, camera)
        renderBlindFaith(snapshot, camera)
    }

    private fun renderConstructionAndBoundaries(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Line)
        snapshot.traps.filter { it.stateIndex < 0 }.forEach {
            shapes.color = Color(0.72f, 0.52f, 0.22f, 0.85f)
            shapes.circle(it.x, it.y, 100f, 64)
        }
        snapshot.totem?.takeIf { it.showChargeArea }?.let {
            shapes.color = Color(0.9f, 0.94f, 1f, 0.9f)
            shapes.circle(it.x, it.y, 150f, 72)
        }
        if (snapshot.shrinkCenterX != null && snapshot.shrinkCenterY != null && snapshot.shrinkRadius != null) {
            shapes.color = Color(0.68f, 0.10f, 0.16f, 0.9f)
            shapes.circle(snapshot.shrinkCenterX, snapshot.shrinkCenterY, snapshot.shrinkRadius, 120)
        }
        shapes.end()
    }

    private fun renderTotemUi(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        val totem = snapshot.totem ?: return
        val label = totem.rouletteLabel ?: totem.displayedResult?.label ?: return
        val width = 146f
        val height = 42f
        val x = totem.x - width / 2f
        val y = totem.y + 105f

        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Color(0.05f, 0.04f, 0.07f, 0.92f)
        shapes.rect(x, y, width, height)
        shapes.end()
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = if (totem.rouletteRemainingSeconds > 0f) Color.GOLD else Color.WHITE
        shapes.rect(x, y, width, height)
        shapes.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        font.color = Color.WHITE
        font.data.setScale(0.72f)
        font.draw(batch, label, x + 8f, y + 25f, width - 16f, 1, false)
        batch.end()
    }

    private fun renderBlindFaith(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        if (snapshot.blindFaithMistLevel <= 0) return
        val texture = texture("item_world/blind_faith_mist_sheet.png")
        val level = (snapshot.blindFaithMistLevel - 1).coerceIn(0, 3)
        val frame = (snapshot.blindFaithAnimationSeconds / 0.22f).toInt().mod(4)
        val xs = intArrayOf(159, 465, 805, 1197)
        val ys = intArrayOf(20, 253, 486, 718)
        val region = TextureRegion(texture, xs[frame], ys[level], 260, 216)
        val width = camera.viewportWidth * camera.zoom
        val height = camera.viewportHeight * camera.zoom
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(1f, 1f, 1f, 0.92f)
        batch.draw(region, camera.position.x - width / 2f, camera.position.y - height / 2f, width, height)
        batch.setColor(Color.WHITE)
        batch.end()
    }

    private fun drawFrame(path: String, columns: Int, rows: Int, frame: Int, centerX: Float, centerY: Float, width: Float, height: Float) {
        val texture = texture(path)
        val cellWidth = texture.width / columns
        val cellHeight = texture.height / rows
        val safeFrame = frame.coerceIn(0, columns * rows - 1)
        val region = TextureRegion(texture, safeFrame.mod(columns) * cellWidth, safeFrame / columns * cellHeight, cellWidth, cellHeight)
        batch.setColor(Color.WHITE)
        batch.draw(region, centerX - width / 2f, centerY - height / 2f, width, height)
    }

    private fun drawTexture(path: String, centerX: Float, centerY: Float, width: Float, height: Float, alpha: Float = 1f) {
        batch.setColor(1f, 1f, 1f, alpha)
        batch.draw(texture(path), centerX - width / 2f, centerY - height / 2f, width, height)
        batch.setColor(Color.WHITE)
    }

    private fun texture(path: String): Texture = textures.getOrPut(path) {
        Texture(Gdx.files.internal(path)).also {
            it.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
    }

    fun dispose() {
        textures.values.forEach(Texture::dispose)
        textures.clear()
        font.dispose()
        batch.dispose()
        shapes.dispose()
    }
}
