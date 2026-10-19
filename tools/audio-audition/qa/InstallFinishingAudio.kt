package qa

import com.azizoglu.grisilith.audio.UiSound
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.abs

object InstallFinishingAudio {
    val selections=linkedMapOf(UiSound.SLING_SPLIT to "SLS4",UiSound.BOOMERANG_CATCH to "BRC5",
        UiSound.SHIELD_READY to "SR5",UiSound.BEAR_HURT to "BRH2",UiSound.GORILLA_HURT to "GRH1",
        UiSound.SPEARMAN_HURT to "SPH1",UiSound.HORSEMAN_HURT to "HOH3")
    @JvmStatic fun main(args:Array<String>) {
        selections.forEach{(cue,id)->
            val target=File("assets/audio/sfx/${cue.path}")
            File("tools/audio-audition/listening-pack-22/$id.wav").copyTo(target,overwrite=true)
            AudioSystem.getAudioInputStream(target).use{check(abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1){"${cue.path}: ${it.frameLength*1000.0/it.format.sampleRate}"}}
        }
        File("assets/audio/FINISHING-LICENSES.txt").writeText("INSTALLED PACK 22: "+selections.values.joinToString()+"\nSL4 interpreted as SLS4. Byte-exact chosen WAVs.\n"+
            "Swarm/Crocodile/Witch/Ghost/Slime hurt voices deliberately omitted.\n\n"+
            File("tools/audio-audition/finishing-source-credits.txt").readText(),Charsets.UTF_8)
        println("FINISHING_INSTALL_PASS: 7 exact selected cues; excluded bosses remain silent on damage")
    }
}
