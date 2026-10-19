package qa

import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.AudioFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/** Licensed source recordings and clearly labeled edits/layers. Standalone preview assets only. */
object PrepareAudioAudition {
    private const val RATE=44100
    private val root=File("tools/audio-audition")
    data class Layer(val pack:String,val file:String,val offset:Double=0.0,val gain:Double=1.0,val maxSeconds:Double=8.0,val speed:Double=1.0,val lowPass:Double=0.0,val transient:Int=0,val highPass:Double=0.0,val startSeconds:Double=0.0,val gust:Boolean=false,val reverse:Boolean=false)
    data class Candidate(val label:String,val layers:List<Layer>,val upgradedLayers:List<Layer>?=null)
    data class Group(val id:String,val title:String,val note:String,val candidates:List<Candidate>)
    private fun r(file:String,offset:Double=0.0,gain:Double=1.0)=Layer("rpg-audio",file,offset,gain)
    private fun i(file:String,limit:Double=8.0)=Layer("interface-sounds",file,maxSeconds=limit)
    private fun hit(file:String,limit:Double=.22,gain:Double=1.0,offset:Double=0.0)=Layer("impact-sounds",file,offset,gain,limit)
    private fun c(label:String,vararg layers:Layer)=Candidate(label,layers.toList())
    private val groups01=listOf(
        Group("A","Metalik buton tıklaması","Kısa, hafif materyal adayları. Darbe kayıtları tıklama uzunluğuna kesildi.",listOf(
            c("Kısa metal mekanizma",r("metalClick")),
            c("Hafif metal vuruş · 1",hit("impactMetal_light_000",.15)),
            c("Hafif metal vuruş · 2",hit("impactMetal_light_003",.13)),
            c("İnce metal plaka",hit("impactPlate_light_001",.15)),
            c("İnce teneke mekanizma",hit("impactTin_medium_002",.12))
        )),
        Group("B","Seçim / onay","İlk dört seçenek paketin özgün onay sesleri; beşinci daha fiziksel, metalik bir alternatif.",listOf(
            c("Onay · 1",i("confirmation_001")),c("Onay · 2",i("confirmation_002")),
            c("Onay · 3",i("confirmation_003")),c("Onay · 4",i("confirmation_004")),
            c("Metalik kilit onayı",r("metalLatch"))
        )),
        Group("C","Geri / iptal","Geri dönme ile hata sesi farklı tutulacak; buradakiler geri dönüş adaylarıdır.",listOf(
            c("Geri · 1",i("back_001")),c("Geri · 2",i("back_002")),c("Geri · 3",i("back_003")),
            c("Geri · 4",i("back_004")),c("Kapatma alternatifi",i("close_001"))
        )),
        Group("D","Geçersiz işlem / hata","Yalnızca oyuncunun işlem yapamadığını bilmesi gereken durumlar için; sürekli çalmayacak.",listOf(
            c("Hata · 1",i("error_001")),c("Hata · 2",i("error_002")),c("Hata · 3",i("error_003")),
            c("Hata · 4",i("error_004")),c("Hata · 5",i("error_005"))
        )),
        Group("E","Defter / sayfa / preset","Sayfa kayıtları preset sayfasını değiştirmek için; açılış ve kapanış iki alternatif materyal dokusudur. Hover'da sürekli çalması önerilmiyor.",listOf(
            c("Sayfa çevirme · 1",r("bookFlip1")),c("Sayfa çevirme · 2",r("bookFlip2")),
            c("Sayfa çevirme · 3",r("bookFlip3")),c("Defter açma",r("bookOpen")),c("Defter kapama",r("bookClose"))
        )),
        Group("F","Ahşap sandık açma","Bunlar ham sandık kayıtları değil: gerçek gıcırtı, kapı açma, kilit ve ahşap temas kayıtlarından hazırlanan 5 taslak. F3/F4 kapı kaydı içerir; sandığa uygunluğu dinlenerek seçilecek.",listOf(
            c("Kilit + gıcırtı · 1",r("metalLatch",0.0,.45),r("creak1",.15,1.0)),
            c("Kısa kilit + gıcırtı · 2",r("metalClick",0.0,.4),r("creak2",.12,1.0)),
            c("Ahşap kapak · 1",r("metalLatch",0.0,.3),r("doorOpen_1",.14,1.0)),
            c("Ahşap kapak · 2",r("doorOpen_2",0.0,1.0)),
            c("Gıcırtı + kapağın dayanması",r("creak3",0.0,1.0),hit("impactWood_light_001",.35,.45,.8))
        ))
    )

    private val groups02=listOf(
        Group("G","Kılıç savurma / kesici silah dokusu","Normal saldırı için kısa adaylar. G1–G2 özgün knifeSlice kayıtları; diğerleri aynı kayıtların kumaş/metal ayrıntısıyla katmanlanmış taslaklarıdır. Her düşmana vuruşta ayrı ayrı çalmaları önerilmiyor.",listOf(
            c("Keskin savurma · 1",r("knifeSlice")),
            c("Keskin savurma · 2",r("knifeSlice2")),
            c("Kumaş dokulu savurma",r("knifeSlice"),r("cloth1",0.0,.35)),
            c("İkili kılıç taslağı",r("knifeSlice",0.0,.8),r("knifeSlice2",.07,.65)),
            c("Metalik ayrıntılı savurma",r("knifeSlice2"),r("drawKnife1",0.0,.25))
        )),
        Group("H","Tok darbe / ağır silah teması","Maul ve Warhammer için darbe karakteri seçimi. Bunlar materyal darbe kayıtlarıdır; hazır bir Earthbreaker veya tam saldırı animasyonu sesi değildir. Ağır ability katmanı daha sonra seçilecek.",listOf(
            c("Yumuşak tok darbe",hit("impactSoft_heavy_000",.65)),
            c("Kuru ahşap darbe",hit("impactWood_heavy_000",.65)),
            c("Kısa metal darbe",hit("impactMetal_medium_000",.5)),
            c("Ahşap + tok gövde",hit("impactWood_heavy_002",.65,.6),hit("impactSoft_heavy_001",.65,1.0)),
            c("Metal + tok gövde",hit("impactMetal_heavy_001",.65,.35),hit("impactSoft_heavy_002",.65,1.0))
        )),
        Group("I","Kalkan / metal temas","Kalkanın normal saldırısında kullanılabilecek metal doku adayları. Bunları her hasar alındığında çaldırmayacağız. Defensive Burst ayrı, daha tok bir ability sesi olarak seçilecek.",listOf(
            c("İnce metal temas",hit("impactMetal_light_000",.5)),
            c("Kısa metal temas",hit("impactMetal_light_003",.5)),
            c("Orta ağırlıkta temas",hit("impactMetal_medium_002",.65)),
            c("Ağır metal temas",hit("impactMetal_heavy_003",.8)),
            c("Tok gövdeli kalkan teması",hit("impactMetal_medium_004",.65,.75),hit("impactSoft_medium_001",.5,.5))
        ))
    )

    private fun sw(n:Int,speed:Double=1.0,offset:Double=0.0,gain:Double=1.0)=Layer("swishes","swishes/swish-$n.wav",offset,gain,speed=speed)
    private fun tiny(file:String,speed:Double=1.0,offset:Double=0.0,gain:Double=1.0)=Layer("tinysized","sfx-cc0/$file.wav",offset,gain,speed=speed)
    private fun whip(speed:Double=1.0,offset:Double=0.0)=Layer("reemax-whip","whip.wav",offset,speed=speed)
    private fun swords(label:String,layer:Layer)=Candidate(label,listOf(layer),listOf(layer,layer.copy(offset=.12,gain=.9)))
    private val groups03=listOf(
        Group("DS","Double Swords · tekli / gelişmiş ikili","Her numara bir eşleşmedir: önce normal saldırının tek sesi, ardından gelişmiş saldırının iki vuruşlu taslağı çalar. Aynı çiftte ikinci savurma 120 ms sonradır; oyundaki animasyona yerleştirirken zamanlama eşleştirilecek. G1 Longsword'a ayrıldı; burada kullanılmıyor.",listOf(
            swords("Yeni savurma kaydı · 1",sw(1)),
            swords("Yeni savurma kaydı · 2",sw(2)),
            swords("Yeni savurma kaydı · 3",sw(5)),
            swords("Yeni savurma kaydı · 4",sw(8)),
            swords("Yeni savurma kaydı · 5",sw(11))
        )),
        Group("LN","Lance · havayı delen ileri itiş","Savurma ve hava geçişi kayıtları. Gerçek bir mızrak çarpışması oldukları iddia edilmiyor; ileri itişin hava dokusu için seçilecek. Son iki aday daha tiz hava geçişidir.",listOf(
            c("İleri itiş / kısa swish",sw(3)),
            c("İleri itiş / geniş swish",sw(6)),
            c("İleri itiş / tok swish",sw(9)),
            c("İnce hava geçişi · 1",tiny("tin-whistle-whoosh-01")),
            c("İnce hava geçişi · 2",tiny("tin-whistle-whoosh-02"))
        )),
        Group("SH","Shield · metalik temas","Önceki I adayları elendi. Bunlar yeni kaynak paketteki beş ayrı sword-clash metal çarpışmasıdır; kalkan kaydı değil, kalkan normal saldırısına önerilen metal dokulardır.",listOf(
            c("Metal çarpışması · 1",tiny("sword-clash-01")),
            c("Metal çarpışması · 2",tiny("sword-clash-02")),
            c("Metal çarpışması · 3",tiny("sword-clash-03")),
            c("Metal çarpışması · 4",tiny("sword-clash-04")),
            c("Metal çarpışması · 5",tiny("sword-clash-05"))
        )),
        Group("WH","Warhammer · ağır metal darbe","Önceki H adayları elendi. İlk iki aday yeni metal-hammer kayıtları. WH3 ve WH4 aynı kayıtların daha yavaş, daha pes varyasyonları; WH5 savurma + darbe katmanıdır. Beş ayrı özgün kayıt değildir.",listOf(
            c("Metal çekiç kaydı · 1",tiny("metal-hammer-hit-01")),
            c("Metal çekiç kaydı · 2",tiny("metal-hammer-hit-02")),
            c("Çekiç 1 · %80 hız / daha pes",tiny("metal-hammer-hit-01",.8)),
            c("Çekiç 2 · %72 hız / daha pes",tiny("metal-hammer-hit-02",.72)),
            c("Ağır savurma + metal darbe",sw(13,.85,gain=.3),tiny("metal-hammer-hit-02",.85,.14))
        )),
        Group("WP","Whip · deri kırbaç","Yeni kaynak: Tuomo Untinen/Reemax'ın deri kayışla yaptığı whip kaydı. WP1 özgün kaydın mono/seviye eşlenmiş hali; WP2–WP4 hız/perde varyasyonları, WP5 savurmayla katmanlanmış taslaktır. Beş ayrı kayıt değildir. Ücretsiz CC BY 4.0; seçilirse oyun içinde atıf eklenecek.",listOf(
            c("Deri kırbaç · özgün hız",whip()),
            c("Deri kırbaç · %90 hız / daha pes",whip(.9)),
            c("Deri kırbaç · %112 hız / daha tiz",whip(1.12)),
            c("Deri kırbaç · %80 hız / daha ağır",whip(.8)),
            c("Hava savurması + deri şaklama",sw(4,offset=0.0,gain=.35),whip(offset=.1))
        ))
    )

    private fun shot(file:String,take:Int,speed:Double=1.0)=Layer("stillnorth-textures",file,speed=speed,transient=take)
    private fun body(file:String,speed:Double=.75,cutoff:Double=700.0,gain:Double=1.0)=Layer("impact-sounds",file,gain=gain,maxSeconds=.45,speed=speed,lowPass=cutoff)
    private fun hammerTone(n:Int,speed:Double,cutoff:Double)=tiny("metal-hammer-hit-0$n",speed).copy(maxSeconds=.45,lowPass=cutoff)
    private val groups04=listOf(
        Group("WM","Warhammer · daha küt, ağır ve kalın","Önceki WH seçeneklerinin yerine yeni tasarımlar: metal çekiç kaydının çınlaması alçak geçiren filtreyle azaltıldı, bazılarına kısa gövde darbesi eklendi. Perde/hız düşürme ve katmanlama içeren taslaklardır; beş ayrı özgün kayıt değildir. Henüz oyuna eklenmedi.",listOf(
            c("Küt metal + tok gövde",hammerTone(2,.70,650.0),body("impactSoft_heavy_000")),
            c("Çok pes, kısa metal darbe",hammerTone(1,.55,500.0)),
            c("Kalın metal + ağır gövde",hammerTone(2,.60,800.0),body("impactSoft_heavy_002",.65,600.0,.8)),
            c("Küt metal + kuru alt darbe",hammerTone(1,.65,550.0),body("impactWood_heavy_002",.70,400.0,.65)),
            c("Kısa vurgu + derin darbe",hammerTone(2,.8,1100.0).copy(maxSeconds=.23,gain=.55),body("impactSoft_heavy_003",.6,500.0))
        )),
        Group("BW","Bow · gerçek yay bırakışı","Still North Media'nın gerçek yay kayıtlarından ayrı atışlar. BW1–BW3 English Longbow, BW4–BW5 Scythian Recurve kaydından kesildi. Yalnızca atış sesi; her ok çarpışmasına ses ekleme veya ability seçimi değildir.",listOf(
            c("English Longbow · atış 1",shot("English Longbow Shoot.wav",1)),
            c("English Longbow · atış 2",shot("English Longbow Shoot.wav",2)),
            c("English Longbow · atış 3",shot("English Longbow Shoot.wav",3)),
            c("Scythian Recurve · atış 1",shot("Scythian Recurve Shoot.wav",1)),
            c("Scythian Recurve · atış 2",shot("Scythian Recurve Shoot.wav",2))
        )),
        Group("CB","Crossbow · gerçek mekanizma / bırakış","Still North Media Crossbow Shoot kaydındaki beş farklı atış kesiti. Bow ile aynı ses kullanılmıyor. Çıkış mekanizması için adaylar; yeniden kurma veya projectile çarpışması değil.",listOf(
            c("Arbalet atışı · 1",shot("Crossbow Shoot.wav",1)),
            c("Arbalet atışı · 2",shot("Crossbow Shoot.wav",2)),
            c("Arbalet atışı · 3",shot("Crossbow Shoot.wav",3)),
            c("Arbalet atışı · 4",shot("Crossbow Shoot.wav",4)),
            c("Arbalet atışı · 5",shot("Crossbow Shoot.wav",5))
        )),
        Group("DG","Dagger · kısa bıçak fırlatışı","Kısa hava kesişi ve bazı adaylarda hafif metal ayrıntısı. Fırlatma hissi için tasarlanmış swish katmanlarıdır; gerçek hançer fırlatma kaydı oldukları iddia edilmiyor. Longsword, DS1 ve LN1 sesleri aynen paylaşılmıyor.",listOf(
            c("İnce kısa fırlatış",sw(4,1.2)),
            c("Hızlı hava kesişi",sw(7,1.25)),
            c("Metalik uçlu kısa savurma",sw(10,1.3),tiny("knife-unsheathe-02",1.35,gain=.18).copy(maxSeconds=.2)),
            c("Kuru hızlı fırlatış",sw(12,1.35)),
            c("İnce metal + hava",sw(13,1.5),tiny("metal-knife-scrape-03",1.5,gain=.12).copy(maxSeconds=.18))
        )),
        Group("SL","Sling · savurma / taş bırakışı","Gerçek sapan kaydı değil: havada savrulan nesne, deri/kumaş ve kısa swish kayıtlarından hazırlanmış öneriler. Projectile parçalanması ve ability taşının iniş sesi bu seçimden ayrı ele alınacak.",listOf(
            c("Dairesel hava savurması · 1",tiny("tube-plastic-whoosh-01",1.1)),
            c("Dairesel hava savurması · 2",tiny("tube-plastic-whoosh-02",1.05)),
            c("Deri dokulu bırakış",tiny("quiver-leather-squeeze-01",1.2,gain=.3).copy(maxSeconds=.2),sw(10,1.05,.08)),
            c("Kumaş dokulu bırakış",tiny("cloth-pouch-shake-02",1.3,gain=.25).copy(maxSeconds=.22),sw(7,.95,.10)),
            c("Kısa sade taş savurması",sw(12,.9))
        )),
        Group("BR","Boomerang · dönerek savrulma","Kısa aralıklı iki/üç hava geçişiyle dönme hissi veren taslaklar. Bunlar sürekli uçuş loop'u değil, tek atış için önerilerdir. Gelişmiş arka tur veya Guided Hunt ability sesi henüz seçilmiyor.",listOf(
            c("İki geçişli kısa dönüş",sw(6,.9,gain=.8),sw(6,1.0,.14,.65)),
            c("İnce iki geçiş",sw(9,1.15,gain=.8),sw(9,1.25,.12,.65)),
            c("Geniş dönüş dokusu",tiny("tube-plastic-whoosh-02",.85),sw(8,1.0,.18,.5)),
            c("Üç kısa hava geçişi",sw(4,1.4,gain=.8),sw(4,1.5,.09,.6),sw(4,1.6,.18,.4)),
            c("Tok iki geçiş",sw(13,.9,gain=.8),sw(11,1.1,.16,.6))
        ))
    )

    private fun thwack(n:Int,speed:Double=1.0,gain:Double=1.0)=Layer("thwack","PCM/thwack-${n.toString().padStart(2,'0')}.wav",gain=gain,speed=speed)
    private val groups05=listOf(
        Group("TH","Warhammer · sert temas / pat–güm","Thwack kayıtlarıyla darbenin vurduğu anı öne çıkaran adaylar. TH1–TH3 bu paketten ayrı dosyalar; TH1 ve TH2 kaynak yazarın filtresiz dediği 01 ve 10 kayıtlarıdır. TH4–TH5 darbe gövdesi eklenmiş tasarımlar. Metal çınlaması veya savurma sesi eklenmedi; beş gerçek Warhammer kaydı oldukları iddia edilmiyor. TH3 seçildi ve oyuna bağlandı, diğerleri arşiv adayıdır.",listOf(
            c("Kuru sert darbe · ham 01",thwack(1)),
            c("Sert pat vurgusu · ham 10",thwack(10)),
            c("Farklı sert temas · kaynak 06",thwack(6)),
            c("Sert pat + kuru darbe gövdesi",thwack(1),hit("impactWood_heavy_002",.35,.8)),
            c("Sert pat + tok güm gövdesi",thwack(10,.95),Layer("impact-sounds","impactSoft_heavy_003",offset=.015,gain=1.25,maxSeconds=.4,speed=.9))
        ))
    )

    private fun burst(label:String,stroke:Layer,count:Int=5,gap:Double=.12,accent:Layer?=null):Candidate {
        val layers=(0 until count).map{stroke.copy(offset=it*gap,gain=stroke.gain*(if(it==count-1)1.0 else .8))}
        return Candidate(label,layers+listOfNotNull(accent))
    }
    private fun earth(label:String,impact:Layer,debris:Layer):Candidate = Candidate(label,
        (0..2).flatMap{n->listOf(impact.copy(offset=n*.18),debris.copy(offset=n*.18+.025,gain=debris.gain*.45))})
    private val groups06=listOf(
        Group("RS","Double Swords · Rapid Strokes","Beş hızlı vuruşlu örnek taslaklar; farklı savurma ve metal ayrıntıları karşılaştırılıyor. Bunlar saldırı başına düşman sayısıyla çoğalmayacak. Seçilen doku oyuna bağlanırken gerçek vuruş zamanlamasına eşlenecek; gelişmiş ability'de yedi vuruşa uyarlanacak.",listOf(
            burst("Beş kısa kesiş",sw(1,1.05)),
            burst("Daha kuru seri kesiş",sw(5,1.15)),
            burst("Keskin seri savurma",sw(8,1.15)),
            burst("Seri kesiş + metal final",sw(1,1.1),accent=tiny("sword-clash-03",offset=.48,gain=.25).copy(maxSeconds=.3)),
            burst("Tok seri + darbe finali",sw(11,1.2),accent=thwack(6,gain=.25).copy(offset=.48))
        )),
        Group("SP","Lance · Spin Attack","Dairesel süpürmeye uygun geniş hava hareketi; bazı adaylarda hafif metal temas. Gerçek mızrak dönüşü kaydı değil, kayıt katmanlarından ses tasarımı. Her düşmana ayrı çarpışma sesi eklenmeyecek.",listOf(
            c("Geniş çift hava süpürmesi",sw(3,.8),sw(6,1.0,.12,.65)),
            c("Daha tok dairesel savurma",sw(6,.65),sw(9,.9,.14,.5)),
            c("Uzun hava hareketi",tiny("tube-plastic-whoosh-01",.8),sw(11,.9,.14,.45)),
            c("Hava süpürmesi + metal temas",sw(3,.8),tiny("sword-clash-02",offset=.10,gain=.25).copy(maxSeconds=.4)),
            c("Kısa güçlü dönüş",sw(13,.85),sw(8,1.0,.10,.5))
        )),
        Group("AS","Longsword · Air Slash","Normal saldırıdan daha geniş ve uzun bir hava dalgası hissi. Bunlar mevcut gerçek swish kayıtlarının katmanlanmış varyasyonları; yeni büyü kaydı veya sentezlenmiş lazer sesi değil.",listOf(
            c("Tok hava dalgası",sw(13,.65),sw(9,.9,.08,.45)),
            c("Geniş hava geçişi",tiny("tube-plastic-whoosh-01",.75),sw(11,.85,.08,.4)),
            c("Keskin başlangıç / geniş kuyruk",sw(5,.85),sw(8,.7,.08,.65)),
            c("İnce ıslıklı hava dalgası",tiny("tin-whistle-whoosh-02",.85),sw(6,.9,.04,.5)),
            c("Kılıç kesişi + hava dalgası",r("knifeSlice",gain=.35),sw(10,.75,.06))
        )),
        Group("SB","Shield · Defensive Burst","Biriktirilen savunmayı tüketen tek, tok metal darbe taslağı. Pasif stack kazanımında sürekli ses çalması için değil; yalnızca aktif burst içindir.",listOf(
            c("Sert pat + metal kalkan",thwack(6),tiny("sword-clash-01",gain=.5).copy(maxSeconds=.65)),
            c("Tok metal plaka",hit("impactPlate_heavy_001",.65),hit("impactPunch_heavy_001",.45,.55)),
            c("Metal vurgu + derin gövde",tiny("metal-hammer-hit-02",.85,gain=.45).copy(maxSeconds=.55),thwack(10)),
            c("Kısa sert burst",thwack(1),tiny("sword-clash-04",gain=.35).copy(maxSeconds=.45)),
            c("Geniş metal patlaması",hit("impactPlate_heavy_003",.65,.7),thwack(6),tiny("sword-clash-05",gain=.25).copy(maxSeconds=.55))
        )),
        Group("EB","Warhammer · Earthbreaker","Üç ileri darbe için 180 ms aralıklı örnek seri. Normal saldırıda seçilen sert TH3 karakteri bazı adaylarda korunuyor, taş/maden darbesi dokusu ekleniyor. Bunlar gerçek deprem kayıtları değil; gelişmiş ability'de dört darbeye uyarlanacak.",listOf(
            earth("Sert üç darbe + taş ayrıntısı",thwack(6),hit("impactMining_000",.3)),
            earth("Kuru taş darbe serisi",thwack(1),hit("impactMining_001",.35)),
            earth("Tok sert zemin serisi",thwack(10,.95),hit("impactMining_002",.35)),
            earth("Daha kuru zemin çatlağı",hit("impactWood_heavy_004",.35),hit("impactMining_003",.35)),
            earth("Sert darbe + ağır gövde",thwack(6),hit("impactPunch_heavy_002",.4))
        )),
        Group("WS","Whip · Whip Sweep","Geniş savurma ve tek belirgin deri şaklama; normal iki vuruşlu saldırıdan farklı, tek geniş ability süpürmesi. Whip kaynak atfı geçerli; tüm adaylar kayıt katmanlarından tasarlanmış varyasyonlar.",listOf(
            c("Geniş hava + deri şaklama",sw(4,.8,gain=.5),whip(offset=.1)),
            c("Daha tok geniş savurma",sw(13,.8,gain=.45),whip(.95,.12)),
            c("Keskin kısa süpürme",sw(8,1.0,gain=.45),whip(1.08,.08)),
            c("Uzun hava hareketi + şaklama",tiny("tube-plastic-whoosh-02",.85,gain=.45),whip(offset=.18)),
            c("Tok başlangıç / deri final",sw(6,.85,gain=.45),whip(.9,.14))
        ))
    )

    private fun air(speed:Double=1.0)=Layer("air-move","wind.wav",speed=speed)
    private fun wind(pack:String,speed:Double,start:Double,cutoff:Double,duration:Double)=Layer(pack,
        if(pack=="wind-loop")"wind.ogg" else "wind.wav",speed=speed,startSeconds=start,highPass=cutoff,maxSeconds=duration,gust=true)
    private val groups07=listOf(
        Group("AW","Longsword · Air Slash / keskin rüzgâr","Önceki AS adayları elendi. Bu beş taslak yalnızca yeni hava/rüzgâr kaynaklarını kullanır; kılıç, metal veya darbe katmanı yok. Üç kaynaktan hazırlanan hız, tiz vurgu, kısa kesit ve rüzgâr zarfı varyasyonlarıdır; beş ayrı özgün kayıt değildir. Hiçbiri henüz oyuna bağlı değil.",listOf(
            c("Hızlı hava geçişi · özgün doku",air()),
            c("Daha ince / keskin hava geçişi",air(1.12).copy(highPass=650.0)),
            c("Kısa, keskin rüzgâr hamlesi",wind("short-wind",1.2,.4,450.0,.65)),
            c("Tiz vurgulu rüzgâr kesiti",wind("wind-loop",1.0,.6,1000.0,.6)),
            c("Hava darbesi + ince rüzgâr kuyruğu",air(.95),wind("wind-loop",1.1,.3,1200.0,.55).copy(offset=.06,gain=.55))
        ))
    )

    private fun rock(label:String,vararg layers:Layer)=Candidate(label,layers.toList(),
        layers.toList()+layers.map{it.copy(offset=it.offset+.35,gain=it.gain*.8)})
    private val groups08=listOf(
        Group("KS","Dagger · Knife Swarm","DG4 dokusunun kalabalıklaştırılmış sürümleri. Tek salvo örnekleri; gelişmiş ikinci salvo oyunda ayrı tetiklenecek.",listOf(
            burst("Sık, kısa bıçak çemberi",sw(12,1.35),8,.012),
            burst("Daha kalabalık çember",sw(12,1.35),12,.009),
            burst("Biraz yayılmış seri",sw(12,1.35),10,.018),
            burst("Tok katmanlı salvo",sw(12,1.25),12,.01,sw(7,1.2,gain=.35)),
            burst("Kısa, yoğun bıçak patlaması",sw(12,1.5),16,.006)
        )),
        Group("BV","Bow · Leisurely Shot","BW3 yay atışından beş oklu salvo tasarımları. Gelişmiş ikinci salvo burada çoğaltılmadı.",listOf(
            burst("Beş sık yay bırakışı",shot("English Longbow Shoot.wav",3),5,.015),
            burst("Beş belirgin yay bırakışı",shot("English Longbow Shoot.wav",3),5,.035),
            burst("Tok, sık ok salvosu",shot("English Longbow Shoot.wav",3,.85),5,.015),
            burst("Yay + ince hava geçişi",shot("English Longbow Shoot.wav",3),5,.02,sw(4,1.2,gain=.3)),
            burst("Biraz tiz salvo",shot("English Longbow Shoot.wav",3,1.12),5,.022)
        )),
        Group("HB","Crossbow · Heavy Bolts","CB4 normal atışının daha pes/kalın sürümleri; bazı seçeneklerde kısa gövde darbesi var.",listOf(
            c("CB4 · %85 hız",shot("Crossbow Shoot.wav",4,.85)),
            c("CB4 · %70 hız",shot("Crossbow Shoot.wav",4,.70)),
            c("Kalın mekanizma",shot("Crossbow Shoot.wav",4,.8),hit("impactMetal_medium_000",.18,.25)),
            c("Tok gövdeli bırakış",shot("Crossbow Shoot.wav",4,.75),body("impactWood_heavy_002",.9,650.0,.35)),
            c("Ağır kısa vurgu",shot("Crossbow Shoot.wav",4,.65),sw(12,1.1,gain=.25))
        )),
        Group("ST","Sling · Shatterstone","Önce tek iniş, ardından gelişmiş iki iniş örneği. İkinci temas 350 ms sonra. Taş/mining ve darbe katmanlarından tasarımlar; gerçek kaya düşüş kaydı oldukları iddia edilmiyor.",listOf(
            rock("Kuru taş ve kırıntı",hit("impactMining_000",.35),thwack(6,gain=.3)),
            rock("Kaba ağır taş",hit("impactMining_001",.4),body("impactWood_heavy_002",.75,900.0,.55)),
            rock("Tok taş inişi",hit("impactMining_002",.35),body("impactSoft_heavy_003",.8,650.0,.7)),
            rock("Sert pat ve kırıntılar",thwack(10,.9),hit("impactMining_003",.4,.65,.02)),
            rock("Derin, kısa kaya teması",hit("impactMining_004",.35).copy(speed=.8),thwack(6,.85,.6))
        )),
        Group("GH","Boomerang · Guided Hunt","GH1 mevcut BR3 normal atışının aynısıdır. Diğerleri hava/perde varyasyonları; sürekli uçuş loop'u değildir.",listOf(
            c("Mevcut normal atış · BR3",tiny("tube-plastic-whoosh-02",.85),sw(8,1.0,.18,.5)),
            c("Biraz daha tok aynı doku",tiny("tube-plastic-whoosh-02",.75),sw(8,.9,.18,.5)),
            c("Kısa yönlendirme vurgusu",tiny("tube-plastic-whoosh-02",.85),sw(8,1.0,.10,.65)),
            c("Üç hafif hava geçişi",sw(4,1.3),sw(4,1.4,.08,.65),sw(4,1.5,.16,.4)),
            c("Rüzgâr ayrıntılı BR3",tiny("tube-plastic-whoosh-02",.85),sw(8,1.0,.18,.5),air(1.2).copy(gain=.18))
        )),
        Group("HW","Kısa insan ıslığı","dklon Whistles paketinden dört ayrı kayıt ve bir pes varyasyon. Enstrüman/düdük değil insan ıslığı adayları. Ücretsiz CC BY 3.0; seçilirse dklon atfı eklenecek.",listOf(
            c("İnsan ıslığı · kayıt 1",Layer("dklon-whistles","whistle_1.wav")),
            c("İnsan ıslığı · kayıt 2",Layer("dklon-whistles","whistle_2.wav")),
            c("İnsan ıslığı · kayıt 3",Layer("dklon-whistles","whistle_3.wav")),
            c("İnsan ıslığı · kayıt 4",Layer("dklon-whistles","whistle_4.wav")),
            c("Kayıt 1 · biraz daha pes",Layer("dklon-whistles","whistle_1.wav",speed=.85))
        ))
    )

    private fun magic(file:String,speed:Double=1.0,limit:Double=.55,gain:Double=1.0)=
        Layer("rubberduck-rpg",file,speed=speed,maxSeconds=limit,gain=gain,gust=true)
    private val groups09=listOf(
        Group("AI","Air · Hava","Normal projectile çıkışı için kısa hava hareketleri. Büyük Air Slash ability kaydı değil; rüzgâr kayıtlarının farklı kesit/perde düzenlemeleri.",listOf(
            c("Keskin, kısa hava itişi",air(1.3).copy(maxSeconds=.32,gust=true)),
            c("Doğal rüzgâr püskürtüsü",wind("short-wind",1.3,.4,450.0,.4)),
            c("İnce mistik hava geçişi",wind("wind-loop",1.2,.6,1100.0,.38))
        )),
        Group("WA","Water · Su","Su kayıtlarından kısa sıvı projectile dokuları. Bir dalganın sürekli loop'u veya her isabette çalan ayrı efekt değil.",listOf(
            c("Kısa su bırakışı",tiny("water-drop-01").copy(maxSeconds=.35,transient=1)),
            c("Akışkan su hamlesi",tiny("water-pour-01",1.2).copy(maxSeconds=.4,transient=1,gust=true)),
            c("Su + hafif hava hareketi",tiny("water-drop-03",.85).copy(maxSeconds=.4,transient=1),air(1.5).copy(maxSeconds=.28,gain=.2,gust=true))
        )),
        Group("FI","Fire · Ateş","Rubberduck'ın ayrı fire spell kayıtlarından kısa saldırı kesitleri. Normal atışlar için büyük patlamalar yerine kompakt başlangıçlar.",listOf(
            c("Kısa alev hamlesi",magic("spell_fire_01")),
            c("Kuru, çatırdayan ateş çıkışı",magic("spell_fire_03")),
            c("Daha tok alev atışı",magic("spell_fire_05",.85,.5))
        )),
        Group("AC","Acid · Asit","Islak/fışkıran ve daha keskin tıslayan tasarımlar. Slime, su ve basınçlı hava katmanları; gerçek asit kaydı oldukları iddia edilmiyor.",listOf(
            c("Islak asit sıçraması",magic("creature_slime_01",1.2,.36)),
            c("Keskin sıvı püskürtüsü",tiny("compressed-air-spray-01",1.15).copy(maxSeconds=.32,transient=1,gust=true),tiny("water-drop-02").copy(maxSeconds=.25,gain=.4,transient=1)),
            c("Tok, köpüklü asit",magic("creature_slime_03",.85,.4),tiny("compressed-air-spray-02",1.3,gain=.3).copy(maxSeconds=.25,transient=1,gust=true))
        )),
        Group("NE","Necrotic · Nekrotik","Karanlık büyü hissi veren pes ve soyut çıkışlar. Gerçek insan fısıltısı değil; spell ve rüzgâr kayıtlarının işlenmiş kısa kesitleri.",listOf(
            c("Pes karanlık büyü darbesi",magic("spell_01",.65,.45)),
            c("Boğuk mistik enerji",Layer("magic-spells","magical_1.ogg",speed=.65,maxSeconds=.5,lowPass=1300.0,gust=true)),
            c("Uğultulu karanlık çıkış",magic("spell_02",.7,.45),wind("wind-loop",.8,.4,450.0,.4).copy(gain=.35))
        )),
        Group("LI","Lightning · Yıldırım","Kısa elektrik boşalmaları. Uzun gök gürültüsü veya büyük ability patlaması yerine normal projectile başlangıcı.",listOf(
            c("Kuru elektrik şaklaması",tiny("paralyzer-discharge-01").copy(maxSeconds=.35,transient=1,gust=true)),
            c("İnce elektrik çatırdaması",tiny("paralyzer-discharge-02",1.15).copy(maxSeconds=.35,transient=1,gust=true)),
            c("Soyut büyülü elektrik",Layer("magic-spells","electricspell.ogg",maxSeconds=.4,transient=1,gust=true))
        )),
        Group("PO","Poison · Zehir","Asitten daha boğuk, yapışkan ve yumuşak tıslayan adaylar. Slime ve hava katmanlarından tasarımlar; gerçek zehir sesi değil.",listOf(
            c("Boğuk zehir damlası",magic("creature_slime_02",.8,.4).copy(lowPass=1000.0)),
            c("Gazımsı zehir püskürtüsü",tiny("compressed-air-spray-02",.85).copy(maxSeconds=.4,lowPass=1300.0,transient=1,gust=true)),
            c("Yapışkan, mistik zehir atışı",magic("creature_slime_04",.9,.4).copy(lowPass=1600.0),magic("spell_02",.8,.3,.2))
        ))
    )

    private fun waterJet(n:Int,speed:Double=1.0,gain:Double=1.0)=Layer("water-splash",
        "splash_${n.toString().padStart(2,'0')}.ogg",gain=gain,speed=speed,maxSeconds=.55,transient=1,gust=true)
    private fun flame(speed:Double=1.0,gain:Double=1.0)=Layer("fire-evil","spells/Fire.aif",
        speed=speed,gain=gain,maxSeconds=.6,transient=1,gust=true)
    private fun curse(n:Int,speed:Double=1.0)=Layer("dark-magic","dark_magic/fout-0$n.flac",
        speed=speed,maxSeconds=.65,transient=1,gust=true)
    private val groups10=listOf(
        Group("WV","Water · Su savurma / su bükme hissi","Önceki WA1–3 kullanılmıyor. Damla yerine yeni splash ve dalga kayıtları, hava hareketiyle birleştirildi. Avatar kaydı veya birebir taklidi yok; hedef akışkan, kuvvetli su savurma hissi. Altı özgün ham kayıt değil; kaynaklar ve varyasyonlar etiketli.",listOf(
            c("Kısa su kamçısı",waterJet(4,1.15),air(1.3).copy(maxSeconds=.35,gain=.3,gust=true)),
            c("Geniş su savurması",waterJet(9,.9),wind("short-wind",1.25,.4,650.0,.45).copy(gain=.3)),
            c("Basınçlı su yumruğu",waterJet(12,1.1),air(1.4).copy(maxSeconds=.3,gain=.5,gust=true)),
            c("Kalın dalga geçişi",Layer("ocean-splash","cannon_miss.ogg",speed=1.3,maxSeconds=.65,transient=1,gust=true),air(1.2).copy(maxSeconds=.4,gain=.25,gust=true)),
            c("Dönen su şeridi",waterJet(6,1.1),waterJet(14,1.2,.55).copy(offset=.07),wind("wind-loop",1.4,.6,600.0,.4).copy(gain=.2)),
            c("Su çekişi ve ileri fırlatma",waterJet(8,1.2,.5).copy(reverse=true,maxSeconds=.18),waterJet(11,.95).copy(offset=.12),air(1.2).copy(offset=.1,maxSeconds=.35,gain=.3,gust=true))
        )),
        Group("FL","Fire · Alev savurma / ateş bükme hissi","Önceki FI1–3 kullanılmıyor. Yeni Fire kaydından basınçlı alev çıkışları; hava katmanı, hız/perde ve filtre varyasyonları. Büyük patlama veya ocak çıtırtısı yerine ileri savrulan alev hissi. Fire kaynağı ücretsiz CC BY 3.0; seçilirse artisticdude atfı oyuna eklenecek.",listOf(
            c("Keskin alev yumruğu",flame(1.2),air(1.35).copy(maxSeconds=.3,gain=.4,gust=true)),
            c("Tok, geniş alev savurma",flame(.8),wind("short-wind",1.1,.4,400.0,.5).copy(gain=.4)),
            c("Kısa basınçlı alev jeti",flame(1.45).copy(maxSeconds=.4),tiny("compressed-air-spray-01",1.2,gain=.25).copy(maxSeconds=.25,transient=1,gust=true)),
            c("Alev kamçısı",flame(1.05),air(1.15).copy(maxSeconds=.5,gain=.65,highPass=450.0,gust=true)),
            c("Derin alev dalgası",flame(.7).copy(lowPass=2200.0,maxSeconds=.65),air(.9).copy(maxSeconds=.45,gain=.35,gust=true)),
            c("Hızlı, kızgın hava kesişi",flame(1.3).copy(highPass=350.0,maxSeconds=.45),wind("wind-loop",1.6,.6,1200.0,.35).copy(gain=.4))
        )),
        Group("CU","Necrotic · Lanet / tekinsiz enerji","Önceki NE1–3 ve bunların spell kaynakları kullanılmıyor. Yeni qubodup dark-magic kayıtlarından kısa, rahatsız edici soyut dokular. Gerçek insan fısıltısı veya konuşma değil; ters çevirme/perde/filtre katmanları etiketli. Önce düşük sesle dinle.",listOf(
            c("Kuru, tekinsiz lanet · kayıt 1",curse(1)),
            c("İç burkan lanet · kayıt 2",curse(2,.9)),
            c("Yabancı, karanlık titreşim · kayıt 3",curse(3,.85)),
            c("Ters akan lanet",curse(1,.8).copy(reverse=true,highPass=180.0)),
            c("Çift katmanlı huzursuzluk",curse(2,.75),curse(3,1.1).copy(offset=.035,gain=.35,reverse=true)),
            c("Boğuk lanet ve tiz gölge",curse(3,.65).copy(lowPass=1500.0),curse(1,1.3).copy(gain=.25,highPass=900.0,reverse=true))
        ))
    )

    /** Exact selected PCM, with only the approved repetition arrangements. */
    private fun installAbilities() {
        fun pcm(pack:String,id:String):FloatArray = AudioSystem.getAudioInputStream(File(root,"listening-pack-$pack/$id.wav")).use {
            val data=ByteBuffer.wrap(it.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            FloatArray(data.remaining()){data.get()/32768f}
        }
        val out=File("assets/audio/sfx")
        fun save(name:String,pcm:FloatArray) { wav(File(out,"$name.wav"),pcm);println("$name: ${pcm.size*1000L/RATE} ms") }
        fun repeated(source:FloatArray,count:Int,gap:Double):FloatArray {
            val result=FloatArray(source.size+((count-1)*gap*RATE).toInt())
            repeat(count){n->val first=(n*gap*RATE).toInt();source.forEachIndexed{i,v->result[first+i]+=v}}
            val peak=result.maxOf{abs(it)};if(peak>.25f)result.indices.forEach{result[it]*=.25f/peak}
            return result
        }
        // Double Swords now reuses normal cues; never recreate obsolete rapid_strokes_N assets.
        val spin=repeated(pcm("06","SP1"),3,.10)
        val fitted=if(spin.size<=RATE*.42)spin else FloatArray((RATE*.42).toInt()){i->
            val pos=i.toDouble()*(spin.size-1)/(RATE*.42-1);val a=pos.toInt();val f=(pos-a).toFloat()
            spin[a]*(1-f)+spin[minOf(a+1,spin.lastIndex)]*f
        }
        save("spin_attack",fitted)
        File(root,"listening-pack-06/SB5.wav").copyTo(File(out,"defensive_burst.wav"),overwrite=true)
        File(root,"listening-pack-07/AW5.wav").copyTo(File(out,"air_slash.wav"),overwrite=true)
        save("earthbreaker_wave",render(c("Single EB1 wave",thwack(6),hit("impactMining_000",.3,.45,.025))))
        val sweep=pcm("06","WS2");save("whip_sweep",sweep+sweep)
        listOf("bow_volley" to "BV2", "sling_impact" to "ST1", "boomerang_throw" to "GH3").forEach{(name,id)->
            File(root,"listening-pack-08/$id.wav").copyTo(File(out,"$name.wav"),overwrite=true)
            AudioSystem.getAudioInputStream(File(out,"$name.wav")).use{println("$name: ${it.frameLength*1000L/RATE} ms")}
        }
        listOf("defensive_burst","air_slash").forEach{name->AudioSystem.getAudioInputStream(File(out,"$name.wav")).use{
            println("$name: ${it.frameLength*1000L/RATE} ms")
        }}
        InstallMagicNormalAudio.main(emptyArray())
    }

    private fun decode(layer:Layer):FloatArray {
        val name=if(File(layer.file).extension.isNotEmpty())layer.file else "${layer.file}.ogg"
        val path=File(root,"sources/${layer.pack}/Audio/$name")
        require(path.isFile){"Missing source: $path"}
        val raw:ByteArray;val rate:Int;val channels:Int
        if(path.extension!="ogg") {
            AudioSystem.getAudioInputStream(path).use { original ->
                val format=AudioFormat(AudioFormat.Encoding.PCM_SIGNED,original.format.sampleRate,16,original.format.channels,original.format.channels*2,original.format.sampleRate,false)
                // FLAC SPI preserves source bit depth; decode 24-bit first, then convert PCM to 16-bit.
                val pcmSource=if(original.format.encoding.toString()=="FLAC") {
                    val bits=original.format.sampleSizeInBits
                    AudioSystem.getAudioInputStream(AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        original.format.sampleRate,bits,original.format.channels,
                        original.format.channels*((bits+7)/8),original.format.sampleRate,false),original)
                } else original
                AudioSystem.getAudioInputStream(format,pcmSource).use {input ->
                    rate=input.format.sampleRate.toInt();channels=input.format.channels;raw=input.readAllBytes()
                }
            }
        } else OggInputStream(path.inputStream()).use { input ->
            rate=input.sampleRate;channels=input.channels
            raw=ByteArrayOutputStream().also { output ->
                val buffer=ByteArray(8192)
                while(true){val n=input.read(buffer);if(n<0)break;if(n>0)output.write(buffer,0,n)}
            }.toByteArray()
        }
        val count=raw.size/(2*channels)
        var mono=FloatArray(count)
        val shorts=ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        repeat(count){frame -> var v=0f;repeat(channels){v+=shorts.get()/32768f};mono[frame]=v/channels}
        if(layer.startSeconds>0.0) {
            val first=(layer.startSeconds*rate).toInt()
            require(first<mono.size){"Requested start is outside ${layer.file}"}
            mono=mono.copyOfRange(first,mono.size)
        }
        if(layer.transient>0) {
            val regions=regions(mono,rate)
            require(layer.transient<=regions.size){"${layer.file}: requested ${layer.transient}, found ${regions.size} transients"}
            val region=regions[layer.transient-1]
            mono=mono.copyOfRange(maxOf(0,region.first-(rate*.015).toInt()),minOf(mono.size,region.last+(rate*.08).toInt()))
        }
        if(layer.reverse) mono.reverse()
        val size=minOf((mono.size.toDouble()*RATE/rate/layer.speed).toInt(),(layer.maxSeconds*RATE).toInt())
        val result=FloatArray(size){ frame ->
            val pos=frame.toDouble()*rate/RATE*layer.speed;val idx=pos.toInt().coerceAtMost(count-1)
            val frac=(pos-idx).toFloat()
            (mono[idx.coerceAtMost(mono.lastIndex)]*(1-frac)+mono[minOf(idx+1,mono.lastIndex)]*frac)*layer.gain.toFloat()
        }
        if(layer.lowPass>0.0) {
            val alpha=(1-kotlin.math.exp(-2*Math.PI*layer.lowPass/RATE)).toFloat();var previous=0f
            result.indices.forEach{previous+=alpha*(result[it]-previous);result[it]=previous}
        }
        if(layer.highPass>0.0) {
            val alpha=(1-kotlin.math.exp(-2*Math.PI*layer.highPass/RATE)).toFloat();var low=0f
            result.indices.forEach {val original=result[it];low+=alpha*(original-low);result[it]=original-low}
        }
        if(layer.gust) {
            result.indices.forEach {
                val t=it.toFloat()/result.lastIndex.coerceAtLeast(1)
                val envelope=if(t<.04f)t/.04f else Math.pow(((1f-t)/.96f).coerceAtLeast(0f).toDouble(),1.6).toFloat()
                result[it]*=envelope
            }
        }
        return result
    }
    private fun regions(pcm:FloatArray,rate:Int):List<IntRange> {
        val threshold=(pcm.maxOfOrNull{abs(it)}?:0f)*.10f
        val hop=maxOf(1,rate/100);val result=mutableListOf<IntRange>()
        var start=-1;var last=-1
        for(i in pcm.indices step hop) {
            val active=(i until minOf(i+hop,pcm.size)).any{abs(pcm[it])>=threshold}
            if(active) {if(start<0)start=i;last=minOf(i+hop,pcm.size)-1}
            if(start>=0 && i-last>rate*.25) {result+=start..last;start=-1}
        }
        if(start>=0)result+=start..last
        return result
    }
    internal fun render(candidate:Candidate):FloatArray {
        val layers=candidate.layers.map { it to decode(it) }
        val length=layers.maxOf { (it.first.offset*RATE).toInt()+it.second.size }
        val result=FloatArray(length)
        layers.forEach{(layer,pcm)->val start=(layer.offset*RATE).toInt();pcm.forEachIndexed{n,v->result[start+n]+=v}}
        // 5ms end fade avoids cuts clicking. Peak matching is NOT loudness normalization.
        val fade=minOf(220,result.size/2)
        repeat(fade){n->result[result.lastIndex-n]*=n.toFloat()/fade}
        val peak=result.maxOfOrNull{abs(it)}?:0f
        require(peak>0f){"Silent candidate"}
        val gain=.25f/peak
        result.indices.forEach{result[it]*=gain}
        return result
    }
    internal fun wav(file:File,pcm:FloatArray) {
        val data=ByteBuffer.allocate(44+pcm.size*2).order(ByteOrder.LITTLE_ENDIAN)
        data.put("RIFF".toByteArray());data.putInt(36+pcm.size*2);data.put("WAVEfmt ".toByteArray())
        data.putInt(16);data.putShort(1);data.putShort(1);data.putInt(RATE);data.putInt(RATE*2);data.putShort(2);data.putShort(16)
        data.put("data".toByteArray());data.putInt(pcm.size*2)
        pcm.forEach{data.putShort((it.coerceIn(-1f,1f)*32767f).roundToInt().toShort())}
        file.writeBytes(data.array())
        AudioSystem.getAudioInputStream(file).use {check(it.format.sampleRate==RATE.toFloat()&&it.frameLength==pcm.size.toLong())}
    }
    private fun esc(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace("\"","&quot;")
    private fun source(layer:Layer)="${layer.pack}/Audio/"+if(File(layer.file).extension.isNotEmpty())layer.file else "${layer.file}.ogg"
    @JvmStatic fun main(args:Array<String>) {
        if(args.contains("--install-abilities")) {installAbilities();return}
        if(args.contains("--inspect-ranged")) {
            listOf("Crossbow Shoot.wav","English Longbow Shoot.wav","Scythian Recurve Shoot.wav").forEach {file ->
                val pcm=decode(Layer("stillnorth-textures",file,maxSeconds=300.0))
                println("$file ${pcm.size.toDouble()/RATE}s: "+regions(pcm,RATE).map{"${it.first.toDouble()/RATE}..${it.last.toDouble()/RATE}"})
            }
            return
        }
        val tenth=args.contains("--pack10")
        val ninth=args.contains("--pack09")
        val eighth=args.contains("--pack08")
        val seventh=args.contains("--pack07")
        val sixth=args.contains("--pack06")
        val fifth=args.contains("--pack05")
        val fourth=args.contains("--pack04")
        val third=args.contains("--pack03")
        val second=args.contains("--pack02")
        val packNumber=if(tenth)"10" else if(ninth)"09" else if(eighth)"08" else if(seventh)"07" else if(sixth)"06" else if(fifth)"05" else if(fourth)"04" else if(third)"03" else if(second)"02" else "01"
        val groups=if(tenth)groups10 else if(ninth)groups09 else if(eighth)groups08 else if(seventh)groups07 else if(sixth)groups06 else if(fifth)groups05 else if(fourth)groups04 else if(third)groups03 else if(second)groups02 else groups01
        val candidateCount=groups.sumOf{it.candidates.size}
        val out=File(root,"listening-pack-$packNumber")
        out.mkdirs()
        val manifest=StringBuilder("GRISILITH · DİNLEME SEÇKİSİ $packNumber\n$candidateCount aday / ${groups.size} grup. Bu seçkinin yeni adayları henüz oyuna eklenmedi.\n\n")
        val sections=StringBuilder()
        groups.forEach {group ->
            val tracks=group.candidates.map(::render)
            val upgradedTracks=group.candidates.map {candidate -> candidate.upgradedLayers?.let{render(Candidate(candidate.label,it))} }
            val slotSeconds=max(3,ceil((tracks+upgradedTracks.filterNotNull()).maxOf{it.size}.toDouble()/RATE+1.1).toInt())
            val sequence=FloatArray(RATE*(slotSeconds*group.candidates.size*2+1))
            manifest.append("${group.id}: ${group.title}\n${group.note}\n")
            val pattern=if(upgradedTracks.any{it!=null})"Her numarada önce normal, ardından gelişmiş sürüm" else "Her aday iki kez"
            sections.append("<section><h2>${group.id} · ${esc(group.title)}</h2><p>${esc(group.note)}</p><p>Toplu dinleme: 1 → ${group.candidates.size}; $pattern, $slotSeconds saniyelik aralıklarla.</p><audio controls preload='none' src='${group.id}-all.wav'></audio><div class='choices'>")
            tracks.forEachIndexed {n,pcm ->
                val id="${group.id}${n+1}";val candidate=group.candidates[n]
                wav(File(out,"$id.wav"),pcm)
                val upgraded=upgradedTracks[n]
                if(upgraded!=null)wav(File(out,"$id-upgraded.wav"),upgraded)
                repeat(2){rep -> val start=RATE*(1+(n*2+rep)*slotSeconds);(if(rep==1)upgraded?:pcm else pcm).copyInto(sequence,start)}
                manifest.append("$id — ${candidate.label}\n  Kaynaklar: ${candidate.layers.joinToString{source(it)}}\n  Toplu kayıtta: ${1+n*2*slotSeconds}s ve ${1+(n*2+1)*slotSeconds}s${if(upgraded!=null)" (ikinci ses gelişmiş ikili)" else ""}\n")
                val extra=if(upgraded!=null)"<small>Gelişmiş sürüm</small><audio controls preload='none' src='$id-upgraded.wav'></audio>" else ""
                sections.append("<article><label><input type='radio' name='${group.id}' value='$id'> <strong>$id</strong> ${esc(candidate.label)}</label><audio controls preload='none' src='$id.wav'></audio>$extra<small>${candidate.layers.joinToString(" + "){esc(source(it))}}</small></article>")
            }
            wav(File(out,"${group.id}-all.wav"),sequence)
            sections.append("</div></section>");manifest.append("\n")
        }
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        val approved=if(sixth)"<section><h2>Normal saldırı seçimleri tamamlandı</h2><p>Warhammer TH3, Bow BW3, Crossbow CB4, Dagger DG4, Sling SL1 ve Boomerang BR3 de oyuna bağlandı. Aşağıdaki 30 ability adayı ise yalnızca dinleme taslağıdır; henüz oyuna eklenmedi. Ranged ability ve Spell Focus seçimleri sonraki turlarda gelecek.</p></section>" else if(third||fourth||fifth)"<section><h2>Güncel durum · oyuna bağlandı</h2><p>Longsword G1 · Double Swords DS1 (tekli/ikili) · Lance LN1 · Shield SH1 · Whip WP5 · Warhammer TH3 · Bow BW3 · Crossbow CB4 · Dagger DG4 · Sling SL1 · Boomerang BR3. Diğerleri yalnızca arşiv adayıdır. <a href='../listening-pack-06/DINLE.html'>Sıradaki seçim: melee ability sesleri</a>.</p></section>" else ""
        val selectionStatus=if(third||fourth||fifth)"Dinleme arşivi: seçilip oyuna bağlanan adaylar aşağıda belirtilir. Diğerleri oyuna eklenmedi." else "Bu seçkinin yeni adayları henüz oyuna eklenmedi."
        val footer=if(fourth)"Kaynaklar: <a href='https://opengameart.org/content/medieval-sound-effects-weapon-textures'>Still North Media · Weapon Textures</a>, <a href='https://opengameart.org/content/swishes-sound-pack'>artisticdude · Swishes</a>, <a href='https://opengameart.org/content/fantasy-sound-effects-tinysized-sfx'>Vehicle · Tinysized</a>, <a href='https://kenney.nl/assets/impact-sounds'>Kenney · Impact Sounds</a>. Bu seçkinin kaynakları CC0. Tasarım varyasyonları etiketlidir; ayrıntılar LICENSE-SOURCES.txt içinde." else if(third)"Kaynaklar: <a href='https://opengameart.org/content/swishes-sound-pack'>artisticdude · Swishes (CC0)</a>, <a href='https://opengameart.org/content/fantasy-sound-effects-tinysized-sfx'>Vehicle · Tinysized (CC0)</a>, <a href='https://opengameart.org/content/whip-sound'>Tuomo Untinen / Reemax · Whip (CC BY 4.0)</a>. Whip ücretsizdir ama atıf gerektirir. Kaynak ve değişiklik bilgileri LICENSE-SOURCES.txt içinde. Varyasyonlar ve katmanlı taslaklar etiketlidir." else "Kaynaklar: <a href='https://kenney.nl/assets/interface-sounds'>Kenney Interface Sounds</a>, <a href='https://kenney.nl/assets/rpg-audio'>RPG Audio</a>, <a href='https://kenney.nl/assets/impact-sounds'>Impact Sounds</a>. Tamamı CC0. Lisans dosyaları bu klasöre kopyalandı. Katmanlanmış taslaklar yukarıda belirtilir."
        val html="""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Ses seçkisi $packNumber</title>
<style>body{margin:0;background:#17191c;color:#eceae4;font:16px/1.5 system-ui,sans-serif}main{max-width:1100px;margin:auto;padding:32px 22px}h1{font-size:28px}h2{font-size:22px;color:#e6c482}p{color:#c8c8c8;max-width:900px}section{padding:24px 0;border-top:1px solid #45474a;margin-top:24px}.choices{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:14px;margin-top:20px}article{background:#24272b;padding:14px;border-radius:6px}label{display:block;min-height:48px;cursor:pointer}audio{display:block;width:100%;max-width:540px;margin:12px 0}small{display:block;color:#aeb2b8;overflow-wrap:anywhere}button{background:#e6c482;border:0;padding:12px 18px;font-weight:600;cursor:pointer}textarea{width:100%;height:75px;background:#24272b;color:#eee;margin:12px 0;border:1px solid #555}a{color:#e6c482}</style>
<main><h1>Grisilith · Ses seçkisi $packNumber</h1><p>$candidateCount aday · $selectionStatus Her bölümden birini seçebilir veya hepsini reddedebilirsin. Toplu kayıtların dinleme düzeni her bölümde belirtilir. Tekil oynatıcılardan da karşılaştırabilirsin.</p><p>Önce cihazın sesini düşük tut. Önizlemeler mono, 44.1 kHz / 16-bit WAV; tepe seviyeleri yaklaşık −12 dBFS'ye eşitlendi. Bu, algısal ses yüksekliği eşitlemesi veya son oyun miksajı değildir. Kısa kesmelerde 5 ms son geçiş uygulanmıştır. Katmanlanmış taslakların kaynakları her adayın altında belirtilir.</p>
<p>Menü müziği tercihi: Gregorian Chant — Kevin MacLeod. Bu dosyalara müzik eklenmedi.</p>
$approved
${if(tenth)"<section><h2>Kararlar bekletiliyor · oyuna hiçbir şey eklenmedi</h2><p>Seçki 09'dan AI3, AC2, LI3 ve PO3 onaylandı; tüm seçimler tamamlanınca eklenecek. Eski Water/Fire/Necrotic adayları reddedildi.</p><p>18 yeni aday: WV1–6, FL1–6, CU1–6. Avatar kaydı yok. Katmanlar ve varyasyonlar etiketli. <a href='LICENSE-SOURCES.txt'>Kaynaklar / ücretsiz lisanslar</a>.</p></section>" else ""}
${if(ninth)"<section><h2>Yalnızca dinleme · henüz oyun değişikliği yok</h2><p>Sesler silaha değil projectile'ın güncel magical hasar tipine bağlanacak. Chaos Theory dönüşümünde görsel ve ses de yeni tipe uyacak. Mekanikler henüz değiştirilmedi.</p><p>21 aday / 7 hasar tipi. Her grupta 3 seçenek; katman ve hız/perde varyasyonları açıkça belirtilir. <a href='LICENSE-SOURCES.txt'>Ücretsiz CC0 kaynaklar ve düzenlemeler</a>.</p></section>" else ""}
${if(eighth)"<section><h2>Melee ability sesleri oyuna eklendi</h2><p>AW5 Air Slash · DS1 dokulu Rapid Strokes · SP1 üç tekrar · SB5 · EB1 dalga başına tek patlama · WS2 boşluksuz iki tekrar. Aşağıdaki ranged ve ıslık adayları henüz oyuna eklenmedi.</p><p>Bu paketin kaynakları ve lisansları: <a href='LICENSE-SOURCES.txt'>LICENSE-SOURCES.txt</a>. Islıklar dklon CC BY 3.0, diğer kaynaklar CC0.</p></section>" else ""}
${if(seventh)"<section><h2>Diğer ability kararları yalnızca not alındı</h2><p>Rapid Strokes: mevcut DS1 dokusu, gerçek saldırı sayısı kadar. Spin Attack: SP1, animasyona uygun üç tekrar. Defensive Burst: SB5. Earthbreaker: EB1, dalga başına bir patlama. Whip Sweep: WS2 (SW2 yazımı), aralıksız iki tekrar. Bunların hiçbiri henüz oyuna uygulanmadı.</p></section>" else ""}
$sections
<section><h2>Tercihlerini tek mesajda paylaş</h2><button id="summary">Seçim özetini oluştur</button><textarea id="result" readonly placeholder="Her bölümden bir seçim yap veya beğenmediğin grupları ayrıca yaz."></textarea></section>
<footer><p>${if(seventh)"Kaynaklar: <a href='https://opengameart.org/content/air-woosh-move'>Almitory · Air Woosh Move</a>, <a href='https://opengameart.org/content/short-wind-sound'>remaxim · Short wind sound</a>, <a href='https://opengameart.org/content/wind-whoosh-loop'>SketchMan3 · wind whoosh loop</a>. Üçü de CC0. Kesit, tiz vurgu ve zarf düzenlemeleri LICENSE-SOURCES.txt içinde belirtilir. Kılıç katmanı kullanılmadı." else if(sixth)"Ability taslakları artisticdude Swishes, Vehicle Tinysized, Kenney RPG/Impact ve Jordan Irwin Thwack (CC0) kayıtlarını kullanır. Whip adaylarında Tuomo Untinen / Reemax Whip (CC BY 4.0) kullanılır. Kaynak bağlantıları, atıflar ve değişiklikler LICENSE-SOURCES.txt dosyasında; aynı kaynakların varyasyonları açıkça etiketlidir." else if(fifth)"Kaynaklar: <a href='https://opengameart.org/content/thwack-sounds'>Jordan Irwin / AntumDeluge · Thwack Sounds (CC0)</a> ve <a href='https://kenney.nl/assets/impact-sounds'>Kenney · Impact Sounds (CC0)</a>. TH4–TH5 katmanlanmış taslaklardır. Lisans ve değişiklik ayrıntıları LICENSE-SOURCES.txt içinde." else footer}</p></footer></main>
<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(b!==a)b.pause()})));document.getElementById('summary').onclick=()=>{document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ')};</script></html>"""
        File(out,"DINLE.html").writeText(if(ninth)html.replace(footer,"Bu seçkideki kaynaklar CC0: rubberduck, JaggedStone, Augmentality, Vehicle, Almitory, remaxim ve SketchMan3. <a href='LICENSE-SOURCES.txt'>Kaynak bağlantıları, lisanslar ve değişiklikler</a>.") else if(eighth)html.replace(footer,"Ranged adayları mevcut onaylı seslerin kaynaklarından türetildi. dklon Whistles: ücretsiz CC BY 3.0, atıf gerekli. <a href='https://opengameart.org/content/whistles'>Islık kaynağı</a> · <a href='LICENSE-SOURCES.txt'>Tüm lisanslar ve değişiklikler</a>.") else html,Charsets.UTF_8)
        if(tenth) {
            File(out,"DINLE.html").writeText(html.replace(footer,"Yeni su ve lanet kaynakları CC0; artisticdude Fire &amp; Evil Spell ücretsiz CC BY 3.0, seçilirse atıf gerektirir. <a href='LICENSE-SOURCES.txt'>Tüm kaynaklar, lisanslar ve düzenlemeler</a>."),Charsets.UTF_8)
            File(root,"magic-refinement-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        }
        else if(ninth) {
            File(root,"magical-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        }
        else if(eighth) {
            File(root,"ranged-ability-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        }
        else if(seventh) {
            File(root,"wind-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        }
        else if(sixth) {
            File(root,"ability-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        }
        else if(fifth) {
            File(root,"hammer-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
            File(root,"sources/thwack/Audio/LICENSE.txt").copyTo(File(out,"LICENSE-thwack.txt"),overwrite=true)
            File(root,"sources/impact-sounds/License.txt").copyTo(File(out,"LICENSE-kenney.txt"),overwrite=true)
        }
        else if(fourth)File(root,"ranged-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        else if(third)File(root,"weapon-source-credits.txt").copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        else listOf("interface-sounds","rpg-audio","impact-sounds").forEach { pack ->
            File(root,"sources/$pack/License.txt").copyTo(File(out,"LICENSE-$pack.txt"),overwrite=true)
        }
        check(out.listFiles()!!.count{it.extension=="wav"}==candidateCount+groups.size+groups.sumOf{g->g.candidates.count{it.upgradedLayers!=null}})
        println("AUDIO_AUDITION_PASS: $candidateCount candidates + ${groups.size} comparisons. WAV headers validated, all candidates non-silent; peak limited to -12 dBFS. $out")
    }
}
