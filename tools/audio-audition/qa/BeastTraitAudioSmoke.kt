package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime
import com.azizoglu.grisilith.content.DamageType
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Rectangle
import java.io.File
import kotlin.math.abs

object BeastTraitAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},
            Lwjgl3ApplicationConfiguration().apply{setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
                setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)})
    }
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    private fun checks() {
        InstallBeastTraitAudio.selections.forEach{(cue,id)->
            val installed=File("audio/sfx/${cue.path}").readBytes();val selected=File("../tools/audio-audition/listening-pack-18/$id.wav")
            if(cue!=UiSound.GORILLA_PREPARE)check(installed.contentEquals(selected.readBytes()))
        }
        var now=1000L
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{AudioLevels(1f,1f,1f)},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,2000f,2000f),AudioSource(500f,500f))
        val registry=EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)}
        val factory=EnemyRuntimeFactory(registry)
        fun enemy(def:EnemyKindDefinition,type:EnemyType)=factory.create(def,type,1,400f,400f)
        val runtimeField=Enemy::class.java.getDeclaredField("traitRuntime").apply{isAccessible=true}
        fun tick(actor:Enemy,dt:Float=.05f) {
            EnemyAudio.update(actor,dt)
            (runtimeField.get(actor) as EnemyTraitRuntime).update(dt,actor,listOf(actor),600f,500f,
                Rectangle(590f,490f,20f,20f),2000f,2000f,emptyList())
            now+=(dt*1000).toLong();GameAudio.update()
        }
        fun await(actor:Enemy,cue:UiSound,previous:Int=0) {
            repeat(400){if(sounds.getValue(cue).played.size>previous)return;tick(actor)}
            error("No real trait cue $cue")
        }
        val boar=enemy(DefaultEnemyKinds.boar,EnemyType.NORMAL)
        await(boar,UiSound.BOAR_PREPARE);near(sounds.getValue(UiSound.BOAR_PREPARE).played.last(),.6f)
        await(boar,UiSound.BOAR_CHARGE)
        check(sounds.getValue(UiSound.BOAR_PREPARE).stops>0)
        repeat(28){tick(boar)};check(sounds.getValue(UiSound.BOAR_CHARGE).stops>0)
        val croc=enemy(DefaultEnemyKinds.crocodile,EnemyType.MINI_BOSS)
        await(croc,UiSound.CROCODILE_PREPARE);near(sounds.getValue(UiSound.CROCODILE_PREPARE).played.last(),.81f)
        await(croc,UiSound.CROCODILE_JUMP);near(sounds.getValue(UiSound.CROCODILE_JUMP).played.last(),.81f)
        val activeStops=sounds.getValue(UiSound.CROCODILE_JUMP).stops
        croc.takeDamage(100000000);GameAudio.update();check(sounds.getValue(UiSound.CROCODILE_JUMP).stops>activeStops)
        val gorilla=enemy(DefaultEnemyKinds.silverbackGorilla,EnemyType.FINAL_BOSS)
        await(gorilla,UiSound.GORILLA_PREPARE);await(gorilla,UiSound.GORILLA_THROW)
        near(sounds.getValue(UiSound.GORILLA_THROW).played.last(),.81f)
        val bear=enemy(DefaultEnemyKinds.grizzlyBear,EnemyType.MINI_BOSS)
        bear.health.takeDamage(bear.health.maximumHealth*3/5);tick(bear)
        check(sounds.getValue(UiSound.BEAR_RAGE).played.size==1)
        repeat(20){tick(bear)};check(sounds.getValue(UiSound.BEAR_RAGE).played.size==1)
        val swarm=enemy(DefaultEnemyKinds.beeSwarm,EnemyType.MINI_BOSS)
        swarm.takeDamage(50,EnemyDamageContext(damageType=DamageType.PIERCING,projectileAttack=true));GameAudio.update()
        check(sounds.getValue(UiSound.SWARM_RESIST).played.size==1)
        val manager=EnemyManager();manager.spawn(swarm)
        swarm.requestSwarmSplit()
        manager.spawnPendingSpecialEnemies(factory,2000f,2000f,emptyList());now+=100;GameAudio.update()
        check(sounds.getValue(UiSound.SWARM_SPLIT).played.size==1)
        val mother=enemy(DefaultEnemyKinds.crocodile,EnemyType.MINI_BOSS);manager.spawn(mother)
        mother.requestBabyCrocodileSpawn();manager.spawnPendingSpecialEnemies(factory,2000f,2000f,emptyList());now+=100;GameAudio.update()
        check(sounds.getValue(UiSound.CROCODILE_EGG).played.size==1)
        manager.clearAll()
        for((def,cue) in listOf(DefaultEnemyKinds.bandit to UiSound.LONGSWORD_SWING,
            DefaultEnemyKinds.archer to UiSound.BOW_SHOT,DefaultEnemyKinds.spearman to UiSound.LANCE_THRUST)) {
            now+=4000;GameAudio.update();val source=enemy(def,def.allowedTypes.first());val before=sounds.getValue(cue).played.size
            EnemyAudio.attack(source,0);GameAudio.update();check(sounds.getValue(cue).played.size==before)
            EnemyAudio.attack(source,3);GameAudio.update();check(sounds.getValue(cue).played.size==before+1)
            near(sounds.getValue(cue).played.last(),if(source.type==EnemyType.MINI_BOSS).81f else .6f)
        }
        now+=4000;GameAudio.update()
        val witch=enemy(DefaultEnemyKinds.witch,EnemyType.FINAL_BOSS)
        EnemyAudio.attack(witch,0,DamageType.FIRE);GameAudio.update();check(sounds.getValue(UiSound.MAGIC_FIRE).played.isEmpty())
        EnemyAudio.attack(witch,5,DamageType.FIRE);GameAudio.update();near(sounds.getValue(UiSound.MAGIC_FIRE).played.last(),.81f)
        val nameField=Enemy::class.java.getDeclaredField("audioAttackSourceName").apply{isAccessible=true}
        for((name,cue) in listOf("Meteor" to UiSound.GLYPH_METEOR,"Call Lightning" to UiSound.GLYPH_LIGHTNING,
            "Tsunami" to UiSound.GLYPH_TSUNAMI,"Static Cloud" to UiSound.STATIC_CLOUDS)) {
            now+=4000;GameAudio.update();EnemyAudio.update(witch,4f);nameField.set(witch,name)
            EnemyAudio.attack(witch,5,DamageType.LIGHTNING);GameAudio.update();near(sounds.getValue(cue).played.last(),.81f)
        }
        listOf(boar,bear,gorilla,witch).forEach(EnemyAudio::clear)
        GameAudio.dispose()
        println("BEAST_TRAIT_AUDIO_SMOKE_PASS: real rush/windup/death cleanup, rage once, resist/split/egg, humanoid hit-only reuse, Witch element/spell cues, all boss cues +35%")
    }
}
