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
    private var lastFeedbackAt = Long.MIN_VALUE / 2

    // Real-time presentation budget, shared by heal and life steal; HP changes are never throttled.
    private fun feedback(anchor: HealingVisualAnchor): Boolean {
        val now = com.azizoglu.grisilith.audio.GameAudio.realTimeMillis()
        if (now >= lastFeedbackAt && now - lastFeedbackAt < 250L) return false
        lastFeedbackAt = now
        com.azizoglu.grisilith.audio.GameAudio.playAt(
            com.azizoglu.grisilith.audio.UiSound.HEAL, anchor.centerX, anchor.centerY, anchor.ownerSize / 2f)
        return true
    }

    val activeEvents: List<HealingVisualEvent>
        get() = events

    fun spawn(type: HealingVisualType, centerX: Float, centerY: Float, ownerSize: Float) {
        if (!feedback(HealingVisualAnchor(centerX, centerY, ownerSize))) return
        events += HealingVisualEvent(
            type = type,
            initialAnchor = HealingVisualAnchor(centerX, centerY, ownerSize)
        )
    }

    fun spawnFollowing(type: HealingVisualType, anchorProvider: () -> HealingVisualAnchor) {
        val anchor = anchorProvider()
        if (!feedback(anchor)) return
        events += HealingVisualEvent(
            type = type,
            initialAnchor = anchor,
            anchorProvider = anchorProvider
        )
    }

    fun update(delta: Float) {
        events.forEach { it.elapsedSeconds += delta.coerceAtLeast(0f) }
        events.removeAll { it.isFinished }
    }

    fun clear() { events.clear(); lastFeedbackAt = Long.MIN_VALUE / 2 }
}
