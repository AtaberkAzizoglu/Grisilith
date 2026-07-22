package com.azizoglu.grisilith.content

object DefaultSpellReactions {

    val all: List<SpellReactionDefinition> =
        listOf(
            SpellReactionDefinition(
                type =
                    SpellReactionType
                        .FIRE_ACID_EXPLOSION,
                requiredDamageTypes =
                    setOf(
                        DamageType.FIRE,
                        DamageType.ACID
                    ),
                displayName =
                    "Combustive Acid",
                description =
                    "Fire detonates an active Acid pool and causes it to apply Burned."
            ),

            SpellReactionDefinition(
                type =
                    SpellReactionType
                        .WATER_LIGHTNING_OVERCHARGE,
                requiredDamageTypes =
                    setOf(
                        DamageType.WATER,
                        DamageType.LIGHTNING
                    ),
                displayName =
                    "Overcharge",
                description =
                    "Lightning deals increased damage to targets affected by Wet."
            ),

            SpellReactionDefinition(
                type =
                    SpellReactionType
                        .EARTH_COLD_SHATTER,
                requiredDamageTypes =
                    setOf(
                        DamageType.EARTH,
                        DamageType.COLD
                    ),
                displayName =
                    "Frozen Fracture",
                description =
                    "Earth deals increased damage to targets slowed by Absolute Zero."
            ),

            SpellReactionDefinition(
                type =
                    SpellReactionType
                        .AIR_NECROTIC_CORRUPTION,
                requiredDamageTypes =
                    setOf(
                        DamageType.AIR,
                        DamageType.NECROTIC
                    ),
                displayName =
                    "Corrupted Vortex",
                description =
                    "Erasing the Soul may corrupt Vortex Pull and apply Necrosis to every enemy inside."
            )
        )
}
