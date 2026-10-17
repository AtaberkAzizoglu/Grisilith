package com.azizoglu.grisilith.weapon.glyph

object GlyphChoicePresentation {
    fun description(choice: ChalkDustSpellChoice): String {
        val effect = when (choice) {
            ChalkDustSpellChoice.CALL_LIGHTNING -> "Once the ground glyph finishes drawing, a Lightning strike hits the selected location. The inner ring deals the strongest hit; the outer ring is weaker and can chain damage to nearby enemies. Damage scales with Wisdom + Might of Nature. Level 5 increases its damage."
            ChalkDustSpellChoice.METEOR -> "Once the ground glyph finishes drawing, a Fire meteor falls at the selected location. Its impact hits an area and applies Burned. Damage scales with Wisdom + Elemental Damage. Level 5 increases impact damage and cast range."
            ChalkDustSpellChoice.TSUNAMI -> "Once the ground glyph finishes drawing, a Water wave begins at a random map edge. It travels toward your position at the moment of casting, then continues until it leaves the map. Damage scales with Wisdom + Elemental Damage. Level 5 increases damage."
            ChalkDustSpellChoice.SINGULARITY -> "Once the ground glyph finishes drawing, a Necrotic singularity opens. It pulls nearby non-boss enemies and repeatedly deals damage based on each target's maximum health and Wisdom + Might of Nature. Bosses are not pulled; moving toward its center speeds them up slightly, while moving away slows them slightly. Deaths feed its final collapse, which also scales with Wisdom + Might of Nature. Duration: 10 seconds (13 at Level 5); Level 5 also makes damage ticks more frequent."
        }
        return "$effect\n\nGround glyph animation: 2.5 seconds. Cooldown: 30 seconds after a successful cast."
    }
}
