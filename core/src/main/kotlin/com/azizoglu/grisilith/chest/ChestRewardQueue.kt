package com.azizoglu.grisilith.chest

/*
 * Stage sırasında toplanmış sandıkların
 * üçlü ödül seçeneklerini saklar.
 */
class ChestRewardQueue {

    private val pendingChoiceSets =
        mutableListOf<ChestRewardChoiceSet>()

    /*
     * Bekleyen sandık sayısıdır.
     * Toplam ödül seçeneği sayısı değildir.
     */
    val rewardCount: Int
        get() =
            pendingChoiceSets.size

    val isEmpty: Boolean
        get() =
            pendingChoiceSets.isEmpty()

    val isNotEmpty: Boolean
        get() =
            pendingChoiceSets.isNotEmpty()

    fun addChoiceSet(
        choiceSet:
        ChestRewardChoiceSet
    ) {
        pendingChoiceSets.add(
            choiceSet
        )
    }

    fun peekFirst():
        ChestRewardChoiceSet? {

        return pendingChoiceSets
            .firstOrNull()
    }

    fun removeFirst():
        ChestRewardChoiceSet? {

        if (
            pendingChoiceSets.isEmpty()
        ) {
            return null
        }

        return pendingChoiceSets
            .removeAt(
                0
            )
    }

    fun allChoiceSets():
        List<ChestRewardChoiceSet> {

        return pendingChoiceSets
            .toList()
    }

    fun clear() {
        pendingChoiceSets.clear()
    }
}
