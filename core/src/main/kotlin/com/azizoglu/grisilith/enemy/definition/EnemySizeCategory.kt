package com.azizoglu.grisilith.enemy.definition

/*
 * Enemy görseli, collision alanı ve spawn doğrulaması için
 * kullanılan merkezi boyut sınıfları.
 *
 * MEDIUM mevcut Character boyutu olan 48 px'i temsil eder.
 * Değerler ileride gerçek sprite ölçülerine göre tek noktadan
 * güncellenebilir.
 */
enum class EnemySizeCategory(
    val baseSizePixels: Float
) {
    SMALL(
        baseSizePixels = 32f
    ),

    MEDIUM(
        baseSizePixels = 48f
    ),

    LARGE(
        baseSizePixels = 72f
    ),

    VERY_LARGE(
        baseSizePixels = 108f
    ),

    COLOSSAL(
        baseSizePixels = 160f
    );

    fun resolvedSize(
        sizeMultiplier: Float = 1f
    ): Float {
        require(sizeMultiplier > 0f) {
            "Enemy size multiplier must be greater than zero."
        }

        return baseSizePixels *
            sizeMultiplier
    }
}
