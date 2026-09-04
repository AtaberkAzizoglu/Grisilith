package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.item.definition.ItemRarity
import com.azizoglu.grisilith.statistics.StatRewardAmountCalculator
import com.azizoglu.grisilith.statistics.StatType
import kotlin.random.Random

object ChestRewardRoller {

    private const val MAXIMUM_LUCK =
        50

    /*
     * Preferred Stat sistemi:
     *
     * 0 Luck  -> preferred ve normal Stat eşit weight
     * 25 Luck -> preferred Stat 1.5x weight
     * 50 Luck -> preferred Stat 2.0x weight
     *
     * Bu yalnız Stat reward seçildikten sonra hangi Stat'ın
     * geleceğini etkiler; Item/Stat oranını değiştirmez.
     */
    private const val PREFERRED_STAT_WEIGHT_PER_LUCK =
        0.02f

    fun rollReward(
        luckScore: Int,

        currentStage:
        Int = 1,

        preferredStatTypes:
        Set<StatType> =
            emptySet(),

        availableStatTypes:
        List<StatType>,

        availableItemDefinitions:
        List<ItemDefinition>,

        random:
        Random = Random.Default
    ): ChestRewardRoll {
        val requestedRewardType =
            ChestRewardTypeCalculator
                .rollRewardType(
                    luckScore =
                        luckScore,

                    random =
                        random
                )

        return when (
            requestedRewardType
        ) {
            ChestRewardType.STAT -> {
                when {
                    availableStatTypes
                        .isNotEmpty() -> {
                        rollStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            preferredStatTypes =
                                preferredStatTypes,

                            availableStatTypes =
                                availableStatTypes,

                            random =
                                random
                        )
                    }

                    availableItemDefinitions
                        .isNotEmpty() -> {
                        rollItemReward(
                            luckScore =
                                luckScore,

                            currentStage =
                                currentStage,

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    else -> {
                        fallbackStatReward(
                            currentStage = currentStage,
                            luckScore = luckScore,
                            random = random
                        )
                    }
                }
            }

            ChestRewardType.ITEM -> {
                when {
                    availableItemDefinitions
                        .isNotEmpty() -> {
                        rollItemReward(
                            luckScore =
                                luckScore,

                            currentStage =
                                currentStage,

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    availableStatTypes
                        .isNotEmpty() -> {
                        rollStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            preferredStatTypes =
                                preferredStatTypes,

                            availableStatTypes =
                                availableStatTypes,

                            random =
                                random
                        )
                    }

                    else -> {
                        fallbackStatReward(
                            currentStage = currentStage,
                            luckScore = luckScore,
                            random = random
                        )
                    }
                }
            }
        }
    }

    fun rollChoiceSet(
        luckScore: Int,

        currentStage:
        Int = 1,

        preferredStatTypes:
        Set<StatType> =
            emptySet(),

        availableStatTypes:
        List<StatType>,

        availableItemDefinitions:
        List<ItemDefinition>,

        random:
        Random = Random.Default
    ): ChestRewardChoiceSet {

        val choices =
            mutableListOf<ChestRewardRoll>()

        var attempts =
            0

        val maximumAttempts =
            30

        while (
            choices.size <
            ChestRewardChoiceSet.CHOICE_COUNT &&
            attempts <
            maximumAttempts
        ) {
            attempts++

            val candidate =
                rollReward(
                    luckScore =
                        luckScore,

                    currentStage =
                        currentStage,

                    preferredStatTypes =
                        preferredStatTypes,

                    availableStatTypes =
                        availableStatTypes,

                    availableItemDefinitions =
                        availableItemDefinitions,

                    random =
                        random
                )

            val duplicate =
                choices.any {
                        existingChoice ->

                    rewardIdentity(
                        existingChoice
                    ) ==
                        rewardIdentity(
                            candidate
                        )
                }

            if (!duplicate) {
                choices.add(
                    candidate
                )
            }
        }

        while (
            choices.size <
            ChestRewardChoiceSet.CHOICE_COUNT
        ) {
            choices.add(
                rollReward(
                    luckScore =
                        luckScore,

                    currentStage =
                        currentStage,

                    preferredStatTypes =
                        preferredStatTypes,

                    availableStatTypes =
                        availableStatTypes,

                    availableItemDefinitions =
                        availableItemDefinitions,

                    random =
                        random
                )
            )
        }

        return ChestRewardChoiceSet(
            choices =
                choices
        )
    }

    private fun rewardIdentity(
        reward:
        ChestRewardRoll
    ): String {
        return when (
            reward.rewardType
        ) {
            ChestRewardType.STAT ->
                "STAT:" +
                    reward.statType
                        ?.name

            ChestRewardType.ITEM ->
                "ITEM:" +
                    reward.itemDefinition
                        ?.id
                        ?.value
        }
    }

    private fun rollStatReward(
        currentStage:
        Int,

        luckScore:
        Int,

        preferredStatTypes:
        Set<StatType>,

        availableStatTypes:
        List<StatType>,

        random:
        Random
    ): ChestRewardRoll {
        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val preferredWeight =
            1f +
                safeLuck *
                PREFERRED_STAT_WEIGHT_PER_LUCK

        val weightedStats =
            availableStatTypes.map {
                    statType ->

                statType to
                    if (
                        statType in
                        preferredStatTypes
                    ) {
                        preferredWeight
                    } else {
                        1f
                    }
            }

        val selectedStat =
            selectWeighted(
                weightedValues =
                    weightedStats,

                random =
                    random
            )

        return ChestRewardRoll(
            rewardType =
                ChestRewardType.STAT,

            statType =
                selectedStat,

            statAmount =
                StatRewardAmountCalculator
                    .rollAmount(
                        currentStage = currentStage,
                        luckScore = luckScore,
                        random = random
                    )
        )
    }

    private fun rollItemReward(
        luckScore:
        Int,

        currentStage:
        Int,

        availableItemDefinitions:
        List<ItemDefinition>,

        random:
        Random
    ): ChestRewardRoll {
        require(
            availableItemDefinitions
                .isNotEmpty()
        )

        /*
         * 1) Önce yalnız mevcut rarity'ler arasında rarity roll yapılır.
         * 2) Sonra o rarity'deki itemler arasında eşit seçim yapılır.
         *
         * Böylece aynı rarity'ye 5 veya 50 item eklemek rarity chance'i
         * değiştirmez.
         */
        val itemsByRarity =
            availableItemDefinitions
                .groupBy {
                    it.rarity
                }

        val distribution =
            ChestItemRarityCalculator
                .distribution(
                    luckScore =
                        luckScore,

                    currentStage =
                        currentStage
                )

        val rarityWeights =
            ItemRarity.entries
                .mapNotNull {
                        rarity ->

                    val items =
                        itemsByRarity[
                            rarity
                        ]

                    if (
                        items.isNullOrEmpty()
                    ) {
                        null
                    } else {
                        rarity to
                            distribution
                                .weightFor(
                                    rarity
                                )
                    }
                }

        val selectedRarity =
            selectWeighted(
                weightedValues =
                    rarityWeights,

                random =
                    random
            )

        val selectedItem =
            itemsByRarity
                .getValue(
                    selectedRarity
                )
                .random(
                    random
                )

        return ChestRewardRoll(
            rewardType =
                ChestRewardType.ITEM,

            itemDefinition =
                selectedItem
        )
    }

    private fun <T> selectWeighted(
        weightedValues:
        List<Pair<T, Float>>,

        random:
        Random
    ): T {
        require(
            weightedValues.isNotEmpty()
        )

        val safeValues =
            weightedValues.map {
                    pair ->

                pair.first to
                    pair.second
                        .coerceAtLeast(
                            0f
                        )
            }

        val totalWeight =
            safeValues.sumOf {
                    pair ->

                pair.second
                    .toDouble()
            }
                .toFloat()

        if (
            totalWeight <=
            0f
        ) {
            return safeValues
                .random(
                    random
                )
                .first
        }

        var roll =
            random.nextFloat() *
                totalWeight

        safeValues.forEach {
                pair ->

            roll -=
                pair.second

            if (
                roll <=
                0f
            ) {
                return pair.first
            }
        }

        return safeValues
            .last()
            .first
    }

    private fun fallbackStatReward(
        currentStage: Int,
        luckScore: Int,
        random: Random
    ):
        ChestRewardRoll {
        return ChestRewardRoll(
            rewardType =
                ChestRewardType.STAT,

            statType =
                StatType.MAX_HP,

            statAmount =
                StatRewardAmountCalculator
                    .rollAmount(
                        currentStage = currentStage,
                        luckScore = luckScore,
                        random = random
                    )
        )
    }
}
