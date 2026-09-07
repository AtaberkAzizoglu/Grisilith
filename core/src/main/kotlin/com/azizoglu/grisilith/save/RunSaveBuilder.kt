package com.azizoglu.grisilith.save

import com.azizoglu.grisilith.armor.runtime.EquippedArmor
import com.azizoglu.grisilith.config.GameDifficulty
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.economy.SessionWallet
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.reward.RunRewardTracker
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.RunStatistics
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import java.util.ArrayList

object RunSaveBuilder {

    fun createSnapshot(
        difficulty: GameDifficulty,
        gameMap: GameMap,

        characterDefinitionId: String,
        characterDisplayName: String,

        presetId: String?,

        checkpointStage: Int,
        maximumStage: Int,

        elapsedRunTimeSeconds: Float,
        currentHealth: Int,

        playerStats: PlayerStats,
        weaponInventory: WeaponInventory,
        equippedArmor: EquippedArmor,
        itemInventory: ItemInventory,
        itemEffectState: RunSaveItemEffectState = RunSaveItemEffectState(),
        sessionWallet: SessionWallet,
        runStatistics: RunStatistics,
        runRewardTracker: RunRewardTracker,

        remainingChestDiscards: Int,

        troubleAtHomeGateHealth: Int = 1000,
        villageGuardCount: Int = 0,

        weaponTamgaDefinitionId: String?,
        armorTamgaDefinitionId: String?,

        weaponTamgaRuntimeState:
        RunSaveActiveTamgaState? =
            null,

        armorTamgaRuntimeState:
        RunSaveActiveTamgaState? =
            null,

        savedAtEpochMillis: Long =
            System.currentTimeMillis()
    ): RunSaveSnapshot {

        require(
            characterDefinitionId
                .isNotBlank()
        ) {
            "Character definition ID cannot be blank."
        }

        require(
            characterDisplayName
                .isNotBlank()
        ) {
            "Character display name cannot be blank."
        }

        require(
            checkpointStage in
                1..maximumStage
        ) {
            "Checkpoint stage must be inside the run's stage range."
        }

        require(
            currentHealth >
                0
        ) {
            "A dead player cannot be used for a checkpoint."
        }

        return RunSaveSnapshot(
            version =
                RunSaveSnapshot
                    .CURRENT_VERSION,

            savedAtEpochMillis =
                savedAtEpochMillis,

            difficultyName =
                difficulty.name,

            mapName =
                gameMap.name,

            characterDefinitionId =
                characterDefinitionId,

            presetId =
                presetId,

            checkpointStage =
                checkpointStage,

            maximumStage =
                maximumStage,

            elapsedRunTimeSeconds =
                elapsedRunTimeSeconds
                    .coerceAtLeast(
                        0f
                    ),

            currentHealth =
                currentHealth,

            sessionGold =
                sessionWallet
                    .exportGold(),

            remainingChestDiscards =
                remainingChestDiscards
                    .coerceAtLeast(
                        0
                    ),

            troubleAtHomeGateHealth =
                troubleAtHomeGateHealth
                    .coerceIn(1, 1000),

            villageGuardCount =
                villageGuardCount.coerceIn(0, 5),

            statLayers =
                playerStats
                    .exportSaveLayers(),

            weaponSlots =
                ArrayList(
                    weaponInventory
                        .exportSaveSlots()
                ),

            armorDefinitionId =
                equippedArmor
                    .exportDefinitionId(),

            weaponTamgaDefinitionId =
                weaponTamgaDefinitionId
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    },

            armorTamgaDefinitionId =
                armorTamgaDefinitionId
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    },

            weaponTamgaRuntimeState =
                weaponTamgaRuntimeState,

            armorTamgaRuntimeState =
                armorTamgaRuntimeState,

            itemStacks =
                ArrayList(
                    itemInventory
                        .exportSaveStacks()
                ),

            itemEffectState =
                itemEffectState,

            statistics =
                runStatistics
                    .exportSaveState(),

            rewardState =
                runRewardTracker
                    .exportSaveState(),

            preview =
                RunSavePreview(
                    characterDisplayName =
                        characterDisplayName,

                    mapDisplayName =
                        gameMap.displayName,

                    difficultyDisplayName =
                        difficulty.displayName,

                    stage =
                        checkpointStage,

                    maximumStage =
                        maximumStage,

                    elapsedRunTimeSeconds =
                        elapsedRunTimeSeconds
                            .coerceAtLeast(
                                0f
                            ),

                    savedAtEpochMillis =
                        savedAtEpochMillis
                )
        )
    }
}
