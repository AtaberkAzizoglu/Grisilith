package com.azizoglu.grisilith.map.enemy

import com.azizoglu.grisilith.content.ContentId

/*
 * Belirli bir Map üzerinde Enemy Kind davranışını değiştirir.
 *
 * weightMultiplier:
 * 1f  -> varsayılan ağırlık
 * 2f  -> iki kat seçim ağırlığı
 * 0f  -> bu Map üzerinde devre dışı
 */
data class MapEnemyKindOverride(
    val enemyKindId: ContentId,
    val weightMultiplier: Float = 1f,
    val isEnabled: Boolean = true
) {

    init {
        require(weightMultiplier >= 0f) {
            "Map Enemy weight multiplier cannot be negative."
        }
    }

    val resolvedWeightMultiplier: Float
        get() =
            if (isEnabled) {
                weightMultiplier
            } else {
                0f
            }
}
