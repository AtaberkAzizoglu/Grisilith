package qa

import java.io.File
import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.MP3Decoder
import javazoom.jl.decoder.OutputBuffer
import javazoom.jl.decoder.Header
import kotlin.math.*

/** Audition-only human recordings. Never writes game assets. */
object PrepareHumanCastingAudition {
    private const val RATE=44100
    @JvmStatic fun main(args:Array<String>) {
        val root=File("tools/audio-audition/sources/human-casting/Audio")
        val bits=Bitstream(File(root,"incantations.mp3").inputStream())
        val decoder=MP3Decoder();var output:OutputBuffer?=null
        val samples=ArrayList<Float>();var rate=44100
        try { while(true) {
            val header=bits.readFrame()?:break
            val channels=if(header.mode()==Header.SINGLE_CHANNEL)1 else 2
            rate=header.frequency()
            if(output==null){output=OutputBuffer(channels,false);decoder.setOutputBuffer(output)}
            val pcm=decoder.decodeFrame(header,bits);val count=pcm.reset()
            var i=0
            while(i<count){var value=0f;repeat(channels){c->val j=i+c*2;value+=((pcm.buffer[j].toInt() and 255) or (pcm.buffer[j+1].toInt() shl 8)).toShort()/32768f};samples.add(value/channels);i+=2*channels}
            bits.closeFrame()
        }} finally {bits.close()}
        val raw=samples.toFloatArray()
        val frame=(rate*.02).toInt()
        val rms=(0 until raw.size/frame).map{n->sqrt((0 until frame).sumOf{raw[n*frame+it].toDouble().pow(2)}/frame)}
        val threshold=rms.max()*.045
        val ranges=mutableListOf<Pair<Int,Int>>()
        var start=-1;var last=-1
        rms.forEachIndexed{i,v->
            if(v>threshold){if(start<0)start=i;last=i}
            if(start>=0 && i-last>25){if(last-start>35)ranges.add(max(0,start-4)*frame to min(raw.size,(last+7)*frame));start=-1}
        }
        if(start>=0)ranges.add(max(0,start-4)*frame to min(raw.size,(last+7)*frame))
        println("VOICE PHRASES: "+ranges.mapIndexed{i,r->"${i+1}: ${r.first.toDouble()/rate}..${r.second.toDouble()/rate}"})
        if(args.contains("--inspect"))return
        check(ranges.size>=10){"Need ten complete separated human phrases, found ${ranges.size}"}
        val out=File("tools/audio-audition/listening-pack-13").apply{mkdirs()}
        data class Clip(val id:String,val label:String,val source:String,val pcm:FloatArray)
        fun normalize(input:FloatArray):FloatArray {
            val peak=input.maxOf{abs(it)};check(peak>.001f)
            return input.map{it*.25f/peak}.toFloatArray().also{a->val n=min(220,a.size/4);repeat(n){i->a[i]*=i.toFloat()/n;a[a.lastIndex-i]*=i.toFloat()/n}}
        }
        fun male(id:String,n:Int):Clip {
            val r=ranges[n];val section=raw.copyOfRange(r.first,r.second)
            val pcm=FloatArray((section.size.toDouble()*RATE/rate).toInt()){i->section[min(section.lastIndex,(i.toDouble()*rate/RATE).toInt())]}
            return Clip(id,"Mistik sözler · ayrı okuma ${n+1}","frosthardr / 553453; ${"%.2f".format(java.util.Locale.ROOT,r.first.toDouble()/rate)}–${"%.2f".format(java.util.Locale.ROOT,r.second.toDouble()/rate)} s; CC0; HQ MP3 önizleme kaynağı",normalize(pcm))
        }
        fun female(id:String,type:Int,word:String):Clip {
            val file="female/RPG Voice Starter Pack/Type $type/$word.wav"
            val candidate=PrepareAudioAudition.Candidate(word,listOf(PrepareAudioAudition.Layer("human-casting",file,maxSeconds=15.0)))
            return Clip(id,"${word.replaceFirstChar{it.uppercase()}} · kadın ses tipi $type", "Cici Fyre / Type $type / $word.wav; CC0; İngilizce tek büyü sözcüğü",PrepareAudioAudition.render(candidate))
        }
        val groups=linkedMapOf(
            "HL" to ("Call Lightning · insan sesi casting" to (0..4).map{male("HL${it+1}",it)}),
            "HT" to ("Tsunami · insan sesi casting" to listOf(female("HT1",1,"water"),female("HT2",1,"aqua"),female("HT3",2,"water"),female("HT4",2,"cyclone"),female("HT5",1,"tornado"))),
            "HM" to ("Meteor · insan sesi casting" to (5..9).map{male("HM${it-4}",it)}),
            "HS" to ("Singularity · insan sesi casting" to listOf(female("HS1",3,"curse"),female("HS2",3,"hex"),female("HS3",3,"corruption"),female("HS4",2,"curse"),female("HS5",3,"hellstorm")))
        )
        val body=StringBuilder();val manifest=StringBuilder()
        groups.forEach{(id,pair)->
            val(title,clips)=pair
            val slot=ceil(clips.maxOf{it.pcm.size}.toDouble()/RATE+1).toInt()
            val combined=FloatArray(RATE*(1+slot*clips.size))
            body.append("<section id='$id'><h2>$title</h2><p>Toplu dinleme: 1 → 5, ${slot} saniyelik aralıklar.</p><audio controls preload='none' src='$id-all.wav'></audio><div class='grid'>")
            clips.forEachIndexed{i,c->
                PrepareAudioAudition.wav(File(out,"${c.id}.wav"),c.pcm);c.pcm.copyInto(combined,RATE*(1+i*slot))
                manifest.append("${c.id}: ${c.label}\n${c.source}\nDuration: ${c.pcm.size.toDouble()/RATE}\n\n")
                body.append("<article><label><input type='radio' name='$id' value='${c.id}'> <b>${c.id}</b> · ${c.label}</label><audio controls preload='none' src='${c.id}.wav'></audio><small>${c.source}</small></article>")
            }
            PrepareAudioAudition.wav(File(out,"$id-all.wav"),combined);body.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File(out,"LICENSE-SOURCES.txt").writeText("""Human casting auditions — NOT INSTALLED.
frosthardr: https://freesound.org/people/frosthardr/sounds/553453/
License CC0: https://creativecommons.org/publicdomain/zero/1.0/
Source: public HQ MP3 preview https://cdn.freesound.org/previews/553/553453_2196245-hq.mp3
Original WAV requires login; these are compressed-preview-derived audition cuts, not original studio WAV quality.
Ten separated readings from ONE recording/performer, NOT ten different actors. Natural pitch/speed; no reversal, layers or added echo.
Cici Fyre: https://cicifyre.itch.io/rpg-voice-starter-pack
License CC0, verified on asset page. Included readme identifies Cici Fyre. Original downloaded free ZIP, individual WAV recordings.
One actress, three performed voice styles; NOT three different actors. Understandable English spell words.
Edits: phrase selection, mono conversion, 44.1kHz conversion, peak normalization, edge fades. No voice mixing.
""",Charsets.UTF_8)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · İnsan sesi casting seçkisi 13</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}</style>
<main><h1>Grisilith · Yeni casting sesleri</h1><p>20 aday. Her adayda tek insan sesi kaydı; katman, ters ses, sentetik hece dizisi veya ek patlama yok. Hiçbiri oyuna eklenmedi.</p>
<p>HL / HM: bir erkek performans kaydından on ayrı mistik söz okuması. HT / HS: bir kadın seslendirmecinin farklı ses stilleri ve farklı İngilizce büyü sözcükleri. Bunlar 20 farklı seslendirmeci değildir. Erkek adaylarda perdeyi yapay olarak kalınlaştırmadım/inceltmedim; son isteğin doğrultusunda farklı doğal okumaları öne aldım.</p>
<p>Doğal kayıtlar kısa tutuldu; seçimden sonra casting süresine uyarlanacak. Bir sözcüğü yapay olarak tekrar edip uzatmadım. İstersen bir gruptaki sesi başka büyü için de seçebilirsin.</p>
<p>Onayların korunuyor: PX2, AN4, DE1, OA1, CL2, TS3, MT4, SG3. Önceki seçki 11 de uygulama için bekletiliyor.</p>
<p><a href='LICENSE-SOURCES.txt'>Ücretsiz kaynaklar ve lisanslar</a> · <a href='SECENEKLER.txt'>Aday dökümü</a>. Erkek kayıtlar public HQ MP3 önizlemesinden türetildi; kadın kayıtlar orijinal WAV. Önce sesi düşük tut.</p>
$body
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='3'></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(out.listFiles()!!.count{it.extension=="wav"}==24)
        println("HUMAN_CASTING_AUDITION_PASS: 20 single-voice candidates + 4 comparisons. No game assets changed.")
    }
}
