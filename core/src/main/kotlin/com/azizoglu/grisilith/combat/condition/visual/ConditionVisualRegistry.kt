package com.azizoglu.grisilith.combat.condition.visual

import com.azizoglu.grisilith.content.ConditionType

object ConditionVisualRegistry {
    private fun d(type: ConditionType, id: String) =
        ConditionVisualDefinition(
            type = type,
            visualTexturePath = "condition/visuals/$id.png",
            symbolTexturePath = "condition/symbols/$id.png"
        )

    private val definitions = mapOf(
        ConditionType.BURNED to d(ConditionType.BURNED, "burned"),
        ConditionType.WET to d(ConditionType.WET, "wet"),
        ConditionType.CORRODING to d(ConditionType.CORRODING, "corroding"),
        ConditionType.POISONED to d(ConditionType.POISONED, "poisoned"),
        ConditionType.NECROSIS to d(ConditionType.NECROSIS, "necrosis"),
        ConditionType.ROOTED to d(ConditionType.ROOTED, "rooted"),
        ConditionType.PARALYZED to d(ConditionType.PARALYZED, "paralyzed"),
        ConditionType.XXX_CURSE to d(ConditionType.XXX_CURSE, "xxx_curse"),
        ConditionType.FRIGHTENED to d(ConditionType.FRIGHTENED, "frightened"),
        ConditionType.STUNNED to d(ConditionType.STUNNED, "stunned")
        // SLOWED intentionally has no visual yet.
    )

    fun definitionFor(type: ConditionType): ConditionVisualDefinition? = definitions[type]
}
