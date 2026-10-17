package com.azizoglu.grisilith.character.trait

object DefaultCharacterTraits {

    val necromancy =
        CharacterTraitDefinition(
            id = CharacterTraitId.NECROMANCY,
            displayName = "Necromancy",
            description =
                "Thanks to forbidden knowledge, Lavena Vol can trigger Life Steal " +
                    "by dealing Necrotic damage."
        )

    val theHypocrisy =
        CharacterTraitDefinition(
            id = CharacterTraitId.THE_HYPOCRISY,
            displayName = "The Hypocrisy",
            description =
                "Although Sir Imbert fears magic, he enchants his blades in combat. " +
                    "Melee Weapon hits deal additional Fire damage and apply Burned. " +
                    "The additional Fire damage increases with Elemental Damage."
        )

    val hurry =
        CharacterTraitDefinition(
            id = CharacterTraitId.HURRY,
            displayName = "Hurry",
            description =
                "Every 5 Movement Speed grants 1 Attack Speed and raises the Attack " +
                    "Speed maximum by the same amount."
        )

    val bloodweight =
        CharacterTraitDefinition(
            id = CharacterTraitId.BLOODWEIGHT,
            displayName = "Bloodweight",
            description =
                "Whenever Koba attacks with a melee Weapon, he deals additional " +
                    "damage equal to 1% of his current HP."
        )

    val theToadCycle =
        CharacterTraitDefinition(
            id = CharacterTraitId.THE_TOAD_CYCLE,
            displayName = "The Toad Cycle",
            description =
                "Summons a poisonous Toad every 15 seconds. Killing an enemy with " +
                    "Poison reduces the next summon timer by 2 seconds. Up to four " +
                    "Toads can fight alongside Linlos at once."
        )
}
