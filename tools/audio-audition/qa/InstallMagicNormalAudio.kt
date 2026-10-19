package qa

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioSystem
import kotlin.math.abs
import kotlin.math.roundToInt

/** Offline preparation only: selected sources are retained in audition archives. */
object InstallMagicNormalAudio {
    private const val RATE=44100
    fun read(file:File):FloatArray=AudioSystem.getAudioInputStream(file).use {
        val data=ByteBuffer.wrap(it.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        FloatArray(data.remaining()){data.get()/32768f}
    }
    fun fit(source:FloatArray,ms:Int):FloatArray {
        val count=ms*RATE/1000
        // Preserve the recognizable transient instead of accelerating a long quiet tail many times.
        val body=if(source.size>count*1.7) {
            val peak=source.indices.maxBy{abs(source[it])}
            val start=(peak-count/3).coerceIn(0,(source.size-count).coerceAtLeast(0))
            source.copyOfRange(start,minOf(source.size,start+count))
        } else source
        val result=FloatArray(count){i->
            val pos=i.toDouble()*(body.size-1)/(count-1);val first=pos.toInt();val fraction=(pos-first).toFloat()
            body[first]*(1-fraction)+body[minOf(first+1,body.lastIndex)]*fraction
        }
        val fade=minOf(661,count/5)
        repeat(fade){i->result[i]*=i.toFloat()/fade;result[result.lastIndex-i]*=i.toFloat()/fade}
        val peak=result.maxOf{abs(it)}
        require(peak>0f)
        result.indices.forEach{result[it]*=.25f/peak}
        return result
    }
    private fun save(file:File,pcm:FloatArray) {
        val bytes=ByteBuffer.allocate(44+pcm.size*2).order(ByteOrder.LITTLE_ENDIAN)
        bytes.put("RIFF".toByteArray());bytes.putInt(36+pcm.size*2);bytes.put("WAVEfmt ".toByteArray())
        bytes.putInt(16);bytes.putShort(1);bytes.putShort(1);bytes.putInt(RATE);bytes.putInt(RATE*2)
        bytes.putShort(2);bytes.putShort(16);bytes.put("data".toByteArray());bytes.putInt(pcm.size*2)
        pcm.forEach{bytes.putShort((it*32767).roundToInt().toShort())};file.writeBytes(bytes.array())
    }
    @JvmStatic fun main(args:Array<String>) {
        val root=File("tools/audio-audition");val out=File("assets/audio/sfx")
        fun source(id:String)=File(root,"listening-pack-$id.wav")
        mapOf("magic_air" to "09/AI3","magic_water" to "10/WV4","magic_fire" to "10/FL5",
            "magic_acid" to "09/AC2","magic_lightning" to "09/LI3","magic_necrotic" to "10/CU4",
            "magic_poison" to "09/PO3").forEach{(name,id)->
            source(id).copyTo(File(out,"$name.wav"),overwrite=true)
        }
        data class Fit(val name:String,val source:String,val ms:Int)
        listOf(Fit("longsword_swing","02/G1",250),Fit("double_swords_swing","03/DS1",240),
            Fit("double_swords_upgraded","03/DS1-upgraded",240),Fit("lance_thrust","03/LN1",240),
            Fit("shield_strike","03/SH1",270),Fit("whip_lash","03/WP5",240),
            Fit("warhammer_impact","05/TH3",240),Fit("air_slash","07/AW5",673),
            Fit("air_slash_upgraded","07/AW5",865)).forEach {
            save(File(out,"${it.name}.wav"),fit(read(source(it.source)),it.ms))
        }
        // Each half preserves the selected WS2's crack; two consecutive halves cover the entire sweep.
        val half=fit(read(source("06/WS2")),175)
        save(File(out,"whip_sweep.wav"),half+half)
        // This wave source is the single approved EB1 impact, not the packed triple preview.
        val wave=File(out,"earthbreaker_wave.wav")
        save(wave,fit(read(wave),350))
        out.listFiles()!!.filter{it.extension=="wav"}.forEach{file->AudioSystem.getAudioInputStream(file).use{
            check(it.frameLength>0);println("${file.name}: ${it.frameLength*1000L/RATE}ms")
        }}
        println("MAGIC_NORMAL_INSTALL_PASS: seven exact selections and animation-fitted melee cues; sources retained")
    }
}
