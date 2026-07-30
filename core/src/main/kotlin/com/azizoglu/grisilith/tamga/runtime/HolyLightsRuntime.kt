package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt


class HolyLightsRuntime(
    private val playerStats:
    PlayerStats,

    private val preferredStats:
    Set<StatType>,

    private val currentStageProvider:
        () -> Int
) {

    companion object {

        const val DAMAGE_SOURCE_NAME =
            "Holy Lights"

        private const val AURA_RANGE =
            155f

        private const val AURA_TICK_INTERVAL =
            0.5f

        private const val STAGE_BONUS_INTERVAL =
            5

        private const val INITIAL_STAT_BONUS =
            1

        /*
         * Prototip hasar değerleri.
         *
         * Daha sonra oyun dengesi sırasında
         * yalnızca bu sabitler değiştirilebilir.
         */
        private const val ARMOR_BASE_DAMAGE =
            4f

        private const val ARMOR_DAMAGE_SCALING =
            0.35f

        private const val WISDOM_BASE_DAMAGE =
            7f

        private const val WISDOM_DAMAGE_SCALING =
            0.75f

        private const val LIFE_STEAL_BASE_DAMAGE =
            4f

        private const val LIFE_STEAL_DAMAGE_SCALING =
            0.35f
    }

    private val candidateStats =
        listOf(
            StatType.ARMOR_CLASS,
            StatType.WISDOM,
            StatType.LIFE_STEAL
        )

    private var tickTimer =
        0f

    private var selectedMode:
        HolyLightsMode? =
        null

    /*
     * Eşitlik devam ettiği sürece her frame
     * farklı rastgele sonuç seçilmesini önler.
     */
    private var previousTieCandidates:
        Set<StatType> =
        emptySet()

    val currentMode:
        HolyLightsMode?
        get() =
            selectedMode

    val auraRange:
        Float
        get() =
            AURA_RANGE

    fun update(
        delta: Float,
        player: Player,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onLifeStealDamage: (Int) -> Unit
    ) {
        if (
            delta <= 0f ||
            player.health.isDead
        ) {
            return
        }

        refreshSelectedMode()
        synchronizeStatBonus()

        tickTimer += delta

        while (
            tickTimer >=
            AURA_TICK_INTERVAL
        ) {
            tickTimer -=
                AURA_TICK_INTERVAL

            performAuraTick(
                player = player,
                enemies = enemies,
                onDamageDealt =
                    onDamageDealt,
                onEnemyKilled =
                    onEnemyKilled,
                onLifeStealDamage =
                    onLifeStealDamage
            )
        }
    }

    fun reset() {
        tickTimer = 0f
        selectedMode = null
        previousTieCandidates =
            emptySet()

        playerStats.clearTamgaBonuses()
    }

    fun deactivate() {
        reset()
    }

    fun render(
        shapeRenderer: ShapeRenderer,
        player: Player
    ) {
        val mode =
            selectedMode
                ?: return

        if (player.health.isDead) {
            return
        }

        shapeRenderer.color =
            auraColor(
                mode
            )

        /*
         * ShapeRenderer Filled modunda çalıştığı
         * için dış ve iç daire çizilerek halka
         * görünümü oluşturulur.
         */
        shapeRenderer.circle(
            player.centerX,
            player.centerY,
            AURA_RANGE,
            72
        )

        shapeRenderer.color =
            Color(
                0.035f,
                0.025f,
                0.055f,
                1f
            )

        shapeRenderer.circle(
            player.centerX,
            player.centerY,
            AURA_RANGE - 5f,
            72
        )
    }

    private fun refreshSelectedMode() {
        val maximumScore =
            candidateStats.maxOf { statType ->
                playerStats
                    .totalScoreWithoutTamga(
                        statType
                    )
            }

        val highestStats =
            candidateStats
                .filter { statType ->
                    playerStats
                        .totalScoreWithoutTamga(
                            statType
                        ) ==
                        maximumScore
                }
                .toSet()

        val selectedStat =
            selectStatFromCandidates(
                highestStats
            )

        selectedMode =
            HolyLightsMode.fromStatType(
                selectedStat
            )
    }

    private fun selectStatFromCandidates(
        highestStats: Set<StatType>
    ): StatType {
        require(
            highestStats.isNotEmpty()
        )

        if (highestStats.size == 1) {
            previousTieCandidates =
                emptySet()

            return highestStats.first()
        }

        val preferredCandidates =
            highestStats
                .intersect(
                    preferredStats
                )

        val finalCandidates =
            if (
                preferredCandidates
                    .isNotEmpty()
            ) {
                preferredCandidates
            } else {
                highestStats
            }

        /*
         * Önceki rastgele seçim aynı eşitlik
         * grubunda hâlâ geçerliyse korunur.
         */
        val previousStat =
            selectedMode
                ?.statType

        if (
            previousTieCandidates ==
            finalCandidates &&
            previousStat in
            finalCandidates
        ) {
            return previousStat!!
        }

        previousTieCandidates =
            finalCandidates

        return finalCandidates
            .toList()
            .random()
    }

    private fun synchronizeStatBonus() {
        val mode =
            selectedMode

        if (mode == null) {
            playerStats
                .clearTamgaBonuses()

            return
        }

        playerStats.setTamgaBonuses(
            mapOf(
                mode.statType to
                    currentStageStatBonus()
            )
        )
    }

    private fun currentStageStatBonus():
        Int {

        val currentStage =
            currentStageProvider()
                .coerceAtLeast(1)

        /*
         * Stage 1–4   = +1
         * Stage 5–9   = +2
         * Stage 10–14 = +3
         */
        return INITIAL_STAT_BONUS +
            currentStage /
            STAGE_BONUS_INTERVAL
    }

    private fun performAuraTick(
        player: Player,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onLifeStealDamage: (Int) -> Unit
    ) {
        val mode =
            selectedMode
                ?: return

        val maximumDistanceSquared =
            AURA_RANGE *
                AURA_RANGE

        enemies.forEach { enemy ->
            if (enemy.isDead) {
                return@forEach
            }

            val distanceSquared =
                Vector2.dst2(
                    player.centerX,
                    player.centerY,
                    enemy.centerX,
                    enemy.centerY
                )

            if (
                distanceSquared >
                maximumDistanceSquared
            ) {
                return@forEach
            }

            damageEnemy(
                enemy = enemy,
                mode = mode,
                onDamageDealt =
                    onDamageDealt,
                onEnemyKilled =
                    onEnemyKilled,
                onLifeStealDamage =
                    onLifeStealDamage
            )
        }
    }

    private fun damageEnemy(
        enemy: Enemy,
        mode: HolyLightsMode,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit,
        onLifeStealDamage: (Int) -> Unit
    ) {
        if (enemy.isDead) {
            return
        }

        val requestedDamage =
            calculateDamage(
                mode
            )

        val wasAliveBeforeDamage =
            !enemy.isDead

        val appliedDamage =
            enemy.takeDamage(
                requestedDamage
            )

        if (appliedDamage > 0) {
            onDamageDealt(
                appliedDamage,
                DAMAGE_SOURCE_NAME
            )

            /*
             * Yalnızca Life Steal varyantındaki
             * aura hitleri Life Steal denemesi
             * yapar.
             */
            if (
                mode ==
                HolyLightsMode.LIFE_STEAL
            ) {
                onLifeStealDamage(
                    appliedDamage
                )
            }
        }

        if (
            wasAliveBeforeDamage &&
            enemy.isDead
        ) {
            onEnemyKilled(
                enemy
            )
        }
    }

    private fun calculateDamage(
        mode: HolyLightsMode
    ): Int {
        val relevantScore =
            playerStats.totalScore(
                mode.statType
            )

        val rawDamage =
            when (mode) {
                HolyLightsMode.ARMOR_CLASS -> {
                    ARMOR_BASE_DAMAGE +
                        relevantScore *
                        ARMOR_DAMAGE_SCALING
                }

                HolyLightsMode.WISDOM -> {
                    WISDOM_BASE_DAMAGE +
                        relevantScore *
                        WISDOM_DAMAGE_SCALING
                }

                HolyLightsMode.LIFE_STEAL -> {
                    LIFE_STEAL_BASE_DAMAGE +
                        relevantScore *
                        LIFE_STEAL_DAMAGE_SCALING
                }
            }

        return rawDamage
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun auraColor(
        mode: HolyLightsMode
    ): Color {
        return when (mode) {
            HolyLightsMode.ARMOR_CLASS -> {
                Color(
                    0.82f,
                    0.88f,
                    1f,
                    0.28f
                )
            }

            HolyLightsMode.WISDOM -> {
                Color(
                    1f,
                    0.91f,
                    0.35f,
                    0.30f
                )
            }

            HolyLightsMode.LIFE_STEAL -> {
                Color(
                    1f,
                    0.42f,
                    0.65f,
                    0.28f
                )
            }
        }
    }
}
