package qa

import java.io.File
import com.azizoglu.grisilith.audio.UiSound
import javax.sound.sampled.AudioSystem

object InstallBeastAudio {
    val selections=linkedMapOf(
        UiSound.TOAD_DEATH to "TD5",UiSound.EAGLE_ATTACK to "EA2",UiSound.EAGLE_DEATH to "ED5",
        UiSound.WOLF_ATTACK to "WA5",UiSound.WOLF_DEATH to "WD3",UiSound.BOAR_ATTACK to "BA4",UiSound.BOAR_DEATH to "BD2",
        UiSound.CROCODILE_ATTACK to "CA3",UiSound.CROCODILE_DEATH to "CD4",UiSound.SWARM_ATTACK to "SA3",UiSound.SWARM_DEATH to "SD3",
        UiSound.BEAR_ATTACK to "RA2",UiSound.BEAR_DEATH to "RD4",UiSound.GORILLA_ATTACK to "GA1",UiSound.GORILLA_DEATH to "GD1")
    @JvmStatic fun main(args:Array<String>) {
        selections.forEach{(cue,code)->
            val from=File("tools/audio-audition/listening-pack-17/$code.wav")
            AudioSystem.getAudioInputStream(from).use{
                check(it.format.channels==1 && it.format.sampleRate==44100f)
                val duration=it.frameLength*1000.0/it.format.sampleRate
                check(kotlin.math.abs(cue.durationMs-duration)<1.1){"${cue.name} duration metadata"}
            }
            from.copyTo(File("assets/audio/sfx/${cue.path}"),overwrite=true)
        }
        val credits=File("tools/audio-audition/beast-combat-source-credits.txt").readText()
            .replace("BEAST COMBAT + TOAD AUDITIONS — PACK 17 — NOT INSTALLED","BEAST COMBAT + TOAD DEATH — INSTALLED PACK 17 SELECTIONS")
        File("assets/audio/BEAST-COMBAT-LICENSES.txt").writeText(
            credits+"\nInstalled selections: "+selections.values.joinToString(", ")+".\n",Charsets.UTF_8)
        println("BEAST_INSTALL_PASS: exact 15 approved selections, durations and licenses")
    }
}
