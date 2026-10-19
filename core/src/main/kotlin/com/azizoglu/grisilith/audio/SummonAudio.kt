package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.summon.SummonType

/** Melee effort cues only; Imp launch audio comes from the shared projectile system. */
object SummonAudio {
    fun attackCue(type:SummonType):UiSound? = when(type) {
        SummonType.COW -> UiSound.COW_ATTACK
        SummonType.KNIGHT_SPIRIT -> UiSound.OATHBREAKER_ATTACK
        SummonType.GUARD -> UiSound.LANCE_THRUST
        else -> null
    }
    fun deathCue(type:SummonType):UiSound? = when(type) {
        SummonType.COW -> UiSound.COW_DEATH
        SummonType.IMP -> UiSound.IMP_DEATH
        SummonType.KNIGHT_SPIRIT -> UiSound.OATHBREAKER_DEATH
        SummonType.TOAD -> UiSound.TOAD_DEATH
        SummonType.GUARD -> UiSound.GUARD_DEATH
        else -> null
    }
    fun attack(type:SummonType,x:Float,y:Float) {attackCue(type)?.let{GameAudio.playAt(it,x,y)}}
    fun death(type:SummonType,x:Float,y:Float) {deathCue(type)?.let{GameAudio.playAt(it,x,y)}}
}
