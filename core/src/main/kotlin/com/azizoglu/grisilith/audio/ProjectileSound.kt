package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.content.DamageType

/** Source-independent launch cues. Weapons, summons and future emitters share cached assets. */
enum class ProjectileSound(val cue: UiSound) {
    ARROW(UiSound.BOW_SHOT), BOLT(UiSound.CROSSBOW_SHOT), DAGGER(UiSound.DAGGER_THROW),
    STONE(UiSound.SLING_THROW), BOOMERANG(UiSound.BOOMERANG_THROW), ARROW_VOLLEY(UiSound.BOW_VOLLEY),
    AIR(UiSound.MAGIC_AIR), WATER(UiSound.MAGIC_WATER), FIRE(UiSound.MAGIC_FIRE),
    ACID(UiSound.MAGIC_ACID), NECROTIC(UiSound.MAGIC_NECROTIC),
    LIGHTNING(UiSound.MAGIC_LIGHTNING), POISON(UiSound.MAGIC_POISON);

    companion object {
        /** Resolve the final projectile element, including Chaos Theory conversions. */
        fun magical(type: DamageType): ProjectileSound? = when (type) {
            DamageType.AIR -> AIR
            DamageType.WATER -> WATER
            DamageType.FIRE -> FIRE
            DamageType.ACID -> ACID
            DamageType.NECROTIC -> NECROTIC
            DamageType.LIGHTNING -> LIGHTNING
            DamageType.POISON -> POISON
            else -> null
        }
    }
}
