package qa

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.config.*
import com.azizoglu.grisilith.screen.*
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.runtime.executor.LongswordAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.ShortswordAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.LanceAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.ShieldAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.DaggerAttackExecutor
import com.azizoglu.grisilith.weapon.runtime.executor.ProjectileWeaponAttackExecutor
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO

/** Hidden native window, isolated preferences, fake mixer output; no hosted requests. */
object AudioScreenSmoke {
    private fun field(target:Any,name:String)=target.javaClass.getDeclaredField(name).apply{isAccessible=true}
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter() {
            override fun create() {
                try { checks();Gdx.app.exit() }
                catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}
            }
        },Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)
        })
    }
    private fun checks() {
        val music=AudioSmoke.FakeMusic();val sound=AudioSmoke.FakeSound()
        var audioTime=1000L
        val loadedCues=mutableSetOf<UiSound>()
        val audio=AudioController({music.api},{loadedCues+=it;sound.api},millis={audioTime})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        val game=GrisilithGame()
        game.showMainMenu();game.screen.render(.016f);GameAudio.update()
        check(GameAudio.inMenus() && music.playing && music.plays==1)
        val menu=game.screen
        val settings=SettingsScreen(game){ };settings.resize(1280,720);game.screen=settings
        check(music.plays==1)
        val original=GameSettings.snapshot()
        field(settings,"draftSettings").set(settings,original.copy(masterVolume=.2f,musicVolume=.3f,soundEffectsVolume=.4f))
        settings.render(.016f);check(kotlin.math.abs(music.volume-.027f)<.0001f)
        settings.javaClass.getDeclaredMethod("discardPendingChanges").apply{isAccessible=true}.invoke(settings)
        check(GameSettings.snapshot()==original)
        check(kotlin.math.abs(music.volume-original.masterVolume*original.musicVolume*.45f)<.0001f)
        game.screen=menu;settings.dispose()
        val run=FirstScreen(game,GameDifficulty.HARD,GameMap.TEST_MAP,DefaultCharacters.koba)
        run.resize(1280,720);game.screen=run
        check(!GameAudio.inMenus() && !music.playing)
        GameAudio.combatPlayback(1f,false)
        // Longsword: no target is silent; one sound per real swing, not per enemy hit.
        GameAudio.update();audioTime+=1000
        val stats=PlayerStats()
        val player=Player(300f,300f,3000f,3000f,stats)
        val enemies=EnemyManager()
        val executor=LongswordAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector())
        fun context(level:Int)=WeaponAttackContext(WeaponInstance(DefaultWeapons.longsword,level),0,player,
            player.centerX,player.centerY,enemies,mutableListOf<Projectile>(),{_,_->},{},{},{_,d->d},{it},{false})
        val silentBefore=sound.played.size
        executor.execute(context(1));GameAudio.update();check(sound.played.size==silentBefore)
        listOf(50f,65f).forEach {distance -> enemies.spawn(Enemy(player.centerX+distance-10f,player.centerY-10f,
            EnemyStats(EnemyType.NORMAL,100000,0,0f,20f,0f))) }
        listOf(1,4).forEach {level ->
            audioTime+=1000;val before=sound.played.size
            executor.execute(context(level));GameAudio.update();check(sound.played.size==before+1)
        }
        val physical=ExtendedPhysicalWeaponRuntime(stats)
        val attacks=listOf(
            ShortswordAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector()) to DefaultWeapons.doubleSwords,
            LanceAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector()) to DefaultWeapons.lance,
            ShieldAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector(),stats) to DefaultWeapons.shield,
            physical to com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.whip,
            physical to com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.warhammer,
            physical to com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.sling,
            physical to com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.boomerang,
            ProjectileWeaponAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector(),stats) to DefaultWeapons.longbow,
            ProjectileWeaponAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector(),stats) to DefaultWeapons.crossbow,
            DaggerAttackExecutor(WeaponDamageCalculator(stats),WeaponTargetSelector(),stats) to DefaultWeapons.dagger
        )
        attacks.forEach{(attack,definition)->
            listOf(1,4).forEach{level->
                val ctx=context(level).copy(weapon=WeaponInstance(definition,level))
                audioTime+=1000;val silence=sound.played.size
                attack.execute(ctx.copy(enemyManager=EnemyManager()));GameAudio.update()
                check(sound.played.size==silence){"No-target attack should be silent: ${definition.id}"}
                audioTime+=1000;val before=sound.played.size
                attack.execute(ctx);GameAudio.update();check(sound.played.size==before+1){"Missing normal cue for ${definition.id} level $level"}
                if(definition.id.value=="WPN_BOOMERANG") {
                    audioTime+=1000;attack.execute(ctx);GameAudio.update()
                    check(sound.played.size==before+1){"Already flying boomerang must not replay its launch cue"}
                }
                if(definition.id.value=="WPN_WHIP" && level==4) {
                    audioTime+=160;physical.update(.16f,setOf(ctx.weapon.runtimeId));GameAudio.update()
                    check(sound.played.size==before+2){"Upgraded Whip needs a second lash cue"}
                }
            }
        }
        check(loadedCues.containsAll(listOf(UiSound.DOUBLE_SWORDS_SWING,UiSound.DOUBLE_SWORDS_UPGRADED,
            UiSound.LANCE_THRUST,UiSound.SHIELD_STRIKE,UiSound.WHIP_LASH,UiSound.WARHAMMER_IMPACT,
            UiSound.BOW_SHOT,UiSound.CROSSBOW_SHOT,UiSound.DAGGER_THROW,UiSound.SLING_THROW,UiSound.BOOMERANG_THROW)))
        // Real ability paths: shared normal cues, selected volleys, and damage-wave cues.
        val calculator=WeaponDamageCalculator(stats)
        val ability=com.azizoglu.grisilith.weapon.skill.PhysicalWeaponAbilityRuntime(stats,calculator,
            ShortswordAttackExecutor(calculator,WeaponTargetSelector()),
            com.azizoglu.grisilith.weapon.skill.DefensiveLineRuntime(stats){1})
        fun invokeAbility(name:String,ctx:WeaponAttackContext,vararg extra:Any) {
            val method=ability.javaClass.declaredMethods.single{it.name==name}.apply{isAccessible=true}
            method.invoke(ability,ctx,*extra)
        }
        listOf(3,5).forEach{level->
            audioTime+=4000;val before=sound.played.size
            val ctx=context(level).copy(weapon=WeaponInstance(DefaultWeapons.doubleSwords,level))
            invokeAbility("executeRapidStrokes",ctx.copy(enemyManager=EnemyManager()),player.centerX,player.centerY)
            GameAudio.update();check(sound.played.size==before)
            invokeAbility("executeRapidStrokes",ctx,player.centerX+50f,player.centerY)
            GameAudio.update();check(sound.played.size==before+1)
            check((if(level==3)UiSound.DOUBLE_SWORDS_SWING else UiSound.DOUBLE_SWORDS_UPGRADED) in loadedCues)
        }
        listOf("fireAirSlash" to DefaultWeapons.longsword,"executeSpinAttack" to DefaultWeapons.lance,
            "executeDefensiveBurst" to DefaultWeapons.shield).forEach{(name,definition)->
            audioTime+=4000;val before=sound.played.size
            val ctx=context(5).copy(weapon=WeaponInstance(definition,5))
            when(name) {
                "fireAirSlash"->invokeAbility(name,ctx,player.centerX+100f,player.centerY)
                "executeDefensiveBurst"->invokeAbility(name,ctx,10)
                else->invokeAbility(name,ctx)
            }
            GameAudio.update();check(sound.played.size==before+1){"Missing ability sound: $name"}
        }
        listOf(3,5).forEach{level->
            val ctx=context(level).copy(weapon=WeaponInstance(com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.warhammer,level))
            physical.updateAim(player.centerX+300f,player.centerY)
            audioTime+=4000;val before=sound.played.size
            physical.activate(ctx);GameAudio.update();check(sound.played.size==before)
            repeat(if(level==5)4 else 3){index->
                audioTime+=180;physical.update(if(index==0).001f else .18f,setOf(ctx.weapon.runtimeId));GameAudio.update()
                check(sound.played.size==before+index+1){"Earthbreaker must sound exactly once per wave"}
            }
        }
        audioTime+=4000;val sweepBefore=sound.played.size
        physical.activate(context(5).copy(weapon=WeaponInstance(com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.whip,5)))
        GameAudio.update();check(sound.played.size==sweepBefore+1)
        check(loadedCues.containsAll(listOf(UiSound.AIR_SLASH_UPGRADED,UiSound.SPIN_ATTACK,UiSound.DEFENSIVE_BURST,
            UiSound.EARTHBREAKER_WAVE,UiSound.WHIP_SWEEP)))
        listOf(3,5).forEach{level->
            listOf("fireKnifeSwarm" to DefaultWeapons.dagger,"fireLongbowVolley" to DefaultWeapons.longbow,
                "fireHeavyBolt" to DefaultWeapons.crossbow).forEach{(name,definition)->
                audioTime+=4000;val before=sound.played.size
                val ctx=context(level).copy(weapon=WeaponInstance(definition,level))
                when(name) {
                    "fireKnifeSwarm" -> invokeAbility(name,ctx,0f)
                    "fireLongbowVolley" -> invokeAbility(name,ctx,player.centerX+100f,player.centerY,level==5)
                    else -> invokeAbility(name,ctx,player.centerX+100f,player.centerY)
                }
                GameAudio.update();check(sound.played.size==before+1){"Missing ranged ability cue: $name"}
            }
            val ctx=context(level).copy(weapon=WeaponInstance(com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.sling,level))
            audioTime+=4000;val before=sound.played.size
            physical.activate(ctx);GameAudio.update();check(sound.played.size==before){"Sling must sound on landing, not launch"}
            val owned=setOf(ctx.weapon.runtimeId)
            physical.update(.59f,owned);GameAudio.update();check(sound.played.size==before)
            audioTime+=600;physical.update(.02f,owned);GameAudio.update();check(sound.played.size==before+1)
            audioTime+=350;physical.update(.36f,owned);GameAudio.update()
            check(sound.played.size==before+if(level==5)2 else 1){"Sling impact must follow actual base/upgraded landings"}
            val boomerang=context(level).copy(weapon=WeaponInstance(com.azizoglu.grisilith.weapon.definition.catalog.NewPhysicalWeapons.boomerang,level))
            audioTime+=4000;val launchBefore=sound.played.size
            physical.activate(boomerang);GameAudio.update();check(sound.played.size==launchBefore+1)
        }
        check(UiSound.BOW_VOLLEY in loadedCues && UiSound.SLING_IMPACT in loadedCues)
        val magicalExecutor=ProjectileWeaponAttackExecutor(calculator,WeaponTargetSelector(),stats)
        val tomeExecutor=com.azizoglu.grisilith.weapon.runtime.executor.TomeOfEryndorAttackExecutor(
            calculator,WeaponTargetSelector(),magicalExecutor,stats)
        listOf(DefaultWeapons.ivyStaff,DefaultWeapons.steamWand,DefaultWeapons.orbOfSky,
            DefaultWeapons.totemOfDarkClouds,DefaultWeapons.enchantedChalkDust,DefaultWeapons.amuletOfTrollKing,
            DefaultWeapons.tomeOfEryndor).forEach{definition->listOf(1,4).forEach{level->
            audioTime+=4000;val before=sound.played.size
            val ctx=context(level).copy(weapon=WeaponInstance(definition,level))
            val attack=if(definition==DefaultWeapons.tomeOfEryndor)tomeExecutor else magicalExecutor
            attack.execute(ctx.copy(enemyManager=EnemyManager()));GameAudio.update();check(sound.played.size==before)
            attack.execute(ctx);GameAudio.update();check(sound.played.size==before+1){"Missing magical normal attack: ${definition.id} lv$level"}
        }}
        // Every damage type selects its own cue, independently of the weapon ID.
        com.azizoglu.grisilith.content.DamageType.entries.forEach{type->
            audioTime+=4000;GameAudio.magicalShot(type);GameAudio.update()
        }
        check(loadedCues.containsAll(UiSound.entries.filter{it.name.startsWith("MAGIC_")}))
        val effects=com.azizoglu.grisilith.item.runtime.ActiveItemEffects
        val inventory=com.azizoglu.grisilith.item.runtime.ItemInventory()
        repeat(5){inventory.addItem(com.azizoglu.grisilith.item.runtime.ItemInstance(
            com.azizoglu.grisilith.item.definition.DefaultItems.chaosTheory))}
        field(effects,"inventory").set(effects,inventory)
        try {
            val renderer=com.azizoglu.grisilith.weapon.visual.ProjectileSpriteRenderer()
            val resolver=renderer.javaClass.getDeclaredMethod("resolveVisualKind",Projectile::class.java).apply{isAccessible=true}
            repeat(30){
                audioTime+=4000
                val ctx=context(4).copy(weapon=WeaponInstance(DefaultWeapons.enchantedChalkDust,4))
                magicalExecutor.execute(ctx);GameAudio.update()
                val shot=ctx.projectiles.single()
                val visual=resolver.invoke(renderer,shot) as com.azizoglu.grisilith.weapon.visual.ProjectileVisualKind
                check(visual.name==shot.damageType.name)
                val resolved=effects.resolveDamageContext(com.azizoglu.grisilith.enemy.EnemyDamageContext(
                    damageType=shot.damageType,chaosDamageResolved=true))
                check(resolved.damageType==shot.damageType)
            }
            renderer.dispose()
        } finally {field(effects,"inventory").set(effects,null)}
        // Real focus channels: persistent field lifecycle, reaction replacement and cleanup.
        val manager=field(run,"weaponRuntimeManager").get(run)
        val focusFor=manager.javaClass.getDeclaredMethod("focusFor",WeaponInstance::class.java).apply{isAccessible=true}
        val channel=focusFor.invoke(manager,WeaponInstance(DefaultWeapons.orbOfSky,5))
        val vortex=field(channel,"vortex").get(channel) as com.azizoglu.grisilith.weapon.skill.VortexPullRuntime
        val pool=field(channel,"pool").get(channel) as com.azizoglu.grisilith.weapon.skill.AcidicPoolRuntime
        val clouds=field(channel,"clouds").get(channel) as com.azizoglu.grisilith.weapon.skill.StaticCloudsRuntime
        val sync=channel.javaClass.getDeclaredMethod("syncAudio").apply{isAccessible=true}
        check(vortex.tryBegin(player.centerX,player.centerY,emptyList(),3000f,3000f,true,com.badlogic.gdx.math.Vector2(500f,500f)))
        sync.invoke(channel);check(UiSound.VORTEX in loadedCues)
        val stopBefore=sound.stops
        check(vortex.activatePlagueStorm());sync.invoke(channel)
        check(UiSound.PLAGUE_STORM in loadedCues && sound.stops>stopBefore)
        check(pool.tryBegin(player.centerX,player.centerY,emptyList(),true,com.badlogic.gdx.math.Vector2(500f,500f)))
        check(pool.tryActivateVitriolicIgnition(500f,500f));sync.invoke(channel)
        check(UiSound.ACIDIC_POOL in loadedCues && UiSound.BURNING_POOL in loadedCues && UiSound.ACID_EXPLOSION in loadedCues)
        check(clouds.tryBegin(player,true));sync.invoke(channel);check(UiSound.STATIC_CLOUDS in loadedCues)
        channel.javaClass.getDeclaredMethod("clear").apply{isAccessible=true}.invoke(channel)
        val channels=field(audio,"channels").get(audio) as Map<*,*>
        check(channels.isEmpty())
        // Actual release runtimes: no cue on mere preparation; accepted effects own their voices.
        val spells=com.azizoglu.grisilith.weapon.glyph.GlyphSpellRuntime(stats)
        com.azizoglu.grisilith.weapon.glyph.GlyphType.entries.forEach{type->
            spells.cast(com.azizoglu.grisilith.weapon.glyph.GlyphCastRequest(type,500f,500f,
                WeaponInstance(DefaultWeapons.enchantedChalkDust,5)),enemies,3000f,3000f,{_,_->},{},true)
            val cue=when(type){
                com.azizoglu.grisilith.weapon.glyph.GlyphType.CALL_LIGHTNING->UiSound.GLYPH_LIGHTNING
                com.azizoglu.grisilith.weapon.glyph.GlyphType.METEOR->UiSound.GLYPH_METEOR
                com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI->UiSound.GLYPH_TSUNAMI
                com.azizoglu.grisilith.weapon.glyph.GlyphType.SINGULARITY->UiSound.GLYPH_SINGULARITY
            }
            check(cue in loadedCues && channels.isNotEmpty())
            // Run the real effect past all active/collapse/explosion phases, not just explicit clear.
            repeat(4){spells.update(1000f,player,enemies,emptyList(),3000f,3000f,{_,_->},{})}
            check(channels.isEmpty()){"Lingering effect sound: $type"}
            spells.clear();check(channels.isEmpty())
        }
        // Single chest: selected sound on the first press only, reveal press intentionally silent.
        val chest=field(run,"chestRewardController").get(run) as com.azizoglu.grisilith.screen.controller.ChestRewardController
        val queue=field(chest,"rewardQueue").get(chest) as com.azizoglu.grisilith.chest.ChestRewardQueue
        queue.addChest();chest.begin(false)
        val press=chest.javaClass.getDeclaredMethod("handlePrimaryChestAction").apply{isAccessible=true}
        audioTime+=4000;val chestBefore=sound.played.size
        press.invoke(chest);GameAudio.update();check(sound.played.size==chestBefore+1 && UiSound.CHEST in loadedCues)
        audioTime+=1000;press.invoke(chest);GameAudio.update();check(sound.played.size==chestBefore+1)
        ability.dispose();physical.dispose()
        game.showSettingsFromPausedRun(run)
        check(!GameAudio.inMenus() && !music.playing)
        val runSettings=game.screen
        runSettings.render(.016f);GameAudio.update();check(!music.playing)
        game.screen=run;runSettings.dispose()
        game.screen=menu;run.dispose()
        check(GameAudio.inMenus() && music.playing)
        val credits=AudioCreditsScreen(game);credits.resize(1280,720);game.screen=credits;menu.dispose()
        credits.render(.016f)
        Pixmap.createFromFrameBuffer(0,0,1280,720).also {
            PixmapIO.writePNG(Gdx.files.local("../tools/audio-audition/audio-credits.png"),it,-1,true);it.dispose()
        }
        check(GameSettings.snapshot()==original)
        game.dispose()
        println("AUDIO_SCREEN_SMOKE_PASS: menu/run/settings, all melee/ranged normal and ability cues, Sling base/upgraded landing timings, credits, disposal")
    }
}
