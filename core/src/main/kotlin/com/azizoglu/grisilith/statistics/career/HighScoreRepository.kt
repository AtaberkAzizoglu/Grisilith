package com.azizoglu.grisilith.statistics.career

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import kotlin.math.max

enum class CareerAchievement {
    ENEMIES,
    BOSSES,
    FINAL_BOSSES
}

object HighScoreRepository {

    private const val PREFERENCES_NAME =
        "grisilith_high_scores"

    // New kill-only Score; never infer these from legacy damage/stage totals.
    private const val KEY_TOTAL_SCORE = "score_v1_total"
    private const val KEY_HIGHEST_SCORE = "score_v1_highest"

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

    private const val KEY_ENEMY_ACHIEVEMENT_CLAIMS =
        "enemy_achievement_claims"

    private const val KEY_BOSS_ACHIEVEMENT_CLAIMS =
        "boss_achievement_claims"

    private const val KEY_FINAL_BOSS_ACHIEVEMENT_CLAIMS =
        "final_boss_achievement_claims"

    private const val ENEMY_ACHIEVEMENT_STEP =
        1_000L

    private const val BOSS_ACHIEVEMENT_STEP =
        20L

    private const val FINAL_BOSS_ACHIEVEMENT_STEP =
        1L

    private val preferences:
        Preferences
        get() =
            Gdx.app.getPreferences(
                PREFERENCES_NAME
            )

    private var currentStatistics =
        CareerStatistics()

    private var enemyAchievementClaims =
        0

    private var bossAchievementClaims =
        0

    private var finalBossAchievementClaims =
        0

    val statistics:
        CareerStatistics
        get() =
            currentStatistics

    fun load() {

        enemyAchievementClaims =
            preferences.getInteger(
                KEY_ENEMY_ACHIEVEMENT_CLAIMS,
                0
            ).coerceAtLeast(0)

        bossAchievementClaims =
            preferences.getInteger(
                KEY_BOSS_ACHIEVEMENT_CLAIMS,
                0
            ).coerceAtLeast(0)

        finalBossAchievementClaims =
            preferences.getInteger(
                KEY_FINAL_BOSS_ACHIEVEMENT_CLAIMS,
                0
            ).coerceAtLeast(0)

        currentStatistics =
            CareerStatistics(
                totalScore = preferences.getLong(KEY_TOTAL_SCORE,0L).coerceAtLeast(0L),
                highestScore = preferences.getLong(KEY_HIGHEST_SCORE,0L).coerceAtLeast(0L),
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

    fun achievementValue(
        achievement: CareerAchievement
    ): Long =
        when (achievement) {
            CareerAchievement.ENEMIES ->
                currentStatistics.enemiesKilled

            CareerAchievement.BOSSES ->
                currentStatistics.bossesDefeated

            CareerAchievement.FINAL_BOSSES ->
                currentStatistics.finalBossesDefeated
        }

    fun achievementTarget(
        achievement: CareerAchievement
    ): Long =
        achievementStep(achievement) *
            (achievementClaims(achievement) + 1L)

    fun achievementProgress(
        achievement: CareerAchievement
    ): Float =
        (
            achievementValue(achievement).toFloat() /
                achievementTarget(achievement).toFloat()
            ).coerceIn(0f, 1f)

    fun canClaimAchievementReward(
        achievement: CareerAchievement
    ): Boolean =
        achievementValue(achievement) >=
            achievementTarget(achievement)

    fun hasClaimableAchievementRewards(): Boolean =
        CareerAchievement.entries.any(
            ::canClaimAchievementReward
        )

    /** Collect every completed milestone without repeated preference writes. */
    fun claimAllAchievementRewards(): Int {
        var rewards = 0
        for (achievement in CareerAchievement.entries) {
            val claimed = achievementClaims(achievement)
            val pending = (achievementValue(achievement) / achievementStep(achievement) - claimed)
                .coerceIn(0L, (Int.MAX_VALUE - claimed).toLong())
                .coerceAtMost((Int.MAX_VALUE - rewards).toLong()).toInt()
            when (achievement) {
                CareerAchievement.ENEMIES -> enemyAchievementClaims += pending
                CareerAchievement.BOSSES -> bossAchievementClaims += pending
                CareerAchievement.FINAL_BOSSES -> finalBossAchievementClaims += pending
            }
            rewards += pending
        }
        if (rewards > 0) save()
        return rewards
    }

    fun claimAchievementReward(
        achievement: CareerAchievement
    ): Boolean {

        if (!canClaimAchievementReward(achievement)) {
            return false
        }

        when (achievement) {
            CareerAchievement.ENEMIES ->
                enemyAchievementClaims += 1

            CareerAchievement.BOSSES ->
                bossAchievementClaims += 1

            CareerAchievement.FINAL_BOSSES ->
                finalBossAchievementClaims += 1
        }

        save()
        return true
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
        val score = run.score.coerceAtLeast(0L)

        currentStatistics =
            previous.copy(
                totalScore = previous.totalScore + score.coerceAtMost(Long.MAX_VALUE-previous.totalScore),
                highestScore = max(previous.highestScore,score),
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

        enemyAchievementClaims =
            0

        bossAchievementClaims =
            0

        finalBossAchievementClaims =
            0

        preferences.clear()
        preferences.flush()
    }

    private fun save() {

        preferences
            .putLong(KEY_TOTAL_SCORE,currentStatistics.totalScore)
            .putLong(KEY_HIGHEST_SCORE,currentStatistics.highestScore)
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
            .putInteger(
                KEY_ENEMY_ACHIEVEMENT_CLAIMS,
                enemyAchievementClaims
            )
            .putInteger(
                KEY_BOSS_ACHIEVEMENT_CLAIMS,
                bossAchievementClaims
            )
            .putInteger(
                KEY_FINAL_BOSS_ACHIEVEMENT_CLAIMS,
                finalBossAchievementClaims
            )
            .flush()
    }

    private fun achievementClaims(
        achievement: CareerAchievement
    ): Int =
        when (achievement) {
            CareerAchievement.ENEMIES ->
                enemyAchievementClaims

            CareerAchievement.BOSSES ->
                bossAchievementClaims

            CareerAchievement.FINAL_BOSSES ->
                finalBossAchievementClaims
        }

    private fun achievementStep(
        achievement: CareerAchievement
    ): Long =
        when (achievement) {
            CareerAchievement.ENEMIES ->
                ENEMY_ACHIEVEMENT_STEP

            CareerAchievement.BOSSES ->
                BOSS_ACHIEVEMENT_STEP

            CareerAchievement.FINAL_BOSSES ->
                FINAL_BOSS_ACHIEVEMENT_STEP
        }
}
