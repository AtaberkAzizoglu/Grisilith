package com.azizoglu.grisilith.tamga.runtime

import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import kotlin.random.Random

object TamgaStatTargetSelector {

    private val holyLightsStats =
        listOf(
            StatType.ARMOR_CLASS,
            StatType.WISDOM,
            StatType.LIFE_STEAL
        )

    private val arcaneAuraStats =
        listOf(
            StatType.SHEPHERD,
            StatType.ELEMENTAL_DAMAGE,
            StatType.MIGHT_OF_NATURE
        )

    /*
     * HOLY LIGHTS
     *
     * 1. Üç Stat arasındaki en yüksek değer bulunur.
     * 2. Eşit adaylar Character tercihlerinde aranır.
     * 3. Tercihlerde aday varsa onlardan biri seçilir.
     * 4. Tercih edilen aday yoksa eşit adaylardan
     *    rastgele biri seçilir.
     */
    fun selectHolyLightsStat(
        totalStats:
        Map<StatType, Int>,

        character:
        CharacterDefinition,

        random:
        Random =
            Random.Default
    ): StatType {

        val highestCandidates =
            highestStats(
                candidates =
                    holyLightsStats,

                totalStats =
                    totalStats
            )

        val preferredCandidates =
            highestCandidates
                .filter { statType ->
                    statType in
                        character
                            .preferredMarketStats
                }

        return if (
            preferredCandidates.isNotEmpty()
        ) {
            preferredCandidates.random(
                random
            )
        } else {
            highestCandidates.random(
                random
            )
        }
    }

    /*
     * ARCANE AURA
     *
     * 1. Üç Stat arasındaki en yüksek değer bulunur.
     * 2. Tek aday varsa doğrudan seçilir.
     * 3. Eşitlik varsa oyuncunun sahip olduğu
     *    Weapon'ların damageScaling değerleri incelenir.
     * 4. Eşit adaylardan Weapon'larda en fazla
     *    kullanılan Stat seçilir.
     * 5. Kullanım sayıları da eşitse rastgele seçilir.
     */
    fun selectArcaneAuraStat(
        totalStats:
        Map<StatType, Int>,

        equippedWeapons:
        Iterable<WeaponDefinition>,

        random:
        Random =
            Random.Default
    ): StatType {

        val highestCandidates =
            highestStats(
                candidates =
                    arcaneAuraStats,

                totalStats =
                    totalStats
            )

        if (
            highestCandidates.size ==
            1
        ) {
            return highestCandidates.first()
        }

        val weaponUsageCounts =
            highestCandidates
                .associateWith { statType ->
                    equippedWeapons.count { weapon ->
                        statType in
                            weapon.damageScaling
                    }
                }

        val highestUsageCount =
            weaponUsageCounts
                .values
                .maxOrNull()
                ?: 0

        val mostUsedCandidates =
            highestCandidates
                .filter { statType ->
                    weaponUsageCounts[
                        statType
                    ] == highestUsageCount
                }

        return mostUsedCandidates.random(
            random
        )
    }

    private fun highestStats(
        candidates:
        List<StatType>,

        totalStats:
        Map<StatType, Int>
    ): List<StatType> {

        require(
            candidates.isNotEmpty()
        ) {
            "At least one candidate Stat is required."
        }

        val highestValue =
            candidates
                .maxOf { statType ->
                    totalStats[
                        statType
                    ] ?: 0
                }

        return candidates
            .filter { statType ->
                (
                    totalStats[
                        statType
                    ] ?: 0
                    ) ==
                    highestValue
            }
    }
}
