package com.azizoglu.grisilith.weapon.definition.catalog

import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.*
import com.azizoglu.grisilith.weapon.skill.*

object NewPhysicalWeapons {
    const val EARTHBREAKER = "WPN_ABILITY_EARTHBREAKER"
    const val WHIP_SWEEP = "WPN_ABILITY_WHIP_SWEEP"
    const val SHATTERSTONE = "WPN_ABILITY_SHATTERSTONE"
    const val GUIDED_HUNT = "WPN_ABILITY_GUIDED_HUNT"
    private fun modifier(type: StatType, value: Int) = WeaponModifierFactory.create(type, value)
    private fun create(id: String, name: String, description: String, melee: Boolean, damageType: DamageType,
                       damage: Int, speed: Float, range: Float, knockback: Float,
                       abilityId: String, abilityName: String, cooldown: Float,
                       lv2: List<StatModifier>, lv4: List<StatModifier>, upgradedRange: Float = 1f,
                       projectileSpeed: Float = 0f, activeDuration: Float = 0f) = WeaponDefinition(
        id = ContentId(id), displayName = name, description = description,
        tags = setOf(if (melee) ContentTag.MELEE_WEAPON else ContentTag.RANGED_WEAPON),
        damageTypes = setOf(damageType), attackPattern = WeaponAttackPattern.EXTENDED_PHYSICAL,
        baseDamage = damage, baseAttacksPerSecond = speed, baseRange = range,
        baseProjectileSpeed = projectileSpeed, baseKnockback = knockback,
        damageScaling = mapOf((if (melee) StatType.STRENGTH else StatType.DEXTERITY) to 1f),
        passiveStatModifiers = emptyList(),
        levelStats = WeaponLevelProgressionFactory.create(level2Modifiers = lv2,
            level4Modifiers = lv4, level4Range = upgradedRange),
        weaponAbility = WeaponAbilityDefinition(id = abilityId, displayName = abilityName,
            activationType = WeaponAbilityActivationType.ACTIVE, cooldownSeconds = cooldown,
            durationSeconds = activeDuration),
        upgradedWeaponAbility = if (abilityId == GUIDED_HUNT) WeaponAbilityDefinition(
            id = abilityId, displayName = abilityName, activationType = WeaponAbilityActivationType.ACTIVE,
            cooldownSeconds = 25f, durationSeconds = 6f) else null,
        initialUnlockState = ContentUnlockState.LOCKED)

    val warhammer = create("WPN_WARHAMMER", "Warhammer",
        "Crushes the target with a circular hammer head; the shaft deals 33% damage. " +
        "Lv4 extends the shaft by 25% and the head radius by 50%. Earthbreaker sends three impacts forward; " +
        "Lv5 increases their damage and expands their radius by 20% and adds a fourth impact.", true, DamageType.BLUDGEONING,
        14, .310f, 245f, 16f, EARTHBREAKER, "Earthbreaker", 25f,
        listOf(modifier(StatType.CRIT_DAMAGE, 1)), listOf(modifier(StatType.PIERCING, 2)), 1.25f)
    val whip = create("WPN_WHIP", "Whip",
        "Strikes with a dynamic lash and a 30-degree tip cone. The lash deals 33% damage; overlapping " +
        "regions never double-hit. Lv4 lashes twice at 75% damage per strike. Whip Sweep covers a wide arc; " +
        "Lv3 sweeps 80 degrees; Lv5 increases damage and sweeps 120 degrees.", true, DamageType.SLASHING,
        6, .750f, 340f, 2f, WHIP_SWEEP, "Whip Sweep", 25f,
        listOf(modifier(StatType.STRENGTH, 1), modifier(StatType.MOVEMENT_SPEED, 1)),
        listOf(modifier(StatType.ATTACK_SPEED, 2)))
    val sling = create("WPN_SLING", "Sling",
        "Throws stones with light knockback. Lv4 stones split into 3-8 short-range fragments at 50% damage; " +
        "fragments ignore the original target and never split again. Shatterstone throws a huge boulder " +
        "at the cursor, lands once. Lv5 increases impact damage and size by 25% and adds one forward bounce.",
        false, DamageType.BLUDGEONING, 11, .500f, 550f, 5f, SHATTERSTONE, "Shatterstone", 30f,
        listOf(modifier(StatType.ATTACK_SPEED, 1)), listOf(modifier(StatType.CRIT_RATE, 1)),
        projectileSpeed = 720f)
    val boomerang = create("WPN_BOOMERANG", "Boomerang",
        "Returns after each throw and can hit once on each flight leg. Flight timing follows Attack Speed. " +
        "Lv4 makes a half-range rear flight at 50% damage after the first catch. Guided Hunt launches an extra boomerang " +
        "and steers only this weapon copy's outbound flights toward the cursor for 5 seconds without extending " +
        "their lifetime. Lv5 extends guidance to 6 seconds and reduces cooldown to 25 seconds.",
        false, DamageType.SLASHING, 20, .120f, 700f, 8f, GUIDED_HUNT, "Guided Hunt", 30f,
        listOf(modifier(StatType.CRIT_DAMAGE, 1)), listOf(modifier(StatType.PIERCING, 2)),
        projectileSpeed = 168f, activeDuration = 5f)
    val all = listOf(warhammer, whip, sling, boomerang)
    fun patternLabel(id: String): String = when(id) {
        "WPN_WARHAMMER" -> "Shaft / Hammer Head"
        "WPN_WHIP" -> "Lash / Tip Cone"
        "WPN_SLING" -> "Stone / Fragments"
        "WPN_BOOMERANG" -> "Returning Projectile"
        else -> "Physical"
    }
    fun levelDescription(id: String, level: Int): String = when(level) {
        4 -> when(id) {
            "WPN_WARHAMMER" -> "Shaft length +25%; head radius +50%; overlap uses head damage only."
            "WPN_WHIP" -> "Two separate lashes at 75% damage each; tip cone stays 30 degrees and grows to length 100."
            "WPN_SLING" -> "3-8 fragments, half damage and range 80; original target is ignored."
            "WPN_BOOMERANG" -> "Rear outbound and return legs deal 50% damage; a new throw can begin after the first catch."
            else -> ""
        }
        5 -> when(id) {
            "WPN_WARHAMMER" -> "Earthbreaker: 2x damage per impact; radii +20%; four circles."
            "WPN_WHIP" -> "Whip Sweep: 2.5x damage per enemy; 120-degree fan."
            "WPN_SLING" -> "Shatterstone: 3.5x/2.5x impact damage; one bounce; boulder size +25%."
            "WPN_BOOMERANG" -> "Guided Hunt: 6 seconds; cooldown 25 seconds; extra projectile size +40%; only this copy's outbound flights are guided."
            else -> ""
        }
        else -> ""
    }
}
