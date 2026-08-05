package com.azizoglu.grisilith.weapon.glyph

data class GlyphRecognitionResult(
    val glyphType: GlyphType?,
    val confidence: Float,
    val templateScore: Float,
    val structuralScore: Float,
    val accepted: Boolean,
    val reason: String
) {

    val confidencePercent: Int
        get() =
            (
                confidence
                    .coerceIn(
                        0f,
                        1f
                    ) *
                    100f
                )
                .toInt()
}
