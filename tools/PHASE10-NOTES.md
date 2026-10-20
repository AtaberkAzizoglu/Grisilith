# Faz 10 — Hotfix ve Open Beta Release

## 10a ilk grup — uygulandı

- MenuButton hover sesi kaldırıldı; tıklama sesleri duruyor.
- Market teklifleri ve envanter weapon drag başlangıcı Pause ile aynı 8px hareket eşiğinde.
- Pause/Market ortak gear ghost: weapon için icon + küçük `Lv. IV` paneli; item için yalnız icon.
- Vogtsweier Siege başlangıç mesajı oyuncunun 120 dünya birimi üstünde, 8 oyun saniyesi.
- Reset All / Reset Controls önce onay ister; Cancel/ESC draft ve kayıtları değiştirmez.
- Pause ana butonları ortak UiComponentRenderer HOVERED/PRESSED görsellerinde.
- Chronicles Back panel içine x190/y38 konumuna taşındı.

## Sonraki mesajı bekleyenler — dokunulmadı

1. Armor sunumu aşağıdaki üçüncü grupta tamamlandı; hasar alma formülü henüz değişmedi.
2. Hasar: düz savunma çıkarımı hem oyuncu hem düşmanda ortak DamageDefense.resolve.
   100 raw physical vs 22 baseArmor x 10 AC -> 1. Savunma hasarı geçince minimum 1.
   Baseline/adaptive katkı olmadan stage25 factory: Witch physical block236/MR560;
   Vampire Lord physical220/MR610. Bunlar tek vuruş eşikleri, yüzde azaltma değil.
   Ana hasar hattında ayrı bir matematik hatası saptanmadı; örnek run/build bazlı
   ayrıntılı ölçüm ve olası denge kararı bekliyor. Sayılar değiştirilmedi.
## 10a ikinci grup — uygulandı

- Oyuncunun hasar sayıları kritik vuruşta da kırmızı; diğer hedeflerin renkleri aynı.
- Siege mesajı aynı font/ölçekte şeffaf PNG, 4x kaynak çözünürlük (asset kaydına bak).
- Totem Level4+ sayaç tek artıyor: 8 normal saldırıda 2 drain; hedefsiz drain sayılmaz.
- Zorluk seçimi kağıt panel, seçili işareti ve dinamik x0.33 / x0.50 / x1.00 Score.
- Praise: top10 ve kendi run'u olmayan girdiler, hesap/run tek oy, idempotent RPC.
  Oy sonrası buton pasif/soluk. Liste dışına çıkınca oylar trigger ile silinir;
  yeniden girdiğinde korunmaz. Sıralama değişiklikleri/oylar aynı işlem kilidini kullanır.
  Doğrudan tablo erişimi kapalı, istemcide gizli anahtar yok.
- Leaderboard aynı yerdeki ok ile daraltılır/açılır; footer da gizlenir.
- Display çözünürlük seçimi: fullscreen'de desteklenen modlar (en az1280x720),
  pencerede monitöre sığan1280x720/1366x768/1600x900/1920x1080/2560x1440/3840x2160.
  Native/Default seçeneği, 15s Keep/Revert, ESC ve zaman aşımında geri dönüş.
  Varsayılan fullscreen; eski fullscreen ve yeni çözünürlük tercihi kalıcıdır.
- Collection/Merchant filtre başlığında tüm listeyi içeren open resmi sıkıştırılmıyor;
  başlık yalnız kapalı resimden, seçenekler mevcut ayrı listeden çiziliyor.

Doğrulama: tools/audio-audition/qa/Phase10aSmoke.kt — izole native pencere,
reset/drag/layout kontrolleri + Totem regresyonu ve savunma için salt okunur diagnostic.
`Phase10bSmoke`: izole GL render; taklit display sürücüsüyle çözünürlük keep/revert/
timeout/persistence; taklit online bağlantıyla praise ve collapse; kırmızı hasar renkleri.
Gerçek monitör mod değiştirme kombinasyonları bütün cihazlarda test edilmiş değildir.

Supabase canlı SQL testleri (tamamı rollback): duplicate/self/outside-top10 engeli,
birden çok run'a oy, top10'dan düşmede silme, dönüşte sıfırlama, DELETE yetkisi yok.
Migration canlı proje ve yerel dosyada `20261020145837_grisilith_run_praises` olarak kayıtlı.
CLI mevcut değildi; migration MCP kaydı requestState hatası verdiği için başarılı
execute_sql kurulumu aynı SQL metniyle migration geçmişine işlendi.
Security advisor: private tablolardaki RLS/policy-yok INFO kasıtlı (yalnız RPC erişimi);
önceden mevcut leaked-password-protection uyarısı bu görevde değiştirilmedi.

Advisor açıklamaları: [Private RLS / policy-yok](https://supabase.com/docs/guides/database/database-linter?lint=0008_rls_enabled_no_policy),
[önceden mevcut parola koruması uyarısı](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection).

Son doğrulama: `phase10aSmoke`, `phase10bSmoke`, `missingSfxSmoke`, `audioScreenSmoke`
geçti. Canlı QA kullanıcıları ve run'ları rollback ile tamamen kaldırıldı (kalan0).
Görsellerin yolları ve son üretim prompt'u: [asset kaydı](PHASE10-ASSETS.md).

## 10a üçüncü grup — uygulandı

- Market/Pause sürüklemede aynı silahın aynı/düşük seviyeli, gerçekten merge edilebilir
  kopyalarına hafif mavi rim pulse; Lv5 kaynak ve kaynak slot işaretlenmez.
- Defeat/Victory müziği Summary reveal'ı değil run bitiş koşulunu izler.
- Chest ve stage stat seçimi mevcut parçayı kesmez/yeniden başlatmaz; gain0.65.
- Difficulty yeni kağıt butonu kaldırıldı; mevcut görsel karartılır, seçili metin beyaz.
  Selected kaldırıldı; yalnız seçilide Score, öncekinin yarısı büyüklükte.
- Leaderboard ilk varsayılan açık; collapsed tercihi cihazda kalıcı.
- Resolution açılır/scroll'lu liste, yüksekliği56; arka UI görünür ama liste açıkken
  etkileşmez. ESC/dış tıkla kapanır; seçimde15s Keep/Revert korunur.
- Wolf/Eagle Necrotic vulnerable; iki Skeleton Bludgeoning vulnerable;
  Boar/Crocodile Fire resistant; Slime Acid immune, Water resistant.
- Ortak ArmorPresentation: Base Health, Base Armor, Base Spell Protection, her stat ayrı satır.
  Base Spell Protection başlığında stat ikonu/kırmızı renk yok; gerçek statlar ikonlu/kırmızı.
  Collection/Merchant/Preset/Pause açıklamaları aynı metin kaynağından gelir.

Kontroller: phase10a/b/cSmoke, runMusicSmoke, audioScreenSmoke geçti.

### Hasar alma modeli kararları — sonraki turda uygulandı

- Kullanıcı: yeni hasar alma modeli player/enemy/summon hepsinde kullanılacak.
- Physical R = Base Armor + Armor Class; Magical R = Base Spell Protection + Magic Resistance.

### 10a son düzenlemeler — uygulandı

- Difficulty kararması çerçevelerin iç ölçülerine oturtuldu, taşan katmanlar kaldırıldı.
  Score font ölçeği 0.31'den 0.70'e çıkarıldı; Easy/Normal/Hard görüntüleri kontrol edildi.
- Armor hover yalnız temel değerleri ve stat bonuslarını gösterir; Collection açıklaması korunur.
- Oyuncu/enemy/summon ortak hasar hesabı: Incoming × 100 / (100 + R).
  Fiziksel R toplamına mevcut Piercing oranı uygulanır; magical savunmaya uygulanmaz.
  Immunity 0, pozitif hasar minimum 1. Tick aralığı savunma yüzdesini değiştirmez.
- Boss savunma ve HP'leri kullanıcının nitelik tablosuna göre ayarlandı. HP seviyeleri
  25. aşamada mevcut family/type çarpanları dahil karşılaştırıldı; Gorilla değişmedi.
- Pause savunma özeti ve ilgili stat/armor matematiği açıklamaları yeni yüzde modeline uyarlandı.
- Sayısal tablo: audio-audition/PHASE10A-DEFENSE-BALANCE.md.
- Kontroller: phase10a/b/cSmoke, Phase10dDefenseChecks, verifyMagicResistance,
  verifyDotBalance geçti. Tam run ile oynanış dengesi testi kullanıcı geri bildirimini bekliyor.

### 10a seçili zorluk görselleri — tamamlandı

- Easy/Normal/Hard için referans kırpımlardan üç ayrı ImageGen PNG eklendi.
  Beyaz başlıklar ve Score x0.33 / x0.50 / x1.00 görsellerin içindedir.
- Seçili buton, alpha sınırları esas alınarak normal merkezinde %15 büyük çizilir.
  Seçilmemiş butonlar aynı kalır; eski karartma ve seçili metin katmanları kaldırıldı.
- Tıklama alanı seçili görselin boyutunu izler; texture'lar ekran kapanışında temizlenir.
- phase10cSmoke: üç seçimde merkez, büyütme, panel sınırları, opak iç/şeffaf dış
  kontrolü ve oyun içi ekran görüntüleri doğrulandı. Üretim kaydı: PHASE10-ASSETS.md.
