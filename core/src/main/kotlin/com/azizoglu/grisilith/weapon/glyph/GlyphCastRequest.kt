package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.weapon.runtime.WeaponInstance

data class GlyphCastRequest(
    val glyphType: GlyphType,
    val centerX: Float,
    val centerY: Float,
    val sourceWeapon: WeaponInstance
)
