package com.azizoglu.grisilith.unlock

import com.azizoglu.grisilith.profile.PlayerProfileRepository

object UnlockManager {

    fun purchase(
        unlockId: String
    ): UnlockPurchaseResult {

        val definition =
            UnlockRegistry.find(
                unlockId
            )
                ?: return UnlockPurchaseResult
                    .UNKNOWN_UNLOCK

        val profile =
            PlayerProfileRepository
                .profile

        val isRepeatableStatPoint =
            definition.type ==
                UnlockType.STAT_POINT

        if (
            !isRepeatableStatPoint &&
            PlayerProfileRepository.isUnlocked(
                unlockId
            )
        ) {
            return UnlockPurchaseResult
                .ALREADY_UNLOCKED
        }

        if (
            !UnlockRegistry
                .dependenciesAreMet(
                    definition =
                        definition,

                    unlockedIds =
                        profile.unlockedIds
                )
        ) {
            return UnlockPurchaseResult
                .LOCKED
        }

        if (
            !profile.canSpendGrisilithCoins(
                definition.cost
            )
        ) {
            return UnlockPurchaseResult
                .NOT_ENOUGH_COINS
        }

        val coinsWereSpent =
            PlayerProfileRepository
                .spendGrisilithCoins(
                    definition.cost
                )

        if (!coinsWereSpent) {
            return UnlockPurchaseResult
                .NOT_ENOUGH_COINS
        }

        if (isRepeatableStatPoint) {

            PlayerProfileRepository
                .addPurchasedStatPoints(
                    1
                )

            return UnlockPurchaseResult
                .SUCCESS
        }

        val wasUnlocked =
            PlayerProfileRepository
                .unlock(
                    definition.id
                )

        if (!wasUnlocked) {

            PlayerProfileRepository
                .addGrisilithCoins(
                    definition.cost
                )

            return UnlockPurchaseResult
                .ALREADY_UNLOCKED
        }

        return UnlockPurchaseResult
            .SUCCESS
    }
}
