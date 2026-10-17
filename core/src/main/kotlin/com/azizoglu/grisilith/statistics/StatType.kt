package com.azizoglu.grisilith.statistics

enum class StatType(
    val displayName: String
) {
    MAX_HP("Max HP"),
    ARMOR_CLASS("Armor Class"),
    STRENGTH("Strength"),
    DEXTERITY("Dexterity"),
    WISDOM("Wisdom"),
    HASTE("Haste"),
    ATTACK_SPEED("Attack Speed"),
    CRIT_RATE("Crit Rate"),
    CRIT_DAMAGE("Crit Damage"),
    MOVEMENT_SPEED("Movement Speed"),
    LUCK("Luck"),
    HARVEST("Harvest"),
    ELEMENTAL_DAMAGE("Elemental Damage"),
    MIGHT_OF_NATURE("Might of Nature"),
    SHEPHERD("Shepherd"),
    PIERCING("Piercing"),
    ENEMY_COUNT("Enemy Count"),
    HP_REGENERATION("HP Regeneration"),
    LIFE_STEAL("Life Steal"),
    MAGIC_RESISTANCE("Magic Resistance");

    companion object {
        /** Canonical presentation order for every player-facing stat list. */
        val alphabetical: List<StatType> =
            entries.sortedBy { statType ->
                statType.displayName.lowercase()
            }

        /** Canonical grouped presentation order for full Stat panels. */
        val grouped: List<StatType> =
            StatPresentationGroup.ordered.flatMap { group -> group.stats }
    }
}

data class StatPresentationGroup(
    val title: String,
    val stats: List<StatType>
) {
    companion object {
        val magicalDamage = StatPresentationGroup(
            title = "MAGICAL DAMAGE",
            stats = listOf(
                StatType.WISDOM,
                StatType.HASTE,
                StatType.ELEMENTAL_DAMAGE,
                StatType.MIGHT_OF_NATURE,
                StatType.SHEPHERD
            )
        )

        val physicalDamage = StatPresentationGroup(
            title = "PHYSICAL DAMAGE",
            stats = listOf(
                StatType.DEXTERITY,
                StatType.STRENGTH,
                StatType.ATTACK_SPEED,
                StatType.CRIT_RATE,
                StatType.CRIT_DAMAGE,
                StatType.PIERCING
            )
        )

        val defensive = StatPresentationGroup(
            title = "DEFENSIVE",
            stats = listOf(
                StatType.MAX_HP,
                StatType.ARMOR_CLASS,
                StatType.MAGIC_RESISTANCE,
                StatType.HP_REGENERATION,
                StatType.LIFE_STEAL
            )
        )

        val utility = StatPresentationGroup(
            title = "UTILITY",
            stats = listOf(
                StatType.ENEMY_COUNT,
                StatType.HARVEST,
                StatType.LUCK,
                StatType.MOVEMENT_SPEED
            )
        )

        /** Layout: Magical | Physical, then Defensive | Utility. */
        val ordered = listOf(
            magicalDamage,
            physicalDamage,
            defensive,
            utility
        )
    }
}
