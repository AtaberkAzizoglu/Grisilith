package com.azizoglu.grisilith.combat.healing

import com.azizoglu.grisilith.combat.HealingVisualType

data class HealingVisualAnchor(
    val centerX: Float,
    val centerY: Float,
    val ownerSize: Float
)

data class HealingVisualEvent(
    val type: HealingVisualType,
    private val initialAnchor: HealingVisualAnchor,
    private val anchorProvider: (() -> HealingVisualAnchor)? = null,
    var elapsedSeconds: Float = 0f
) {
    fun currentAnchor(): HealingVisualAnchor = anchorProvider?.invoke() ?: initialAnchor

    val progress: Float
        get() = (elapsedSeconds / DURATION_SECONDS).coerceIn(0f, 0.999f)

    val isFinished: Boolean
        get() = elapsedSeconds >= DURATION_SECONDS

    companion object {
        const val DURATION_SECONDS = 0.64f
    }
}

object HealingVisualManager {
    private val events = mutableListOf<HealingVisualEvent>()

    val activeEvents: List<HealingVisualEvent>
        get() = events

    fun spawn(type: HealingVisualType, centerX: Float, centerY: Float, ownerSize: Float) {
        events += HealingVisualEvent(
            type = type,
            initialAnchor = HealingVisualAnchor(centerX, centerY, ownerSize)
        )
    }

    fun spawnFollowing(type: HealingVisualType, anchorProvider: () -> HealingVisualAnchor) {
        events += HealingVisualEvent(
            type = type,
            initialAnchor = anchorProvider(),
            anchorProvider = anchorProvider
        )
    }

    fun update(delta: Float) {
        events.forEach { it.elapsedSeconds += delta.coerceAtLeast(0f) }
        events.removeAll { it.isFinished }
    }

    fun clear() = events.clear()
}
