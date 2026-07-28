package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.content.ContentRegistry
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

/*
 * RunPreset içinde yalnızca Content ID'leri
 * saklanır.
 *
 * Bu sınıf ID'leri gerçek içerik tanımlarına
 * çevirir ve toplam başlangıç Statlarını hesaplar.
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

            totalStats =
                calculateTotalStats(
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
        )
    }

    private fun calculateTotalStats(
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
    ): Map<StatType, Int> {

        val totals =
            preset.statAllocation
                .finalScores(
                    character.baseStats
                )
                .toMutableMap()

        armor.allStatModifiers()
            .forEach { modifier ->
                totals.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        item.statModifiers
            .forEach { modifier ->
                totals.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        weapon.passiveStatModifiers
            .forEach { modifier ->
                totals.addModifier(
                    statType =
                        modifier.statType,

                    amount =
                        modifier.amount
                )
            }

        return StatType.entries
            .associateWith { statType ->
                (
                    totals[
                        statType
                    ] ?: 0
                    )
                    .coerceAtLeast(
                        0
                    )
            }
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

    val totalStats:
    Map<StatType, Int>
)
