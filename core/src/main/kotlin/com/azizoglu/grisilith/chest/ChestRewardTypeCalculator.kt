package com.azizoglu.grisilith.chest

import kotlin.random.Random

object ChestRewardTypeCalculator {

    private const val BASE_ITEM_CHANCE = 0.50f
    private const val MAXIMUM_ITEM_CHANCE = 0.80f
    private const val MAXIMUM_LUCK = 50

    private const val ITEM_CHANCE_PER_LUCK =
        (MAXIMUM_ITEM_CHANCE - BASE_ITEM_CHANCE) /
            MAXIMUM_LUCK

    fun calculateItemChance(
        luckScore: Int
    ): Float {
        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        return (
            BASE_ITEM_CHANCE +
                safeLuck *
                ITEM_CHANCE_PER_LUCK
            )
            .coerceIn(
                BASE_ITEM_CHANCE,
                MAXIMUM_ITEM_CHANCE
            )
    }

    fun calculateStatChance(
        luckScore: Int
    ): Float {
        return 1f -
            calculateItemChance(
                luckScore
            )
    }

    fun rollRewardType(
        luckScore: Int,
        random: Random = Random.Default
    ): ChestRewardType {
        return if (
            random.nextFloat() <
            calculateItemChance(
                luckScore
            )
        ) {
            ChestRewardType.ITEM
        } else {
            ChestRewardType.STAT
        }
    }
}
