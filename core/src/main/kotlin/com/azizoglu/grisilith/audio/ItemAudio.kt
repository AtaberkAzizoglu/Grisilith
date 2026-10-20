package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.item.world.TotemResult

/** Semantic item events; callers report real outcomes, not attempts or passive stat ticks. */
object ItemAudio {
    fun at(cue:UiSound,x:Float,y:Float) = GameAudio.effectAt(cue,x,y)

    fun totemResult(result:TotemResult?,x:Float,y:Float) {
        val cue=when(result) {
            TotemResult.HEALING_AURA -> UiSound.ITEM_HEALING_AURA
            TotemResult.PROTECTERS_AREA -> UiSound.ITEM_PROTECTERS
            TotemResult.BURRIED_TREASURE -> UiSound.ITEM_TREASURE
            TotemResult.HOURGLASS -> UiSound.ITEM_HOURGLASS
            TotemResult.TOADS -> UiSound.ITEM_TOADS
            TotemResult.HALF_GOLD -> UiSound.ITEM_LOSE_GOLD
            TotemResult.HOSTILE_MINI_BOSS -> UiSound.ITEM_HOSTILE_BOSS
            TotemResult.SHRINK_MAP -> UiSound.ITEM_SHRINK
            TotemResult.TOTEM_CURSE -> UiSound.ITEM_CURSE
            null -> return
        }
        at(cue,x,y)
    }

    fun broomInterval(copies:Int)=(3f-.3f*(copies.coerceAtLeast(1)-1)).coerceAtLeast(.3f)
}
