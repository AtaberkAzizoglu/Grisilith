package com.azizoglu.grisilith.preset

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.content.ContentRegistry
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

object PresetValidator {

    fun validate(
        preset:
        RunPreset,

        contentRegistry:
        ContentRegistry
    ): PresetValidationResult {
        if (
            preset.presetName
                .isBlank()
        ) {
            return invalid(
                "Preset name cannot be empty."
            )
        }

        if (
            preset.presetName.length >
            PresetConstants
                .MAXIMUM_PRESET_NAME_LENGTH
        ) {
            return invalid(
                "Preset name cannot be longer than " +
                    "${PresetConstants.MAXIMUM_PRESET_NAME_LENGTH} characters."
            )
        }

        val character =
            contentRegistry.findById(
                preset.characterId
            ) as?
                CharacterDefinition
                ?: return invalid(
                    "The selected Character does not exist."
                )

        val weapon =
            contentRegistry.findById(
                preset.weaponId
            ) as?
                WeaponDefinition
                ?: return invalid(
                    "The selected Weapon does not exist."
                )

        val armor =
            contentRegistry.findById(
                preset.armorId
            ) as?
                ArmorDefinition
                ?: return invalid(
                    "The selected Armor does not exist."
                )

        val item =
            contentRegistry.findById(
                preset.itemId
            ) as?
                ItemDefinition
                ?: return invalid(
                    "The selected Item does not exist."
                )

        /*
         * Değişkenler bilinçli olarak resolve edilir.
         * Böylece yanlış kategorideki bir Content ID
         * Preset'e kaydedilemez.
         */
        @Suppress("UNUSED_VARIABLE")
        val resolvedEquipment =
            listOf(
                weapon,
                armor,
                item
            )

        if (
            preset.statPointBudget <
            PresetConstants
                .DEFAULT_STAT_POINT_BUDGET
        ) {
            return invalid(
                "The Preset Stat Point budget is invalid."
            )
        }

        if (
            preset.statAllocation
                .spentPointCount >
            preset.statPointBudget
        ) {
            return invalid(
                "The Preset spends more Stat Points than available."
            )
        }

        for (
        statType in
        StatType.entries
        ) {
            val bonus =
                preset.statAllocation
                    .bonusFor(
                        statType
                    )

            if (bonus < 0) {
                return invalid(
                    "${statType.displayName} has a negative Preset bonus."
                )
            }

            val characterBase =
                character.baseStats[
                    statType
                ] ?: 0

            val finalPreRunScore =
                characterBase +
                    bonus

            if (
                finalPreRunScore >
                PresetConstants
                    .MAXIMUM_PRE_RUN_STAT_SCORE
            ) {
                return invalid(
                    "${statType.displayName} cannot exceed " +
                        "${PresetConstants.MAXIMUM_PRE_RUN_STAT_SCORE} " +
                        "before the run starts."
                )
            }
        }

        /*
         * Tamga sistemi hazır olduğunda burada:
         *
         * - Weapon Tamga ID doğrulaması
         * - Armor Tamga ID doğrulaması
         * - Weapon/Tamga uyumluluğu
         * - Armor/Tamga uyumluluğu
         *
         * eklenecek.
         */

        return PresetValidationResult.Valid
    }

    private fun invalid(
        message:
        String
    ): PresetValidationResult.Invalid {
        return PresetValidationResult
            .Invalid(
                message =
                    message
            )
    }
}
