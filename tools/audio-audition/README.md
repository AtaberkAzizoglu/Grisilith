# Grisilith — dinleme seçkisi 01

## Güncel dinleme — Seçki 12 (uygulama bekliyor)

listening-pack-12/DINLE.html: 60 aday, 12 grup; ilk dört tarife yakın, beşinci alternatif.
PX patlama; AN/DE/OA Ivy çağrıları; CL/TS/MT/SG Chalk sonuç efektleri;
VL/VT/VM/VS Chalk casting sözleri. SG özel düzeni 2 vızıltı + 3 farklı alternatif.
Casting 1–2 kayıt kesiti/ters ses tasarımı; 3–4 sentetik hece dizileri, 5 sözsüz alternatif.
Kaynaklar gerçek Elfçe/Celestial diye tanıtılmaz. CC BY/CC BY-SA koşulları seçkinin lisans dosyasında.
Önceki Seçki 11 kararı: IG1 ER3 VP4 SC3 AP2 PS4 BL5; HENÜZ oyuna eklenmedi.
Kullanıcı tüm yeni seçimleri tamamlayınca entegrasyon birlikte yapılacak.

## Son durum — magical normal saldırılar tamamlandı

Onaylanan AI3/AC2/LI3/PO3/WV4/FL5/CU4 normal/gelişmiş normal atışlara bağlandı.
Chaos Theory projectile tipi launch anında sabitlenir; hasar, sprite ve ses uyuşur.
Melee kaynakları visual süresine fit edilir; combat sesleri oyun hızını ve pause durumunu izler.
Chest ilk tık selected sound, ikinci reveal tıkı sessiz.
install-selected.ps1 güncel kurulumu yapar; installMeleeAbilityAudio görevi sonunda
InstallMagicNormalAudio da çalışır ve eski uzun kayıtların geri gelmesini engeller.
Yeni büyü seçimi önerileri kullanıcı notları gelene kadar bekletilir.

## Son durum — ranged ability seçimi tamamlandı

BV2 Bow salvosu, ST1 Sling gerçek inişleri, GH3 Boomerang normal + ability.
Dagger ve Crossbow ability'leri kendi normal sesini kullanır; Rapid Strokes da normal tekli/ikili sesi kullanır.
Kullanılmayan rapid_strokes_1..7.wav silindi; hazırlama görevi bunları yeniden oluşturmaz.
Islık adayları kullanılmayacak. Tarihsel seçkiler kaynak arşivi olarak kalır, oyun paketine girmez.
Yeni roadmap SES-ROADMAP.md başında; henüz sonraki aşamaya geçilmedi.

## Güncel durum — Seçki 08

AW5 ve tüm onaylanan melee ability sesleri artık oyuna bağlı. Önceki bölümlerdeki bekletme notları tarihsel.
Yeni dinleme: listening-pack-08/DINLE.html; KS/BV/HB/ST/GH/HW gruplarında toplam 30 aday.
Ranged ability ve insan ıslığı adayları seçilene kadar oyuna eklenmez.
HW1–4 dklon'un dört ayrı kaydı; HW5 bir pes varyasyon. Kaynak/lisans notları seçkide.
Hazırlama görevleri: installMeleeAbilityAudio, prepareRangedAbilityAudition (audition.gradle).
Melee testleri gerçek Rapid Strokes 5/7 sayısını, hedefsiz sessizliği, Air Slash/Spin/Burst/Sweep
tetiklerini ve Earthbreaker 3/4 gerçek dalgasına birebir ses eşlemesini doğrular.

Bu klasör ses seçkisini ve ses sistemi testlerini içerir. Onaylanan A1, B5, C2, D2, E1, F1 kayıtları
`assets/audio/sfx/` içine birebir kopyalandı. Gregorian Chant menü müziği `assets/audio/music/` içinde.
Kaynak/lisans bilgileri `assets/audio/CREDITS.txt` ve oyun içindeki Audio Credits ekranındadır.

`listening-pack-01/DINLE.html` dosyasını tarayıcıda açın. Ağ bağlantısı gerektirmeden 30 adayı tek tek
veya 6 toplu kayıtla dinleyebilirsiniz. Her toplu kayıt 31 saniyedir: adaylar 1 → 5 sırasındadır ve
her aday iki kez çalar. Yeni adaylar 1, 7, 13, 19 ve 25. saniyelerde başlar.

- A1–A5: hafif metalik buton tıklaması.
- B1–B5: seçim / onay. B5 fiziksel metalik alternatif, B1–B4 özgün UI onaylarıdır.
- C1–C5: geri / iptal.
- D1–D5: geçersiz işlem / hata.
- E1–E5: defter / sayfa / preset.
- F1–F5: ahşap sandık açma taslakları. Bunlar özel sandık kayıtları değildir; kilit,
  gıcırtı, ahşap temas ve F3/F4'te kapı açma kayıtları kullanılmıştır.

Materyal uyumuna göre ilk denenecek kombinasyon: **A1, B5, C1, D2, E1, F1**.
Bu bir dinleme onayı veya ölçülmüş algısal kalite sıralaması değildir. Hepsi değiştirilebilir.

Kaynaklar yalnızca resmi Kenney indirmeleridir:

- https://kenney.nl/assets/interface-sounds
- https://kenney.nl/assets/rpg-audio
- https://kenney.nl/assets/impact-sounds

Üçü de CC0. Paketlerin özgün lisans dosyaları dinleme klasöründe bulunur. `SECENEKLER.txt`,
her adayın özgün dosya adlarını ve karşılaştırmadaki zamanlarını listeler.

## İşleme

Önizlemeler 44.1 kHz / 16-bit mono WAV'a çevrildi. Metal darbe adaylarının bazıları kısa
tıklamaya kesildi. Sandık denemelerinde belirtilen kayıtlar katmanlandı. 5 ms son fade uygulandı.
Her adayın tepe seviyesi yaklaşık -12 dBFS'ye eşitlendi; algısal ses yüksekliği eşitlenmedi.
Bu, son oyun miksajı değildir. Orijinal dosyalar `sources/` içinde değiştirilmeden tutulur.

Teknik doğrulama: 30 adayın tamamı sıfırdan farklı ses verisi içerir; 36 WAV dosyasının başlığı,
örnekleme hızı ve kare sayısı Java AudioSystem ile kontrol edildi. Toplu kayıtlarda adaylar
üst üste binmez. Henüz oyun içinde veya insan dinlemesiyle kalite onayı yapılmadı.

Yeniden hazırlamak için `download-packs.ps1`, ardından:

`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareAudioAudition`

Gregorian Chant menü müziği tercihi korunmuştur; bu seçkiye müzik dosyası dahil değildir.

## Oyuna bağlantı

- Menü müziği menüler arasında devam eder, run içinde (pause/settings dahil) çalmaz.
- Tüm efektler Master × Sound Effects; müzik Master × Music üzerinden kontrol edilir.
- Settings slider'ları geçici önizleme yapar; kaydetmeden çıkınca önceki düzey geri gelir.
- Hover, tooltip, kaydırma ve stat tuşlarının otomatik tekrarı sessizdir.
- Aynı karede tek UI sonucu ve en fazla iki farklı silah sesi seçilir; toplam üç eşzamanlı efekt sınırı vardır.
- Silahlar dönüşümlü öncelik alır; sabit slot sırası diğer silahların sürekli sessiz kalmasına yol açmaz.
- UI için ve silah sesi başına en az 80 ms tekrar aralığı vardır. UI sesleri düşük öncelikli silah seslerini bastırabilir.
- Henüz seçilmeyen silah, düşman ve harita ambiyans sesleri eklenmedi.

Testler (hoparlör/ağ kullanmaz):

`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:audioSmoke :lwjgl3:audioScreenSmoke`

Uygulama doğrulaması (2026-10-18): mixer ve native ekran testleri geçti. Seçilen altı WAV'ın
önizlemelerle SHA-256 eşitliği doğrulandı; MP3'ün tamamı hoparlör kullanmadan çözüldü (194 saniye).
Menü/run/pause-Settings geçişleri, canlı ses önizlemesi, geri alma, mute ve uygulama yaşam döngüsü
kontrol edildi. Mevcut online backend durum ve isim seçimi ekranı testleri de geçti.
Oyun içi algısal miksaj için kullanıcının dinleyerek yapacağı son değerlendirme ayrıca gereklidir.

## Seçki 02 ve son düğme düzenlemesi

Geri/iptal/Return to Game düğmeleri ve Merchant kategori düğmeleri A1 normal tık sesini kullanır.
C2 yalnızca mevcut ödül atma / gear kaldırma gibi farklı işlemlerde tutulur.
Preset sayfası değiştirme E1 olarak kalır.

`listening-pack-02/DINLE.html`: G kılıç savurma, H tok darbe, I kalkan/metal teması;
her grupta 5 aday ve bir toplu dinleme dosyası. Toplu dosyalarda 1–5 sırası, her aday iki kez.
Katmanlanmış taslaklar kaynaklarıyla açıkça etiketlidir. Yeni adaylar henüz oyuna bağlı değildir.
Üretim komutu: `gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareCombatAudition`.
15 WAV + 3 toplu WAV üretimi ve mevcut ses/ekran testleri geçti.
Aşamaların sırası `SES-ROADMAP.md` dosyasında bulunur.

## Seçki 03 — silaha özel seçenekler

G1 Longsword normal saldırısına bağlandı; H ve I adayları reddedildi.
G1 hem normal hem gelişmiş Longsword vuruşunda saldırı başına bir kez çalar, hedef yoksa çalmaz.
Ability'ye veya başka silahlara bağlanmadı. Master × Sound Effects ve mevcut ses sınırları geçerli.

`listening-pack-03/DINLE.html`: DS1–DS5 Double Swords tekli/ikili eşleri;
LN1–LN5 Lance hava itişi, SH1–SH5 Shield metal teması, WH1–WH5 Warhammer ağır metal,
WP1–WP5 Whip deri kırbaç. 25 seçenek, 5 ek gelişmiş Double Swords dosyası ve 5 toplu dinleme.
Yeni seçenekler oyuna eklenmedi. WH ve WP özgün kayıt + tasarım varyasyonları olarak açıkça etiketli.
Kaynaklar: artisticdude Swishes (CC0), Vehicle Tinysized (CC0), Tuomo Untinen/Reemax Whip (CC BY 4.0).
Sonuncusu ücretsiz ama atıf zorunlu; seçilirse oyun içi kredilere eklenecek.
Tam kaynak/lisans bilgisi `weapon-source-credits.txt` ve seçkideki `LICENSE-SOURCES.txt` dosyasında.

Hazırlama: `download-weapon-sources.ps1`, ardından
`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareWeaponAudition`.

## Onaylanan melee sesleri / Seçki 04

DS1 normal ve gelişmiş Double Swords, LN1 Lance, SH1 Shield ve WP5 Whip normal saldırılarına bağlandı.
Gelişmiş Whip'in 150 ms sonraki ikinci savurması da aynı seçilen sesi tetikler.
Ability sesleri değişmedi. WP5'in Tuomo Untinen atfı oyun içi Audio Credits'e ve audio/CREDITS.txt'ye eklendi.
Önizleme ile oyun dosyalarının birebir eşitliği, tekli/ikili saldırılar ve eşzamanlı silah sırası test edildi.

Warhammer henüz atanmadı. `listening-pack-04/DINLE.html` sırası:
WM1–WM5 (daha küt/ağır/kalın Warhammer) → BW1–BW5 Bow → CB1–CB5 Crossbow →
DG1–DG5 Dagger → SL1–SL5 Sling → BR1–BR5 Boomerang.
30 aday / 6 toplu karşılaştırma. Yeni adaylar oyuna bağlı değil.
Bow/Crossbow gerçek Still North Media kayıtlarından ayrı atış kesitleridir;
diğer tasarım varyasyonları ve katmanlar açıkça etiketlidir. Bu seçkinin tüm kaynakları CC0.

Kaynak hazırlama: `download-ranged-sources.ps1`, ardından
`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareRangedAudition`.
Kaynak arşivler /sources altında tutulur ve büyük indirilen dosyalar Git'e eklenmez;
indiriciler, kaynak URL'leri, lisans notları ve hazırlanmış dinleme dosyaları ayrıca korunur.

## Seçki 05 — yalnızca Warhammer / ranged seçimleri beklemede

BW3, CB4, DG4, SL1 ve BR3 seçildi ama kullanıcının talebiyle şimdilik oyuna EKLENMEDİ.
Bu değerler yalnızca SECIM-NOTLARI.md ve roadmap'e kaydedildi; yükleyiciye veya assets/audio'ya eklenmedi.
WM seçenekleri reddedildi. Yeni hedef bir yüzeye çarpışta sert, kısa, tok "pat/güm" darbesi.

`listening-pack-05/DINLE.html`: TH1–TH5, her aday iki kez çalan TH-all.wav ile birlikte.
Yeni CC0 Thwack kaynağından üç dosya ve iki darbe-gövdesi katmanı taslağı.
İlk iki kaynak kayıt yazar tarafından filtresiz olarak belirtiliyor. Yeni alçak geçiren filtre veya metal çınlaması eklenmedi.
Kaynaklar/lisanslar seçkinin içinde ve hammer-source-credits.txt dosyasında.

Hazırlama: `download-weapon-sources.ps1`, ardından
`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareHammerAudition`.

## Güncel durum / Seçki 06

Önceki bekletme kaldırıldı: TH3 Warhammer ve BW3, CB4, DG4, SL1, BR3 normal saldırıları oyuna eklendi.
İlk atış/savurma başına bir ses; Sling parçaları ve Boomerang'ın dönüş/arka turu sesi çoğaltmaz.
Hedef yokken sessizlik, normal/gelişmiş saldırılar ve zaten uçan Boomerang kontrolü test edildi.
Seçilen altı WAV'ın oyundaki dosyalarla birebir eşitliği ve tüm ses düzeyleri doğrulandı.

`listening-pack-06/DINLE.html`: 30 melee ability taslağı + 6 toplu karşılaştırma.
RS1–5 Rapid Strokes, SP1–5 Spin Attack, AS1–5 Air Slash, SB1–5 Defensive Burst,
EB1–5 Earthbreaker, WS1–5 Whip Sweep. Bunlar yeni özgün kayıtlar değil, lisanslı kayıtların
etiketli katman/timing varyasyonlarıdır. Ability sesleri henüz oyuna eklenmedi.
Kaynaklar ve düzenlemeler ability-source-credits.txt içinde.
Üretim: `gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareMeleeAbilityAudition`.

## Seçki 07 — keskin rüzgâr / kararlar uygulama bekliyor

Melee ability kararları SECIM-NOTLARI.md içinde ayrıntılı olarak kaydedildi, henüz uygulanmadı.
Rapid Strokes mevcut DS1 dokusuyla gerçek saldırı sayısı kadar; SP1 animasyona uygun üç tekrar;
SB5; EB1 gerçek hasar dalgası başına tek patlama; WS2 (SW2 yazımı) boşluksuz iki ardışık tekrar.
AS1–AS5 reddedildi.

`listening-pack-07/DINLE.html`: AW1–AW5 yeni hava/rüzgâr adayları, kılıç veya metal katmanı yok.
Üç CC0 kaynaktan hazırlanmış hız, kesit, tiz vurgu ve gust-envelope varyasyonları açıkça etiketli.
Bunlar da henüz oyuna eklenmedi. Kaynaklar wind-source-credits.txt içinde.
Hazırlama: download-wind-sources.ps1 ardından
`gradlew.bat -I tools/audio-audition/audition.gradle :lwjgl3:prepareAirSlashAudition`.
