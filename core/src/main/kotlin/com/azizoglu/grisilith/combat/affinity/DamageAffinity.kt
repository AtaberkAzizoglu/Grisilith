package com.azizoglu.grisilith.combat.affinity

import com.azizoglu.grisilith.content.DamageType
import kotlin.math.roundToInt

/*
 * Tek bir DamageType için savunma ilişkisi.
 * Aynı DamageType aynı anda birden fazla affinity taşıyamaz.
 */
enum class DamageAffinity {
    NORMAL,
    RESISTANT,
    VULNERABLE,
    IMMUNE
}

/*
 * Player ve Enemy tarafından ortak kullanılır.
 * Tanımlanmamış DamageType'lar NORMAL kabul edilir.
 */
data class DamageAffinityProfile(
    private val values:
    Map<DamageType, DamageAffinity> =
        emptyMap()
) {
    fun affinityFor(
        damageType: DamageType
    ): DamageAffinity {
        if (
            damageType ==
            DamageType.NONE
        ) {
            return DamageAffinity.NORMAL
        }

        return values[
            damageType
        ] ?: DamageAffinity.NORMAL
    }

    fun withAffinity(
        damageType: DamageType,
        affinity: DamageAffinity
    ): DamageAffinityProfile {
        if (
            damageType ==
            DamageType.NONE
        ) {
            return this
        }

        val updated =
            values.toMutableMap()

        if (
            affinity ==
            DamageAffinity.NORMAL
        ) {
            updated.remove(
                damageType
            )
        } else {
            updated[
                damageType
            ] = affinity
        }

        return DamageAffinityProfile(
            updated.toMap()
        )
    }

    companion object {
        val NONE =
            DamageAffinityProfile()

        fun of(
            vararg entries:
            Pair<DamageType, DamageAffinity>
        ): DamageAffinityProfile {
            return DamageAffinityProfile(
                entries
                    .filter { entry ->
                        entry.first !=
                            DamageType.NONE &&
                            entry.second !=
                            DamageAffinity.NORMAL
                    }
                    .toMap()
            )
        }
    }
}

object DamageAffinityResolver {

    /*
     * IMMUNE      -> x0
     * RESISTANT   -> x0.5
     * NORMAL      -> x1
     * VULNERABLE  -> x2
     */
    fun resolve(
        requestedDamage: Int,
        damageType: DamageType,
        profile: DamageAffinityProfile
    ): Int {
        if (
            requestedDamage <=
            0
        ) {
            return 0
        }

        return when (
            profile.affinityFor(
                damageType
            )
        ) {
            DamageAffinity.IMMUNE ->
                0

            DamageAffinity.RESISTANT ->
                (
                    requestedDamage *
                        0.50f
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    )

            DamageAffinity.NORMAL ->
                requestedDamage

            DamageAffinity.VULNERABLE ->
                (
                    requestedDamage *
                        2f
                    )
                    .roundToInt()
                    .coerceAtLeast(
                        1
                    )
        }
    }
}
