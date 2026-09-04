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
                "Activate to gradually increase Movement Speed while building up " +
                    "an explosive charge. Stopping or colliding with an obstacle " +
                    "causes an early explosion whose damage and radius scale with " +
                    "the time spent charging. Completing the full charge produces " +
                    "the largest explosion and grants additional damage and radius. " +
                    "Sharp turns become harder while charging.",

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
                "Deal additional damage to creatures larger than the " +
                    "player, including Unique enemies, Mini Bosses and " +
                    "Final Bosses. The damage bonus begins at 5% and " +
                    "increases by another 5% for every 10 stages reached.",

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
                "Activate to summon a mount for 15 seconds. While mounted, " +
                    "Weapon Range, Max HP and Movement Speed increase, while " +
                    "Harvest decreases. It initially grants +50 px Weapon " +
                    "Range, +2 Max HP, +2 Movement Speed and -5 Harvest. Every " +
                    "10 stages, Range gains another 10 px, Max HP and Movement " +
                    "Speed gain another +1, and Harvest loses another 1 point. " +
                    "This Tamga has a 120-second cooldown.",

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
                "Creates a permanent aura centered on the player. Its " +
                    "behavior is determined by the highest value among " +
                    "Armor Class, Wisdom and Life Steal. Armor Class creates " +
                    "a defensive light that deals low damage and slightly " +
                    "increases Armor Class. Wisdom creates a stronger damaging " +
                    "aura. Life Steal creates a low-damage aura whose hits can " +
                    "trigger Life Steal. The selected Stat gains a stage-based " +
                    "bonus beginning at +1 and increasing every 5 stages.",

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
                "Creates a permanent damaging aura centered on the player. " +
                    "Its damage type and appearance are determined by the " +
                    "highest value among Shepherd, Elemental Damage and " +
                    "Might of Nature. Shepherd produces a dark-green Nature " +
                    "aura, Elemental Damage produces a blue Water aura, and " +
                    "Might of Nature produces a purple Necrotic aura. Damage " +
                    "slowly increases as stages advance.",

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
                "Melee attacks made by the Weapon in the first Weapon slot " +
                    "apply Burned to every enemy they damage. Burned enemies " +
                    "also receive additional Fire damage whenever they are " +
                    "damaged by any equipped Weapon. The additional damage is " +
                    "equal to 5 plus 110% of Elemental Damage.",

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
                "Whenever the ranged Weapon in the first Weapon slot " +
                    "performs an attack, gain +1 Attack Speed. The bonus " +
                    "stacks up to 3 times at first, increases to 4 stacks " +
                    "at Stage 11, and reaches its maximum of 5 stacks at " +
                    "Stage 21. All stacks are lost if the first Weapon slot " +
                    "does not attack for 1 second.",

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

    val all:
        List<TamgaDefinition> =
        listOf(
            charger,
            heightInsecurity,
            phantomSteed,
            holyLights,
            arcaneAura,
            flameBlades,
            quickShot
        )
}
