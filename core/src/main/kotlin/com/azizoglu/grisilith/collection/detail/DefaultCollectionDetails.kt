package com.azizoglu.grisilith.collection.detail

import com.azizoglu.grisilith.armor.definition.ArmorDefinition
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.ContentTag
import com.azizoglu.grisilith.content.StatModifier
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
import com.azizoglu.grisilith.item.definition.DefaultItems
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.tamga.definition.DefaultTamgas
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.definition.WeaponDefinition
import com.azizoglu.grisilith.weapon.definition.WeaponLevelStats
import java.util.Locale

/** Collection copy is generated from the same definitions used by gameplay. */
object DefaultCollectionDetails {

    val all: List<CollectionDetailDefinition> =
        buildList {
            DefaultCharacters.all.forEach { character ->
                add(
                    CollectionDetailDefinition(
                        contentId = character.id,
                        description = character.description,
                        rows = listOf(
                            CollectionDetailRow("Trait", "${character.trait.displayName}: ${character.trait.description}"),
                            CollectionDetailRow("Main Stats", com.azizoglu.grisilith.character.CharacterPresentation.mainStatsText(character)),
                            CollectionDetailRow(
                                "Base Stats",
                                StatType.alphabetical.joinToString(", ") { stat ->
                                    "${stat.displayName} ${character.baseStats.getValue(stat)}"
                                }
                            )
                        )
                    )
                )
            }

            DefaultWeapons.all.forEach { add(it.toCollectionDetail()) }
            DefaultArmors.all.forEach { add(it.toCollectionDetail()) }

            DefaultItems.all.forEach { item ->
                val maximumCopies =
                    item.maximumInventoryCopies ?: item.rarity.maximumInventoryCopies
                add(
                    CollectionDetailDefinition(
                        contentId = item.id,
                        description = item.description.substringBefore("\nMaximum copies:"),
                        rows = listOf(
                            CollectionDetailRow("Rarity", item.rarity.displayName),
                            CollectionDetailRow("Maximum Copies", maximumCopies.toString())
                        )
                    )
                )
            }

            DefaultTamgas.all.forEach { tamga ->
                add(
                    CollectionDetailDefinition(
                        contentId = tamga.id,
                        description = com.azizoglu.grisilith.tamga.definition.TamgaPresentation.details(tamga),
                        rows = buildList {
                            add(CollectionDetailRow("Slot", tamga.slotType.friendlyName()))
                            add(CollectionDetailRow("Activation", tamga.activationType.friendlyName()))
                            if (tamga.interactableStats.isNotEmpty()) {
                                add(CollectionDetailRow("Related Stats", tamga.interactableStats.joinToString(", ") { it.displayName }))
                            }
                            if (tamga.statModifiers.isNotEmpty()) {
                                add(CollectionDetailRow("Stat Bonuses", tamga.statModifiers.formatted()))
                            }
                        }
                    )
                )
            }

            DefaultEnemyKinds.all.forEach { add(it.toCollectionDetail()) }
        }

    private fun WeaponDefinition.toCollectionDetail(): CollectionDetailDefinition {
        val ability = weaponAbility
        return CollectionDetailDefinition(
            contentId = id,
            description = com.azizoglu.grisilith.weapon.definition.WeaponPresentation.hover(this),
            rows = buildList {
                add(CollectionDetailRow("Weapon Type", weaponTypeName()))
                add(CollectionDetailRow("Damage Scaling", orderedDamageScaling().joinToString(" + ") { (stat, multiplier) -> "${stat.displayName} x${multiplier.clean()}" }))
                add(CollectionDetailRow("Damage Type", damageTypes.joinToString(", ") { it.displayName }))
                add(CollectionDetailRow("Attack Pattern", if(attackPattern == com.azizoglu.grisilith.weapon.definition.WeaponAttackPattern.EXTENDED_PHYSICAL)
                    com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.patternLabel(id.value)
                    else attackPattern.friendlyName()))
                add(
                    CollectionDetailRow(
                        "Base Stats",
                        buildList {
                            add("Damage $baseDamage")
                            add("Speed ${baseAttacksPerSecond.clean()}/s")
                            add("Range ${baseRange.clean()} px")
                            if (baseProjectileSpeed > 0f) add("Projectile Speed ${baseProjectileSpeed.clean()}")
                            if (baseKnockback > 0f) add("Knockback ${baseKnockback.clean()}")
                            if (baseProjectileCount > 1) add("Projectiles $baseProjectileCount")
                            if (baseMaximumTargets > 1) add("Targets $baseMaximumTargets")
                        }.joinToString(" | ")
                    )
                )
                if (ability != null) {
                    add(CollectionDetailRow("Weapon Ability", buildString {
                        append(ability.displayName)
                        if (ability.cooldownSeconds > 0f) append(" (${ability.cooldownSeconds.clean()}s cooldown)")
                    }))
                }
                add(CollectionDetailRow("Normal Attack",com.azizoglu.grisilith.weapon.definition.WeaponPresentation.normal(this@toCollectionDetail)))
                add(CollectionDetailRow("Ability Effect",com.azizoglu.grisilith.weapon.definition.WeaponPresentation.ability(this@toCollectionDetail)))
                add(CollectionDetailRow("Scaling Reference",com.azizoglu.grisilith.weapon.definition.WeaponPresentation.scaling(this@toCollectionDetail)))
            },
            notes = listOf(com.azizoglu.grisilith.weapon.definition.WeaponPresentation.upgrades(this))
        )
    }

    private fun WeaponDefinition.levelSummary(stats: WeaponLevelStats): String =
        buildList {
            if (stats.attackSpeedMultiplier != 1f) add("Attack Speed x${stats.attackSpeedMultiplier.clean()}")
            if (stats.rangeMultiplier != 1f) add("Range x${stats.rangeMultiplier.clean()}")
            if (stats.knockbackMultiplier != 1f) add("Knockback x${stats.knockbackMultiplier.clean()}")
            if (stats.additionalTargets > 0) add("+${stats.additionalTargets} targets")
            if (stats.grantedStatModifiers.isNotEmpty()) add(stats.grantedStatModifiers.formatted())
            if (stats.weaponAbilityUnlocked && !stats.weaponAbilityUpgraded) add("Unlocks ${weaponAbility?.displayName ?: "Weapon Ability"}")
            if (stats.basicAttackUpgraded) add("Basic Attack Upgrade")
            if (stats.weaponAbilityUpgraded) add("${upgradedWeaponAbility?.displayName ?: weaponAbility?.displayName ?: "Weapon Ability"} Upgrade")
            val gearNote=com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.levelDescription(id.value,stats.level)
            if(gearNote.isNotBlank()) add(gearNote)
        }.ifEmpty { listOf("No additional effect") }.joinToString("; ")

    private fun ArmorDefinition.toCollectionDetail(): CollectionDetailDefinition =
        CollectionDetailDefinition(
            contentId = id,
            description = description,
            rows = listOf(
                CollectionDetailRow("Armor Type", armorTypeName()),
                CollectionDetailRow("Base Health", baseHealth.toString()),
                CollectionDetailRow("Base Armor", baseArmor.toString()),
                CollectionDetailRow("Magic Resistance", baseMagicResistance.toString()),
                CollectionDetailRow("Stat Modifiers", statModifiers.takeIf { it.isNotEmpty() }?.formatted() ?: "None")
            )
        )

    private fun EnemyKindDefinition.toCollectionDetail(): CollectionDetailDefinition =
        CollectionDetailDefinition(
            contentId = id,
            description = description,
            rows = listOf(
                CollectionDetailRow("Type", allowedTypes.joinToString(", ") { if(id.value=="ENEMY_KIND_VAMPIRE_LORD" && it==com.azizoglu.grisilith.enemy.EnemyType.FINAL_BOSS) "Arc Boss" else it.friendlyName() }),
                CollectionDetailRow("Family", enemyTags.joinToString(", ") { if(it == com.azizoglu.grisilith.enemy.definition.EnemyTag.MONSTER) "Monstrosity" else it.friendlyName() })
            )
        )

    private fun WeaponDefinition.weaponTypeName(): String = when {
        ContentTag.MELEE_WEAPON in tags -> "Melee"
        ContentTag.RANGED_WEAPON in tags -> "Ranged"
        ContentTag.SPELL_FOCUS in tags -> "Spell Focus"
        else -> "Weapon"
    }

    private fun ArmorDefinition.armorTypeName(): String = when {
        ContentTag.LIGHT_ARMOR in tags -> "Light Armor"
        ContentTag.MEDIUM_ARMOR in tags -> "Medium Armor"
        ContentTag.HEAVY_ARMOR in tags -> "Heavy Armor"
        ContentTag.CLOTHES in tags -> "Clothes"
        else -> "Armor"
    }

    private fun List<StatModifier>.formatted(): String = sortedBy { it.statType.displayName }.joinToString(", ") { modifier ->
        val sign = if (modifier.amount > 0) "+" else ""
        "${modifier.statType.displayName} $sign${modifier.amount}"
    }

    private fun ContentId.friendlyName(): String =
        value.removePrefix("ENEMY_BEHAVIOR_").removePrefix("ENEMY_ABILITY_").removePrefix("ENEMY_PASSIVE_").friendlyName()

    private fun Enum<*>.friendlyName(): String = name.friendlyName()

    private fun String.friendlyName(): String =
        lowercase(Locale.ENGLISH).split('_').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.ENGLISH) } }

    private fun Float.clean(): String =
        if (this == toInt().toFloat()) toInt().toString()
        else String.format(Locale.ENGLISH, "%.3f", this).trimEnd('0').trimEnd('.')
}
