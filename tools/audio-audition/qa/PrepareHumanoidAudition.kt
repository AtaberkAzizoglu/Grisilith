package qa

import java.io.File
import kotlin.math.*
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import qa.AuditionPage.Group as G

object PrepareHumanoidAudition {
    private fun c(name:String,vararg layers:L)=C(name,layers.toList())
    private fun human(name:String,speed:Double=1.0,seconds:Double=1.6)=L("summon-voices","humans/yelling sounds/$name.wav",speed=speed,maxSeconds=seconds,transient=1)
    private fun woman(type:Int,n:Int,speed:Double=1.0)=L("human-casting","female/RPG Voice Starter Pack/Type $type/damaged$n.wav",speed=speed,maxSeconds=1.6)
    private fun air(n:Int,speed:Double=.8,seconds:Double=.65)=L("swishes","swishes/swish-$n.wav",speed=speed,maxSeconds=seconds)
    private fun impact(n:Int,speed:Double=.5)=L("impact-sounds","impactSoft_heavy_00$n.ogg",speed=speed,maxSeconds=.8)
    private fun wood(n:Int,speed:Double=.8)=L("rubberduck-rpg","wood_0$n.ogg",speed=speed,maxSeconds=.5)
    private fun rock(name:String,speed:Double=.65)=L("beast-traits","bfh1_$name.ogg",speed=speed,maxSeconds=.7)
    private fun slime(n:Int,speed:Double=.8,seconds:Double=.7)=L("rubberduck-rpg","creature_slime_0$n.ogg",speed=speed,maxSeconds=seconds)
    private fun creature(n:Int,speed:Double=.7,seconds:Double=1.2)=L("rubberduck-rpg","creature_monster_0$n.ogg",speed=speed,maxSeconds=seconds)
    private fun creak(speed:Double=.8)=L("tinysized","sfx-cc0/floor-creak-01.wav",speed=speed,maxSeconds=.8)
    internal fun groups()=listOf(
        G("GS","Gorilla Stomp · yeni ağır vuruşlar","Önceki kırıntılı/ince seçenekler yerine büyük bir kütlenin yere oturması: daha düşük frekanslı gövde, kısa ve ağır darbe. GS4 ayrıca 52Hz kısa rezonans içerir.",listOf(
            c("Küt, ağır toprak yumruğu",impact(3,.35).copy(lowPass=1200.0),rock("rock_hit_01",.55).copy(gain=.45,lowPass=1900.0)),
            c("Dev kaya zemine düşüyor",rock("rock_falling_05",.4).copy(lowPass=1600.0),impact(4,.4).copy(gain=.8)),
            c("Kalın, kapalı gövdeli çarpma",impact(0,.3).copy(lowPass=950.0),L("impact-sounds","impactWood_heavy_003.ogg",speed=.5,maxSeconds=.6,gain=.35,lowPass=1700.0)),
            c("En pes, kısa ve sert gövde",impact(2,.4).copy(lowPass=900.0),rock("rock_hit_01",.6).copy(gain=.35)),
            c("Ağır darbe, az ve boğuk zemin dokusu",impact(4,.32).copy(lowPass=1500.0),rock("rock_falling_02",.5).copy(offset=.03,gain=.25,lowPass=900.0))
        )),
        G("BD","Bandit · ölüm","Normal saldırı Longsword sesi olarak bağlı. Burada yalnız kısa insan ölüm/son nefes tasarımları var.",listOf(
            c("Kısa, doğal erkek haykırışı",human("yell1",1.0,1.2)),
            c("Boğuk, pes son nefes",human("2yell2",.85,1.4).copy(lowPass=2500.0)),
            c("Keskin, hızlı ölüm eforu",human("1yell3",1.1,1.0)),
            c("Daha derin, uzun düşüş",human("3yell3",.85,1.6)),
            c("Kuru, kısa insan kapanışı",human("yell5",.95,1.1))
        )),
        G("AD","Archer · ölüm","Normal saldırı Bow sesi olarak bağlı. Ability adayı yok; yalnız ölüm seçenekleri.",listOf(
            c("İnce ve kısa son çığlık",human("1yell1",1.1,1.1)),
            c("Nefesi kesilen okçu",human("2yell1",1.05,1.3)),
            c("Kısa, boğuk haykırış",human("yell3",.95,1.1).copy(lowPass=2800.0)),
            c("Daha tiz insan kapanışı",human("2yell4",1.15,1.2)),
            c("Yorgun, alçak son nefes",human("3yell1",1.0,1.4))
        )),
        G("GD","Guard summon · ölüm","Hatırlatma: Guard ölüm sesi şimdi bu Humanoid turunda seçilecek. Saldırısı mevcut Lance sesidir; başka Guard seçimi yok.",listOf(
            c("Kararlı, kısa asker haykırışı",human("3yell2",1.0,1.3)),
            c("Ağır, pes son nefes",human("1yell4",.85,1.4)),
            c("Doğal, tok asker ölümü",human("yell4",.95,1.4)),
            c("Kısa ve boğuk düşüş",human("2yell3",.85,1.1).copy(lowPass=2200.0)),
            c("Daha uzun, hırıltılı kapanış",human("3yell4",.9,1.6))
        )),
        G("SD","Spearman · ölüm","Normal saldırı mevcut Lance sesini paylaşır; boss kazancı oynanışta ayrıca uygulanır.",listOf(
            c("Derin, tok savaşçı ölümü",human("3yell1",.8,1.6)),
            c("Güçlü, kısa son haykırış",human("1yell5",.85,1.4)),
            c("Boğuk, nefesi tükenen savaşçı",human("3yell5",.9,1.5).copy(lowPass=2000.0)),
            c("Sert, pürüzlü insan kapanışı",human("2yell5",.9,1.4)),
            c("Daha doğal, uzun son nefes",human("yell9",.9,1.8))
        )),
        G("ST","Spearman · Throw the Spear","Mızrak fırlatma ability'si: kısa efor + uzun silahın havayı delmesi. 1,25s nişan + 0,75s atış hazırlığı var; ses seçilince fırlatma anına bağlanacak.",listOf(
            c("Keskin, temiz mızrak fırlatma",air(8,1.1,.45)),
            c("Uzun ve ağır mızrak savrulması",air(4,.7,.65)),
            c("Eforlu, metal gövdeli fırlatma",human("3grunt1",.95,.2).copy(gain=.4),air(12,.9,.55)),
            c("Kuru, hızlı hava delişi",air(8,1.35,.3).copy(highPass=350.0)),
            c("Daha tok, geniş hava yarma",air(4,.55,.65).copy(lowPass=2600.0))
        )),
        G("HD","Horseman · ölüm","Binicinin ölüm vokali; at kişnemesi/at ölümü eklenmedi. İstenirse daha sonra ayrıca değerlendirilir.",listOf(
            c("Kalın, güçlü binici haykırışı",human("3yell4",.75,1.8)),
            c("Tok, boğazdan son nefes",human("1yell2",.8,1.6)),
            c("Kısa ve sert binici ölümü",human("3yell2",.85,1.2)),
            c("Boğuk, uzun çöküş",human("yell8",.8,1.9).copy(lowPass=1900.0)),
            c("Doğal, pürüzlü kapanış",human("2yell4",.8,1.6))
        )),
        G("HA","Horseman · normal saldırı","Uzun saplı silahın tok savurma sesi. Seçilen normal saldırı sesi yalnız gerçek hasarda çalacak.",listOf(
            c("Uzun, ağır saplı savuruş",air(4,.65,.6)),
            c("Geniş ve boğuk hava yarma",air(12,.65,.6).copy(lowPass=2300.0)),
            c("Keskin uçlu uzun silah",air(8,.85,.5)),
            c("Kalın gövdeli, güçlü savurma",air(4,.5,.65).copy(lowPass=1800.0)),
            c("Kuru, kısa uzun-silah saldırısı",air(12,1.0,.4).copy(highPass=120.0))
        )),
        G("HS","Horseman · Swipe Around","Kodda eski Battle Cry kimliğini taşıyor ama gerçek ability çevresel sektör savuruşudur; bir bağırma/buff değildir. Geniş süpürüş/çoklu savuruş adayları.",listOf(
            c("Geniş, tek çevresel süpürme",air(4,.55,.8)),
            c("Çift yönlü ağır süpürüş",air(8,.75,.4),air(12,.7,.4).copy(offset=.25,gain=.8)),
            c("Boğuk, güçlü dairesel savurma",air(12,.45,.9).copy(lowPass=1900.0)),
            c("Metal dokulu ağır süpürüş",air(4,.7,.65),L("rubberduck-rpg","chain_01.ogg",speed=.75,maxSeconds=.4,gain=.2,offset=.1)),
            c("Keskin başlayan, geniş biten hamle",air(8,1.0,.3),air(4,.6,.65).copy(offset=.12,gain=.8))
        )),
        G("WD","Witch · ölüm","Normal isabette hasar tipine uygun mevcut projectile sesi; Meteor/Tsunami/Lightning/Cloud isabetinde kendi mevcut büyü sesi kullanılır. Burada sadece ölüm adayları: Cici Fyre'ın sözsüz hasar vokallerinden tasarımlar.",listOf(
            c("Doğal, kısa kadın çığlığı",woman(1,3,.9)),
            c("Daha kalın ve karanlık kadın sesi",woman(3,3,.7).copy(lowPass=2600.0)),
            c("Keskin, kısa cadı kapanışı",woman(2,2,1.0)),
            c("Boğuk, hırıltılı kadın sesi",woman(3,2,.8).copy(lowPass=1800.0)),
            c("Nefesi kesilen son haykırış",woman(1,2,.75))
        )),
        G("MX","Mimic · sandıktan canavara dönüşüm","D&D Mimic'in nesne taklidi, yapışkan beden ve dişli ısırık kimliğinden esinlenen özgün ses tasarımları. Kaynak oyunlardan ses alınmadı; mekanik değişiklik yok. Dönüşüm animasyonu 0,85s.",listOf(
            c("Sandık gıcırtısı, ıslak beden açılması",creak(),slime(1,.75,.6).copy(offset=.2,gain=.8)),
            c("Ahşap kabuğun altından hırlama",wood(3,.6),creature(2,.65,.75).copy(offset=.15,gain=.65)),
            c("Yapışkan ağız ve açılan sandık",slime(3,.7,.7),creak(.65).copy(offset=.08,gain=.55)),
            c("Kuru kapak, tok canavar nefesi",wood(1,.7),creature(1,.75,.7).copy(offset=.12,gain=.7)),
            c("Sandık çözülüyor, organik gövde çıkıyor",creak(.7),slime(4,.65,.8).copy(offset=.1,gain=.8),creature(3,.8,.5).copy(offset=.3,gain=.25))
        )),
        G("MA","Mimic · saldırı","Dişli sandık ısırığı / yapışkan uzantı hissi. Normal saldırı sesi yalnız hasar verildiğinde tetiklenecek.",listOf(
            c("Tok çene kapanışı ve ıslak ısırık",wood(2,1.1).copy(maxSeconds=.25),slime(1,1.1,.3).copy(gain=.55)),
            c("Kalın, yapışkan uzantı darbesi",slime(2,.7,.4),impact(2,.9).copy(maxSeconds=.25,gain=.45)),
            c("Kısa hırlama ve diş vurgusu",creature(1,1.0,.3),wood(4,1.2).copy(maxSeconds=.2,gain=.55)),
            c("Islak, sert canavar ısırığı",slime(4,1.0,.4),impact(0,1.1).copy(maxSeconds=.2,gain=.35)),
            c("Ahşap dişli kuru çene darbesi",wood(5,.85).copy(maxSeconds=.35),creature(4,1.15,.25).copy(gain=.4))
        )),
        G("MD","Mimic · ölüm","Organik çöküş + sandık gövdesi. Vokal/sümüksü ya da daha ahşap ağırlıklı seçenekler.",listOf(
            c("Boğuk canavar çöküşü ve ahşap",L("rubberduck-rpg","creature_die_01.ogg",speed=.7,maxSeconds=1.5),wood(3,.65).copy(offset=.35,gain=.35)),
            c("Yapışkan bedenin sönmesi",slime(3,.55,1.4),creature(2,.6,1.0).copy(offset=.12,gain=.45)),
            c("Kısa, dişli sandık kapanışı",creature(4,.65,.8),wood(2,.65).copy(offset=.25,gain=.7)),
            c("Ağır ve ıslak gövde çöküşü",slime(4,.5,1.2),impact(3,.65).copy(offset=.3,gain=.45)),
            c("Daha ahşap, kuru canavar ölümü",wood(5,.55),creak(.6).copy(offset=.1,gain=.6),creature(3,.7,.9).copy(gain=.55))
        ))
    )
    @JvmStatic fun main(args:Array<String>) {
        AuditionPage.write(19,"Humanoid + Mimic ve yeni ağır Stomp", "GS1–5 yeni Gorilla Stomp adaylarıdır (önceki seçkidekiler değil). Bandit/Archer/Spearman saldırıları yeniden seçilmiyor. Witch mevcut element/büyü seslerini paylaşır. Guard ölümünü bu turda seçiyoruz. Humanoid ölüm adayları HaelDB insan vokallerinden; Witch sözsüz kadın vokallerinden tasarımdır.",
            groups(),File("tools/audio-audition/humanoid-source-credits.txt")) {group,index,pcm->
            if(group.id!="GS" || index!=3)pcm else {
                val result=FloatArray(max(pcm.size,44100*3/5)){n->
                    val t=n/44100.0
                    val body=.16*sin(2*PI*52*t)*exp(-t*9)*min(1.0,t/.009)
                    (pcm.getOrElse(n){0f}+body).toFloat()
                };result
            }
        }
    }
}
