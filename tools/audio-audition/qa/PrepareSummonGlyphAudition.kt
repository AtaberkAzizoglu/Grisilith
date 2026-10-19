package qa

import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import java.io.File
import kotlin.math.ceil

/** Pack 12 is an audition artifact only; no installed assets or gameplay are changed. */
object PrepareSummonGlyphAudition {
    private const val RATE=44100
    private data class Group(val id:String,val title:String,val note:String,val choices:List<C>)
    private fun c(label:String,vararg l:L)=C(label,l.toList())
    private fun s(file:String,speed:Double=1.0,seconds:Double=2.4,gain:Double=1.0)=
        L("spell-choices",file,speed=speed,maxSeconds=seconds,gain=gain)
    private fun air(speed:Double=1.0)=L("air-move","wind.wav",speed=speed,maxSeconds=1.0,gust=true)
    private fun fire(speed:Double=1.0)=L("fire-evil","spells/Fire.aif",speed=speed,maxSeconds=1.0,transient=1,gust=true)
    private fun boom(speed:Double=1.0)=s("explosions/explode.wav",speed,1.7).copy(transient=1)
    private fun mini(speed:Double=1.0)=s("explosions/explodemini.wav",speed,1.2).copy(transient=1)
    private fun rumble(speed:Double=.7)=L("focus-ability","thunder.wav",speed=speed,maxSeconds=2.3,transient=1,lowPass=1100.0)
    private fun dark(n:Int=1,speed:Double=.65)=L("dark-magic","dark_magic/fout-0$n.flac",speed=speed,maxSeconds=1.7,transient=1,lowPass=1500.0)
    private fun cow(speed:Double=1.0)=s("MudchuteAnimals/Mudchute_cow_1.ogg",speed,2.5).copy(transient=1)
    private fun naturalMale(speed:Double=.7)=s("greetings.wav",speed,2.0).copy(reverse=true,transient=1)
    private fun naturalFemale(speed:Double=1.0)=s("magic_words.wav",speed,2.3).copy(reverse=true,transient=1)
    private fun echo(label:String,layer:L,depth:Double=.2)=C(label,listOf(layer,layer.copy(offset=layer.offset+.14,gain=layer.gain*depth),
        layer.copy(offset=layer.offset+.29,gain=layer.gain*depth*.5)))
    private fun chant(label:String,tone:String,seed:Int,speed:Double=1.0):C {
        val order=when(seed%4){0->listOf(1,3,2,4,2,1,4,3);1->listOf(4,1,3,2,1,4,2,3);
            2->listOf(2,4,1,3,4,2,3,1);else->listOf(3,2,4,1,3,1,2,4)}
        val layers=order.mapIndexed{i,n->s("${tone}_$n.ogg",speed,.45).copy(offset=i*.23)}
        return C(label,layers+layers.map{it.copy(offset=it.offset+.12,gain=.17)})
    }
    private fun voiceGroup(id:String,title:String,note:String,male:Boolean,deep:Boolean):Group {
        val natural=if(male)naturalMale(if(deep).65 else 1.3) else naturalFemale(if(deep).8 else 1.15)
        val tone=if(male){if(deep)"male_deep" else "male_standard"}else{if(deep)"female_standard" else "female_light"}
        val speed=if(deep).85 else 1.15
        return Group(id,title,note+" İlk iki seçenek kayıt kesiti/ters ses tasarımı, 3–4 açıkça sentetik hece dizisi. Dil iddiası yok; beşinci seçenek sözsüz alternatif.",listOf(
            echo("1 · Kayıttan mistik ters sözler",natural),
            echo("2 · Daha yankılı ve katmanlı sözler",natural.copy(speed=natural.speed*.92,lowPass=if(deep)1700.0 else 6000.0),.35),
            chant("3 · Sentetik ritüel heceleri",tone,0,speed),
            chant("4 · Sentetik farklı hece dizisi",tone,2,speed*.95),
            c("5 · Alternatif: sözsüz büyü hazırlığı",dark(2,.9).copy(reverse=true,gain=.4),L("magic-spells","magical_1.ogg",maxSeconds=1.7,gain=.6))
        ))
    }
    private val groups=listOf(
        Group("PX","Acidic Pool · ateş patlaması","1–4 ateşli patlama tasarımı; 5 daha farklı bir basınç boşalması. Asit döngüsü bu kayıtlara gömülmedi.",listOf(
            c("1 · Tok alev patlaması",boom(),fire(.8).copy(gain=.45)),
            c("2 · Kalın ve ağır alev",boom(.8),fire(.7).copy(gain=.55)),
            c("3 · Hızlı alev infilakı",mini(),fire(1.1).copy(gain=.6)),
            c("4 · Geniş alev darbesi",boom(.9),air(.8).copy(gain=.3),fire(.85).copy(offset=.12,gain=.5)),
            c("5 · Alternatif: buhar/basınç boşalması",L("tinysized","sfx-cc0/compressed-air-spray-02.wav",maxSeconds=1.3,speed=.75,transient=1),rumble().copy(gain=.2))
        )),
        Group("AN","Ivy Staff · Summon Animals","İlk dört aday aynı gerçek inek kaydının doğal/perde/yankı varyasyonlarıdır, dört farklı inek kaydı değildir. Beşinci moo yerine doğa büyüsü yaklaşımı. Cow kaynağı CC BY-SA 3.0; seçilirse türetilen sesin de aynı lisansla dağıtılması gerekir.",listOf(
            c("1 · Doğal kısa möö",cow()),
            c("2 · Biraz daha kalın möö",cow(.85)),
            echo("3 · Hafif yankılı çağrı",cow(.95),.15),
            c("4 · Kısa ve daha canlı möö",cow(1.12).copy(maxSeconds=1.8)),
            c("5 · Alternatif: doğa çağrısı, moo yok",L("magic-spells","magical_1.ogg",maxSeconds=1.4),air(.9).copy(gain=.25))
        )),
        Group("DE","Ivy Staff · Summon Demons","1–4 imp çığlığı/demon heceleri. 1–2 yeni Goblin Scream kaydından; 3–4 sentetik demon konuşma heceleri. 5 sözsüz portal alternatifi.",listOf(
            c("1 · Kısa imp çığlığı",s("goblin_scream.wav",1.15,1.5).copy(transient=1)),
            echo("2 · Daha karanlık imp çığlığı",s("goblin_scream.wav",.85,1.7).copy(transient=1),.25),
            chant("3 · Sentetik demon heceleri","demon",1,.85),
            chant("4 · Boğuk demon mırıltısı","demon",3,.65),
            c("5 · Alternatif: alevli portal",fire(.7),dark(3,.7).copy(gain=.5))
        )),
        Group("OA","Ivy Staff · Call the Oathbreaker","İlk dört çok pes erkek tınısında anlamsız ritüel/dua tasarımı; gerçek dini metin kullanılmıyor. 1–2 ters çevrilmiş erkek kayıtları, 3–4 sentetik hece dizileri. 5 konuşmasız mühür kırılması.",listOf(
            echo("1 · Çok pes ters ritüel",naturalMale(.5).copy(lowPass=1500.0,maxSeconds=2.7),.3),
            echo("2 · Çift katmanlı ağır çağrı",s("wellmet1.wav",.45,2.6).copy(reverse=true,transient=1,lowPass=1400.0),.4),
            chant("3 · Sentetik bas dua dizisi","male_deep",1,.6),
            chant("4 · Sentetik derin erkek ayini","male_deep",3,.5),
            c("5 · Alternatif: konuşmasız kadim mühür",dark(3,.5),rumble(.55).copy(gain=.4))
        )),
        Group("CL","Chalk Dust · Call Lightning etkisi","Yere inen yıldırım: 1–4 net başlangıç darbesi + gürleyen kuyruk. 5 daha elektrik ağırlıklı, gök gürültüsü az. Casting sözleri aşağıdaki ayrı grupta.",listOf(
            c("1 · Net yıldırım ve gümbürtü",L("focus-ability","thunder.wav",maxSeconds=2.3,transient=1)),
            c("2 · Daha ağır yere iniş",rumble(.8),mini().copy(gain=.25)),
            c("3 · Keskin çakma, kalın kuyruk",L("tinysized","sfx-cc0/paralyzer-discharge-01.wav",maxSeconds=.2,transient=1,gain=.4),rumble().copy(offset=.05)),
            c("4 · Çok katmanlı yıldırım darbesi",rumble(.9),L("focus-ability","thunder.wav",startSeconds=3.0,maxSeconds=1.7,gain=.4,offset=.12)),
            c("5 · Alternatif: kısa elektrik yarılması",L("magic-spells","electricspell.ogg",maxSeconds=.9,transient=1),mini(1.2).copy(gain=.2))
        )),
        Group("TS","Chalk Dust · Tsunami etkisi","1–4 su şıpırtısından ziyade ezici dalga/basınç/kütle hissi için dalga ve düşük frekans katmanları. 5 daha soyut yaklaşan tehdit.",listOf(
            c("1 · Derin dalga duvarı",s("wave_01.flac",.75,2.8).copy(lowPass=1600.0),rumble(.6).copy(gain=.6)),
            c("2 · Ezici yaklaşan su kütlesi",s("wave_02.flac",.65,3.0).copy(lowPass=1200.0),rumble(.55).copy(gain=.7)),
            c("3 · Geniş dalga ve basınç",s("wave_03.flac",.8,2.8),rumble(.7).copy(gain=.65),air(.7).copy(gain=.3)),
            c("4 · Karanlık, ağır dalga çöküşü",s("wave_02.flac",.55,2.8).copy(lowPass=1000.0),s("wave_01.flac",.85,2.5,.45).copy(offset=.15),rumble(.6).copy(gain=.7)),
            c("5 · Alternatif: su yerine yaklaşan basınç",rumble(.45),L("wind-loop","wind.ogg",maxSeconds=2.8,speed=.55,lowPass=950.0,gain=.7))
        )),
        Group("MT","Chalk Dust · Meteor etkisi","1–4 düşüş/hava geçişi ardından büyük çarpışma ve patlama. Bunlar meteor kaydı değil, tasarlanmış katmanlar. 5 daha kuru taş parçalanması.",listOf(
            c("1 · Düşüş + ağır patlama",air(.8).copy(maxSeconds=.8),boom(.8).copy(offset=.6)),
            c("2 · Hızlı düşüş + tok darbe",air(1.1).copy(maxSeconds=.6),boom(.7).copy(offset=.45),rumble(.7).copy(offset=.5,gain=.4)),
            c("3 · Geniş, alevli meteor",air(.7),boom(.9).copy(offset=.7),fire(.75).copy(offset=.65,gain=.45)),
            c("4 · Çok ağır göktaşı çarpışması",air(.6),boom(.6).copy(offset=.75),rumble(.55).copy(offset=.8,gain=.6)),
            c("5 · Alternatif: kuru taş yarılması",L("impact-sounds","impactMining_000",maxSeconds=.8,speed=.6),L("impact-sounds","impactMining_003",maxSeconds=.7,speed=.8,offset=.12),rumble(.7).copy(gain=.3))
        )),
        Group("SG","Chalk Dust · Singularity etkisi","Özel isteğine göre ilk iki aday hayali karadelik vızıltısı/gürültüsü. Son üç benim alternatiflerim: içe çöküş, uzay yırtılması, düşük nabız. Gerçek karadelik ses kaydı/sonifikasyonu değil.",listOf(
            c("1 · Boğuk uzaysal vızıltı",L("magic-spells","electricspell.ogg",maxSeconds=2.4,speed=.5,lowPass=800.0,transient=1),L("wind-loop","wind.ogg",maxSeconds=2.4,speed=.6,gain=.3)),
            c("2 · Kalın elektromanyetik gürültü",L("magic-spells","electricspell.ogg",maxSeconds=2.4,speed=.65,lowPass=1100.0,transient=1),dark(1,.5).copy(gain=.35)),
            c("3 · Alternatif: içe çöken boşluk",rumble(.5).copy(reverse=true),air(.5).copy(reverse=true,gain=.45)),
            c("4 · Alternatif: uzay yırtılması",dark(2,.6).copy(reverse=true),L("magic-spells","magical_1.ogg",speed=.6,maxSeconds=2.1,lowPass=1500.0,gain=.4)),
            c("5 · Alternatif: düşük kozmik nabız",L("impact-sounds","impactSoft_heavy_003",maxSeconds=.7,speed=.55,lowPass=700.0),L("impact-sounds","impactSoft_heavy_003",maxSeconds=.7,speed=.55,lowPass=700.0,offset=.5,gain=.7),dark(3,.5).copy(gain=.3))
        )),
        voiceGroup("VL","Casting · Call Lightning / pes erkek","Büyü çizilirken kullanılacak sözler; yıldırım patlaması içermez.",true,true),
        voiceGroup("VT","Casting · Tsunami / ince kadın","Büyü çizilirken ince kadın tınısı; tsunami etkisi içermez.",false,false),
        voiceGroup("VM","Casting · Meteor / ince erkek","Büyü çizilirken daha ince erkek tınısı; meteor patlaması içermez.",true,false),
        voiceGroup("VS","Casting · Singularity / pes kadın","Büyü çizilirken daha pes kadın tınısı; karadelik etkisi içermez.",false,true)
    )

    private fun esc(value:String)=value.replace("&","&amp;").replace("<","&lt;").replace("'","&#39;")
    @JvmStatic fun main(args:Array<String>) {
        val out=File("tools/audio-audition/listening-pack-12").apply{mkdirs()}
        val html=StringBuilder();val manifest=StringBuilder("60 audition candidates; not installed. First four follow request; fifth alternative. SG has requested 2+3 exception.\n")
        groups.forEach{group->
            val clips=group.choices.map(PrepareAudioAudition::render)
            val seconds=ceil(clips.maxOf{it.size}.toDouble()/RATE+1).toInt().coerceAtLeast(3)
            val combined=FloatArray(RATE*(1+seconds*5))
            html.append("<section id='${group.id}'><h2>${group.id} · ${esc(group.title)}</h2><p>${esc(group.note)}</p><p>Toplu dinleme 1 → 5, her aday bir kez, $seconds saniyelik aralıklarla.</p><audio controls preload='none' src='${group.id}-all.wav'></audio><div class='grid'>")
            group.choices.forEachIndexed{i,candidate->
                val id="${group.id}${i+1}";PrepareAudioAudition.wav(File(out,"$id.wav"),clips[i])
                clips[i].copyInto(combined,RATE*(1+i*seconds))
                val sources=candidate.layers.map{"${it.pack}/${it.file}"}.distinct().joinToString(" + ")
                manifest.append("$id: ${candidate.label}\n$sources\n")
                html.append("<article><label><input type='radio' name='${group.id}' value='$id'><b>$id</b> ${esc(candidate.label)}</label><audio controls preload='none' src='$id.wav'></audio><small>${esc(sources)}</small></article>")
            }
            PrepareAudioAudition.wav(File(out,"${group.id}-all.wav"),combined);html.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File("tools/audio-audition/summon-glyph-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        val navigation=groups.joinToString(" · "){"<a href='#${it.id}'>${it.id}</a>"}
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Ses seçkisi 12</title>
<style>body{background:#17191c;color:#eee;font:16px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}nav{position:sticky;top:0;background:#17191c;padding:10px}</style>
<main><h1>Grisilith · Summon, büyü etkileri ve casting sözleri</h1><p>60 aday / 12 grup. Henüz hiçbirisi oyuna eklenmedi. Önceki IG1, ER3, VP4, SC3, AP2, PS4, BL5 seçimleri de bekletiliyor.</p>
<p>Genel düzen: 1–4 tarifine yakın, 5 farklı yaklaşım. Singularity özel isteğin: 1–2 vızıltı/gürültü, 3–5 farklı fikirler.</p>
<p><b>Sesli sözler:</b> bazıları ters çevrilmiş/işlenmiş insan kayıtları, bazıları açıkça sentetik ses hecelerinin birleşimi. Her biri ayrı özgün aktör kaydı değildir. Anlaşılır bir dil amaçlanmıyor; gerçek Elfçe veya D&amp;D Celestial değildir. Oyunda casting süresine göre uyarlanacak; bunlar seçim taslakları.</p>
<p>Ivy AN/DE/OA: seçenek onayı ve ability kullanımı için. Chalk CL/TS/MT/SG: büyü gerçekleştiğinde; VL/VT/VM/VS: çizim/casting sırasında. Bu sesler birleştirilmedi. Önce cihazın sesini düşük tut; mono 44.1kHz/16-bit, −12dBFS peak eşleme, algısal loudness eşitlemesi değil.</p>
<p><a href='LICENSE-SOURCES.txt'>Ücretsiz kaynaklar ve lisanslar</a>. Cow ve doğal erkek kayıtları CC BY-SA 3.0: seçilirse düzenlenmiş sesler de aynı lisansla dağıtılır. Atıf ve varlık lisansı koşulları kaynak dosyasında belirtilmiştir.</p><nav>$navigation</nav>
$html
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='3'></textarea></section></main>
<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(groups.size==12 && out.listFiles()!!.count{it.extension=="wav"}==72)
        println("SUMMON_GLYPH_AUDITION_PASS: 60 candidates + 12 comparisons; headers/non-silence validated. No game changes.")
    }
}
