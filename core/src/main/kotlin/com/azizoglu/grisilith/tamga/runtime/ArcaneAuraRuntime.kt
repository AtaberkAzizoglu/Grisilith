package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

class ArcaneAuraRuntime(
    private val playerStats:
    PlayerStats,

    private val weaponInventory:
    WeaponInventory,

    private val currentStageProvider:
        () -> Int
) {

    companion object {

        const val DAMAGE_SOURCE_NAME =
            "Arcane Aura"

        private const val AURA_RANGE =
            170f

        private const val AURA_TICK_INTERVAL =
            0.55f

        private const val BASE_DAMAGE =
            5f

        private const val STAT_SCALING =
            0.62f

        /*
         * Her 5 stage için aura taban hasarı
         * 1 artar.
         */
        private const val STAGE_DAMAGE_INTERVAL =
            5

        private const val STAGE_DAMAGE_INCREASE =
            1f
    }

    private val candidateStats =
        listOf(
            StatType.SHEPHERD,
            StatType.ELEMENTAL_DAMAGE,
            StatType.MIGHT_OF_NATURE
        )

    private var tickTimer =
        0f

    private var selectedMode:
        ArcaneAuraMode? =
        null

    /*
     * Eşitlik değişmediği sürece rastgele
     * seçimin her frame değişmesini önler.
     */
    private var previousFinalCandidates:
        Set<StatType> =
        emptySet()

    val currentMode:
        ArcaneAuraMode?
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
        onEnemyKilled: (Enemy) -> Unit
    ) {
        if (
            delta <= 0f ||
            player.health.isDead
        ) {
            return
        }

        refreshSelectedMode()

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
                    onEnemyKilled
            )
        }
    }

    fun reset() {
        tickTimer = 0f

        selectedMode =
            null

        previousFinalCandidates =
            emptySet()
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

        /*
         * Önce dış aura alanı.
         */
        shapeRenderer.color =
            outerAuraColor(
                mode
            )

        shapeRenderer.circle(
            player.centerX,
            player.centerY,
            AURA_RANGE,
            72
        )

        /*
         * Ardından iç kısmı arka plan rengiyle
         * kapatarak kalın bir halka oluşturuyoruz.
         */
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
            AURA_RANGE - 8f,
            72
        )

        /*
         * İkinci ince halka aura modunu daha
         * görünür hâle getirir.
         */
        shapeRenderer.color =
            innerAuraColor(
                mode
            )

        shapeRenderer.circle(
            player.centerX,
            player.centerY,
            AURA_RANGE - 13f,
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
            AURA_RANGE - 16f,
            72
        )
    }

    private fun refreshSelectedMode() {
        val maximumScore =
            candidateStats.maxOf { statType ->
                playerStats.totalScore(
                    statType
                )
            }

        val highestStats =
            candidateStats
                .filter { statType ->
                    playerStats.totalScore(
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
            ArcaneAuraMode.fromStatType(
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
            previousFinalCandidates =
                emptySet()

            return highestStats.first()
        }

        /*
         * Eşit kalan Stat'lar arasında, kuşanılmış
         * silahların primary scaling Stat'ı olanları
         * topluyoruz.
         */
        val weaponPreferredStats =
            equippedWeaponScalingPreferences()
                .intersect(
                    highestStats
                )

        val finalCandidates =
            if (
                weaponPreferredStats
                    .isNotEmpty()
            ) {
                weaponPreferredStats
            } else {
                highestStats
            }

        val previousStat =
            selectedMode
                ?.statType

        /*
         * Eşitlik grubu aynıysa önceki seçimi
         * koruruz.
         */
        if (
            previousFinalCandidates ==
            finalCandidates &&
            previousStat in
            finalCandidates
        ) {
            return previousStat!!
        }

        previousFinalCandidates =
            finalCandidates

        return finalCandidates
            .toList()
            .random()
    }

    private fun equippedWeaponScalingPreferences():
        Set<StatType> {

        return buildSet {

            repeat(
                WeaponInventory
                    .WEAPON_SLOT_COUNT
            ) { slotIndex ->

                val weapon =
                    weaponInventory.weaponAt(
                        slotIndex
                    )
                        ?: return@repeat

                val primaryScalingStat =
                    weapon.definition
                        .resolvedPrimaryDamageScalingStat
                        ?: return@repeat

                if (
                    primaryScalingStat in
                    candidateStats
                ) {
                    add(
                        primaryScalingStat
                    )
                }
            }
        }
    }

    private fun performAuraTick(
        player: Player,
        enemies: List<Enemy>,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
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
                    onEnemyKilled
            )
        }
    }

    private fun damageEnemy(
        enemy: Enemy,
        mode: ArcaneAuraMode,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
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

        /*
         * DamageType henüz Enemy.takeDamage()
         * tarafından işlenmiyor.
         *
         * mode.damageType şu an görsel ve gelecekteki
         * resistance sistemi için metadata görevi
         * görüyor.
         */
        val appliedDamage =
            enemy.takeDamage(
                requestedDamage
            )

        if (appliedDamage > 0) {
            onDamageDealt(
                appliedDamage,
                DAMAGE_SOURCE_NAME
            )
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
        mode: ArcaneAuraMode
    ): Int {
        val statScore =
            playerStats.totalScore(
                mode.statType
            )

        val stageBonus =
            completedStageDamageIntervals() *
                STAGE_DAMAGE_INCREASE

        return (
            BASE_DAMAGE +
                statScore *
                STAT_SCALING +
                stageBonus
            )
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun completedStageDamageIntervals():
        Int {

        val currentStage =
            currentStageProvider()
                .coerceAtLeast(1)

        /*
         * Stage 1–4   = +0
         * Stage 5–9   = +1
         * Stage 10–14 = +2
         */
        return currentStage /
            STAGE_DAMAGE_INTERVAL
    }

    private fun outerAuraColor(
        mode: ArcaneAuraMode
    ): Color {

        return when (mode) {
            ArcaneAuraMode.SHEPHERD -> {
                Color(
                    0.20f,
                    0.88f,
                    0.28f,
                    0.30f
                )
            }

            ArcaneAuraMode.ELEMENTAL_DAMAGE -> {
                Color(
                    0.20f,
                    0.62f,
                    1f,
                    0.30f
                )
            }

            ArcaneAuraMode.MIGHT_OF_NATURE -> {
                Color(
                    0.58f,
                    0.22f,
                    0.82f,
                    0.30f
                )
            }
        }
    }

    private fun innerAuraColor(
        mode: ArcaneAuraMode
    ): Color {

        return when (mode) {
            ArcaneAuraMode.SHEPHERD -> {
                Color(
                    0.55f,
                    1f,
                    0.42f,
                    1f
                )
            }

            ArcaneAuraMode.ELEMENTAL_DAMAGE -> {
                Color(
                    0.45f,
                    0.84f,
                    1f,
                    1f
                )
            }

            ArcaneAuraMode.MIGHT_OF_NATURE -> {
                Color(
                    0.82f,
                    0.46f,
                    1f,
                    1f
                )
            }
        }
    }
}
