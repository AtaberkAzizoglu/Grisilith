package qa

import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.*
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.runtime.executor.*
import com.azizoglu.grisilith.weapon.skill.*
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt

object PhysicalWeaponSmoke {
    private fun field(o: Any, n: String): Any = o.javaClass.getDeclaredField(n).apply { isAccessible = true }.get(o)
    private fun damage(p: Projectile): Int = field(p, "damage") as Int
    private fun invoke(o: Any, n: String, vararg args: Any) = o.javaClass.declaredMethods.first {
        it.name == n && it.parameterCount == args.size
    }.apply { isAccessible = true }.invoke(o, *args)

    @JvmStatic fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(320, 240); setInitialVisible(false); disableAudio(true)
            setPreferencesConfig("../tools/physical-weapon-smoke/prefs", Files.FileType.Local)
        }
        Lwjgl3Application(object : ApplicationAdapter() {
            override fun create() {
                try {
                    val stats = PlayerStats()
                    val calc = WeaponDamageCalculator(stats)
                    val selector = WeaponTargetSelector()
                    val player = Player(300f, 300f, 3000f, 3000f, stats)
                    val enemies = EnemyManager()
                    val shots = mutableListOf<Projectile>()
                    fun enemy(distance: Float): Enemy = Enemy(player.centerX+distance-10f, player.centerY-10f,
                        EnemyStats(EnemyType.NORMAL, 100000, 0, 0f, 20f, 0f))
                    fun ctx(w: WeaponInstance) = WeaponAttackContext(w, 0, player, player.centerX,
                        player.centerY, enemies, shots, { _, _ -> }, {}, {}, { _, d -> d }, { it }, { false })
                    fun reset() { enemies.allEnemies.toList().forEach { enemies.remove(it) }; shots.clear() }
                    fun hit(p: Projectile, e: Enemy) {
                        val pos = field(p, "position") as Vector2
                        pos.set(e.centerX-7f, e.centerY-7f)
                        (field(p,"previousPosition") as Vector2).set(pos)
                        p.checkCollision(listOf(e), {}, {}, {}, { _, d -> d })
                    }

                    val physical = listOf(DefaultWeapons.doubleSwords, DefaultWeapons.longsword, DefaultWeapons.lance,
                        DefaultWeapons.shield, DefaultWeapons.dagger, DefaultWeapons.longbow, DefaultWeapons.crossbow)
                    physical.forEach { def ->
                        for(l in 1..5) check(WeaponInstance(def,l).currentLevelStats.damageMultiplier == 1f)
                        check(calc.canAbilityCriticallyHit(WeaponInstance(def,5)))
                    }
                    check(!calc.canCriticallyHit(WeaponInstance(DefaultWeapons.longsword,5), DamageType.AIR))
                    check(!calc.canAbilityCriticallyHit(WeaponInstance(DefaultWeapons.ivyStaff,5)))

                    // Spell-focus regression checks use actual executors and active fields.
                    val physicalTypes=listOf(DamageType.SLASHING,DamageType.PIERCING,DamageType.BLUDGEONING)
                    val magicalTypes=listOf(DamageType.AIR,DamageType.WATER,DamageType.FIRE,DamageType.ACID,
                        DamageType.NECROTIC,DamageType.LIGHTNING,DamageType.POISON)
                    check(physicalTypes.all { it.isPhysical && !it.isMagical })
                    check(magicalTypes.all { it.isMagical && !it.isPhysical })
                    val defenseStats=PlayerStats().apply { setArmorBaseValues(1000,3,2) }
                    val defensePlayer=Player(100f,100f,3000f,3000f,defenseStats)
                    physicalTypes.forEach {
                        check(defensePlayer.takeDamage(100,it)==70)
                        check(defensePlayer.takeDamage(100,it,.5f)==85)
                    }
                    magicalTypes.forEach { check(defensePlayer.takeDamage(100,it,.75f)==80) }
                    check(defensePlayer.takePhysicalWeaponDamage(100,.5f,DamageType.SLASHING)==85)
                    check(defensePlayer.takePhysicalWeaponDamage(100,.75f,DamageType.POISON)==80)
                    val defenseEnemy=Enemy(500f,500f,EnemyStats(EnemyType.NORMAL,10000,0,0f,20f,0f,
                        armorClass=10,baseArmor=3,baseMagicResistance=2,magicResistance=10))
                    physicalTypes.forEach {
                        check(defenseEnemy.takeDamage(100,EnemyDamageContext(damageType=it))==70)
                        check(defenseEnemy.takeDamage(100,EnemyDamageContext(damageType=it,attackerPiercingIgnoreRatio=.5f))==85)
                    }
                    magicalTypes.forEach {
                        check(defenseEnemy.takeDamage(100,EnemyDamageContext(damageType=it,attackerPiercingIgnoreRatio=.75f,
                            appliesAutomaticCondition=false))==80)
                    }
                    check(defenseEnemy.takeDamage(1,EnemyDamageContext(damageType=DamageType.SLASHING))==1)
                    val immuneStats=EnemyStats(EnemyType.NORMAL,1000,0,0f,20f,0f,
                        damageAffinities=com.azizoglu.grisilith.combat.affinity.DamageAffinityProfile.of(
                            DamageType.POISON to com.azizoglu.grisilith.combat.affinity.DamageAffinity.IMMUNE))
                    check(Enemy(0f,0f,immuneStats).takeDamage(100,EnemyDamageContext(damageType=DamageType.POISON))==0)
                    com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.all.forEach {
                        val type=com.azizoglu.grisilith.enemy.definition.EnemyAttackDamageTypes.contactDamageType(it.id)
                        check(type.isPhysical || type.isMagical)
                    }
                    val adaptiveStats=PlayerStats()
                    StatType.entries.forEach { adaptiveStats.setBaseScore(it,10) }
                    val adaptiveInventory=WeaponInventory()
                    com.azizoglu.grisilith.enemy.runtime.EnemyAdaptiveScalingRuntime.bind(adaptiveStats,adaptiveInventory)
                    StatType.entries.forEach { adaptiveStats.addRunBonus(it,40) }
                    repeat(6) { check(adaptiveInventory.addWeapon(WeaponInstance(DefaultWeapons.longsword,5))) }
                    val caps=com.azizoglu.grisilith.enemy.runtime.EnemyAdaptiveScalingRuntime.current()
                    check(kotlin.math.abs(caps.healthMultiplier-1.74375f)<.0001f)
                    check(kotlin.math.abs(caps.armorClassMultiplier-1.2603125f)<.0001f)
                    check(kotlin.math.abs(caps.damageMultiplier-1.35f)<.0001f)
                    check(kotlin.math.abs(caps.attackSpeedMultiplier-1.065625f)<.0001f)
                    com.azizoglu.grisilith.enemy.runtime.EnemyAdaptiveScalingRuntime.clear()
                    val assignedResistance=listOf(
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.grayWolf,2,4),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.boar,2,4),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.baldEagle,2,6),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.grizzlyBear,3,6),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.beeSwarm,2,5),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.crocodile,3,5),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.silverbackGorilla,5,10),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.bandit,4,8),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.archer,4,8),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.spearman,5,13),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.horseman,6,16),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.witch,8,25),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.skeletonWarrior,5,10),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.skeletonArcher,5,11),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.zombie,6,13),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.slime,8,8),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.ghost,9,14),
                        Triple(com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.vampireLord,10,22)
                    )
                    for((definition,baseMR,mrScore) in assignedResistance) {
                        check(definition.baseStats.baseMagicResistance==baseMR && definition.baseStats.magicResistance==mrScore)
                        val types=definition.allowedTypes.toList() +
                            if(EnemyType.NORMAL in definition.allowedTypes) listOf(EnemyType.UNIQUE) else emptyList()
                        for(stage in listOf(1,5,25,35,50)) for(type in types) {
                            val resolved=com.azizoglu.grisilith.enemy.runtime.EnemyKindStatScaler.resolve(definition,type,stage)
                            val p=stage-1
                            val stageArmor=1f+p*0.0225f+kotlin.math.floor(p/5f)*0.06f
                            val bossFactor=when(type) {EnemyType.MINI_BOSS->1f; EnemyType.FINAL_BOSS->1.25f; else->.35f}
                            val familyArmor=when {
                                com.azizoglu.grisilith.enemy.definition.EnemyTag.HUMANOID in definition.enemyTags -> 1f+p*.0015f*bossFactor
                                com.azizoglu.grisilith.enemy.definition.EnemyTag.UNDEAD in definition.enemyTags -> 1f+p*.0012f*bossFactor
                                else -> 1f
                            }
                            val typeArmor=com.azizoglu.grisilith.enemy.definition.DefaultEnemyTypeModifiers.forType(type).armorMultiplier
                            val expected=(mrScore*stageArmor*familyArmor*typeArmor).roundToInt()
                            check(resolved.baseMagicResistance==baseMR && resolved.magicResistance==expected)
                            val probe=Enemy(500f,500f,resolved.copy(maximumHealth=1000000))
                            check(probe.takeDamage(10000,EnemyDamageContext(damageType=DamageType.AIR,
                                attackerPiercingIgnoreRatio=.75f,appliesAutomaticCondition=false))==(10000-baseMR*expected).coerceAtLeast(1))
                        }
                    }
                    val factory=com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory(
                        com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.createRegistry())
                    for((map,baseMR,mrScore) in listOf(
                        Triple(com.azizoglu.grisilith.config.GameMap.ROAD_TO_HOME,2,4),
                        Triple(com.azizoglu.grisilith.config.GameMap.TEST_MAP,2,4),
                        Triple(com.azizoglu.grisilith.config.GameMap.TROUBLE_AT_HOME,4,8),
                        Triple(com.azizoglu.grisilith.config.GameMap.FOLLOWING_THE_DUNGEON_DELVER,6,13))) {
                        val mimic=factory.createMimic(map,1,500f,500f)
                        val mimicStats=field(mimic,"stats") as EnemyStats
                        check(mimicStats.baseMagicResistance==baseMR && mimicStats.magicResistance==(mrScore*1.7f).roundToInt())
                    }
                    val dummyStats=field(factory.createTestDummy(35,500f,500f),"stats") as EnemyStats
                    check(dummyStats.baseMagicResistance==0 && dummyStats.magicResistance==0)
                    println("ENEMY_MR_ASSIGNMENTS_PASS: all 18 assignments, stages1/5/25/35/50, normal/unique/boss scaling, actual magical reduction without piercing, map-derived Mimics and zero-defense dummy.")
                    println("DAMAGE_DEFENSE_SMOKE_PASS: three physical / seven magical types, player and enemy armor / MR, piercing ignored by magic, no double defense, minimum damage / immunity preserved, all enemy contacts typed, adaptive caps reduced 12.5%.")
                    check(DamageType.POISON.isMagical && !DamageType.POISON.canCriticallyHit)
                    val defendedStats=PlayerStats().apply { setArmorBaseValues(100,0,2) }
                    val defendedPlayer=Player(100f,100f,3000f,3000f,defendedStats)
                    check(defendedPlayer.takeDamage(100,DamageType.POISON)==
                        (100-defendedStats.rawMagicDamageReduction()).roundToInt().coerceAtLeast(1))
                    val summon=com.azizoglu.grisilith.summon.SummonedEntity("test","test","test",
                        com.azizoglu.grisilith.summon.SummonType.COW,
                        com.azizoglu.grisilith.summon.SummonStats(1000,1,0f,1f,1f,magicResistance=20),
                        100f,100f,15f)
                    check(summon.takeDamage(100,DamageType.POISON)==80)
                    val spellExecutor=ProjectileWeaponAttackExecutor(calc,selector,stats)
                    reset(); val spellTarget=enemy(150f); enemies.spawn(spellTarget)
                    val steam=WeaponInstance(DefaultWeapons.steamWand,4)
                    repeat(3) { spellExecutor.execute(ctx(steam)) }
                    check(shots.map(::damage)==listOf(216,216,54,54,151,54,54))
                    reset(); enemies.spawn(spellTarget)
                    spellExecutor.execute(ctx(WeaponInstance(DefaultWeapons.enchantedChalkDust,4)))
                    check(shots.size==1 && damage(shots.single())==101)
                    check(field(shots.single(),"remainingWallBounces")==2)
                    check(field(shots.single(),"safeMaximumTravelDistance")==1620f)
                    val boss=Enemy(player.centerX+60f,player.centerY,
                        EnemyStats(EnemyType.MINI_BOSS,100000,0,0f,20f,0f))
                    val finalBoss=Enemy(player.centerX+100f,player.centerY,
                        EnemyStats(EnemyType.FINAL_BOSS,100000,0,0f,20f,0f))
                    val vortex=VortexPullRuntime()
                    check(vortex.tryBegin(player.centerX,player.centerY,listOf(boss,finalBoss,spellTarget),3000f,3000f,true))
                    for(plague in listOf(false,true)) {
                        if(plague) check(vortex.activatePlagueStorm())
                        check(vortex.movementMultiplier(boss)==0.90f && vortex.movementMultiplier(finalBoss)==0.90f)
                        check(VortexPullTargetRules.pullMultiplier(boss,1)==0f)
                        var pulls=0
                        vortex.update(1f/60f,listOf(boss,finalBoss,spellTarget),1,50f,
                            { e,_,_,_,_ -> check(e.type==EnemyType.NORMAL); pulls++ },{_,_->},{})
                        check(pulls==1)
                        val area=vortex.field!!
                        val shot=Projectile(area.center.x-70f,area.center.y+60f,
                            area.center.x+500f,area.center.y+60f,damage=10,maximumTravelDistance=600f,
                            projectileSpeed=500f,customVisualId="AIR_SLASH",sourceWeaponName="Air Slash test",
                            isCriticalHit=false,maximumTargets=1,damageType=DamageType.AIR)
                        val before=Vector2(shot.visualVelocityX,shot.visualVelocityY).len()
                        vortex.bendProjectiles(listOf(shot),.1f)
                        check(shot.visualVelocityY<0f && kotlin.math.abs(Vector2(shot.visualVelocityX,shot.visualVelocityY).len()-before)<.01f)
                        // Actual enemy-arrow update, not just the shared player projectile path.
                        val archer=Enemy(area.center.x-80f,area.center.y+50f,
                            EnemyStats(EnemyType.NORMAL,1000,10,0f,20f,0f))
                        var enemyBends=0
                        archer.projectileVelocityBender={x,y,v,dt -> enemyBends++; vortex.bendProjectileVelocity(x,y,v,dt)}
                        val trait=Class.forName("com.azizoglu.grisilith.enemy.trait.AimedArrowEnemyTrait")
                            .getDeclaredConstructor().apply { isAccessible=true }.newInstance()
                        trait.javaClass.getDeclaredField("aimX").apply { isAccessible=true }.setFloat(trait,area.center.x+500f)
                        trait.javaClass.getDeclaredField("aimY").apply { isAccessible=true }.setFloat(trait,archer.centerY)
                        val traitContext=com.azizoglu.grisilith.enemy.trait.EnemyTraitUpdateContext(
                            archer,emptyList(),.1f,player.centerX,player.centerY,player.bounds,3000f,3000f,emptyList())
                        invoke(trait,"fire",traitContext); invoke(trait,"update",traitContext)
                        val enemyShot=(field(trait,"projectiles") as List<*>).single()!!
                        check(enemyBends==1 && (field(enemyShot,"velocityY") as Float)<0f)
                        // A glyph Tsunami is a separate moving-wave representation; verify its hook too.
                        val glyph=com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime(stats)
                        glyph.projectileVelocityBender=vortex::bendProjectileVelocity
                        glyph.cast(com.azizoglu.grisilith.weapon.glyph.GlyphCastRequest(
                            com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI,area.center.x,area.center.y,
                            WeaponInstance(DefaultWeapons.enchantedChalkDust,5)),enemies,3000f,3000f,{_,_->},{})
                        val wave=field(glyph,"tsunamiWave")
                        (field(wave,"center") as Vector2).set(area.center.x-70f,area.center.y+60f)
                        (field(wave,"direction") as Vector2).set(1f,0f)
                        glyph.update(.1f,player,enemies,emptyList(),3000f,3000f,{_,_->},{})
                        check((field(wave,"direction") as Vector2).y<0f)
                    }
                    vortex.clear(); check(vortex.movementMultiplier(boss)==1f)
                    val corpse=Enemy(player.centerX,player.centerY,EnemyStats(EnemyType.NORMAL,80,0,0f,20f,0f))
                    val victim=Enemy(player.centerX+80f,player.centerY,EnemyStats(EnemyType.NORMAL,80,0,0f,20f,0f))
                    val beyond=Enemy(player.centerX+180f,player.centerY,EnemyStats(EnemyType.NORMAL,80,0,0f,20f,0f))
                    corpse.takeDamage(100)
                    var explosions=0
                    val deaths=ErasingSoulsRuntime().resolveExplosion(corpse,WeaponInstance(DefaultWeapons.tomeOfEryndor,3),
                        player.centerX,player.centerY,listOf(corpse,victim,beyond),100,stats,{_,_->},{explosions++})
                    check(deaths==listOf(victim) && !beyond.isDead && explosions==1)
                    println("SPELL_FOCUS_SMOKE_PASS: magical Poison defense, Steam fan ratios, one unchanged-damage bouncing Chalk shot, no boss pull / reversible 10% slow in Vortex and Plague Storm, ability projectile bending with preserved speed, non-chaining Tome explosion.")

                    stats.setBaseScore(StatType.STRENGTH,20)
                    val dagger = DaggerAttackExecutor(calc, selector, stats)
                    for(distance in listOf(25f,300f)) {
                        reset(); val e=enemy(distance); enemies.spawn(e)
                        dagger.execute(ctx(WeaponInstance(DefaultWeapons.dagger,1)))
                        check(shots.size==1 && damage(shots[0])==90 && e.health.currentHealth==100000)
                    }
                    check(ContentTag.MELEE_WEAPON !in DefaultWeapons.dagger.tags)
                    check(DefaultWeapons.dagger.baseDamage==9)

                    stats.setBaseScore(StatType.CRIT_RATE,0)
                    val projectileExecutor = ProjectileWeaponAttackExecutor(calc,selector,stats)
                    for(l in listOf(1,4)) {
                        reset(); val a=enemy(100f); val b=enemy(200f); enemies.spawn(a); enemies.spawn(b)
                        projectileExecutor.execute(ctx(WeaponInstance(DefaultWeapons.crossbow,l)))
                        val bolt=shots.single(); hit(bolt,a)
                        check(bolt.isActive && a.health.currentHealth==99850)
                        hit(bolt,b)
                        check(!bolt.isActive && b.health.currentHealth==100000-if(l==1)75 else 150)
                    }

                    // Force criticals to verify actual collision/direct damage, not just capability flags.
                    stats.setBaseScore(StatType.CRIT_RATE,25); stats.addRunBonus(StatType.CRIT_RATE,25)
                    stats.setBaseScore(StatType.STRENGTH,10)
                    val runtime = PhysicalWeaponAbilityRuntime(stats,calc,ShortswordAttackExecutor(calc,selector),
                        DefensiveLineRuntime(stats){1})
                    for(l in listOf(3,5)) {
                        reset(); val e=enemy(100f); enemies.spawn(e)
                        invoke(runtime,"fireAirSlash",ctx(WeaponInstance(DefaultWeapons.longsword,l)),e.centerX,e.centerY)
                        val p=shots.single(); val base=if(l==3)220 else 275
                        check(damage(p)==base); hit(p,e)
                        check(100000-e.health.currentHealth==(base*calc.criticalDamageMultiplier()).roundToInt())
                        reset(); val s=enemy(20f); enemies.spawn(s)
                        invoke(runtime,"executeSpinAttack",ctx(WeaponInstance(DefaultWeapons.lance,l)))
                        check(100000-s.health.currentHealth==((120*calc.criticalDamageMultiplier()).roundToInt()*
                            if(l==3)2f else 2.5f).roundToInt())
                    }
                    reset(); val s=enemy(20f); enemies.spawn(s)
                    invoke(runtime,"executeDefensiveBurst",ctx(WeaponInstance(DefaultWeapons.shield,5)),15)
                    val burst=(15*(10+15)*0.45f).roundToInt()
                    check(100000-s.health.currentHealth==(burst*calc.criticalDamageMultiplier()).roundToInt())
                    reset(); stats.setBaseScore(StatType.STRENGTH,20)
                    invoke(runtime,"fireKnifeSwarm",ctx(WeaponInstance(DefaultWeapons.dagger,5)),0f)
                    check(shots.size==6 && shots.all { damage(it)==180 }) // previous formula, not a hidden nerf
                    val knifeTarget=enemy(100f); enemies.spawn(knifeTarget); hit(shots.first(),knifeTarget)
                    check(100000-knifeTarget.health.currentHealth==(180*calc.criticalDamageMultiplier()).roundToInt())
                    stats.setBaseScore(StatType.STRENGTH,10)
                    for(l in listOf(3,5)) {
                        reset(); val bowTarget=enemy(100f); enemies.spawn(bowTarget)
                        invoke(runtime,"fireLongbowVolley",ctx(WeaponInstance(DefaultWeapons.longbow,l)),
                            bowTarget.centerX,bowTarget.centerY,l==5)
                        check(shots.size==5 && shots.all { damage(it)==120 })
                        hit(shots.first(),bowTarget)
                        check(100000-bowTarget.health.currentHealth==(120*calc.criticalDamageMultiplier()).roundToInt())
                        reset(); val boltTarget=enemy(100f); enemies.spawn(boltTarget)
                        invoke(runtime,"fireHeavyBolt",ctx(WeaponInstance(DefaultWeapons.crossbow,l)),
                            boltTarget.centerX,boltTarget.centerY)
                        val heavy=shots.single(); val heavyBase=if(l==3)225 else 300
                        check(damage(heavy)==heavyBase); hit(heavy,boltTarget)
                        check(100000-boltTarget.health.currentHealth==(heavyBase*calc.criticalDamageMultiplier()).roundToInt())
                        reset(); val rapidTarget=enemy(20f); enemies.spawn(rapidTarget)
                        invoke(runtime,"executeRapidStrokes",ctx(WeaponInstance(DefaultWeapons.doubleSwords,l)),
                            rapidTarget.centerX, rapidTarget.centerY)
                        check(100000-rapidTarget.health.currentHealth==(70*calc.criticalDamageMultiplier()).roundToInt() *
                            if(l==3)5 else 7)
                    }
                    runtime.dispose()
                    println("PHYSICAL_WEAPON_SMOKE_PASS: near/far dagger throw, preserved dagger damage, two-target crossbow at Lv1/Lv4, actual critical damage for all seven physical abilities, level damage x1.")
                } catch(t: Throwable) { t.printStackTrace(); kotlin.system.exitProcess(1) }
                Gdx.app.exit()
            }
        },config)
    }
}
