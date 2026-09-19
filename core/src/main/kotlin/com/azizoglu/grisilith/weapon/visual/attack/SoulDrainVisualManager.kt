package com.azizoglu.grisilith.weapon.visual.attack

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player

object SoulDrainVisualManager {
    private val events = mutableListOf<SoulDrainVisualEvent>()

    val activeEvents: List<SoulDrainVisualEvent>
        get() = events

    fun spawn(
        enemy: Enemy,
        player: Player
    ) {
        events.add(
            SoulDrainVisualEvent(
                enemy = enemy,
                player = player
            )
        )
    }

    fun update(delta: Float) {
        events.forEach { event ->
            event.update(delta)
        }

        events.removeAll { event ->
            event.isFinished
        }
    }

    fun clear() {
        events.clear()
    }
}
