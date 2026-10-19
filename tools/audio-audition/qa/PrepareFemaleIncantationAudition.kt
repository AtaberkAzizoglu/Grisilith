package qa

import java.io.File
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.MP3Decoder
import javazoom.jl.decoder.OutputBuffer
import javazoom.jl.decoder.Header
import kotlin.math.*

/** Local audition only: no game code, installed assets or settings are changed. */
object PrepareFemaleIncantationAudition {
    private const val RATE=44100
    internal data class Recording(val samples:FloatArray,val rate:Int)
    internal fun decode(file:File):Recording {
        val bits=Bitstream(file.inputStream());val decoder=MP3Decoder()
        var output:OutputBuffer?=null;var rate=RATE;val samples=ArrayList<Float>()
        try {while(true){
            val h=bits.readFrame()?:break
            val channels=if(h.mode()==Header.SINGLE_CHANNEL)1 else 2;rate=h.frequency()
            if(output==null){output=OutputBuffer(channels,false);decoder.setOutputBuffer(output)}
            val pcm=decoder.decodeFrame(h,bits);val count=pcm.reset()
            var i=0
            while(i<count){var value=0f;repeat(channels){c->val j=i+c*2;value+=((pcm.buffer[j].toInt() and 255) or (pcm.buffer[j+1].toInt() shl 8)).toShort()/32768f};samples.add(value/channels);i+=2*channels}
            bits.closeFrame()
        }}finally{bits.close()}
        return Recording(samples.toFloatArray(),rate)
    }
    private data class Clip(val id:String,val label:String,val pcm:FloatArray,val note:String)
    private fun cuts(rec:Recording,id:String,title:String):List<Clip> {
        val a=rec.samples;val hop=rec.rate/100
        val rms=(0 until a.size/hop).map{n->sqrt((0 until hop).sumOf{a[n*hop+it].toDouble().pow(2)}/hop)}
        val threshold=rms.max()*.035
        val active=rms.indices.filter{rms[it]>threshold};check(active.isNotEmpty())
        val first=max(0,active.first()-8);val last=min(rms.lastIndex,active.last()+12)
        // Split at local low-energy pauses near evenly spaced points, not arbitrary hard cuts.
        val bounds=mutableListOf(first)
        for(n in 1..5){
            val target=first+(last-first)*n/6
            val radius=min(85,(last-first)/20)
            val best=(max(bounds.last()+70,target-radius)..min(last-70,target+radius)).minByOrNull{i->
                (max(0,i-3)..min(rms.lastIndex,i+3)).sumOf{rms[it]}
            }?:target
            bounds.add(best)
        }
        bounds.add(last)
        println("$id source segments: "+bounds.zipWithNext().map{(s,e)->"${s/100.0}..${e/100.0}"})
        return (0..4).map{n->
            var start=bounds[n]*hop;var end=bounds[n+1]*hop
            while(start+hop<end && sqrt((0 until hop).sumOf{a[start+it].toDouble().pow(2)}/hop)<threshold)start+=hop
            start=max(bounds[n]*hop,start-hop*6)
            while(end-hop>start && sqrt((0 until hop).sumOf{a[end-hop+it].toDouble().pow(2)}/hop)<threshold)end-=hop
            end=min(bounds[n+1]*hop,end+hop*8)
            val pcm=FloatArray(((end-start).toDouble()*RATE/rec.rate).toInt()){i->
                val pos=start+i.toDouble()*rec.rate/RATE;val p=pos.toInt().coerceAtMost(a.lastIndex)
                a[p]*(1-(pos-p).toFloat())+a[min(p+1,a.lastIndex)]*(pos-p).toFloat()
            }
            val peak=pcm.maxOf{abs(it)};check(peak>.001f)
            pcm.indices.forEach{pcm[it]*=.25f/peak}
            val fade=min(RATE/50,pcm.size/4)
            repeat(fade){i->val g=i.toFloat()/fade;pcm[i]*=g;pcm[pcm.lastIndex-i]*=g}
            Clip("$id${n+1}","$title · ayrı kesit ${n+1}",pcm,
                "Kaynak aralığı: ${"%.2f".format(java.util.Locale.ROOT,start.toDouble()/rec.rate)}–${"%.2f".format(java.util.Locale.ROOT,end.toDouble()/rec.rate)} s; doğal kaynak hızı/perdesi, ek ses katmanı yok.")
        }
    }
    @JvmStatic fun main(args:Array<String>) {
        val root=File("tools/audio-audition/sources/female-incantation/Audio")
        val groups=linkedMapOf(
            "TW" to ("Tsunami · anlamsız kadın vokali" to cuts(decode(File(root,"female-gibberish.mp3")),"TW","Kadın sesi / anlamsız sözler")),
            "SV" to ("Singularity · cadı / demonik kadın vokali" to cuts(decode(File(root,"evil-witch.mp3")),"SV","İşlenmiş cadı / demonik vokal"))
        )
        val out=File("tools/audio-audition/listening-pack-14").apply{mkdirs()}
        val body=StringBuilder();val manifest=StringBuilder("Audition only; NOT INSTALLED.\n")
        groups.forEach{(id,pair)->val(title,clips)=pair
            val slot=ceil(clips.maxOf{it.pcm.size}.toDouble()/RATE+1).toInt()
            val combined=FloatArray(RATE*(1+slot*clips.size))
            body.append("<section id='$id'><h2>$title</h2><p>Toplu dinleme: 1 → 5, $slot saniyelik aralıklar.</p><audio controls preload='none' src='$id-all.wav'></audio><div class='grid'>")
            clips.forEachIndexed{i,c->
                PrepareAudioAudition.wav(File(out,"${c.id}.wav"),c.pcm);c.pcm.copyInto(combined,RATE*(1+i*slot))
                manifest.append("${c.id}: ${c.label}\n${c.note}\nDuration: ${c.pcm.size.toDouble()/RATE}\n")
                body.append("<article><label><input type='radio' name='$id' value='${c.id}'> <b>${c.id}</b> · ${c.label}</label><audio controls preload='none' src='${c.id}.wav'></audio><small>${c.note}</small></article>")
            }
            PrepareAudioAudition.wav(File(out,"$id-all.wav"),combined);body.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File(out,"LICENSE-SOURCES.txt").writeText("""Female incantation auditions — NOT INSTALLED.
TW1–5: cloyen, Female Gibberish, CC0.
https://freesound.org/people/cloyen/sounds/800349/
https://cdn.freesound.org/previews/800/800349_17223158-hq.mp3
SV1–5: Bananaboatman33, evilwitch_Master.wav, CC0.
https://freesound.org/people/Bananaboatman33/sounds/315655/
https://cdn.freesound.org/previews/315/315655_3245819-hq.mp3
The source author describes recorded/processed evil witch / female demon gibberish; tagged backwards.
Existing source processing is retained. No added multi-voice layers, reversal, pitch change or synthetic syllables.
License: https://creativecommons.org/publicdomain/zero/1.0/
Public HQ MP3 previews used; not original uncompressed studio WAV files. Source metadata verified 2026-10-19.
Five cuts per source, TWO source recordings total — not ten separate actors or recordings.
Edits: pause-based excerpts, mono/resampling to 44.1kHz, peak matching -12dBFS (not perceived loudness matching), 20ms fades.
No claim of authentic Elvish/Celestial or any other real language.
""",Charsets.UTF_8)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Kadın casting sesleri 14</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}</style>
<main><h1>Grisilith · Anlamsız kadın vokalleri</h1><p>10 yeni casting adayı. Oyun kodu ve kurulu sesler değiştirilmedi. HL2 ve HS3 not edildi; HS3 yeni Singularity seçimi gelene kadar kayıtlı tutuluyor.</p>
<p><b>TW1–5:</b> Female Gibberish kaydından beş ayrı kesit. Kaynak, anlaşılır cümleler yerine anlamsız kadın konuşması olarak yayımlanmış.</p>
<p><b>SV1–5:</b> cadı/demonik kadın sesi olarak işlenmiş ikinci kayıttan beş ayrı kesit. Kaynakta ters ses işlemi zaten var; ben ek ses, başka konuşmacı, ek ters çevirme veya yankı bindirmedim.</p>
<p>Bunlar 10 farklı seslendirmeci değil, iki kayıttan beşer ayrı kesittir. Önceki İngilizce büyü sözcükleri kullanılmadı. Kaynakların doğal perdesi/hızı korundu; hiçbirine patlama/müzik eklenmedi. Seçilen kayıt daha sonra casting süresine uyarlanabilir.</p>
<p>Önce sesi düşük tut. <a href='LICENSE-SOURCES.txt'>Kaynaklar ve ücretsiz CC0 lisansları</a> · <a href='SECENEKLER.txt'>Kesit dökümü</a></p>
$body
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='3'></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(out.listFiles()!!.count{it.extension=="wav"}==12)
        println("FEMALE_INCANTATION_AUDITION_PASS: ten candidates + two comparisons; no game assets changed.")
    }
}
