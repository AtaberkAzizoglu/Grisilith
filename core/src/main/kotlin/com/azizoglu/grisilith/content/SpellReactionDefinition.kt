package com.azizoglu.grisilith.content

data class SpellReactionDefinition(
    val type: SpellReactionType,
    val requiredDamageTypes: Set<DamageType>,
    val displayName: String,
    val description: String
) {
    init {
        require(
            requiredDamageTypes.size >= 2
        ) {
            "A spell reaction requires at least two damage types."
        }
    }
}
