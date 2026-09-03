package com.azizoglu.grisilith.combat.condition.visual

import com.azizoglu.grisilith.content.ConditionType

data class ConditionVisualDefinition(
    val type: ConditionType,
    val visualTexturePath: String,
    val symbolTexturePath: String,
    val frameCount: Int = 4,
    val frameDurationSeconds: Float = 0.14f,
    val worldWidth: Float = 58f,
    val worldHeight: Float = 77f
)
