package com.azizoglu.grisilith.tamga.definition

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition

object TamgaCompatibility {

    fun check(
        tamga:
        TamgaDefinition,

        targetSlot:
        TamgaPresetSlot,

        selectedWeapon:
        WeaponDefinition?,

        selectedArmor:
        ArmorDefinition?
    ): TamgaCompatibilityResult {

        val slotResult =
            checkSlot(
                tamga =
                    tamga,

                targetSlot =
                    targetSlot
            )

        if (
            slotResult is
                TamgaCompatibilityResult.Incompatible
        ) {
            return slotResult
        }

        val weaponResult =
            checkWeaponRequirements(
                tamga =
                    tamga,

                selectedWeapon =
                    selectedWeapon
            )

        if (
            weaponResult is
                TamgaCompatibilityResult.Incompatible
        ) {
            return weaponResult
        }

        val armorResult =
            checkArmorRequirements(
                tamga =
                    tamga,

                selectedArmor =
                    selectedArmor
            )

        if (
            armorResult is
                TamgaCompatibilityResult.Incompatible
        ) {
            return armorResult
        }

        return TamgaCompatibilityResult.Compatible
    }

    fun isCompatible(
        tamga:
        TamgaDefinition,

        targetSlot:
        TamgaPresetSlot,

        selectedWeapon:
        WeaponDefinition?,

        selectedArmor:
        ArmorDefinition?
    ): Boolean {

        return check(
            tamga =
                tamga,

            targetSlot =
                targetSlot,

            selectedWeapon =
                selectedWeapon,

            selectedArmor =
                selectedArmor
        ) == TamgaCompatibilityResult.Compatible
    }

    private fun checkSlot(
        tamga:
        TamgaDefinition,

        targetSlot:
        TamgaPresetSlot
    ): TamgaCompatibilityResult {

        return when (targetSlot) {
            TamgaPresetSlot
                .WEAPON_TAMGA_SLOT -> {

                if (
                    tamga.canUseWeaponSlot
                ) {
                    TamgaCompatibilityResult
                        .Compatible
                } else {
                    TamgaCompatibilityResult
                        .Incompatible(
                            "This Tamga cannot be placed in the Weapon Tamga slot."
                        )
                }
            }

            TamgaPresetSlot
                .ARMOR_TAMGA_SLOT -> {

                if (
                    tamga.canUseArmorSlot
                ) {
                    TamgaCompatibilityResult
                        .Compatible
                } else {
                    TamgaCompatibilityResult
                        .Incompatible(
                            "This Tamga cannot be placed in the Armor Tamga slot."
                        )
                }
            }
        }
    }

    private fun checkWeaponRequirements(
        tamga:
        TamgaDefinition,

        selectedWeapon:
        WeaponDefinition?
    ): TamgaCompatibilityResult {

        if (
            tamga.requiredWeaponTags
                .isEmpty()
        ) {
            return TamgaCompatibilityResult
                .Compatible
        }

        if (
            selectedWeapon == null
        ) {
            return TamgaCompatibilityResult
                .Incompatible(
                    "Choose a compatible Weapon before equipping this Tamga."
                )
        }

        /*
         * Birden fazla required tag varsa silahın
         * bunların tamamına sahip olması gerekir.
         */
        val missingTags =
            tamga.requiredWeaponTags
                .filterNot { requiredTag ->
                    requiredTag in
                        selectedWeapon.tags
                }

        if (
            missingTags.isNotEmpty()
        ) {
            return TamgaCompatibilityResult
                .Incompatible(
                    "The selected Weapon is not compatible with ${tamga.displayName}."
                )
        }

        return TamgaCompatibilityResult
            .Compatible
    }

    private fun checkArmorRequirements(
        tamga:
        TamgaDefinition,

        selectedArmor:
        ArmorDefinition?
    ): TamgaCompatibilityResult {

        if (
            tamga.requiredArmorTags
                .isEmpty()
        ) {
            return TamgaCompatibilityResult
                .Compatible
        }

        if (
            selectedArmor == null
        ) {
            return TamgaCompatibilityResult
                .Incompatible(
                    "Choose a compatible Armor before equipping this Tamga."
                )
        }

        val missingTags =
            tamga.requiredArmorTags
                .filterNot { requiredTag ->
                    requiredTag in
                        selectedArmor.tags
                }

        if (
            missingTags.isNotEmpty()
        ) {
            return TamgaCompatibilityResult
                .Incompatible(
                    "The selected Armor is not compatible with ${tamga.displayName}."
                )
        }

        return TamgaCompatibilityResult
            .Compatible
    }
}

sealed class TamgaCompatibilityResult {

    data object Compatible :
        TamgaCompatibilityResult()

    data class Incompatible(
        val message:
        String
    ) : TamgaCompatibilityResult()
}
