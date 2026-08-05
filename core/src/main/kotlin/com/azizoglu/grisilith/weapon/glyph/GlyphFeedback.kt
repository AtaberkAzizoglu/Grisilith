package com.azizoglu.grisilith.weapon.glyph

data class GlyphFeedback(
    val detectedName: String,
    val confidencePercent: Int,
    val resultText: String,
    val cooldownSeconds: Int,
    val remainingDisplaySeconds: Float
)
