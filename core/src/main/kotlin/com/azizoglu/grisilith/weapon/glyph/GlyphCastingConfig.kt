package com.azizoglu.grisilith.weapon.glyph

import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

/*
 * Glyph sisteminin oynanış kurallarına tek noktadan erişim sağlar.
 *
 * Paket 2'de Glyph Motion runtime, recognizer, trail ve sonuç kutusu
 * bu değerleri kullanacaktır. Denge ayarı için farklı sınıfları
 * düzenlemek gerekmez.
 */
object GlyphCastingConfig {

    const val DRAW_DURATION_SECONDS =
        DefaultWeaponAbilities
            .GLYPH_CASTING_DRAW_DURATION_SECONDS

    const val SUCCESS_COOLDOWN_SECONDS =
        DefaultWeaponAbilities
            .GLYPH_CASTING_SUCCESS_COOLDOWN_SECONDS

    const val FAILURE_COOLDOWN_SECONDS =
        DefaultWeaponAbilities
            .GLYPH_CASTING_FAILURE_COOLDOWN_SECONDS

    const val TRAIL_DAMAGE_TICK_SECONDS =
        DefaultWeaponAbilities
            .GLYPH_TRAIL_DAMAGE_TICK_SECONDS

    const val TRAIL_WISDOM_DAMAGE_MULTIPLIER =
        DefaultWeaponAbilities
            .GLYPH_TRAIL_WISDOM_DAMAGE_MULTIPLIER

    const val MINIMUM_DRAW_DURATION_SECONDS =
        DefaultWeaponAbilities
            .GLYPH_MINIMUM_DRAW_DURATION_SECONDS

    const val MINIMUM_PATH_LENGTH =
        DefaultWeaponAbilities
            .GLYPH_MINIMUM_PATH_LENGTH

    const val MINIMUM_POINT_COUNT =
        DefaultWeaponAbilities
            .GLYPH_MINIMUM_POINT_COUNT
}
