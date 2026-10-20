package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.skill.DefensiveLineRuntime
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Vector2
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.abs

/** Native hidden context, fake per-cue mixer, no network or audible playback. */
object FinishingAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){
            try { checks();Gdx.app.exit() } catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}
        }},Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)
        })
    }
    private fun checks() {
        InstallFinishingAudio.selections.forEach { (cue,id) ->
            val file=File("audio/sfx/${cue.path}")
            check(file.readBytes().contentEquals(File("../tools/audio-audition/listening-pack-22/$id.wav").readBytes()))
            AudioSystem.getAudioInputStream(file).use { check(abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1) }
        }
        var now=1000L
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{AudioLevels(1f,1f,1f)},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,4000f,4000f),AudioSource(500f,500f))
        GameAudio.combatPlayback(1f,false)
        fun count(c:UiSound)=sounds.getValue(c).played.size
        fun settle(){now+=10000;GameAudio.update()}
        val factory=EnemyRuntimeFactory(EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)})
        fun actor(d:EnemyKindDefinition)=factory.create(d,d.allowedTypes.first(),1,400f,400f)
        val hurt=listOf(DefaultEnemyKinds.grizzlyBear to UiSound.BEAR_HURT,
            DefaultEnemyKinds.silverbackGorilla to UiSound.GORILLA_HURT,
            DefaultEnemyKinds.spearman to UiSound.SPEARMAN_HURT,
            DefaultEnemyKinds.horseman to UiSound.HORSEMAN_HURT)
        hurt.forEach { (definition,cue) ->
            val e=actor(definition);settle();val n=count(cue)
            e.takeDamage(0);GameAudio.update();check(count(cue)==n)
            e.takeDamage(1);GameAudio.update();check(count(cue)==n+1){"Missing hurt ${definition.id}"}
            check(abs(sounds.getValue(cue).played.last()-.81f)<.0001f)
            e.takeDamage(1);GameAudio.update();check(count(cue)==n+1)
            EnemyAudio.update(e,1f);settle();e.takeDamage(1);GameAudio.update();check(count(cue)==n+2)
            EnemyAudio.update(e,1f);settle();e.takeDamage(100000000);GameAudio.update();check(count(cue)==n+2)
            EnemyAudio.clear(e)
        }
        val hurtCues=hurt.map{it.second}+CharacterVoices.vampireHurt+CharacterVoices.witchHurt
        listOf(DefaultEnemyKinds.beeSwarm,DefaultEnemyKinds.crocodile,
            DefaultEnemyKinds.ghost,DefaultEnemyKinds.slime).forEach { def ->
            settle();val before=hurtCues.sumOf(::count);val e=actor(def)
            EnemyAudio.hurt(e);GameAudio.update();check(hurtCues.sumOf(::count)==before){"Excluded hurt ${def.id}"}
            EnemyAudio.clear(e)
        }
        val stats=PlayerStats();val player=Player(500f,500f,4000f,4000f,stats)
        val shield=DefensiveLineRuntime(stats){1};val equipment=listOf(WeaponInstance(DefaultWeapons.shield,5))
        settle();val ready=count(UiSound.SHIELD_READY)
        shield.update(30f,player,equipment);GameAudio.update();check(count(UiSound.SHIELD_READY)==ready+1)
        settle();shield.update(30f,player,equipment);GameAudio.update();check(count(UiSound.SHIELD_READY)==ready+1)
        shield.reset();settle();shield.update(30f,player,equipment);GameAudio.update();check(count(UiSound.SHIELD_READY)==ready+2)
        shield.reset()
        fun context(w:WeaponInstance)=WeaponAttackContext(w,0,player,player.centerX,player.centerY,EnemyManager(),
            mutableListOf<Projectile>(),{_,_->},{},{},{_,d->d},{it},{false})
        for(level in listOf(1,4)) {
            val runtime=ExtendedPhysicalWeaponRuntime(stats)
            val c=context(WeaponInstance(DefaultWeapons.boomerang,level))
            val launch=runtime.javaClass.getDeclaredMethod("launchBoomerang",WeaponAttackContext::class.java,Vector2::class.java,Boolean::class.javaPrimitiveType).apply{isAccessible=true}
            settle();val before=count(UiSound.BOOMERANG_CATCH)
            launch.invoke(runtime,c,Vector2(player.centerX+100f,player.centerY),true)
            val interval=c.normalAttackIntervalSeconds.coerceAtLeast(.1f)
            runtime.update(interval+.0001f,setOf(c.weapon.runtimeId));GameAudio.update()
            check(count(UiSound.BOOMERANG_CATCH)==before+if(level==1)1 else 0)
            settle();runtime.update(interval+.01f,setOf(c.weapon.runtimeId));GameAudio.update()
            check(count(UiSound.BOOMERANG_CATCH)==before+1)
            settle();runtime.update(20f,setOf(c.weapon.runtimeId));GameAudio.update();check(count(UiSound.BOOMERANG_CATCH)==before+1)
            runtime.clear()
        }
        // Inject the existing collision callback request: one sound for the split,
        // never one sound per generated fragment or again on the following frame.
        val physical=ExtendedPhysicalWeaponRuntime(stats)
        val c=context(WeaponInstance(DefaultWeapons.sling,4))
        val victim=Enemy(600f,500f,EnemyStats(EnemyType.NORMAL,1000,0,0f,20f,0f))
        val fragmentClass=physical.javaClass.declaredClasses.single{it.simpleName=="FragmentRequest"}
        val request=fragmentClass.declaredConstructors.single().apply{isAccessible=true}.newInstance(c,victim,Vector2(600f,500f))
        @Suppress("UNCHECKED_CAST")
        val queue=physical.javaClass.getDeclaredField("fragments").apply{isAccessible=true}.get(physical) as MutableList<Any>
        queue+=request;settle();val split=count(UiSound.SLING_SPLIT)
        physical.update(.01f,setOf(c.weapon.runtimeId));GameAudio.update();check(count(UiSound.SLING_SPLIT)==split+1)
        check(c.projectiles.size in 3..8)
        settle();physical.update(.01f,setOf(c.weapon.runtimeId));GameAudio.update();check(count(UiSound.SLING_SPLIT)==split+1)
        physical.clear();GameAudio.dispose()
        println("FINISHING_AUDIO_SMOKE_PASS: exact 7 cues, real positive hurt/nonlethal/throttle/boss gain, 4 exclusions (Witch superseded by pack27), Shield max once/rearm, Boomerang final catch only, Sling one split cue")
    }
}
