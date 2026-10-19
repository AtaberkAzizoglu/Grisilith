package qa

import java.io.File
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C

/** Preview preparation only. No installed audio, gameplay or directional mixer changes. */
object PrepareSummonCombatAudition {
    private const val RATE=44100
    private fun s(file:String,speed:Double=1.0,seconds:Double=3.0)=L("summon-voices",file,speed=speed,maxSeconds=seconds)
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun monster(n:Int,speed:Double=1.0,seconds:Double=2.0)=s("monsters/monster_sfx_pack/monster-$n.wav",speed,seconds).copy(transient=1)
    private fun human(file:String,speed:Double=.8,seconds:Double=2.5)=s("humans/yelling sounds/$file.wav",speed,seconds).copy(transient=1)
    private fun fire(speed:Double=1.0)=L("fire-evil","spells/Fire.aif",speed=speed,maxSeconds=.7,transient=1,gust=true)
    private fun swish(n:Int,speed:Double=1.0)=L("swishes","swishes/swish-$n.wav",speed=speed,maxSeconds=.7,transient=1)
    private fun thud(n:Int=3,speed:Double=1.0)=L("impact-sounds","impactSoft_heavy_00$n.ogg",speed=speed,maxSeconds=.55)
    private fun moo(id:Int,speed:Double=.95)=s("cow-main-$id.wav",speed,3.0)
    private data class Group(val id:String,val title:String,val note:String,val choices:List<C>)

    private fun groups()=listOf(
        Group("CA","Cow · saldırı","Kısa homurtu/üfleme + kafa veya gövde darbesi. Bazıları aynı saha kaydının ayrı kesitleri; tek bir moo'nun beş perde kopyası değil.",listOf(
            c("Kısa üfleme ve tok darbe",s("cow-puff-1.wav",1.05,.5),thud().copy(offset=.14,gain=.6)),
            c("Daha pes homurtu",s("cow-puff-2.wav",.85,.65),thud(1,.85).copy(offset=.18,gain=.4)),
            c("Möö başlangıçlı kafa darbesi",moo(513558,1.1).copy(maxSeconds=.65),thud(0,.9).copy(offset=.22,gain=.5)),
            c("Kuru gövde darbesi, vokal az",thud(2,.8),s("cow-puff-3.wav",.9,.45).copy(gain=.25)),
            c("Daha vahşi kısa böğürtü",moo(513563,.85).copy(maxSeconds=.8),thud(4,.8).copy(offset=.20,gain=.45))
        )),
        Group("CD","Cow · ölüm","Normal, zarar görmemiş ineklerin beş ayrı moo kaydından tasarımlar. Gerçek hayvan acısı/ölümü kaydı değil. Daha pes/zayıflayan çağrı ve kimi adaylarda hafif düşüş darbesi.",listOf(
            c("Yavaş, kalın möö",moo(513559,.8)),
            c("Yakın, güçlü son böğürtü",moo(513558,.85),thud(0,.7).copy(offset=.8,gain=.2)),
            c("Daha kısa, yorgun çağrı",moo(513557,.9).copy(maxSeconds=2.2)),
            c("Boğuk möö ve yere düşüş",moo(513564,.75).copy(lowPass=1800.0),thud(2,.65).copy(offset=1.0,gain=.3)),
            c("Dramatik böğürtü, yumuşak kapanış",moo(513563,.75).copy(maxSeconds=2.6))
        )),
        Group("IA","Imp · saldırı","Gerçek saldırı menzilli ateş projectile'ı. İlk iki aday ateş ağırlıklı; diğerlerinde tek bir yaratık vokali ile kısa ateş atışı bir arada. Birden çok konuşmacı üst üste bindirilmedi.",listOf(
            c("Kısa ve kuru ateş tükürmesi",fire(1.3)),
            c("Tok ateş pufu",fire(.9),L("tinysized","sfx-cc0/compressed-air-spray-02.wav",maxSeconds=.3,transient=1,gain=.25)),
            c("Küçük yaratık hırlaması + alev",monster(2,1.25,.45).copy(gain=.55),fire(1.15).copy(offset=.09,gain=.8)),
            c("Keskin imp çığlığı + kısa ateş",L("spell-choices","goblin_scream.wav",speed=1.4,maxSeconds=.45,transient=1,gain=.4),fire(1.3).copy(offset=.06,gain=.7)),
            c("Demonik tıslama ve ateş",monster(5,1.15,.55).copy(gain=.5),fire(1.05).copy(offset=.08,gain=.8))
        )),
        Group("ID","Imp · ölüm","Yaratık ölümü/çığlıkları; summon çağrı sesinden ayrı kimlik. Çoğu orijinal yaratık/erkek kaydının imp için inceltilmiş hali, gerçek bir imp kaydı iddiası yok.",listOf(
            c("Kısa goblin çığlığı",L("spell-choices","goblin_scream.wav",speed=1.3,maxSeconds=1.5,transient=1)),
            c("Yırtıcı yaratık çöküşü",monster(7,1.3,1.6)),
            c("Boğuk, kısa yaratık ölümü",L("rubberduck-rpg","creature_die_01.ogg",speed=1.2,maxSeconds=1.5)),
            c("İnce, nefesi tükenen çığlık",monster(10,1.4,1.7)),
            c("İnsansı imp haykırışı",human("2yell5",1.4,1.6))
        )),
        Group("OA","Oathbreaker · saldırı","Ağır yakın dövüş/necrotic kimlik: savuruş ve metal gövde, bazı adaylarda pes erkek efor sesi. Tüm metal vurma seslerinin temas anına kilitlenmesi seçimden sonra yapılacak.",listOf(
            c("Ağır kılıç savuruşu, konuşmasız",swish(4,.8),L("rubberduck-rpg","chain_01.ogg",speed=.9,maxSeconds=.45,gain=.3,offset=.10)),
            c("Pes efor ve savuruş",human("3grunt1",.8,1.0).copy(gain=.6),swish(8,.9).copy(offset=.06,gain=.6)),
            c("Daha sert erkek hırıltısı",human("3grunt3",.7,.7).copy(gain=.65),swish(4,.8).copy(offset=.08,gain=.55)),
            c("Metalik, ağır ruh darbesi",swish(12,.7),L("tinysized","sfx-cc0/sword-clash-02.wav",speed=.8,maxSeconds=.5,gain=.45,offset=.1)),
            c("Derin yaratık eforu ve kesiş",monster(3,.65,.65).copy(lowPass=1800.0,gain=.5),swish(8,.75).copy(offset=.08,gain=.65))
        )),
        Group("OD","Oathbreaker · ölüm","Pes erkek ölüm performansları ve daha karanlık yaratık alternatifi. 1–4 farklı insan kaydından; 5 yaratık tasarımı. İsteğe göre doğaüstü sönüş veya insansı ölüm seçilebilir.",listOf(
            c("Derin, nefesi kesilen ölüm",human("3yell9",.7,2.7)),
            c("Boğuk ve daha kısa ölüm",human("3yell5",.75,2.2).copy(lowPass=2000.0)),
            c("Ağır erkek haykırışı",human("1yell10",.65,2.4)),
            c("Düşüş ve pes son nefes",human("yell8",.7,2.3),L("rubberduck-rpg","chain_03.ogg",speed=.65,maxSeconds=.8,gain=.3,offset=.5)),
            c("Demonik / ruhsal dağılma",monster(9,.6,2.6).copy(lowPass=1600.0),L("magic-spells","magical_1.ogg",speed=.65,maxSeconds=1.5,gain=.2,offset=.2))
        ))
    )

    @JvmStatic fun main(args:Array<String>) {
        val source=File("tools/audio-audition/sources/summon-voices/Audio")
        source.listFiles()!!.filter{it.extension=="mp3"}.forEach{file->
            val rec=PrepareFemaleIncantationAudition.decode(file)
            val pcm=FloatArray((rec.samples.size.toDouble()*RATE/rec.rate).toInt()){i->rec.samples[min(rec.samples.lastIndex,(i.toDouble()*rec.rate/RATE).toInt())]}
            PrepareAudioAudition.wav(File(source,"${file.nameWithoutExtension}.wav"),pcm)
        }
        // Field recordings contain long ambient lead-ins; extract the strongest calls instead
        // of mistakenly presenting the first short wind/noise transient as a moo.
        listOf(513559,513558,513557,513564,513563).forEach{id->
            val raw=PrepareAudioAudition.render(c("moo",s("cow-$id.wav",seconds=90.0).copy(highPass=80.0,lowPass=4000.0)))
            val hop=RATE/20
            val energy=(0 until raw.size/hop).map{n->(0 until hop).sumOf{raw[n*hop+it].toDouble().pow(2)}}
            val peak=energy.indices.maxBy{energy[it]};var first=peak;var last=peak
            while(first>0&&energy[first-1]>energy[peak]*.12)first--
            while(last<energy.lastIndex&&energy[last+1]>energy[peak]*.12)last++
            val start=max(0,max(first*hop-RATE/20,peak*hop-(RATE*1.2).toInt()))
            val end=min(raw.size,min((last+1)*hop+RATE/8,start+RATE*3))
            check(end>start)
            PrepareAudioAudition.wav(File(source,"cow-main-$id.wav"),raw.copyOfRange(start,end))
            println("Cow $id foreground: ${start.toDouble()/RATE}..${end.toDouble()/RATE}")
        }
        val puffs=PrepareAudioAudition.render(c("puffs",s("cow-513560.wav",seconds=90.0).copy(highPass=180.0,lowPass=3000.0)))
        val hop=RATE/20
        val energy=(0 until puffs.size/hop).map{n->(0 until hop).sumOf{puffs[n*hop+it].toDouble().pow(2)}}
        val peaks=mutableListOf<Int>()
        energy.indices.sortedByDescending{n->
            val previous=(max(0,n-8) until n).map{energy[it]}.average()
            energy[n]-.7*(if(previous.isFinite())previous else 0.0)
        }
            .forEach{n->if(peaks.size<3&&peaks.none{abs(it-n)<40})peaks+=n}
        peaks.sorted().forEachIndexed{i,n->val start=max(0,n*hop-RATE/8);val end=min(puffs.size,start+(RATE*.8).toInt())
            PrepareAudioAudition.wav(File(source,"cow-puff-${i+1}.wav"),puffs.copyOfRange(start,end))
            println("Cow puff ${i+1}: ${start.toDouble()/RATE}..${end.toDouble()/RATE}")}
        if(args.contains("--inspect")){
            source.walkTopDown().filter{it.isFile&&it.extension=="wav"&&!it.name.startsWith("._")}.forEach{file->
                val path=file.relativeTo(source).invariantSeparatorsPath
                val pcm=PrepareAudioAudition.render(c(path,s(path,seconds=120.0)))
                val regions=regions(pcm)
                println("$path: ${pcm.size.toDouble()/RATE}s; "+regions.take(12).mapIndexed{i,r->"${i+1}:${"%.2f".format(r.first.toDouble()/RATE)}–${"%.2f".format(r.last.toDouble()/RATE)}"})
            };return
        }
        val out=File("tools/audio-audition/listening-pack-16").apply{mkdirs()}
        val html=StringBuilder();val manifest=StringBuilder("Summon combat: 30 previews, NOT INSTALLED. No directional stereo changes.\n")
        val groups=groups()
        groups.forEach{g->
            val clips=g.choices.map{candidate->PrepareAudioAudition.render(candidate).also{pcm->
                val fade=min(1323,pcm.size/4);repeat(fade){i->pcm[i]*=i.toFloat()/fade;pcm[pcm.lastIndex-i]*=i.toFloat()/fade}
            }}
            val slot=ceil(clips.maxOf{it.size}.toDouble()/RATE+1).toInt()
            val combined=FloatArray(RATE*(1+slot*5))
            html.append("<section id='${g.id}'><h2>${g.id} · ${g.title}</h2><p>${g.note}</p><p>Toplu dinleme: 1 → 5, $slot saniyelik aralıklar.</p><audio controls preload='none' src='${g.id}-all.wav'></audio><div class='grid'>")
            g.choices.forEachIndexed{i,candidate->
                val id="${g.id}${i+1}";PrepareAudioAudition.wav(File(out,"$id.wav"),clips[i]);clips[i].copyInto(combined,RATE*(1+i*slot))
                val sources=candidate.layers.joinToString(" + "){"${it.pack}/${it.file} (speed=${it.speed}, take=${it.transient}, max=${it.maxSeconds}s, offset=${it.offset}s)"}
                manifest.append("$id: ${candidate.label}\n$sources\n")
                html.append("<article><label><input type='radio' name='${g.id}' value='$id'> <b>$id</b> · ${candidate.label}</label><audio controls preload='none' src='$id.wav'></audio><small>$sources</small></article>")
            }
            PrepareAudioAudition.wav(File(out,"${g.id}-all.wav"),combined);html.append("</div></section>")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File("tools/audio-audition/summon-combat-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Summon saldırı ve ölüm 16</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}nav{position:sticky;top:0;background:#17191c;padding:12px}</style>
<main><h1>Summon sesleri · Cow, Imp, Oathbreaker</h1><p>Her biri için 5 saldırı + 5 ölüm: toplam 30 aday. Hiçbiri oyuna eklenmedi. Mevcut spell seçim/Singularity sesleri korunuyor; yönlü stereo bu tur eklenmedi.</p>
<p>CA/CD Cow, IA/ID Imp, OA/OD Oathbreaker. Her gruptan bir ses seçebilirsin. Bunlar ses karakterini seçmek için taslaklar; gerçek saldırı/ölüm süresine uyarlama, oyun hızı ve yön bağlantıları seçimin ardından yapılacak.</p>
<p>Ücretsiz kaynak kayıtlarından kesitler ve tasarımlar: bazı adaylar vokal + tek darbe/ateş dokusu, bazıları tek kayıt. Beş adayın hepsi ayrı aktör değildir; kaynak ve işlem ayrıntıları her kartta bulunur. Cow için gerçek acı/ölüm kaydı kullanılmadı. İnsan vokalleri gerçek kayıtlar, TTS değil; yaratık/demon tınıları kaynak işlemleri ve perde değişimleri içerebilir.</p>
<p>Önce sesi düşük tut. Peak eşleme −12dBFS, algısal ses yüksekliği eşleme değil. <a href='LICENSE-SOURCES.txt'>Lisanslar/kaynaklar</a> · <a href='SECENEKLER.txt'>Aday dökümü</a></p>
<nav>${groups.joinToString(" · "){"<a href='#${it.id}'>${it.id}</a>"}}</nav>
$html
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='3'></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(out.listFiles()!!.count{it.extension=="wav"}==36)
        println("SUMMON_COMBAT_AUDITION_PASS: 30 candidates + 6 comparisons; no game asset or stereo changes.")
    }
    private fun regions(pcm:FloatArray):List<IntRange>{
        val threshold=pcm.maxOf{abs(it)}*.1f;val hop=RATE/100;val result=mutableListOf<IntRange>();var start=-1;var last=-1
        for(i in pcm.indices step hop){val active=(i until min(i+hop,pcm.size)).any{abs(pcm[it])>=threshold}
            if(active){if(start<0)start=i;last=min(i+hop,pcm.size)-1}
            if(start>=0&&i-last>RATE*.25){result+=start..last;start=-1}}
        if(start>=0)result+=start..last;return result
    }
}
