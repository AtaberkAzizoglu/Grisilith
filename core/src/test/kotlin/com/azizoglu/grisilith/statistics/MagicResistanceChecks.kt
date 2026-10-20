package com.azizoglu.grisilith.statistics

import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.armor.runtime.ArmorStatBonusSynchronizer
import com.azizoglu.grisilith.armor.runtime.EquippedArmor
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.item.runtime.ItemInventory
import com.azizoglu.grisilith.item.runtime.ItemInstance
import com.azizoglu.grisilith.save.RunSaveStatLayers
import com.azizoglu.grisilith.player.Player

/** Headless regression checks; no graphics context or additional test dependency needed. */
fun main() {
    val armors = DefaultArmors.all
    check(armors.map { it.baseMagicResistance } == listOf(13, 10, 5, 17, 11, 12, 2, 20))
    check(armors.takeLast(4).map { it.baseHealth } == listOf(8, 16, 20, 5))
    check(armors.takeLast(4).map { it.baseArmor } == listOf(7, 9, 25, 2))
    val magical = setOf(DamageType.FIRE, DamageType.AIR, DamageType.WATER,
        DamageType.LIGHTNING, DamageType.NECROTIC, DamageType.ACID, DamageType.POISON)
    check(DamageType.entries.filter { it.isMagical }.toSet() == magical)

    val koba = PlayerStats(canEquipArmor = false, unarmoredBaseHealth = 14, unarmoredBaseArmor = 9)
    koba.setArmorBaseValues(100, 100, 100)
    check(koba.armorBaseHealth() == 14 && koba.armorBaseArmor() == 9)
    check(koba.armorBaseMagicResistance() == 0)
    check(runCatching { EquippedArmor(false).equip(armors.first()) }.isFailure)
    koba.setGearBonuses(mapOf(StatType.MOVEMENT_SPEED to 99))
    ArmorStatBonusSynchronizer(EquippedArmor(false), koba).synchronize()
    check(koba.armorBaseHealth() == 14 && koba.armorBaseArmor() == 9)
    check(koba.totalScore(StatType.MOVEMENT_SPEED) == 10)

    fun incoming(type: DamageType, damage: Int): Int {
        val stats = PlayerStats(mapOf(StatType.MAX_HP to 50, StatType.MAGIC_RESISTANCE to 10))
        stats.setArmorBaseValues(20, 25, 13)
        check(kotlin.math.abs(stats.rawMagicDamageReduction() - 100f*23f/123f)<0.001f)
        return Player(100f, 100f, 2000f, 2000f, stats).takeDamage(damage, type)
    }
    magical.forEach {
        check(incoming(it, 200) == 163) { "Incorrect defense for $it" }
        check(incoming(it, 100) == 81)
        check(incoming(it, 0) == 0)
    }
    DamageType.entries.filter { !it.isMagical && !it.isPhysical }.forEach {
        check(incoming(it, 200) == 200) { "Magic defense must not affect $it" }
    }
    DamageType.entries.filter { it.isPhysical }.forEach {
        check(incoming(it,200)==148) // 25 Base Armor + the default 10 Armor Class.
    }
    val pendant = DefaultItems.obsidianWardPendant
    check(pendant.statModifiers.single().statType == StatType.MAGIC_RESISTANCE)
    check(pendant.statModifiers.single().amount == 1)
    val inventory = ItemInventory()
    repeat(7) {
        check(inventory.canAddDefinition(pendant))
        inventory.addItem(ItemInstance(pendant))
    }
    check(!inventory.canAddDefinition(pendant))
    val boosted = PlayerStats(mapOf(StatType.MAGIC_RESISTANCE to 10))
    boosted.setArmorBaseValues(8, 7, 11)
    boosted.setItemBonuses(mapOf(StatType.MAGIC_RESISTANCE to 1))
    check(kotlin.math.abs(boosted.rawMagicDamageReduction() - 100f*22f/122f)<0.001f)
    val restored = PlayerStats()
    restored.restoreSaveLayers(boosted.exportSaveLayers())
    check(restored.totalScore(StatType.MAGIC_RESISTANCE) == 11)
    val legacy = PlayerStats(mapOf(StatType.MAGIC_RESISTANCE to 10))
    legacy.restoreSaveLayers(RunSaveStatLayers(baseScores = hashMapOf("MAX_HP" to 15)))
    check(legacy.totalScore(StatType.MAGIC_RESISTANCE) == 10)
    println("Magic Resistance / Koba / armor / damage-type checks passed.")
}
