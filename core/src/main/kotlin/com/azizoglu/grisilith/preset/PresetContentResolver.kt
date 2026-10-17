package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.content.ContentRegistry
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.definition.TamgaDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

class PresetContentResolver(
    private val contentRegistry:
    ContentRegistry
) {

    fun resolve(
        preset:
        RunPreset
    ): ResolvedPresetContent? {

        // Legacy saves remain intact, but higher-rarity items are never starting gear.
        val startingItem = preset.itemId?.let(contentRegistry::findById) as? ItemDefinition
        if (startingItem != null && !PresetItemRules.canStartWith(startingItem)) {
            return resolve(preset.copy(itemId = null))
        }

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
            preset.armorId
                ?.let { armorId ->
                    contentRegistry.findById(
                        armorId
                    ) as? ArmorDefinition
                        ?: return null
                }

        if (!character.canEquipArmor && (armor != null || preset.armorTamgaId != null)) return null

        val item =
            preset.itemId
                ?.let { itemId ->
                    contentRegistry.findById(
                        itemId
                    ) as? ItemDefinition
                        ?: return null
                }

        val weaponTamga =
            preset.weaponTamgaId
                ?.let { tamgaId ->
                    contentRegistry.findById(
                        tamgaId
                    ) as? TamgaDefinition
                        ?: return null
                }

        val armorTamga =
            preset.armorTamgaId
                ?.let { tamgaId ->
                    contentRegistry.findById(
                        tamgaId
                    ) as? TamgaDefinition
                        ?: return null
                }

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
                    item,

                weaponTamga =
                    weaponTamga,

                armorTamga =
                    armorTamga
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

            weaponTamga =
                weaponTamga,

            armorTamga =
                armorTamga,

            baseStats =
                statBreakdown.baseStats,

            presetStatBonuses =
                statBreakdown.presetStatBonuses,

            armorAndTamgaBonuses =
                statBreakdown.armorAndTamgaBonuses,

            weaponBonuses =
                statBreakdown.weaponBonuses,

            itemBonuses =
                statBreakdown.itemBonuses,

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
        ArmorDefinition?,

        item:
        ItemDefinition?,

        weaponTamga:
        TamgaDefinition?,

        armorTamga:
        TamgaDefinition?
    ): PresetStatBreakdown {

        val baseStats =
            StatType.entries
                .associateWith { statType ->
                    character.baseStats[
                        statType
                    ] ?: 0
                }

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

        val armorAndTamgaBonuses =
            emptyStatMap()

        armor
            ?.allStatModifiers()
            ?.forEach { modifier ->
                armorAndTamgaBonuses.addModifier(
                    modifier.statType,
                    modifier.amount
                )
            }

        weaponTamga
            ?.statModifiers
            ?.forEach { modifier ->
                armorAndTamgaBonuses.addModifier(
                    modifier.statType,
                    modifier.amount
                )
            }

        armorTamga
            ?.statModifiers
            ?.forEach { modifier ->
                armorAndTamgaBonuses.addModifier(
                    modifier.statType,
                    modifier.amount
                )
            }

        val weaponBonuses =
            emptyStatMap()

        weapon.passiveStatModifiers
            .forEach { modifier ->
                weaponBonuses.addModifier(
                    modifier.statType,
                    modifier.amount
                )
            }

        val itemBonuses =
            emptyStatMap()

        item
            ?.statModifiers
            ?.forEach { modifier ->
                itemBonuses.addModifier(
                    modifier.statType,
                    modifier.amount
                )
            }

        val gearBonuses =
            StatType.entries
                .associateWith { statType ->
                    (
                        armorAndTamgaBonuses[
                            statType
                        ] ?: 0
                        ) +
                        (
                            weaponBonuses[
                                statType
                            ] ?: 0
                            ) +
                        (
                            itemBonuses[
                                statType
                            ] ?: 0
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

            armorAndTamgaBonuses =
                armorAndTamgaBonuses,

            weaponBonuses =
                weaponBonuses,

            itemBonuses =
                itemBonuses,

            gearBonuses =
                gearBonuses,

            totalStats =
                totalStats
        )
    }

    private fun emptyStatMap():
        MutableMap<StatType, Int> {

        return StatType.entries
            .associateWith {
                0
            }
            .toMutableMap()
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

    val armorAndTamgaBonuses:
    Map<StatType, Int>,

    val weaponBonuses:
    Map<StatType, Int>,

    val itemBonuses:
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
    ArmorDefinition?,

    val item:
    ItemDefinition?,

    val weaponTamga:
    TamgaDefinition?,

    val armorTamga:
    TamgaDefinition?,

    val baseStats:
    Map<StatType, Int>,

    val presetStatBonuses:
    Map<StatType, Int>,

    val armorAndTamgaBonuses:
    Map<StatType, Int>,

    val weaponBonuses:
    Map<StatType, Int>,

    val itemBonuses:
    Map<StatType, Int>,

    val gearBonuses:
    Map<StatType, Int>,

    val totalStats:
    Map<StatType, Int>
) {

    val preRunBaseStats:
        Map<StatType, Int>
        get() =
            StatType.entries
                .associateWith { statType ->
                    (
                        baseStats[
                            statType
                        ] ?: 0
                        ) +
                        (
                            presetStatBonuses[
                                statType
                            ] ?: 0
                            )
                }
}
