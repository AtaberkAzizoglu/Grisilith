package com.azizoglu.grisilith.chest

import kotlin.random.Random

object ChestDropCalculator {

    private const val DROP_CHANCE_PER_LUCK =
        0.0005f

    private const val MAXIMUM_LUCK =
        50

    fun baseDropChance(
        source:
        ChestDropSource
    ): Float {
        return when (source) {
            ChestDropSource
                .NORMAL_ENEMY -> {
                0.025f
            }

            ChestDropSource
                .UNIQUE_ENEMY -> {
                0.06f
            }

            ChestDropSource
                .MINI_BOSS -> {
                0.13f
            }

            ChestDropSource
                .FINAL_BOSS -> {
                0f
            }
        }
    }

    fun calculateDropChance(
        source:
        ChestDropSource,

        luckScore:
        Int,

        segmentChanceMultiplier:
        Float
    ): Float {
        val baseChance =
            baseDropChance(
                source
            )

        if (baseChance <= 0f) {
            return 0f
        }

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val luckBonus =
            safeLuck *
                DROP_CHANCE_PER_LUCK

        val chanceBeforeSegmentPenalty =
            baseChance +
                luckBonus

        val safeSegmentMultiplier =
            segmentChanceMultiplier
                .coerceIn(
                    0f,
                    1f
                )

        return (
            chanceBeforeSegmentPenalty *
                safeSegmentMultiplier
            )
            .coerceIn(
                0f,
                1f
            )
    }

    fun shouldDropChest(
        source:
        ChestDropSource,

        luckScore:
        Int,

        segmentChanceMultiplier:
        Float,

        random:
        Random = Random.Default
    ): Boolean {
        val dropChance =
            calculateDropChance(
                source =
                    source,

                luckScore =
                    luckScore,

                segmentChanceMultiplier =
                    segmentChanceMultiplier
            )

        if (dropChance <= 0f) {
            return false
        }

        return random.nextFloat() <
            dropChance
    }
}
