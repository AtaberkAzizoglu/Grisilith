package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.content.ContentId
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import java.io.File
import kotlin.math.abs

object BeastAudioSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}}},
            Lwjgl3ApplicationConfiguration().apply{setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
                setPreferencesConfig("../tools/audio-audition/test-prefs",Files.FileType.Local)})
    }
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    private fun checks() {
        InstallBeastAudio.selections.forEach{(cue,code)->check(File("audio/sfx/${cue.path}").readBytes()
            .contentEquals(File("../tools/audio-audition/listening-pack-17/$code.wav").readBytes()))}
        var now=1000L;var levels=AudioLevels(1f,1f,1f)
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        fun flush(){GameAudio.update();now+=4000;GameAudio.update()}
        fun actor(id:ContentId,type:EnemyType=EnemyType.NORMAL)=Enemy(300f,400f,
            EnemyStats(type,100,10,0f,20f,0f,kindId=id))
        audio.worldView(AudioView(0f,0f,1000f,1000f),AudioSource(300f,400f))
        val ids=listOf(EnemyKindIds.GRAY_WOLF,EnemyKindIds.BALD_EAGLE,EnemyKindIds.BOAR,EnemyKindIds.CROCODILE,
            EnemyKindIds.BEE_SWARM,EnemyKindIds.GRIZZLY_BEAR,EnemyKindIds.SILVERBACK_GORILLA)
        val syncDeath=Enemy::class.java.getDeclaredMethod("playDeathAudioIfNeeded").apply{isAccessible=true}
        ids.forEach{id->
            val enemy=actor(id);val hit=EnemyAudio.attackCue(id)!!;val death=EnemyAudio.deathCue(id)!!
            EnemyAudio.attack(enemy,0);flush();check(sounds.getValue(hit).played.isEmpty())
            EnemyAudio.attack(enemy,5);flush();check(sounds.getValue(hit).played.size==1)
            enemy.takeDamage(10000);flush();check(sounds.getValue(death).played.size==1)
            syncDeath.invoke(enemy);syncDeath.invoke(enemy);flush();check(sounds.getValue(death).played.size==1)
            near(sounds.getValue(death).played.last(),.6f)
            for(type in listOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)) {
                actor(id,type).takeDamage(10000);flush();near(sounds.getValue(death).played.last(),.6f*1.35f)
            }
        }
        check(EnemyAudio.attackCue(EnemyKindIds.BANDIT)==UiSound.LONGSWORD_SWING && EnemyAudio.deathCue(EnemyKindIds.SKELETON_WARRIOR)==UiSound.SKELETON_DEATH)
        // Actual HP death outside takeDamage is still detected by the one-time death hook.
        val direct=actor(EnemyKindIds.GRAY_WOLF);direct.health.takeDamage(1000)
        syncDeath.invoke(direct);flush();check(sounds.getValue(UiSound.WOLF_DEATH).played.size==4)
        val loop=Any();audio.sustain(loop,UiSound.VORTEX,source=AudioSource(500f,400f))
        near(sounds.getValue(UiSound.VORTEX).pans.last(),.3f)
        // Camera center changes, player does not: stereo must not change.
        audio.worldView(AudioView(-200f,-100f,1400f,1200f),AudioSource(300f,400f))
        near(sounds.getValue(UiSound.VORTEX).pans.last(),.3f)
        audio.worldView(AudioView(-200f,-100f,1400f,1200f),AudioSource(700f,400f))
        near(sounds.getValue(UiSound.VORTEX).pans.last(),-.3f)
        levels=levels.copy(directionalStereo=false);audio.update();near(sounds.getValue(UiSound.VORTEX).pans.last(),0f)
        levels=levels.copy(directionalStereo=true);audio.update();near(sounds.getValue(UiSound.VORTEX).pans.last(),-.3f)
        val old=GameSettings.snapshot()
        check(GameSettings.defaults().directionalStereo)
        GameSettings.restore(old.copy(directionalStereo=false));GameSettings.load();check(!GameSettings.directionalStereo)
        GameSettings.restore(old.copy(directionalStereo=true));GameSettings.load();check(GameSettings.directionalStereo)
        GameSettings.restore(old)
        audio.stopSustain(loop)
        actor(EnemyKindIds.SILVERBACK_GORILLA,EnemyType.FINAL_BOSS).takeDamage(10000)
        GameAudio.update()
        val deathPauses=sounds.getValue(UiSound.GORILLA_DEATH).pauses
        val deathStops=sounds.getValue(UiSound.GORILLA_DEATH).stops
        audio.combatPlayback(1f,true,deathTails=true)
        check(sounds.getValue(UiSound.GORILLA_DEATH).pauses==deathPauses)
        now+=2200;audio.update();check(sounds.getValue(UiSound.GORILLA_DEATH).stops>deathStops)
        audio.combatPlayback(1f,false)
        check(SummonAudio.attackCue(com.azizoglu.grisilith.summon.SummonType.GUARD)==UiSound.LANCE_THRUST)
        check(SummonAudio.deathCue(com.azizoglu.grisilith.summon.SummonType.GUARD)==UiSound.GUARD_DEATH)
        GameAudio.dispose()
        println("BEAST_AUDIO_SMOKE_PASS: exact assets, positive damage gate, death once, +35% mini/final boss gain, player-relative/camera-independent stereo, live toggle and persisted default-on setting")
    }
}
