package com.azizoglu.grisilith.character.trait

object DefaultCharacterTraits {

    val necromancy =
        CharacterTraitDefinition(
            id = CharacterTraitId.NECROMANCY,
            displayName = "Necromancy",
            description =
                "Necrotic damage can trigger Life Steal."
        )

    val theHypocrisy =
        CharacterTraitDefinition(
            id = CharacterTraitId.THE_HYPOCRISY,
            displayName = "The Hypocrisy",
            description =
                "Melee Weapon hits deal a small amount of Fire damage " +
                    "scaling with Elemental Damage and apply Burned."
        )

    val hurry =
        CharacterTraitDefinition(
            id = CharacterTraitId.HURRY,
            displayName = "Hurry",
            description =
                "Every 5 Movement Speed grants 1 Attack Speed and raises " +
                    "the Attack Speed maximum by the same amount."
        )

    val bloodweight =
        CharacterTraitDefinition(
            id = CharacterTraitId.BLOODWEIGHT,
            displayName = "Bloodweight",
            description =
                "Every attack gains bonus damage equal to 5% of current HP."
        )

    val theToadCycle =
        CharacterTraitDefinition(
            id = CharacterTraitId.THE_TOAD_CYCLE,
            displayName = "The Toad Cycle",
            description =
                "Summons a Toad every 15 seconds. Poisoned enemy deaths " +
                    "reduce the timer by 2 seconds. Up to 4 Toad Cycle " +
                    "Toads can be alive at once."
        )
}
