package qa

import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import java.io.File
import kotlin.math.abs
import kotlin.math.ceil

/** Preview only. No runtime, installed game asset, or mixer writes. */
object PrepareFocusAbilityAudition {
    private const val RATE=44100
    private data class Group(val id:String,val title:String,val note:String,val loop:Boolean,val choices:List<C>)
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun f(file:String,speed:Double=1.0,gain:Double=1.0,seconds:Double=2.4)=
        L("focus-ability",file,speed=speed,gain=gain,maxSeconds=seconds)
    private fun teleport(speed:Double=1.0)=f("teleport.wav",speed,seconds=.4).copy(transient=1,gust=true)
    private fun flame(speed:Double=1.0)=L("fire-evil","spells/Fire.aif",speed=speed,maxSeconds=.8,transient=1,gust=true)
    private fun dark(n:Int,speed:Double=.7,seconds:Double=.8)=
        L("dark-magic","dark_magic/fout-0$n.flac",speed=speed,maxSeconds=seconds,transient=1,lowPass=1200.0,gust=seconds<1.0)
    private fun wind(speed:Double=1.0,gain:Double=1.0,cut:Double=350.0)=
        L("wind-loop","wind.ogg",speed=speed,gain=gain,maxSeconds=2.4,startSeconds=.3,highPass=cut)
    private fun air(speed:Double=1.0,gain:Double=.5)=L("air-move","wind.wav",speed=speed,gain=gain,maxSeconds=.7,gust=true)
    private fun bubble(speed:Double=1.0,gain:Double=1.0)=f("acid_bubble.wav",speed,gain)
    private fun hiss(gain:Double=.25)=L("tinysized","sfx-cc0/compressed-air-spray-01.wav",gain=gain,maxSeconds=2.4,transient=1)
    private fun fire(speed:Double=1.0,gain:Double=1.0,start:Double=2.0)=f("fire.wav",speed,gain).copy(startSeconds=start,lowPass=2400.0)
    private fun thunder(take:Int=1,speed:Double=1.0)=f("thunder.wav",speed).copy(transient=take,maxSeconds=2.0)
    private fun arrival(layer:L,offset:Double=.22)=layer.copy(offset=offset)
    private val groups=listOf(
        Group("IG","Ignite Dash · ışınlanma + alevli patlama","İki aşamalı taslaklar: önce ışınlanma, kısa bir aralıkla varışta alev patlaması. Seçimde bileşenler ayrı stem olarak da saklanır; oyuna eklerken gerçek varış anına bağlanacak. Beş ayrı özgün teleport kaydı değil, işlenmiş eşleşmeler.",false,listOf(
            c("Kısa teleport + tok alev",teleport(),arrival(flame(.85))),
            c("Pes teleport + basınçlı patlama",teleport(.8),arrival(flame(.7)),arrival(air(.8,.5))),
            c("Hızlı geçiş + keskin alev",teleport(1.2),arrival(flame(1.1),.16)),
            c("Ters geçiş + geniş alev darbesi",teleport(.95).copy(reverse=true),arrival(flame(.8),.25),arrival(air(.9,.35),.25)),
            c("Boğuk teleport + ağır alev",teleport(.7).copy(lowPass=1600.0),arrival(flame(.65),.28),arrival(L("impact-sounds","impactSoft_heavy_003",gain=.35,maxSeconds=.45),.28))
        )),
        Group("ER","Erasing Soul · tok ruh emme / lanet patlaması","Tiz çığlık yerine pes, boğuk ve karanlık dokular. qubodup dark-magic kaynaklarının farklı kesit/perde/ters-katman varyasyonları; gerçek insan sesi veya konuşma değil.",false,listOf(
            c("Tok lanetli darbe",dark(1,.6).copy(lowPass=950.0)),
            c("Ruh çekişi + boğuk çöküş",dark(2,.65).copy(reverse=true,maxSeconds=.4),arrival(dark(3,.65),.22)),
            c("Derin büyüsel patlama",dark(3,.55).copy(lowPass=850.0),L("impact-sounds","impactSoft_heavy_003",gain=.45,maxSeconds=.5,speed=.8)),
            c("İçe çöken lanet",dark(1,.7).copy(reverse=true),dark(2,.55).copy(offset=.12,gain=.35)),
            c("Kalın, uğultulu ruh emişi",dark(2,.5).copy(lowPass=800.0,maxSeconds=1.0),air(.7,.25).copy(reverse=true,lowPass=900.0))
        )),
        Group("VP","Vortex Pull · hortum / sürekli rüzgâr","Kesintisiz rüzgâr döngüsü adayları. Ham kaynak kesitleri crossfade ile loop'a hazırlandı; seçilince yalnız alan aktifken çalacak ve alan bitince kapanacak.",true,listOf(
            c("Sade sürekli hortum",wind()),
            c("Kalın rüzgâr gövdesi",wind(.8,1.0,180.0).copy(lowPass=2000.0)),
            c("İnce hızlı dönme",wind(1.25,1.0,700.0)),
            c("Derin + ince katmanlı girdap",wind(.7,.8,150.0),wind(1.3,.35,1000.0).copy(startSeconds=.6)),
            c("Doğal rüzgâr dokusu",L("short-wind","wind.wav",speed=.8,maxSeconds=2.4,highPass=200.0))
        )),
        Group("SC","Static Clouds · yıldırım çakması","Tekrarlanan yıldırım darbeleri ve gök gürültüsü. SC1–3 yeni Thunder kaydının kesit/perde varyasyonları; SC4–5 elektrik katmanlı tasarımlar. Gerçek hava kaydı değil, kaynak yazarın sentezlenmiş gök gürültüsü.",true,listOf(
            c("Kısa yıldırım ve gök gürültüsü",thunder(1)),
            c("Daha kalın yıldırım",thunder(1,.8)),
            c("Farklı yıldırım kesiti",f("thunder.wav",1.05).copy(startSeconds=3.0,maxSeconds=2.0)),
            c("Elektrik çakması + gök gürültüsü",L("tinysized","sfx-cc0/paralyzer-discharge-01.wav",maxSeconds=.2,gain=.35,transient=1),thunder(1).copy(offset=.04)),
            c("Boğuk fırtına ve elektrik",thunder(1,.7).copy(lowPass=2600.0),L("magic-spells","electricspell.ogg",maxSeconds=.35,gain=.25,transient=1))
        )),
        Group("AP","Acidic Pool · baloncuk / asidik cızırtı","Baloncuk ve tıslama dengesi farklı beş döngü. Asit kaydı spookymodem CC BY 3.0; diğer katmanlar CC0. Hasar tick'i başına ayrı efekt değil, alanın sürekli sesi.",true,listOf(
            c("Sade asit kabarcıkları",bubble()),
            c("Kabarcık + belirgin cızırtı",bubble(1.0,.8),hiss(.4)),
            c("Kalın, yavaş kaynayan asit",bubble(.75),f("boil_cover.ogg",.9,.4)),
            c("İnce ve hareketli asit",bubble(1.2),hiss(.25).copy(highPass=1000.0)),
            c("Yoğun havuz kaynaması",bubble(.9,.8),f("boil_open.ogg",1.0,.55),hiss(.2))
        )),
        Group("PS","Plague Storm · necrotic fırtına","Vortex'ten farklı, daha boğuk ve lanetli rüzgâr dokuları. Dark-magic katmanları soyut/sentetik; gerçek fısıltı kaydı değil. Döngü yalnız büyünün ömrü boyunca çalacak.",true,listOf(
            c("Boğuk lanet rüzgârı",wind(.65,1.0,100.0).copy(lowPass=1600.0),dark(1,.6,2.4).copy(gain=.25)),
            c("Ters akan karanlık fırtına",wind(.8,.8,220.0),dark(2,.7,2.4).copy(gain=.3,reverse=true)),
            c("Derin uğultulu salgın",wind(.6,.8,100.0),dark(3,.5,2.4).copy(gain=.4,lowPass=900.0)),
            c("Huzursuz, çift katmanlı fırtına",wind(.9,.8,300.0),dark(1,.8,2.4).copy(gain=.25,reverse=true),dark(3,.6,2.4).copy(gain=.2)),
            c("Sert ve kuru necrotic rüzgâr",wind(1.05,1.0,500.0),dark(2,.55,2.4).copy(gain=.35,lowPass=1100.0))
        )),
        Group("BL","Yanan Acidic Pool · yalnız yanma katmanı","Sadece eklenecek yanma/sıcak sıvı katmanı. Acid Bubble veya seçilecek AP sesi BURADA YOK; oyunda AP ile üst üste bindirilecek. Gerçek yanıcı sıvı kaydı değil: filtrelenmiş ateş ve kaynama kayıtlarından tasarımlar.",true,listOf(
            c("Yumuşak sıvı alevi",fire(1.0,.8).copy(lowPass=1400.0),f("boil_open.ogg",1.1,.3)),
            c("Yoğun sıcak sıvı cızırtısı",fire(1.2,.6,4.0),f("boil_open.ogg",1.2,.65).copy(highPass=600.0)),
            c("Kalın ve ağır yanma",fire(.75,1.0).copy(lowPass=1000.0),f("boil_cover.ogg",.8,.3)),
            c("İnce alev uğultusu",fire(1.05,.8,6.0).copy(highPass=350.0),hiss(.2).copy(lowPass=2200.0)),
            c("Daha hareketli sıvı yanması",fire(.9,.8,8.0),f("boil_open.ogg",.9,.45),hiss(.15))
        ))
    )

    private fun loop(pcm:FloatArray):FloatArray {
        val overlap=minOf(RATE/12,pcm.size/4)
        val result=pcm.copyOf(pcm.size-overlap)
        repeat(overlap){i->val t=i.toFloat()/overlap;result[i]=pcm[result.size+i]*(1-t)+pcm[i]*t}
        // A short boundary repair prevents a single-sample discontinuity after quantization.
        repeat(64){i->val k=result.size-64+i;val t=i/63f;result[k]=result[k]*(1-t)+result[0]*t}
        check(abs(result.first()-result.last())<.00001f)
        return result
    }
    private fun esc(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace("'","&#39;")
    @JvmStatic fun main(args:Array<String>) {
        val out=File("tools/audio-audition/listening-pack-11").apply{mkdirs()}
        val html=StringBuilder();val notes=StringBuilder("35 candidates / 7 groups. Preview only; not installed.\n")
        groups.forEach{group->
            val clips=group.choices.map{candidate->
                val raw=PrepareAudioAudition.render(candidate)
                if(group.loop)loop(raw) else raw
            }
            val demos=clips.map{if(group.loop)it+it else it}
            val slot=ceil(demos.maxOf{it.size}.toDouble()/RATE+1.0).toInt().coerceAtLeast(3)
            val comparison=FloatArray(RATE*(1+group.choices.size*slot))
            html.append("<section><h2>${group.id} · ${esc(group.title)}</h2><p>${esc(group.note)}</p><p>Toplu dinleme: 1 → 5, her aday bir kez. Sürekli alan örneklerinde iki döngü duyulur.</p><audio controls preload='none' src='${group.id}-all.wav'></audio><div class='grid'>")
            group.choices.forEachIndexed{i,candidate->
                val id="${group.id}${i+1}"
                PrepareAudioAudition.wav(File(out,"$id.wav"),demos[i])
                demos[i].copyInto(comparison,RATE*(1+i*slot))
                val sources=candidate.layers.joinToString(" + "){"${it.pack}/${it.file}"}
                notes.append("$id — ${candidate.label}\n$sources\n")
                html.append("<article><label><input type='radio' name='${group.id}' value='$id'><b>$id</b> ${esc(candidate.label)}</label><audio controls preload='none' src='$id.wav'></audio>")
                if(group.loop) {
                    PrepareAudioAudition.wav(File(out,"$id-loop.wav"),clips[i])
                    html.append("<details><summary>Kesintisiz döngüyü dinle · durdurana kadar tekrar eder</summary><audio controls loop preload='none' src='$id-loop.wav'></audio></details>")
                }
                if(group.id=="IG") {
                    // Keep components separate so implementation need not hardcode the preview's arrival delay.
                    val split=candidate.layers.partition{it.offset==0.0}
                    PrepareAudioAudition.wav(File(out,"$id-teleport.wav"),PrepareAudioAudition.render(C("Teleport",split.first)))
                    PrepareAudioAudition.wav(File(out,"$id-burst.wav"),PrepareAudioAudition.render(C("Arrival",split.second.map{it.copy(offset=it.offset-split.second.minOf{l->l.offset})})))
                }
                html.append("<small>${esc(sources)}</small></article>")
            }
            PrepareAudioAudition.wav(File(out,"${group.id}-all.wav"),comparison)
            html.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(notes.toString(),Charsets.UTF_8)
        File("tools/audio-audition/focus-ability-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Ses seçkisi 11</title>
<style>body{background:#17191c;color:#eee;font:16px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:28px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;margin-top:12px;background:#25282d;color:#eee}</style>
<main><h1>Grisilith · Spell Focus ability seçkisi 11</h1><p>35 aday, yedi grup. Henüz oyuna eklenmedi. Ivy Staff ve Enchanted Chalk Dust bu turda yok.</p>
<p>Önce sesi düşük tut. Kayıtlar mono 44.1kHz/16-bit; tepe eşleme yaklaşık −12dBFS, algısal ses yüksekliği eşitlemesi değil. Bazıları ayrı kayıt, bazıları açıkça etiketli hız/perde/filtre ve katman varyasyonlarıdır.</p>
<p>VP, SC, AP, PS ve BL seçilince gerçek alan ömrüne bağlanacak: erken bitme, silinme ve pause dahil. Buradaki kısa örnekler büyünün toplam süresi değildir. BL yalnız yanma katmanıdır; asit sesini içermez.</p>
<p><a href="LICENSE-SOURCES.txt">Ücretsiz kaynaklar, lisanslar ve değişiklikler</a>. CC BY 3.0 kaynaklar seçilirse oyun içinde atıf eklenecek.</p>
$html
<section><h2>Seçimlerin</h2><button id="summary">Seçim özetini oluştur</button><textarea id="result" readonly rows="2"></textarea></section>
<p>Sonraki not: ability sesleri tamamlandıktan sonra bazı gelişmiş saldırılara özel sesler ayrıca değerlendirilecek.</p></main>
<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(out.listFiles()!!.count{it.extension=="wav"}==35+25+10+7)
        println("FOCUS_ABILITY_AUDITION_PASS: 35 candidates, 25 seamless loop stems, 10 teleport/burst stems, 7 comparisons. No game changes.")
    }
}
