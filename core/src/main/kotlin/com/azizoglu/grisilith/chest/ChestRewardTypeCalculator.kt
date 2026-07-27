package com.azizoglu.grisilith.chest

import kotlin.random.Random

object ChestRewardTypeCalculator {

    private const val BASE_ITEM_CHANCE =
        0.20f

    private const val ITEM_CHANCE_PER_LUCK =
        0.003f

    private const val MAXIMUM_LUCK =
        50

    fun calculateItemChance(
        luckScore:
        Int
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
                0.35f
            )
    }

    fun calculateStatChance(
        luckScore:
        Int
    ): Float {
        return 1f -
            calculateItemChance(
                luckScore
            )
    }

    fun rollRewardType(
        luckScore:
        Int,

        random:
        Random = Random.Default
    ): ChestRewardType {
        val itemChance =
            calculateItemChance(
                luckScore
            )

        return if (
            random.nextFloat() <
            itemChance
        ) {
            ChestRewardType.ITEM
        } else {
            ChestRewardType.STAT
        }
    }
}
