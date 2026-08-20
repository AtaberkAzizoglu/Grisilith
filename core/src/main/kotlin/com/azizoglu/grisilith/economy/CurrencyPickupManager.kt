package com.azizoglu.grisilith.economy

import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.statistics.RunStatistics
import com.badlogic.gdx.math.MathUtils
import kotlin.math.floor
import kotlin.math.sqrt

data class CurrencyStageLoss(
    val goldLost: Int,
    val grisilithLost: Int
)

class CurrencyPickupManager {

    private val pickups =
        mutableListOf<CurrencyPickup>()

    private var fractionalGoldCarry =
        0f

    private var deferredGold =
        0

    private var deferredGrisilith =
        0

    private var recentlyTransferredGold =
        0

    private var recentlyTransferredGrisilith =
        0

    private var transferFeedbackSeconds =
        0f

    val allPickups:
        List<CurrencyPickup>
        get() =
            pickups

    val pendingGold: Int
        get() =
            deferredGold

    val pendingGrisilith: Int
        get() =
            deferredGrisilith

    val displayedAutoGold: Int
        get() =
            if (
                transferFeedbackSeconds >
                0f
            ) {
                recentlyTransferredGold
            } else {
                0
            }

    val displayedAutoGrisilith: Int
        get() =
            if (
                transferFeedbackSeconds >
                0f
            ) {
                recentlyTransferredGrisilith
            } else {
                0
            }

    fun displayedGrisilithBalance(
        totalAwardedByTracker: Int
    ): Int {
        val unbanked =
            pickups.count {
                it.type ==
                    CurrencyPickupType.GRISILITH
            } +
                deferredGrisilith

        return (
            totalAwardedByTracker -
                unbanked
            )
            .coerceAtLeast(
                0
            )
    }

    fun spawnGoldReward(
        worldX: Float,
        worldY: Float,
        amount: Float
    ): Int {
        if (
            amount <=
            0f
        ) {
            return 0
        }

        /*
         * Existing Gold scaling produces fractional rewards.
         * Physical coins must always be worth exactly 1, so fractions are
         * preserved here until they form another whole coin.
         */
        val exact =
            amount +
                fractionalGoldCarry

        val count =
            floor(
                exact
            )
                .toInt()
                .coerceAtLeast(
                    0
                )

        fractionalGoldCarry =
            exact -
                count

        spawnCoins(
            type =
                CurrencyPickupType.GOLD,
            worldX =
                worldX,
            worldY =
                worldY,
            count =
                count
        )

        return count
    }

    fun spawnGrisilithCoins(
        worldX: Float,
        worldY: Float,
        count: Int
    ) {
        spawnCoins(
            type =
                CurrencyPickupType.GRISILITH,
            worldX =
                worldX,
            worldY =
                worldY,
            count =
                count
        )
    }

    private fun spawnCoins(
        type: CurrencyPickupType,
        worldX: Float,
        worldY: Float,
        count: Int
    ) {
        repeat(
            count.coerceAtLeast(
                0
            )
        ) {
            val angle =
                MathUtils.random(
                    0f,
                    MathUtils.PI2
                )

            /*
             * sqrt(random) gives a visually even distribution over the circle,
             * rather than bunching every drop at the center.
             */
            val radius =
                sqrt(
                    MathUtils.random()
                ) *
                    DROP_AREA_RADIUS

            val landingX =
                worldX +
                    MathUtils.cos(
                        angle
                    ) *
                    radius

            val landingY =
                worldY +
                    MathUtils.sin(
                        angle
                    ) *
                    radius

            pickups.add(
                CurrencyPickup(
                    type =
                        type,
                    startX =
                        worldX,
                    startY =
                        worldY,
                    landingX =
                        landingX,
                    landingY =
                        landingY
                )
            )
        }
    }

    fun update(
        delta: Float,
        playerX: Float,
        playerY: Float,
        pickupRadius: Float,
        sessionWallet: SessionWallet,
        runStatistics: RunStatistics
    ) {
        transferFeedbackSeconds =
            (
                transferFeedbackSeconds -
                    delta.coerceAtLeast(
                        0f
                    )
                )
                .coerceAtLeast(
                    0f
                )

        pickups.forEach {
                pickup ->

            val collected =
                pickup.update(
                    delta =
                        delta,
                    playerX =
                        playerX,
                    playerY =
                        playerY,
                    pickupRadius =
                        pickupRadius
                )

            if (!collected) {
                return@forEach
            }

            when (
                pickup.type
            ) {
                CurrencyPickupType.GOLD -> {
                    sessionWallet.addGold(
                        1f
                    )

                    runStatistics
                        .recordGoldCollected(
                            1f
                        )
                }

                CurrencyPickupType.GRISILITH -> {
                    /*
                     * RunRewardTracker already reserves/awards this currency
                     * when the boss dies. Pickup controls only when it becomes
                     * visible as banked run currency.
                     */
                }
            }
        }

        pickups.removeAll {
            it.isCollected
        }
    }

    fun deferUncollectedForNextStage():
        CurrencyStageLoss {

        val uncollectedGold =
            pickups.count {
                it.type ==
                    CurrencyPickupType.GOLD
            }

        val uncollectedGrisilith =
            pickups.count {
                it.type ==
                    CurrencyPickupType.GRISILITH
            }

        /*
         * 25% of every uncollected currency is lost.
         * The amount that survives is floor(total * 0.75).
         *
         * Examples:
         * 1 -> 0 survives
         * 5 -> 3 survives
         * 7 -> 5 survives
         */
        val retainedGold =
            kotlin.math.floor(
                uncollectedGold *
                    UNCOLLECTED_CURRENCY_RETENTION
            )
                .toInt()

        val retainedGrisilith =
            kotlin.math.floor(
                uncollectedGrisilith *
                    UNCOLLECTED_CURRENCY_RETENTION
            )
                .toInt()

        deferredGold +=
            retainedGold

        deferredGrisilith +=
            retainedGrisilith

        pickups.clear()

        return CurrencyStageLoss(
            goldLost =
                (
                    uncollectedGold -
                        retainedGold
                    )
                    .coerceAtLeast(
                        0
                    ),

            grisilithLost =
                (
                    uncollectedGrisilith -
                        retainedGrisilith
                    )
                    .coerceAtLeast(
                        0
                    )
        )
    }

    fun transferDeferredAtStageStart(
        sessionWallet: SessionWallet,
        runStatistics: RunStatistics
    ) {
        val gold =
            deferredGold

        val grisilith =
            deferredGrisilith

        if (
            gold >
            0
        ) {
            sessionWallet.addGold(
                gold.toFloat()
            )

            runStatistics.recordGoldCollected(
                gold.toFloat()
            )
        }

        recentlyTransferredGold =
            gold

        recentlyTransferredGrisilith =
            grisilith

        if (
            gold >
            0 ||
            grisilith >
            0
        ) {
            transferFeedbackSeconds =
                TRANSFER_FEEDBACK_SECONDS
        }

        deferredGold = 0
        deferredGrisilith = 0
    }

    fun bankEverythingAtRunEnd(
        sessionWallet: SessionWallet,
        runStatistics: RunStatistics
    ): CurrencyStageLoss {
        val loss =
            deferUncollectedForNextStage()

        if (
            deferredGold >
            0
        ) {
            sessionWallet.addGold(
                deferredGold.toFloat()
            )

            runStatistics.recordGoldCollected(
                deferredGold.toFloat()
            )
        }

        deferredGold = 0
        deferredGrisilith = 0

        return loss
    }

    fun recordStageStartGoldBonus(
        amount: Int
    ) {
        if (
            amount <=
            0
        ) {
            return
        }

        recentlyTransferredGold +=
            amount

        transferFeedbackSeconds =
            TRANSFER_FEEDBACK_SECONDS
    }

    fun clearAll() {
        pickups.clear()
        deferredGold = 0
        deferredGrisilith = 0
        recentlyTransferredGold = 0
        recentlyTransferredGrisilith = 0
        transferFeedbackSeconds = 0f
        fractionalGoldCarry = 0f
    }

    companion object {
        private const val DROP_AREA_RADIUS =
            34f

        private const val TRANSFER_FEEDBACK_SECONDS =
            4.0f

        private const val UNCOLLECTED_CURRENCY_RETENTION =
            0.75f
    }
}
