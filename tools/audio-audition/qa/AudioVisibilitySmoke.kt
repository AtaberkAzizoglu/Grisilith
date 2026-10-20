package qa

import com.azizoglu.grisilith.audio.*
import kotlin.math.abs

/** Silent fake-backend checks of screen culling, virtual phase, gain and UI exceptions. */
object AudioVisibilitySmoke {
    private fun near(a:Float,b:Float){check(abs(a-b)<.002f){"$a != $b"}}
    @JvmStatic fun main(args:Array<String>) {
        var now=1000L;var levels=AudioLevels(1f,1f,.5f)
        val sounds=UiSound.entries.associateWith{AudioSmoke.FakeSound()}
        val seeks=mutableListOf<Float>()
        val audio=AudioController({AudioSmoke.FakeMusic().api},{sounds.getValue(it).api},{levels},{now},
            seekSound={_,_,seconds->seeks+=seconds;true})
        val view=AudioView(0f,0f,100f,100f);val player=AudioSource(50f,50f)
        val off=AudioSource(300f,50f,10f);val on=AudioSource(50f,50f,10f);val owner=Any()
        check(view.contains(AudioSource(110f,50f,10f)))
        check(!view.contains(AudioSource(110f,110f,10f)))
        audio.worldView(view,player);audio.combatPlayback(1f,false)
        audio.sustain(owner,UiSound.VORTEX,source=off)
        val wind=sounds.getValue(UiSound.VORTEX);check(wind.played.isEmpty())
        now+=1250;audio.update();audio.sustain(owner,UiSound.VORTEX,source=on)
        near(seeks.last(),1.25f);check(wind.played.size==1)
        near(wind.volumes.last(),.5f*.32f*1.35f*1.1f)
        audio.sustain(owner,UiSound.VORTEX,source=off);check(wind.stops==1)
        now+=1000;audio.update();audio.sustain(owner,UiSound.VORTEX,source=on)
        near(seeks.last(),2.25f)
        audio.sustain(owner,UiSound.VORTEX,source=off)
        audio.combatPlayback(2f,false);now+=500;audio.update()
        audio.sustain(owner,UiSound.VORTEX,source=on)
        near(seeks.last(),3.25f%(UiSound.VORTEX.durationMs/1000f))
        audio.sustain(owner,UiSound.VORTEX,source=off)
        audio.combatPlayback(2f,true);now+=5000;audio.update()
        audio.combatPlayback(.5f,false);now+=500;audio.update()
        audio.sustain(owner,UiSound.VORTEX,source=on)
        near(seeks.last(),3.5f%(UiSound.VORTEX.durationMs/1000f))
        // Muting virtualizes too; unmute seeks forward instead of replaying from the beginning.
        levels=levels.copy(effects=0f);audio.update();now+=1000;audio.update()
        levels=levels.copy(effects=.5f);audio.update()
        near(seeks.last(),4f%(UiSound.VORTEX.durationMs/1000f))
        // Finished hidden one-shots must not suddenly play when their former location enters view.
        val burst=Any();audio.sustain(burst,UiSound.GLYPH_METEOR,false,off)
        now+=3000;audio.update();audio.sustain(burst,UiSound.GLYPH_METEOR,false,on)
        check(sounds.getValue(UiSound.GLYPH_METEOR).played.isEmpty())
        // Off-screen point events suppressed; menu/popup UI remains exempt from world culling.
        audio.request(UiSound.SLING_IMPACT,off,true);audio.update()
        check(sounds.getValue(UiSound.SLING_IMPACT).played.isEmpty())
        audio.request(UiSound.CLICK,off);audio.update();check(sounds.getValue(UiSound.CLICK).played.size==1)
        // Shared normal/ability samples: only the ability gets the new base gain.
        now+=1000;audio.request(UiSound.DAGGER_THROW,on);audio.update()
        val dagger=sounds.getValue(UiSound.DAGGER_THROW);val normal=dagger.played.last()
        now+=1000;audio.request(UiSound.DAGGER_THROW,on,true);audio.update()
        near(dagger.played.last(),normal*1.35f)
        check(UiSound.GLYPH_SINGULARITY.gain/.35f>2.4f)
        audio.dispose()
        // Portable fallback: same ID keeps running at zero volume, no freeze/restart on re-entry.
        val fallbackSound=AudioSmoke.FakeSound()
        val fallback=AudioController({AudioSmoke.FakeMusic().api},{fallbackSound.api},{AudioLevels(1f,1f,1f)},{now})
        fallback.worldView(view,player);fallback.sustain(owner,UiSound.VORTEX,source=off)
        check(fallbackSound.played.single()==0f)
        now+=10000;fallback.update();fallback.sustain(owner,UiSound.VORTEX,source=on)
        check(fallbackSound.played.size==1 && fallbackSound.volumes.last()>0f && fallbackSound.pauses==0)
        fallback.dispose()
        println("AUDIO_VISIBILITY_SMOKE_PASS: hidden/no-output, correct re-entry phase, speed/pause/mute, expired bursts, circle visibility, shared ability gain and UI exceptions.")
    }
}
