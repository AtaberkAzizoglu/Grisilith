package com.azizoglu.grisilith.statistics.career

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import kotlin.math.max

object HighScoreRepository {

    private const val PREFERENCES_NAME =
        "grisilith_high_scores"

    private const val KEY_ENEMIES_KILLED =
        "enemies_killed"

    private const val KEY_BOSSES_DEFEATED =
        "bosses_defeated"

    private const val KEY_FINAL_BOSSES_DEFEATED =
        "final_bosses_defeated"

    private const val KEY_DAMAGE_DEALT =
        "damage_dealt"

    private const val KEY_DAMAGE_TAKEN =
        "damage_taken"

    private const val KEY_GOLD_COLLECTED =
        "gold_collected"

    private const val KEY_GOLD_SPENT =
        "gold_spent"

    private const val KEY_GRISILITH_COINS_EARNED =
        "grisilith_coins_earned"

    private const val KEY_TOTAL_PLAY_TIME_SECONDS =
        "total_play_time_seconds"

    private const val KEY_RUNS_STARTED =
        "runs_started"

    private const val KEY_RUNS_FINISHED =
        "runs_finished"

    private const val KEY_RUNS_WON =
        "runs_won"

    private const val KEY_RUNS_LOST =
        "runs_lost"

    private const val KEY_LONGEST_RUN_SECONDS =
        "longest_run_seconds"

    private const val KEY_HIGHEST_DAMAGE =
        "highest_damage"

    private const val KEY_MOST_GOLD =
        "most_gold"

    private const val KEY_MOST_GRISILITH_COINS =
        "most_grisilith_coins"

    private const val KEY_MOST_DAMAGE_TAKEN =
        "most_damage_taken"

    private val preferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                PREFERENCES_NAME
            )

    private var currentStatistics =
        CareerStatistics()

    val statistics:
        CareerStatistics
        get() =
            currentStatistics

    fun load() {

        currentStatistics =
            CareerStatistics(
                enemiesKilled =
                    preferences.getLong(
                        KEY_ENEMIES_KILLED,
                        0L
                    ),

                bossesDefeated =
                    preferences.getLong(
                        KEY_BOSSES_DEFEATED,
                        0L
                    ),

                finalBossesDefeated =
                    preferences.getLong(
                        KEY_FINAL_BOSSES_DEFEATED,
                        0L
                    ),

                damageDealt =
                    preferences.getLong(
                        KEY_DAMAGE_DEALT,
                        0L
                    ),

                damageTaken =
                    preferences.getLong(
                        KEY_DAMAGE_TAKEN,
                        0L
                    ),

                goldCollected =
                    preferences.getFloat(
                        KEY_GOLD_COLLECTED,
                        0f
                    ),

                goldSpent =
                    preferences.getFloat(
                        KEY_GOLD_SPENT,
                        0f
                    ),

                grisilithCoinsEarned =
                    preferences.getLong(
                        KEY_GRISILITH_COINS_EARNED,
                        0L
                    ),

                totalPlayTimeSeconds =
                    preferences.getLong(
                        KEY_TOTAL_PLAY_TIME_SECONDS,
                        0L
                    ),

                runsStarted =
                    preferences.getLong(
                        KEY_RUNS_STARTED,
                        0L
                    ),

                runsFinished =
                    preferences.getLong(
                        KEY_RUNS_FINISHED,
                        0L
                    ),

                runsWon =
                    preferences.getLong(
                        KEY_RUNS_WON,
                        0L
                    ),

                runsLost =
                    preferences.getLong(
                        KEY_RUNS_LOST,
                        0L
                    ),

                longestRunSeconds =
                    preferences.getLong(
                        KEY_LONGEST_RUN_SECONDS,
                        0L
                    ),

                highestDamage =
                    preferences.getLong(
                        KEY_HIGHEST_DAMAGE,
                        0L
                    ),

                mostGold =
                    preferences.getFloat(
                        KEY_MOST_GOLD,
                        0f
                    ),

                mostGrisilithCoins =
                    preferences.getInteger(
                        KEY_MOST_GRISILITH_COINS,
                        0
                    ),

                mostDamageTaken =
                    preferences.getLong(
                        KEY_MOST_DAMAGE_TAKEN,
                        0L
                    )
            )
    }

    fun recordRunStarted() {

        currentStatistics =
            currentStatistics.copy(
                runsStarted =
                    currentStatistics.runsStarted +
                        1L
            )

        save()
    }

    fun commitCompletedRun(
        run: CompletedRunStatistics
    ) {

        val previous =
            currentStatistics

        currentStatistics =
            previous.copy(
                enemiesKilled =
                    previous.enemiesKilled +
                        run.enemiesKilled,

                bossesDefeated =
                    previous.bossesDefeated +
                        run.bossesDefeated,

                finalBossesDefeated =
                    previous.finalBossesDefeated +
                        run.finalBossesDefeated,

                damageDealt =
                    previous.damageDealt +
                        run.damageDealt,

                damageTaken =
                    previous.damageTaken +
                        run.damageTaken,

                goldCollected =
                    previous.goldCollected +
                        run.goldCollected,

                goldSpent =
                    previous.goldSpent +
                        run.goldSpent,

                grisilithCoinsEarned =
                    previous.grisilithCoinsEarned +
                        run.grisilithCoinsEarned,

                totalPlayTimeSeconds =
                    previous.totalPlayTimeSeconds +
                        run.durationSeconds,

                runsFinished =
                    previous.runsFinished +
                        1L,

                runsWon =
                    previous.runsWon +
                        if (run.wasSuccessful) {
                            1L
                        } else {
                            0L
                        },

                runsLost =
                    previous.runsLost +
                        if (run.wasSuccessful) {
                            0L
                        } else {
                            1L
                        },

                longestRunSeconds =
                    max(
                        previous.longestRunSeconds,
                        run.durationSeconds
                    ),

                highestDamage =
                    max(
                        previous.highestDamage,
                        run.damageDealt
                    ),

                mostGold =
                    max(
                        previous.mostGold,
                        run.goldCollected
                    ),

                mostGrisilithCoins =
                    max(
                        previous.mostGrisilithCoins,
                        run.grisilithCoinsEarned
                    ),

                mostDamageTaken =
                    max(
                        previous.mostDamageTaken,
                        run.damageTaken
                    )
            )

        save()
    }

    fun resetForBeta() {

        currentStatistics =
            CareerStatistics()

        preferences.clear()
        preferences.flush()
    }

    private fun save() {

        preferences
            .putLong(
                KEY_ENEMIES_KILLED,
                currentStatistics.enemiesKilled
            )
            .putLong(
                KEY_BOSSES_DEFEATED,
                currentStatistics.bossesDefeated
            )
            .putLong(
                KEY_FINAL_BOSSES_DEFEATED,
                currentStatistics.finalBossesDefeated
            )
            .putLong(
                KEY_DAMAGE_DEALT,
                currentStatistics.damageDealt
            )
            .putLong(
                KEY_DAMAGE_TAKEN,
                currentStatistics.damageTaken
            )
            .putFloat(
                KEY_GOLD_COLLECTED,
                currentStatistics.goldCollected
            )
            .putFloat(
                KEY_GOLD_SPENT,
                currentStatistics.goldSpent
            )
            .putLong(
                KEY_GRISILITH_COINS_EARNED,
                currentStatistics.grisilithCoinsEarned
            )
            .putLong(
                KEY_TOTAL_PLAY_TIME_SECONDS,
                currentStatistics.totalPlayTimeSeconds
            )
            .putLong(
                KEY_RUNS_STARTED,
                currentStatistics.runsStarted
            )
            .putLong(
                KEY_RUNS_FINISHED,
                currentStatistics.runsFinished
            )
            .putLong(
                KEY_RUNS_WON,
                currentStatistics.runsWon
            )
            .putLong(
                KEY_RUNS_LOST,
                currentStatistics.runsLost
            )
            .putLong(
                KEY_LONGEST_RUN_SECONDS,
                currentStatistics.longestRunSeconds
            )
            .putLong(
                KEY_HIGHEST_DAMAGE,
                currentStatistics.highestDamage
            )
            .putFloat(
                KEY_MOST_GOLD,
                currentStatistics.mostGold
            )
            .putInteger(
                KEY_MOST_GRISILITH_COINS,
                currentStatistics.mostGrisilithCoins
            )
            .putLong(
                KEY_MOST_DAMAGE_TAKEN,
                currentStatistics.mostDamageTaken
            )
            .flush()
    }
}
