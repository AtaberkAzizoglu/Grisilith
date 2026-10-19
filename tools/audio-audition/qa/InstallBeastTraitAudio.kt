package qa

import java.io.File
import com.azizoglu.grisilith.audio.UiSound
import javax.sound.sampled.AudioSystem

object InstallBeastTraitAudio {
    val selections=linkedMapOf(UiSound.BOAR_CHARGE to "BC1",UiSound.BOAR_PREPARE to "BP2",
        UiSound.CROCODILE_JUMP to "CJ2",UiSound.CROCODILE_PREPARE to "CP1",UiSound.CROCODILE_EGG to "EG5",
        UiSound.SWARM_RESIST to "SB3",UiSound.SWARM_SPLIT to "SS3",UiSound.BEAR_RAGE to "RG2",
        UiSound.GORILLA_PREPARE to "GC3",UiSound.GORILLA_THROW to "GT4")
    @JvmStatic fun main(args:Array<String>) {
        selections.forEach{(cue,code)->
            val source=File("tools/audio-audition/listening-pack-18/$code.wav")
            val target=File("assets/audio/sfx/${cue.path}")
            if(cue==UiSound.GORILLA_PREPARE) {
                // Preserve the selected growl's strongest onset inside the actual 0.5s windup.
                PrepareAudioAudition.wav(target,InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(source),500))
            } else source.copyTo(target,overwrite=true)
            AudioSystem.getAudioInputStream(target).use {
                check(it.format.channels==1 && kotlin.math.abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1)
            }
        }
        val licenses=File("tools/audio-audition/beast-trait-source-credits.txt").readText()
            .replace("BEAST TRAIT / ABILITY AUDITIONS — PACK 18 — NOT INSTALLED","BEAST TRAIT AUDIO — INSTALLED PACK 18 SELECTIONS")
        File("assets/audio/BEAST-TRAIT-LICENSES.txt").writeText(licenses+
            "\nInstalled: "+selections.values.joinToString(", ")+". GC3 fitted to 500ms windup; other selections exact.\n",Charsets.UTF_8)
        println("BEAST_TRAIT_INSTALL_PASS: 10 approved cues; GC3 windup fit; Stomp remains unselected")
    }
}
