package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.enemy.trait.EnemyTraitRuntime
import com.azizoglu.grisilith.summon.SummonType
import com.azizoglu.grisilith.content.DamageType
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Rectangle
import java.io.File
import kotlin.math.abs

object HumanoidAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},
            Lwjgl3ApplicationConfiguration().apply{setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
                setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)})
    }
    private fun checks() {
        InstallHumanoidAudio.selections.forEach{(cue,code)->if(cue!=UiSound.MIMIC_TRANSFORM)
            check(File("audio/sfx/${cue.path}").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-19/$code.wav").readBytes()))}
        var now=1000L
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{AudioLevels(1f,1f,1f)},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,2000f,2000f),AudioSource(500f,500f))
        val factory=EnemyRuntimeFactory(EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)})
        fun actor(def:EnemyKindDefinition)=factory.create(def,def.allowedTypes.first(),1,400f,400f)
        fun settle(){now+=10000;GameAudio.update()}
        fun hit(e:Enemy,cue:UiSound,type:DamageType=DamageType.PIERCING){
            settle();val before=sounds.getValue(cue).played.size
            EnemyAudio.attack(e,0,type);GameAudio.update();check(sounds.getValue(cue).played.size==before)
            EnemyAudio.attack(e,3,type);GameAudio.update();check(sounds.getValue(cue).played.size==before+1)
            val expected=if(e.type in setOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)).95f else .6f
            check(abs(sounds.getValue(cue).played.last()-expected)<.0001f)
        }
        for(def in listOf(DefaultEnemyKinds.bandit,DefaultEnemyKinds.archer,DefaultEnemyKinds.spearman,DefaultEnemyKinds.horseman)){
            val e=actor(def);hit(e,EnemyAudio.attackCue(e.kindId)!!)
            settle();val cue=EnemyAudio.deathCue(e.kindId)!!;val before=sounds.getValue(cue).played.size
            e.takeDamage(100000000);GameAudio.update();check(sounds.getValue(cue).played.size==before+1)
            Enemy::class.java.getDeclaredMethod("playDeathAudioIfNeeded").apply{isAccessible=true}.invoke(e)
            GameAudio.update();check(sounds.getValue(cue).played.size==before+1)
        }
        val name=Enemy::class.java.getDeclaredField("audioAttackSourceName").apply{isAccessible=true}
        val spear=actor(DefaultEnemyKinds.spearman);name.set(spear,"Throw the Spear");hit(spear,UiSound.SPEARMAN_THROW)
        val horse=actor(DefaultEnemyKinds.horseman);name.set(horse,"Swipe Around");hit(horse,UiSound.HORSEMAN_SWIPE)
        hit(actor(DefaultEnemyKinds.skeletonWarrior),UiSound.LONGSWORD_SWING)
        hit(actor(DefaultEnemyKinds.skeletonArcher),UiSound.BOW_SHOT)
        val slime=actor(DefaultEnemyKinds.slime)
        for(source in listOf("Slime Spit","Acid Projectile")){name.set(slime,source);hit(slime,UiSound.MAGIC_ACID,DamageType.ACID)}
        settle();name.set(slime,"Acidic Path");val acidCount=sounds.getValue(UiSound.MAGIC_ACID).played.size
        EnemyAudio.attack(slime,3,DamageType.ACID);GameAudio.update();check(sounds.getValue(UiSound.MAGIC_ACID).played.size==acidCount)
        check(SummonAudio.deathCue(SummonType.GUARD)==UiSound.GUARD_DEATH)
        val mimic=actor(DefaultEnemyKinds.mimic)
        val runtime=Enemy::class.java.getDeclaredField("traitRuntime").apply{isAccessible=true}.get(mimic) as EnemyTraitRuntime
        settle()
        repeat(30){runtime.update(.05f,mimic,listOf(mimic),mimic.centerX+5,mimic.centerY+5,
            Rectangle(mimic.centerX,mimic.centerY,20f,20f),2000f,2000f,emptyList());now+=50;GameAudio.update()}
        check(sounds.getValue(UiSound.MIMIC_TRANSFORM).played.size==1)
        check(sounds.getValue(UiSound.MIMIC_TRANSFORM).stops>0)
        EnemyAudio.clear(mimic);GameAudio.dispose()
        println("HUMANOID_AUDIO_SMOKE_PASS: exact selections, actual trait transform once/cleanup, hit-only normal/ability cues, boss gain, Guard death, skeleton reuse, Slime element reuse without path misfire")
    }
}
