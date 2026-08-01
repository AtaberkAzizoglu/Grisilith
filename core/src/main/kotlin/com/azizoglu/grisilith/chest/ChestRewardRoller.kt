package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatRewardAmountCalculator
import com.azizoglu.grisilith.statistics.StatType
import kotlin.random.Random

object ChestRewardRoller {

    private const val MAXIMUM_LUCK =
        50

    /*
     * Mevcut rarity + Luck sistemini koruyoruz.
     *
     * Luck arttıkça daha yüksek rarity tier'larının
     * göreceli ağırlığı yükselir.
     */
    private const val RARITY_WEIGHT_BONUS_PER_LUCK =
        0.001f

    fun rollReward(
        currentStage: Int,
        luckScore: Int,
        availableStatTypes: List<StatType>,
        availableItemDefinitions: List<ItemDefinition>,
        random: Random = Random.Default
    ): ChestRewardRoll {

        val requestedRewardType =
            ChestRewardTypeCalculator
                .rollRewardType(
                    luckScore =
                        luckScore,

                    random =
                        random
                )

        return when (requestedRewardType) {

            ChestRewardType.STAT -> {
                when {
                    availableStatTypes.isNotEmpty() -> {
                        rollStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            availableStatTypes =
                                availableStatTypes,

                            random =
                                random
                        )
                    }

                    availableItemDefinitions.isNotEmpty() -> {
                        rollItemReward(
                            luckScore =
                                luckScore,

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    else -> {
                        fallbackStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            random =
                                random
                        )
                    }
                }
            }

            ChestRewardType.ITEM -> {
                when {
                    availableItemDefinitions.isNotEmpty() -> {
                        rollItemReward(
                            luckScore =
                                luckScore,

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    availableStatTypes.isNotEmpty() -> {
                        rollStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            availableStatTypes =
                                availableStatTypes,

                            random =
                                random
                        )
                    }

                    else -> {
                        fallbackStatReward(
                            currentStage =
                                currentStage,

                            luckScore =
                                luckScore,

                            random =
                                random
                        )
                    }
                }
            }
        }
    }

    private fun fallbackStatReward(
        currentStage: Int,
        luckScore: Int,
        random: Random
    ): ChestRewardRoll {

        return ChestRewardRoll(
            rewardType =
                ChestRewardType.STAT,

            statType =
                StatType.MAX_HP,

            statAmount =
                StatRewardAmountCalculator
                    .rollAmount(
                        currentStage =
                            currentStage,

                        luckScore =
                            luckScore,

                        random =
                            random
                    )
        )
    }

    private fun rollStatReward(
        currentStage: Int,
        luckScore: Int,
        availableStatTypes: List<StatType>,
        random: Random
    ): ChestRewardRoll {

        val selectedStat =
            availableStatTypes.random(
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
                        currentStage =
                            currentStage,

                        luckScore =
                            luckScore,

                        random =
                            random
                    )
        )
    }

    private fun rollItemReward(
        luckScore: Int,
        availableItemDefinitions: List<ItemDefinition>,
        random: Random
    ): ChestRewardRoll {

        val selectedItem =
            weightedItemSelection(
                luckScore =
                    luckScore,

                itemDefinitions =
                    availableItemDefinitions,

                random =
                    random
            )

        return ChestRewardRoll(
            rewardType =
                ChestRewardType.ITEM,

            itemDefinition =
                selectedItem
        )
    }

    private fun weightedItemSelection(
        luckScore: Int,
        itemDefinitions: List<ItemDefinition>,
        random: Random
    ): ItemDefinition {

        require(
            itemDefinitions.isNotEmpty()
        )

        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val weightedItems =
            itemDefinitions.map {
                    definition ->

                val rarityTier =
                    rarityTier(
                        definition
                            .rarity
                            .name
                    )

                val baseWeight =
                    baseRarityWeight(
                        definition
                            .rarity
                            .name
                    )

                val luckMultiplier =
                    1f +
                        safeLuck *
                        RARITY_WEIGHT_BONUS_PER_LUCK *
                        rarityTier

                definition to
                    (
                        baseWeight *
                            luckMultiplier
                        )
                        .coerceAtLeast(
                            0.001f
                        )
            }

        val totalWeight =
            weightedItems.sumOf {
                    weightedItem ->

                weightedItem
                    .second
                    .toDouble()
            }
                .toFloat()

        var roll =
            random.nextFloat() *
                totalWeight

        weightedItems.forEach {
                weightedItem ->

            roll -=
                weightedItem.second

            if (
                roll <= 0f
            ) {
                return weightedItem.first
            }
        }

        return weightedItems
            .last()
            .first
    }

    private fun baseRarityWeight(
        rarityName: String
    ): Float {

        return when (
            rarityName.uppercase()
        ) {
            "COMMON" ->
                100f

            "UNCOMMON" ->
                45f

            "RARE" ->
                18f

            "LEGENDARY" ->
                5f

            "ARTIFACT" ->
                1f

            else ->
                100f
        }
    }

    private fun rarityTier(
        rarityName: String
    ): Int {

        return when (
            rarityName.uppercase()
        ) {
            "COMMON" ->
                0

            "UNCOMMON" ->
                1

            "RARE" ->
                2

            "LEGENDARY" ->
                3

            "ARTIFACT" ->
                4

            else ->
                0
        }
    }
}
