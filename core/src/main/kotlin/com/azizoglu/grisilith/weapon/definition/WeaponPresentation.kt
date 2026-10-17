package com.azizoglu.grisilith.weapon.definition

import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.statistics.StatType
import java.util.Locale

/** Selection copy and mechanical reference; never used to calculate combat damage. */
object WeaponPresentation {
    private fun key(w:WeaponDefinition)=w.id.value.removePrefix("WPN_")
    fun normal(w:WeaponDefinition,level:Int=1):String = when(key(w)) {
        "DOUBLE_SWORDS" -> if(level>=4) "Paired cross-slashes strike nearby enemies in an expanded reach." else "Fast short slashes strike nearby enemies. Trades reach for attack frequency."
        "LONGSWORD" -> if(level>=4) "Sweeps a longer, wider 30-degree melee cone, striking several enemies at once." else "Sweeps a melee cone, striking several enemies at once. A versatile close-range weapon."
        "LANCE" -> if(level>=4) "Thrusts along an expanded line, 35% wider than before. Can hit enemies behind the first target." else "Thrusts along a long, narrow line and can hit enemies behind the first target. Strong reach, limited width."
        "SHIELD" -> "Bashes enemies in front of you. Damage scales with Armor Class rather than Strength."+if(level>=4)" The upgrade adds 15% of Strength as flat damage." else ""
        "DAGGER" -> "Throws a small dagger at its target, even at close range. Fast ranged pressure with no separate melee attack."+if(level>=4)" Critical-hit damage multiplier is increased by 50%." else ""
        "LONGBOW" -> if(level>=4) "Fires arrows that can ricochet to another target." else "Fires targeted arrows over a long distance. Reliable ranged pressure against individual enemies."
        "CROSSBOW" -> if(level>=4) "Fires piercing bolts with no damage loss after the first hit and longer post-hit travel." else "Fires slow, powerful piercing bolts. Hits after the first deal 50% damage; lining enemies up rewards each shot."
        "WARHAMMER" -> "A slow hammer strike deals full damage with its head and 33% with its shaft. Overlap uses only the stronger hit."+if(level>=4)" The shaft is 25% longer and the head radius is 50% larger." else ""
        "WHIP" -> if(level>=4) "Lashes twice at 75% damage per strike. The tip deals full strike damage; the thin lash deals 33%, without double-hitting overlap." else "A long lash ends in a narrow 30-degree cone. The tip deals full damage; the thin lash deals 33%. Overlap never double-hits."
        "SLING" -> if(level>=4) "Stones burst into 3–8 evenly spaced fragments: 50% damage, 80 px range. Fragments cannot hit the original victim or split again." else "Throws small stones with light knockback. Higher impact and slower attacks than a dagger."
        "BOOMERANG" -> if(level>=4) "Hits on outward and returning flights, then makes a half-range rear trip at 50% damage. The next throw can begin after the first catch." else "Throws a slow, hard-hitting returning projectile. Flight timing follows Attack Speed; each flight leg can hit a target once."
        "STEAM_WAND" -> if(level>=4) "Fires a five-projectile fan: the center deals 70% normal spell damage and each side projectile 25%." else "Launches a focused magical projectile. Later levels trade a single hit for a spreading fan."
        "AMULET_OF_TROLL_KING" -> if(level>=4) "Fires Water projectiles. Its acid pool remains a separate active ability." else "Randomly fires Water or Acid projectiles. Its acid pool rewards keeping enemies inside an area."
        "ORB_OF_SKY" -> if(level>=4) "Fires Lightning projectiles with up to four ricochets. Chained hits deal 50% damage." else "Randomly fires Air or Lightning. Lightning can chain once at 50% damage."
        "IVY_STAFF" -> if(level>=4) "Fires larger Poison projectiles that inflict Armor Break (−1 Armor Class for 3s). Summons add independent pressure." else "Randomly fires Acid or Poison projectiles. Summons add independent pressure."
        "TOME_OF_ERYNDOR" -> if(level>=4) "Replaces its projectiles with a Fire beam at 85% of normal range. Targets after the first take 75% damage." else "Randomly alternates Fire and Necrotic projectiles. A heavy spell focus with a passive kill explosion."
        "TOTEM_OF_DARK_CLOUDS" -> if(level>=4) "Fires Necrotic projectiles, replacing every fourth attack with a Necrotic drain at 135% normal spell damage." else "Fires magical projectiles before developing its Necrotic drain attack."
        "ENCHANTED_CHALK_DUST" -> if(level>=4) "Fires one central projectile at 70% normal spell damage. It can bounce off walls twice; no side projectiles are created." else "Fires a single magical projectile. Its chosen drawn spell provides a different area effect."
        else -> w.description
    }

    fun ability(w:WeaponDefinition,upgraded:Boolean=false):String = when(key(w)) {
        "DOUBLE_SWORDS" -> "Rapid Strokes: delivers ${if(upgraded)7 else 5} rapid strikes around an in-range target."
        "LONGSWORD" -> "Air Slash: sends a broad Air wave forward."+if(upgraded)" Longer reach and increased damage against wounded targets." else ""
        "LANCE" -> "Spin Attack: sweeps a circle around the weapon, damaging nearby enemies."+if(upgraded)" Larger area and stronger hits." else ""
        "SHIELD" -> "Defensive Line: after standing still for 3s, gain 1 Armor Class per second, up to 15 with one shield. Moving clears the bonus. At level 5, Defensive Burst consumes the stacked Armor Class bonus for a blast whose damage scales with Armor Class and the consumed stacks."
        "DAGGER" -> "Knife Swarm: instantly throws six daggers around you. ${if(upgraded)"Repeats once with a rotated spread." else "Each dagger can score a critical hit."}"
        "LONGBOW" -> "Leisurely Shot: releases five arrows along separate trajectories. ${if(upgraded)"Repeats the volley after 0.5s; arrows can ricochet twice." else "The level-4 basic upgrade adds one ricochet."}"
        "CROSSBOW" -> "Heavy Bolts: fires an oversized piercing bolt. ${if(upgraded)"Deals more damage and can carry its first normal target." else "Deals increased damage to bosses."}"
        "WARHAMMER" -> "Earthbreaker: sends ${if(upgraded)4 else 3} overlapping impacts forward. Briefly stuns ordinary enemies and slows bosses."
        "WHIP" -> "Whip Sweep: strikes a broad ${if(upgraded)120 else 80}-degree arc in one sweep."
        "SLING" -> "Shatterstone: drops a large boulder at the aimed location. ${if(upgraded)"A larger impact is followed by one forward bounce." else "One landing, with no bounce."}"
        "BOOMERANG" -> "Guided Hunt: launches an extra boomerang and guides ALL boomerangs from this weapon copy toward the cursor for ${if(upgraded)6 else 5}s during outward and rear trips. Return flights are not guided; travel limits remain."
        "STEAM_WAND" -> "Ignite Dash: teleports to the aimed point, then explodes. The inner circle hits hardest; the outer ring deals 40% of that damage."
        "AMULET_OF_TROLL_KING" -> "Acidic Pool: places a lingering Acid field that damages and corrodes enemies inside it."
        "ORB_OF_SKY" -> "Vortex Pull: pulls ordinary enemies inward and bends projectile paths."
        "IVY_STAFF" -> "Choose to Summon: select animals, an Imp, or the neutral Oathbreaker."
        "TOME_OF_ERYNDOR" -> "Erasing the Souls (passive): nearby eligible kills explode in a 100 px radius and apply Necrosis. Explosion kills do not create another explosion."
        "TOTEM_OF_DARK_CLOUDS" -> "Static Clouds: surrounds you with a Lightning storm. Activate again while it is running to leave it fixed in place for the remaining duration."
        "ENCHANTED_CHALK_DUST" -> "Glyph Casting: finishes a drawing, then casts the chosen spell: Call Lightning, Meteor, Tsunami or Singularity. Each has its own pattern."
        else -> w.weaponAbility?.displayName ?: "No active ability."
    }
    fun hover(w:WeaponDefinition,level:Int?=null):String {
        return (level?.let{"Level ${it.coerceIn(1,5)}\n"} ?: "")+overview(w,level ?: 1)
    }

    fun hover(weapon:com.azizoglu.grisilith.weapon.runtime.WeaponInstance):String {
        var copy=hover(weapon.definition,weapon.level)
        weapon.ivySummonChoice?.let{ choice ->
            val cooldown=com.azizoglu.grisilith.summon.SummonSpawnScheduler(weapon.runtimeId,choice).cooldownSeconds
            copy=copy.replace(timing(weapon.definition,weapon.level),"Active • cooldown ${f(cooldown)}s after the summon wave ends")
            copy+="\nSelected summon: ${choice.displayName}"
        }
        weapon.chalkDustSpellChoice?.let{copy+="\nSelected spell: ${it.displayName}"}
        return copy
    }

    fun damageTypesAtLevel(w:WeaponDefinition,level:Int):Set<DamageType> =
        if(w.statsForLevel(level).basicAttackUpgraded && w.upgradedBasicAttackDamageType!=null)
            setOf(w.upgradedBasicAttackDamageType) else w.damageTypes

    fun attackType(w:WeaponDefinition,level:Int=1):String {
        val base=when(key(w)) {
            "DOUBLE_SWORDS" -> "Slashing"
            "LONGSWORD", "SHIELD" -> "Cone"
            "LANCE" -> "Line"
            "WARHAMMER" -> "Line + Circle"
            "WHIP" -> "Line + Cone"
            "BOOMERANG" -> "Returning projectile"
            else -> "Projectile"
        }
        val change=basicUpgrade(w)
        val current=when {
            level>=4 && key(w)=="TOME_OF_ERYNDOR" -> "Beam"
            level>=4 && key(w)=="TOTEM_OF_DARK_CLOUDS" -> "Projectile + Drain"
            else -> base
        }
        return "$current (${if(level>=4)"upgraded: " else "Lv4: "}$change)"
    }

    private fun basicUpgrade(w:WeaponDefinition):String=when(key(w)) {
        "DOUBLE_SWORDS" -> "paired cross-slashes; larger reach"
        "LONGSWORD" -> "longer reach; 30° cone"
        "LANCE" -> "line width +35%"
        "SHIELD" -> "adds 15% Strength as flat damage"
        "DAGGER" -> "critical damage multiplier ×1.5"
        "LONGBOW" -> "one ricochet"
        "CROSSBOW" -> "no piercing damage loss; longer travel"
        "WARHAMMER" -> "shaft +25%; head radius +50%"
        "WHIP" -> "two 75% hits; larger tip cone"
        "SLING" -> "3–8 fragments at 50% damage, range 80"
        "BOOMERANG" -> "half-range rear trip at 50% damage"
        "STEAM_WAND" -> "five-shot fan: center 70%, sides 25%"
        "AMULET_OF_TROLL_KING" -> "Water only"
        "ORB_OF_SKY" -> "Lightning only; four ricochets at 50% damage"
        "IVY_STAFF" -> "Poison; larger shot; Armor Break for 3s"
        "TOME_OF_ERYNDOR" -> "Fire beam; range ×0.85; later targets 75%"
        "TOTEM_OF_DARK_CLOUDS" -> "Necrotic; every fourth attack drains at 135%"
        "ENCHANTED_CHALK_DUST" -> "one 70% shot; two wall bounces"
        else -> "enhanced normal attack"
    }

    fun abilityUpgrade(w:WeaponDefinition):String=when(key(w)) {
        "DOUBLE_SWORDS" -> "7 strikes instead of 5"
        "LONGSWORD" -> "damage N ×2 → ×2.5; range 350 → 450; wounded-target bonus"
        "LANCE" -> "damage N ×2 → ×2.5; spin radius +33.3%"
        "SHIELD" -> "unlocks the active Defensive Burst"
        "DAGGER" -> "second six-dagger wave"
        "LONGBOW" -> "second volley; two ricochets per arrow"
        "CROSSBOW" -> "damage N ×1.5 → ×2; boss bonus ×1.25 → ×1.5; carries a target"
        "WARHAMMER" -> "damage N ×1.5 → ×2; 3 → 4 impacts; radius +20%"
        "WHIP" -> "damage N ×2 → ×2.5; arc 80° → 120°"
        "SLING" -> "damage N ×3 → ×3.5; one ×2.5 bounce; size +25%"
        "BOOMERANG" -> "guidance 6s; extra projectile +40%; cooldown 25s"
        "STEAM_WAND" -> "stronger explosion; shorter cooldown"
        "AMULET_OF_TROLL_KING" -> "longer pool duration; shorter cooldown"
        "ORB_OF_SKY" -> "faster damage ticks; shorter cooldown"
        "IVY_STAFF" -> "stronger Shepherd scaling for summons"
        "TOME_OF_ERYNDOR" -> "explosion ×2; trigger range 600"
        "TOTEM_OF_DARK_CLOUDS" -> "duration 12s; cooldown 38s"
        "ENCHANTED_CHALK_DUST" -> "stronger chosen spell; longer Singularity"
        else -> "enhanced ability"
    }

    fun scalings(w:WeaponDefinition,level:Int=1):String {
        val types=damageTypesAtLevel(w,level)
        fun related(type:DamageType)=when(type) {
            DamageType.FIRE,DamageType.WATER,DamageType.AIR,DamageType.EARTH -> StatType.ELEMENTAL_DAMAGE
            DamageType.POISON -> StatType.SHEPHERD
            else -> StatType.MIGHT_OF_NATURE
        }
        val orderedTypes=listOfNotNull(w.upgradedBasicAttackDamageType).filter{it in types}+types.filter{it!=w.upgradedBasicAttackDamageType}
        val normal=if(ContentTag.SPELL_FOCUS in w.tags) listOf(StatType.WISDOM)+orderedTypes.map(::related).distinct()
            else listOf(primary(w))+w.orderedDamageScaling().map{it.first}.filter{it!=primary(w)}+
                if(key(w)=="SHIELD" && level>=4)listOf(StatType.STRENGTH) else emptyList()
        return normal.distinct().joinToString(" > "){it.displayName}
    }

    fun abilityLine(w:WeaponDefinition,level:Int=1):String {
        val actual=level.coerceIn(1,5)
        val unlocked=w.statsForLevel(actual).weaponAbilityUnlocked
        val prefix=if(unlocked)"" else "Lv3 unlock: "
        val copy=if(key(w)=="LONGBOW" && actual==4)"Leisurely Shot: releases five arrows, each able to ricochet once." else ability(w,actual>=5)
        return "$prefix$copy (${if(actual>=5)"upgraded: " else "Lv5: "}${abilityUpgrade(w)}). ${timing(w,maxOf(3,actual))}"
    }

    /** Definition values adjusted by weapon level, never pretending to be live build damage. */
    fun overview(w:WeaponDefinition,level:Int=1):String {
        val s=w.statsForLevel(level)
        val range=(w.secondaryRange ?: w.baseRange)*s.rangeMultiplier*if(level>=4 && key(w)=="TOME_OF_ERYNDOR") .85f else 1f
        val category=if(ContentTag.MELEE_WEAPON in w.tags)"melee" else "ranged"
        return listOf(
            "Attack Type: ${attackType(w,level)}",
            "Damage: ${f(w.baseDamage*s.damageMultiplier)} (base)",
            "Base Attack Speed: ${f(w.baseAttacksPerSecond*s.attackSpeedMultiplier)}/s",
            "Range: ${f(range)} px ($category)",
            "Scaling: ${scalings(w,level)}",
            "Damage Type: ${damageTypesAtLevel(w,level).joinToString(" / "){it.displayName}}",
            "Ability: ${abilityLine(w,level)}"
        ).joinToString("\n")
    }
    fun primary(w:WeaponDefinition)=when {
        key(w)=="SHIELD" -> StatType.ARMOR_CLASS
        ContentTag.SPELL_FOCUS in w.tags -> StatType.WISDOM
        ContentTag.RANGED_WEAPON in w.tags -> StatType.DEXTERITY
        else -> StatType.STRENGTH
    }
    fun scaling(w:WeaponDefinition):String {
        val main=primary(w)
        val normal=when {
            key(w)=="SHIELD" -> "N = round(Base Damage × level damage multiplier × Armor Class × 0.45)."
            ContentTag.SPELL_FOCUS in w.tags -> "N = round(Base Damage × level damage multiplier × (0.70 × Wisdom + 0.50 × related stat))."
            else -> {
                val secondary=w.damageScaling.filterKeys{it!=main}
                val bonus=if(secondary.isEmpty()) "" else " × max(0.20, 1 + ${secondary.entries.joinToString(" + "){"0.08 × ${f(it.value)} × (${it.key.displayName} - 10)"}})"
                "N = round(Base Damage × level damage multiplier × ${main.displayName}$bonus)."
            }
        }
        return "$normal\n\nAbility scaling\n${abilityScaling(w)}\n\nN is the non-critical normal damage foundation before attack-pattern coefficients and target defenses. S(type) = Base Damage × (Wisdom + related stat), without a level damage multiplier. Fire/Water/Air use Elemental Damage; Lightning/Acid/Necrotic use Might of Nature; Poison uses Shepherd. Results have a minimum of 1. Physical weapons' attacks and abilities can crit; spell-focus attacks do not use that critical rule. Item, trait and target-defense modifiers are separate."
    }
    fun abilityScaling(w:WeaponDefinition):String=when(key(w)) {
        "DOUBLE_SWORDS" -> "Each strike uses normal melee-hit damage; 5 strikes, or 7 at level 5. This is per strike, not a single total-damage value."
        "LONGSWORD" -> "Air Slash = round(N × 2), or N × 2.5 at level 5. Level 5 also multiplies damage by 1 + 0.003 × target missing-HP percentage, capped at 70 percentage points."
        "LANCE" -> "Spin = round(N × 2), or N × 2.5 at level 5. Radius = normal range × 0.66 × 0.75; level 5 replaces 0.75 with 1."
        "SHIELD" -> "Burst = round(Base Damage × (current Armor Class + captured Defensive Line stacks) × 0.45). Radius: 190 px. Separately, the level-4 normal attack adds flat damage equal to 0.15 × Strength after its hit roll."
        "DAGGER" -> "Knife Swarm retains its separate Strength-based physical formula, with Dexterity as a secondary stat if defined. Six projectiles; level 5 repeats the six-projectile wave. Each projectile rolls its own hit."
        "LONGBOW" -> "Each arrow deals N; five arrows per volley. No ricochet at level 3; one at level 4. Level 5 fires a second volley and grants two ricochets per arrow."
        "CROSSBOW" -> "Bolt = round(N × 1.5), or N × 2 at level 5. Mini/Final Boss hits gain a further ×1.25, or ×1.5 at level 5. Subsequent bolt hits do not progressively lose damage."
        "WARHAMMER" -> "Each impact = round(N × 1.5), or N × 2 at level 5. Three circles of radius 70 px; level 5 has four circles of radius 84 px. A target can be hit by overlapping separate impacts."
        "WHIP" -> "Sweep = round(N × 2), or N × 2.5 at level 5. Normal shaft = 33% of the strike; level 4 has two strikes at 75% each, never adding shaft and cone damage for one strike."
        "SLING" -> "Landing = round(N × 3). At level 5: first landing N × 3.5, bounce N × 2.5. Impact radius: 75 px, rising to 93.75 px at level 5. Normal fragments deal N × 0.5."
        "BOOMERANG" -> "Each normal outward/return leg deals N. Level-4 rear legs deal N × 0.5. Guided Hunt adds an independent projectile; it guides existing outward flights without resetting range or lifetime."
        "STEAM_WAND" -> "Inner explosion = S(Fire) × 7.9125; level 5 uses 10.55. Outer explosion = inner × 0.40. Inner/outer radii: 120/250 px. From level 4 the normal fan is N × 0.70 in the center and N × 0.25 per side shot."
        "AMULET_OF_TROLL_KING" -> "Each pool tick = round(target maximum HP × (Wisdom + Might of Nature) × 0.00015), every 0.25s. Radius: 165 px. This is per tick, not DPS or total pool damage."
        "ORB_OF_SKY" -> "Each vortex tick = round(target maximum HP × (Wisdom + Elemental Damage) × 0.00010). Tick interval: 0.25s, or about 0.167s at level 5. Radius: 195 px."
        "IVY_STAFF" -> "Let P = capped Shepherd × 0.70; at level 5 use ×1.40 for animals/Imp or ×1.55 for Oathbreaker. Summon damage multiplier = 1 + 0.020P; health = 1 + 0.015P; attack speed = 1 + 0.008P; movement speed = 1 + 0.004P. These multiply summon-specific stage baselines, not the staff's projectile damage. Item modifiers are separate."
        "TOME_OF_ERYNDOR" -> "Passive explosion = round(S(Necrotic) × 1.25), or × 2 at level 5. Radius: 100 px. Trigger range: 450 px, or 600 px at level 5. Does not recursively trigger itself."
        "TOTEM_OF_DARK_CLOUDS" -> "Each Lightning tick = round(target maximum HP × (Wisdom + Might of Nature) × 0.000165), every approximately 0.333s. Radius: 145 px. From level 4 every fourth normal attack becomes a Necrotic drain dealing N × 1.35."
        "ENCHANTED_CHALK_DUST" -> "Call Lightning inner: S(Lightning) × 7.5 / 10.45 at level 5; outer × 0.72 of inner; chain × 0.31 of outer. Meteor: S(Fire) × 10 / 15.55. Tsunami: S(Water) × 7.5 / 11.11. Singularity tick: target maximum HP × (Wisdom + Might of Nature) × 0.00020; collapse: S(Necrotic) × effective deaths × 1.55. Each tick/strike is separate."
        else -> "No separate ability damage."
    }
    fun baseStats(w:WeaponDefinition):String=overview(w)+"\n\nBase values are before character, item and other build modifiers. Base Attack Speed is attacks per second, not the Attack Speed stat."
    fun timing(w:WeaponDefinition,level:Int=3):String {
        if(key(w)=="ENCHANTED_CHALK_DUST")return "Drawing: 2.5s • shared spell cooldown: 30s"
        if(key(w)=="IVY_STAFF")return "Active • cooldown 20–30s depending on chosen summon (after it expires)"
        if(key(w)=="SHIELD")return if(level>=5)"Passive + active Defensive Burst • cooldown 35s" else "Passive (active Defensive Burst at level 5) • burst cooldown 35s"
        val stats=w.statsForLevel(level)
        val a=if(stats.weaponAbilityUpgraded)w.upgradedWeaponAbility ?: w.weaponAbility else w.weaponAbility
        return if(a==null) "No active ability" else buildString {
            append(a.activationType.name.lowercase().replaceFirstChar{it.titlecase()})
            if(a.durationSeconds>0 && key(w) in setOf("BOOMERANG","ORB_OF_SKY","AMULET_OF_TROLL_KING","TOTEM_OF_DARK_CLOUDS"))append(" • duration ${f(a.durationSeconds)}s")
            if(a.cooldownSeconds>0) append(" • cooldown ${f(a.cooldownSeconds)}s") else append(" • no active cooldown")
        }
    }
    fun upgrades(w:WeaponDefinition):String="Only changes newly gained at each level are listed.\n\n"+(2..5).joinToString("\n\n") { level ->
        val s=w.statsForLevel(level)
        val previous=w.statsForLevel(level-1)
        "Level $level\n"+buildList {
            val currentBonuses=s.grantedStatModifiers.groupBy{it.statType}.mapValues{(_,mods)->mods.sumOf{it.amount}}
            val oldBonuses=previous.grantedStatModifiers.groupBy{it.statType}.mapValues{(_,mods)->mods.sumOf{it.amount}}
            val changes=(currentBonuses.keys+oldBonuses.keys).mapNotNull { stat ->
                val delta=(currentBonuses[stat] ?: 0)-(oldBonuses[stat] ?: 0)
                if(delta==0)null else "${stat.displayName} ${if(delta>0)"+" else ""}$delta"
            }
            if(changes.isNotEmpty())add(changes.joinToString(", "))
            fun multiplier(label:String,current:Float,old:Float) {
                if(current!=old)add("$label ${if(current>old)"+" else ""}${f(kotlin.math.round((current/old-1f)*1000f)/10f)}% from previous level")
            }
            multiplier("Base Attack Speed",s.attackSpeedMultiplier,previous.attackSpeedMultiplier)
            multiplier("Normal range",s.rangeMultiplier,previous.rangeMultiplier)
            if(previous.knockbackMultiplier>0f)multiplier("Knockback",s.knockbackMultiplier,previous.knockbackMultiplier)
            val targets=s.additionalTargets-previous.additionalTargets
            if(targets!=0)add("Additional targets: ${if(targets>0)"+" else ""}$targets")
            multiplier("Normal damage",s.damageMultiplier,previous.damageMultiplier)
            if(s.basicAttackUpgraded && !previous.basicAttackUpgraded)add("Normal attack: ${basicUpgrade(w)}")
            if(s.weaponAbilityUnlocked && !previous.weaponAbilityUnlocked)add("Ability unlocked: ${ability(w)} ${timing(w,level)}")
            if(s.weaponAbilityUpgraded && !previous.weaponAbilityUpgraded)add("Ability upgrade: ${abilityUpgrade(w)}. ${timing(w,level)}")
            if(key(w)=="LONGBOW" && level==4)add("Ability arrows gain one ricochet.")
        }.ifEmpty{listOf("No additional change.")}.joinToString("\n")
    }
    fun choices(w:WeaponDefinition):String=when(key(w)) {
        "ENCHANTED_CHALK_DUST" -> "Call Lightning: two-ring Lightning strike with chaining.\n\nMeteor: a Fire impact that burns targets.\n\nTsunami: a Water wave starts at a random map edge, travels toward the player's cast-time position, then exits the map; no preparation marker.\n\nSingularity: a Necrotic pull field followed by a death-scaled collapse.\n\nThe drawing finishes before casting. Prepared/instant modes use the mouse position at completion where applicable; Auto uses an eligible target."
        "IVY_STAFF" -> com.azizoglu.grisilith.weapon.summon.IvySummonChoice.entries.joinToString("\n\n"){"${it.displayName}\n${it.description}"}
        else -> "No additional spell or summon selection."
    }
    fun hasChoices(w:WeaponDefinition)=key(w) in setOf("ENCHANTED_CHALK_DUST","IVY_STAFF")
    fun f(n:Float)=if(n==n.toInt().toFloat())n.toInt().toString() else String.format(Locale.ROOT,"%.3f",n).trimEnd('0').trimEnd('.')
}
