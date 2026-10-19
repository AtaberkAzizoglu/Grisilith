package qa

import java.io.File
import javax.sound.sampled.AudioSystem

/** Install exact approved auditions: no re-rendering or changes to their timbre. */
object InstallSummonAudio {
    val selections=mapOf("cow_attack" to "CA5","cow_death" to "CD5","imp_death" to "ID1",
        "oathbreaker_attack" to "OA2","oathbreaker_death" to "OD2")
    @JvmStatic fun main(args:Array<String>) {
        selections.forEach{(name,code)->
            val source=File("tools/audio-audition/listening-pack-16/$code.wav")
            AudioSystem.getAudioInputStream(source).use {
                check(it.format.channels==1 && it.format.sampleRate==44100f && it.frameLength>0)
            }
            source.copyTo(File("assets/audio/sfx/$name.wav"),overwrite=true)
        }
        println("SUMMON_INSTALL_PASS: CA5/CD5/ID1/OA2/OD2 exact copies; Imp reuses magic_fire.wav")
    }
}
