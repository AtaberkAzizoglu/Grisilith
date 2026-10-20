package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.badlogic.gdx.utils.JsonReader
import java.io.File
import kotlin.math.abs

object AudioPolishSmoke {
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    @JvmStatic fun main(args:Array<String>) {
        var now=1000L;var levels=AudioLevels(1f,1f,1f)
        val music=AudioSmoke.FakeMusic()
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now},
            loadRunMusic={music.api})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,1000f,1000f),AudioSource(500f,500f))
        fun actor(type:EnemyType,kind:com.azizoglu.grisilith.content.ContentId,x:Float=500f)=
            Enemy(x,500f,EnemyStats(type,100,10,0f,20f,0f,kindId=kind))
        fun death(type:EnemyType){actor(type,EnemyKindIds.GRAY_WOLF).takeDamage(100000);GameAudio.update()}
        val wolf=sounds.getValue(UiSound.WOLF_DEATH)
        death(EnemyType.NORMAL);near(wolf.played.last(),.27f)
        now+=100;death(EnemyType.UNIQUE);check(wolf.played.size==1)
        now+=289;death(EnemyType.UNIQUE);check(wolf.played.size==2);near(wolf.played.last(),.3024f)
        now+=389;death(EnemyType.NORMAL);check(wolf.played.size==3)
        // Hidden normals never consume the audible crowd budget.
        now+=389;actor(EnemyType.NORMAL,EnemyKindIds.GRAY_WOLF,3000f).takeDamage(100000);GameAudio.update()
        death(EnemyType.NORMAL);check(wolf.played.size==4)
        now+=5000;GameAudio.update() // Finish the unrelated crowd transients before testing boss gain.
        val boss=actor(EnemyType.MINI_BOSS,EnemyKindIds.CROCODILE,3000f)
        EnemyAudio.play(boss,UiSound.CROCODILE_ATTACK);GameAudio.update()
        near(sounds.getValue(UiSound.CROCODILE_ATTACK).played.last(),.95f)
        GameAudio.runMusic(MusicTrack.FOREST_NORMAL);near(music.volume,.27f)
        val witch=actor(EnemyType.FINAL_BOSS,EnemyKindIds.WITCH,3000f)
        EnemyAudio.spawned(witch);GameAudio.update()
        val quote=CharacterVoices.witchQuotes.single{sounds.getValue(it).played.isNotEmpty()}
        near(sounds.getValue(quote).played.last(),1f);near(music.volume,.27f*.45f)
        now+=1000;EnemyAudio.play(actor(EnemyType.NORMAL,EnemyKindIds.BANDIT),UiSound.LONGSWORD_SWING);GameAudio.update()
        near(sounds.getValue(UiSound.LONGSWORD_SWING).played.last(),.6f*.65f)
        // Reserved portal notification: offscreen, amid 12 owned effects, not ducked by boss speech.
        repeat(12){GameAudio.sustain(Any(),UiSound.VORTEX,source=AudioSource(500f,500f))}
        GameAudio.portalOpened(5000f,500f);GameAudio.update()
        near(sounds.getValue(UiSound.PORTAL_OPEN).played.single(),.8f)
        repeat(4){now+=50;GameAudio.playAt(UiSound.LONGSWORD_SWING,500f,500f);GameAudio.update()}
        check(sounds.getValue(UiSound.PORTAL_OPEN).stops==0)
        GameAudio.stopStageEffects();check(sounds.getValue(UiSound.PORTAL_OPEN).stops==1)
        GameAudio.combatPlayback(1f,false)
        now+=5000;EnemyAudio.stageContext(com.azizoglu.grisilith.stage.StageType.MINI_BOSS)
        death(EnemyType.NORMAL);near(wolf.played.last(),.24f)
        val deaths=wolf.played.size
        now+=437;death(EnemyType.NORMAL);check(wolf.played.size==deaths)
        now+=1;death(EnemyType.NORMAL);check(wolf.played.size==deaths+1)
        EnemyAudio.stageContext(com.azizoglu.grisilith.stage.StageType.NORMAL)
        EnemyAudio.clear(witch);GameAudio.update();near(music.volume,.27f)
        levels=AudioLevels(.4f,.5f,.3f);GameAudio.update();near(music.volume,.054f)
        GameAudio.dispose()
        val summary=JsonReader().parse(File("audio/SUMMARY-MUSIC-SELECTIONS.json").readText())
        check(summary.size==2)
        for((track,code) in listOf(MusicTrack.SUMMARY_DEFEAT to "DF3",MusicTrack.SUMMARY_VICTORY to "VC17")) {
            val row=summary.single{it.getString("code")==code}
            check(!track.looping && row.getString("title")==track.title)
            check(File("audio/music/${track.path}").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-29/$code.mp3").readBytes()))
        }
        val audit=File("audio/MUSIC-ATTRIBUTION-AUDIT.txt").readText()
        MusicTrack.entries.forEach{check(it.title in audit)}
        println("AUDIO_POLISH_PASS: 350ms real-time crowd vocal cap, half-volume normals, exact +12% Unique, hidden-source budget protection, louder offscreen boss, final speech prominence/duck/release, lowered map/music Settings, exact DF3/VC17 and all 13 attributions")
    }
}
