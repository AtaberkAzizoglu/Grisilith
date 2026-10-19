# Ses seçimleri — güncel durum

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
