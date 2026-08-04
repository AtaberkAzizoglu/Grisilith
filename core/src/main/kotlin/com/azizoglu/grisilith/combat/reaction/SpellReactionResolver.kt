package com.azizoglu.grisilith.combat.reaction

import com.azizoglu.grisilith.combat.condition.ConditionContainer
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.content.SpellReactionType
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import kotlin.math.roundToInt

object SpellReactionResolver {

    /*
     * Wet hedefe gelen Lightning hasarı %25 artar.
     *
     * İlk denge değeri bilinçli olarak düşüktür:
     * reaction hissedilir fakat ana hasar kaynağının
     * önüne geçmez.
     */
    const val OVERCHARGE_DAMAGE_MULTIPLIER =
        1.25f

    /*
     * false:
     * Wet, Overcharge tetiklenince silinmez.
     * Wet süresi boyunca birden fazla Lightning
     * saldırısı bonus alabilir.
     *
     * İleride tek kullanımlık davranış istenirse
     * yalnızca bu değer true yapılmalıdır.
     */
    const val OVERCHARGE_CONSUMES_WET =
        false

    fun resolveIncomingDamage(
        baseDamage: Int,
        damageContext: EnemyDamageContext,
        conditions: ConditionContainer
    ): SpellReactionResult {

        val safeBaseDamage =
            baseDamage
                .coerceAtLeast(
                    0
                )

        if (
            safeBaseDamage <= 0
        ) {
            return SpellReactionResult(
                resolvedDamage =
                    0
            )
        }

        val triggersOvercharge =
            damageContext.damageType ==
                DamageType.LIGHTNING &&
                conditions.contains(
                    ConditionType.WET
                )

        if (
            !triggersOvercharge
        ) {
            return SpellReactionResult(
                resolvedDamage =
                    safeBaseDamage
            )
        }

        val increasedDamage =
            (
                safeBaseDamage *
                    OVERCHARGE_DAMAGE_MULTIPLIER
                )
                .roundToInt()
                .coerceAtLeast(
                    safeBaseDamage
                )

        return SpellReactionResult(
            resolvedDamage =
                increasedDamage,

            reactionType =
                SpellReactionType
                    .WATER_LIGHTNING_OVERCHARGE,

            consumedCondition =
                if (
                    OVERCHARGE_CONSUMES_WET
                ) {
                    ConditionType.WET
                } else {
                    null
                }
        )
    }
}
