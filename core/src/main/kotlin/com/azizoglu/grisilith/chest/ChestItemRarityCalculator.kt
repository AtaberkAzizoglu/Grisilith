package com.azizoglu.grisilith.chest

import com.azizoglu.grisilith.item.definition.ItemRarity

/*
 * Chest item rarity sistemi.
 *
 * ÖNEMLİ:
 * Rarity önce seçilir.
 * Sonra o rarity içindeki uygun itemlerden bir tanesi seçilir.
 *
 * Böylece havuzdaki Rare/Legendary/Artifact item sayısı,
 * rarity'nin çıkma ihtimalini değiştirmez.
 */
object ChestItemRarityCalculator {

    private const val MAXIMUM_LUCK =
        50

    /*
     * Stage ilerlemesi Luck yatırımının yerini tamamen almasın;
     * yalnızca hafif bir rarity desteği versin.
     *
     * Stage 30'a kadar her 5 stage = +1 effective rarity luck.
     * Stage 30'dan sonra her 5 stage = +2 effective rarity luck.
     * Stage bonusu maksimum +15.
     *
     * Örnek:
     * Stage 1  -> +0
     * Stage 5  -> +1
     * Stage 30 -> +6
     * Stage 35 -> +8
     * Stage 55+-> +15
     */
    private const val STAGES_PER_EFFECTIVE_LUCK =
        5

    private const val DOUBLE_STAGE_BONUS_AFTER_STAGE =
        30

    private const val MAXIMUM_STAGE_LUCK_BONUS =
        15

    data class Distribution(
        val common: Float,
        val uncommon: Float,
        val rare: Float,
        val legendary: Float,
        val artifact: Float
    ) {
        fun weightFor(
            rarity:
            ItemRarity
        ): Float {
            return when (rarity) {
                ItemRarity.COMMON ->
                    common

                ItemRarity.UNCOMMON ->
                    uncommon

                ItemRarity.RARE ->
                    rare

                ItemRarity.LEGENDARY ->
                    legendary

                ItemRarity.ARTIFACT ->
                    artifact
            }
        }
    }

    /*
     * Kullanıcının belirlediği üç anchor:
     *
     * Effective Luck 0:
     * U 85 / R 10 / L 4.5 / A 0.5
     *
     * Effective Luck 25:
     * U 40 / R 35 / L 20 / A 5
     *
     * Effective Luck 50:
     * U 10 / R 30 / L 40 / A 20
     *
     * Anchor'lar arasında piecewise-linear interpolation yapılır.
     */
    private val ZERO =
        Distribution(
            common = 0f,
            uncommon = 0.85f,
            rare = 0.10f,
            legendary = 0.045f,
            artifact = 0.005f
        )

    private val MID =
        Distribution(
            common = 0f,
            uncommon = 0.40f,
            rare = 0.35f,
            legendary = 0.20f,
            artifact = 0.05f
        )

    private val MAX =
        Distribution(
            common = 0f,
            uncommon = 0.10f,
            rare = 0.30f,
            legendary = 0.40f,
            artifact = 0.20f
        )

    fun effectiveLuck(
        luckScore:
        Int,

        currentStage:
        Int
    ): Int {
        val safeLuck =
            luckScore.coerceIn(
                0,
                MAXIMUM_LUCK
            )

        val safeStage =
            currentStage.coerceAtLeast(
                1
            )

        val completedBlocksBeforeDoubleBonus =
            safeStage
                .coerceAtMost(
                    DOUBLE_STAGE_BONUS_AFTER_STAGE
                ) /
                STAGES_PER_EFFECTIVE_LUCK

        val completedBlocksAfterDoubleBonus =
            if (
                safeStage <=
                DOUBLE_STAGE_BONUS_AFTER_STAGE
            ) {
                0
            } else {
                (
                    safeStage -
                        DOUBLE_STAGE_BONUS_AFTER_STAGE
                    ) /
                    STAGES_PER_EFFECTIVE_LUCK
            }

        val stageBonus =
            (
                completedBlocksBeforeDoubleBonus +
                    completedBlocksAfterDoubleBonus *
                    2
                )
                .coerceAtMost(
                    MAXIMUM_STAGE_LUCK_BONUS
                )

        return (
            safeLuck +
                stageBonus
            )
            .coerceAtMost(
                MAXIMUM_LUCK
            )
    }

    fun distribution(
        luckScore:
        Int,

        currentStage:
        Int
    ): Distribution {
        val effective =
            effectiveLuck(
                luckScore =
                    luckScore,

                currentStage =
                    currentStage
            )

        val base = if (
            effective <=
            25
        ) {
            val t =
                effective /
                    25f

            interpolate(
                from =
                    ZERO,

                to =
                    MID,

                t =
                    t
            )
        } else {
            val t =
                (
                    effective -
                        25
                    ) /
                    25f

            interpolate(
                from =
                    MID,

                to =
                    MAX,

                t =
                    t
            )
        }

        /*
         * Common yalnızca temel stat itemlerini taşır. Eski Uncommon payını
         * Common/Uncommon arasında bölerek üst rarity anchor'larını koruruz.
         * Luck yükseldikçe Common payı azalır fakat düşük Luck'ta build'in
         * temel statlarına erişim güvenilir kalır.
         */
        val luckProgress = effective / MAXIMUM_LUCK.toFloat()
        val commonShareOfLowTier = lerp(0.65f, 0.35f, luckProgress)

        /*
         * Erken oyunda Legendary ve özellikle Artifact baskılanır. Stage 30'da
         * eski oranlara tamamen ulaşılır; kesilen pay Common'a aktarılır.
         */
        val stageMaturity =
            ((currentStage.coerceAtLeast(1) - 1) / 29f)
                .coerceIn(0f, 1f)
        val legendary = base.legendary * lerp(0.50f, 1f, stageMaturity)
        val artifact = base.artifact * lerp(0.20f, 1f, stageMaturity)
        val suppressedHighRarity =
            (base.legendary - legendary) +
                (base.artifact - artifact)

        return Distribution(
            common =
                base.uncommon * commonShareOfLowTier +
                    suppressedHighRarity,
            uncommon = base.uncommon * (1f - commonShareOfLowTier),
            rare = base.rare,
            legendary = legendary,
            artifact = artifact
        )
    }

    private fun interpolate(
        from:
        Distribution,

        to:
        Distribution,

        t:
        Float
    ): Distribution {
        val safeT =
            t.coerceIn(
                0f,
                1f
            )

        return Distribution(
            common =
                lerp(
                    from.common,
                    to.common,
                    safeT
                ),

            uncommon =
                lerp(
                    from.uncommon,
                    to.uncommon,
                    safeT
                ),

            rare =
                lerp(
                    from.rare,
                    to.rare,
                    safeT
                ),

            legendary =
                lerp(
                    from.legendary,
                    to.legendary,
                    safeT
                ),

            artifact =
                lerp(
                    from.artifact,
                    to.artifact,
                    safeT
                )
        )
    }

    private fun lerp(
        from:
        Float,

        to:
        Float,

        t:
        Float
    ): Float {
        return from +
            (
                to -
                    from
                ) *
            t
    }
}
