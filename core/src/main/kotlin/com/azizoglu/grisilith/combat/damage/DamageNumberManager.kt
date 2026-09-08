package com.azizoglu.grisilith.combat.damage

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.map.TroubleAtHomeDefenseRuntime
import com.azizoglu.grisilith.summon.SummonManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import kotlin.math.sin

class DamageNumberManager {
    private data class ActiveDamageNumber(
        val amount: Int,
        val startX: Float,
        val startY: Float,
        val isCriticalHit: Boolean,
        val horizontalOffset: Float,
        var elapsedSeconds: Float = 0f
    )

    private val activeNumbers = mutableListOf<ActiveDamageNumber>()
    private var sequence = 0

    fun collectFrom(enemies: List<Enemy>) {
        enemies.forEach { enemy ->
            collectEvents(enemy.consumeDamageNumberEvents())
        }
    }

    fun collectFromSummons(summonManager: SummonManager) {
        collectEvents(summonManager.consumeDamageNumberEvents())
    }

    fun collectFromGate(runtime: TroubleAtHomeDefenseRuntime) {
        collectEvents(runtime.consumeDamageNumberEvents())
    }

    private fun collectEvents(events: List<DamageNumberEvent>) {
        events.forEach { event ->
            activeNumbers.add(
                ActiveDamageNumber(
                    amount = event.amount,
                    startX = event.worldX,
                    startY = event.worldY,
                    isCriticalHit = event.isCriticalHit,
                    horizontalOffset = when (sequence++ % 5) {
                        0 -> -10f
                        1 -> 8f
                        2 -> -4f
                        3 -> 12f
                        else -> 0f
                    }
                )
            )
        }
    }

    fun update(delta: Float) {
        val iterator = activeNumbers.iterator()
        while (iterator.hasNext()) {
            val number = iterator.next()
            number.elapsedSeconds += delta.coerceAtLeast(0f)
            if (number.elapsedSeconds >= LIFETIME_SECONDS) {
                iterator.remove()
            }
        }
    }

    fun render(
        batch: SpriteBatch,
        font: BitmapFont,
        projectionMatrix: Matrix4,
        enabled: Boolean
    ) {
        if (!enabled || activeNumbers.isEmpty()) return

        batch.projectionMatrix = projectionMatrix
        val originalScaleX = font.data.scaleX
        val originalScaleY = font.data.scaleY
        batch.begin()

        activeNumbers.forEach { number ->
            val progress =
                (number.elapsedSeconds / LIFETIME_SECONDS)
                    .coerceIn(0f, 1f)

            val alpha =
                if (progress < 0.62f) {
                    1f
                } else {
                    (1f - (progress - 0.62f) / 0.38f)
                        .coerceIn(0f, 1f)
                }

            font.data.setScale(
                if (number.isCriticalHit) CRITICAL_SCALE else NORMAL_SCALE
            )

            font.color =
                if (number.isCriticalHit) {
                    Color(1f, 0.88f, 0.12f, alpha)
                } else {
                    Color(1f, 1f, 1f, alpha)
                }

            font.draw(
                batch,
                number.amount.toString(),
                number.startX +
                    sin(progress * 3.1415927f) *
                    number.horizontalOffset,
                number.startY +
                    RISE_DISTANCE *
                    progress,
                0f,
                Align.center,
                false
            )
        }

        font.data.setScale(originalScaleX, originalScaleY)
        font.color = Color.WHITE
        batch.end()
    }

    fun clear() {
        activeNumbers.clear()
    }

    companion object {
        private const val LIFETIME_SECONDS = 0.75f
        private const val RISE_DISTANCE = 54f
        private const val NORMAL_SCALE = 1.05f
        private const val CRITICAL_SCALE = 1.22f
    }
}
