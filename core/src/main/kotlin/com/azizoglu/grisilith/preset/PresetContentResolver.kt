package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.content.ContentRegistry
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

/*
 * Preset içindeki Content ID'lerini gerçek
 * tanımlara dönüştürür.
 *
 * Stat sonuçlarını dört ayrı katmanda verir:
 *
 * - Character base Stat
 * - Preset Stat Point bonusu
 * - Gear bonusu
 * - Toplam başlangıç Statı
 */
class PresetContentResolver(
    private val contentRegistry:
    ContentRegistry
) {

    fun resolve(
        preset:
        RunPreset
    ): ResolvedPresetContent? {

        val character =
            contentRegistry.findById(
                preset.characterId
            ) as? CharacterDefinition
                ?: return null

        val weapon =
            contentRegistry.findById(
                preset.weaponId
            ) as? WeaponDefinition
                ?: return null

        val armor =
            contentRegistry.findById(
                preset.armorId
            ) as? ArmorDefinition
                ?: return null

        val item =
            contentRegistry.findById(
                preset.itemId
            ) as? ItemDefinition
                ?: return null

        val statBreakdown =
            calculateStatBreakdown(
                preset =
                    preset,

                character =
                    character,

                weapon =
                    weapon,

                armor =
                    armor,

                item =
                    item
            )

        return ResolvedPresetContent(
            preset =
                preset,

            character =
                character,

            weapon =
                weapon,

            armor =
                armor,

            item =
                item,

            baseStats =
                statBreakdown.baseStats,

            presetStatBonuses =
                statBreakdown.presetStatBonuses,

            gearBonuses =
                statBreakdown.gearBonuses,

            totalStats =
                statBreakdown.totalStats
        )
    }

    private fun calculateStatBreakdown(
        preset:
        RunPreset,

        character:
        CharacterDefinition,

        weapon:
        WeaponDefinition,

        armor:
        ArmorDefinition,

        item:
        ItemDefinition
    ): PresetStatBreakdown {

        val baseStats =
            StatType.entries
                .associateWith { statType ->
                    character.baseStats[
                        statType
                    ] ?: 0
                }

        /*
         * PresetStatAllocation.finalScores()
         * Character base değerini ve maksimum 25
         * sınırını birlikte uygular.
         */
        val preRunStats =
            preset.statAllocation
                .finalScores(
                    character.baseStats
                )

        val presetStatBonuses =
            StatType.entries
                .associateWith { statType ->
                    val base =
                        baseStats[
                            statType
                        ] ?: 0

                    val preRun =
                        preRunStats[
                            statType
                        ] ?: base

                    (
                        preRun -
                            base
                        )
                        .coerceAtLeast(
                            0
                        )
                }

        val gearBonuses =
            StatType.entries
                .associateWith {
                    0
                }
                .toMutableMap()

        armor.allStatModifiers()
            .forEach { modifier ->
                gearBonuses.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        item.statModifiers
            .forEach { modifier ->
                gearBonuses.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        weapon.passiveStatModifiers
            .forEach { modifier ->
                gearBonuses.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        val totalStats =
            StatType.entries
                .associateWith { statType ->
                    val base =
                        baseStats[
                            statType
                        ] ?: 0

                    val presetBonus =
                        presetStatBonuses[
                            statType
                        ] ?: 0

                    val gearBonus =
                        gearBonuses[
                            statType
                        ] ?: 0

                    (
                        base +
                            presetBonus +
                            gearBonus
                        )
                        .coerceAtLeast(
                            0
                        )
                }

        return PresetStatBreakdown(
            baseStats =
                baseStats,

            presetStatBonuses =
                presetStatBonuses,

            gearBonuses =
                gearBonuses,

            totalStats =
                totalStats
        )
    }

    private fun MutableMap<StatType, Int>
        .addModifier(
        statType:
        StatType,

        amount:
        Int
    ) {

        this[
            statType
        ] =
            (
                this[
                    statType
                ] ?: 0
                ) +
                amount
    }
}

private data class PresetStatBreakdown(
    val baseStats:
    Map<StatType, Int>,

    val presetStatBonuses:
    Map<StatType, Int>,

    val gearBonuses:
    Map<StatType, Int>,

    val totalStats:
    Map<StatType, Int>
)

data class ResolvedPresetContent(
    val preset:
    RunPreset,

    val character:
    CharacterDefinition,

    val weapon:
    WeaponDefinition,

    val armor:
    ArmorDefinition,

    val item:
    ItemDefinition,

    val baseStats:
    Map<StatType, Int>,

    val presetStatBonuses:
    Map<StatType, Int>,

    val gearBonuses:
    Map<StatType, Int>,

    val totalStats:
    Map<StatType, Int>
)
