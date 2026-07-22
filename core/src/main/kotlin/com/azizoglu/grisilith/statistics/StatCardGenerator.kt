package com.azizoglu.grisilith.statistics

object StatCardGenerator {

    fun generateCards(): List<StatCard> {
        val cardPool =
            listOf(
                createCard(
                    StatUpgradeType.MAX_HEALTH,
                    "Max Health"
                ),

                createCard(
                    StatUpgradeType.ARMOR_CLASS,
                    "Armor Class"
                ),

                createCard(
                    StatUpgradeType.STRENGTH,
                    "Strength"
                ),

                createCard(
                    StatUpgradeType.DEXTERITY,
                    "Dexterity"
                ),

                createCard(
                    StatUpgradeType.WISDOM,
                    "Wisdom"
                ),

                createCard(
                    StatUpgradeType.HASTE,
                    "Haste"
                ),

                createCard(
                    StatUpgradeType.ATTACK_SPEED,
                    "Attack Speed"
                ),

                createCard(
                    StatUpgradeType.CRIT_RATE,
                    "Crit Rate"
                ),

                createCard(
                    StatUpgradeType.CRIT_DAMAGE,
                    "Crit Damage"
                ),

                createCard(
                    StatUpgradeType.MOVEMENT_SPEED,
                    "Movement Speed"
                ),

                createCard(
                    StatUpgradeType.LUCK,
                    "Luck"
                ),

                createCard(
                    StatUpgradeType.HARVEST,
                    "Harvest"
                ),

                createCard(
                    StatUpgradeType.PIERCING,
                    "Piercing"
                ),

                createCard(
                    StatUpgradeType.ENEMY_COUNT,
                    "Enemy Count"
                ),

                createCard(
                    StatUpgradeType.HP_REGENERATION,
                    "HP Regeneration"
                ),

                createCard(
                    StatUpgradeType.LIFE_STEAL,
                    "Life Steal"
                )
            )

        return cardPool
            .shuffled()
            .take(3)
    }

    private fun createCard(
        type: StatUpgradeType,
        title: String
    ): StatCard {
        return StatCard(
            type = type,
            title = title,
            description = "+1 $title score"
        )
    }
}
