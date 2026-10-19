package qa

import java.io.File
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import qa.AuditionPage.Group as G

/** Pack 20 is preview-only. No game asset or setting is modified here. */
object PrepareUndeadAudition {
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun voice(file:String,speed:Double=1.0,seconds:Double=1.8,start:Double=0.0)=
        L("undead-voices",file,speed=speed,maxSeconds=seconds,startSeconds=start)
    private fun zombie(n:Int,speed:Double=1.0,seconds:Double=1.0)=
        voice("zombies/zombies/zombie-$n.wav",speed,seconds).copy(transient=1)
    private fun moan(n:Int,speed:Double=1.0,seconds:Double=2.0)=
        voice("ghostmoans/qubodup-GhostMoans/wav/qubodup-GhostMoan${n.toString().padStart(2,'0')}.wav",speed,seconds).copy(transient=1)
    private fun foley(name:String,speed:Double=1.0,seconds:Double=.8)=
        L("beast-traits","bfh1_$name.ogg",speed=speed,maxSeconds=seconds)
    private fun slime(n:Int,speed:Double=.8,seconds:Double=1.0)=
        L("rubberduck-rpg","creature_slime_0$n.ogg",speed=speed,maxSeconds=seconds)
    private fun acid(speed:Double=1.0,seconds:Double=.7,start:Double=0.0)=
        L("focus-ability","acid_bubble.wav",speed=speed,maxSeconds=seconds,startSeconds=start)
    private fun air(n:Int,speed:Double=1.0)=L("tinysized","sfx-cc0/compressed-air-spray-0$n.wav",speed=speed,maxSeconds=.6,transient=1)

    private fun groups()=listOf(
        G("WD","Witch · yeni ölüm çığlıkları","Yaşlı/cadı hissi için çatallı, pürüzlü ve keskin kadın vokali tasarımları. Hafif rahatsız edici olabilir: önce düşük sesle dinle. Eski WD seçenekleri kullanılmıyor.",listOf(
            c("Keskin, kuru cadı çığlığı",voice("witch.wav",.95,1.2)),
            c("Boğazdan gelen kadın çığlığı",voice("female_scream.ogg",.82,2.1).copy(transient=1)),
            c("Tiz ve çatallı cadı feryadı",voice("witch.wav",1.15,1.0).copy(highPass=180.0)),
            c("Daha yaşlı, pürüzlü ve alçalan feryat",voice("female_scream.ogg",.67,2.4).copy(transient=1,lowPass=4800.0)),
            c("Kısa, kırılgan ve hırçın kapanış",L("human-casting","female/RPG Voice Starter Pack/Type 3/damaged3.wav",speed=1.08,maxSeconds=1.4,highPass=250.0))
        )),
        G("SK","İki Skeleton için ortak ölüm","10 farklı yaklaşım: kuru kemik kırılması, iskeletin yere dağılması, içi boş kafatası ve hafif lanetli çözülme. Ahşap/taş/metal foley kemiksi bir dokuya dönüştürüldü. İkisine aynı seçilen ses atanacak.",listOf(
            c("Kuru ve kısa kemik kırılması",foley("wood_breaking_01",1.35,.65)),
            c("Küçük kemiklerin yere saçılması",foley("rock_falling_01",1.45,1.0).copy(highPass=400.0)),
            c("Ağır iskeletin bütün hâlde çökmesi",foley("wood_falling_02",.85,1.2),foley("rock_falling_02",1.25,.7).copy(offset=.12,gain=.45)),
            c("İçi boş, tok kafatası çatlağı",foley("wood_breaking_03",.8,.7),L("rubberduck-rpg","wood_02.ogg",speed=.8,maxSeconds=.4,gain=.5)),
            c("İnce kemik çıtırtısı ve son tıkırtı",foley("wood_breaking_04",1.65,.6),foley("rock_falling_03",1.7,.6).copy(offset=.18,gain=.45)),
            c("Zırhlı iskeletin kemik-zincir çöküşü",foley("wood_falling_01",1.25,1.0),L("rubberduck-rpg","chain_01.ogg",speed=.9,maxSeconds=.75,gain=.35)),
            c("Gevrek kemikler, çok kısa kapanış",foley("wood_breaking_02",1.8,.45).copy(highPass=350.0)),
            c("Kemiksi bir yığının genişçe dağılması",foley("rock_falling_05",1.6,1.3).copy(highPass=300.0),foley("wood_breaking_01",1.1,.5).copy(gain=.35)),
            c("Hafif lanetli iskelet çözülmesi",foley("wood_breaking_03",1.3,.6),voice("ghostbreath.flac",1.8,.8).copy(gain=.18,highPass=450.0)),
            c("Ağır çatlak, ardından küçük kemikler",foley("wood_breaking_04",.65,.8),foley("rock_falling_04",1.6,.8).copy(offset=.25,gain=.6))
        )),
        G("ZA","Zombie · saldırı","Kısa hırıltı/ısırık eforu; yalnız gerçekten hasar verdiğinde çalacak. Sürekli yürüyüş homurtusu eklenmiyor.",listOf(
            c("Kuru, kısa boğaz hırıltısı",zombie(2,1.0,.65)),
            c("Hızlı, öfkeli zombi ısırığı",voice("zombienoises/fastzombie1.ogg",1.1,.7).copy(transient=1)),
            c("Kalın, ağır zombi eforu",zombie(7,.72,.85)),
            c("Islak, boğuk saldırı sesi",voice("zombienoises/zombienoise2.ogg",.9,.8).copy(transient=1)),
            c("Daha keskin, aç zombi hırlaması",zombie(15,1.08,.7))
        )),
        G("ZD","Zombie · çığlıklı ölüm","Zombi vokallerinin daha uzun, çöken ve çığlığa yaklaşan kesitleri; insan konuşması yok.",listOf(
            c("Uzun ve pürüzlü zombi feryadı",voice("zombies/zombies/zombie-16.wav",.85,2.4)),
            c("Keskin, hızlı çığlıklı ölüm",voice("zombienoises/fastzombie1.ogg",.82,2.1).copy(transient=1)),
            c("Pesleşen, boğazdan ölüm hırıltısı",zombie(18,.72,2.3)),
            c("Açık ağızlı, bozuk zombi haykırışı",voice("zombies/zombies/zombie-17.wav",.95,2.2)),
            c("Kaba, nefesi tükenen kapanış",voice("zombienoises/zombienoise3.ogg",.8,2.4).copy(transient=1))
        )),
        G("GA","Ghost · fısıltılı saldırı","10 seçenek: hayalet fısıltısı, anlamsız sözler ve uzak ses dokusu. Bazı kaynaklar zaten çok katmanlıdır; bazı fısıltılarda anlaşılabilir kısa parçalar olabilir. İsabet başına kısa kesitler.",listOf(
            c("Ters/anlamsız, kuru fısıltı",voice("whispers_litruv.wav",1.0,.8,.5)),
            c("Boğuk, derinden fısıldama",voice("whispers_litruv.wav",.72,1.1,5.0).copy(lowPass=2500.0)),
            c("Birden çok uzaktan fısıltı",voice("whispers_ienba.wav",1.0,1.0,2.0)),
            c("Kadınsı, karanlık fısıltı",voice("whispers_bluesiren.wav",.9,1.1,.8)),
            c("Hafif tiz, kısa hayalet sözü",voice("whispers_litruv.wav",1.2,.65,10.0)),
            c("Daha yakın ve nefesli fısıltı",voice("whispers_ienba.wav",.9,.9,7.0).copy(highPass=180.0)),
            c("Pes, sürünen hayalet sesi",voice("whispers_bluesiren.wav",.65,1.3,4.0)),
            c("Kesik kesik, uğursuz fısıltı",voice("whispers_litruv.wav",.85,.9,16.0)),
            c("Uzak, geniş yankılı mırıldanma",voice("whispers_ienba.wav",.8,1.1,15.0)),
            c("İnce ve rahatsız edici son hece",voice("whispers_bluesiren.wav",1.15,.75,7.0).copy(highPass=350.0))
        )),
        G("GD","Ghost · çığlıklı ölüm","10 seçenek: insanımsı feryattan derin, uzayan hayalet çözülmesine. Kadın çığlığı ve işlenmiş hayalet vokali aileleri ayrı ayrı dinlenebilir.",listOf(
            c("Uzaklaşan kadın hayalet çığlığı",voice("female_scream.ogg",1.0,2.0).copy(transient=1)),
            c("Derin ve bozuk ruh feryadı",moan(1,.65,2.4)),
            c("Tiz, ani ruh kopuşu",voice("witch.wav",1.35,.8)),
            c("İnce, yükselen hayalet haykırışı",moan(2,1.3,1.7)),
            c("Kalın, uzun ve karanlık hayalet ölümü",moan(3,.62,2.8)),
            c("Nefesli kadın feryadı, uzun kuyruk",voice("female_scream.ogg",.86,2.7).copy(transient=1,highPass=450.0)),
            c("Kısa, canavarımsı ruh çığlığı",moan(4,.9,1.5)),
            c("Yırtıcı, yankılı hayalet çığlığı",voice("witch.wav",.75,1.5)),
            c("Çok pes ve içten gelen ruh çözülmesi",moan(5,.58,3.0)),
            c("Uzun, titreşimli hayalet feryadı",moan(2,.77,2.6).copy(highPass=180.0))
        )),
        G("GC","Ghost · ability ağlaması","10 kısa/uzun ağlama adayı. Gerçek ability: 1,25s hazırlık ardından 5s Necrotic Storm; seçimden sonra ses bu evrelere bağlanacak, her hasar tick'inde yeniden başlamayacak.",listOf(
            c("Kısa, uzak kadın hıçkırığı",voice("cry_amaiguri.wav",1.0,1.8,.8)),
            c("Uzun ve ince hayalet ağlaması",voice("cry_amaiguri.wav",1.15,5.0,3.0)),
            c("Alçak, korkutucu kadın ağıdı",voice("cry_purplereptar.wav",.7,4.5,1.0)),
            c("Kısa ve nefesli hayalet sızlanması",voice("cry_purplereptar.wav",1.0,1.8,7.0)),
            c("Uzayan, yankılı kadın ağlaması",voice("cry_amaiguri.wav",.82,5.0,6.0)),
            c("İnsanlıktan uzak, pes ruh ağıdı",moan(1,.55,4.0)),
            c("Tiz, kırılgan ve kesik hayalet ağlaması",voice("cry_purplereptar.wav",1.2,3.0,11.0)),
            c("Çok derin, yavaş hayalet iniltisi",moan(3,.48,5.0)),
            c("Daha doğal, uzun nefesli ağlama",voice("cry_purplereptar.wav",.9,5.0,3.0)),
            c("Kısa, gölgeli ruh hıçkırığı",voice("cry_amaiguri.wav",.68,2.0,11.0))
        )),
        G("LD","Slime · ölüm","Islak sönme, baloncuk patlaması ve sümüksü çöküş; konuşma veya insansı çığlık yok.",listOf(
            c("Kısa, ıslak baloncuk ölümü",slime(1,1.0,.8)),
            c("Kalın jöle gövdesinin çöküşü",slime(3,.6,1.3)),
            c("Patlayan, asitli küçük gövde",slime(4,1.15,.8),acid(1.2,.5).copy(gain=.35)),
            c("Yavaşça çözülen slime",slime(2,.55,1.5)),
            c("Tok patlama ve sıvı dağılması",slime(4,.7,1.0),L("tinysized","sfx-cc0/water-drop-02.wav",speed=.6,maxSeconds=.5,gain=.3,transient=1))
        )),
        G("LP","Slime · Acidic Path temas sesi","Asit izinin hasar verdiği temas için kısa cızırtı/baloncuk. Adım veya her karede ses değil; gerçek condition hasarında sınırlandırılacak. Ranged Acid projectile sesi ayrıca mevcut ortak ses.",listOf(
            c("Kısa, ince asit cızırtısı",acid(1.35,.55)),
            c("Kabarcıklı, tok asit teması",acid(.75,.8,.2)),
            c("Daha kuru ve keskin yanma",acid(1.05,.65).copy(highPass=800.0),air(1,1.5).copy(gain=.2,maxSeconds=.3)),
            c("Yavaş ve ıslak asit kabarcığı",slime(2,1.3,.4).copy(gain=.4),acid(.65,.8)),
            c("Hızlı püsküren asit yakması",air(2,1.1).copy(maxSeconds=.4),acid(1.6,.5).copy(gain=.55))
        )),
        G("LS","Ek · Slime ölümünde parçalanma","Kodda ayrıca ölüm sırasında parçacık saçılması var. İstenirse kısa ortak bir split sesi; ölüm sesiyle üst üste çok dolu gelirse ayrıca ses koymayabiliriz.",listOf(
            c("İnce, hızlı parçacık pıtlaması",slime(1,1.45,.45)),
            c("Islak, geniş parçalanma",slime(4,.9,.7)),
            c("Küçük asit baloncuklarının saçılması",acid(1.8,.55,.3)),
            c("Kısa ve tok jöle ayrılması",slime(3,1.25,.5)),
            c("Sıvı parçaların hafif püskürmesi",slime(2,1.55,.45),air(1,1.6).copy(maxSeconds=.25,gain=.15))
        ))
    )

    private fun decodePreviews() {
        val root=File("tools/audio-audition/sources/undead-voices/Audio")
        root.listFiles()!!.filter{it.extension=="mp3"}.forEach{file->
            val rec=PrepareFemaleIncantationAudition.decode(file)
            val samples=rec.samples;val threshold=samples.maxOf{abs(it)}*.015f
            val first=max(0,(samples.indexOfFirst{abs(it)>threshold}).coerceAtLeast(0)-rec.rate/20)
            val last=min(samples.size,(samples.indexOfLast{abs(it)>threshold}).coerceAtLeast(first)+rec.rate/8)
            val pcm=FloatArray(((last-first).toDouble()*44100/rec.rate).toInt()){i->
                val pos=first+i.toDouble()*rec.rate/44100;val p=pos.toInt().coerceAtMost(samples.lastIndex)
                (samples[p]*(1-(pos-p))+samples[min(p+1,samples.lastIndex)]*(pos-p)).toFloat()
            }
            PrepareAudioAudition.wav(File(root,"${file.nameWithoutExtension}.wav"),pcm)
            println("SOURCE ${file.name}: ${"%.2f".format(pcm.size/44100.0)} seconds after edge trim")
        }
    }
    @JvmStatic fun main(args:Array<String>) {
        decodePreviews()
        val carried=PrepareHumanoidAudition.groups().filter{it.id in setOf("GS","MA","MD")}.map{g->
            g.copy(title="Bekleyen · ${g.title}",note="Seçki 19'dan aynen taşındı; henüz seçilmedi/kurulmadı. "+g.note)
        }
        AuditionPage.write(20,"Witch + Undead", "WD yeni Witch; SK iki iskeletin ortak ölümü; ZA/ZD Zombie; GA/GD/GC Ghost; LD/LP Slime; LS isteğe bağlı split. GS/MA/MD önceki turdan bekleyen seçeneklerdir. Skeleton saldırıları Longsword/Bow, Slime menzilli saldırısı mevcut element sesi olarak bağlandı. Vampire Lord için bu sayfada ses yok.",
            groups()+carried,File("tools/audio-audition/undead-source-credits.txt")){g,i,pcm->
            when {
                g.id=="GS" && i==3 -> FloatArray(max(pcm.size,44100*3/5)){n->
                    val t=n/44100.0;(pcm.getOrElse(n){0f}+.16*sin(2*PI*52*t)*exp(-t*9)*min(1.0,t/.009)).toFloat()}
                g.id=="WD" -> FloatArray(pcm.size){n->(pcm[n]*(.85+.15*sin(2*PI*(21+i*3)*n/44100.0))).toFloat()}
                g.id in setOf("GA","GD","GC") -> {
                    val delay=(44100*(if(g.id=="GA").085 else .17)).toInt()
                    val decay=if(g.id=="GA").2f else .28f
                    FloatArray(pcm.size+delay*3){n->
                        var v=pcm.getOrElse(n){0f}
                        for(k in 1..3)if(n>=delay*k)v+=pcm.getOrElse(n-delay*k){0f}*decay.pow(k)
                        v
                    }
                }
                else -> pcm
            }
        }
    }
}
