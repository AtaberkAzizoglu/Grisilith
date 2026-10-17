package qa

import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.badlogic.gdx.utils.Json
import java.io.File
import java.util.Random
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Isolated, raw-damage benchmark. No world, save, preferences or game balance writes.
 * Calculator, level definitions, bonus synchronizer and normal attack timer are production code.
 * Ability hit counts are explicit analytical scenarios, NOT a collision/world simulation.
 */
object WeaponBalance {
    data class Row(
        val profile: String, val level: Int, val weapon: String, val mode: String,
        val base: Int, val strength: Int, val dexterity: Int, val ac: Int,
        val attackSpeed: Int, val critChance: Float, val critMultiplier: Float,
        val nonCrit: Int, val crit: Int, val expectedHit: Double,
        val aps: Float, val basicDps: Double, val attacks60: Int,
        val ability: String, val cooldown: Float, val castDelay: Float,
        val abilityNonCritPerHit: Int, val abilityExpectedPerHit: Double,
        val abilityEmissionCeiling: Int, val abilitySingleTargetHits: Int,
        val abilityCasts60: Int, val abilitySingleTarget60: Double,
        val expectedTotal60: Double, val mcMean60: Double, val mcP05: Double,
        val mcP95: Double, val standardError: Double
    )

    @JvmStatic fun main(args: Array<String>) {
        val definitions = listOf(DefaultWeapons.doubleSwords, DefaultWeapons.longsword,
            DefaultWeapons.lance, DefaultWeapons.shield, DefaultWeapons.dagger,
            DefaultWeapons.longbow, DefaultWeapons.crossbow)
        val rows = mutableListOf<Row>()
        val rng = Random(20261004L)
        for (profile in listOf("All10", "All20", "All40", "Dex40Str10")) {
            val score = when(profile) { "All10" -> 10; "All20" -> 20; else -> 40 }
            for (level in 1..5) for (definition in definitions) {
                val modes = if (definition == DefaultWeapons.dagger) listOf("Melee", "Throw") else listOf("Normal")
                for (mode in modes) {
                    val stats = PlayerStats()
                    StatType.entries.forEach { type ->
                        val value = if (profile == "Dex40Str10" && type == StatType.STRENGTH) 10 else score
                        stats.setBaseScore(type, value.coerceAtMost(25))
                        if (value > 25) stats.addRunBonus(type, value - 25)
                    }
                    val weapon = WeaponInstance(definition, level)
                    val inventory = WeaponInventory()
                    check(inventory.addWeapon(weapon))
                    WeaponStatBonusSynchronizer(inventory, stats).synchronize()
                    val calculator = WeaponDamageCalculator(stats)
                    val primary = if (mode == "Throw") StatType.DEXTERITY else null
                    val raw = calculator.calculateNonCriticalDamage(weapon, DamageType.PIERCING, primary)
                    val chance = calculator.criticalHitChance(weapon)
                    val mult = calculator.criticalDamageMultiplier()
                    val shieldExtra = if (definition == DefaultWeapons.shield && level >= 4)
                        stats.totalScore(StatType.STRENGTH) * 0.15f else 0f
                    val normal = (raw + shieldExtra).roundToInt()
                    val critical = if (definition == DefaultWeapons.dagger && level >= 4) {
                        if (mode == "Melee") ((raw * mult).roundToInt() * 1.5f).roundToInt()
                        else (raw * mult * 1.5f).roundToInt()
                    } else ((raw * mult).roundToInt() + shieldExtra).roundToInt()
                    val expected = normal * (1.0 - chance) + critical * chance
                    val aps = (definition.baseAttacksPerSecond +
                        (definition.maximumAttacksPerSecond - definition.baseAttacksPerSecond) *
                        stats.attackSpeedProgress()) * weapon.currentLevelStats.attackSpeedMultiplier
                    val timer = WeaponAttackTimer()
                    var attacks = 0
                    repeat(60 * 240 - 1) { timer.update(1f / 240f, aps) { attacks++ } }
                    check(kotlin.math.abs(attacks - (60 * aps).toInt()) <= 1)
                    check(weapon.currentLevelStats.damageMultiplier == 1f)
                    val ability = weapon.unlockedAbility
                    val cd = ability?.cooldownSeconds ?: 0f
                    val delay = when(definition) { DefaultWeapons.longbow -> 1f; DefaultWeapons.crossbow -> 2f; else -> 0f }
                    // All ability projectiles use the default physical stat, including Knife Swarm.
                    val foundation = calculator.calculateNonCriticalDamage(weapon, DamageType.PIERCING)
                    val abilityBase = when(definition) {
                        DefaultWeapons.crossbow -> (foundation * if(level == 5) 2f else 1.5f).roundToInt()
                        DefaultWeapons.shield -> ((stats.totalScore(StatType.ARMOR_CLASS) + 15) * 0.75f).roundToInt()
                        else -> foundation
                    }
                    val abilityCrit = when(definition) {
                        DefaultWeapons.longsword, DefaultWeapons.shield -> abilityBase
                        DefaultWeapons.lance -> ((foundation * mult).roundToInt() * if(level == 5) 1.5f else 1f).roundToInt()
                        else -> (abilityBase * mult).roundToInt()
                    }
                    val abilityNormal = if(definition == DefaultWeapons.lance && level == 5)
                        (foundation * 1.5f).roundToInt() else abilityBase
                    val abilityMean = abilityNormal * (1.0 - chance) + abilityCrit * chance
                    val enabled = level >= 3 && definition != DefaultWeapons.shield
                    val ceiling = if(!enabled) 0 else when(definition) {
                        DefaultWeapons.doubleSwords -> if(level == 5) 7 else 5 // may additionally splash
                        DefaultWeapons.dagger -> if(level == 5) 12 else 6
                        DefaultWeapons.longbow -> if(level == 5) 30 else if(level == 4) 10 else 5
                        DefaultWeapons.longsword, DefaultWeapons.lance, DefaultWeapons.crossbow -> -1 // geometry-limited
                        else -> 0
                    }
                    // Narrow, isolated target: one ray/arrow from each volley, no ricochet targets.
                    val hits = if(!enabled) 0 else when(definition) {
                        DefaultWeapons.doubleSwords -> if(level == 5) 7 else 5
                        DefaultWeapons.dagger, DefaultWeapons.longbow -> if(level == 5) 2 else 1
                        else -> 1
                    }
                    var casts = 0
                    var time = delay
                    while(enabled && time < 60.0) { casts++; time += cd + delay }
                    val ability60 = casts * hits * abilityMean
                    val total = attacks * expected + ability60
                    val samples = DoubleArray(5000) {
                        var sum = 0.0
                        repeat(attacks) { sum += if(rng.nextDouble() < chance) critical else normal }
                        repeat(casts * hits) { sum += if(rng.nextDouble() < chance) abilityCrit else abilityNormal }
                        sum
                    }
                    val mean = samples.average()
                    val se = sqrt(samples.sumOf { (it-mean)*(it-mean) } / (samples.size-1)) / sqrt(samples.size.toDouble())
                    check(kotlin.math.abs(mean-total) < 6 * se + 1) { "Monte Carlo expectation mismatch" }
                    samples.sort()
                    rows += Row(profile, level, definition.displayName, mode, definition.baseDamage,
                        stats.totalScore(StatType.STRENGTH), stats.totalScore(StatType.DEXTERITY),
                        stats.totalScore(StatType.ARMOR_CLASS), stats.totalScore(StatType.ATTACK_SPEED),
                        chance, mult, normal, critical, expected, aps, aps * expected, attacks,
                        ability?.displayName ?: "Locked", cd, if(enabled) delay else 0f,
                        if(level >= 3 && (definition != DefaultWeapons.shield || level == 5)) abilityNormal else 0,
                        if(level >= 3 && (definition != DefaultWeapons.shield || level == 5)) abilityMean else 0.0,
                        ceiling, hits, casts, ability60, total, mean, samples[249], samples[4749], se)
                }
            }
        }
        val json = Json().apply { setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json) }
        File("tools/weapon-balance/results.json").writeText(json.prettyPrint(rows))
        val fields = Row::class.java.declaredFields.filterNot { it.isSynthetic }
        File("tools/weapon-balance/results.csv").printWriter().use { out ->
            out.println(fields.joinToString(",") { it.name })
            rows.forEach { row -> out.println(fields.joinToString(",") { field ->
                field.isAccessible = true
                field.get(row).toString()
            }) }
        }
        println("WEAPON_BALANCE_PASS: ${rows.size} rows; 5000 seeded trials/row; production damage, bonuses and timer; no gameplay changes.")
        rows.filter { it.profile == "All10" && it.level in listOf(1,3,5) }.forEach {
            println("${it.weapon}/${it.mode} L${it.level}: hit=${it.nonCrit}, crit=${it.crit}, APS=${it.aps}, DPS=${it.basicDps}, 60s=${it.expectedTotal60}, P05=${it.mcP05}, P95=${it.mcP95}")
        }
    }
}
