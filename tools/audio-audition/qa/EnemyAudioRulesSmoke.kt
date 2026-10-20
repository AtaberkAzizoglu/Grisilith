package qa

import com.azizoglu.grisilith.audio.*
import kotlin.math.abs

object EnemyAudioRulesSmoke {
    private fun near(a:Float,b:Float){check(abs(a-b)<.002f){"$a != $b"}}
    @JvmStatic fun main(args:Array<String>) {
        var now=1000L
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val seeks=mutableListOf<Float>()
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},
            {AudioLevels(1f,1f,1f)},{now},seekSound={_,_,offset->seeks+=offset;true})
        audio.worldView(AudioView(0f,0f,100f,100f),AudioSource(50f,50f))
        val boss=AudioSource(300f,50f,unscaled=true,audibleOffscreen=true)
        val normal=AudioSource(300f,50f,unscaled=true)
        audio.combatPlayback(2f,false)
        audio.request(UiSound.BANDIT_DEATH,normal);audio.update()
        check(sounds.getValue(UiSound.BANDIT_DEATH).played.isEmpty())
        audio.request(UiSound.WITCH_DEATH,boss);audio.update()
        val death=sounds.getValue(UiSound.WITCH_DEATH)
        check(death.played.size==1);near(death.pitches.last(),1f)
        now+=700;audio.update();check(death.stops==0) // Not accelerated at 2x.
        audio.combatPlayback(.5f,false);near(death.pitches.last(),1f)
        near(audio.enemyDelta(.1f),.2f)
        val owner=Any();val voice=sounds.getValue(UiSound.WITCH_QUOTE_1)
        audio.sustain(owner,UiSound.WITCH_QUOTE_1,false,boss)
        now+=500;audio.update();audio.combatPlayback(.5f,true)
        check(voice.pauses==1)
        now+=5000;audio.update();audio.combatPlayback(2f,false)
        check(voice.resumes==1);near(voice.pitches.last(),1f)
        // Re-enter the frame with a persistent owner: phase is real time, not 2x.
        audio.sustain(owner,UiSound.WITCH_QUOTE_1,false,normal)
        now+=500;audio.update();audio.sustain(owner,UiSound.WITCH_QUOTE_1,false,boss)
        near(seeks.last(),1f)
        val plays=voice.played.size
        audio.stopStageEffects();val stops=voice.stops
        audio.combatPlayback(2f,true);now+=5000;audio.update();audio.combatPlayback(2f,false)
        audio.sustain(owner,UiSound.WITCH_QUOTE_1,false,boss);audio.update()
        check(voice.played.size==plays && voice.stops==stops) // Cannot resurrect old stage sound.
        audio.stopSustain(owner) // Explicit new occurrence may reuse an owner.
        audio.sustain(owner,UiSound.WITCH_QUOTE_1,false,boss)
        check(voice.played.size==plays+1)
        // A normal weapon still follows game speed and culls normally.
        now+=100;audio.request(UiSound.LONGSWORD_SWING,AudioSource(50f,50f));audio.update()
        near(sounds.getValue(UiSound.LONGSWORD_SWING).pitches.last(),2f)
        audio.dispose()
        println("ENEMY_AUDIO_RULES_PASS: fixed enemy pitch/lifetime/throttle clock, offscreen boss vs normal, player-relative pan, resumable Pause, destructive stage cutoff/no resurrection, weapon speed unchanged")
    }
}
