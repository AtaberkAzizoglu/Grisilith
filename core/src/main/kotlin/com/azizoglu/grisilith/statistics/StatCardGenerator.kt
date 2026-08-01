package com.azizoglu.grisilith.statistics

import kotlin.random.Random

object StatCardGenerator {

    const val STAGE_REWARD_CARD_COUNT = 4

    fun generateCards(
        currentStage: Int,
        luckScore: Int,
        availableUpgradeTypes:
        List<StatUpgradeType> =
            StatUpgradeType.entries.toList(),
        random: Random = Random.Default
    ): List<StatCard> {
        if (availableUpgradeTypes.isEmpty()) {
            return emptyList()
        }

        val distinctPool =
            availableUpgradeTypes.distinct()

        val selectedTypes =
            distinctPool
                .shuffled(random)
                .take(STAGE_REWARD_CARD_COUNT)
                .toMutableList()

        while (
            selectedTypes.size <
            STAGE_REWARD_CARD_COUNT
        ) {
            selectedTypes.add(
                distinctPool.random(random)
            )
        }

        return selectedTypes.map { upgradeType ->
            val amount =
                StatRewardAmountCalculator
                    .rollAmount(
                        currentStage = currentStage,
                        luckScore = luckScore,
                        random = random
                    )

            createCard(
                type = upgradeType,
                amount = amount
            )
        }
    }

    /*
     * Eski Controller henüz değiştirilmeden proje
     * derlenmeye devam etsin diye geçici uyumluluk.
     */
    fun generateCards(): List<StatCard> {
        return generateCards(
            currentStage = 1,
            luckScore = 0
        ).take(3)
    }

    private fun createCard(
        type: StatUpgradeType,
        amount: Int
    ): StatCard {
        val title =
            type.statType.displayName

        return StatCard(
            type = type,
            title = title,
            amount = amount,
            description =
                "+$amount $title score"
        )
    }
}
