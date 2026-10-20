package com.azizoglu.grisilith.profile

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

object PlayerProfileRepository {

    // v0.3 open-beta starting funds; never refill an existing saved wallet on launch.
    const val BETA_STARTING_GRISILITH_COINS = 1000

    private const val PREFERENCES_NAME =
        "grisilith_player_profile"

    private const val KEY_VERSION =
        "version"

    private const val KEY_GRISILITH_COINS =
        "grisilith_coins"

    private const val KEY_PURCHASED_STAT_POINTS =
        "purchased_stat_points"

    private const val KEY_UNLOCKED_IDS =
        "unlocked_ids"

    private const val UNLOCK_ID_SEPARATOR =
        "\n"

    private const val KEY_DEFEATED_HARD_FINAL_BOSS_IDS =
        "defeated_hard_final_boss_ids"

    private const val ID_SEPARATOR =
        "\n"

    private const val KEY_ENCOUNTERED_MAP_IDS =
        "encountered_map_ids"

    private const val KEY_ENCOUNTERED_ENEMY_IDS =
        "encountered_enemy_ids"

    private val preferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                PREFERENCES_NAME
            )

    private var currentProfile =
        PlayerProfile()

    val profile:
        PlayerProfile
        get() =
            currentProfile

    fun load() {
        val isNewProfile = !preferences.contains(KEY_VERSION) &&
            !preferences.contains(KEY_GRISILITH_COINS)
        val loadedSnapshot =
            PlayerProfileSnapshot(
                version =
                    preferences.getInteger(
                        KEY_VERSION,
                        1
                    ),

                grisilithCoins =
                    preferences.getInteger(
                        KEY_GRISILITH_COINS,
                        if (isNewProfile) BETA_STARTING_GRISILITH_COINS else 0
                    )
                        .coerceAtLeast(0),

                purchasedStatPoints =
                    preferences.getInteger(
                        KEY_PURCHASED_STAT_POINTS,
                        0
                    )
                        .coerceAtLeast(0),

                unlockedIds =
                    decodeIds(
                        preferences.getString(
                            KEY_UNLOCKED_IDS,
                            ""
                        )
                    ),

                defeatedHardFinalBossIds =
                    decodeIds(
                        preferences.getString(
                            KEY_DEFEATED_HARD_FINAL_BOSS_IDS,
                            ""
                        )
                    ),

                encounteredMapIds =
                    decodeIds(
                        preferences.getString(
                            KEY_ENCOUNTERED_MAP_IDS,
                            ""
                        )
                    ),

                encounteredEnemyIds =
                    decodeIds(
                        preferences.getString(
                            KEY_ENCOUNTERED_ENEMY_IDS,
                            ""
                        )
                    )
            )

        currentProfile =
            PlayerProfile(
                initialGrisilithCoins =
                    loadedSnapshot
                        .grisilithCoins,

                initialPurchasedStatPoints =
                    loadedSnapshot
                        .purchasedStatPoints,

                initialUnlockedIds =
                    loadedSnapshot
                        .unlockedIds,

                initialDefeatedHardFinalBossIds =
                    loadedSnapshot
                        .defeatedHardFinalBossIds,

                initialEncounteredMapIds =
                    loadedSnapshot
                        .encounteredMapIds,

                initialEncounteredEnemyIds =
                    loadedSnapshot
                        .encounteredEnemyIds
            )

        if (
            isNewProfile || loadedSnapshot.version <
            PlayerProfileSnapshot
                .CURRENT_VERSION
        ) {
            save()
        }
    }

    fun addGrisilithCoins(
        amount: Int
    ) {
        if (amount <= 0) {
            return
        }

        currentProfile
            .addGrisilithCoins(
                amount
            )

        save()
    }

    fun spendGrisilithCoins(
        amount: Int
    ): Boolean {

        val wasSpent =
            currentProfile
                .spendGrisilithCoins(
                    amount
                )

        if (wasSpent) {
            save()
        }

        return wasSpent
    }

    fun addPurchasedStatPoints(
        amount: Int
    ) {
        if (amount <= 0) {
            return
        }

        currentProfile
            .addPurchasedStatPoints(
                amount
            )

        save()
    }

    fun isUnlocked(
        unlockId: String
    ): Boolean {

        return com.azizoglu.grisilith.unlock.UnlockRegistry.previewUnlocked(unlockId) || currentProfile
            .isUnlocked(
                unlockId
            )
    }

    fun unlock(
        unlockId: String
    ): Boolean {

        val wasUnlocked =
            currentProfile
                .unlock(
                    unlockId
                )

        if (wasUnlocked) {
            save()
        }

        return wasUnlocked
    }

    fun resetForBeta() {

        currentProfile =
            PlayerProfile(initialGrisilithCoins = BETA_STARTING_GRISILITH_COINS)

        preferences.clear()
        save()
    }

    fun save() {
        preferences
            .putInteger(
                KEY_VERSION,
                PlayerProfileSnapshot
                    .CURRENT_VERSION
            )
            .putInteger(
                KEY_GRISILITH_COINS,
                currentProfile
                    .grisilithCoins
            )
            .putInteger(
                KEY_PURCHASED_STAT_POINTS,
                currentProfile
                    .purchasedStatPoints
            )
            .putString(
                KEY_UNLOCKED_IDS,
                encodeIds(
                    currentProfile.unlockedIds
                )
            )

            .putString(
                KEY_DEFEATED_HARD_FINAL_BOSS_IDS,
                encodeIds(
                    currentProfile
                        .defeatedHardFinalBossIds
                )
            )

            .putString(
                KEY_ENCOUNTERED_MAP_IDS,
                encodeIds(
                    currentProfile
                        .encounteredMapIds
                )
            )
            .putString(
                KEY_ENCOUNTERED_ENEMY_IDS,
                encodeIds(
                    currentProfile
                        .encounteredEnemyIds
                )
            )

            .flush()
    }

    private fun encodeIds(
        ids: Set<String>
    ): String {
        return ids
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .sorted()
            .joinToString(
                separator =
                    ID_SEPARATOR
            )
    }

    private fun decodeIds(
        encodedValue: String
    ): Set<String> {
        if (encodedValue.isBlank()) {
            return emptySet()
        }

        return encodedValue
            .split(
                ID_SEPARATOR
            )
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .toSet()
    }

    fun applyRunRewards(
        grisilithCoins: Int,
        defeatedHardFinalBossIds: Set<String>
    ) {
        var changed =
            false

        if (grisilithCoins > 0) {
            currentProfile.addGrisilithCoins(
                grisilithCoins
            )

            changed =
                true
        }

        if (
            currentProfile.markHardFinalBossesDefeated(
                defeatedHardFinalBossIds
            )
        ) {
            changed =
                true
        }

        if (changed) {
            save()
        }
    }

    fun markMapEncountered(
        mapId: String
    ): Boolean {

        val changed =
            currentProfile
                .markMapEncountered(
                    mapId
                )

        if (changed) {
            save()
        }

        return changed
    }

    fun markEnemyEncountered(
        enemyId: String
    ): Boolean {

        val changed =
            currentProfile
                .markEnemyEncountered(
                    enemyId
                )

        if (changed) {
            save()
        }

        return changed
    }
}
