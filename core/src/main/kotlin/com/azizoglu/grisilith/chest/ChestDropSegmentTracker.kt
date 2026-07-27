package com.azizoglu.grisilith.chest

import kotlin.math.pow

/*
 * Bir Mini Boss ile sonraki Mini Boss veya
 * Final Boss arasındaki düşen sandık sayısını
 * takip eder.
 */
class ChestDropSegmentTracker {

    companion object {

        /*
         * İlk 10 düşen sandık herhangi bir
         * azaltma cezası almaz.
         */
        private const val FULL_CHANCE_CHEST_LIMIT =
            10

        /*
         * İlk 10 sandıktan sonra her 5 sandıkta
         * ihtimal bir kez daha yarıya düşer.
         */
        private const val CHESTS_PER_REDUCTION_STEP =
            5
    }

    var droppedChestCount:
        Int = 0
        private set

    /*
     * Örnekler:
     *
     * 0–9 sandık düşmüşse  → 1.0
     * 10–14 düşmüşse       → 0.5
     * 15–19 düşmüşse       → 0.25
     * 20–24 düşmüşse       → 0.125
     *
     * Değer, bir sonraki sandığın düşme
     * kontrolünde kullanılacaktır.
     */
    fun currentDropChanceMultiplier():
        Float {

        if (
            droppedChestCount <
            FULL_CHANCE_CHEST_LIMIT
        ) {
            return 1f
        }

        val reductionStep =
            1 +
                (
                    droppedChestCount -
                        FULL_CHANCE_CHEST_LIMIT
                    ) /
                CHESTS_PER_REDUCTION_STEP

        return 0.5
            .pow(
                reductionStep.toDouble()
            )
            .toFloat()
    }

    /*
     * Yalnızca sandık gerçekten dünyaya
     * düştüğünde çağrılmalıdır.
     */
    fun recordDroppedChest() {
        droppedChestCount++
    }

    /*
     * Yeni Mini Boss → Mini Boss/Final Boss
     * aralığı başladığında çağrılır.
     */
    fun resetForNewBossInterval() {
        droppedChestCount = 0
    }
}
