# Ses seçimleri — güncel durum

## Kuruldu — Faz 9f / Seçki 28 müzik kararları

- Crolymnos Forest: normal CF2 / Ascending the Vale, mini boss CF5 / Lord of the Land,
  final boss VS9 / Five Armies.
- Vogtsweier Siege: normal VS1 / Crusade, mini boss VS2 / The Descent,
  final boss VS6 / Hero Down.
- Hollowgrave Depths: normal HD1 / Ossuary 1 - A Beginning, mini boss HD8 / Darkest Child,
  final boss HD10 / Decay.
- Run market: MK2 / Teller of the Tales.

On tam parça birebir kuruldu; tek aktif streaming Music kaynağı kullanılır.
Master × Music düzeyi, Settings preview/discard ve uygulama pause/resume geçerli.
Müzik oyun hızından etkilenmez. Run Pause ve oradan açılan Settings müziği kesmez.
Chest/end-stage stat reward/summary'de müzik susar. Aynı parçaya dönülürse
pause edilen müzik devam eder; farklı map/stage/market parçası önceki stream'i
durdurur/dispose eder. Aynı rota her frame yeniden başlatılmaz. Parçalar tam
kayıt sonunda loop eder; kayıtlar kesilmedi, değiştirilmedi. Atıflar oyun içi
Audio Credits müzik sayfasında ve assets/audio/RUN-MUSIC-LICENSES.txt'de.
Test Map için müzik seçilmediğinden map soundtrack'i yok; market açılırsa MK2 geçerli.

## Faz 9g — Summary müzik seçimi / Seçki 29

DF1–DF20 defeat, VC1–VC20 victory: kırk farklı CC BY 4.0 / Kevin MacLeod adayı.
Kısa victory fanfarları notlarında açıkça işaretli. Resmî katalog bilgilerine
dayalı tarz önerileri; kaynaklar, lisanslar ve hashler seçki klasöründe.
Summary müzikleri henüz oyuna kurulmaz; kullanıcı seçimi beklenecek.

## Faz 9f — Ses davranışı düzeltmeleri / Seçki 28

Enemy konuşma, hurt ve ölüm kayıtlarının oynatma hızı ve tekrar kilitleri
oyun hızından bağımsız. Witch/Vampire konuşma aralığı artık pause hariç
30 normal saniye; 0.5x/2x bunu değiştirmez. Enemy normal saldırı/ability
efektleri (hazırlık/dönüşüm dahil) oyun hızına uymaya devam eder.
Mini ve final boss sesleri ekran dışında da duyulur; oyuncu merkezli stereo
ve Master/SFX geçerli. Diğer dünya kaynaklarının ekran dışı sessizliği korunur.
Pause mevcut efektleri dondurur ve geri dönerken kaldığı yerden sürdürür.
Stage sonu, run sonu, chest, stat reward ve market geçişleri mevcut efektleri
tamamen keser; eski devamlı efektler dönüşte kendiliğinden ses başlatamaz.
Ölüm kuyruğunun Summary'de sürmesi artık geçerli değil. Menü/ödül ekranındaki
yeni UI sesleri engellenmez. Yeni müzikler seçilmedi/oyuna kurulmadı.

Seçki 28: CF1–CF10 Forest, VS1–VS10 Siege, HD1–HD10 Depths,
MK1–MK10 Market. 40 farklı Kevin MacLeod tam MP3 adayı, resmî katalogdan;
CC BY 4.0 / kaynak belirtme şartlı ücretsiz kullanım. Kod, kaynak, hash ve
atıf dökümü listening-pack-28/MUSIC-SOURCES.json ve LICENSE-SOURCES.txt.
Stil notları katalog/enstrüman bilgilerine dayalı aday önerileri, dinleyici onayı değil.
Boss müziği adayları henüz sunulmayacak. Seçim sonrası map müziği run/Pause'da;
chest, end-stage stat reward, market ve boss stage'de çalmayacak. Market için
ayrı müzik; her map'in mini/final boss müziği sonraki seçim turu.

## Kuruldu — Seçki 27 / karakterler ve boss vokalleri

Bu bölüm alttaki eski bekleme kararlarının yerini alır. 53 oyun dosyası kuruldu;
seçimler kaynak kesitlerle birebir, Vampire final ölümü D08 + D01 sırasıyla tek WAV.

- Imbert: S01; H09/H08/H07/H02/H01; D10 (IM27).
- Ywaleth: S01; H01/H06/H08; D10 (YW27).
- Linlos: S01; H03/H01/H06/H07/H09; D01 (LN27).
- Koba: S01; H05/H06/H08/H09/H01; D09 (KB27). Son karar gereği tek rastgele havuz.
- Lavena: S01; H04/H01/H08; D10 (LV27).
- Witch: H02/H04/H03/H06/H10; D07; P01/P02/P03/P05/P07/P09 (WI27).
- Vampire: H02/H05/H08/H09; D08 ardından D01; P01/P02/P03/P08/P06 (VL27).

Hasar havuzları eşit olasılıklı; aynı varlığın sesleri üst üste binmesin diye
en az 0.8 saniye / ses bitimine kadar ortak tekrar koruması var. Ölümcül vuruş
yalnız ölüm sesi çıkarır. Karakter seçim sesi açık preset/karakter seçimiyle
başlar; otomatik kayıt yükleme ve hover sessizdir. Boss replikleri ilk doğuşta
ve hayattayken her 30 oyun saniyesinde başlar (pause hariç, oyun hızı etkili).
Kaynak konumu sürekli güncellenir; ekran dışı sessizlik, stereo, Master/SFX,
oyun hızı ve Summary ölüm kuyruğu korunur. Boss +%35 kuralı devam eder.
Vampire'ın ilk form kaçışı ölüm değildir; iki parçalı kayıt gerçek ölümündedir.
Eski kurulu Witch ölümü ve Vampire hurt/death içerikleri değiştirildi;
eski kurulum aracı bunları geri yazmaz. Kaynak ve dinleme arşivleri korunur.
Tam dosya/kod/hash dökümü: assets/audio/CHARACTER-VOICE-SELECTIONS.json.

## Faz 9e — Kullanıcının uzun kayıtları / Seçki 27

Voices klasöründeki 8 MP3 kesildi; hiçbir ses yeniden üretilmedi veya oyuna
eklenmedi. 7 karakter/enemy için 140 Hurt/Death, Witch/Vampire için 20 Punchline,
beş player karakterinin kayıttaki tek açılış cümlesi: toplam 165 ayrı WAV.
Kodlar IM27H01/IM27D01/IM27S01; YW/LN/KB/LV/WI/VL diğer gruplar.
Başlıklar ve söylenen sıra numaraları çıkarılır. Çok parçalı inleme/kahkaha,
replik içi boşluklar korunur. Ton/perde/hız ve işlenmemiş kaynak içeriği korunur;
yalnız dış sessizlik, güvenli kenar payları ve 2ms uç fade; ek kayıplı sıkıştırma yok.
159 sayı bölgesi bağımsız dar konuşma pencereleriyle kontrol edildi; 6
homofon tanıma sapması geniş bağlamla değerlendirildi. Witch P02'de ayrı sayı
bölgesi tespit edilmedi; Come closer cümlesinin başlangıcı kesilmez.
Orijinaller SHA256 ile korunur, CUTS.json/CSV tam yerleri içerir; dinleme
sayfasındaki kaynak karşılaştırmaları ile kontrol edilebilir. Teknik kontrol
insan tarafından tek tek dinlenmiş estetik kalite onayı olarak sunulmayacak.
Kullanıcı seçiminden önce hiçbir runtime/asset bağlantısı değiştirilmeyecek.

## Faz 9e — Kullanıcının yeni tablosu / Seçki 26

Grisilith_Character_Voice_Prompts.txt: 5 karakter × (10 hurt + 10 death) = 100
satır yeni kodlarla üretildi. Kod şeması IM26H01/IM26D01; YW/LN/KB/LV diğerleri.
Dosyadaki nefessiz yeni yön esas: yeni adaylarda nefes/noise kuyruğu yok;
Lavena hurt minimal kapalı ağız ünlemi, death kısa çığlık.
Koba hurt 01–05 acıdan keyif, 06–10 fazla acı. İki havuz ayrı seçilebilir.
Diğerlerinde 01–05 ana yön, 06–10 alternatif.

Onaylı seçim sesleri IMB5, ORC10, LV3, YW10, LD1 korunur; ses bankaları,
perde ve kimlik işlemleri değişmez. Her karakterin başında önceki seçim sesi
birebir kopya; Koba'nınki Seçki25'teki yeni replikli referanstır.
Kullanıcı önceki hurt/death seçim kodlarını iletmeyi gerekli görmedi;
herhangi bir ek hurt/death adayı seçilmiş varsayılmayacak. Önceki seçkiler silinmez.
Tablodaki parantezler oyunculuk hedefi/veri; seslendirilmez. Kokoro bu
komutları doğrudan uygulayamaz; fonetik ünlem, tempo ve zarf taslağı olarak
yaklaşılır, gerçek oyunculuk/algısal aynı ses garantisi verilmez.
Henüz hiçbir karakter sesi gameplay veya assets/audio'ya kurulmayacak.

## Faz 9e — Beş seçim sesi onaylı, hasar/ölüm seçimi / Seçki 25

Onaylar: IMB5 Sir Imbert, ORC10 Koba, LV3 Lavena Vol, YW10 Ywaleth Yoster,
LD1 Linlos Dustman. HENÜZ HİÇBİRİNİ OYUNA EKLEME; tümü seçimde bekliyor.
Koba'nın yeni repliği aynen: "On behalf of all the outcasts and who have been
called monsters... I will show them the REAL MONSTER!"
Bu tur yeni replik yalnız önizleme; eski ORC10 dosyası korunur.

Seçki 25: karakter başına 10 hasar ve 10 ölüm; Koba'da ek olarak ayrı 10
acı-fazla-geldi hasar örneği. Toplam 110 aday, beş tarzın ikişer varyantı;
55 ikili dinleme karşılaştırması. Karaktere göre gruplu, seçim sesi en başta.
Koba KBH keyif alma, KBP rahatsız olma; sonraki kurulumda dönüşümlü kullanılacak.
IMH/IMD Imbert, YWH/YWD Ywaleth, LNH/LND Linlos, LVH/LVD Lavena, KBD Koba ölüm.
Onaylanan ses bankaları, perde ve kimlik işlemleri aynı; yeni ses bankası yok.
Sözsüz ünlemler sentetik; gerçek çığlık/acı/kahkaha oyunculuğu garanti değil.
Nefesler seçili sesin spektral renginden tasarlanmış gürültü dokusudur, insanın
gerçek nefes kaydı değildir. Lavena hasar örnekleri yalnız nefes içerir.
Bu tur runtime, gameplay bağlantıları ve assets/audio dosyaları değiştirilmeyecek.

## Faz 9e — Karakter seçimleri bekletiliyor / Seçki 24

LV3 (Lavena Vol), YW10 (Ywaleth Yoster), LD1 (Linlos Dustman) SEÇİLDİ.
Kullanıcının açık talimatı: HENÜZ OYUNA EKLEME. WAV'lar Seçki 23'te korunuyor.
Sir Imbert ve Koba için önceki seçenekler onaylanmadı.
Yeni yön: Imbert 50'lerinde, kalın ve asker sertliğinde; birkaç bağırış hedefli
varyant. Koba korkutucu, çok kalın ve gerçek anlamda ork/barbar tınısı.
Seçki 24 IMB1–10, ORC1–10; yeni sesler de yalnız dinleme, kurulum yok.
Sentetik motorun yaş/bağırış kontrolü sınırlı; bağırış taslakları ve yaratık
işlemesi dürüstçe etiketlenir. Hasar/ölüm sesleri henüz seçilmiyor.

## Uygulandı — Seçki 22 / Enemy turu kapandı; Faz 9e dinleme aşaması

Kullanıcının SL4 yazımı SLS4 olarak alındı. SLS4 Sling gelişmiş taş
parçalanmasında bir kez; BRC5 Boomerang'ın gerçek son yakalamasında bir kez
(gelişmiş normalin ara geçişinde değil); SR5 Shield maksimum birikime ilk
ulaştığında çalar. BRH2 Bear, GRH1 Gorilla, SPH1 Spearman, HOH3 Horseman
pozitif ve ölümcül olmayan hasarda, düşman başına en fazla .8 saniyede bir.
Vampire mevcut VP5 ve .7 saniye sınırını korur. Boss gain +%35 korunur.
Bee Swarm, Crocodile, Witch, Ghost ve Slime için hurt vokali EKLENMEYECEK.
Bu karar resist/ability/ölüm efektlerini kaldırmaz. Normal/unique hurt eklenmedi.
Yedi kurulu WAV seçilenlerle birebir, lisanslar FINISHING-LICENSES.txt içinde.

Seçki 23: 5 karakter için 10'ar sentetik seçim repliği, toplam 50 aday;
10 toplu karşılaştırma WAV'ı ve DINLE.html. Karakter sesleri OYUNA KURULMADI.
IM Sir Imbert; LV Lavena Vol; YW Ywaleth Yoster; KB Koba; LD Linlos Dustman.
Koba MOSTER -> MONSTER düzeltildi. Replik anlamları korunur.
Kokoro yerel ve ücretsiz sentetik model, rol/duygu etiketleri hedef tınıdır;
gerçek aktör performansı veya otantik aksan garantisi değildir.
YW8–10 ve KB8–10 farklı dil ses bankalarının İngilizce okumalarıdır, deneysel
olarak etiketlendi. Model/araç lisansları, her adayın metni, ayarları ve hash'i
seçkide bulunur. Hasar/ölüm adayları seçilen karakter sesine göre sonraki turda.
Finishing, Undead/Vampire ve ekran ses testleri geçti; estetik ses onayı değildir.

Alttaki bekleme kayıtları tarihçedir; bu üst kayıt günceldir.

## Seçki 22 — hazır, henüz seçilmedi / kurulmadı

60 yeni aday, her grupta 5. SLS Sling gelişmiş taş parçalanması; BRC Boomerang
son yakalama; SR Shield maksimum Defensive Line birikimi.
Boss hurt: BRH Bear, BSH Bee Swarm, CRH Crocodile, GRH Gorilla, SPH Spearman,
HOH Horseman, WIH Witch, GHH Ghost, SLH Slime. Vampire Lord VP5 zaten kurulu.
Her boss grubunun başında mevcut ölüm sesi var; bu referans yeniden seçilmeyecek.
Sling/Boomerang/Shield normal ve ability sesleri de birebir referans olarak
eklendi. Boomerang normal/ability aynı dosya; iki etiketle açıkça gösteriliyor.
Vampire Lord VP5/VS5 tamamlanmış referans bölümünde. Toplam 17 mevcut ses kopyası.
Yeni adaylar yalnız tools/audio-audition içinde; core/assets bu tur değiştirilmedi.
Witch hurt kesitleri mevcut xl57 kaydının sessiz başlangıcını atlayarak gerçek
vokal foreground bölgelerinden alındı; kaynak/lisans atıfları seçkide mevcut.
Bağlantı kuralları seçimden sonra: gerçek hasar; boss başına tekrar sınırı;
ölümcül darbede yalnız ölüm önceliği; Bee Swarm resist/hurt aynı hit'te çift değil.
SLS ana taş parçalanmasında bir kez, BRC son yakalamada bir kez, SR maksimuma
ilk ulaşmada bir kez. Normal/unique hit, spawn/adım sesleri istenmedi/eklenmedi.

## Uygulandı — Undead + Vampire Lord + yeni Witch kaydı

Seçki 20: SK3, ZA1, ZD1, GA2, GD1, GC7, LD4, LP5 kuruldu.
LD4 büyük Slime ile tüm küçük Slime parçalarının ortak ölümüdür; LS seçilmedi.
Witch'in bekleyen WD2 seçimi yerine xl57 / Freesound 671201 kullanıldı:
short scream no effects.wav, CC BY 4.0. Kaynak/atıf/değişiklikler CREDITS ve
UNDEAD-VAMPIRE-LICENSES içinde; oyunun Audio Credits sayfasına da eklendi.
Seçki 21: GS9, MA3, MD2, VH5, VB1, ME1, VF5, TP1, LC2, SC5, VS5, VP5 kuruldu.
VS5 tek gerçek son ölüm sesidir; VD grubundan seçim yok, ikinci ölüm sesi çalmaz.
VF5 3.2s forma, TP1 0.75s teleporta, LC2 1s cone charge'a uyarlandı.
Diğer seçilmiş WAV'lar birebir. SC5 kısa bir charge başlangıç vurgusudur;
3/4.5s charge boyunca konuşma tekrarı veya aşırı zaman esnetme yapılmadı.
Ghost ağlaması charge+storm boyunca owner-bound loop, evre bitince/ölümde kapanır.
Hunger yalnız başarılı can çalma hasarında en fazla 1.2s'de bir; Vampire hurt
pozitif ve ölümcül olmayan hasarda en fazla .7s'de bir. İlk form kaçışı ölüm değildir.
Vampire cone hit Longsword, Swipe Around hit Lance; normal isabet vampir ısırığı.
Acidic Path sesi oyuncu/summon condition tick'inde gerçekten HP kaybı varsa çalar;
boss kaynağı snapshot olarak taşınır, canlı enemy referansı tutulmaz.
Boss +%35, Master/SFX, oyun hızı, player-relative stereo, offscreen mute korunur.

Kontrol: 19 enemy türünün tamamında death cue var; Witch/Slime element bazlı
isabet kullanır, diğerleri normal cue eşlemeli. 18 weapon'ın normal/ability
akışları ve 5 summon'ın saldırı/ölüm bağlantıları incelendi. Süre dolumu/despawn
ölüm sesi değildir. Kalan seçim: Vampire dışındaki mini/final boss hasar alma
vokalleri. İsteğe bağlı ayrıntılar: Archer/Skeleton aim uyarısı, Sling parçalanma,
Boomerang yakalama, Shield maksimum stack; bu ayrıntılar henüz eklenmedi.

## Seçki 20 kararları — NOT ALINDI, KURULMAYACAK (kullanıcının sonraki mesajına kadar)

WD2 Witch ölüm; SK3 iki Skeleton ortak ölüm; ZA1/ZD1 Zombie saldırı/ölüm;
GA2/GD1/GC7 Ghost fısıltılı saldırı/ölüm/ability ağlaması;
LD4 Slime ölümü: hem ilk büyük Slime hem de küçükler AYNI sesi kullanacak;
LP5 Acidic Path teması. Bu seçimlerin hiçbiri bu tur core/assets içine eklenmedi.
LS split sesi seçilmedi, kendiliğinden eklenmeyecek.

## Seçki 21 — yalnız yeni öneriler; oyuna kurulum YOK

90 aday, 13 grup. GS1–10 patlamalı/sert Gorilla Stomp; MA1–10 dil/yalama
Mimic saldırısı; MD1–10 tiz+kalın gövdeli canavar ölümü. Önceki GS/MA/MD reddedildi.
Vampire: VH1–5 Hunger, VB1–5 ısırık, ME1–5 Misty Escape, VF1–10 ikinci forma
dönüşüm, TP1–5 teleport, LC1–5 konik kılıç charge, SC1–5 Swipe Around charge,
VS1–5 çığlıklı ölüm alternatifi, VP1–5 erkek hasar alma, VD1–10 son erkek vampir ölümü.
VS / VD alternatif ölüm aileleridir; birlikte çalmaları varsayılmayacak.
Konik saldırı isabeti Longsword; Swipe Around isabeti Lance olacak, bu tur bağlanmadı.
Tahm Kench / Vladimir yalnız estetik referans; ticari oyun sesleri kullanılmadı.
Yeni yardımcı hazırlama kodu ve kaynaklar yalnız tools/audio-audition altındadır;
kurulu ses dosyaları / gameplay kodu değiştirilmeden öneri üretildi.

## Uygulandı — Seçki 19 ve Undead ortak saldırılar

BD4 Bandit ölüm, AD2 Archer ölüm, GD2 Guard SUMMON ölüm, SD1 Spearman ölüm,
ST3 Throw the Spear, HD5 Horseman ölüm, HA1 normal saldırı, HS5 Swipe Around,
MX3 Mimic dönüşümü kuruldu. Guard için bekleyen ölüm seçimi tamamlandı.
MX3 0.85s dönüşüme uyarlandı; diğer sekiz ses birebir kopyadır.
Spearman/Horseman normal ve ability isabet sesleri yalnız gerçek hasarda çalar.
Mimic dönüşümü gerçek disguised->transforming geçişinde bir kez başlar,
aktif evreye geçince veya ölüm/despawn olunca durur.
Skeleton Warrior normal saldırısı Longsword; Skeleton Archer Bow ortak sesidir.
Slime Spit / Acid Projectile gerçek hasar tipinin ortak projectile sesini kullanır.
Acidic Path bu eşlemeye dahil değildir; condition teması için ayrı ses seçilecek.
Mini/final boss +%35, oyuncu merkezli stereo, offscreen/speed/Master/SFX korunur.

## Seçki 20 — HAZIR, önizleme; yeni adaylar henüz kurulmadı

85 aday / 13 grup: WD1–5 yeni cadı çığlıkları; SK1–10 iki iskeletin ortak ölümü;
ZA/ZD1–5 Zombie saldırı/ölüm; GA/GD/GC1–10 Ghost fısıltı/çığlık/ağlama;
LD/LP1–5 Slime ölüm/Acidic Path teması; LS1–5 isteğe bağlı ölümde parçalanma.
GS/MA/MD1–5 önceki seçkiden birebir taşınan henüz seçilmemiş Stomp/Mimic saldırı/ölüm.
ÖNEMLİ: Bu sayfadaki WD/GD/GA/GC kodları Seçki 20'ye aittir;
önceki pack'lerde aynı kısa kodlarla seçilen sesler değiştirilmedi.
Vampire Lord için ses adayı yok; yalnız koddan çıkarılan olay listesi hazırlandı.
Ghost ability: 1.25s hazırlık + 5s Necrotic Storm; ağlama seçimi buna uyarlanacak.
Normal/Unique enemy hasar alma ve doğma sesleri bu tur eklenmedi.

## Uygulandı — Seçki 18 trait sesleri ve tüm enemy hit/boss kuralları

BC1, BP2, CJ2, CP1, EG5, SB3, SS3, RG2, GC3, GT4 kuruldu.
GC3 0.5s hazırlığa uyarlandı; diğer dokuz dosya seçkidekinin birebir kopyası.
Hücum/hazırlık kanalları phase ile başlar-biter, ölüm/despawn/stage clear temizler.
SB3 yalnız gerçek hasarda direnç devreye girdiyse (en fazla 0.5s'de bir);
SS3 yalnız gerçek split oluştuğunda, EG5 yavru gerçekten spawn olunca çalar.
RG2 öfke eşiğinde bir kez; Gorilla hazırlık ve atış ayrı olaydır.
Mini + final boss için TÜM enemy cue'ları (normal hit, trait/ability, ölüm) +%35.
Ortak weapon/büyü dosyaları tek asset olarak kalır; enemy miks tabanı .6,
boss .81; tavanın artışı bozmasını önlemek için shared cue gain'i normalize edilir.
TÜM enemy normal saldırı sesleri gerçek appliedDamage > 0 kapısından geçer;
hazırlık/uyarı/hareket trait sesleri ilgili phase'in sesidir, saldırı isabet sesi değildir.
Bandit Longsword, Archer Bow, Spearman Lance normal sesleri bağlı.
Witch isabeti gerçek damage type'a göre ortak projectile sesini kullanır;
Static Cloud / Meteor / Tsunami / Call Lightning kendi mevcut cue'larını kullanır.
Cloud gibi tekrarlanan hit'ler aynı cue'yu sürekli üst üste başlatmaz.

## Seçki 19 — HAZIR, henüz kurulmadı

65 aday, 13 grup: GS1–5 YENİ ağır Stomp; BD Bandit ölüm; AD Archer ölüm;
GD Guard SUMMON ölümü (bu tur mutlaka seçilecek); SD Spearman ölüm, ST spear throw;
HD Horseman ölüm, HA normal uzun-silah savurma, HS Swipe Around;
WD Witch ölüm; MX Mimic dönüşüm, MA saldırı, MD ölüm (hepsi 1–5).
Normal enemy'lere ability önerisi yok. Witch ability'leri mevcut büyü sesleri, yeniden seçilmiyor.
Horseman'ın eski Battle Cry kod kimliği gerçek çalışma şeklini anlatmıyor:
bu bir çevresel sektör savuruşudur. Mimic D&D SRD'deki nesne taklidi/yapışkan
beden/ısırık kimliğinden sadece ses tasarımı ilhamı aldı; yeni mekanik eklenmedi.

## Uygulandı — Seçki 17 ve oyuncu merkezli stereo

TD5; EA2/ED5; WA5/WD3; BA4/BD2; CA3/CD4; SA3/SD3; RA2/RD4; GA1/GD1.
Toad saldırısı ortak Poison projectile, Guard saldırısı Lance sesi.
Beast normal saldırı cue'ları yalnız FirstScreen'deki gerçek appliedDamage > 0
sonucunda tetiklenir (player/summon/gate); kaçırma, bağışıklık/0 hasar sessizdir.
Bu kural yeni Beast saldırı seslerine uygulandı; mevcut oyuncu/summon projectile
fırlatma sesleri launch anında kalır. Ölümler enemy başına bir defa, mini/final
boss ölüm gain'i normalin 1.35 katı. SFX/Master, hız ve ekran dışı sessizlik ortak.
Stereo referansı artık oyuncudur; kamera merkezine/genişliğine bağlı değildir.
Settings > General > Audio > Directional Stereo, varsayılan ON, kayıt/preview/revert/reset bağlı.
Varsayım: 'boss' = MINI_BOSS ve FINAL_BOSS.
GUARD ÖLÜM SESİ HUMANOID ENEMY TURUNDA MUTLAKA HATIRLATILACAK.

## Seçki 18 — Beast Trait / Ability önizlemeleri HAZIR (henüz eklenmedi)

60 aday: BC1–5 Boar Charge (1–3 1.15s, 4–5 kısa), BP1–5 Boar hazırlık;
CJ1–5 Jump (1–3 .55s, 4–5 kısa), CP1–5 Crocodile hazırlık; EG1–5 yumurta;
SB1–5 Swarm Being tepki, SS1–5 Split; RG1–5 Bear Angry; GC1–5 Gorilla hazırlık
kükremesi, GT1–5 uçan kaya; GS1–10 Stomp the Earth.
'Charge başlamadan önce' isteği iki hayvanı da kapsayacak şekilde iki hazırlık
grubu ile sunuldu; hiçbir trait cue'su otomatik seçilmedi veya runtime'a bağlanmadı.
Swarm Being pasif olduğundan sürekli loop değil hasar azaltma tepkisi önerildi.

## Seçki 17 — Beast normal saldırı/ölüm + Toad ölüm HAZIR

75 aday: TD1–5 Toad ölüm; EA/ED Eagle, WA/WD Wolf, BA/BD Boar,
CA/CD Crocodile, SA/SD Bee Swarm, RA/RD Bear, GA/GD Gorilla (her grupta 1–5).
Kodlar seçki 17'ye özeldir; CA/CD burada önceki Cow grubunu ifade etmez.
Toad saldırısı mevcut Poison sesi olarak SEÇİLDİ, henüz runtime'a eklenmedi.
Guard saldırısı mevcut Lance sesi olarak SEÇİLDİ, henüz runtime'a eklenmedi.
ÖNEMLİ: Guard ölüm sesini humanoid enemy seçim turunda MUTLAKA hatırlat ve seçtir.
Oyuncu merkezli stereo + Audio altında varsayılan açık toggle BEKLİYOR;
kullanıcının önceki 'henüz yapma' talimatı gereği bu tur uygulanmadı.
Oyun assets/ ve core/ dosyalarına bu tur dokunulmadı; yalnız audition araçları/çıktıları.
Sonraki seçim: Boar Charge, Crocodile Jump/Baby Crocodiles, Swarm Being/
Split the Swarm, Grizzly Angry, Gorilla Throw/Stomp the Earth için trait sesleri.
Lisanslar ve gerçek hayvan/taklit/işlenmiş tasarım ayrımları dinleme sayfasında ve
beast-combat-source-credits.txt dosyasında. Normal çağrılardan ölüm tasarımları.

## Uygulandı — Summon saldırı/ölüm ve ortak projectile sesleri

Cow CA5/CD5, Imp ID1, Oathbreaker OA2/OD2 oyuna eklendi (Seçki 16).
Imp saldırısı weapon fire projectile FL5 / magic_fire.wav dosyasını paylaşır.
Onaylı WAV'lar birebir kopyalandı; eski seçimler yeniden tasarlanmadı.
ProjectileSound + GameAudio.projectile ortak API'si tüm ranged normal/ability
atışlarını ve element bazlı sesleri kapsar; magicalShot son hasar tipini çözer.
Yeni kaynağın konumu verilebilir; Settings Master/SFX, oyun hızı ve ekran dışı
sessizlik ortak mikserden gelir. Kaynağa göre ölçülü sağ/sol stereo eklendi.
Ölüm sesi sadece health death içindir; süre dolumu, clear ve silah kaldırma sessiz.
Cow/Oathbreaker efor sesi gerçek saldırı başlangıcında, Imp ateşi projectile oluşunca çalar.
Toad/Guard veya enemy sesleri eklenmedi; enemy aşamasına geçilmedi.
Lisanslar: assets/audio/SUMMON-COMBAT-LICENSES.txt.
Alttaki onay-bekliyor bölümleri önceki turların tarihçesidir.

## Kullanıcı onayı — Mevcut seçim/Singularity seslerini KORU

Kullanıcı mevcut ortak spell seçim sesini ve Singularity seslerini beğendi;
şimdiki gibi kalacaklar. NS1–16 adaylarından seçim yapılmadı; CONFIRM ortak
spell seçim sesi korunacak. Singularity SG3 tabanı ve mevcut bitiş/patlama korunur.
Sıradaki seçim: Cow, Imp, Oathbreaker için beşer saldırı ve ölüm sesi (30 aday).
Bu tur yalnızca dinleme seçkisi; oyun kodu/sesleri değiştirilmeyecek.
Yönlü stereo şimdi eklenmeyecek; sonraki kullanıcı mesajında summon seslerinin
entegrasyonu ile birlikte ele alınacak. Mevcut ekran-dışı görünürlük filtresi korunur.

Seçki 16 HAZIR / ONAY BEKLİYOR: CA1–5 Cow saldırı, CD1–5 Cow ölüm;
IA1–5 Imp saldırı, ID1–5 Imp ölüm; OA1–5 Oathbreaker saldırı, OD1–5 ölüm.
OA kodları burada yalnız Seçki 16 saldırı adaylarıdır; eski Seçki 12 OA1 summon
çağrısı ile karıştırılmayacak. Lisanslar: summon-combat-source-credits.txt.
30 ayrı WAV + 6 grup karşılaştırması; sessizlik/header, benzersizlik ve bağlantılar
kontrol edildi. Bu tur oyun runtime/assetleri ve yönlü stereo değiştirilmedi.

## Son düzeltmeler — Seçki 15 ve ekran dışı sesler

Ability temel kazancı +%35; paylaşılan Dagger/Crossbow/Double Swords/Boomerang
örneklerinde yalnız ability çağrısı güçlenir, normal vuruş aynı kalır.
Singularity sürekli kazancı ayrıca .35 -> .85; nihai ses seviyesi 1.0'da sınırlandırılır.
Onaylı SG3'ten 1.2s çöküş/bitiş, MT4'ten 0.9s final patlaması eklendi;
gerçek Singularity ACTIVE -> COLLAPSE -> EXPLOSION evrelerine bağlandı.

Chalk Dust seçimlerindeki dört ayrı release preview kaldırıldı. NS1–16 seçilene
kadar tüm seçimler aynı mevcut CONFIRM sesini kullanır. Seçki 15: 16 farklı
Piper libritts_r-medium sentetik sesle 'A new spell.' / 'New spell learned.'
Ortak konuşma sesi henüz seçilmedi/oyuna eklenmedi. Bunlar insan aktör kaydı değil.
Kaynak ve atıf ayrıntıları listening-pack-15/LICENSE-SOURCES.txt içinde.

Ekran dışı dünya sesleri duyurulmaz. Efekt alanı görünüyorsa alan sesi duyulabilir.
LWJGL görünmeyen loop'ları sanallaştırır; geri girince oyun hızı/pause/mute dahil
geçen zamandan loop offset'i hesaplanıp OpenAL SEC_OFFSET ile devam edilir.
Seek desteklemeyen backend sessiz ama zaman akışı devam eden kaynağı kullanır.
UI/popup/menü sesleri bu dünya görünürlük filtresinden muaftır. Stereo panning
eklenmedi; bu yalnızca zaman-konumu geri yüklemesidir. Enemy/summon saldırı/ölüm
sesleri bu tur eklenmedi: kullanıcı bu konularda yalnızca fikir istedi.
Kontroller: AudioVisibilitySmoke, FocusAudioSmoke, AudioSmoke, AudioScreenSmoke,
AudioSeekSmoke (gerçek OpenAL seek, tamamen sessiz); son test sonuçları çalışma sonrası.

## Son karar — Spell Focus Ability sesleri UYGULANDI

Kullanıcı düzeltmesi: HS3 değil HM3 seçildi. Nihai casting seçimleri:
HL2 Call Lightning, HM3 Meteor, SV2 Tsunami, SV3 Singularity. HS3 kullanılmadı.
Seçki 11: IG1, ER3, VP4, SC3, AP2, PS4, BL5.
Seçki 12: PX2, AN4, DE1, OA1, CL2, TS3, MT4, SG3.
Toplam 19 oyun WAV'ı eklendi. Aşağıdaki eski bekletme başlıkları artık tarihsel.

Casting sesi gerçek 2.5s çizim tamamlanınca/iptal olunca kesilir; tam SV kaydı
çalınmaz. Kısa HM3 ~1.94s'den 2.5s'ye hafif yavaşlatıldı; diğer seçilmiş seslerin
başlangıcı ve orijinal hızı korundu. Casting kayıtlarına yeni katman eklenmedi.
Burst sesleri gerçek visual sürelerine 0.65 / 0.78 / 0.86s uyarlandı.
Alan döngüleri sahip runtime ile başlar/biter; Vortex -> Plague ses değişimi;
asit + yanma birlikte. Tsunami/Singularity etkileri de efekt ömrünü takip eder.
Pause/resume ve .5x/1x/2x pitch/süre; Master*SFX ve canlı Settings önizlemesi bağlı.
Ivy seçenek seçiminde ve gerçek wave spawn'ında, Chalk seçenek seçiminde release
preview, gerçek kullanımda önce casting sonra release. Hazırlık/başarısız kullanım
release sesi üretmez. Ayrı summon saldırı/ölüm sesleri henüz bu aşamanın dışında.
Lisans/atıf: assets/audio/FOCUS-ABILITY-LICENSES.txt ve CREDITS.txt.
Kayıtlar audition arşivinde korunur; kullanılmayan alternatifler oyuna yüklenmez.

## Seçki 13 kısmi onay — UYGULAMA YOK

HL2 ve HS3 kullanıcı tarafından seçildi; henüz oyun koduna/assetlerine eklenmeyecek.
HS3 Singularity grubunda olmasına rağmen kullanıcı aynı mesajda yeni Singularity
adayları istedi: HS3 kaydı korunuyor, otomatik iptal/yeniden atama yapılmayacak.
Yeni seçim gelirse ilgili kayıt güncellenecek. Meteor için HM seçimi henüz yok.
Tsunami ve Singularity casting sesleri kadın sesi olacak, anlaşılır kelime/cümle
içermeyecek. Singularity daha pes, korkutucu veya demonik olabilir.
Seçki 14 hazır: TW1–5 Tsunami, SV1–5 Singularity; her grupta tek kaynak
kaydından beş ayrı kesit. Ek ses katmanı yok; SV kaynağı yazar tarafından
önceden işlenmiş kadın/cadı gibberish kaydı. Onay bekliyor, kurulum yapılmadı.

## Seçki 12 ONAYLARI — HENÜZ UYGULANMAYACAK

PX2, AN4, DE1, OA1, CL2, TS3, MT4, SG3 seçildi.
Seçki 11 ile birlikte entegrasyon, casting sesleri de seçilene kadar bekletiliyor.
VL/VT/VM/VS adayları reddedildi. Yeni casting önerileri farklı insan sesi
kayıtlarından hazırlanacak; aday içinde birden fazla ses karıştırılmayacak.
Sentetik hece dizileri ve ters çevrilmiş selamlaşmalar tekrar önerilmeyecek.

Seçki 13: HL1–5 Lightning, HT1–5 Tsunami, HM1–5 Meteor, HS1–5 Singularity.
20 tek-insan-kayıt adayı; hiçbir oyun entegrasyonu yok. HL/HM bir erkek kaydından
10 ayrı okuma; HT/HS bir kadın oyuncunun farklı ses stilleri ve İngilizce büyü
sözcükleri. 20 ayrı oyuncu değildir. Katman, ters çevirme veya ek yankı yok.
Kaynaklar/lisanslar listening-pack-13/LICENSE-SOURCES.txt içinde.

## Seçki 11 ONAYLANDI, UYGULAMA BEKLETİLİYOR

IG1 Ignite Dash, ER3 Erasing Soul, VP4 Vortex Pull, SC3 Static Clouds,
AP2 Acidic Pool, PS4 Plague Storm, BL5 yanan sıvı katmanı.
Bu sesler henüz oyuna eklenmeyecek; yeni seçimlerin tamamlanması bekleniyor.

Yeni istek: Acidic Pool ateş patlaması; Ivy Staff Animal/Cow moo, Demon konuşma/imp çığlığı,
Oathbreaker aşırı pes erkek/dua hissi. Ivy seçim onayı ve gerçek ability kullanımında aynı seçim sesi.
Chalk Dust release etkileri: yere düşen yıldırım, dehşetli tsunami, düşüş+patlamalı meteor,
Singularity için iki vızıltı/gürültü tasarımı ve üç farklı alternatif.
Chalk casting sözleri ayrı: Lightning pes erkek; Tsunami ince kadın; Meteor ince erkek;
Singularity pes kadın. Anlamsız/mistik heceler; gerçek Elfçe/Celestial oldukları iddia edilmeyecek.
Her grupta ilk dört tarife yakın, beşinci farklı bir yaklaşım; Singularity 2+3 özel isteği korunacak.
Ivy/Chalk popup seçimi ve cast/release tetikleri birbirinden ayırt edilerek entegrasyon yapılacak.
Bu aşamada yalnız örnekler hazırlanır, oyun kodu/ses ayarı/kurulu assets değiştirilmez.

## Spell Focus ability seçim notları — Seçki 11, HENÜZ UYGULANMADI

- Ignite Dash: teleport + varışta alev patlaması; iki bileşen ayrı saklanacak.
- Erasing Soul: ruh emme/lanetli patlama, tok ve pes; tiz olmamalı.
- Vortex Pull: sürekli rüzgâr/hortum.
- Static Clouds: tekrarlayan yıldırım.
- Acidic Pool: kabarcık + asidik cızırtı.
- Plague Storm: Vortex'ten ayrı kimlikli, necrotic rüzgâr.
- Yanan Acidic Pool: mevcut asit ile üst üste binecek ayrı yanma/sıcak sıvı katmanı;
  bu turda yalnız yanma sesi seçilecek, asit bu dosyaya gömülmeyecek.
VP/SC/AP/PS/yanma sesleri alan ömrü boyunca tekrarlanacak; sona erme/silinme/pause yaşam döngüsüyle yönetilecek.
IG/ER tek seferlik. Beşer aday; 35 adayın hiçbiri henüz oyuna eklenmedi.
Ivy Staff ve Enchanted Chalk Dust ayrıntıları sonraki kullanıcı mesajında gelecek, şimdilik kapsam dışı.
HATIRLATMA: Ability sesleri tamamlandıktan sonra bazı gelişmiş normal saldırılara ek sesler
ayrı bir adımda ele alınacak; mevcut elemental normal seslerinin yerine otomatik değişiklik yapılmayacak.

## Son karar — Spell Focus NORMAL / GELİŞMİŞ NORMAL saldırılar UYGULANDI

Air AI3, Acid AC2, Lightning LI3, Poison PO3; Water WV4, Fire FL5, Necrotic CU4.
Yedi özgün onaylı önizleme WAV'ı birebir assets/audio/sfx/magic_*.wav içine kopyalandı.
Projectile executor'ü ve Tome'un gelişmiş beam yolu bağlandı; hedefsiz atış sesi yok.
Chaos Theory normal atışın tipini oluşurken belirler; sprite/audio/hit tipi aynıdır,
isabette veya sekmede yeniden dönüştürülmez. Diğer hasar kaynaklarının Chaos davranışı korunur.
Temel hasar/scaling ve silaha özgü sekme/yelpaze kuralları korunur.
Combat sesleri gerçek run hızı .5x/1x/2x ile değişir; duran oyun boyunca mevcut combat sesleri duraklar.
UI ve müzik hızdan etkilenmez. Master/Music/SFX bağlantısı korunur.
Melee sesleri visual süresine uyarlandı; kaynak sesler audition arşivinde tutulur.
Chest sesi ilk kilit tıkında, ikinci reveal tıkı sessiz; batch açma seçilen sesi bir kez kullanır.
Testler: tüm normal/gelişmiş Spell Focus executor'leri, tip->ses eşlemesi, Chaos sprite tutarlılığı,
fitted WAV kaynakları/süreleri, .5x/2x ve hız değişiminde kalan süre, pause/resume, chest ilk/ikinci tık.

Kullanıcı notlarını göndermeden Spell Focus ability/spell SESİ ÖNERİLMEYECEK veya eklenmeyecek.
Önceki bekletme başlıkları bu son kararla tarihsel hale geldi.

## Magical normal saldırılar — Seçki 09/10, UYGULAMA BEKLETİLİYOR

Onaylandı: AI3 Air, AC2 Acid, LI3 Lightning, PO3 Poison. Henüz oyuna eklenmeyecek.
Water WA1–3, Fire FI1–3 ve Necrotic NE1–3 reddedildi.
Yeni seçki 10: WV1–6 su savurma, FL1–6 alev savurma, CU1–6 lanet/tekinsizlik.
Avatar sesleri alınmıyor; yalnızca benzer hareket hissi hedefleniyor.
Tüm seçimler tamamlanınca aynı uygulamada ele alınacak bekleyen kontroller:
- Melee normal/ability seslerinin animasyon başlangıç/bitiş süreleriyle uyumu (ranged hariç).
- Combat seslerinin oyun hızı .5x/1x/2x ile gerçek çalma hızı/süresi uyumu.
- Chest seçilen ses yalnız ilk tıkta; ikinci tık şimdilik sessiz, ileride ayrı ses.
- Magical normal ses seçimi silaha değil projectile'ın güncel hasar tipine göre.
- Chaos Theory sonrası projectile görseli ve sesi dönüşmüş hasar tipini izlemeli.
- Roadmap son maddesi: Son kontroller ve yapılacak eklemeler (level up, merge, item, end stage vb.).

## Son karar — fiziksel silah sesleri tamamlandı / Seçki 08

Bu karar önceki seçim ve bekletme notlarının üzerindedir.
- Crossbow Heavy Bolts: normal CB4 atışı, ayrı pes ability sesi yok.
- Dagger Knife Swarm: normal DG4; her gerçek salvoda bir kez, gelişmiş ikinci salvo da aynı.
- Double Swords Rapid Strokes: normal saldırı sesi, gelişmiş normal varsa tekli/ikili karşılığı.
  Önceki ayrı gruplanmış rapid_strokes_1..7.wav oyun dosyaları ve enum referansları kaldırıldı.
- Bow Leisurely Shot: BV2, her gerçek salvoda bir kez.
- Sling Shatterstone: ST1; base tek iniş, gelişmişte gerçek ikinci inişte aynı ST1 tekrar.
- Boomerang: GH3 hem normal saldırı hem Guided Hunt; eski BR3 oyun dosyasının yerini aldı.
  Ayrı Boomerang ability WAV'ı çoğaltılmadı. Dinleme arşivleri kaynak/tarihçe olarak korunur.
- Islık kullanılmayacak; oyuna yüklenmedi ve yüklenecekler listesinde değil.

Sonraki aşamaya henüz geçilmeyecek. Yeni sıra SES-ROADMAP.md başında.

## Son karar — AW5 ve tüm melee ability sesleri UYGULANDI

Bu karar aşağıdaki eski bekletme notlarının yerine geçer.
- Longsword Air Slash: AW5 birebir.
- Rapid Strokes: DS1; gerçekten gerçekleşen 1–7 saldırı kadar gruplanmış ses. Normalde 5, gelişmişte 7;
  hedefler erken ölürse yalnızca gerçekleşen sayı. Oyun içi hasar zamanlaması değişmedi.
- Lance Spin Attack: SP1 üç tekrar, toplam 420 ms animasyona sığacak şekilde sıkıştırıldı.
- Shield Defensive Burst: SB5 birebir.
- Earthbreaker: EB1'in tek patlama katmanı gerçek hasar dalgası başına; 3 / 4 dalga.
- Whip Sweep: WS2 tam iki kopya, ek ara olmadan ardışık.
Tüm sesler Master × Sound Effects'e bağlıdır. Normal saldırı seçimleri değişmedi.

Sıradaki seçim: listening-pack-08/DINLE.html, 30 aday.
KS1–5 Dagger, BV1–5 Bow, HB1–5 Crossbow, ST1–5 Sling, GH1–5 Boomerang, HW1–5 insan ıslığı.
ST ayrıca gelişmiş sekme karşılaştırmasını içerir. GH1 mevcut BR3 ile aynı.
HW1–4 ayrı kayıt; HW5 HW1'in pes varyasyonu. dklon CC BY 3.0, seçilirse oyun içi atıf eklenecek.
Bu 30 yeni aday HENÜZ oyuna eklenmedi.

## Onaylandı ve oyuna eklendi

Kullanıcı son mesajında TH3 ile birlikte aşağıdaki beş sesin de eklenmesini istedi.
Bu güncelleme önceki bekletme kararının yerine geçer. Hepsi normal saldırı sesleridir.

- Bow: Seçki 04 / BW3.
- Crossbow: Seçki 04 / CB4.
- Dagger: Seçki 04 / DG4.
- Sling: Seçki 04 / SL1.
- Boomerang: Seçki 04 / BR3.
- Warhammer: Seçki 05 / TH3.

Kaynak dosyalar ranged için listening-pack-04/<seçim>.wav, Warhammer için listening-pack-05/TH3.wav.
Seçilen WAV dosyaları oyuna birebir kopyalandı. Tüm sesler Master × Sound Effects'e bağlıdır.

## Warhammer — TH3 ile tamamlandı

Önceki H, WH ve WM seçenekleri reddedildi; hiçbiri oyuna atanmadı.
Yeni hedef: savurma/çınlama değil, bir yere gerçekten vurulduğunda duyulan
sert, kısa, tok "pat/güm" vurgusu. Uzun metal çınlaması ve yalnızca filtrelenmiş yumuşak bas istenmiyor.
TH3 seçildi ve normal saldırıya bağlandı. Ability sesi ayrıca seçilecek.

## Önceden oyuna eklenmiş olanlar — değişmedi

Longsword G1; Double Swords DS1 (tekli/ikili); Lance LN1; Shield SH1; Whip WP5.
Mevcut UI ve müzik seçimleri de değişmedi.

## Sıradaki seçimler

### Melee ability kararları — NOT ALINDI, HENÜZ UYGULANMAYACAK

Kullanıcı bu kararların yalnızca kaydedilmesini, henüz oyuna eklenmemesini istedi.

- Double Swords / Rapid Strokes: önerilen RS adaylarından biri seçilmedi. Şu anda normal saldırıda
  kullanılan DS1 ses dokusu kullanılacak; ability gerçekten kaç saldırı yapıyorsa o sayıda vuruş sesi olacak.
  Gerçek saldırı zamanlaması ve sayısı (mevcut düzende 5 / gelişmişte 7) esas alınacak.
  Tek dosyada birleştirmek veya olay bazlı gruplamak mümkün; sabit, yanlış sayılı bir seri kullanılmayacak.
- Lance / Spin Attack: SP1. Tek ability kullanımında kısa aralıklarla arka arkaya üç kez.
  Animasyon süresi yetmezse aralıklar/sayı animasyona göre ayarlanabilir; kullanıcı buna izin verdi.
- Shield / Defensive Burst: SB5.
- Warhammer / Earthbreaker: EB1. Her gerçek hasar dalgasına tam bir ses patlaması eşlenecek.
  Üç veya dört dalga varsa aynı sayıda patlama; EB1'in tüm üçlü önizlemesi her dalgada tekrar çalınmayacak.
- Whip / Whip Sweep: kullanıcı "SW2" yazdı; mevcut tek karşılığı WS2 olarak yorumlandı.
  İki kopya arka arkaya, aralarında ek boşluk olmadan; eşzamanlı üst üste bindirme değil.
- Longsword / Air Slash: AS1–AS5 reddedildi. Yeni hedef keskin, rüzgârımsı hava geçişi;
  kılıç savurma veya metal kesişi gibi duyulmamalı. Seçki 07 yalnızca yeni Air Slash önerileri.

Bu ability kararları yükleyiciye, core runtime'ına veya assets/audio'ya henüz bağlanmayacak.
Ardından ranged ability'leri, sonra Spell Focus / büyü sesleri seçilecek.
