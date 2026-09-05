package com.azizoglu.grisilith.character.trait

enum class CharacterTraitId {
    NECROMANCY,
    THE_HYPOCRISY,
    HURRY,
    BLOODWEIGHT,
    THE_TOAD_CYCLE
}

data class CharacterTraitDefinition(
    val id: CharacterTraitId,
    val displayName: String,
    val description: String
) {
    init {
        require(displayName.isNotBlank())
        require(description.isNotBlank())
    }
}
