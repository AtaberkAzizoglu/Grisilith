package qa

import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.*
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.*
import com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.item.definition.*
import com.azizoglu.grisilith.item.runtime.*
import com.azizoglu.grisilith.weapon.skill.*
import com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice
import com.azizoglu.grisilith.character.trait.*
import com.azizoglu.grisilith.tamga.runtime.TamgaRuntimeManager
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Vector2

object GearAdditionSmoke {
    private fun field(o: Any,n: String): Any?=o.javaClass.getDeclaredField(n).apply{isAccessible=true}.get(o)
    private fun invoke(o: Any,n: String,vararg args: Any)=o.javaClass.declaredMethods.first{
        it.name==n && it.parameterCount==args.size }.apply{isAccessible=true}.invoke(o,*args)
    @JvmStatic fun main(args: Array<String>) {
        val config=Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(960,540);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/gear-v04/qa-prefs",Files.FileType.Local)
        }
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{runChecks()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)};Gdx.app.exit()}},config)
    }
    private fun runChecks(){
        val stats=PlayerStats().apply{setBaseScore(StatType.CRIT_RATE,0)}
        val player=Player(300f,300f,3000f,3000f,stats)
        val enemies=EnemyManager();val shots=mutableListOf<Projectile>()
        fun enemy(dx:Float,dy:Float=0f)=Enemy(player.centerX+dx-10f,player.centerY+dy-10f,
            EnemyStats(EnemyType.NORMAL,1000000,0,0f,20f,0f))
        fun reset(){enemies.allEnemies.toList().forEach{enemies.remove(it)};shots.clear()}
        fun ctx(w:WeaponInstance)=WeaponAttackContext(w,0,player,player.centerX,player.centerY,enemies,shots,
            {_,_->},{},{},{_,d->d},{it},{false},normalAttackIntervalSeconds=2f)
        fun damage(e:Enemy)=1000000-e.health.currentHealth
        val gear=ExtendedPhysicalWeaponRuntime(stats)
        val settings=com.azizoglu.grisilith.settings.GameSettings
        val settingsBefore=settings.snapshot()
        val near=enemy(50f)
        val boss=Enemy(player.centerX+150f-10f,player.centerY-10f,
            EnemyStats(EnemyType.MINI_BOSS,1000000,0,0f,20f,0f))
        settings.restore(settingsBefore.copy(bossPriorityAbilities=false))
        check(AbilityTargeting.select(listOf(near,boss),player.centerX,player.centerY,200f)===boss)
        settings.restore(settingsBefore.copy(bossPriorityAbilities=true))
        check(AbilityTargeting.select(listOf(near,boss),player.centerX,player.centerY,200f)===boss)
        check(AbilityTargeting.select(listOf(near,boss),player.centerX,player.centerY,80f)===near)
        settings.restore(settingsBefore.copy(bossPriorityAbilities=false, abilityCasting=com.azizoglu.grisilith.settings.GameSettings.AbilityCasting.INSTANT))
        for(def in NewPhysicalWeapons.all){
            check(DefaultWeapons.findById(def.id.value)===def)
            check(com.azizoglu.grisilith.unlock.UnlockRegistry.find(def.id.value)?.cost==5)
            check(com.azizoglu.grisilith.ui.icon.ContentIconRenderer.hasIcon(def.id.value))
            check(Gdx.files.internal("weapon_visuals/carriers_v4/"+def.id.value.removePrefix("WPN_").lowercase()+".png").exists())
            for(l in 1..5)check(def.statsForLevel(l).damageMultiplier==1f)
        }
        for(unlock in com.azizoglu.grisilith.unlock.UnlockRegistry.all().filter { it.id != "STAT_POINT" }) {
            check(com.azizoglu.grisilith.profile.PlayerProfileRepository.isUnlocked(unlock.id))
            check(com.azizoglu.grisilith.unlock.UnlockManager.purchase(unlock.id)==com.azizoglu.grisilith.unlock.UnlockPurchaseResult.ALREADY_UNLOCKED)
        }
        check(DefaultItems.discardVoucher.rarity==ItemRarity.RARE)
        val items=ItemInventory()
        repeat(5){check(items.addItem(ItemInstance(DefaultItems.discardVoucher))==ItemInventoryResult.ADDED)}
        check(!items.canAddDefinition(DefaultItems.discardVoucher))
        val chest=com.azizoglu.grisilith.chest.ChestOpeningSession()
        chest.begin(3,2+items.countByDefinitionId(DefaultItems.discardVoucher.id))
        var normal=2
        fun spend(){check(chest.consumeBatchDiscard());if(normal>0)normal-- else check(items.consumeOneDefinition(DefaultItems.discardVoucher.id))}
        spend();spend();check(items.countByDefinitionId(DefaultItems.discardVoucher.id)==5)
        spend();check(items.countByDefinitionId(DefaultItems.discardVoucher.id)==4)
        chest.reset();check(items.countByDefinitionId(DefaultItems.discardVoucher.id)==4)

        reset();val head=enemy(200f);val shaft=enemy(100f);val overlap=enemy(190f)
        listOf(head,shaft,overlap).forEach(enemies::spawn)
        invoke(gear,"hammer",ctx(WeaponInstance(DefaultWeapons.warhammer,1)),Vector2(head.centerX,head.centerY))
        check(damage(head)==140 && damage(shaft)==46 && damage(overlap)==140)
        reset();val reach=enemy(300f);enemies.spawn(reach)
        gear.execute(ctx(WeaponInstance(DefaultWeapons.warhammer,3)));check(damage(reach)==0)
        gear.execute(ctx(WeaponInstance(DefaultWeapons.warhammer,4)));check(damage(reach)==140)
        reset();val cone=enemy(200f);val lash=enemy(100f);val outside=enemy(200f,30f)
        listOf(cone,lash,outside).forEach(enemies::spawn)
        invoke(gear,"lash",ctx(WeaponInstance(DefaultWeapons.whip,1)),Vector2(cone.centerX,cone.centerY),1f)
        check(damage(cone)==60 && damage(lash)==20 && damage(outside)==0)
        gear.clear();reset();val twice=enemy(200f);enemies.spawn(twice)
        val whip=WeaponInstance(DefaultWeapons.whip,4)
        gear.execute(ctx(whip));check(damage(twice)==45)
        gear.update(.16f,setOf(whip.runtimeId));check(damage(twice)==90)

        gear.clear();reset();val impact=enemy(100f);enemies.spawn(impact)
        val sling=WeaponInstance(DefaultWeapons.sling,4);gear.execute(ctx(sling))
        val parent=shots.single();(field(parent,"position") as Vector2).set(impact.centerX-7f,impact.centerY-7f)
        (field(parent,"previousPosition") as Vector2).set(field(parent,"position") as Vector2)
        parent.checkCollision(listOf(impact),{},{},{},{_,d->d});check(damage(impact)==110)
        gear.update(.016f,setOf(sling.runtimeId))
        val pieces=shots.filter{it!==parent};check(pieces.size in 3..8)
        val angles=pieces.map { (field(it,"velocity") as Vector2).angleDeg() }.sorted()
        angles.indices.forEach { i ->
            val difference=(angles[(i+1)%angles.size]-angles[i]+360f)%360f
            check(kotlin.math.abs(difference-360f/angles.size)<.01f)
        }
        pieces.forEach{p->
            check(field(p,"damage")==55 && field(p,"safeMaximumTravelDistance")==80f)
            (field(p,"position") as Vector2).set(impact.centerX-4f,impact.centerY-4f)
            (field(p,"previousPosition") as Vector2).set(field(p,"position") as Vector2)
            p.checkCollision(listOf(impact),{},{},{},{_,d->d});check(p.isActive)
            check(field(p,"onImpact")==null)
        };check(damage(impact)==110)

        gear.clear();reset();enemies.spawn(enemy(300f))
        val boom=WeaponInstance(DefaultWeapons.boomerang,4);val other=WeaponInstance(DefaultWeapons.boomerang,4)
        gear.execute(ctx(boom));gear.execute(ctx(other))
        gear.update(.25f,setOf(boom.runtimeId,other.runtimeId))
        gear.updateAim(player.centerX,player.centerY+600f);gear.activate(ctx(boom))
        val flights=field(gear,"boomerangs") as List<*>
        check(flights.size==3)
        val otherFlight=flights.first{(field(it!!,"ctx") as WeaponAttackContext).weapon===other}!!
        val otherY=(field(otherFlight,"position") as Vector2).y
        gear.update(.1f,setOf(boom.runtimeId,other.runtimeId))
        check((field(otherFlight,"position") as Vector2).y==otherY)
        val guided=flights.first{(field(it!!,"ctx") as WeaponAttackContext).weapon===boom}!!
        check((field(guided,"position") as Vector2).y>otherY)
        repeat(140){gear.update(1f/60f,setOf(boom.runtimeId,other.runtimeId))}
        check((field(gear,"boomerangs") as List<*>).none{!(field(it!!,"normal") as Boolean)})
        check((field(gear,"boomerangs") as List<*>).all{(field(it!!,"phase") as Int)>=2})
        gear.execute(ctx(boom));check((field(gear,"boomerangs") as List<*>).any{(field(it!!,"phase") as Int)==0})
        gear.clear();reset();enemies.spawn(enemy(300f))
        val rearTest=WeaponInstance(DefaultWeapons.boomerang,4)
        gear.execute(ctx(rearTest));gear.update(2f,setOf(rearTest.runtimeId))
        val rear=(field(gear,"boomerangs") as List<*>).single()!!
        check(field(rear,"phase")==2)
        check((field(rear,"direction") as Vector2).x< -.99f)
        gear.update(1f,setOf(rearTest.runtimeId))
        check(kotlin.math.abs((field(rear,"position") as Vector2).dst(player.centerX,player.centerY)-350f)<.01f)
        for(level in listOf(3,5)) {
            gear.clear()
            val w=WeaponInstance(DefaultWeapons.boomerang,level)
            gear.activate(ctx(w))
            val extra=(field(gear,"boomerangs") as List<*>).single()!!
            check(kotlin.math.abs((field(extra,"sizeScale") as Float)-(if(level==3)1.25f else 1.4f))<.001f)
        }
        gear.clear()
        check(DefaultWeapons.boomerang.upgradedWeaponAbility?.durationSeconds==6f)
        check(DefaultWeapons.boomerang.upgradedWeaponAbility?.cooldownSeconds==25f)

        // Lv5 abilities use their approved damage and area/size upgrades.
        for(l in listOf(3,5)) {
            gear.clear();reset();val target=enemy(90f);enemies.spawn(target)
            val hammer=WeaponInstance(DefaultWeapons.warhammer,l)
            gear.updateAim(player.centerX+300f,player.centerY);gear.activate(ctx(hammer))
            check((field(gear,"events") as List<*>).size==if(l==3)3 else 4)
            gear.update(.01f,setOf(hammer.runtimeId));check(damage(target)==if(l==3)210 else 280)
            val flash=(field(gear,"flashes") as List<*>).last()!!
            check(kotlin.math.abs((field(flash,"radius") as Float)-if(l==3)70f else 84f)<.001f)
            gear.clear();reset();val whipTarget=enemy(200f);enemies.spawn(whipTarget)
            gear.activate(ctx(WeaponInstance(DefaultWeapons.whip,l)));check(damage(whipTarget)==if(l==3)120 else 150)
            gear.clear();reset()
            val angleTarget=enemy(150f,180f) // 50 degrees: excluded by Lv3, included by Lv5.
            val straight=enemy(100f);enemies.spawn(straight);enemies.spawn(angleTarget)
            gear.activate(ctx(WeaponInstance(DefaultWeapons.whip,l)))
            check(damage(angleTarget)==if(l==3)0 else 150)
            gear.clear();reset();val stoneTarget=enemy(200f);enemies.spawn(stoneTarget)
            val slingSkill=WeaponInstance(DefaultWeapons.sling,l)
            gear.updateAim(stoneTarget.centerX,stoneTarget.centerY);gear.activate(ctx(slingSkill))
            val expected=if(l==3)floatArrayOf(3f) else floatArrayOf(3.5f,2.5f)
            var damageBefore=0
            for(hop in expected.indices) {
                val rock=(field(gear,"boulders") as List<*>).single()!!
                val end=field(rock,"end") as Vector2
                (field(stoneTarget,"position") as Vector2).set(end.x-10f,end.y-10f)
                gear.update(if(hop==0).60f else .35f,setOf(slingSkill.runtimeId))
                check(damage(stoneTarget)-damageBefore==(110*expected[hop]).toInt())
                damageBefore=damage(stoneTarget)
            }
            check((field(gear,"boulders") as List<*>).isEmpty())
        }

        // Independent duplicate spell channels, no global type lock.
        fun manager(inv:WeaponInventory)=WeaponRuntimeManager(inv,stats,
            TamgaRuntimeManager(inv,stats,null,null,emptySet()){1},
            CharacterTraitDefinition(CharacterTraitId.NECROMANCY,"Test","Test"),player){1}
        for(def in listOf(DefaultWeapons.orbOfSky,DefaultWeapons.totemOfDarkClouds,
            DefaultWeapons.amuletOfTrollKing,DefaultWeapons.steamWand)){
            reset();enemies.spawn(enemy(100f))
            val inv=WeaponInventory();val a=WeaponInstance(def,5);val b=WeaponInstance(def,5)
            check(inv.addWeapon(a)&&inv.addWeapon(b));val runtime=manager(inv)
            check(runtime.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
            check(runtime.tryActivateAbilityInSlot(1,player,enemies,3000f,3000f))
            check(runtime.abilityStateInSlot(0)==WeaponAbilityState.ACTIVE)
            check(runtime.abilityStateInSlot(1)==WeaponAbilityState.ACTIVE)
            val channels=field(runtime,"focusChannels") as Map<*,*>;check(channels.size==2)
            if(def==DefaultWeapons.orbOfSky || def==DefaultWeapons.amuletOfTrollKing) {
                val component=if(def==DefaultWeapons.orbOfSky)"vortex" else "pool"
                val firstComponent=field(channels[a.runtimeId]!!,component)!!
                val position=if(component=="vortex") {
                    ((firstComponent as VortexPullRuntime).field!!.center)
                } else Vector2((firstComponent as AcidicPoolRuntime).visualCenterX,firstComponent.visualCenterY)
                com.azizoglu.grisilith.combat.interaction.CombatInteractionManager.notifyGlobal(
                    com.azizoglu.grisilith.combat.interaction.CombatInteractionEvent(
                        com.azizoglu.grisilith.combat.interaction.CombatInteractionTrigger.PROJECTILE_PASSED,
                        damageType=if(component=="vortex")DamageType.NECROTIC else DamageType.FIRE,
                        worldX=position.x,worldY=position.y))
                channels.values.forEach { channel ->
                    val effect=field(channel!!,component)!!
                    if(component=="vortex") check((effect as VortexPullRuntime).isPlagueStormActive)
                    else check((effect as AcidicPoolRuntime).isVitriolicIgnitionActive)
                }
            }
            runtime.updateActiveSkillAim(player.centerX+150f,player.centerY)
            runtime.update(delta=.016f,player=player,enemyManager=enemies,projectiles=shots,
                worldWidth=3000f,worldHeight=3000f,mapObstacles=emptyList(),onDamageDealt={_,_->},onEnemyKilled={})
            runtime.prepareForStageTransition()
            check(runtime.abilityStateInSlot(0)==WeaponAbilityState.COOLDOWN && runtime.abilityStateInSlot(1)==WeaponAbilityState.COOLDOWN)
            inv.removeWeaponFromSlot(0);runtime.currentAttacksPerSecondFor(b)
            check((field(runtime,"focusChannels") as Map<*,*>).size==1)
            runtime.dispose()
        }
        val chalkInventory=WeaponInventory()
        repeat(2){val w=WeaponInstance(DefaultWeapons.enchantedChalkDust,5);check(w.chooseChalkDustSpell(ChalkDustSpellChoice.CALL_LIGHTNING));check(chalkInventory.addWeapon(w))}
        val chalkManager=manager(chalkInventory)
        check(chalkManager.tryActivateAbilityInSlot(0,player,enemies,3000f,3000f))
        check(!chalkManager.tryActivateAbilityInSlot(1,player,enemies,3000f,3000f));chalkManager.dispose()

        // Actual attack clock: normal Boomerang throws remain continuous across catches.
        reset();enemies.spawn(enemy(300f))
        val clockInventory=WeaponInventory();val clockBoom=WeaponInstance(DefaultWeapons.boomerang,4)
        check(clockInventory.addWeapon(clockBoom));val clockManager=manager(clockInventory)
        val period=1f/clockManager.currentAttacksPerSecondFor(clockBoom)
        val simulationSeconds=period*5.1f
        var simulated=0f
        while(simulated<simulationSeconds){
            clockManager.update(delta=1f/60f,player=player,enemyManager=enemies,projectiles=shots,
                mapObstacles=emptyList(),worldWidth=3000f,worldHeight=3000f,onDamageDealt={_,_->},onEnemyKilled={})
            simulated+=1f/60f
        }
        check(clockBoom.completedNormalAttackCount==5)
        clockManager.dispose()

        val timingInventory=WeaponInventory();val timingBoom=WeaponInstance(DefaultWeapons.boomerang,4)
        check(timingInventory.addWeapon(timingBoom));val timingManager=manager(timingInventory)
        stats.setBaseScore(StatType.ATTACK_SPEED,10)
        val originalPeriod=1f/timingManager.currentAttacksPerSecondFor(timingBoom)
        var time=0f;var changed=false
        while(time<originalPeriod*2.1f){
            timingManager.update(delta=1f/60f,player=player,enemyManager=enemies,projectiles=shots,
                mapObstacles=emptyList(),worldWidth=3000f,worldHeight=3000f,onDamageDealt={_,_->},onEnemyKilled={})
            time+=1f/60f
            if(!changed && time>originalPeriod*1.25f){stats.setBaseScore(StatType.ATTACK_SPEED,25);changed=true}
        }
        check(timingBoom.completedNormalAttackCount==2)
        timingManager.dispose();stats.setBaseScore(StatType.ATTACK_SPEED,10)

        // Independent Ivy summon waves remain supported.
        val summonInventory=WeaponInventory()
        repeat(2){val w=WeaponInstance(DefaultWeapons.ivyStaff,5);check(w.chooseIvySummon(
            com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_ANIMALS));check(summonInventory.addWeapon(w))}
        val summons=com.azizoglu.grisilith.summon.SummonManager(summonInventory,stats,{1})
        check(summons.tryActivateIvyStaffInSlot(0,player.centerX,player.centerY,3000f,3000f))
        check(summons.tryActivateIvyStaffInSlot(1,player.centerX,player.centerY,3000f,3000f))
        check(summons.activeSummons.size==6)

        // Render actual UI frames and carrier consumers, then capture only this QA framebuffer.
        Gdx.gl.glClearColor(.12f,.11f,.10f,1f);Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT)
        val batch=com.badlogic.gdx.graphics.g2d.SpriteBatch()
        batch.projectionMatrix=com.badlogic.gdx.math.Matrix4().setToOrtho2D(0f,0f,960f,540f)
        batch.begin()
        (NewPhysicalWeapons.all+DefaultItems.discardVoucher).forEachIndexed { i,def ->
            check(com.azizoglu.grisilith.ui.icon.ContentIconRenderer.draw(batch,def,
                com.badlogic.gdx.math.Rectangle(20f+i*185f,330f,160f,160f)))
        }
        batch.end();batch.dispose()
        val camera=com.badlogic.gdx.graphics.OrthographicCamera(960f,540f)
        camera.position.set(480f,270f,0f);camera.update()
        val carrier=com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierRenderer()
        DefaultWeapons.all.forEachIndexed { i,def ->
            val x=45f+(i%9)*105f;val y=if(i<9)235f else 110f
            carrier.renderPlayerCarrier(com.azizoglu.grisilith.weapon.visual.carrier.WeaponCarrierVisualSystem.CarrierSnapshot(
                0,WeaponInstance(def,5),x,y,com.azizoglu.grisilith.character.visual.CharacterVisualDirection.RIGHT,
                null,null,false,0f,0f,false,x+100f,y),camera,{})
        }
        carrier.dispose()
        gear.clear();reset();val visualTarget=enemy(160f);enemies.spawn(visualTarget)
        val hammerVisual=WeaponInstance(DefaultWeapons.warhammer,5)
        val whipVisual=WeaponInstance(DefaultWeapons.whip,5)
        gear.execute(ctx(hammerVisual));gear.execute(ctx(whipVisual));gear.activate(ctx(hammerVisual));gear.activate(ctx(whipVisual))
        gear.update(.10f,setOf(hammerVisual.runtimeId,whipVisual.runtimeId))
        val shapes=com.badlogic.gdx.graphics.glutils.ShapeRenderer()
        shapes.projectionMatrix=camera.combined
        shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled)
        gear.render(shapes)
        shapes.end()
        gear.clear();reset();enemies.spawn(enemy(200f))
        val hdWhip=WeaponInstance(DefaultWeapons.whip,5)
        gear.activate(ctx(hdWhip))
        repeat(16) { frame ->
            gear.update(if(frame==0).001f else .35f/16f,setOf(hdWhip.runtimeId))
            Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT)
            shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled)
            gear.render(shapes);shapes.end()
            if(frame==8) {
                val effectPixels=com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0,0,960,540)
                com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("../tools/weapon-visual-smoke/whip-hd-playback.png"),effectPixels,-1,true)
                effectPixels.dispose()
            }
        }
        val hdTextures=field(gear,"textures") as Map<*,*>
        repeat(4) { part ->
            val texture=hdTextures["weapon_visuals/physical_revision/whip_sweep_hd_"+part+".png"] as com.badlogic.gdx.graphics.Texture
            check(texture.width/2>=600 && texture.height/2>=600)
        }
        shapes.dispose()
        settings.restore(settingsBefore)
        val pixels=com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0,0,960,540)
        com.badlogic.gdx.graphics.PixmapIO.writePNG(Gdx.files.local("../tools/gear-v04/qa-ui-preview.png"),pixels,-1,true)
        pixels.dispose()

        gear.dispose()
        println("GEAR_ADDITION_SMOKE_PASS: catalogs/icons/merchant, Rare5 voucher consumption, hammer overlap/reach, whip30deg/33%/75% double hit, Sling fragments80 and excluded original victim, per-copy guided boomerang lifetime/half-range rear flight, duplicate non-Chalk spell channels and cleanup, Chalk shared restriction.")
    }
}
