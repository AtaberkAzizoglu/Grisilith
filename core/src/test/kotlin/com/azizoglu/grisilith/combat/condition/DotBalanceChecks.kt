package com.azizoglu.grisilith.combat.condition

import com.azizoglu.grisilith.combat.affinity.DamageAffinity
import com.azizoglu.grisilith.content.ConditionType
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.runtime.EnemyKindStatScaler
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.runtime.WeaponDamageCalculator
import com.azizoglu.grisilith.weapon.skill.DefaultWeaponAbilities

fun main() {
    val stats = PlayerStats(mapOf(
        StatType.ELEMENTAL_DAMAGE to 25,
        StatType.MIGHT_OF_NATURE to 25,
        StatType.SHEPHERD to 25,
        StatType.WISDOM to 25
    ))
    listOf(
        StatType.ELEMENTAL_DAMAGE,
        StatType.MIGHT_OF_NATURE,
        StatType.SHEPHERD,
        StatType.WISDOM
    ).forEach { stats.adjustRunBonus(it, 25) }
    fun condition(type: ConditionType) =
        ConditionDamageCalculator.createApplication(type, stats).source
    check(condition(ConditionType.BURNED).resolvedTickDamage(10_000) == 250)
    check(condition(ConditionType.POISONED).resolvedTickDamage(10_000) == 250)
    check(condition(ConditionType.CORRODING).resolvedTickDamage(10_000) == 300)
    check(condition(ConditionType.NECROSIS).resolvedTickDamage(10_000) == 300)
    check(DefaultConditionDefinitions.DOT_TICK_INTERVAL_SECONDS == 0.5f)
    check(DefaultConditionDefinitions.DEFAULT_DURATION_SECONDS == 3f)
    check(DamageConditionRules.conditionFor(DamageType.WATER) == ConditionType.WET)
    check(DamageConditionRules.conditionFor(DamageType.FIRE) == ConditionType.BURNED)
    check(DamageConditionRules.conditionFor(DamageType.NECROTIC) == ConditionType.NECROSIS)
    check(DamageConditionRules.conditionFor(DamageType.ACID) == ConditionType.CORRODING)
    check(DamageConditionRules.conditionFor(DamageType.POISON) == ConditionType.POISONED)

    val calculator = WeaponDamageCalculator(stats)
    check(calculator.calculateMaximumHealthSpellTickDamage(10_000, DamageType.LIGHTNING,
        DefaultWeaponAbilities.STATIC_CLOUDS_LIGHTNING_DPS_MULTIPLIER) == 165)
    check(calculator.calculateMaximumHealthSpellTickDamage(10_000, DamageType.ACID,
        DefaultWeaponAbilities.ACIDIC_POOL_DAMAGE_PER_SECOND_MULTIPLIER) == 150)
    check(calculator.calculateMaximumHealthSpellTickDamage(10_000, DamageType.AIR,
        DefaultWeaponAbilities.VORTEX_PULL_DAMAGE_PER_SECOND_MULTIPLIER) == 100)

    val undead = EnemyKindStatScaler.resolve(DefaultEnemyKinds.skeletonWarrior, EnemyType.NORMAL, 5)
    check(undead.damageAffinities.affinityFor(DamageType.POISON) == DamageAffinity.IMMUNE)
    check(undead.damageAffinities.affinityFor(DamageType.NECROTIC) == DamageAffinity.IMMUNE)
    check(ConditionType.POISONED in undead.conditionImmunities)
    check(ConditionType.NECROSIS in undead.conditionImmunities)
    val witch = EnemyKindStatScaler.resolve(DefaultEnemyKinds.witch, EnemyType.FINAL_BOSS, 5)
    check(witch.damageAffinities.affinityFor(DamageType.POISON) == DamageAffinity.RESISTANT)
    check(ConditionType.POISONED !in witch.conditionImmunities)
    println("DoT percentage, automatic condition and resistance checks passed.")
}
