package com.azizoglu.grisilith.character

import com.azizoglu.grisilith.statistics.StatType

/** Short selection copy is deliberately separate from the archive backstory. */
object CharacterPresentation {
    fun summary(character: CharacterDefinition): String = when(character.id.value) {
        "CHR_KOBA" -> "A Strength-based barbarian with high health and damage that scales with his current HP."
        "CHR_LAVENA_VOL" -> "A powerful spellcaster and necromancer. A Wisdom-based caster."
        "CHR_LINLOS_DUSTMAN" -> "A summoner and spellcaster. A Wisdom-based caster."
        "CHR_SIR_IMBERT" -> "A heavily armored, high-health knight. A Strength-based fighter."
        "CHR_YWALETH_YOSTER" -> "An agile, fast ranged fighter. A Dexterity-based archer."
        else -> character.description
    }

    // Linked set order is the existing character-specific market/stat priority.
    fun mainStats(character: CharacterDefinition): List<StatType> = character.preferredMarketStats.toList()
    fun mainStatsText(character: CharacterDefinition): String = mainStats(character)
        .mapIndexed { index, stat -> "${index + 1}. ${stat.displayName}" }.joinToString("   >   ")
}
