package qa

import java.io.File
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.azizoglu.grisilith.audio.EnemyAudio
import qa.PrepareAudioAudition.Layer as L
import qa.PrepareAudioAudition.Candidate as C
import qa.AuditionPage.Group as G
import qa.AuditionPage.Reference as R

/** Pack 22 is selection material only; installed game audio is never overwritten. */
object PrepareFinishingAudioAudition {
    private fun c(label:String,vararg layers:L)=C(label,layers.toList())
    private fun current(name:String,speed:Double=1.0,seconds:Double=.45,start:Double=0.0)=
        L("finishing-preview","$name.wav",speed=speed,maxSeconds=seconds,startSeconds=start)
    private fun animal(file:String,speed:Double=1.0,seconds:Double=.45)=
        L("beast-voices",file,speed=speed,maxSeconds=seconds)
    private fun human(name:String,speed:Double=1.0,seconds:Double=.4)=
        L("summon-voices","humans/yelling sounds/$name.wav",speed=speed,maxSeconds=seconds,transient=1)
    private fun rock(name:String,speed:Double=1.0,seconds:Double=.3)=
        L("beast-traits","bfh1_$name.ogg",speed=speed,maxSeconds=seconds)
    private fun wood(n:Int,speed:Double=1.0)=L("rubberduck-rpg","wood_0$n.ogg",speed=speed,maxSeconds=.22)
    private fun metal(name:String,speed:Double=1.0,seconds:Double=.3)=L("rpg-audio",name,speed=speed,maxSeconds=seconds)
    private fun group(id:String,title:String,note:String,vararg choices:C)=G(id,title,note,choices.toList())
    private fun groups()=listOf(
        group("SLS","Sling · gelişmiş taş parçalanması",
            "Önce mevcut normal saldırı ve ability inişini dinle. Yeni ses ana taş bir kez parçalanınca tek defa çalacak; 3–8 parçanın her biri için tekrarlanmayacak. Daha küçük, kuru taş dokusu; ikinci bir büyük ability patlaması değil.",
            c("Kuru ve kısa taş çatlaması",rock("rock_breaking_01",1.5,.3),current("sling_impact",1.25,.12).copy(gain=.16)),
            c("Küçük taşların hafifçe saçılması",rock("rock_falling_02",1.7,.34).copy(highPass=450.0)),
            c("Gevrek, çok kısa taş kırıntısı",rock("rock_breaking_02",1.8,.23).copy(highPass=250.0),rock("rock_falling_03",2.0,.16).copy(offset=.05,gain=.25)),
            c("Biraz daha tok taş çatlağı",current("sling_impact",1.35,.16).copy(gain=.45),rock("rock_breaking_01",1.1,.32)),
            c("Hızlı, dağınık ve ince taş parçacıkları",rock("rock_falling_04",1.9,.3).copy(highPass=550.0),rock("rock_breaking_02",1.4,.14).copy(gain=.35))
        ),
        group("BRC","Boomerang · son geri yakalama",
            "Normal ve ability aynı mevcut fırlatma dosyasını paylaşır; ikisi de aşağıda. Yeni ses küçük ve yakın bir ahşap/el yakalama vurgusu. Gelişmiş bumerang arkaya devam edecekse ara dönüşte değil, gerçekten bittiği son yakalamada kullanacağız.",
            c("Yumuşak ahşap tık ve kısa hava kapanışı",wood(1,1.2),current("boomerang_throw",1.25,.12,.1).copy(gain=.22)),
            c("Kuru, sade elde yakalama",wood(2,1.35).copy(lowPass=3200.0,maxSeconds=.16)),
            c("Daha tok ama hafif tutuş",wood(4,.95).copy(lowPass=2200.0,maxSeconds=.2),current("boomerang_throw",1.0,.1,.25).copy(gain=.15)),
            c("Hızlı, ince ahşap kapanışı",wood(5,1.65).copy(maxSeconds=.14),current("boomerang_throw",1.4,.1,.08).copy(gain=.18)),
            c("Yumuşak bez/el teması, az ahşap",L("tinysized","sfx-cc0/cloth-pouch-shake-02.wav",speed=1.4,maxSeconds=.18,transient=1,lowPass=1900.0),wood(3,1.3).copy(gain=.3,maxSeconds=.12))
        ),
        group("SR","Shield · maksimum Defensive Line birikimi",
            "Mevcut metalik normal saldırı ve Defensive Burst aşağıda. Yeni ses yalnız maksimum birikime ilk ulaşıldığında bir kez; her Armor Class puanında veya maksimumda beklenen her karede değil. Kısa metal kilit/hazır olma işareti.",
            c("Kalkan mekanizması yerine oturuyor",metal("metalLatch",1.15,.26),current("shield_strike",1.3,.1).copy(gain=.18)),
            c("Kısa, temiz metalik hazır tınısı",metal("metalPot1",1.5,.38).copy(highPass=500.0)),
            c("Çift, hafif kilit tıkırtısı",metal("metalClick",1.1,.11),metal("metalClick",1.35,.11).copy(offset=.075,gain=.65)),
            c("Tok kilit, çok hafif metal çınlaması",metal("metalLatch",.9,.23),metal("metalPot3",1.4,.3).copy(offset=.08,gain=.2)),
            c("Kendi kalkan sesinden ince hazır vurgusu",current("shield_strike",1.5,.18).copy(highPass=600.0),metal("metalClick",1.25,.11).copy(gain=.35))
        ),
        group("BRH","Bear · hasar alma",
            "Ölüm sesi mevcut ve değişmiyor. Yeni adaylar kısa homurtu/nefes kırılması; uzun öfke kükremesi değil. Normal ayı çağrılarından tasarımdır, gerçek hayvan acısı kaydı değildir.",
            c("Kısa, tok ayı homurtusu",animal("bear-call-1.wav",1.15,.4)),
            c("Boğuk, nefesi kesilen ayı",animal("bear-call-2.wav",.95,.48).copy(lowPass=2400.0)),
            c("Daha keskin, hızlı tepki",current("bear_attack",1.25,.32).copy(highPass=120.0)),
            c("Pes, küçük bir göğüs homurtusu",animal("bear-call-1.wav",.8,.5).copy(lowPass=1800.0)),
            c("Kuru, kısa ve hırıltılı tepki",animal("bear-call-2.wav",1.3,.35))
        ),
        group("BSH","Bee Swarm · hasar tepkisi",
            "Arı çığlığı değil: sürü vızıltısında kısa kırılma/tökezleme. Ölüm ve Swarm Being sesinden kısa olmalı. Seçimden sonra direnç tepkisiyle aynı hit'te üst üste binmesini engelleyeceğiz.",
            c("Kısa vızıltı kırılması",animal("bee-call.wav",1.2,.25)),
            c("Daha tok, kısa sürü sarsılması",animal("bee-call.wav",.85,.3).copy(startSeconds=.18,lowPass=2600.0)),
            c("İnce, anlık kanat telaşı",animal("bee-call.wav",1.45,.2).copy(startSeconds=.35,highPass=300.0)),
            c("Boğuk ve çok kısa sürü darbesi",current("swarm_attack",.95,.22).copy(lowPass=1700.0)),
            c("Dalgalanan kısa vızıltı tepkisi",animal("bee-call.wav",1.05,.32).copy(startSeconds=.6))
        ),
        group("CRH","Crocodile · hasar alma",
            "Ölüm sesindeki sürüngen dokusuna yakın, daha kısa tepki. Alligator kaydından Crocodile tasarımları; uzun sıçrama hazırlığına veya ölüm çığlığına dönüşmeyecek.",
            c("Kısa, boğazdan sürüngen homurtusu",animal("croc-call-1.wav",1.1,.4)),
            c("Kuru ve keskin tıslama",animal("croc-call-3.wav",1.2,.32).copy(highPass=450.0)),
            c("Tok, kısa sürüngen hırıltısı",animal("croc-call-4.wav",.9,.5).copy(lowPass=2400.0)),
            c("Hızlı ve çatallı darbe tepkisi",animal("croc-call-2.wav",1.35,.3)),
            c("Daha pes, kontrollü çene homurtusu",current("crocodile_attack",.85,.45).copy(lowPass=2100.0))
        ),
        group("GRH","Gorilla · hasar alma",
            "Stomp/Throw hazırlığındaki büyük kükremelerden ayrı kısa goril eforu. Ölüm sesi karşılaştırma için eklendi; saldırı/savunma sırasında sürekli bağırma olmayacak.",
            c("Kısa, tok goril homurtusu",animal("gorilla/ogg/gorilla_grunt.ogg",1.05,.4)),
            c("Sert ve kuru diş gösterme",animal("gorilla/ogg/gorilla_snarl_01.ogg",1.15,.35)),
            c("Kalın, boğuk goril tepkisi",animal("gorilla/ogg/gorilla_snarl_02.ogg",.8,.5).copy(lowPass=2300.0)),
            c("Keskin, çabuk nefes kırılması",animal("gorilla/ogg/gorilla_snarl_03.ogg",1.2,.3)),
            c("Daha yırtıcı, kısa hırlama",animal("gorilla/ogg/gorilla_snarl_04.ogg",.95,.48))
        ),
        group("SPH","Spearman · hasar alma",
            "Mevcut erkek savaşçı ölüm sesine yakın insan vokal ailesi. Öncelik kısa acı/efor; tek hit'te uzun ölüm haykırışı çalmayacak.",
            c("Kısa, derin savaşçı eforu",human("3grunt1",.88,.36)),
            c("Diş sıkan, tok asker tepkisi",human("3grunt2",.92,.38)),
            c("Nefesi kesilen kısa homurtu",human("3grunt3",1.0,.3)),
            c("Daha sert ve pürüzlü darbe tepkisi",human("3grunt4",.85,.42)),
            c("Sert başlayan, hızlı sönen tepki",human("3grunt5",1.1,.32))
        ),
        group("HOH","Horseman · hasar alma",
            "At kişnemesi değil, binicinin kısa erkek vokali. Ölümde seçilen HaelDB 2yell ailesinden ve yakın efor kayıtlarından kısa tasarımlar.",
            c("Ölüm sesine yakın kısa binici tepkisi",human("2yell4",.95,.3)),
            c("Kuru, nefesli asker acısı",human("2yell1",1.05,.27)),
            c("Kalın, kısa binici homurtusu",human("3grunt6",.8,.45)),
            c("Keskin ve hızlı erkek tepki sesi",human("2yell3",1.1,.25)),
            c("Daha tok, düşük tonlu darbe tepkisi",human("2yell2",.82,.4).copy(lowPass=2800.0))
        ),
        group("WIH","Witch · hasar alma",
            "Aşağıdaki mevcut xl57 ölüm çığlığından kısa kesitler. Aynı ses karakterini koruyoruz; ölümdeki 3,4 saniyelik çığlığın tamamı her hit'te çalmayacak. Yine de tiz olabilir, önce kısık sesle dinle.",
            c("Kısa, keskin cadı tepkisi",current("witch-hurt-1",1.05,.28)),
            c("Biraz daha kalın, boğazdan tepki",current("witch-hurt-2",.85,.42).copy(lowPass=4800.0)),
            c("İnce, çok kısa irkilme",current("witch-hurt-3",1.2,.22)),
            c("Çatallı, alçalan kadın feryadı",current("witch-hurt-4",.95,.38)),
            c("Daha boğuk ve kontrollü cadı acısı",current("witch-hurt-5",.75,.45).copy(lowPass=3000.0))
        ),
        group("GHH","Ghost · hasar alma",
            "Normal saldırı fısıltısı / ölüm çığlığı kimliğine yakın, çok kısa ruh tepkisi. Uzun ability ağlaması tekrar edilmez; bağışıklık/kaçınma ile sıfır hasar varsa sessiz olacak.",
            c("Kısa ve keskin hayalet irkilmesi",current("ghost_death",1.1,.3,.04)),
            c("Daha pes, nefesi kırılan ruh",current("ghost_death",.8,.4,.2).copy(lowPass=3400.0)),
            c("Fısıltılı, kuru hayalet tepkisi",current("ghost_attack",1.15,.3,.1)),
            c("Tiz, anlık ruh çığlığı",current("ghost_death",1.3,.23,.35)),
            c("Boğuk, kısa fısıltı ve sönüş",current("ghost_attack",.85,.4,.45).copy(lowPass=2100.0))
        ),
        group("SLH","Slime · hasar alma",
            "Mevcut LD4 ölümüne yakın ıslak/jölemsi ama çok kısa tepkiler. Acidic Path'in oyuncuyu yakma sesiyle karışmaması için burada cızırtı yerine gövde titreşimi kullanılıyor.",
            c("Kısa jöle gövde dalgalanması",current("slime_death",1.35,.27)),
            c("İnce, hızlı ıslak pıtırtı",L("rubberduck-rpg","creature_slime_01.ogg",speed=1.5,maxSeconds=.23)),
            c("Daha tok, kısa sümüksü darbe",L("rubberduck-rpg","creature_slime_03.ogg",speed=1.15,maxSeconds=.35,lowPass=2600.0)),
            c("Yumuşak, kabarcıklı sarsılma",L("rubberduck-rpg","creature_slime_02.ogg",speed=1.4,maxSeconds=.28)),
            c("Hızlı, yapışkan gövde titremesi",L("rubberduck-rpg","creature_slime_04.ogg",speed=1.55,maxSeconds=.25))
        )
    )

    @JvmStatic fun main(args:Array<String>) {
        val currentSource=File("tools/audio-audition/sources/finishing-preview/Audio").apply{mkdirs()}
        val filenames=listOf("sling_throw","sling_impact","boomerang_throw","shield_strike","defensive_burst",
            "bear_attack","bear_death","swarm_attack","swarm_death","crocodile_attack","crocodile_death",
            "gorilla_death","spearman_death","horseman_death","witch_death","ghost_attack","ghost_death",
            "slime_death","vampire_death","vampire_hurt")
        filenames.forEach{File("assets/audio/sfx/$it.wav").copyTo(File(currentSource,"$it.wav"),overwrite=true)}
        // Locate real vocal foreground instead of assuming that the whole 3.4s file is a scream.
        val witch=InstallMagicNormalAudio.read(File(currentSource,"witch_death.wav"))
        val hop=441
        val energies=(0 until witch.size/hop).map{frame->
            kotlin.math.sqrt((0 until hop).sumOf{k->witch[frame*hop+k].toDouble().let{it*it}}/hop)
        }
        val foreground=energies.indices.filter{energies[it]>energies.max()*.15}
        check(foreground.isNotEmpty())
        for(i in 0..4){
            val frame=foreground[(foreground.lastIndex*i/5).coerceAtMost(foreground.lastIndex)]
            val first=(frame*hop-220).coerceAtLeast(0);val last=(first+22050).coerceAtMost(witch.size)
            PrepareAudioAudition.wav(File(currentSource,"witch-hurt-${i+1}.wav"),witch.copyOfRange(first,last))
        }
        fun ref(name:String,label:String)=R(label,File("assets/audio/sfx/$name.wav"))
        val references=linkedMapOf(
            "SLS" to listOf(ref("sling_throw","Sling · mevcut normal saldırı"),ref("sling_impact","Sling · mevcut ability inişi; level 5'te her inişte aynı ses")),
            "BRC" to listOf(ref("boomerang_throw","Boomerang · mevcut normal saldırı"),ref("boomerang_throw","Boomerang · mevcut ability; normalle aynı dosya")),
            "SR" to listOf(ref("shield_strike","Shield · mevcut normal saldırı"),ref("defensive_burst","Shield · mevcut Defensive Burst")),
            "BRH" to listOf(ref("bear_death","Bear · mevcut ölüm")),
            "BSH" to listOf(ref("swarm_death","Bee Swarm · mevcut ölüm")),
            "CRH" to listOf(ref("crocodile_death","Crocodile · mevcut ölüm")),
            "GRH" to listOf(ref("gorilla_death","Gorilla · mevcut ölüm")),
            "SPH" to listOf(ref("spearman_death","Spearman · mevcut ölüm")),
            "HOH" to listOf(ref("horseman_death","Horseman · mevcut ölüm")),
            "WIH" to listOf(ref("witch_death","Witch · mevcut xl57 ölüm çığlığı")),
            "GHH" to listOf(ref("ghost_death","Ghost · mevcut ölüm")),
            "SLH" to listOf(ref("slime_death","Slime · mevcut büyük/küçük ortak ölüm")),
            "VAMPIRE" to listOf(ref("vampire_hurt","Vampire Lord · VP5 hasar alma zaten tamam"),ref("vampire_death","Vampire Lord · VS5 son ölüm zaten tamam"))
        )
        val missingBosses=DefaultEnemyKinds.all.filter{
            it.allowedTypes.any{type->type in setOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS)} && it.id!=EnemyKindIds.VAMPIRE_LORD
        }
        check(missingBosses.size==9)
        check(missingBosses.all{EnemyAudio.deathCue(it.id)!=null})
        val groups=groups();check(groups.size==12 && groups.sumOf{it.choices.size}==60)
        val credits=File("tools/audio-audition/finishing-source-credits.txt")
        credits.writeText("PACK 22 — 60 NEW PREVIEW CANDIDATES + 17 EXACT INSTALLED REFERENCES.\n"+
            "No new cue installed. Reference files are unchanged copies of assets/audio/sfx.\n"+
            "Finishing-preview inputs are snapshots of those same installed licensed assets.\n\n"+
            "NEW CANDIDATE DESIGN: short excerpts, pitch/speed, EQ, fades, light stone/wood/metal layers.\n"+
            "Animal hurt designs use ordinary calls, not real animal injury recordings.\n"+
            "Witch hurt uses excerpts of short scream no effects.wav by xl57, CC BY 4.0:\n"+
            "https://freesound.org/people/xl57/sounds/671201/\nhttps://creativecommons.org/licenses/by/4.0/\n"+
            "Current installed licenses, including attribution and edit details, follow.\n\n"+
            listOf("CREDITS.txt","BEAST-COMBAT-LICENSES.txt","HUMANOID-LICENSES.txt","UNDEAD-VAMPIRE-LICENSES.txt")
                .joinToString("\n\n"){name->"=== $name ===\n"+File("assets/audio/$name").readText()},Charsets.UTF_8)
        AuditionPage.write(22,"Boss hasar tepkileri · son silah ayrıntıları",
            "60 YENİ ADAY; henüz oyuna bağlanmadı. Her grubun başındaki MEVCUT SES kayıtları sadece karşılaştırma içindir. Eksik: Bear, Bee Swarm, Crocodile, Spearman, Horseman, Ghost, Slime, Gorilla, Witch HASAR ALMA sesleri. Ölüm sesleri zaten tamam, tekrar seçilmeyecek. Vampire Lord'un VP5/VS5'i sayfa sonunda referans. Yeni boss tepkileri yalnız gerçek HP kaybında, düşman başına tekrar aralığıyla kullanılacak; normal/unique düşmanlara eklenmeyecek. Referanslar ham dosyalardır; oyundaki Master/SFX ve boss +%35 miksini bu sayfa uygulamaz.",
            groups,credits,references)
        val out=File("tools/audio-audition/listening-pack-22")
        references.forEach{(id,refs)->refs.forEachIndexed{i,r->check(r.file.readBytes().contentEquals(File(out,"$id-reference-${i+1}.wav").readBytes()))}}
        File(out,"EKSİKLER-VE-KULLANIM.txt").writeText(
            "HASAR ALMA EKSİK BOSSLAR: "+missingBosses.joinToString{it.displayName}+"\n"+
            "Vampire Lord VP5 hazır. Tüm boss ölüm sesleri hazır ve referans olarak eklendi.\n"+
            "Normal/unique düşmana yeni hit vokali, spawn/adım sesi eklenmeyecek.\n"+
            "SLS: ana Sling taşının gelişmiş parçalanmasında bir kere.\nBRC: gerçek son yakalamada bir kere.\nSR: maksimum birikime ilk ulaşmada bir kere.\n"+
            "BSH: Swarm Being ve hasar alma tepkisi aynı hit'te iki kere çalmamalı.\n"+
            "Ölümcül darbede hasar alma vokali yerine ölüm önceliklendirilecek.\n",Charsets.UTF_8)
        println("FINISHING_AUDIO_REFERENCES_PASS: 9 missing bosses verified against catalog, 17 byte-exact references; gameplay untouched")
    }
}
