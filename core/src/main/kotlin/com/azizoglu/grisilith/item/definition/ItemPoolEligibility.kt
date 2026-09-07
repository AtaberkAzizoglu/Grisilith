package com.azizoglu.grisilith.item.definition

object ItemPoolEligibility {

    private val boneTotemListEffects =
        setOf(
            ItemEffectId.BONE_TOTEM_HEALING_AURA,
            ItemEffectId.BONE_TOTEM_PROTECTERS_AREA,
            ItemEffectId.BONE_TOTEM_BURRIED_TREASURE,
            ItemEffectId.BONE_TOTEM_HOURGLASS,
            ItemEffectId.BONE_TOTEM_TOADS
        )

    fun isEligible(
        item: ItemDefinition,
        ownedItemIds: Set<String>
    ): Boolean {
        return item.effectId !in boneTotemListEffects ||
            DefaultItems.boneTotems.id.value in ownedItemIds
    }
}
