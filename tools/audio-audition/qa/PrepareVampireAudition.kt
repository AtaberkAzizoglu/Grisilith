package qa

import java.io.File
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import qa.AuditionPage.Group as G

/** Preview-only pack 21: never installs selected cues or changes the game. */
object PrepareVampireAudition {
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun p(file:String,speed:Double=1.0,seconds:Double=1.0)=L("vampire-preview",file,speed=speed,maxSeconds=seconds)
    private fun lick(name:String="lick",speed:Double=1.0)=p("$name.wav",speed,.9)
    private fun wet(n:Int,speed:Double=1.0,seconds:Double=.65)=p("wet/impsplat/impactsplat${n.toString().padStart(2,'0')}.mp3.flac",speed,seconds)
    private fun beast(n:Int,speed:Double=1.0,seconds:Double=2.0)=p("monster2/monster_sfx_pack_2/monster-$n.wav",speed,seconds).copy(transient=1)
    private fun human(name:String,speed:Double=1.0,seconds:Double=1.7)=L("summon-voices","humans/yelling sounds/$name.wav",speed=speed,maxSeconds=seconds,transient=1)
    private fun dark(n:Int=1,speed:Double=.9,seconds:Double=1.5)=L("dark-magic","dark_magic/fout-0$n.flac",speed=speed,maxSeconds=seconds,transient=1)
    private fun breath(speed:Double=1.0,seconds:Double=1.2)=L("undead-voices","ghostbreath.flac",speed=speed,maxSeconds=seconds)
    private fun air(speed:Double=1.0,seconds:Double=.7)=L("tinysized","sfx-cc0/compressed-air-spray-02.wav",speed=speed,maxSeconds=seconds,transient=1)
    private fun swish(n:Int=4,speed:Double=1.0,seconds:Double=.5)=L("swishes","swishes/swish-$n.wav",speed=speed,maxSeconds=seconds)
    private fun boom(speed:Double=1.0,seconds:Double=1.25)=L("spell-choices","explosions/explode.wav",speed=speed,maxSeconds=seconds)
    private fun mini(speed:Double=1.0,seconds:Double=.65)=L("spell-choices","explosions/explodemini.wav",speed=speed,maxSeconds=seconds)
    private fun rumble(speed:Double=1.0,seconds:Double=1.4)=p("rumble.flac",speed,seconds)
    private fun hit(n:Int,speed:Double=1.0)=L("impact-sounds","impactPunch_heavy_00$n.ogg",speed=speed,maxSeconds=.35)
    private fun rock(n:Int,speed:Double=1.0)=L("beast-traits","bfh1_rock_falling_0$n.ogg",speed=speed,maxSeconds=.8)
    private fun teleport(speed:Double=1.0,seconds:Double=.8)=L("focus-ability","teleport.wav",speed=speed,maxSeconds=seconds,transient=1)
    private fun voicePair(n:Int,high:Double,low:Double,seconds:Double=1.8)=arrayOf(beast(n,high,seconds),beast(n,low,seconds).copy(gain=.3,offset=.025,lowPass=2300.0))

    private fun groups()=listOf(
        G("GS","Gorilla · Stomp the Earth · tamamen yeni","10 yeni PAT/GÜM tasarımı: patlamalı sert başlangıç + kütle hissi veren düşük gövde. Eski GS adayları taşınmadı. Bazıları daha kuru darbe, bazıları daha büyük yer patlamasıdır; en başta bu grubu dinleyebilirsin.",listOf(
            c("Sert yer darbesi, kısa patlamalı gümbürtü",mini(.8,.85),hit(3,.85).copy(gain=.65)),
            c("Dev kütle yere çarpıyor, geniş bas gövdesi",boom(.85,1.4),rumble(.8,1.3).copy(gain=.6,lowPass=650.0)),
            c("Kuru PAT, ardından ağır GÜM",hit(0,.7),mini(.65,.95).copy(offset=.035,gain=.9)),
            c("Yere çakılan dev kaya, keskin başlangıç",boom(1.0,.95).copy(lowPass=3200.0),rock(4,.8).copy(gain=.3)),
            c("Daha kısa ve kaba, yumruk gibi patlayan darbe",mini(1.0,.6),hit(4,.8).copy(gain=.8)),
            c("Zemini ezen kalın, boğuk patlama",boom(.62,1.6).copy(lowPass=2000.0),hit(2,.65).copy(gain=.7)),
            c("Yer yarılması, tok basınç dalgası",rumble(.9,1.3),mini(.7,.85).copy(gain=.7),rock(2,.9).copy(gain=.15)),
            c("Sert ve kompakt çift-el yere vurma",hit(1,.75),mini(.9,.7).copy(gain=.9,offset=.015)),
            c("En büyük darbe, mağaramsı uzun gümbürtü",boom(.72,1.8),rumble(.65,1.8).copy(gain=.65)),
            c("Tok gövdeli ve keskin, kısa savaş darbesi",mini(.6,.8).copy(highPass=50.0),hit(3,1.0).copy(gain=.6),rock(5,1.2).copy(gain=.12))
        )),
        G("MA","Mimic · dil atma / yalama saldırısı","10 yeni ıslak dil adayı. MA1–2, referans verdiğin elastik ve tok dil şaplağı hissine yakın tasarımlar; LoL kaydı veya birebir kopyası değiller. Diğerleri daha doğal yalama/çekme tarzında.",listOf(
            c("Elastik uzun dil atışı, tok ıslak şaplak",swish(8,1.15,.16).copy(gain=.18),lick("lick",.75).copy(offset=.06),wet(3,.9,.4).copy(offset=.12,gain=.35)),
            c("Kalın, hızlı dil şaklaması ve geri çekiş",lick("slurp",.8),wet(6,1.05,.35).copy(offset=.08,gain=.45)),
            c("Temiz ve kısa çizgi-film yalaması",lick("comic-lick",1.0)),
            c("İnce, çabuk uzanan dil",lick("lick",1.15)),
            c("Büyük ağızdan ağır slurp",lick("slurp",.62)),
            c("Çok kısa, tek ıslak dil vuruşu",lick("fast-slurp",.85),wet(1,1.3,.25).copy(gain=.2)),
            c("Yapışkan dil, emerek geri çekilme",wet(5,.8,.5).copy(reverse=true,gain=.45),lick("lick",.9).copy(offset=.05)),
            c("Daha doğal, ağız kenarı yalama",lick("comic-lick",.82).copy(lowPass=4300.0)),
            c("Tok dil şaplağı, kısa emiş",wet(7,1.15,.3),lick("fast-slurp",.65).copy(gain=.7,offset=.07)),
            c("Islak uzun yalama, hafif gırtlaksı gövde",lick("slurp",.92),beast(4,1.2,.25).copy(offset=.05,gain=.12,lowPass=1800.0))
        )),
        G("MD","Mimic · yeni canavar ölüm çığlıkları","Tiz çığlığın altında kalın bir gövde: aynı vokalin iki perde katmanı bazı adaylarda birleştirildi. Önceki ahşap/çöküş MD sesleri yok; bu grup çığlık odaklıdır.",listOf(
            c("Keskin çığlık, pes gövdeli kapanış",*voicePair(1,1.15,.72)),
            c("Burundan tiz ve boğazdan kalın canavar",*voicePair(3,1.3,.78,2.0)),
            c("Hırıltılı, dalgalanan uzun çığlık",*voicePair(7,1.12,.62,2.2)),
            c("Kısa, yırtıcı ve tok ölüm",*voicePair(9,1.4,.85,1.3)),
            c("İri yaratık, tizleşerek kopan feryat",p("monster-new/Monster SFX.wav",1.1,2.0).copy(transient=1),p("monster-new/Monster SFX.wav",.7,2.0).copy(transient=1,gain=.28,lowPass=2400.0)),
            c("Daha insansı ama bozuk canavar feryadı",human("2yell8",1.2,1.7),human("2yell8",.74,1.7).copy(gain=.3)),
            c("Boğuk gövde ve ince çığlık birleşimi",*voicePair(12,1.22,.7,2.0)),
            c("İnce, çatallı canavar kapanışı",*voicePair(14,1.42,.8,1.5)),
            c("Uzun, kalın boğazlı tiz feryat",*voicePair(16,1.08,.6,2.4)),
            c("En saldırgan, kısa ve keskin ölüm",*voicePair(17,1.3,.68,1.7))
        )),
        G("VH","Vampire Hunger · vampirik can çalma","5 vampirik kan/can emiş tasarımı. Islak çekiş ile koyu büyü gövdesi birlikte; başka oyundan ses alınmadı. Seçimden sonra aktif emişe uyarlanacak, her hasar tick'inde yeniden başlatılmayacak.",listOf(
            c("Koyu kan emişi, kısa büyülü çekim",wet(2,.75,.8).copy(reverse=true),dark(1,.85,1.0).copy(gain=.5,lowPass=1600.0)),
            c("Nabızlı, derin yaşam enerjisi emme",dark(3,.65,1.4),lick("fast-slurp",.65).copy(gain=.3),breath(.8,1.2).copy(gain=.15)),
            c("Kanı çekip kapatan tok vampir büyüsü",lick("slurp",.65).copy(reverse=true),wet(8,.7,.5).copy(offset=.2,gain=.55),dark(2,1.1,.8).copy(gain=.3)),
            c("Daha kuru, lanetli ruh ve can çekimi",breath(.7,1.6).copy(reverse=true,highPass=100.0),dark(1,.6,1.4).copy(gain=.65)),
            c("Keskin kan çekişi, dolgun büyü kuyruğu",wet(4,.9,.7).copy(reverse=true),dark(3,.9,1.2).copy(gain=.55),lick("fast-slurp",.9).copy(offset=.1,gain=.3))
        )),
        G("VB","Vampire Lord · normal ısırık","Konuşma yok: diş batması, kısa ağız şaplağı ve az miktarda emiş. Gerçek hasarda çalacak; uzun sürekli kan emişi bu grupta değil.",listOf(
            c("Kuru diş, kısa ıslak ısırık",wet(1,1.25,.35),human("3grunt2",1.05,.16).copy(gain=.12)),
            c("Boğazdan gelen tok vampir ısırığı",wet(3,.85,.5),beast(2,.85,.25).copy(gain=.2)),
            c("Kısa diş şaplağı ve kan çekişi",wet(5,1.15,.3),lick("fast-slurp",1.0).copy(offset=.05,gain=.5)),
            c("Keskin, avcı gibi tek ısırık",wet(7,1.35,.3),air(1.5,.2).copy(gain=.12)),
            c("Kalın, daha vahşi vampir ısırığı",wet(8,.7,.5),beast(5,.75,.25).copy(gain=.15))
        )),
        G("ME","Misty Escape · toza dönüşüp kaçış","5 toz/sis çözülmesi + ışınlanma tasarımı. İlk ölümcül darbedeki kaçış içindir; gerçek ölüm vokali çalınmayacak.",listOf(
            c("Kuru toz savrulması ve kısa kayboluş",air(.8,.8),teleport(1.1,.6).copy(offset=.15,gain=.5)),
            c("Koyu sis, emilerek kaybolma",breath(.85,1.1).copy(reverse=true),dark(2,1.0,.8).copy(gain=.4)),
            c("Parçalanan toz, tok portal kapanışı",L("tinysized","sfx-cc0/cloth-pouch-shake-01.wav",speed=.8,maxSeconds=.8,transient=1,highPass=800.0),teleport(.8,.8).copy(offset=.12,gain=.65)),
            c("Hızlı kül bulutu ve kaçış fısıltısı",air(1.15,.55).copy(highPass=450.0),breath(1.0,.8).copy(gain=.3)),
            c("Daha ağır, karanlık vampir kayboluşu",dark(3,.8,1.1).copy(reverse=true),air(.65,.9).copy(gain=.4),teleport(.65,.7).copy(offset=.2,gain=.35))
        )),
        G("VF","İkinci forma dönüşüm","10 dönüşüm tasarımı: vampirik erkek vokali, büyüsel beden değişimi, koyu enerji veya tozdan yeniden oluşma. Oyun animasyonu 3,2s; seçimden sonra başlangıç/tepe/bitiş buna oturtulacak.",listOf(
            c("Erkek hırıltısıyla karanlık forma yükseliş",dark(1,.75,2.6).copy(reverse=true,gain=.65),human("3yell7",.8,2.4).copy(offset=.35)),
            c("Kan nabzı, ardından vampir feryadı",wet(4,.6,1.0).copy(gain=.4),human("1yell8",.8,2.5).copy(offset=.25),dark(3,.7,2.8).copy(gain=.45)),
            c("Tozdan yeniden beden kazanma",air(.65,1.8).copy(reverse=true),dark(2,.65,2.7),human("3grunt4",.75,.7).copy(offset=1.0,gain=.4)),
            c("Kemiksi kırılma ve erkek canavar dönüşümü",L("beast-traits","bfh1_wood_breaking_03.ogg",speed=.7,maxSeconds=.7,gain=.25),beast(11,.85,2.3).copy(offset=.2),dark(1,.85,2.0).copy(gain=.3)),
            c("Sözsüz, aristokratik karanlık güç açılması",human("2yell9",.9,2.6),dark(3,.8,2.5).copy(gain=.45),teleport(.7,.8).copy(offset=.8,gain=.25)),
            c("Derin ve vahşi vampir uyanışı",human("3yell11",.65,2.8),beast(15,.85,1.5).copy(offset=.25,gain=.25)),
            c("Büyüsel emilme, sonra kalın yeniden oluşma",dark(2,.55,2.6).copy(reverse=true),wet(6,.6,.8).copy(offset=.6,gain=.35),human("1yell7",.75,2.2).copy(offset=.35,gain=.7)),
            c("Yükselen erkek feryadı ve koyu enerji",human("2yell10",1.05,2.4),dark(1,.65,2.7).copy(gain=.55)),
            c("Daha sakin, tehditkâr vampirik dönüşüm",human("3grunt6",.65,1.5),breath(.6,2.5).copy(gain=.55),dark(3,.9,2.5).copy(gain=.4)),
            c("Büyük boss dönüşümü, dramatik vampir gövdesi",human("3yell13",.75,2.8),dark(2,.7,2.8).copy(gain=.6),rumble(.9,.7).copy(offset=.6,gain=.12))
        )),
        G("TP","İkinci form · ayrı teleport","Önceki olay listesindeki teleport için ayrıca 5 seçenek. Misty Escape'ten ayrı, kısa kaybolma/belirme; bazıları aynı sesin çıkış-giriş iki vurgusudur.",listOf(
            c("Kısa koyu portal açılışı",teleport(.95,.7)),
            c("Sisli ve kuru vampir sıçraması",air(1.3,.35),teleport(1.3,.5).copy(gain=.5)),
            c("Karanlık emilme ve tok belirme",dark(2,1.1,.5).copy(reverse=true),teleport(.8,.6).copy(offset=.22,gain=.6)),
            c("Daha ince, keskin büyülü kayboluş",teleport(1.5,.45).copy(highPass=200.0),breath(1.2,.6).copy(gain=.2)),
            c("Kalın ve çok kısa vampir teleportu",teleport(.65,.6).copy(lowPass=2800.0),wet(2,.8,.25).copy(gain=.12))
        )),
        G("LC","Konik kılıç · yalnız charge","5 hazırlık/gerilme sesi. İsabette mevcut Longsword kullanılacak; yeniden saldırı sesi seçmiyoruz. Hazırlık animasyonu 1s; aday seçiminden sonra süre buna uyarlanacak.",listOf(
            c("Kılıca çekilen karanlık hava",swish(8,.7,.7).copy(reverse=true),dark(1,1.25,.8).copy(gain=.25)),
            c("Diş sıkan erkek eforuyla kılıç hazırlığı",human("3grunt3",.85,.6),swish(4,.75,.8).copy(reverse=true,gain=.35)),
            c("Kısa, büyülü ve yükselen gerilim",dark(2,1.1,.9).copy(reverse=true)),
            c("Metalik gerilme, az vampir nefesi",L("rubberduck-rpg","chain_01.ogg",speed=.8,maxSeconds=.6,gain=.4).copy(reverse=true),breath(1.2,.9).copy(gain=.55)),
            c("Daha doğal, keskin silah geri çekişi",swish(12,.7,.8).copy(reverse=true),human("3grunt1",.9,.35).copy(gain=.25))
        )),
        G("SC","Swipe Around · yalnız charge","5 çevresel savuruş hazırlığı. Hasar anında mevcut Lance sesi kullanılacak. Bu eski Battle Cry kimliğini taşıyor ama buff/bağırma mekaniği değildir.",listOf(
            c("Ağır silahı genişçe geriye toplama",swish(4,.55,.9).copy(reverse=true)),
            c("Kısa erkek eforu, çevresel gerilim",human("3grunt5",.8,.6),swish(12,.65,.8).copy(reverse=true,gain=.4)),
            c("Koyu vampir nefesiyle hazırlık",breath(.85,1.0),dark(3,1.3,.7).copy(reverse=true,gain=.3)),
            c("Metal gövdeli ağır savuruş hazırlığı",L("rubberduck-rpg","chain_02.ogg",speed=.7,maxSeconds=.7).copy(reverse=true),swish(4,.8,.7).copy(reverse=true,gain=.5)),
            c("Keskin ve kısa dönüş hazırlığı",swish(8,.85,.6).copy(reverse=true),human("3grunt6",.85,.4).copy(gain=.25))
        )),
        G("VS","Vampir çığlığı · ölüm alternatifi A","5 vampir çığlığı: bazıları daha keskin, bazıları gövdeli. VD grubundaki daha insansı erkek ölüm seslerine alternatif; ikisini birlikte seçmek zorunda değilsin.",listOf(
            c("Keskin erkek vampir çığlığı",human("1yell9",1.05,2.0)),
            c("Boğazdan, karanlık vampir feryadı",human("3yell8",.85,2.3),breath(.9,1.5).copy(gain=.15)),
            c("Yırtıcı ve insan dışı vampir çığlığı",*voicePair(10,1.18,.7,2.2)),
            c("Tiz ama kalın gövdeli uzun vampir sesi",human("2yell11",1.1,2.4),human("2yell11",.72,2.4).copy(gain=.25)),
            c("Daha doğaüstü, lanetli son çığlık",human("3yell15",.9,2.5),dark(1,1.1,1.8).copy(gain=.18))
        )),
        G("VP","Erkek vampir · hasar alma","5 kısa tepki. Normal/unique enemy'lere eklenmeyecek; boss için tekrar aralığıyla kullanılır. Uzun ölüm çığlığı veya konuşma değil.",listOf(
            c("Kuru, kısa erkek acı eforu",human("3grunt3",.95,.4)),
            c("Kalın, diş arasından vampir hırıltısı",human("3grunt2",.7,.6)),
            c("Keskin ve nefesli darbe tepkisi",human("3grunt5",1.1,.4)),
            c("Daha karanlık, boğazdan kısa acı",human("3grunt4",.85,.5),beast(2,1.0,.3).copy(gain=.12)),
            c("Kontrollü, sert erkek tepki sesi",human("3grunt1",.82,.5))
        )),
        G("VD","Vampire Lord · son ölüm · alternatif B","10 farklı erkek vokalinden vampirik son ölüm tasarımları. VS kadar çığlık odaklı değil; son nefes, hırıltı, dramatik feryat ve çözülme çeşitleri. Bir seçim yapılacaksa VS veya VD yeterli olabilir.",listOf(
            c("Doğal erkek feryadı, karanlık kapanış",human("3yell2",.82,2.1),breath(.9,1.1).copy(offset=.45,gain=.18)),
            c("Derin, nefesi sönen vampir",human("1yell4",.7,2.5)),
            c("Keskin başlayıp çöken erkek ölümü",human("2yell6",.95,2.2)),
            c("Uzun, dramatik vampir son nefesi",human("3yell10",.85,2.8),dark(3,1.0,1.5).copy(offset=.3,gain=.12)),
            c("Daha boğuk, ağır erkek kapanışı",human("yell6",.78,2.5).copy(lowPass=3500.0)),
            c("İnsan sesi baskın, hafif vampirik pürüz",human("1yell12",.9,2.2),beast(8,.9,1.0).copy(gain=.12)),
            c("Vahşi ama insansı son haykırış",human("2yell7",.8,2.4)),
            c("Alçak, yorgun vampir çöküşü",human("3yell14",.7,2.8),breath(.7,1.5).copy(gain=.15)),
            c("Çatallı erkek ölüm sesi, ruhu çözülüyor",human("1yell15",.92,2.6),dark(2,1.1,1.5).copy(offset=.35,gain=.15)),
            c("Tok ve uzun boss vedası",human("3yell12",.75,2.9),human("3yell12",1.05,2.4).copy(gain=.12),breath(.8,1.6).copy(offset=.3,gain=.12))
        ))
    )

    private fun decodeMp3s() {
        val dir=File("tools/audio-audition/sources/vampire-preview/Audio")
        dir.listFiles()!!.filter{it.extension=="mp3"}.forEach{file->
            val rec=PrepareFemaleIncantationAudition.decode(file)
            val a=rec.samples;val threshold=a.maxOf{abs(it)}*.015f
            val first=max(0,a.indexOfFirst{abs(it)>threshold}-rec.rate/40)
            val last=min(a.size,a.indexOfLast{abs(it)>threshold}+rec.rate/20)
            val pcm=FloatArray(((last-first).toDouble()*44100/rec.rate).toInt()){i->
                val pos=first+i.toDouble()*rec.rate/44100;val p=pos.toInt().coerceAtMost(a.lastIndex)
                (a[p]*(1-(pos-p))+a[min(p+1,a.lastIndex)]*(pos-p)).toFloat()
            }
            PrepareAudioAudition.wav(File(dir,"${file.nameWithoutExtension}.wav"),pcm)
        }
    }
    @JvmStatic fun main(args:Array<String>) {
        decodeMp3s()
        val groups=groups();check(groups.size==13 && groups.sumOf{it.choices.size}==90)
        AuditionPage.write(21,"Ağır Stomp · Mimic · Vampire Lord", "YALNIZ ÖNİZLEME. Seçki 20 kararların not alındı, henüz kurulmadı. Bu sayfada 90 yeni aday var. GS/MA/MD ve VF/VD grupları 10'ar, diğerleri 5'er. Misty Escape ve ikinci form teleport ayrı gruplar. LC/SC yalnız hazırlık sesidir; vuruşlar mevcut Longsword/Lance olacak. VS ve VD ölüm için iki alternatif aile; ikisini birlikte seçmek zorunda değilsin.",groups,
            File("tools/audio-audition/vampire-source-credits.txt")){g,i,pcm->
            when {
                g.id=="GS" -> {
                    val rate=44100;val bodySeconds=if(i in setOf(1,5,8))1.1 else .65
                    val size=max(pcm.size,(rate*bodySeconds).toInt())
                    FloatArray(size){n->
                        val t=n.toDouble()/rate
                        val f0=120.0+i*3;val f1=43.0+i*1.8;val k=9.0
                        val phase=2*PI*(f1*t+(f0-f1)*(1-exp(-k*t))/k)
                        val attack=min(1.0,t/.0025)
                        val body=(.16*sin(phase)+.055*sin(phase*2.01))*exp(-t*7)*attack
                        (pcm.getOrElse(n){0f}*exp(-t*(if(i==8).65 else 1.5))+body).toFloat()
                    }
                }
                g.id in setOf("MD","VS","VD","VF") -> {
                    val delay=(44100*.085).toInt();val amount=if(g.id=="VF").17f else .11f
                    FloatArray(pcm.size+delay*2){n->pcm.getOrElse(n){0f}+
                        (if(n>=delay)pcm.getOrElse(n-delay){0f}*amount else 0f)+
                        (if(n>=delay*2)pcm.getOrElse(n-delay*2){0f}*amount*.5f else 0f)}
                }
                else -> pcm
            }
        }
    }
}
