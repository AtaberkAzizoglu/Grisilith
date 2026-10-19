package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.summon.*
import com.azizoglu.grisilith.weapon.Projectile
import com.azizoglu.grisilith.weapon.runtime.WeaponInventory
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import java.io.File
import kotlin.math.abs

/** Hidden native context, fake audio output; no saves, network or audible test sounds. */
object SummonAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter() {
            override fun create() {
                try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}
            }
        },Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)
        })
    }
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    private fun checks() {
        InstallSummonAudio.selections.forEach{(name,code)->check(
            File("audio/sfx/$name.wav").readBytes().contentEquals(
                File("../tools/audio-audition/listening-pack-16/$code.wav").readBytes()))}
        check(ProjectileSound.entries.size==13)
        check(ProjectileSound.magical(DamageType.FIRE)?.cue==UiSound.MAGIC_FIRE)
        check(ProjectileSound.magical(DamageType.BLUDGEONING)==null)
        check(SummonAudio.attackCue(SummonType.IMP)==null)
        check(SummonAudio.deathCue(SummonType.TOAD)==UiSound.TOAD_DEATH)
        var now=1000L;var levels=AudioLevels(1f,1f,1f)
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
        val controller=GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}
        controller.set(null,audio)
        audio.worldView(AudioView(0f,0f,1000f,1000f),AudioSource(500f,500f))
        fun flush(){GameAudio.update();now+=3000;GameAudio.update()}
        fun summon(type:SummonType,lifetime:Float=10f)=SummonedEntity("qa","qa","qa",type,
            SummonStats(100,10,100f,100f,1f),100f,500f,lifetime)
        val attack=SummonedEntity::class.java.getDeclaredMethod("tryAttack",Float::class.javaPrimitiveType,
            kotlin.jvm.functions.Function0::class.java).apply{isAccessible=true}
        for((type,cue) in listOf(SummonType.COW to UiSound.COW_ATTACK, SummonType.KNIGHT_SPIRIT to UiSound.OATHBREAKER_ATTACK,
            SummonType.GUARD to UiSound.LANCE_THRUST)) {
            val actor=summon(type);var attacks=0;val action:()->Unit={attacks++}
            check(attack.invoke(actor,1f,action)==true);flush()
            check(sounds.getValue(cue).played.size==1)
            near(sounds.getValue(cue).pans.first(),-.6f)
            check(attack.invoke(actor,1f,action)==false);flush()
            check(attacks==1 && sounds.getValue(cue).played.size==1)
        }
        val manager=SummonManager(WeaponInventory(),PlayerStats(),{1})
        val entitiesField=SummonManager::class.java.getDeclaredField("entities").apply{isAccessible=true}
        @Suppress("UNCHECKED_CAST") val entities=entitiesField.get(manager) as MutableList<SummonedEntity>
        fun tick(){manager.update(.02f,500f,500f,1000f,1000f,emptyList(),mutableListOf(),{_,_->},{},{_,_->});flush()}
        for(type in listOf(SummonType.COW,SummonType.IMP,SummonType.KNIGHT_SPIRIT, SummonType.TOAD, SummonType.GUARD)) {
            val actor=summon(type);actor.health.takeDamage(100);entities+=actor;tick()
            val cue=SummonAudio.deathCue(type)!!;check(sounds.getValue(cue).played.size==1)
            tick();check(sounds.getValue(cue).played.size==1 && entities.isEmpty())
        }
        entities+=summon(SummonType.COW,.001f);tick()
        check(entities.isEmpty() && sounds.getValue(UiSound.COW_DEATH).played.size==1)
        entities+=summon(SummonType.IMP);manager.clear();flush()
        check(sounds.getValue(UiSound.IMP_DEATH).played.size==1)
        val imp=summon(SummonType.IMP);val projectiles=mutableListOf<Projectile>()
        val launch=SummonManager::class.java.getDeclaredMethod("spawnImpFireProjectile",SummonedEntity::class.java,
            Float::class.javaPrimitiveType,Float::class.javaPrimitiveType,MutableList::class.java).apply{isAccessible=true}
        launch.invoke(manager,imp,800f,500f,projectiles);flush()
        check(projectiles.size==1 && sounds.getValue(UiSound.MAGIC_FIRE).played.size==1)
        imp.health.takeDamage(100);launch.invoke(manager,imp,800f,500f,projectiles);flush()
        check(projectiles.size==1 && sounds.getValue(UiSound.MAGIC_FIRE).played.size==1)
        GameAudio.projectile(ProjectileSound.FIRE,900f,500f);flush()
        val toad=summon(SummonType.TOAD)
        val poisonLaunch=SummonManager::class.java.getDeclaredMethod("spawnToadPoisonProjectile",SummonedEntity::class.java,
            Float::class.javaPrimitiveType,Float::class.javaPrimitiveType,MutableList::class.java).apply{isAccessible=true}
        poisonLaunch.invoke(manager,toad,800f,500f,projectiles);flush()
        check(projectiles.size==2 && sounds.getValue(UiSound.MAGIC_POISON).played.size==1)
        check(sounds.getValue(UiSound.MAGIC_FIRE).played.size==2)
        near(sounds.getValue(UiSound.MAGIC_FIRE).pans.last(),.6f)
        GameAudio.projectile(ProjectileSound.FIRE,500f,500f)
        GameAudio.projectile(ProjectileSound.FIRE,2000f,500f);flush()
        check(sounds.getValue(UiSound.MAGIC_FIRE).played.size==3)
        near(sounds.getValue(UiSound.MAGIC_FIRE).pans.last(),0f)
        val owner=Any()
        GameAudio.sustain(owner,UiSound.VORTEX,source=AudioSource(100f,500f))
        near(sounds.getValue(UiSound.VORTEX).pans.last(),-.6f)
        GameAudio.sustain(owner,UiSound.VORTEX,source=AudioSource(900f,500f))
        near(sounds.getValue(UiSound.VORTEX).pans.last(),.6f)
        GameAudio.combatPlayback(.5f,false);near(sounds.getValue(UiSound.VORTEX).pitches.last(),.5f)
        GameAudio.combatPlayback(2f,false);near(sounds.getValue(UiSound.VORTEX).pitches.last(),2f)
        levels=AudioLevels(.5f,1f,.4f);GameAudio.update()
        near(sounds.getValue(UiSound.VORTEX).volumes.last(),.2f*.32f*1.35f)
        GameAudio.play(UiSound.CLICK);GameAudio.update();near(sounds.getValue(UiSound.CLICK).pans.last(),0f)
        GameAudio.dispose()
        println("SUMMON_AUDIO_SMOKE_PASS: exact assets, real attack cooldown/death cleanup, no expiry cry, real Imp fire launch, shared cues, stereo/update, speed and Settings gain")
    }
}
