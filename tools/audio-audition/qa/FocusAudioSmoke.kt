package qa

import com.azizoglu.grisilith.audio.*
import com.azizoglu.grisilith.weapon.glyph.*
import com.azizoglu.grisilith.weapon.runtime.WeaponInstance
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.statistics.PlayerStats
import java.io.File
import kotlin.math.abs

/** No speakers/network/saves. Real runtime cast boundaries plus fake backend lifecycle checks. */
object FocusAudioSmoke {
    private fun near(a:Float,b:Float)=check(abs(a-b)<.0001f){"$a != $b"}
    @JvmStatic fun main(args:Array<String>) {
        var levels=AudioLevels(.5f,.8f,.4f);var now=1000L
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now})
        val owner=Any();val other=Any()
        audio.combatPlayback(.5f,false)
        audio.sustain(owner,UiSound.VORTEX)
        val vortex=sounds.getValue(UiSound.VORTEX)
        near(vortex.played.single(),.2f*UiSound.VORTEX.gain*1.35f*1.1f);near(vortex.pitches.last(),.5f)
        repeat(20){audio.sustain(owner,UiSound.VORTEX);now+=100;audio.update()}
        check(vortex.played.size==1){"Loop restarted per frame"}
        audio.combatPlayback(2f,false);near(vortex.pitches.last(),2f)
        audio.combatPlayback(2f,true);check(vortex.pauses==1)
        now+=10000;audio.update();check(vortex.stops==0)
        audio.combatPlayback(.5f,false);check(vortex.resumes==1)
        audio.sustain(other,UiSound.VORTEX);check(vortex.played.size==2)
        audio.stopSustain(owner);check(vortex.stops==1)
        audio.sustain(other,UiSound.PLAGUE_STORM);check(vortex.stops==2)
        check(sounds.getValue(UiSound.PLAGUE_STORM).played.size==1)
        levels=levels.copy(effects=0f);audio.update();check(sounds.getValue(UiSound.PLAGUE_STORM).volumes.last()==0f)
        levels=levels.copy(effects=.4f);audio.update();check(sounds.getValue(UiSound.PLAGUE_STORM).played.size==1)
        audio.stopSustain(other)
        // Real Chalk runtime: each selected voice starts only on accepted casts, and stops at
        // exactly the drawing end, cancellation or clear, including without a spell target.
        GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,audio)
        ChalkDustSpellChoice.entries.forEach{choice->
            val cast=GlyphMotionRuntime(PlayerStats());val weapon=WeaponInstance(DefaultWeapons.enchantedChalkDust,3)
            check(weapon.chooseChalkDustSpell(choice))
            val cue=GameAudio.castCue(choice.glyphType);val sound=sounds.getValue(cue)
            val plays=sound.played.size;val stops=sound.stops
            check(cast.handleActivation(weapon)==GlyphMotionRuntime.ActivationResult.STARTED)
            check(cast.handleActivation(weapon)==GlyphMotionRuntime.ActivationResult.REJECTED)
            check(sound.played.size==plays+1)
            var resolved=0
            fun tick(dt:Float)=cast.update(dt,50f,50f,emptyList(),{_,_->},{},{resolved++})
            tick(2.49f);check(cast.isDrawing && sound.stops==stops)
            tick(.02f);check(!cast.isDrawing && resolved==1 && sound.stops==stops+1)
            check(cast.consumePendingCastRequest()?.glyphType==choice.glyphType)
            cast.clear();check(cast.handleActivation(weapon)==GlyphMotionRuntime.ActivationResult.STARTED)
            cast.cancelActiveDrawing();check(sound.stops==stops+2)
            check(cast.consumePendingCastRequest()==null)
            check(cast.handleActivation(weapon)==GlyphMotionRuntime.ActivationResult.STARTED)
            cast.clear();check(sound.stops==stops+3)
        }
        // UI choice cues must work while combat is paused, without starting an actual spell.
        audio.combatPlayback(1f,true);now+=100
        GameAudio.glyphChoice();audio.update()
        check(sounds.getValue(UiSound.CONFIRM).played.size==1)
        audio.combatPlayback(1f,false)
        val channelOwner=Any();audio.sustain(channelOwner,UiSound.ACIDIC_POOL)
        val pool=sounds.getValue(UiSound.ACIDIC_POOL)
        audio.pause();check(pool.pauses==1);audio.resume();check(pool.resumes==1)
        audio.setMenuContext(true);check(pool.stops==1)
        // Selected assets, durations, loop seams and casting starts.
        val selected=mapOf("focus_vortex" to "11/VP4-loop","focus_clouds" to "11/SC3-loop",
            "focus_acid" to "11/AP2-loop","focus_plague" to "11/PS4-loop","focus_burning" to "11/BL5-loop",
            "focus_animal" to "12/AN4","focus_demon" to "12/DE1","focus_oathbreaker" to "12/OA1")
        selected.forEach{(name,id)->check(File("assets/audio/sfx/$name.wav").readBytes().contentEquals(File("tools/audio-audition/listening-pack-$id.wav").readBytes()))}
        mapOf("focus_ignite" to 780,"focus_erasing" to 650,"focus_acid_explosion" to 780,
            "glyph_lightning" to 860,"glyph_meteor" to 860).forEach{(name,ms)->
            check(InstallMagicNormalAudio.read(File("assets/audio/sfx/$name.wav")).size==44100*ms/1000)
        }
        mapOf("cast_lightning" to "13/HL2","cast_tsunami" to "14/SV2","cast_singularity" to "14/SV3").forEach{(name,id)->
            check(File("assets/audio/sfx/$name.wav").readBytes().contentEquals(File("tools/audio-audition/listening-pack-$id.wav").readBytes()))
        }
        check(InstallMagicNormalAudio.read(File("assets/audio/sfx/cast_meteor.wav")).size==110250)
        listOf("focus_vortex","focus_clouds","focus_acid","focus_plague","focus_burning","glyph_tsunami","glyph_singularity").forEach{
            val pcm=InstallMagicNormalAudio.read(File("assets/audio/sfx/$it.wav"));check(abs(pcm.first()-pcm.last())<.0001f)
        }
        audio.dispose();GameAudio::class.java.getDeclaredField("controller").apply{isAccessible=true}.set(null,null)
        println("FOCUS_AUDIO_SMOKE_PASS: cast completion/cancel, independent owners, transform, bounded lifetimes, .5x/2x, pause/resume, mute/live volume, paused UI choices, exact approved assets and seams.")
    }
}
