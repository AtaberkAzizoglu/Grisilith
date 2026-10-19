package qa

import java.io.File
import java.security.MessageDigest
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C

/** Auditions only: never writes assets/ or changes gameplay/settings. */
object PrepareBeastCombatAudition {
    private const val RATE=44100
    private val source=File("tools/audio-audition/sources/beast-voices/Audio")
    private fun s(file:String,speed:Double=1.0,seconds:Double=1.0)=L("beast-voices",file,speed=speed,maxSeconds=seconds)
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun dog(n:Int,speed:Double=1.0,seconds:Double=.65)=s("dog/$n.ogg",speed,seconds)
    private fun eagle(n:Int,speed:Double=1.0,seconds:Double=.6)=s("eagle-call-$n.wav",speed,seconds)
    private fun pig(n:Int,speed:Double=.9,seconds:Double=.7)=s("pig-call-$n.wav",speed,seconds)
    private fun bear(n:Int,speed:Double=1.0,seconds:Double=.8)=s("bear-call-$n.wav",speed,seconds)
    private fun croc(n:Int,speed:Double=1.0,seconds:Double=.8)=s("croc-call-$n.wav",speed,seconds)
    private fun gorilla(name:String,speed:Double=1.0,seconds:Double=.7)=s("gorilla/ogg/gorilla_$name.ogg",speed,seconds)
    private fun bee(speed:Double=1.0,seconds:Double=.6,start:Double=0.0)=s("bee-call.wav",speed,seconds).copy(startSeconds=start)
    private fun thud(n:Int=2,speed:Double=1.0)=L("impact-sounds","impactSoft_heavy_00$n.ogg",speed=speed,maxSeconds=.4,gain=.35)
    private fun air(n:Int=4,speed:Double=1.0)=L("swishes","swishes/swish-$n.wav",speed=speed,maxSeconds=.35,gain=.25)
    private data class Group(val id:String,val title:String,val note:String,val choices:List<C>)
    private fun groups()=listOf(
        Group("TD","Toad · ölüm","Vıraklama tasarımları. Üç normal kurbağa çağrısından beş farklı düzenleme; gerçek ölüm kaydı değil.",listOf(
            c("Kısa, doğal vıraklama",s("frog-call-1.wav",1.0,.8)),
            c("Pes, boğazdan gelen kapanış",s("frog-call-2.wav",.78,1.1)),
            c("İnce ve hızla sönen vırak",s("frog-call-3.wav",1.15,.65).copy(gust=true)),
            c("İki heceli, ağır vıraklama",s("frog-call-1.wav",.72,1.25)),
            c("Boğuk vırak ve hafif düşüş",s("frog-call-3.wav",.85,.95).copy(lowPass=1800.0),thud(0,.85).copy(offset=.25,gain=.18))
        )),
        Group("EA","Eagle · saldırı","Gerçek kartal çağrıları: kısa gagalama/saldırı vurgusu; çığlıklar özel ability sesi değil.",listOf(
            c("Kısa kartal çığlığı",eagle(1)),
            c("Daha sert, tok ötüş",eagle(2,.88,.65)),
            c("İnce, keskin saldırı çağrısı",eagle(3,1.12,.5)),
            c("Çağrı ve hafif hava yarma",eagle(4,1.0,.6),air(8,1.3).copy(offset=.07)),
            c("Alçak, kuru gagalama vurgusu",eagle(2,.95,.45).copy(lowPass=3500.0),thud(1,1.4).copy(offset=.1,gain=.15))
        )),
        Group("ED","Eagle · ölüm","Normal kartal çağrılarının daha uzun/sönen tasarımları; gerçek acı kaydı değil.",listOf(
            c("Uzatılmış son çığlık",eagle(1,.85,1.3)),
            c("Boğuk, düşen kartal çağrısı",eagle(2,.72,1.4).copy(lowPass=2500.0)),
            c("Keskin kısa çığlık, hızlı sönüş",eagle(3,.95,.9).copy(gust=true)),
            c("Çığlık ve hafif kanat/hava kapanışı",eagle(4,.85,1.2),air(12,.85).copy(offset=.35,gain=.15)),
            c("Kuru, pes ve kısa kapanış",eagle(3,.7,1.0).copy(lowPass=1800.0,gust=true))
        )),
        Group("WA","Wolf · saldırı","Altı köpek hırlamasından kurt tasarımları. Gerçek kurt kaydı değil; kısa ısırma/hırlama kimliği.",listOf(
            c("Kısa, diş gösteren hırlama",dog(0)),
            c("Pes, güçlü saldırı homurtusu",dog(1,.85)),
            c("Kuru ve keskin hırlama",dog(2,1.1,.45)),
            c("Hırıltılı nefes ve ısırık vurgusu",dog(3,.95,.65),thud(1,1.3).copy(offset=.12,gain=.15)),
            c("Derin, daha yırtıcı hırlama",dog(4,.78,.75))
        )),
        Group("WD","Wolf · ölüm","Normal hırlamadan sönüş/çöküş tasarımları. Uzun uluma değil; kalabalıkta kısa kalacak.",listOf(
            c("Boğuk, sönen hırıltı",dog(5,.75,1.3).copy(gust=true)),
            c("Alçak, nefesi tükenen homurtu",dog(1,.65,1.35).copy(lowPass=1800.0)),
            c("Kısa hırlama ve yere düşüş",dog(2,.85,.8),thud(2,.8).copy(offset=.35)),
            c("Daha uzun, boğazdan son hırıltı",dog(3,.7,1.5)),
            c("Yumuşak ve kısa sönüş",dog(0,.8,.9).copy(lowPass=1500.0,gust=true))
        )),
        Group("BA","Boar · saldırı","Vokal domuz taklidi ve normal domuz homurtularından tasarımlar. Charge/hücum sesini sonraki turda ayrıca seçeceğiz.",listOf(
            c("Küt, kısa yaban domuzu homurtusu",s("boar.ogg",1.0,.65)),
            c("Doğal, pes burun homurtusu",pig(1,.8,.65)),
            c("Daha kuru, sert nefes",pig(2,.85,.55)),
            c("Hırıltı ve kısa gövde darbesi",pig(3,.75,.7),thud(3,.8).copy(offset=.12)),
            c("Daha sert, kısa burun sesi",pig(4,.85,.65).copy(highPass=100.0))
        )),
        Group("BD","Boar · ölüm","Yalnız normal çağrı ve taklit kaynakları: gerçek domuz acı/hit kayıtları kullanılmadı.",listOf(
            c("Pes, uzayan homurtu",pig(1,.65,1.25)),
            c("Boğuk son nefes",s("boar.ogg",.65,1.2).copy(lowPass=1800.0)),
            c("Kısa homurtu ve ağır düşüş",pig(2,.75,.9),thud(4,.7).copy(offset=.35)),
            c("Daha pürüzlü, uzun kapanış",pig(3,.65,1.4)),
            c("Sönen, kuru homurtu",pig(4,.72,1.1).copy(gust=true,lowPass=2200.0))
        )),
        Group("CA","Crocodile · saldırı","Alligator hırlamasından Crocodile tasarımları. Kısa çene saldırısı; sıçrama ve yavru sesi bu turda değil.",listOf(
            c("Kısa sürüngen hırlaması",croc(1)),
            c("Daha pes ve gövdeli hırıltı",croc(2,.8,.8)),
            c("Kuru, tıslamaya yakın kesit",croc(3,1.15,.55).copy(highPass=450.0)),
            c("Hırlama ve tok çene kapanışı",croc(2,.95,.65),thud(2,1.1).copy(offset=.15,gain=.45)),
            c("Derin, kısa saldırı nefesi",croc(4,.78,.8).copy(lowPass=1900.0))
        )),
        Group("CD","Crocodile · ölüm","Tek alligator saha kaydının farklı kesitleri + sönüş/çöküş tasarımları; gerçek ölüm kaydı değil.",listOf(
            c("Uzun, pes sürüngen hırıltısı",croc(1,.7,1.7)),
            c("Boğuk, hızla sönen hırlama",croc(2,.65,1.4).copy(gust=true,lowPass=1600.0)),
            c("Hırlama ve ağır gövde düşüşü",croc(3,.8,1.1),thud(3,.65).copy(offset=.45,gain=.45)),
            c("Daha pürüzlü, uzun kapanış",croc(4,.7,1.8)),
            c("Kısa, nefesi tükenen sürüngen",croc(2,.85,.95).copy(gust=true))
        )),
        Group("SA","Bee Swarm · saldırı","Tek arı kaydından sürü tasarımları. Sürekli ortam uğultusu değil, kısa saldırı vurguları.",listOf(
            c("Kısa, yakın arı vızıltısı",bee(1.1,.45)),
            c("Kalın, sıkışık sürü hamlesi",bee(.85,.6),bee(1.1,.5,.2).copy(offset=.04,gain=.5)),
            c("İnce ve saldırgan vızıltı",bee(1.35,.4,.3).copy(highPass=400.0)),
            c("Üç katmanlı kısa sürü saldırısı",bee(.9,.55),bee(1.15,.5,.2).copy(offset=.03,gain=.55),bee(1.3,.4,.4).copy(offset=.06,gain=.35)),
            c("Vızıltı ve hafif batma vurgusu",bee(1.05,.45,.5),air(8,1.8).copy(offset=.05,gain=.2))
        )),
        Group("SD","Bee Swarm · ölüm","Arılara çığlık vermek yerine dağılan/sönen kanat vızıltısı. Split sesi ayrıca seçilecek.",listOf(
            c("Kısa, sönen vızıltı",bee(.9,.9).copy(gust=true)),
            c("Pes, dağılan sürü",bee(.7,1.2).copy(gust=true),bee(1.0,.8,.2).copy(offset=.15,gain=.35,gust=true)),
            c("İnce, hızla kesilen vızıltı",bee(1.25,.65,.3).copy(gust=true)),
            c("Dağılan iki kanat dokusu",bee(.85,1.0,.1).copy(gust=true),bee(1.2,.7,.4).copy(offset=.12,gain=.4,gust=true)),
            c("Boğuk ve kısa sürü sönüşü",bee(.75,.8,.2).copy(lowPass=1300.0,gust=true))
        )),
        Group("RA","Bear · saldırı","İki ayı kaydından farklı kesitler. Rage aktivasyonu değil, normal pençe saldırısının kısa vurgusu.",listOf(
            c("Kısa, tok ayı hırlaması",bear(1)),
            c("Daha sert, boğazdan hırıltı",bear(2,.9,.85)),
            c("Derin homurtu ve pençe havası",bear(1,.8,.7),air(4,.85).copy(offset=.1)),
            c("Kuru, hızlı pençe eforu",bear(2,1.15,.5).copy(highPass=120.0)),
            c("Ağır hırlama ve gövde darbesi",bear(2,.78,.8),thud(4,.75).copy(offset=.15))
        )),
        Group("RD","Bear · ölüm","Normal ayı hırlamasından uzun sönüş tasarımları; gerçek ölüm performansı değil.",listOf(
            c("Derin ve uzun son hırıltı",bear(1,.65,1.9)),
            c("Boğuk, sönen ayı sesi",bear(2,.7,1.6).copy(lowPass=1400.0,gust=true)),
            c("Hırlama ve ağır düşüş",bear(1,.8,1.2),thud(3,.6).copy(offset=.5,gain=.4)),
            c("Daha sert, pürüzlü kapanış",bear(2,.6,1.8)),
            c("Kısa ve yorgun sönüş",bear(1,.8,1.0).copy(gust=true))
        )),
        Group("GA","Gorilla · saldırı","Altı gerçek goril çağrısından normal yakın dövüş eforları. Kaya atışı / Stomp sonraki turda.",listOf(
            c("Tok, kısa goril homurtusu",gorilla("grunt")),
            c("Sert, diş gösteren hırlama",gorilla("snarl_01",.9,.7)),
            c("Derin saldırı homurtusu",gorilla("snarl_02",.8,.8)),
            c("Keskin hırlama ve yumruk vurgusu",gorilla("snarl_03",1.05,.55),thud(4,.75).copy(offset=.12)),
            c("Kısa kükreme, güçlü efor",gorilla("roar",.95,.8))
        )),
        Group("GD","Gorilla · ölüm","Kükreme/hırlamadan boss kapanışı. Hasar alma veya özel ability sesleri bu turda değil.",listOf(
            c("Derin, uzun kükreme",gorilla("roar",.7,2.0)),
            c("Boğuk, nefesi tükenen hırıltı",gorilla("snarl_04",.7,1.6).copy(lowPass=1800.0,gust=true)),
            c("Homurtu ve ağır gövde düşüşü",gorilla("grunt",.65,1.3),thud(4,.6).copy(offset=.45,gain=.45)),
            c("Sert hırlamadan sönüş",gorilla("snarl_02",.65,1.8).copy(gust=true)),
            c("Daha kısa, tok boss kapanışı",gorilla("snarl_01",.7,1.25).copy(lowPass=1500.0),thud(2,.65).copy(offset=.4,gain=.25))
        ))
    )

    /** Extract a foreground window around the strongest 50ms region, keeping enough lead-in. */
    private fun foreground(input:L,out:String,seconds:Double) {
        val pcm=PrepareAudioAudition.render(c("foreground",input.copy(maxSeconds=120.0)))
        val hop=RATE/20
        val peak=(0 until max(1,pcm.size/hop)).maxBy {n->
            (n*hop until min(pcm.size,(n+1)*hop)).sumOf{pcm[it].toDouble().pow(2)}
        }
        val count=min(pcm.size,(seconds*RATE).toInt())
        val start=(peak*hop-RATE/8).coerceIn(0,max(0,pcm.size-count))
        PrepareAudioAudition.wav(File(source,out),pcm.copyOfRange(start,start+count))
    }
    @JvmStatic fun main(args:Array<String>) {
        source.listFiles()!!.filter{it.extension=="mp3"}.forEach{file->
            val rec=PrepareFemaleIncantationAudition.decode(file)
            val pcm=FloatArray((rec.samples.size.toDouble()*RATE/rec.rate).toInt()){i->rec.samples[min(rec.samples.lastIndex,(i.toDouble()*rec.rate/RATE).toInt())]}
            PrepareAudioAudition.wav(File(source,"${file.nameWithoutExtension}.wav"),pcm)
        }
        (1..4).forEach{foreground(s("eagle/pla1554alaska-ccby-gpl-eagle/eagle$it.flac"),"eagle-call-$it.wav",1.8)}
        (1..4).forEach{foreground(s(if(it==1)"pig_idle.wav" else "pig_idle$it.wav"),"pig-call-$it.wav",1.8)}
        (1..3).forEach{foreground(s("ribbit_0$it.wav"),"frog-call-$it.wav",1.5)}
        (1..2).forEach{foreground(s("bear/ogg/bear_0$it.ogg"),"bear-call-$it.wav",2.0)}
        // Four separate windows from one alligator recording, clearly disclosed in the listening page.
        (1..4).forEach{foreground(s("alligator.wav").copy(startSeconds=(it-1)*1.8),"croc-call-$it.wav",1.7)}
        foreground(s("bee/bee.wav"),"bee-call.wav",3.0)
        if("--inspect" in args) {
            source.walkTopDown().filter{it.isFile&&it.extension in setOf("wav","ogg")&&!it.name.startsWith("._")}.forEach{
                val pcm=PrepareAudioAudition.render(c(it.name,s(it.relativeTo(source).invariantSeparatorsPath,seconds=120.0)))
                println("${it.relativeTo(source)}: ${pcm.size.toDouble()/RATE}s")
            };return
        }
        writePack()
    }
    private fun writePack() {
        val out=File("tools/audio-audition/listening-pack-17").apply{mkdirs()}
        val html=StringBuilder();val manifest=StringBuilder("75 preview candidates, NOT INSTALLED. Codes are local to PACK 17.\n")
        val hashes=mutableSetOf<String>();var count=0
        val groups=groups()
        groups.forEach{g->
            val clips=g.choices.map{candidate->PrepareAudioAudition.render(candidate).also{pcm->
                check(pcm.size>RATE/20 && pcm.all{it.isFinite()})
                val fade=min(882,pcm.size/4);repeat(fade){i->pcm[i]*=i.toFloat()/fade;pcm[pcm.lastIndex-i]*=i.toFloat()/fade}
                check(pcm.maxOf{abs(it)}>.02f)
            }}
            val slot=ceil(clips.maxOf{it.size}.toDouble()/RATE+.8).toInt()
            val combined=FloatArray(RATE*(1+slot*5))
            html.append("<section id='${g.id}'><h2>${g.id} · ${g.title}</h2><p>${g.note}</p><p>Toplu dinleme: 1 → 5, $slot saniyelik aralıklarla.</p><audio controls preload='none' src='${g.id}-all.wav'></audio><div class='grid'>")
            g.choices.forEachIndexed{i,candidate->
                val id="${g.id}${i+1}";val file=File(out,"$id.wav")
                PrepareAudioAudition.wav(file,clips[i]);clips[i].copyInto(combined,RATE*(1+i*slot))
                val hash=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
                check(hashes.add(hash)){"Identical candidate $id"};count++
                val recipe=candidate.layers.joinToString(" + "){"${it.pack}/${it.file} speed=${it.speed} start=${it.startSeconds}s max=${it.maxSeconds}s offset=${it.offset}s"}
                manifest.append("$id: ${candidate.label}\n$recipe\n")
                html.append("<article><label><input type='radio' name='${g.id}' value='$id'> <b>$id</b> · ${candidate.label}</label><audio controls preload='none' src='$id.wav'></audio><details><summary>Kaynak / düzenleme</summary><small>$recipe</small></details></article>")
            }
            PrepareAudioAudition.wav(File(out,"${g.id}-all.wav"),combined);html.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File("tools/audio-audition/beast-combat-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Beast ve Toad sesleri 17</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0;cursor:pointer}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}nav{position:sticky;top:0;background:#17191c;padding:12px;z-index:1}label{cursor:pointer}</style>
<main><h1>Beast saldırı / ölüm + Toad ölüm · Seçki 17</h1>
<p>Her grupta 5 seçenek: toplam 75 aday. Henüz hiçbiri oyuna eklenmedi. Sadece normal saldırı ve ölüm; Beast ability sesleri sonraki turda.</p>
<p>TD Toad ölüm; EA/ED Eagle; WA/WD Wolf; BA/BD Boar; CA/CD Crocodile; SA/SD Bee Swarm; RA/RD Bear; GA/GD Gorilla. A: saldırı, D: ölüm. Kodlar yalnız bu seçkiye aittir (CA5 burada Cow değildir).</p>
<p>Ölüm adayları normal çağrı/hırlamalardan tasarlanmıştır; gerçek hayvan acısı kaydı kullanılmadı. Wolf için köpek, Crocodile için alligator kayıtları uyarlanmıştır. Beş seçenek her zaman beş bağımsız saha kaydı anlamına gelmez; bazıları farklı kesit/miks/perde tasarımlarıdır. Sürüde çığlık yerine dağılan vızıltı vardır.</p>
<p>Toad saldırısı: mevcut Poison sesi seçildi. Guard saldırısı: mevcut Lance sesi seçildi. <b>Guard ölüm sesi humanoid enemy turunda seçilecek.</b> Oyuncu merkezli stereo ve Settings aç/kapat ayarı sonraki uygulama için bekliyor; bu tur değiştirilmedi.</p>
<p>Başta sesi düşük tut. Peak eşleme −12dBFS, algısal ses yüksekliği eşleme değil. <a href='LICENSE-SOURCES.txt'>Lisanslar</a> · <a href='SECENEKLER.txt'>Aday dökümü</a></p>
<nav>${groups.joinToString(" · "){"<a href='#${it.id}'>${it.id}</a>"}}</nav>
$html<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='4'></textarea></section></main>
<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(count==75 && out.listFiles()!!.count{it.extension=="wav"}==90)
        println("BEAST_AUDITION_PASS: 75 distinct non-silent candidates + 15 comparisons; no game assets/settings changed.")
    }
}
