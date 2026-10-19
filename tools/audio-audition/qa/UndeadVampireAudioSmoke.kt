package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.enemy.trait.*
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.combat.condition.*
import com.azizoglu.grisilith.summon.*
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.azizoglu.grisilith.statistics.PlayerStats
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.math.Rectangle
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.abs

object UndeadVampireAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},
            Lwjgl3ApplicationConfiguration().apply{setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
                setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)})
    }
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    private fun checks() {
        InstallUndeadVampireAudio.selections.forEach{(name,selected)->
            val cue=UiSound.entries.single{it.path=="$name.wav"};val file=File("audio/sfx/${cue.path}")
            if(name !in InstallUndeadVampireAudio.fitted)check(file.readBytes().contentEquals(
                File("../tools/audio-audition/listening-pack-${selected.first}/${selected.second}.wav").readBytes()))
            AudioSystem.getAudioInputStream(file).use{check(abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1)}
        }
        check(File("audio/UNDEAD-VAMPIRE-LICENSES.txt").readText().contains("xl57"))
        var now=1000L;var levels=AudioLevels(1f,1f,1f)
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,4000f,4000f),AudioSource(500f,500f))
        val factory=EnemyRuntimeFactory(EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)})
        fun actor(def:EnemyKindDefinition)=factory.create(def,def.allowedTypes.first(),1,400f,400f).also{e->
            // The real Enemy.update sets these before combat; this test drives trait updates directly.
            for(field in listOf("lastWorldWidth","lastWorldHeight"))
                Enemy::class.java.getDeclaredField(field).apply{isAccessible=true}.setFloat(e,4000f)
        }
        fun settle(){now+=10000;GameAudio.update()}
        fun count(c:UiSound)=sounds.getValue(c).played.size
        val nameField=Enemy::class.java.getDeclaredField("audioAttackSourceName").apply{isAccessible=true}
        fun hit(e:Enemy,c:UiSound,name:String=""){
            settle();nameField.set(e,name);val n=count(c)
            EnemyAudio.attack(e,0);GameAudio.update();check(count(c)==n)
            EnemyAudio.attack(e,3);GameAudio.update();check(count(c)==n+1)
            near(sounds.getValue(c).played.last(),if(e.type in setOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)).81f else .6f)
        }
        for(def in listOf(DefaultEnemyKinds.zombie,DefaultEnemyKinds.ghost,DefaultEnemyKinds.mimic)) {
            val e=actor(def);hit(e,EnemyAudio.attackCue(e.kindId)!!)
        }
        for(def in DefaultEnemyKinds.all){check(EnemyAudio.deathCue(def.id)!=null){"Missing death ${def.id}"}}
        for(def in listOf(DefaultEnemyKinds.witch,DefaultEnemyKinds.skeletonWarrior,DefaultEnemyKinds.skeletonArcher,
            DefaultEnemyKinds.zombie,DefaultEnemyKinds.ghost,DefaultEnemyKinds.mimic)) {
            settle();val e=actor(def);val c=EnemyAudio.deathCue(e.kindId)!!;val n=count(c)
            // Isolate the death hook from random Ghost evasion / disguised Mimic targetability.
            e.health.takeDamage(100000000)
            Enemy::class.java.getDeclaredMethod("playDeathAudioIfNeeded").apply{isAccessible=true}.invoke(e)
            GameAudio.update();check(count(c)==n+1){"No death cue ${def.id}"}
            Enemy::class.java.getDeclaredMethod("playDeathAudioIfNeeded").apply{isAccessible=true}.invoke(e)
            GameAudio.update();check(count(c)==n+1)
        }
        val big=actor(DefaultEnemyKinds.slime);val fragment=factory.createSlimeFragment(big,450f,450f)
        for(e in listOf(big,fragment)){settle();val n=count(UiSound.SLIME_DEATH);e.takeDamage(100000000);GameAudio.update();check(count(UiSound.SLIME_DEATH)==n+1)}
        hit(actor(DefaultEnemyKinds.silverbackGorilla),UiSound.GORILLA_STOMP,"Stomp the Earth")

        val runtime=Enemy::class.java.getDeclaredField("traitRuntime").apply{isAccessible=true}
        fun tick(e:Enemy,dt:Float=.05f,far:Boolean=false){
            val x=e.centerX+(if(far)1400f else 10f);val y=e.centerY+10f
            EnemyAudio.update(e,dt)
            (runtime.get(e) as EnemyTraitRuntime).update(dt,e,listOf(e),x,y,Rectangle(x-5,y-5,10f,10f),4000f,4000f,emptyList(),
                listOf(EnemyTraitCombatTarget(EnemyTraitTargetType.PLAYER,centerX=x,centerY=y,bounds=Rectangle(x-5,y-5,10f,10f),maximumHealth=100)))
            now+=(dt*1000).toLong();GameAudio.update()
        }
        settle();val ghost=actor(DefaultEnemyKinds.ghost);val stormBefore=count(UiSound.GHOST_STORM)
        repeat(110){tick(ghost)};check(count(UiSound.GHOST_STORM)==stormBefore+1)
        val stopped=sounds.getValue(UiSound.GHOST_STORM).stops
        repeat(125){tick(ghost)};check(sounds.getValue(UiSound.GHOST_STORM).stops>stopped)
        val whispers=count(UiSound.GHOST_ATTACK);nameField.set(ghost,"Ghost Storm")
        EnemyAudio.attack(ghost,2);GameAudio.update();check(count(UiSound.GHOST_ATTACK)==whispers)
        EnemyAudio.clear(ghost)

        val vampire=actor(DefaultEnemyKinds.vampireLord)
        hit(vampire,UiSound.VAMPIRE_BITE);hit(vampire,UiSound.LONGSWORD_SWING,"Vampire Longsword Cone")
        hit(vampire,UiSound.LANCE_THRUST,"Swipe Around")
        hit(vampire,UiSound.VAMPIRE_HUNGER,"Vampire Hunger")
        val hunger=count(UiSound.VAMPIRE_HUNGER);EnemyAudio.attack(vampire,2);GameAudio.update();check(count(UiSound.VAMPIRE_HUNGER)==hunger)
        settle();EnemyAudio.update(vampire,2f);val hurt=count(UiSound.VAMPIRE_HURT)
        vampire.takeDamage(1);GameAudio.update();check(count(UiSound.VAMPIRE_HURT)==hurt+1)
        vampire.takeDamage(1);GameAudio.update();check(count(UiSound.VAMPIRE_HURT)==hurt+1)
        settle();val deaths=count(UiSound.VAMPIRE_DEATH)
        vampire.takeDamage(100000000);GameAudio.update();check(!vampire.isDead && count(UiSound.VAMPIRE_DEATH)==deaths)
        tick(vampire);check(count(UiSound.VAMPIRE_MIST)==1)
        vampire.teleportCenter(vampire.vampireEscapeTargetX,vampire.vampireEscapeTargetY,4000f,4000f)
        tick(vampire);check(vampire.isVampireTransforming && count(UiSound.VAMPIRE_FORM)==1)
        Enemy::class.java.getDeclaredField("vampireTransformationRemainingSeconds").apply{isAccessible=true}.setFloat(vampire,0f)
        repeat(35){tick(vampire)};check(count(UiSound.VAMPIRE_CONE_CHARGE)>0)
        repeat(40){tick(vampire)};check(count(UiSound.VAMPIRE_SWIPE_CHARGE)>0)
        repeat(110){tick(vampire,far=true)};check(count(UiSound.VAMPIRE_TELEPORT)>=2)
        val stops=sounds.getValue(UiSound.VAMPIRE_TELEPORT).stops
        EnemyAudio.clear(vampire);check(sounds.getValue(UiSound.VAMPIRE_TELEPORT).stops>=stops)
        vampire.setVampireTeleportLocked(false)
        settle();vampire.takeDamage(100000000);GameAudio.update();check(vampire.isDead && count(UiSound.VAMPIRE_DEATH)==deaths+1)
        near(sounds.getValue(UiSound.VAMPIRE_DEATH).played.last(),.81f)

        val summon=SummonedEntity("qa","qa","qa",SummonType.GUARD,SummonStats(100,10,100f,100f,1f),500f,500f,20f)
        summon.applyCondition(ConditionApplication(ConditionType.ACIDIC_PATH_DAMAGE,3f,
            ConditionSource(damageType=DamageType.ACID,tickDamage=1,sourceEnemyBoss=true)))
        val manager=SummonManager(WeaponInventory(),PlayerStats(),{1})
        @Suppress("UNCHECKED_CAST") val entities=SummonManager::class.java.getDeclaredField("entities").apply{isAccessible=true}.get(manager) as MutableList<SummonedEntity>
        entities+=summon;settle();val paths=count(UiSound.SLIME_PATH)
        manager.update(.51f,500f,500f,4000f,4000f,emptyList(),mutableListOf(),{_,_->},{},{_,_->});GameAudio.update()
        check(count(UiSound.SLIME_PATH)==paths+1);near(sounds.getValue(UiSound.SLIME_PATH).played.last(),.81f)
        settle();EnemyAudio.pathDamage(0,500f,500f);GameAudio.update();check(count(UiSound.SLIME_PATH)==paths+1)
        levels=AudioLevels(.5f,1f,.4f);settle();EnemyAudio.pathDamage(1,500f,500f);GameAudio.update();near(sounds.getValue(UiSound.SLIME_PATH).played.last(),.12f)
        manager.clear();GameAudio.dispose()
        println("UNDEAD_VAMPIRE_AUDIO_SMOKE_PASS: exact selected assets/metadata, every enemy death covered, large+small Slime, hit gates/boss gain, Ghost phase cleanup, real Vampire phases/teleport/charges/death, hurt/drain throttles, actual acid condition damage and Settings")
    }
}
