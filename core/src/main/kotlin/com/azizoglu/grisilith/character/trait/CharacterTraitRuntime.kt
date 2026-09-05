package com.azizoglu.grisilith.character.trait

import com.azizoglu.grisilith.combat.interaction.CombatInteractionEvent
import com.azizoglu.grisilith.combat.interaction.CombatInteractionRule
import com.azizoglu.grisilith.combat.interaction.CombatInteractionTrigger
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyDamageContext
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponHitEvent
import kotlin.math.roundToInt

class CharacterTraitRuntime(
    private val traitId: CharacterTraitId,
    private val player: Player,
    private val playerStats: PlayerStats
) : CombatInteractionRule {

    override fun handle(
        event: CombatInteractionEvent
    ) {
        if (
            traitId != CharacterTraitId.NECROMANCY ||
            event.trigger != CombatInteractionTrigger.DAMAGE_APPLIED ||
            event.damageType != DamageType.NECROTIC ||
            event.appliedDamage <= 0
        ) {
            return
        }

        /*
         * Necromancy, kaynağı Weapon, Condition, Tamga veya Skill olsa da
         * uygulanmış her Necrotic hasar olayı için bir Life Steal denemesi
         * yapar. Hasar miktarı Life Steal şansını değiştirmez.
         */
        player.tryLifeSteal(
            isEligibleHit = true
        )
    }

    fun onWeaponHit(
        event: WeaponHitEvent,
        onDamageDealt: (
            damage: Int,
            sourceName: String
        ) -> Unit,
        onEnemyKilled: (Enemy) -> Unit
    ) {
        if (
            traitId != CharacterTraitId.THE_HYPOCRISY ||
            event.appliedDamage <= 0 ||
            event.enemy.isDead ||
            ContentTag.MELEE_WEAPON !in
            event.weapon.definition.tags
        ) {
            return
        }

        event.enemy.applyCondition(
            conditionType = ConditionType.BURNED,
            durationSeconds = BURNED_DURATION_SECONDS
        )

        val elementalDamage =
            playerStats.totalScore(
                StatType.ELEMENTAL_DAMAGE
            )

        val requestedDamage =
            (
                FIRE_BASE_DAMAGE +
                    elementalDamage *
                    ELEMENTAL_DAMAGE_SCALING
                )
                .roundToInt()
                .coerceAtLeast(1)

        val wasAlive =
            !event.enemy.isDead

        val sourceName =
            event.weapon.displayName +
                " — The Hypocrisy"

        val appliedDamage =
            event.enemy.takeDamage(
                amount = requestedDamage,
                damageContext =
                    EnemyDamageContext(
                        damageType = DamageType.FIRE,
                        sourceWeaponRuntimeId =
                            event.weapon.runtimeId,
                        sourceWeaponDefinitionId =
                            event.weapon.definition.id.value,
                        sourceName = sourceName
                    )
            )

        if (appliedDamage > 0) {
            onDamageDealt(
                appliedDamage,
                sourceName
            )
        }

        if (
            wasAlive &&
            event.enemy.isDead
        ) {
            onEnemyKilled(
                event.enemy
            )
        }
    }

    companion object {
        private const val FIRE_BASE_DAMAGE =
            1f

        private const val ELEMENTAL_DAMAGE_SCALING =
            0.08f

        private const val BURNED_DURATION_SECONDS =
            3f
    }
}
