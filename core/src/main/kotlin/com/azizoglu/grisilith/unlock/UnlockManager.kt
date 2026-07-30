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
                ?: return UnlockPurchaseResult.UNKNOWN_UNLOCK

        val profile =
            PlayerProfileRepository
                .profile

        if (
            profile.isUnlocked(
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

        val wasUnlocked =
            PlayerProfileRepository
                .unlock(
                    definition.id
                )

        if (!wasUnlocked) {
            /*
             * Normal akışta buraya girilmez.
             * Coin kaybını önlemek için geri ödeme yapılır.
             */
            PlayerProfileRepository
                .addGrisilithCoins(
                    definition.cost
                )

            return UnlockPurchaseResult
                .ALREADY_UNLOCKED
        }

        applyPurchasedEffect(
            definition
        )

        return UnlockPurchaseResult
            .SUCCESS
    }

    private fun applyPurchasedEffect(
        definition: UnlockDefinition
    ) {
        when (definition.id) {
            UnlockRegistry
                .STAT_POINT_01_ID -> {

                PlayerProfileRepository
                    .addPurchasedStatPoints(
                        1
                    )
            }
        }
    }
}
