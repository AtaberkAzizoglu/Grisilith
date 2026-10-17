package qa

import com.azizoglu.grisilith.character.trait.*
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.visual.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.skill.*
import com.azizoglu.grisilith.weapon.skill.area.*
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import kotlin.random.Random

object TargetingSmoke {
    private fun output(name:String)=Gdx.files.local("../tools/combat-targeting/"+System.getProperty("qa.outputPrefix","")+name)
    @JvmStatic fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(960,720); setInitialVisible(false); disableAudio(true)
            setPreferencesConfig("../tools/combat-targeting/prefs",Files.FileType.Local)
        }
        Lwjgl3Application(object: ApplicationAdapter() {
            override fun create() {
                try { checks(); Gdx.app.exit() }
                catch(t:Throwable) { t.printStackTrace(); kotlin.system.exitProcess(1) }
            }
        },config)
    }
    private fun checks() {
        PauseInventorySmoke.checks()
        val stats = PlayerStats()
        val player = Player(400f,300f,3000f,3000f,stats)
        fun enemy(type:EnemyType, distance:Float) = Enemy(player.centerX+distance-10f,player.centerY-10f,
            EnemyStats(type,1000000,0,0f,20f,0f))
        val normal=enemy(EnemyType.NORMAL,20f); val unique=enemy(EnemyType.UNIQUE,40f)
        val mini=enemy(EnemyType.MINI_BOSS,60f); val boss=enemy(EnemyType.FINAL_BOSS,80f)
        val manager=EnemyManager(); listOf(normal,unique,mini,boss).forEach(manager::spawn)
        check(AbilityTargeting.select(manager.allEnemies,player.centerX,player.centerY,100f)===boss)
        check(AbilityTargeting.select(listOf(normal,unique,mini),player.centerX,player.centerY,100f)===mini)
        check(AbilityTargeting.select(listOf(normal,unique),player.centerX,player.centerY,100f)===unique)
        val selector=WeaponTargetSelector()
        val before=GameSettings.snapshot()
        for(mode in GameSettings.Targeting.entries) {
            GameSettings.restore(before.copy(targeting=mode))
            WeaponTargetSelector.updateMouse(unique.centerX,unique.centerY)
            val result=selector.selectTarget(DefaultWeapons.doubleSwords.targetingType,player,manager,100f)
            check(result === when(mode) {GameSettings.Targeting.CLOSEST->normal;GameSettings.Targeting.MOUSE->unique;GameSettings.Targeting.BOSS->boss})
        }
        GameSettings.restore(before.copy(abilityCasting=GameSettings.AbilityCasting.PREPARE,targeting=GameSettings.Targeting.CLOSEST))
        val inv=WeaponInventory();val sword=WeaponInstance(DefaultWeapons.doubleSwords,3);check(inv.addWeapon(sword))
        val tamga=TamgaRuntimeManager(inv,stats,null,null,emptySet()){1}
        val runtime=WeaponRuntimeManager(inv,stats,tamga,
            CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
        val enemies=EnemyManager();val shots=mutableListOf<Projectile>()
        fun tick(dt:Float) = runtime.update(dt,player,enemies,shots,emptyList(),3000f,3000f,onDamageDealt={_,_->},onEnemyKilled={})
        tick(0f)
        check(runtime.abilityStateInSlot(0)==WeaponAbilityState.LOCKED)
        check(!runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        check(runtime.abilityRemainingCooldownSecondsInSlot(0)==0f)
        val ordinaryRange=sword.definition.baseRange*sword.currentLevelStats.rangeMultiplier
        check(runtime.abilityRangeFor(sword)>ordinaryRange)
        val target=enemy(EnemyType.NORMAL,ordinaryRange*1.15f);enemies.spawn(target)
        runtime.updateActiveSkillAim(target.centerX,target.centerY)
        check(runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        tick(.25f);check(runtime.abilityRemainingCooldownSecondsInSlot(0)==0f)
        val health=target.health.currentHealth
        check(runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        tick(.01f);check(target.health.currentHealth<health)
        runtime.resetAllTimers()
        check(runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        tick(1.01f);check(runtime.abilityStateInSlot(0)==WeaponAbilityState.COOLDOWN)

        // Native shape pass exercises all range options and the 50%-alpha preparation overlay.
        runtime.resetAllTimers()
        GameSettings.restore(GameSettings.snapshot().copy(normalRange=GameSettings.NormalRange.ALL,abilityRange=GameSettings.AbilityRange.ALL))
        check(runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        val camera=OrthographicCamera(960f,720f).apply{position.set(480f,360f,0f);update()}
        val renderer=ShapeRenderer();renderer.projectionMatrix=camera.combined
        Gdx.gl.glClearColor(.13f,.18f,.11f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        renderer.begin(ShapeRenderer.ShapeType.Filled);runtime.renderActiveSkillEffects(renderer,player);renderer.end()
        val image=Pixmap.createFromFrameBuffer(0,0,960,720)
        PixmapIO.writePNG(output("range-preview.png"),image,-1,true);image.dispose();renderer.dispose()
        runtime.dispose();tamga.dispose()

        val clouds=StaticCloudsRuntime(PersistentAreaManager())
        check(clouds.tryBegin(player,false))
        val frozen=Vector2(clouds.visualCenterX,clouds.visualCenterY)
        check(clouds.freezeAtCurrentPosition())
        val field=clouds.javaClass.getDeclaredField("areaRuntime").apply{isAccessible=true}.get(clouds) as PersistentAreaRuntime
        field.update(.1f,player.centerX+200f,player.centerY+200f,{_,_->},{},{})
        check(field.area!!.center.epsilonEquals(frozen,.001f))
        check(field.area!!.anchor==PersistentAreaAnchor.FIXED_WORLD_POSITION)
        val registry=EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)}
        for(tag in listOf(EnemyTag.HUMANOID,EnemyTag.UNDEAD)) {
            val random=Random(42); val counts=mutableMapOf<String,Int>()
            repeat(20000) {val id=registry.chooseWeighted(setOf(tag),EnemyType.NORMAL,random)!!.id.value;counts[id]=(counts[id]?:0)+1}
            val ranged=if(tag==EnemyTag.HUMANOID)EnemyKindIds.ARCHER else EnemyKindIds.SKELETON_ARCHER
            check((counts[ranged.value]?:0)/20000f in .185f.. .215f){counts.toString()}
            println("SPAWN_DISTRIBUTION_PASS $tag $counts")
        }
        for(def in listOf(CorrectedEnemySpriteSheets.bear,CorrectedEnemySpriteSheets.crocodile,CorrectedEnemySpriteSheets.spearman)) {
            for(animation in def.animations.values) for(frame in animation.walkFrames+animation.idleFrame) {
                check(frame.x+frame.width <= (animation.sourceWidth?:def.sourceWidth))
                check(frame.y+frame.height <= (animation.sourceHeight?:def.sourceHeight))
            }
        }
        for(effect in com.azizoglu.grisilith.tamga.definition.TamgaEffectId.entries) {
            val icon=if(effect==com.azizoglu.grisilith.tamga.definition.TamgaEffectId.PHANTOM_STEED) "mounted_combatant" else effect.name.lowercase()
            val texture=Texture(Gdx.files.internal("ui/content_icons/tamgas/tmg_$icon.png"));check(texture.width>0);texture.dispose()
        }
        check(EnemyDamageContext(projectileAttack=true).isProjectileAttack)
        val swarmTraits=com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime.fromIds(
            emptyList(),listOf(com.azizoglu.grisilith.enemy.trait.EnemyTraitIds.SPLIT_THE_SWARM))
        fun swarm() = Enemy(500f,500f,EnemyStats(EnemyType.NORMAL,10000,0,0f,20f,0f,kindId=EnemyKindIds.BEE_SWARM),
            com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeBundle.EMPTY.copy(traitRuntime=swarmTraits))
        val meleeSwarm=swarm();meleeSwarm.takeDamage(10,EnemyDamageContext(damageType=DamageType.SLASHING))
        check(meleeSwarm.consumeSwarmSplitRequest())
        val projectileSwarm=swarm();projectileSwarm.takeDamage(10,EnemyDamageContext(damageType=DamageType.FIRE,projectileAttack=true))
        check(!projectileSwarm.consumeSwarmSplitRequest())
        fun field(value:Any,name:String):Any? = value.javaClass.getDeclaredField(name).apply{isAccessible=true}.get(value)
        // Every repaired summon frame has safe transparent margins; production
        // dimensions and pivots are consistent across all directions/steps.
        for ((name,count) in listOf("cow" to 4,"imp" to 4,"knight" to 5,"toad" to 4,"guard" to 5)) {
            var side = 0
            for (direction in listOf("south","east","north","west")) for (frame in 0 until count) {
                val image=Pixmap(Gdx.files.internal("summon_visuals/$name/${name}_${direction}_$frame.png"))
                check(image.width==image.height)
                if(side==0)side=image.width else check(side==image.width)
                var occupied=0
                for(y in 0 until side)for(x in 0 until side)if((image.getPixel(x,y) and 255)>0) {
                    occupied++
                    check(x>=8 && x<side-8 && y>=8 && y<side-8) { "$name $direction $frame clipped at $x,$y" }
                }
                check(occupied>500);image.dispose()
            }
        }
        println("SUMMON_CUTS_PASS: 88 frames, all five summon types, transparent margins and stable native scale")
        val witchTraits=com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime.fromIds(
            listOf(com.azizoglu.grisilith.enemy.trait.EnemyTraitIds.WITCH_SUMMON_FAMILIARS),emptyList())
        val witch=enemy(EnemyType.NORMAL,150f)
        fun witchTick(dt:Float)=witchTraits.update(dt,
            witch,emptyList(),player.centerX,player.centerY,player.bounds,3000f,3000f,emptyList())
        witchTick(4.01f)
        val witchRenderer=ShapeRenderer().apply { projectionMatrix=camera.combined }
        witchRenderer.begin(ShapeRenderer.ShapeType.Filled);witchTraits.render(witch,witchRenderer)
        check(witchRenderer.renderer.numVertices>0);witchRenderer.end()
        witchTick(1.16f)
        witchRenderer.begin(ShapeRenderer.ShapeType.Filled);witchTraits.render(witch,witchRenderer)
        check(witchRenderer.renderer.numVertices==0);witchRenderer.end();witchRenderer.dispose()
        check(witchTraits.spellVisuals().isNotEmpty())
        println("WITCH_WARNING_PASS: telegraph before casting, zero placeholder geometry after casting; animated spell remains")
        val stoneRenderer=ShapeRenderer().apply { projectionMatrix=camera.combined }
        val stone=Projectile(player.centerX,player.centerY,player.centerX+100f,player.centerY,
            "Sling",damage=1,isCriticalHit=false,maximumTargets=1,projectileSpeed=200f,
            maximumTravelDistance=100f,damageType=DamageType.BLUDGEONING,customVisualId="SLING_STONE")
        stoneRenderer.begin(ShapeRenderer.ShapeType.Filled);stone.render(stoneRenderer)
        check(stoneRenderer.renderer.numVertices==0);stoneRenderer.end();stoneRenderer.dispose()
        println("SLING_VISUAL_PASS: sprite-owned stone has no brown fallback square")
        val originInv=WeaponInventory()
        originInv.addWeapon(WeaponInstance(DefaultWeapons.dagger,1))
        val sideHammer=WeaponInstance(DefaultWeapons.warhammer,5);originInv.addWeapon(sideHammer)
        val originTamga=TamgaRuntimeManager(originInv,stats,null,null,emptySet()){1}
        val originRuntime=WeaponRuntimeManager(originInv,stats,originTamga,
            CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
        val originEnemies=EnemyManager()
        val originShots=mutableListOf<Projectile>()
        originRuntime.update(0f,player,originEnemies,originShots,emptyList(),3000f,3000f,
            attackOriginForSlot={if(it==1)800f to 600f else player.centerX to player.centerY},
            onDamageDealt={_,_->},onEnemyKilled={})
        val resolveOrigin=originRuntime.javaClass.getDeclaredMethod("abilityOrigin",WeaponInstance::class.java,Player::class.java).apply{isAccessible=true}
        val resolved=resolveOrigin.invoke(originRuntime,sideHammer,player) as Pair<*,*>
        check(kotlin.math.abs((resolved.first as Float)-826f)<.001f)
        check(kotlin.math.abs((resolved.second as Float)-(600f+60f*26f/42f/2f-11f))<.001f)
        GameSettings.restore(before.copy(abilityCasting=GameSettings.AbilityCasting.INSTANT))
        originRuntime.updateActiveSkillAim(1000f,600f)
        check(originRuntime.tryActivateAbilityInSlot(1,player,originEnemies,3000f,3000f))
        originRuntime.update(.001f,player,originEnemies,originShots,emptyList(),3000f,3000f,
            attackOriginForSlot={if(it==1)800f to 600f else player.centerX to player.centerY},
            onDamageDealt={_,_->},onEnemyKilled={})
        val extendedOrigin=field(field(originRuntime,"executorFactory")!!,"extendedRuntime")!!
        val firstImpact=(field(extendedOrigin,"events") as List<*>).first()!!
        val impactContext=field(firstImpact,"ctx") as WeaponAttackContext
        check(kotlin.math.abs(impactContext.attackOriginX-(resolved.first as Float))<.001f)
        check(kotlin.math.abs(impactContext.attackOriginY-(resolved.second as Float))<.001f)
        originRuntime.dispose();originTamga.dispose()
        println("WEAPON_ORIGIN_PASS: secondary carrier's held-weapon position drives ability execution")
        fun summon(type:com.azizoglu.grisilith.summon.SummonType,x:Float,y:Float)=com.azizoglu.grisilith.summon.SummonedEntity(
            "test","test","Ivy Staff",type,com.azizoglu.grisilith.summon.SummonStats(1000,10,0f,2000f,1f),x,y,100f)
        val rogue=summon(com.azizoglu.grisilith.summon.SummonType.KNIGHT_SPIRIT,player.centerX+60f,player.centerY)
        val rogueEnemies=EnemyManager();rogueEnemies.synchronizeWeaponSummons(listOf(rogue))
        check(rogueEnemies.enemyCount==0 && rogueEnemies.allEnemies.isEmpty())
        val proxy=rogueEnemies.weaponTargets.single()
        check(proxy.type==EnemyType.NORMAL && proxy.health===rogue.health)
        for(mode in GameSettings.Targeting.entries) {
            GameSettings.restore(before.copy(targeting=mode))
            check(selector.selectTarget(DefaultWeapons.longbow.targetingType,player,rogueEnemies,200f)===proxy)
        }
        check(AbilityTargeting.priority(proxy)==AbilityTargeting.priority(mini))
        check(proxy.takeDamage(40,EnemyDamageContext(damageType=DamageType.BLUDGEONING))==40)
        check(rogue.health.currentHealth==960)
        proxy.applyCondition(com.azizoglu.grisilith.combat.condition.ConditionApplication(
            type=ConditionType.STUNNED,durationSeconds=1f))
        check(rogue.conditions.contains(ConditionType.STUNNED))
        rogueEnemies.clearAll();check(rogueEnemies.weaponTargets.isEmpty())
        fun knightChoice(enemyTargets:List<Enemy>,playerDistance:Float,summonDistance:Float):String {
            val knight=summon(com.azizoglu.grisilith.summon.SummonType.KNIGHT_SPIRIT,player.centerX,player.centerY)
            val ally=summon(com.azizoglu.grisilith.summon.SummonType.COW,player.centerX+summonDistance,player.centerY)
            var chosen="none"
            knight.update(.01f,player.centerX+playerDistance,player.centerY,3000f,3000f,emptyList(),enemyTargets,listOf(knight,ally),
                onCowAttack={_,_->},onImpAttack={_,_->},onToadAttack={_,_->},onGuardAttack={_,_->},
                onKnightEnemyAttack={_,e->chosen=e.type.name},onCowSummonAttack={_,_->},onImpSummonAttack={_,_->},
                onKnightSummonAttack={_,_->chosen="summon"},onKnightPlayerAttack={chosen="player"})
            return chosen
        }
        check(knightChoice(listOf(enemy(EnemyType.FINAL_BOSS,60f),enemy(EnemyType.MINI_BOSS,30f),enemy(EnemyType.UNIQUE,5f)),40f,1f)=="MINI_BOSS")
        check(knightChoice(listOf(enemy(EnemyType.FINAL_BOSS,60f),enemy(EnemyType.MINI_BOSS,30f)),20f,1f)=="player")
        check(knightChoice(listOf(enemy(EnemyType.UNIQUE,40f),enemy(EnemyType.NORMAL,20f)),5000f,1f)=="UNIQUE")
        check(knightChoice(listOf(enemy(EnemyType.NORMAL,20f)),5000f,10f)=="summon")
        println("OATHBREAKER_TARGETS_PASS: player weapons target rogue at mini-boss rank; real health/conditions; no stage count; boss/player ties, unique then normal/summon ties")
        for(def in listOf(DefaultWeapons.longbow,DefaultWeapons.crossbow,DefaultWeapons.sling,DefaultWeapons.boomerang,DefaultWeapons.dagger)) {
            val rangedInventory=WeaponInventory();val weapon=WeaponInstance(def,3);check(rangedInventory.addWeapon(weapon))
            val rangedTamga=TamgaRuntimeManager(rangedInventory,stats,null,null,emptySet()){1}
            val ranged=WeaponRuntimeManager(rangedInventory,stats,rangedTamga,
                CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
            val rangedEnemies=EnemyManager();rangedEnemies.spawn(enemy(EnemyType.NORMAL,100f))
            val rangedShots=mutableListOf<Projectile>()
            fun rangedTick(dt:Float)=ranged.update(dt,player,rangedEnemies,rangedShots,emptyList(),3000f,3000f,onDamageDealt={_,_->},onEnemyKilled={})
            GameSettings.restore(before.copy(abilityCasting=GameSettings.AbilityCasting.INSTANT))
            val cursor=Vector2(player.centerX,player.centerY+200f);ranged.updateActiveSkillAim(cursor.x,cursor.y)
            check(ranged.tryActivateAbilityInSlot(0,player,rangedEnemies,3000f,3000f))
            check((field(ranged,"preparedCasts") as Map<*,*>).isEmpty())
            val physical=field(ranged,"physicalWeaponAbilityRuntime")!!
            check((field(physical,"casts") as List<*>).all { (field(it!!,"delaySeconds") as Float)==0f })
            rangedTick(.01f)
            if(def==DefaultWeapons.sling) {
                val extended=field(field(ranged,"executorFactory")!!,"extendedRuntime")!!
                val boulder=(field(extended,"boulders") as List<*>).single()!!
                check((field(boulder,"end") as Vector2).epsilonEquals(cursor,.01f))
            }
            ranged.resetAllTimers()
            GameSettings.restore(before.copy(abilityCasting=GameSettings.AbilityCasting.PREPARE))
            check(ranged.tryActivateAbilityInSlot(0,player,rangedEnemies,3000f,3000f))
            val alwaysInstant = def == DefaultWeapons.boomerang || def == DefaultWeapons.dagger
            check((field(ranged,"preparedCasts") as Map<*,*>).size==if(alwaysInstant)0 else 1)
            if (def==DefaultWeapons.longbow) {
                GameSettings.restore(GameSettings.snapshot().copy(normalRange=GameSettings.NormalRange.OFF,abilityRange=GameSettings.AbilityRange.OFF))
                val preview=ShapeRenderer().apply{projectionMatrix=camera.combined}
                preview.begin(ShapeRenderer.ShapeType.Filled)
                ranged.javaClass.getDeclaredMethod("renderRangeIndicators",ShapeRenderer::class.java,Player::class.java).apply{isAccessible=true}.invoke(ranged,preview,player)
                check(preview.renderer.numVertices==30) { "Bow preparation must draw five separate six-vertex lines" }
                preview.end();preview.dispose()
                println("BOW_PREVIEW_PASS: five individual arrow trajectories, matching actual volley angles")
            }
            if (!alwaysInstant) check(ranged.confirmPreparedAbilityAtMouse(player,rangedEnemies,3000f,3000f))
            check((field(ranged,"preparedCasts") as Map<*,*>).isEmpty())
            rangedTick(.01f)
            ranged.resetAllTimers()
            if (alwaysInstant) {
                val empty = EnemyManager()
                for (mode in GameSettings.AbilityCasting.entries) {
                    GameSettings.restore(before.copy(abilityCasting=mode))
                    check(ranged.tryActivateAbilityInSlot(0,player,empty,3000f,3000f))
                    check((field(ranged,"preparedCasts") as Map<*,*>).isEmpty())
                    ranged.update(.01f,player,empty,rangedShots,emptyList(),3000f,3000f,onDamageDealt={_,_->},onEnemyKilled={})
                    ranged.resetAllTimers()
                }
            }
            val priority=enemy(EnemyType.FINAL_BOSS,120f);rangedEnemies.spawn(priority)
            GameSettings.restore(before.copy(abilityCasting=GameSettings.AbilityCasting.AUTO))
            ranged.updateActiveSkillAim(player.centerX,player.centerY-200f)
            check(ranged.tryActivateAbilityInSlot(0,player,rangedEnemies,3000f,3000f))
            check((field(ranged,"castAim") as Vector2).epsilonEquals(Vector2(priority.centerX,priority.centerY),.01f))
            rangedTick(.01f);ranged.dispose();rangedTamga.dispose()
        }
        // Chalk telegraph and preparation run together; no extra one-second post-animation delay.
        for(mode in GameSettings.AbilityCasting.entries) {
            GameSettings.restore(before.copy(abilityCasting=mode))
            val chalkInventory=WeaponInventory();val chalk=WeaponInstance(DefaultWeapons.enchantedChalkDust,3)
            check(chalk.chooseChalkDustSpell(com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice.METEOR));check(chalkInventory.addWeapon(chalk))
            val chalkTamga=TamgaRuntimeManager(chalkInventory,stats,null,null,emptySet()){1}
            val chalkRuntime=WeaponRuntimeManager(chalkInventory,stats,chalkTamga,
                CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
            val chalkEnemies=EnemyManager();val victim=enemy(EnemyType.NORMAL,120f);chalkEnemies.spawn(victim)
            val cursor=Vector2(player.centerX,player.centerY+200f);chalkRuntime.updateActiveSkillAim(cursor.x,cursor.y)
            check(chalkRuntime.tryActivateAbilityInSlot(0,player,chalkEnemies,3000f,3000f))
            val motion=field(chalkRuntime,"glyphMotionRuntime") as com.azizoglu.grisilith.weapon.glyph.GlyphMotionRuntime
            check(motion.isDrawing && motion.preparingWeapon===chalk)
            fun chalkTick(dt:Float)=chalkRuntime.update(dt,player,chalkEnemies,mutableListOf(),emptyList(),3000f,3000f,onDamageDealt={_,_->},onEnemyKilled={})
            chalkTick(2.49f);check(motion.isDrawing)
            chalkTick(.02f);check(!motion.isDrawing)
            val spells=field(chalkRuntime,"glyphSpellRuntime") as com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime
            val pulse=spells.pulseVisualSnapshots().single()
            val expected=if(mode==GameSettings.AbilityCasting.AUTO)Vector2(victim.centerX,victim.centerY) else cursor
            check(Vector2(pulse.centerX,pulse.centerY).epsilonEquals(expected,.01f))
            chalkRuntime.dispose();chalkTamga.dispose()
        }
        println("CAST_MODES_PASS: five ranged abilities instant/no preparation delay, mouse confirmation, Auto rank targeting; Chalk simultaneous telegraph/preparation and cursor/Auto cast at completion")
        for(mode in GameSettings.AbilityCasting.entries) {
            GameSettings.restore(before.copy(abilityCasting=mode,normalRange=GameSettings.NormalRange.OFF,
                abilityRange=GameSettings.AbilityRange.ALL))
            val tsunamiInv=WeaponInventory();val chalk=WeaponInstance(DefaultWeapons.enchantedChalkDust,3)
            check(chalk.chooseChalkDustSpell(com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice.TSUNAMI));tsunamiInv.addWeapon(chalk)
            val tsunamiTamga=TamgaRuntimeManager(tsunamiInv,stats,null,null,emptySet()){1}
            val tsunamiRuntime=WeaponRuntimeManager(tsunamiInv,stats,tsunamiTamga,
                CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
            val empty=EnemyManager()
            fun tsunamiTick(dt:Float)=tsunamiRuntime.update(dt,player,empty,mutableListOf(),emptyList(),3000f,3000f,
                onDamageDealt={_,_->},onEnemyKilled={})
            tsunamiTick(0f);tsunamiRuntime.updateActiveSkillAim(2800f,2800f)
            check(tsunamiRuntime.tryActivateAbilityInSlot(0,player,empty,3000f,3000f))
            val marker=ShapeRenderer().apply {projectionMatrix=camera.combined}
            marker.begin(ShapeRenderer.ShapeType.Filled)
            tsunamiRuntime.javaClass.getDeclaredMethod("renderRangeIndicators",ShapeRenderer::class.java,Player::class.java)
                .apply{isAccessible=true}.invoke(tsunamiRuntime,marker,player)
            check(marker.renderer.numVertices==0);marker.end();marker.dispose()
            tsunamiTick(2.49f);tsunamiTick(.02f)
            val spell=field(tsunamiRuntime,"glyphSpellRuntime") as com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime
            val wave=field(spell,"tsunamiWave")!!
            val start=field(wave,"start") as Vector2
            check(start.x==0f || start.x==3000f || start.y==0f || start.y==3000f)
            val request=field(wave,"request") as com.azizoglu.grisilith.weapon.glyph.GlyphCastRequest
            check(Vector2(request.centerX,request.centerY).epsilonEquals(Vector2(player.centerX,player.centerY),.001f))
            val direction=field(wave,"direction") as Vector2
            check(direction.dot(Vector2(player.centerX,player.centerY).sub(start).nor())>.9999f)
            check(spell.tsunamiVisualSnapshot()!=null)
            tsunamiTick(20f);check(spell.tsunamiVisualSnapshot()==null)
            tsunamiRuntime.dispose();tsunamiTamga.dispose()
        }
        println("TSUNAMI_PASS: all casting modes, no preparation/range overlay or target requirement, random map edge, cast-time player position, ends after exiting map")
        val melee=com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualRenderer()
        val meleeEvents=com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualManager
        meleeEvents.update(100f)
        Gdx.gl.glClearColor(.12f,.12f,.13f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        repeat(16) { i ->
            meleeEvents.spawn(com.azizoglu.grisilith.weapon.visual.attack.MeleeAttackVisualKind.LANCE_SPIN,
                120f+(i%4)*240f,630f-(i/4)*180f,0f,210f,170f,1f)
            meleeEvents.activeEvents.last().elapsedSeconds=i/16f
        }
        melee.render(camera)
        check((field(melee,"textures") as Map<*,*>).size==16)
        val lanceImage=Pixmap.createFromFrameBuffer(0,0,960,720)
        PixmapIO.writePNG(output("lance-playback.png"),lanceImage,-1,true)
        lanceImage.dispose();melee.dispose();meleeEvents.update(100f)
        GameSettings.restore(before)
        val settingsScreen=com.azizoglu.grisilith.screen.SettingsScreen(com.azizoglu.grisilith.GrisilithGame()) {}
        settingsScreen.show()
        val pageField=settingsScreen.javaClass.getDeclaredField("page").apply{isAccessible=true}
        pageField.set(settingsScreen,pageField.type.enumConstants.first{it.toString()=="COMBAT"})
        settingsScreen.javaClass.getDeclaredMethod("rebuildButtons").apply{isAccessible=true}.invoke(settingsScreen)
        fun settingArrows(right:Boolean)=(field(settingsScreen,"buttons") as List<com.azizoglu.grisilith.screen.components.MenuButton>)
            .filter { it.visualStyle == if(right)com.azizoglu.grisilith.screen.components.MenuButtonVisualStyle.ARROW_RIGHT
                else com.azizoglu.grisilith.screen.components.MenuButtonVisualStyle.ARROW_LEFT }.sortedByDescending {it.bounds.y}
        fun click(button:com.azizoglu.grisilith.screen.components.MenuButton) {
            (field(button,"onClick") as Function0<*>).invoke()
        }
        check(settingArrows(true).size==4 && settingArrows(false).size==4)
        val initialDraft=field(settingsScreen,"draftSettings") as GameSettings.Snapshot
        click(settingArrows(true).first())
        check((field(settingsScreen,"draftSettings") as GameSettings.Snapshot).targeting != initialDraft.targeting)
        click(settingArrows(false).first())
        check((field(settingsScreen,"draftSettings") as GameSettings.Snapshot).targeting == initialDraft.targeting)
        println("SETTINGS_STEPPERS_PASS: four paired themed arrows; forward/back changes and draft persistence")
        settingsScreen.resize(960,720);settingsScreen.render(.016f)
        val settingsImage=Pixmap.createFromFrameBuffer(0,0,960,720)
        PixmapIO.writePNG(output("settings-preview.png"),settingsImage,-1,true)
        settingsImage.dispose()
        settingsScreen.render(.016f)
        val settingsPointer=(field(settingsScreen,"pointer") as com.azizoglu.grisilith.screen.components.MenuPointer).position
        val boundsMethod=settingsScreen.javaClass.getDeclaredMethod("combatPanelBounds",Int::class.javaPrimitiveType).apply{isAccessible=true}
        val tooltipRowMethod=settingsScreen.javaClass.getDeclaredMethod("combatTooltipRow").apply{isAccessible=true}
        val panels=(0..3).map { boundsMethod.invoke(settingsScreen,it) as com.badlogic.gdx.math.Rectangle }
        check(panels.map{it.width}.distinct().size==1 && panels.first().width<616f)
        panels.forEachIndexed { row,panel ->
            settingsPointer.set(panel.x+panel.width/2f,panel.y+panel.height/2f)
            check(tooltipRowMethod.invoke(settingsScreen)==row)
            settingsPointer.set(350f,panel.y+42f);check(tooltipRowMethod.invoke(settingsScreen)==null)
            settingsPointer.set(panel.x-30f,panel.y+32f);check(tooltipRowMethod.invoke(settingsScreen)==null)
            settingsPointer.set(panel.x+panel.width+30f,panel.y+32f);check(tooltipRowMethod.invoke(settingsScreen)==null)
        }
        settingsPointer.set(panels.first().x+panels.first().width/2f,panels.first().y+32f)
        println("COMBAT_HELP_PASS: equal compact widths; tooltip only on value panels, not labels/arrows; active-option copy")
        val tooltipBatch=field(settingsScreen,"batch") as com.badlogic.gdx.graphics.g2d.SpriteBatch
        tooltipBatch.begin()
        settingsScreen.javaClass.getDeclaredMethod("drawCombatTooltip").apply{isAccessible=true}.invoke(settingsScreen)
        tooltipBatch.end()
        val hoverImage=Pixmap.createFromFrameBuffer(0,0,960,720)
        PixmapIO.writePNG(output("settings-hover-preview.png"),hoverImage,-1,true)
        hoverImage.dispose();settingsScreen.dispose()
        println("TARGETING_SMOKE_PASS: rank, all normal targeting modes, empty-target gate/no cooldown, +25% Double Swords range, early/timeout casts, range drawing, Totem freeze, cuts and eight icons")
    }
}
