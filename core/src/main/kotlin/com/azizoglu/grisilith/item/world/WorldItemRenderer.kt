package com.azizoglu.grisilith.item.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import kotlin.math.abs
import kotlin.math.min

class WorldItemRenderer {
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val shapes = ShapeRenderer()
    private val textures = mutableMapOf<String, Texture>()

    fun renderGround(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        batch.projectionMatrix = camera.combined
        batch.begin()
        snapshot.traps.forEach {
            val frame =
                if (it.stateIndex >= 0) it.stateIndex
                else (it.constructionProgress * 3.999f).toInt().coerceIn(0, 3)
            drawFrame(
                "item_world/pit_trap_sheet.png",
                5,
                1,
                frame,
                it.x,
                it.y,
                112f,
                115f
            )
        }
        batch.end()
    }

    fun render(
        snapshot: WorldItemVisualSnapshot,
        camera: OrthographicCamera,
        ghostDarknessActive: Boolean = false
    ) {
        batch.projectionMatrix = camera.combined
        batch.begin()
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
            val frame = (it.animationSeconds / 0.24f).toInt().mod(8)
            drawFrame("item_world/portal_sheet.png", 4, 2, frame, it.x, it.y + 54f, 112f, 150f)
        }
        snapshot.merchant?.let {
            val frame = (it.animationSeconds / 0.64f).toInt().mod(6)
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
        renderProgressBars(snapshot, camera)
        renderTotemUi(snapshot, camera)
        if (ghostDarknessActive) renderGhostDarkness(camera)
        renderBlindFaith(snapshot, camera)
    }

    private fun renderGhostDarkness(camera: OrthographicCamera) {
        val width = camera.viewportWidth * camera.zoom
        val height = camera.viewportHeight * camera.zoom
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Color(0.015f, 0.02f, 0.045f, 0.34f)
        shapes.rect(
            camera.position.x - width / 2f,
            camera.position.y - height / 2f,
            width,
            height
        )
        shapes.end()
    }

    private fun renderConstructionAndBoundaries(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Line)
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

    private fun renderProgressBars(snapshot: WorldItemVisualSnapshot, camera: OrthographicCamera) {
        val bars = mutableListOf<Triple<Float, Float, Float>>()
        snapshot.traps.filter { it.stateIndex < 0 }.forEach {
            bars.add(Triple(it.x, it.y + 66f, it.constructionProgress))
        }
        snapshot.totem?.takeIf { it.showChargeArea }?.let {
            bars.add(Triple(it.x, it.y + 156f, it.chargeProgress))
        }
        if (bars.isEmpty()) return

        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        bars.forEach { (centerX, y, progress) ->
            shapes.color = Color(0.04f, 0.04f, 0.05f, 0.92f)
            shapes.rect(centerX - 47f, y, 94f, 10f)
            shapes.color = Color(0.78f, 0.88f, 1f, 0.96f)
            shapes.rect(centerX - 44f, y + 3f, 88f * progress.coerceIn(0f, 1f), 4f)
        }
        shapes.end()
        shapes.begin(ShapeRenderer.ShapeType.Line)
        shapes.color = Color.WHITE
        bars.forEach { (centerX, y, _) -> shapes.rect(centerX - 47f, y, 94f, 10f) }
        shapes.end()
    }

    fun renderEdgeIndicators(
        snapshot: WorldItemVisualSnapshot,
        enemies: List<Enemy>,
        camera: OrthographicCamera
    ) {
        data class Target(
            val x: Float,
            val y: Float,
            val path: String?,
            val fallbackColor: Color
        )

        val targets = mutableListOf<Target>()
        enemies.filter {
            !it.isDead &&
                (it.type == EnemyType.MINI_BOSS || it.type == EnemyType.FINAL_BOSS)
        }.forEach { enemy ->
            val path = when (enemy.kindId) {
                EnemyKindIds.GRIZZLY_BEAR -> availableTokenPath("item_world/indicators/bear_token.png")
                EnemyKindIds.BEE_SWARM -> availableTokenPath("item_world/indicators/bee_token.png")
                EnemyKindIds.CROCODILE -> availableTokenPath("item_world/indicators/crocodile_token.png")
                EnemyKindIds.SILVERBACK_GORILLA -> availableTokenPath("item_world/indicators/gorilla_token.png")
                EnemyKindIds.SPEARMAN -> availableTokenPath("item_world/indicators/spearman_token.png")
                EnemyKindIds.HORSEMAN -> availableTokenPath("item_world/indicators/horseman_token.png")
                EnemyKindIds.WITCH -> availableTokenPath("item_world/indicators/witch_token.png")
                EnemyKindIds.GHOST -> availableTokenPath("item_world/indicators/ghost_token.png")
                EnemyKindIds.SLIME -> availableTokenPath("item_world/indicators/slime_token.png")
                EnemyKindIds.VAMPIRE_LORD -> availableTokenPath("item_world/indicators/vampire_lord_token.png")
                else -> null
            }
            targets.add(
                Target(
                    enemy.centerX,
                    enemy.centerY,
                    path,
                    Color(0.72f, 0.06f, 0.08f, 0.96f)
                )
            )
        }
        snapshot.merchant?.let {
            targets.add(
                Target(
                    it.x,
                    it.y,
                    availableTokenPath("item_world/indicators/merchant_token.png"),
                    Color(0.08f, 0.68f, 0.28f, 0.96f)
                )
            )
        }
        snapshot.totem?.let {
            targets.add(
                Target(
                    it.x,
                    it.y,
                    availableTokenPath("item_world/indicators/bone_totem_token.png"),
                    Color(0.12f, 0.66f, 0.78f, 0.96f)
                )
            )
        }

        val halfWidth = camera.viewportWidth * camera.zoom / 2f
        val halfHeight = camera.viewportHeight * camera.zoom / 2f
        val margin = 27f * camera.zoom
        val size = 34f * camera.zoom
        val indicators = targets.mapNotNull { target ->
            val dx = target.x - camera.position.x
            val dy = target.y - camera.position.y
            if (abs(dx) <= halfWidth - margin && abs(dy) <= halfHeight - margin) {
                null
            } else {
                val xScale = if (abs(dx) < 0.001f) Float.POSITIVE_INFINITY else (halfWidth - margin) / abs(dx)
                val yScale = if (abs(dy) < 0.001f) Float.POSITIVE_INFINITY else (halfHeight - margin) / abs(dy)
                val scale = min(xScale, yScale)
                target to Vector2(
                    camera.position.x + dx * scale,
                    camera.position.y + dy * scale
                )
            }
        }

        if (indicators.isEmpty()) return
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        indicators.filter { it.first.path == null }.forEach { (target, position) ->
            shapes.color = target.fallbackColor
            shapes.circle(position.x, position.y, size / 2f, 24)
            shapes.color = Color(0.20f, 0.02f, 0.03f, 1f)
            shapes.circle(position.x, position.y, size * 0.28f, 20)
        }
        shapes.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        indicators.filter { it.first.path != null }.forEach { (target, position) ->
            drawTexture(target.path!!, position.x, position.y, size, size)
        }
        batch.end()
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

    private fun availableTokenPath(path: String): String? =
        path.takeIf { Gdx.files.internal(it).exists() }

    fun dispose() {
        textures.values.forEach(Texture::dispose)
        textures.clear()
        font.dispose()
        batch.dispose()
        shapes.dispose()
    }
}
