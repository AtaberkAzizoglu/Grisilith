package com.azizoglu.grisilith.tamga.definition

import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.ContentUnlockState
import com.azizoglu.grisilith.statistics.StatType

object DefaultTamgas {

    val charger =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_CHARGER"
                ),

            displayName =
                "Charger",

            description =
                "Build up speed, then explode around you. Charging longer makes the blast stronger and larger; stopping or hitting an obstacle ends it early. Turning is harder while charging.",

            slotType =
                TamgaSlotType.WEAPON,

            activationType =
                TamgaActivationType.ACTIVE,

            effectId =
                TamgaEffectId.CHARGER,

            interactableStats =
                setOf(
                    StatType.MOVEMENT_SPEED,
                    StatType.ELEMENTAL_DAMAGE
                ),

            additionalTags =
                setOf(
                    ContentTag.SPECIAL,
                    ContentTag.GLYPH_MOTION,
                    ContentTag.FIRE_DAMAGE
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val heightInsecurity =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_HEIGHT_INSECURITY"
                ),

            displayName =
                "Height Insecurity",

            description =
                "Deal extra damage to Unique enemies, Mini Bosses and Final Bosses. The bonus grows as you advance through stages.",

            slotType =
                TamgaSlotType.GENERAL,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.HEIGHT_INSECURITY,

            interactableStats =
                emptySet(),

            additionalTags =
                setOf(
                    ContentTag.SPECIAL
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val phantomSteed =

        TamgaDefinition(
            id =
                ContentId(
                    "TMG_MOUNTED_COMBATANT"
                ),

            displayName =
                "Phantom Steed",

            description =
                "Ride a spectral mount for a short time. Gain weapon range, Max HP and Movement Speed at the cost of Harvest. The bonuses grow with stage progress.",

            slotType =
                TamgaSlotType.ARMOR,

            activationType =
                TamgaActivationType.ACTIVE,

            effectId =
                TamgaEffectId.PHANTOM_STEED,

            interactableStats =
                setOf(
                    StatType.MAX_HP,
                    StatType.MOVEMENT_SPEED,
                    StatType.HARVEST
                ),

            additionalTags =
                setOf(
                    ContentTag.SPECIAL,
                    ContentTag.GLYPH_MOTION
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val holyLights =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_HOLY_LIGHTS"
                ),

            displayName =
                "Holy Lights",

            description =
                "A constant aura adapts to your highest Armor Class, Wisdom or Life Steal. It strengthens that stat and damages nearby enemies; the Life Steal version can also restore health.",

            slotType =
                TamgaSlotType.ARMOR,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.HOLY_LIGHTS,

            interactableStats =
                setOf(
                    StatType.ARMOR_CLASS,
                    StatType.WISDOM,
                    StatType.LIFE_STEAL
                ),

            additionalTags =
                setOf(
                    ContentTag.AURA,
                    ContentTag.SPECIAL
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val arcaneAura =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_ARCANE_AURA"
                ),

            displayName =
                "Arcane Aura",

            description =
                "A constant aura damages nearby enemies. Its appearance and strength follow your highest Shepherd, Elemental Damage or Might of Nature, and it grows stronger with stage progress.",

            slotType =
                TamgaSlotType.ARMOR,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.ARCANE_AURA,

            interactableStats =
                emptySet(),

            additionalTags =
                setOf(
                    ContentTag.AURA,
                    ContentTag.SPECIAL,
                    ContentTag.WATER_DAMAGE,
                    ContentTag.NECROTIC_DAMAGE
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val flameBlades =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_FLAME_BLADES"
                ),

            displayName =
                "Flame Blades",

            description =
                "Your first-slot melee weapon burns enemies. Any equipped weapon deals extra damage when hitting a Burned target, scaling with Elemental Damage.",

            slotType =
                TamgaSlotType.WEAPON,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.FLAME_BLADES,

            interactableStats =
                setOf(
                    StatType.ELEMENTAL_DAMAGE
                ),

            requiredWeaponTags =
                setOf(
                    ContentTag.MELEE_WEAPON
                ),

            additionalTags =
                setOf(
                    ContentTag.SPECIAL,
                    ContentTag.FIRE_DAMAGE
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val quickShot =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_QUICK_SHOT"
                ),

            displayName =
                "Quick Shot",

            description =
                "Repeated attacks with your first-slot ranged weapon build Attack Speed. Keep firing to maintain the stacks; later stages allow more stacks.",

            slotType =
                TamgaSlotType.WEAPON,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.QUICK_SHOT,

            interactableStats =
                setOf(
                    StatType.ATTACK_SPEED
                ),

            requiredWeaponTags =
                setOf(
                    ContentTag.RANGED_WEAPON
                ),

            additionalTags =
                setOf(
                    ContentTag.SPECIAL
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val panic =
        TamgaDefinition(
            id =
                ContentId(
                    "TMG_PANIC"
                ),

            displayName =
                "Panic",

            description =
                "When a crowd gets too close, automatically push it away with a knockback pulse. Creates breathing room, but deals no damage.",

            slotType =
                TamgaSlotType.GENERAL,

            activationType =
                TamgaActivationType.PASSIVE,

            effectId =
                TamgaEffectId.PANIC,

            additionalTags =
                setOf(
                    ContentTag.SPECIAL
                ),

            initialUnlockState =
                ContentUnlockState.UNLOCKED
        )

    val all:
        List<TamgaDefinition> =
        listOf(
            charger,
            heightInsecurity,
            phantomSteed,
            holyLights,
            arcaneAura,
            flameBlades,
            quickShot,
            panic
        )
}
