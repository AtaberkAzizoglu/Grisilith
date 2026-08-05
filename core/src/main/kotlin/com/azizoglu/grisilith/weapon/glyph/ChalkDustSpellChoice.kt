package com.azizoglu.grisilith.weapon.glyph

enum class ChalkDustSpellChoice(
    val displayName: String,
    val shapeName: String,
    val description: String,
    val glyphType: GlyphType
) {

    CALL_LIGHTNING(
        displayName = "CALL LIGHTNING",
        shapeName = "Z Shape",
        description = "Draw an open Z to call a two-ring Lightning strike.",
        glyphType = GlyphType.CALL_LIGHTNING
    ),

    TSUNAMI(
        displayName = "TSUNAMI",
        shapeName = "Square / Rectangle",
        description = "Draw a closed square or rectangle to send a Water wave.",
        glyphType = GlyphType.TSUNAMI
    ),

    METEOR(
        displayName = "METEOR",
        shapeName = "Triangle",
        description = "Draw a closed triangle to call a powerful Fire meteor.",
        glyphType = GlyphType.METEOR
    ),

    SINGULARITY(
        displayName = "SINGULARITY",
        shapeName = "Circle / Oval",
        description = "Draw a closed circle or oval to create a Necrotic singularity.",
        glyphType = GlyphType.SINGULARITY
    );

    companion object {
        fun fromSavedName(savedName: String?): ChalkDustSpellChoice? {
            val normalized = savedName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            return entries.firstOrNull { it.name == normalized }
        }
    }
}
