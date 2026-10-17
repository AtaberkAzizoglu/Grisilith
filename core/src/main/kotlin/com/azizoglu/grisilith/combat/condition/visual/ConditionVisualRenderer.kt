package com.azizoglu.grisilith.combat.condition.visual

import com.azizoglu.grisilith.combat.condition.ConditionContainer
import com.azizoglu.grisilith.content.ConditionType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class ConditionVisualRenderer {
    private val batch = SpriteBatch()
    private val textures = mutableMapOf<String, Texture>()

    private val stableOrder = listOf(
        ConditionType.BURNED, ConditionType.WET, ConditionType.CORRODING,
        ConditionType.POISONED, ConditionType.NECROSIS, ConditionType.ROOTED,
        ConditionType.PARALYZED, ConditionType.XXX_CURSE,
        ConditionType.FRIGHTENED, ConditionType.STUNNED
    )

    fun renderGroundVisuals(
        conditions: ConditionContainer,
        centerX: Float,
        feetY: Float,
        targetVisualWidth: Float,
        camera: OrthographicCamera
    ) {
        val active = activeVisualTypes(conditions)
        if (active.isEmpty()) return

        batch.projectionMatrix = camera.combined
        batch.begin()
        val alpha = if (active.size > 1) 0.68f else 0.90f
        batch.setColor(1f, 1f, 1f, alpha)

        active.forEachIndexed { index, type ->
            val d = ConditionVisualRegistry.definitionFor(type) ?: return@forEachIndexed
            val texture = textureFor(d.visualTexturePath)
            val frameWidth = texture.width / d.frameCount
            val frame = ((visualClockSeconds() / d.frameDurationSeconds).toInt() + index) % d.frameCount
            val region =
                TextureRegion(
                    texture,
                    frame * frameWidth,
                    0,
                    frameWidth,
                    texture.height
                )

            /*
             * Condition VFX follows the target's real rendered width instead
             * of using one fixed world-space size for every entity.
             *
             * Example:
             * 45 px visual target -> 45 world-unit condition width
             * 72 px visual target -> 72 world-unit condition width
             *
             * Height is derived from the actual animation frame aspect ratio,
             * so tall effects (Burned/Necrosis/etc.) never get squashed.
             */
            val visualWidth =
                (
                    targetVisualWidth *
                        TARGET_WIDTH_MULTIPLIER
                    )
                    .coerceAtLeast(
                        MIN_VISUAL_WIDTH
                    )

            val frameAspectRatio =
                texture.height.toFloat() /
                    frameWidth.toFloat()

            val visualHeight =
                visualWidth *
                    frameAspectRatio

            val x =
                centerX -
                    visualWidth /
                    2f

            val y =
                feetY -
                    visualHeight *
                    FEET_OVERLAP_RATIO

            batch.draw(
                region,
                x,
                y,
                visualWidth,
                visualHeight
            )
        }

        batch.setColor(Color.WHITE)
        batch.end()
    }

    fun renderSymbols(
        conditions: ConditionContainer,
        centerX: Float,
        symbolY: Float,
        camera: OrthographicCamera,
        leadingIcons: List<String> = emptyList()
    ) {
        val active = symbolTypes(conditions)
        if (active.isEmpty() && leadingIcons.isEmpty()) return

        val size = 15f
        val gap = 2f
        val count = active.size + leadingIcons.size
        val totalWidth = count * size + (count - 1).coerceAtLeast(0) * gap
        var x = centerX - totalWidth / 2f

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(Color.WHITE)
        leadingIcons.forEach { path ->
            batch.draw(textureFor(path), x, symbolY, size, size)
            x += size + gap
        }
        active.forEach { type ->
            if(type==ConditionType.ARMOR_BREAK) {
                com.azizoglu.grisilith.ui.stat.SemanticSymbolRenderer.drawIcon(batch,
                    com.azizoglu.grisilith.ui.stat.SemanticSymbolRenderer.ARMOR_BREAK,x,symbolY,size)
            }else {
                val d = ConditionVisualRegistry.definitionFor(type) ?: return@forEach
                batch.draw(textureFor(d.symbolTexturePath), x, symbolY, size, size)
            }
            x += size + gap
        }
        batch.end()
    }

    fun symbolTypes(conditions:ConditionContainer):List<ConditionType> =
        (if(conditions.contains(ConditionType.ARMOR_BREAK))listOf(ConditionType.ARMOR_BREAK) else emptyList())+activeVisualTypes(conditions)

    private fun activeVisualTypes(conditions: ConditionContainer): List<ConditionType> =
        stableOrder.filter { conditions.contains(it) && ConditionVisualRegistry.definitionFor(it) != null }

    private fun visualClockSeconds(): Float =
        (System.nanoTime() / 1_000_000_000.0).toFloat()

    private fun textureFor(path: String): Texture =
        textures.getOrPut(path) {
            Texture(Gdx.files.internal(path)).also {
                it.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
            }
        }

    companion object {
        /*
         * 1.0 = condition visual width is equal to the entity's visual width.
         * Increase slightly later if you want every effect to spill outside
         * the body silhouette.
         */
        private const val TARGET_WIDTH_MULTIPLIER =
            1.0f

        private const val MIN_VISUAL_WIDTH =
            24f

        /*
         * Keeps the base of the VFX slightly below the feet so ground effects
         * visually wrap around the target instead of floating above it.
         */
        private const val FEET_OVERLAP_RATIO =
            0.18f
    }

    fun dispose() {
        textures.values.forEach(Texture::dispose)
        textures.clear()
        batch.dispose()
    }
}
