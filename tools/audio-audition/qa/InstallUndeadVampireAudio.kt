package qa

import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.*

object InstallUndeadVampireAudio {
    val selections=linkedMapOf(
        "skeleton_death" to (20 to "SK3"),"zombie_attack" to (20 to "ZA1"),"zombie_death" to (20 to "ZD1"),
        "ghost_attack" to (20 to "GA2"),"ghost_death" to (20 to "GD1"),"ghost_storm" to (20 to "GC7"),
        "slime_death" to (20 to "LD4"),"slime_path" to (20 to "LP5"),
        "gorilla_stomp" to (21 to "GS9"),"mimic_attack" to (21 to "MA3"),"mimic_death" to (21 to "MD2"),
        "vampire_hunger" to (21 to "VH5"),"vampire_bite" to (21 to "VB1"),"vampire_mist" to (21 to "ME1"),
        "vampire_form" to (21 to "VF5"),"vampire_teleport" to (21 to "TP1"),
        "vampire_cone_charge" to (21 to "LC2"),"vampire_swipe_charge" to (21 to "SC5"))
    val fitted=mapOf("vampire_form" to 3200,"vampire_teleport" to 750,"vampire_cone_charge" to 1000)
    @JvmStatic fun main(args:Array<String>) {
        val output=File("assets/audio/sfx")
        selections.forEach{(name,selection)->
            val source=File("tools/audio-audition/listening-pack-${selection.first}/${selection.second}.wav")
            val target=File(output,"$name.wav")
            if(name in fitted)PrepareAudioAudition.wav(target,InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(source),fitted.getValue(name)))
            else source.copyTo(target,overwrite=true)
        }
        // Pack27 supersedes Witch death / Vampire hurt+death. Never restore those
        // retired recordings by rerunning this older effects installer.
        selections.keys.forEach{name->AudioSystem.getAudioInputStream(File(output,"$name.wav")).use{
            check(it.format.channels==1);println("$name ${round(it.frameLength*1000.0/it.format.sampleRate).toLong()}")
        }}
        File("assets/audio/UNDEAD-VAMPIRE-LICENSES.txt").writeText(
            "INSTALLED SELECTIONS: "+selections.entries.joinToString{it.key+"="+it.value.first+"/"+it.value.second}+"\n"+
            "LD4 is shared by full-size and fragment Slimes. VF5 fitted 3200ms, TP1 750ms, LC2 1000ms. Other selected WAVs copied exactly.\n\n"+
            "Witch death / Vampire hurt+death superseded by approved user recordings; see CHARACTER-VOICE-SOURCES.txt.\n\n"+
            File("tools/audio-audition/undead-source-credits.txt").readText().replace("NOT INSTALLED","SELECTED CUES INSTALLED")+"\n\n"+
            File("tools/audio-audition/vampire-source-credits.txt").readText().replace("NOT INSTALLED","SELECTED CUES INSTALLED"),Charsets.UTF_8)
    }
}
