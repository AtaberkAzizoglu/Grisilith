package com.azizoglu.grisilith.statistics

/** Shared tooltip text and source breakdown for every full stat view. */
object StatDetailPresentation {
    private val descriptions = mapOf(
        StatType.MAX_HP to "Increases the maximum amount of hit points you may have.",
        StatType.ARMOR_CLASS to "Physical Defense = Base Armor + Armor Class. Damage taken = Incoming damage × 100 / (100 + Defense). Incoming Piercing reduces Defense.",
        StatType.STRENGTH to "Improves damage from melee weapons.",
        StatType.DEXTERITY to "Improves damage from ranged weapons.",
        StatType.WISDOM to "Improves damage from spell-focus weapons.",
        StatType.HASTE to "Increases the attack rate of spell-focus weapons.",
        StatType.ATTACK_SPEED to "Increases the attack rate of physical weapons.",
        StatType.CRIT_RATE to "Raises the chance for attacks to deal critical damage.",
        StatType.CRIT_DAMAGE to "Raises the damage multiplier of critical hits.",
        StatType.MOVEMENT_SPEED to "Increases the player's movement speed.",
        StatType.LUCK to "Improves favorable random outcomes and reward rolls.",
        StatType.HARVEST to "Improves the amount of resources gained during a run.",
        StatType.ELEMENTAL_DAMAGE to "Increases any Fire, Air and Water damages.",
        StatType.MIGHT_OF_NATURE to "Increases any Lightning, Acid and Necrotic damages.",
        StatType.SHEPHERD to "Increases any poison damage and increases the stats of the summons you call.",
        StatType.PIERCING to "Ignores 1.5% of physical Defense per point, up to 75%. Does not bypass magical Defense.",
        StatType.ENEMY_COUNT to "Changes how many enemies can appear during a stage.",
        StatType.HP_REGENERATION to "Restores a portion of maximum HP over time.",
        StatType.LIFE_STEAL to "Grants a chance to recover HP when dealing physical damage.",
        StatType.MAGIC_RESISTANCE to "Magical Defense = Base Spell Protection + Magic Resistance. Damage taken = Incoming damage × 100 / (100 + Defense)."
    )

    fun description(statType: StatType): String =
        descriptions[statType] ?: "Modifies ${statType.displayName}."

    fun tooltipText(playerStats: PlayerStats, statType: StatType): String {
        val sources = mutableListOf<Pair<String, Int>>()
        fun add(name: String, value: Int) {
            if (value != 0) sources += name to value
        }
        add("Base", playerStats.baseScore(statType))
        add("Armor / Gear", playerStats.gearBonus(statType))
        add("Weapons", playerStats.weaponBonus(statType))
        add("Items", playerStats.itemBonus(statType))
        add("Stage rewards", playerStats.runBonus(statType))
        add("Tamgas", playerStats.tamgaBonus(statType))
        add("Active Tamga", playerStats.activeTamgaBonus(statType))
        add("Weapon skills", playerStats.weaponSkillBonus(statType))
        add("Character trait", playerStats.characterTraitBonus(statType))

        val total = playerStats.totalScore(statType)
        val knownTotal = sources.sumOf { it.second }
        add("Conditional effects", total - knownTotal)

        val sourceText = sources.joinToString("\n") { (name, value) ->
            "$name: ${if (value > 0) "+" else ""}$value"
        }
        return buildString {
            appendLine(description(statType))
            appendLine()
            appendLine("Total: $total")
            append(sourceText.ifBlank { "No active sources." })
        }
    }
}
