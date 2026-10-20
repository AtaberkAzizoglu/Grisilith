package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.enemy.*
import com.azizoglu.grisilith.enemy.definition.*
import com.azizoglu.grisilith.enemy.runtime.EnemyRuntimeFactory
import com.azizoglu.grisilith.player.Player
import com.azizoglu.grisilith.statistics.PlayerStats
import com.azizoglu.grisilith.screen.preset.PresetJournalScreen
import com.azizoglu.grisilith.GrisilithGame
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.utils.JsonReader
import java.io.File
import java.security.MessageDigest
import javax.sound.sampled.AudioSystem
import kotlin.math.abs

/** Actual HP/preset/boss flows, deterministic output device, isolated preferences. */
object CharacterVoiceSmoke {
    @JvmStatic fun main(args:Array<String>) {
        Lwjgl3Application(object:ApplicationAdapter(){override fun create(){
            try{checks();Gdx.app.exit()}catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)}
        }},Lwjgl3ApplicationConfiguration().apply{
            setWindowedMode(320,240);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/audio-audition/character-voice-test-prefs",Files.FileType.Local)
        })
    }
    private fun near(a:Float,b:Float){check(abs(a-b)<.0001f){"$a != $b"}}
    private fun hash(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    private fun pcm(file:File):ByteArray=AudioSystem.getAudioInputStream(file).use{it.readBytes()}
    private fun checks() {
        val manifest=JsonReader().parse(File("audio/CHARACTER-VOICE-SELECTIONS.json").readText())
        check(manifest.size==53)
        for(row in manifest) {
            val cue=UiSound.valueOf(row.getString("enum"));val file=File("audio/sfx/${cue.path}")
            check(hash(file.readBytes())==row.getString("sha256"))
            check(cue.path==row.getString("path"))
            val codes=row.get("source_codes").map{it.asString()}
            val originals=codes.map{File("../tools/audio-audition/listening-pack-27/$it.wav")}
            if(codes.size==1)check(file.readBytes().contentEquals(originals.single().readBytes()))
            else {check(codes==listOf("VL27D08","VL27D01"));check(pcm(file).contentEquals(originals.flatMap{pcm(it).asIterable()}.toByteArray()))}
            AudioSystem.getAudioInputStream(file).use{
                check(it.format.channels==1 && it.format.sampleRate==44100f)
                check(abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1)
            }
        }
        check(!File("audio/sfx/vampire_death.wav").readBytes().contentEquals(File("../tools/audio-audition/listening-pack-21/VS5.wav").readBytes()))
        var now=1000L;var levels=AudioLevels(1f,1f,1f)
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        audio.worldView(AudioView(0f,0f,4000f,4000f),AudioSource(500f,500f))
        fun count(cue:UiSound)=sounds.getValue(cue).played.size
        fun count(pool:List<UiSound>)=pool.sumOf{count(it)}
        fun settle(ms:Long=10000){now+=ms;GameAudio.update()}

        // Real journal selection, not merely mapping tests; no quote on automatic show.
        audio.setMenuContext(true)
        val game=GrisilithGame()
        val journal=PresetJournalScreen(game,createNew=true)
        journal.show()
        check(CharacterVoices.selectionCues.sumOf{count(it)}==0)
        val slot=journal.javaClass.declaredClasses.single{it.simpleName=="Slot"}.enumConstants.first{it.toString()=="CHARACTER"}
        val open=journal.javaClass.getDeclaredField("openSlot").apply{isAccessible=true}
        val select=journal.javaClass.getDeclaredMethod("selectContent",ContentDefinition::class.java).apply{isAccessible=true}
        for(character in DefaultCharacters.all) {
            settle();open.set(journal,slot)
            val set=CharacterVoices.characters.getValue(character.id.value);val before=count(set.selection)
            select.invoke(journal,character);GameAudio.update();check(count(set.selection)==before+1)
            near(sounds.getValue(set.selection).pans.last(),0f)
            near(sounds.getValue(set.selection).pitches.last(),1f)
            select.invoke(journal,character);GameAudio.update();check(count(set.selection)==before+1)
        }
        val selected=CharacterVoices.characters.getValue(DefaultCharacters.all.last().id.value).selection
        val stops=sounds.getValue(selected).stops;journal.hide();check(sounds.getValue(selected).stops>stops)
        journal.dispose()
        audio.setMenuContext(false);audio.combatPlayback(1f,false)

        for((id,set) in CharacterVoices.characters) {
            settle()
            val p=Player(476f,476f,4000f,4000f,PlayerStats(),ContentId(id))
            val before=count(set.hurt)
            p.health.takeDamage(0);p.health.heal(100);GameAudio.update();check(count(set.hurt)==before)
            p.sacrificeHalfCurrentHealth();GameAudio.update();check(count(set.hurt)==before)
            p.health.heal(100)
            p.health.takeDamage(1);GameAudio.update();check(count(set.hurt)==before+1)
            p.health.takeDamage(1);GameAudio.update();check(count(set.hurt)==before+1)
            p.health.heal(100)
            now+=3000;p.update(3f);p.health.takeDamage(1);GameAudio.update();check(count(set.hurt)==before+2)
            val hurtBeforeDeath=count(set.hurt);val deaths=count(set.death)
            p.health.takeDamage(1000000);GameAudio.update();check(count(set.hurt)==hurtBeforeDeath && count(set.death)==deaths+1)
            p.health.takeDamage(1);GameAudio.update();check(count(set.death)==deaths+1)
            // Death must finish even while the Summary freezes combat.
            audio.combatPlayback(1f,true,deathTails=true)
            val stopped=sounds.getValue(set.death).stops
            now+=set.death.durationMs+1;GameAudio.update();check(sounds.getValue(set.death).stops>stopped)
            audio.combatPlayback(1f,false);p.stopVoiceAudio()
            // Deterministic round-robin proves every selected hurt variant is reachable.
            var pick=0;val runtime=PlayerVoiceRuntime(ContentId(id)){(pick++)%it}
            val counts=set.hurt.map(::count)
            set.hurt.indices.forEach { i->
                settle();runtime.update(10f,500f,500f);runtime.damaged(1,false,500f,500f);GameAudio.update()
                check(count(set.hurt[i])==counts[i]+1)
            }
            runtime.clear()
        }
        val factory=EnemyRuntimeFactory(EnemyKindRegistry().apply{registerAll(DefaultEnemyKinds.all)})
        fun actor(def:EnemyKindDefinition)=factory.create(def,def.allowedTypes.first(),1,700f,500f).also { enemy ->
            // Production initializes these during the first world update.
            listOf("lastWorldWidth", "lastWorldHeight").forEach { field ->
                Enemy::class.java.getDeclaredField(field).apply { isAccessible=true }.setFloat(enemy,4000f)
            }
        }
        for(def in listOf(DefaultEnemyKinds.witch,DefaultEnemyKinds.vampireLord)) {
            audio.combatPlayback(2f,false)
            settle();val enemy=actor(def)
            val pool=if(def==DefaultEnemyKinds.witch)CharacterVoices.witchQuotes else CharacterVoices.vampireQuotes
            val hurt=if(def==DefaultEnemyKinds.witch)CharacterVoices.witchHurt else CharacterVoices.vampireHurt
            val manager=EnemyManager();val before=count(pool);val perCueBefore=pool.associateWith(::count)
            manager.spawn(enemy);GameAudio.update();check(count(pool)==before+1)
            EnemyAudio.spawned(enemy);GameAudio.update();check(count(pool)==before+1)
            val active=pool.single{count(it)>perCueBefore.getValue(it)}
            near(sounds.getValue(active).played.last(),.81f)
            // Per-source stereo updates with motion, not camera movement.
            enemy.teleportCenter(800f,500f,4000f,4000f);EnemyAudio.update(enemy,0f);GameAudio.update()
            near(sounds.getValue(active).pans.last(),.45f)
            levels=levels.copy(directionalStereo=false);GameAudio.update();near(sounds.getValue(active).pans.last(),0f)
            levels=levels.copy(directionalStereo=true)
            now+=29990;EnemyAudio.update(enemy,59.98f);GameAudio.update();check(count(pool)==before+1)
            val beforeSecond=pool.associateWith(::count)
            now+=20;EnemyAudio.update(enemy,.04f);GameAudio.update();check(count(pool)==before+2)
            val second=pool.single{count(it)>beforeSecond.getValue(it)}
            // Pause the newly started second line, not a quote that has finished.
            val plays=count(pool);val pauses=pool.sumOf{sounds.getValue(it).pauses}
            audio.combatPlayback(.5f,true);now+=5000;GameAudio.update()
            check(pool.sumOf{sounds.getValue(it).pauses}>pauses)
            audio.combatPlayback(2f,false);check(count(pool)==plays)
            near(sounds.getValue(second).pitches.last(),1f) // Enemy voices ignore game speed.
            val h=count(hurt);enemy.takeDamage(0);GameAudio.update();check(count(hurt)==h)
            enemy.takeDamage(1);GameAudio.update();check(count(hurt)==h+1)
            enemy.takeDamage(1);GameAudio.update();check(count(hurt)==h+1)
            now+=4000;EnemyAudio.update(enemy,4f);enemy.takeDamage(1);GameAudio.update();check(count(hurt)==h+2)
            val hurtCue=hurt.filter{count(it)>0}.maxBy{sounds.getValue(it).played.size}
            near(sounds.getValue(hurtCue).played.last(),.81f)
            near(sounds.getValue(hurtCue).pitches.last(),1f)
            // The exception is vocal-only: even boss attacks remain game-speed coupled.
            now+=1000;EnemyAudio.play(enemy,UiSound.LONGSWORD_SWING);GameAudio.update()
            near(sounds.getValue(UiSound.LONGSWORD_SWING).pitches.last(),2f)
            // Removing an alive boss closes owned lines and leaves no actor state.
            manager.remove(enemy)
            val stateField=EnemyAudio::class.java.getDeclaredField("bossVoices").apply{isAccessible=true}
            check(!(stateField.get(EnemyAudio) as Map<*,*>).containsKey(enemy))
        }
        audio.combatPlayback(1f,false)
        settle();val firstForm=actor(DefaultEnemyKinds.vampireLord);val d=count(UiSound.VAMPIRE_DEATH)
        firstForm.takeDamage(10000000);GameAudio.update();check(!firstForm.isDead && count(UiSound.VAMPIRE_DEATH)==d)
        EnemyAudio.clear(firstForm)
        settle();val finalForm=actor(DefaultEnemyKinds.vampireLord)
        Enemy::class.java.getDeclaredField("isVampireSecondForm").apply{isAccessible=true}.setBoolean(finalForm,true)
        val deathStops=sounds.getValue(UiSound.VAMPIRE_DEATH).stops
        finalForm.takeDamage(10000000)
        // A busy combat frame cannot drop the final, two-part death voice.
        listOf(UiSound.LONGSWORD_SWING,UiSound.BOW_SHOT,UiSound.MAGIC_FIRE).forEach(GameAudio::play)
        GameAudio.update();check(finalForm.isDead && count(UiSound.VAMPIRE_DEATH)==d+1)
        EnemyAudio.clear(finalForm);audio.combatPlayback(1f,true,deathTails=true)
        now+=UiSound.VAMPIRE_DEATH.durationMs/2;GameAudio.update();check(sounds.getValue(UiSound.VAMPIRE_DEATH).stops==deathStops)
        now+=UiSound.VAMPIRE_DEATH.durationMs;GameAudio.update();check(sounds.getValue(UiSound.VAMPIRE_DEATH).stops>deathStops)
        // Settings are live for the new owner-bound vocal lanes too.
        audio.combatPlayback(1f,false);settle();levels=AudioLevels(.5f,1f,.4f)
        val runtime=PlayerVoiceRuntime(ContentId("CHR_SIR_IMBERT")){0}
        runtime.damaged(1,false,500f,500f);GameAudio.update();near(sounds.getValue(UiSound.IMBERT_HURT_1).played.last(),.13f)
        levels=AudioLevels(1f,1f,0f);GameAudio.update();near(sounds.getValue(UiSound.IMBERT_HURT_1).volumes.last(),0f)
        runtime.clear();GameAudio.dispose()
        println("CHARACTER_VOICE_SMOKE_PASS: 53 exact assets, gapless ordered Vampire PCM, real journal choices, five actual HP paths, full random pools/shared throttles, death-only lethal hits, spawn/30s/cleanup, live positional stereo, pause/speed, Settings, death-priority and Summary tails")
    }
}
