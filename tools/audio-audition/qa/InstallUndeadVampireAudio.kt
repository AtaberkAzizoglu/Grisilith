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
        "vampire_cone_charge" to (21 to "LC2"),"vampire_swipe_charge" to (21 to "SC5"),
        "vampire_death" to (21 to "VS5"),"vampire_hurt" to (21 to "VP5"))
    val fitted=mapOf("vampire_form" to 3200,"vampire_teleport" to 750,"vampire_cone_charge" to 1000)
    @JvmStatic fun main(args:Array<String>) {
        val output=File("assets/audio/sfx")
        selections.forEach{(name,selection)->
            val source=File("tools/audio-audition/listening-pack-${selection.first}/${selection.second}.wav")
            val target=File(output,"$name.wav")
            if(name in fitted)PrepareAudioAudition.wav(target,InstallMagicNormalAudio.fit(InstallMagicNormalAudio.read(source),fitted.getValue(name)))
            else source.copyTo(target,overwrite=true)
        }
        val source=File("tools/audio-audition/sources/vampire-preview/Audio/witch-xl57.mp3")
        check(source.isFile){"Witch source missing; do not overwrite previous Witch without an approved source"}
        val recording=PrepareFemaleIncantationAudition.decode(source)
        val pcm=FloatArray((recording.samples.size.toDouble()*44100/recording.rate).toInt()){i->
            val pos=i.toDouble()*recording.rate/44100;val p=pos.toInt().coerceAtMost(recording.samples.lastIndex)
            (recording.samples[p]*(1-(pos-p))+recording.samples[min(p+1,recording.samples.lastIndex)]*(pos-p)).toFloat()
        }
        val gain=.25f/pcm.maxOf{abs(it)};pcm.indices.forEach{pcm[it]*=gain}
        repeat(min(441,pcm.size/4)){i->pcm[i]*=i/441f;pcm[pcm.lastIndex-i]*=i/441f}
        PrepareAudioAudition.wav(File(output,"witch_death.wav"),pcm)
        (selections.keys+"witch_death").forEach{name->AudioSystem.getAudioInputStream(File(output,"$name.wav")).use{
            check(it.format.channels==1);println("$name ${round(it.frameLength*1000.0/it.format.sampleRate).toLong()}")
        }}
        File("assets/audio/UNDEAD-VAMPIRE-LICENSES.txt").writeText(
            "INSTALLED SELECTIONS: "+selections.entries.joinToString{it.key+"="+it.value.first+"/"+it.value.second}+"\n"+
            "LD4 is shared by full-size and fragment Slimes. VF5 fitted 3200ms, TP1 750ms, LC2 1000ms. Other selected WAVs copied exactly.\n\n"+
            "WITCH: short scream no effects.wav by xl57 — CC BY 4.0.\nhttps://freesound.org/people/xl57/sounds/671201/\nhttps://creativecommons.org/licenses/by/4.0/\n"+
            "Public HQ MP3 preview converted to mono PCM16/44.1kHz, peak matched -12dBFS, 10ms edge fades. No pitch change.\n\n"+
            File("tools/audio-audition/undead-source-credits.txt").readText().replace("NOT INSTALLED","SELECTED CUES INSTALLED")+"\n\n"+
            File("tools/audio-audition/vampire-source-credits.txt").readText().replace("NOT INSTALLED","SELECTED CUES INSTALLED"),Charsets.UTF_8)
    }
}
