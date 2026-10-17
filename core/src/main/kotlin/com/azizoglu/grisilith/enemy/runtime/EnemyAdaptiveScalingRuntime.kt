package com.azizoglu.grisilith.enemy.runtime

import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory

/*
 * Run boyunca Enemy scaling'in oyuncu gücünü okuyabilmesi için
 * hafif, merkezi bir bağlayıcı.
 *
 * Tasarım ilkeleri:
 * - Character'ın başlangıç stat kimliği cezalandırılmaz.
 * - Weapon stat bonusları Player Power hesabına ikinci kez girmez;
 *   Weapon Power kendi katmanında ayrıca hesaplanır.
 * - Main/Secondary offense artışı Enemy dayanıklılığını yükseltir.
 * - Defensive stat artışı Enemy offense'ini yükseltir.
 * - Bütün adaptive katkılar kontrollü ve cap'lidir.
 */
object EnemyAdaptiveScalingRuntime {

    private var playerStats: PlayerStats? = null
    private var weaponInventory: WeaponInventory? = null

    private var baselineScores:
        Map<StatType, Int> =
        emptyMap()

    fun bind(
        playerStats: PlayerStats,
        weaponInventory: WeaponInventory
    ) {
        this.playerStats =
            playerStats

        this.weaponInventory =
            weaponInventory

        baselineScores =
            StatType.entries
                .associateWith { statType ->
                    effectiveNonWeaponScore(
                        playerStats = playerStats,
                        statType = statType
                    )
                }
    }

    fun clear() {
        playerStats = null
        weaponInventory = null
        baselineScores = emptyMap()
    }

    fun current(): EnemyAdaptiveScalingSnapshot {
        val stats =
            playerStats
                ?: return EnemyAdaptiveScalingSnapshot.IDENTITY

        val inventory =
            weaponInventory
                ?: return EnemyAdaptiveScalingSnapshot.IDENTITY

        val currentMainType =
            resolveCurrentMainStatType(
                stats
            )

        val mainGrowth =
            normalizedGrowth(
                current =
                    effectiveNonWeaponScore(
                        playerStats = stats,
                        statType = currentMainType
                    ),

                baseline =
                    baselineScores[
                        currentMainType
                    ] ?: 0
            )

        val secondaryTypes =
            if (
                currentMainType ==
                StatType.WISDOM
            ) {
                WISDOM_SECONDARY_STATS
            } else {
                PHYSICAL_SECONDARY_STATS
            }

        val secondaryGrowth =
            normalizedGroupGrowth(
                playerStats = stats,
                statTypes = secondaryTypes
            )

        val defensiveGrowth =
            normalizedGroupGrowth(
                playerStats = stats,
                statTypes = DEFENSIVE_STATS
            )

        val weaponGrowth =
            normalizedWeaponPower(
                inventory
            )

        /*
         * Main offense growth Enemy durability üzerinde daha ağırdır.
         * Secondary offense daha düşük ağırlıkla gelir.
         * Weapon Power ayrıca kontrollü bir tehdit katkısı verir.
         */
        val durabilityExtra =
            mainGrowth *
                MAXIMUM_MAIN_DURABILITY_BONUS +
                secondaryGrowth *
                MAXIMUM_SECONDARY_DURABILITY_BONUS +
                weaponGrowth *
                MAXIMUM_WEAPON_DURABILITY_BONUS

        /*
         * Armor, HP ile aynı oranda büyütülmez.
         * BaseArmor × ArmorClass modeli nedeniyle aynı bonusu iki savunma
         * katmanına tam uygulamak fiziksel effective HP'yi gereğinden fazla
         * büyütürdü.
         */
        val armorExtra =
            durabilityExtra *
                ARMOR_SHARE_OF_DURABILITY_BONUS

        val offenseExtra =
            defensiveGrowth *
                MAXIMUM_DEFENSE_TO_ENEMY_OFFENSE_BONUS +
                weaponGrowth *
                MAXIMUM_WEAPON_TO_ENEMY_OFFENSE_BONUS

        val attackSpeedExtra =
            defensiveGrowth *
                MAXIMUM_DEFENSE_TO_ENEMY_ATTACK_SPEED_BONUS +
                weaponGrowth *
                MAXIMUM_WEAPON_TO_ENEMY_ATTACK_SPEED_BONUS

        return EnemyAdaptiveScalingSnapshot(
            healthMultiplier =
                1f + durabilityExtra,

            armorClassMultiplier =
                1f + armorExtra,

            damageMultiplier =
                1f + offenseExtra,

            attackSpeedMultiplier =
                1f + attackSpeedExtra,

            movementSpeedMultiplier =
                1f,

            mainPowerGrowth =
                mainGrowth,

            secondaryPowerGrowth =
                secondaryGrowth,

            defensivePowerGrowth =
                defensiveGrowth,

            weaponPowerGrowth =
                weaponGrowth
        )
    }

    private fun resolveCurrentMainStatType(
        playerStats: PlayerStats
    ): StatType {
        return MAIN_DAMAGE_STATS
            .maxBy { statType ->
                effectiveNonWeaponScore(
                    playerStats = playerStats,
                    statType = statType
                )
            }
    }

    private fun normalizedGroupGrowth(
        playerStats: PlayerStats,
        statTypes: List<StatType>
    ): Float {
        var currentGrowth =
            0f

        var availableGrowth =
            0f

        statTypes.forEach { statType ->
            val baseline =
                baselineScores[
                    statType
                ] ?: 0

            val current =
                effectiveNonWeaponScore(
                    playerStats = playerStats,
                    statType = statType
                )

            currentGrowth +=
                (current - baseline)
                    .coerceAtLeast(0)
                    .toFloat()

            availableGrowth +=
                (PlayerStats.RUN_MAXIMUM_SCORE - baseline)
                    .coerceAtLeast(0)
                    .toFloat()
        }

        if (
            availableGrowth <=
            0f
        ) {
            return 0f
        }

        return (
            currentGrowth /
                availableGrowth
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun normalizedGrowth(
        current: Int,
        baseline: Int
    ): Float {
        val availableGrowth =
            (
                PlayerStats.RUN_MAXIMUM_SCORE -
                    baseline
                )
                .coerceAtLeast(0)

        if (
            availableGrowth <=
            0
        ) {
            return 0f
        }

        return (
            (current - baseline)
                .coerceAtLeast(0)
                .toFloat() /
                availableGrowth.toFloat()
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun normalizedWeaponPower(
        inventory: WeaponInventory
    ): Float {
        val totalPower =
            inventory
                .allWeapons()
                .sumOf { weapon ->
                    weaponPowerForLevel(
                        weapon.level
                    )
                        .toDouble()
                }
                .toFloat()

        /*
         * İlk Lv1 Weapon ücretsiz baseline kabul edilir.
         * Altı adet Lv5 Weapon teorik maksimum referanstır.
         */
        val scalablePower =
            (totalPower - FREE_WEAPON_POWER)
                .coerceAtLeast(0f)

        val scalableMaximum =
            MAXIMUM_REFERENCE_WEAPON_POWER -
                FREE_WEAPON_POWER

        if (
            scalableMaximum <=
            0f
        ) {
            return 0f
        }

        return (
            scalablePower /
                scalableMaximum
            )
            .coerceIn(
                0f,
                1f
            )
    }

    private fun weaponPowerForLevel(
        level: Int
    ): Float {
        return when (
            level.coerceIn(
                1,
                5
            )
        ) {
            1 -> 1.5f
            2 -> 2.5f
            3 -> 4.0f
            4 -> 5.0f
            else -> 6.5f
        }
    }

    private fun effectiveNonWeaponScore(
        playerStats: PlayerStats,
        statType: StatType
    ): Int {
        return (
            playerStats
                .totalScore(
                    statType
                ) -
                playerStats
                    .weaponBonus(
                        statType
                    )
            )
            .coerceIn(
                PlayerStats.MINIMUM_SCORE,
                PlayerStats.RUN_MAXIMUM_SCORE
            )
    }

    private val MAIN_DAMAGE_STATS =
        listOf(
            StatType.STRENGTH,
            StatType.DEXTERITY,
            StatType.WISDOM
        )

    private val PHYSICAL_SECONDARY_STATS =
        listOf(
            StatType.ATTACK_SPEED,
            StatType.CRIT_RATE,
            StatType.CRIT_DAMAGE,
            StatType.PIERCING
        )

    private val WISDOM_SECONDARY_STATS =
        listOf(
            StatType.HASTE,
            StatType.ELEMENTAL_DAMAGE,
            StatType.MIGHT_OF_NATURE,
            StatType.SHEPHERD
        )

    private val DEFENSIVE_STATS =
        listOf(
            StatType.MAX_HP,
            StatType.ARMOR_CLASS,
            StatType.LIFE_STEAL,
            StatType.HP_REGENERATION
        )

    private const val MAXIMUM_MAIN_DURABILITY_BONUS =
        // All maximum additive contributions are reduced by 12.5%, not percentage points.
        0.35f

    private const val MAXIMUM_SECONDARY_DURABILITY_BONUS =
        0.175f

    private const val MAXIMUM_WEAPON_DURABILITY_BONUS =
        0.21875f

    private const val ARMOR_SHARE_OF_DURABILITY_BONUS =
        0.35f

    private const val MAXIMUM_DEFENSE_TO_ENEMY_OFFENSE_BONUS =
        0.2625f

    private const val MAXIMUM_WEAPON_TO_ENEMY_OFFENSE_BONUS =
        0.0875f

    private const val MAXIMUM_DEFENSE_TO_ENEMY_ATTACK_SPEED_BONUS =
        0.04375f

    private const val MAXIMUM_WEAPON_TO_ENEMY_ATTACK_SPEED_BONUS =
        0.021875f

    private const val FREE_WEAPON_POWER =
        1.5f

    private const val MAXIMUM_REFERENCE_WEAPON_POWER =
        39f
}


data class EnemyAdaptiveScalingSnapshot(
    val healthMultiplier: Float,
    val armorClassMultiplier: Float,
    val damageMultiplier: Float,
    val attackSpeedMultiplier: Float,
    val movementSpeedMultiplier: Float,
    val mainPowerGrowth: Float,
    val secondaryPowerGrowth: Float,
    val defensivePowerGrowth: Float,
    val weaponPowerGrowth: Float
) {
    companion object {
        val IDENTITY =
            EnemyAdaptiveScalingSnapshot(
                healthMultiplier = 1f,
                armorClassMultiplier = 1f,
                damageMultiplier = 1f,
                attackSpeedMultiplier = 1f,
                movementSpeedMultiplier = 1f,
                mainPowerGrowth = 0f,
                secondaryPowerGrowth = 0f,
                defensivePowerGrowth = 0f,
                weaponPowerGrowth = 0f
            )
    }
}
