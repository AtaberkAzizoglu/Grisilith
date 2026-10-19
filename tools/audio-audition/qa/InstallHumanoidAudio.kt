package qa

import java.io.File
import com.azizoglu.grisilith.audio.UiSound
import javax.sound.sampled.AudioSystem

object InstallHumanoidAudio {
    val selections=linkedMapOf(UiSound.BANDIT_DEATH to "BD4",UiSound.ARCHER_DEATH to "AD2",
        UiSound.GUARD_DEATH to "GD2",UiSound.SPEARMAN_DEATH to "SD1",UiSound.SPEARMAN_THROW to "ST3",
        UiSound.HORSEMAN_DEATH to "HD5",UiSound.HORSEMAN_ATTACK to "HA1",UiSound.HORSEMAN_SWIPE to "HS5",
        UiSound.MIMIC_TRANSFORM to "MX3")
    @JvmStatic fun main(args:Array<String>) {
        selections.forEach{(cue,code)->
            val source=File("tools/audio-audition/listening-pack-19/$code.wav")
            val target=File("assets/audio/sfx/${cue.path}")
            if(cue==UiSound.MIMIC_TRANSFORM)PrepareAudioAudition.wav(target,
                InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(source),850))
            else source.copyTo(target,overwrite=true)
            AudioSystem.getAudioInputStream(target).use {
                check(it.format.channels==1 && kotlin.math.abs(cue.durationMs-it.frameLength*1000.0/it.format.sampleRate)<1.1)
            }
        }
        File("assets/audio/HUMANOID-LICENSES.txt").writeText(
            "INSTALLED PACK 19: "+selections.values.joinToString(", ")+". MX3 fitted to 850ms transform; others exact.\n\n"+
                File("tools/audio-audition/humanoid-source-credits.txt").readText()
                    .replace("PACK 19 — 65 PREVIEW OPTIONS — NOT INSTALLED","PACK 19 SOURCE PROVENANCE — SELECTED CUES INSTALLED"),Charsets.UTF_8)
        println("HUMANOID_INSTALL_PASS: 9 approved cues, Guard death, 850ms Mimic transform")
    }
}
