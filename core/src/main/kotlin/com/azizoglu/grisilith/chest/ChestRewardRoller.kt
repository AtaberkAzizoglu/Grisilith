package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import kotlin.random.Random

object ChestRewardRoller {

    private const val MAXIMUM_LUCK =
        50

    /*
     * Luck, daha nadir Item ağırlıklarını çok
     * küçük miktarda artırır.
     *
     * Her Luck seviyesi rarity tier başına
     * yaklaşık %0,1 göreceli ağırlık verir.
     *
     * Örnek, 50 Luck:
     * Uncommon → yaklaşık +%5 ağırlık
     * Rare     → yaklaşık +%10
     * Legendary→ yaklaşık +%15
     * Artifact → yaklaşık +%20
     *
     * Artifact temel ağırlığı çok düşük olduğu
     * için yine de son derece nadir kalır.
     */
    private const val RARITY_WEIGHT_BONUS_PER_LUCK =
        0.001f

    fun rollReward(
        luckScore: Int,

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

        /*
         * İstenen türün havuzu boşsa diğer tür
         * kullanılır. Böylece geçersiz ödül
         * üretilmez.
         */
        return when (requestedRewardType) {
            ChestRewardType.STAT -> {
                when {
                    availableStatTypes
                        .isNotEmpty() -> {
                        rollStatReward(
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

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    else -> {
                        /*
                         * Bütün Statlar maksimum ve bütün
                         * Item limitleri doluysa güvenli
                         * fallback.
                         *
                         * Normal oyunda bu noktaya gelmesi
                         * çok düşük ihtimaldir.
                         */
                        ChestRewardRoll(
                            rewardType =
                                ChestRewardType.STAT,

                            statType =
                                StatType.MAX_HP,

                            statAmount =
                                1
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

                            availableItemDefinitions =
                                availableItemDefinitions,

                            random =
                                random
                        )
                    }

                    availableStatTypes
                        .isNotEmpty() -> {
                        rollStatReward(
                            availableStatTypes =
                                availableStatTypes,

                            random =
                                random
                        )
                    }

                    else -> {
                        ChestRewardRoll(
                            rewardType =
                                ChestRewardType.STAT,

                            statType =
                                StatType.MAX_HP,

                            statAmount =
                                1
                        )
                    }
                }
            }
        }
    }

    fun rollChoiceSet(
        luckScore: Int,

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

                    availableStatTypes =
                        availableStatTypes,

                    availableItemDefinitions =
                        availableItemDefinitions,

                    random =
                        random
                )

            val duplicate =
                choices.any { existingChoice ->
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
            ChestRewardType.STAT -> {
                "STAT:" +
                    reward.statType
                        ?.name
            }

            ChestRewardType.ITEM -> {
                "ITEM:" +
                    reward.itemDefinition
                        ?.id
                        ?.value
            }
        }
    }

    private fun rollStatReward(
        availableStatTypes:
        List<StatType>,

        random:
        Random
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
                1
        )
    }

    private fun rollItemReward(
        luckScore: Int,

        availableItemDefinitions:
        List<ItemDefinition>,

        random:
        Random
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

        itemDefinitions:
        List<ItemDefinition>,

        random:
        Random
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
            itemDefinitions.map { definition ->
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

            if (roll <= 0f) {
                return weightedItem.first
            }
        }

        return weightedItems
            .last()
            .first
    }

    /*
     * Başlangıç rarity ağırlıkları.
     *
     * Şimdilik Common Item sayısı daha fazla
     * olduğu için ayrıca her Item kendi rarity
     * ağırlığını paylaşır.
     */
    private fun baseRarityWeight(
        rarityName:
        String
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
        rarityName:
        String
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
