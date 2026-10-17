package com.azizoglu.grisilith.content

enum class DamageType(
    val displayName: String,
    val canCriticallyHit: Boolean
) {
    NONE(
        displayName = "None",
        canCriticallyHit = false
    ),

    SLASHING(
        displayName = "Slashing",
        canCriticallyHit = true
    ),

    PIERCING(
        displayName = "Piercing",
        canCriticallyHit = true
    ),

    BLUDGEONING(
        displayName = "Bludgeoning",
        canCriticallyHit = true
    ),

    AIR(
        displayName = "Air",
        canCriticallyHit = false
    ),

    WATER(
        displayName = "Water",
        canCriticallyHit = false
    ),

    FIRE(
        displayName = "Fire",
        canCriticallyHit = false
    ),

    EARTH(
        displayName = "Earth",
        canCriticallyHit = false
    ),

    POISON(
        displayName = "Poison",
        canCriticallyHit = false
    ),

    ACID(
        displayName = "Acid",
        canCriticallyHit = false
    ),

    LIGHTNING(
        displayName = "Lightning",
        canCriticallyHit = false
    ),

    NECROTIC(
        displayName = "Necrotic",
        canCriticallyHit = false
    ),

    COLD(
        displayName = "Cold",
        canCriticallyHit = false
    );

    val isPhysical: Boolean
        get() = this == SLASHING || this == PIERCING || this == BLUDGEONING

    /** Earth, Cold and untyped damage deliberately bypass this defense. */
    val isMagical: Boolean
        get() = this == FIRE || this == AIR || this == WATER ||
            this == LIGHTNING || this == NECROTIC || this == ACID || this == POISON
}
