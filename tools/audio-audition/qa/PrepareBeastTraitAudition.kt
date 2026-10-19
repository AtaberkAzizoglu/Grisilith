package qa

import java.io.File
import java.security.MessageDigest
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C

/** Preview-only designs for the next selection. */
object PrepareBeastTraitAudition {
    private const val RATE=44100
    private fun c(name:String,vararg layers:L)=C(name,layers.toList())
    private fun beast(file:String,speed:Double=1.0,seconds:Double=1.0)=L("beast-voices",file,speed=speed,maxSeconds=seconds)
    private fun foley(name:String,speed:Double=1.0,seconds:Double=1.3)=L("beast-traits","$name.ogg",speed=speed,maxSeconds=seconds)
    private fun rock(n:Int,speed:Double=1.0)=foley("bfh1_rock_falling_%02d".format(n),speed)
    private fun crack(n:Int,speed:Double=1.0)=foley("bfh1_rock_breaking_%02d".format(n),speed)
    private fun hit(n:Int=3,speed:Double=.8)=L("impact-sounds","impactSoft_heavy_00$n.ogg",speed=speed,maxSeconds=.6)
    private fun air(n:Int=4,speed:Double=1.0,seconds:Double=.65)=L("swishes","swishes/swish-$n.wav",speed=speed,maxSeconds=seconds)
    private fun step(n:Int,offset:Double,speed:Double=1.0)=L("impact-sounds","footstep_grass_00$n.ogg",offset=offset,speed=speed,maxSeconds=.25,gain=.55)
    private fun boar(speed:Double=.8,seconds:Double=.9)=beast("boar.ogg",speed,seconds)
    private fun croc(n:Int=1,speed:Double=.8,seconds:Double=1.5)=beast("croc-call-$n.wav",speed,seconds)
    private fun bee(speed:Double=1.0,seconds:Double=.6,start:Double=0.0)=beast("bee-call.wav",speed,seconds).copy(startSeconds=start)
    private fun bear(n:Int,speed:Double=.8,seconds:Double=1.8)=beast("bear-call-$n.wav",speed,seconds)
    private fun gorilla(file:String="roar",speed:Double=.85,seconds:Double=2.0)=beast("gorilla/ogg/gorilla_$file.ogg",speed,seconds)
    private fun egg(speed:Double=1.0,seconds:Double=.8)=L("beast-traits","egg.wav",speed=speed,maxSeconds=seconds,transient=1)
    private data class Group(val id:String,val title:String,val note:String,val choices:List<C>)
    private fun groups()=listOf(
        Group("BC","Boar Charge · hareket","1–3 TAM 1,15 saniye: hücum boyunca. 4–5 kısa başlangıç vurguları. Normal saldırı sesi değildir.",listOf(
            c("1,15s · homurtu ve dört koşu adımı",boar(),step(0,.1),step(1,.35),step(2,.6),step(3,.85)),
            c("1,15s · ağır koşu ve hava yarma",step(4,.0,.8),step(2,.28,.8),step(0,.56,.8),step(3,.84,.8),air(8,.7).copy(gain=.65,offset=.2)),
            c("1,15s · sert nefes ve hızlanan koşu",boar(.65,.5),step(1,.3),step(2,.55),step(3,.75),step(4,.9),air(12,.9).copy(offset=.45,gain=.45)),
            c("Kısa · sert burun nefesi",boar(1.15,.3).copy(highPass=200.0)),
            c("Kısa · gövdeli hücum çıkışı",air(4,.8,.3),hit(2,.8).copy(maxSeconds=.25,gain=.35))
        )),
        Group("BP","Boar · hücum öncesi","Charge öncesi için ek grup: normal saldırıdan ayrı, darbesiz hırıltı/nefes. İlk üç aday 1 saniyelik hazırlığa uyarlanır.",listOf(
            c("Pes, sabit tehdit homurtusu",boar(.65,1.0).copy(lowPass=1600.0)),
            c("Boğuk burun hırıltısı",boar(.8,.9).copy(highPass=120.0)),
            c("İki kısa nefesle hazırlanma",boar(.75,.4),boar(.95,.4).copy(offset=.5,gain=.65)),
            c("Kısa, kuru uyarı",boar(.9,.45).copy(lowPass=2000.0)),
            c("Kalın, hırıltılı başlangıç",boar(.55,.65).copy(highPass=150.0))
        )),
        Group("CJ","Crocodile Jump · sıçrama","1–3 TAM 0,55 saniye: sıçrama boyunca hava/gövde hareketi. 4–5 kısa fırlama vurguları.",listOf(
            c("0,55s · ağır gövdenin havayı yarması",air(4,.65,.55)),
            c("0,55s · nefes ve hızlı sıçrama",croc(3,1.2,.2).copy(gain=.35,highPass=600.0),air(8,.9,.5)),
            c("0,55s · kalkış, hava ve hafif iniş",air(12,.8,.45),hit(1,.8).copy(offset=.35,maxSeconds=.2,gain=.3)),
            c("Kısa · keskin sıçrama çıkışı",air(8,1.3,.22)),
            c("Kısa · tok sürüngen fırlaması",croc(4,1.0,.2),air(4,1.2,.2).copy(gain=.5))
        )),
        Group("CP","Crocodile · sıçrama öncesi","Seçilen CA3 normal saldırı tıslamasından farklı, derin tehdit hırıltıları. İlk üçü 2 saniyelik hazırlık için; son ikisi kısa.",listOf(
            c("2s · derin, bekleyen sürüngen",croc(1,.65,2.0).copy(lowPass=1400.0)),
            c("2s · giderek koyulaşan hırıltı",croc(2,.7,1.1),croc(4,.65,1.0).copy(offset=.9,gain=.7)),
            c("2s · boğazdan iki tehdit nefesi",croc(4,.75,.8),croc(1,.6,1.0).copy(offset=1.0,gain=.8)),
            c("Kısa · koyu sürüngen homurtusu",croc(2,.65,.65).copy(lowPass=1200.0)),
            c("Kısa · pürüzlü tehdit hırıltısı",croc(1,.9,.55).copy(lowPass=2400.0))
        )),
        Group("EG","Baby Crocodiles · yumurta kırılması","Gerçek yumurta kabuğu kaydından beş tasarım; bazıları hafif kırıntı dokulu. Cam patlaması veya büyü sesi değil.",listOf(
            c("Doğal, tek kabuk çatlaması",egg()),
            c("Kalın kabuklu büyük yumurta",egg(.7,1.1).copy(lowPass=3500.0)),
            c("Hızlı, kuru kabuk çıtırtısı",egg(1.25,.5).copy(highPass=500.0)),
            c("Kabuk çatlaması ve küçük parçalar",egg(.9,.8),crack(1,1.8).copy(offset=.12,maxSeconds=.35,gain=.12,highPass=1600.0)),
            c("İki adımda yumurtadan çıkış",egg(1.0,.25),egg(.8,.55).copy(offset=.25,gain=.7))
        )),
        Group("SB","Swarm Being · savunma tepkisi","Pasif için önerilen kısa direnç/dağılıp toparlanma sesleri. Sürekli çalmaz; her hasar tick'inde spam yapılması önerilmez.",listOf(
            c("Kısa, kuru vızıltı sekmesi",bee(1.2,.25).copy(gust=true)),
            c("Yoğun, tok sürü kalkanı",bee(.8,.35),bee(1.15,.25,.2).copy(gain=.45,offset=.03)),
            c("İnce, hızlı dağılma",bee(1.5,.25,.4).copy(highPass=600.0,gust=true)),
            c("Vızıltı ve hafif hava sekmesi",bee(.95,.35),air(8,1.8,.18).copy(gain=.15)),
            c("Boğuk, kısa sürü toparlanması",bee(.7,.4,.1).copy(lowPass=1700.0,gust=true))
        )),
        Group("SS","Split the Swarm · bölünme","Kısa genişleme/ikiye ayrılma vızıltıları. Ölüm sesi gibi tamamen sönmek yerine ikinci sürü hissi bırakır.",listOf(
            c("İkiye açılan doğal sürü",bee(.9,.7),bee(1.3,.55,.2).copy(offset=.12,gain=.7)),
            c("Keskin bölünme ve ince sürü",bee(1.4,.35),bee(1.05,.65,.5).copy(offset=.2,gain=.8)),
            c("Derin sürüden iki ayrı vızıltı",bee(.65,.8),bee(1.25,.6,.3).copy(offset=.18,gain=.55),air(4,1.5,.25).copy(gain=.12)),
            c("Hızlı, kısa sürü çatallanması",bee(1.1,.4),bee(1.5,.35,.4).copy(offset=.1,gain=.65)),
            c("Yoğun, uzun bölünme dalgası",bee(.8,1.0),bee(1.15,.8,.2).copy(offset=.15,gain=.7),bee(1.35,.6,.5).copy(offset=.3,gain=.4))
        )),
        Group("RG","Bear · Grizzly Angry","Öfke başladığında bir defa çalacak sinirli ayı hırlamaları. Normal saldırıdaki kısa RA2'den daha uzun/gövdeli.",listOf(
            c("Uzun ve derin öfke hırlaması",bear(1,.7,2.0)),
            c("Sert, pürüzlü ayı tehdidi",bear(2,.75,1.8).copy(highPass=100.0)),
            c("İki nefesli öfkeli ayı",bear(1,.85,.8),bear(2,.7,1.0).copy(offset=.65,gain=.8)),
            c("Boğuk ve ağır öfke",bear(2,.6,2.0).copy(lowPass=1600.0)),
            c("Keskin başlangıçlı, doğal hırlama",bear(1,1.0,1.5).copy(highPass=180.0))
        )),
        Group("GC","Gorilla Throw · hazırlık kükremesi","Güçlü goril kükremesi / saldırgan tehdit. Runtime hazırlığı 0,5s; seçimden sonra kesit buna uyarlanır, kükreme süresi uzatılmaz.",listOf(
            c("Doğal, güçlü goril kükremesi",gorilla("roar",1.0,1.3)),
            c("Daha kalın göğüslü kükreme",gorilla("roar",.75,1.6)),
            c("Sert, boğazdan tehdit",gorilla("snarl_04",.85,1.0)),
            c("Homurtudan kükremeye",gorilla("grunt",.8,.25),gorilla("roar",.9,1.1).copy(offset=.15,gain=.9)),
            c("Kısa, saldırgan goril haykırışı",gorilla("snarl_03",.75,.8).copy(highPass=100.0))
        )),
        Group("GT","Gorilla Throw · uçan kaya","Atış anı için taş ağırlığı + hava yarma. Yere düşüş/patlama değil; bazı adaylarda hafif sürtünme/kırıntı dokusu var.",listOf(
            c("Ağır kayanın hava yarışı",air(4,.55,.75),rock(1,.8).copy(maxSeconds=.2,gain=.2)),
            c("Kaba, sürtünmeli taş fırlatma",L("rubberduck-rpg","stones_01.ogg",maxSeconds=.25,gain=.45),air(8,.7,.65).copy(offset=.04)),
            c("Hızlı ve tok kaya geçişi",air(12,.75,.55).copy(lowPass=2500.0)),
            c("Geniş, pes hava kütlesi",air(8,.5,.85),air(4,1.2,.4).copy(offset=.06,gain=.3)),
            c("Taş kaldırışından kısa fırlatma",L("rubberduck-rpg","item_stone_03.ogg",maxSeconds=.25,gain=.5),air(4,.8,.55).copy(offset=.12))
        )),
        Group("GS","Gorilla · Stomp the Earth","10 farklı gövde: toprak yumruğu, taş çatlaması, kaba kaya ve daha sinematik bass seçenekleri. Bunlar tek etki darbesidir, hazırlık sesi değildir.",listOf(
            c("Kuru, ağır toprak darbesi",hit(3,.6),rock(1,.8).copy(offset=.04,gain=.4)),
            c("Taş çatlatan yere vuruş",crack(1,.75),hit(4,.65).copy(gain=.7)),
            c("Parçalanan kaya ve ağır gövde",crack(2,.8),rock(3,.7).copy(offset=.1,gain=.55)),
            c("Çok pes, tek yumruk",hit(0,.45).copy(lowPass=1000.0),foley("bfh1_rock_hit_01",.65).copy(gain=.5)),
            c("Kısa, keskin deprem çatlağı",crack(3,1.0),hit(1,.7).copy(gain=.45)),
            c("Geniş alana yayılan moloz",rock(5,.7),rock(8,.9).copy(offset=.15,gain=.65),hit(2,.55).copy(gain=.7)),
            c("Toprak yumruğu, az taş",L("tinysized","sfx-cc0/soil-steps-01.wav",speed=.6,maxSeconds=.5,gain=.45),hit(4,.55)),
            c("İki yumruk hissi, kısa çift darbe",hit(3,.7),foley("bfh1_rock_hit_01",.8).copy(offset=.1,gain=.8)),
            c("Sinematik bass ve kırılan zemin",hit(2,.35).copy(lowPass=750.0),crack(2,.65).copy(offset=.04,gain=.65),rock(9,.7).copy(offset=.12,gain=.35)),
            c("Doğal kaba kaya, uzun kırıntı kuyruğu",foley("bfh1_rock_hit_01",.85),rock(6,.8).copy(offset=.06,gain=.8),crack(3,.9).copy(offset=.1,gain=.4))
        ))
    )
    private fun fit(pcm:FloatArray,seconds:Double):FloatArray {
        val count=(RATE*seconds).roundToInt()
        return FloatArray(count){i->val pos=i.toDouble()*(pcm.size-1)/(count-1);val n=pos.toInt();val t=(pos-n).toFloat()
            pcm[n]*(1-t)+pcm[min(n+1,pcm.lastIndex)]*t}
    }
    @JvmStatic fun main(args:Array<String>) {
        val root=File("tools/audio-audition")
        val decoded=PrepareFemaleIncantationAudition.decode(File(root,"sources/beast-traits/Audio/egg.mp3"))
        PrepareAudioAudition.wav(File(root,"sources/beast-traits/Audio/egg.wav"),FloatArray((decoded.samples.size.toDouble()*RATE/decoded.rate).toInt()){i->decoded.samples[min(decoded.samples.lastIndex,(i.toDouble()*decoded.rate/RATE).toInt())]})
        val out=File(root,"listening-pack-18").apply{mkdirs()}
        val body=StringBuilder();val manifest=StringBuilder("Beast trait auditions: PREVIEW ONLY, NOT INSTALLED\n")
        val hashes=mutableSetOf<String>();var count=0
        val groups=groups()
        groups.forEach{g->
            val clips=g.choices.mapIndexed{i,candidate->
                val raw=PrepareAudioAudition.render(candidate)
                val exact=when{g.id=="BC"&&i<3->1.15;g.id=="CJ"&&i<3->.55;g.id=="CP"&&i<3->2.0;g.id=="BP"&&i<3->1.0;else->null}
                val pcm=if(exact!=null)fit(raw,exact) else raw
                val fade=min(882,pcm.size/4);repeat(fade){n->pcm[n]*=n.toFloat()/fade;pcm[pcm.lastIndex-n]*=n.toFloat()/fade}
                check(pcm.size>RATE/20 && pcm.all{it.isFinite()} && pcm.maxOf{abs(it)}>.02f)
                pcm
            }
            val slot=ceil(clips.maxOf{it.size}.toDouble()/RATE+.8).toInt()
            val combined=FloatArray(RATE*(1+slot*clips.size))
            body.append("<section id='${g.id}'><h2>${g.id} · ${g.title}</h2><p>${g.note}</p><p>Toplu dinleme: 1 → ${clips.size}, $slot saniyelik aralıklarla.</p><audio controls preload='none' src='${g.id}-all.wav'></audio><div class='grid'>")
            g.choices.forEachIndexed{i,candidate->
                val id="${g.id}${i+1}";val file=File(out,"$id.wav")
                PrepareAudioAudition.wav(file,clips[i]);clips[i].copyInto(combined,RATE*(1+i*slot))
                val hash=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
                check(hashes.add(hash)){"Duplicate $id"};count++
                val recipe=candidate.layers.joinToString(" + "){"${it.pack}/${it.file} speed=${it.speed}, offset=${it.offset}s, max=${it.maxSeconds}s, gain=${it.gain}"}
                val duration="%.2f".format(java.util.Locale.US,clips[i].size.toDouble()/RATE)
                manifest.append("$id · ${candidate.label} · ${duration}s\n$recipe\n")
                body.append("<article><label><input type='radio' name='${g.id}' value='$id'> <b>$id</b> · ${candidate.label}</label><p>${duration}s</p><audio controls preload='none' src='$id.wav'></audio><details><summary>Kaynak / düzenleme</summary><small>$recipe</small></details></article>")
            }
            PrepareAudioAudition.wav(File(out,"${g.id}-all.wav"),combined);body.append("</div></section>")
        }
        File(root,"beast-trait-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Beast Trait sesleri 18</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0;cursor:pointer}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}nav{position:sticky;top:0;background:#17191c;padding:12px;z-index:1}label{cursor:pointer}</style>
<main><h1>Beast ability / trait sesleri · Seçki 18</h1><p>60 aday. GS grubunda 10; diğer her grupta 5. Bunlar henüz oyuna eklenmedi.</p>
<p>BC Boar hücum; BP Boar hazırlık; CJ Crocodile sıçrama; CP Crocodile hazırlık; EG yumurta; SB Swarm Being; SS Split; RG Bear öfke; GC Gorilla hazırlık kükremesi; GT uçan kaya; GS yere vuruş.</p>
<p>BC1–3 1,15s, CJ1–3 0,55s; 4–5 kısa başlangıç alternatifleri. Hazırlık hırıltısı isteğini hem Boar hem Crocodile için ayrı sundum. Gorilla hazırlığı 0,5s: uzun kükreme adayları seçilirse uygun kesit kullanılacak.</p>
<p>Egg grubunda tek gerçek yumurta kaydının beş düzenlemesi var; bu beş bağımsız kayıt iddiası değildir. Swarm Being sürekli loop değil kısa pasif-tepki önerisidir. Sesler ücretsiz kaynaklardan tasarımlardır; bazıları farklı kesit/perde/miks içerir. Önce düşük sesle dinle. Peak −12dBFS, algısal loudness eşleme değil.</p>
<p><a href='LICENSE-SOURCES.txt'>Lisanslar</a> · <a href='SECENEKLER.txt'>Aday dökümü</a>. Guard ölüm sesi humanoid turunda seçilecek.</p>
<nav>${groups.joinToString(" · "){"<a href='#${it.id}'>${it.id}</a>"}}</nav>$body
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='4'></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(count==60 && out.listFiles()!!.count{it.extension=="wav"}==71)
        println("BEAST_TRAIT_AUDITION_PASS: 60 unique non-silent candidates + 11 comparisons; timed charge/jump variants; no new runtime sounds")
    }
}
