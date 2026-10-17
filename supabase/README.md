# Grisilith — Faz 8b / 8c kurulumu

## Şu anki durum

17 Ekim 2026: Supabase bağlantısı üzerinden **Grisilith** (`wadrsdretczaoarjduko`, `eu-west-1`)
projesine kurulum uygulandı. Sunucu API'si HTTPS üzerinden publishable anahtarla doğrulandı;
API/Score sürümü 1, oyun sürümü 0.3, katalog 130 içerik. Migration dosya adları canlı migration
geçmişiyle eşleştirildi. Paylaşılan publishable anahtar yalnızca istemci bağlantısı içindir.

Canlı PostgreSQL üzerinde geçici transaction içinde profil, sahiplik, heartbeat, Score (2319),
tekrar gönderim, leaderboard ve bağlantı zaman aşımı testleri geçti. Transaction geri alındı;
test sonunda oyuncu, run ve Auth kullanıcı sayısı 0. Test verisi sıralamada bırakılmadı.

HTTP kontrolleri: girişsiz profil isteği **401**, özel şemayı REST'e açma girişimi **406**
ile reddedildi. İstemciye doğrudan tablo okuma/yazma izni yok; public şemada yetki yükselten
Grisilith fonksiyonu yoktur.

Faz 8c son Supabase Advisors kontrolü: ERROR/WARN veya performans uyarısı yok.
İleride parolayla giriş eklenirse Auth servisinin
[Leaked Password Protection](https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection)
ayarı ayrıca değerlendirilmelidir; mevcut anonim oturumların parolası yoktur. Auth ayarları değiştirilmedi.
Üç özel tabloda bilinçli olarak hiç RLS erişim politikası
olmaması için [RLS Enabled No Policy — INFO](https://supabase.com/docs/guides/database/database-linter?lint=0008_rls_enabled_no_policy)
notu var: bu tasarımda doğrudan erişim reddedilir, yalnızca kimlik kontrolü yapan özel fonksiyonlar
çalışır. Bu bilgi notları için RLS kapatılmamalı
veya istemcilere geniş tablo yetkisi verilmemelidir.

**Faz 8b canlı doğrulaması tamamlandı:** Anonim giriş açıldıktan sonra gerçek HTTPS üzerinden
anonim hesap → profil → run başlangıcı → heartbeat → sonuç kaydı → tekrar gönderim → leaderboard
testi geçti. Sunucunun 10 normal öldürme, Hard, stage 1 için hesapladığı Score **11** doğrulandı.
Test hesabından global çıkış başarılı oldu; yalnızca bu teste ait kullanıcı/profil/run kayıtları
kimlikleri kontrol edilerek silindi. Token'lar yalnızca test sürecinin belleğinde kaldı.
Önceki `anonymous_provider_disabled` engeli artık yoktur.

**Faz 8c tamamlandı:** ilk online ana menü ziyareti için isim seçimi, Settings → Account,
ana menüde tüm map'lerin ortak leaderboard'u ve HUD bağlantı durumu eklendi.
İsim seçmeyen oyuncu kendi büyük harfli UUID'siyle devam edebilir; geçerli run'ları bununla gösterilir.
Gerçek HTTPS testinde profil → isim değiştirme → sıfırlama → run → Score → leaderboard geçti.
Test hesabından global çıkış yapıldı; yalnızca doğrulanmış QA kullanıcı/profil/run kayıtları
silindi ve üçünün de kalmadığı kontrol edildi. Gerçek oyunculara dokunulmadı.

## Supabase panelinde yapılacaklar

**Bu projede kurulum yapıldı; SQL dosyalarını tekrar çalıştırmayın.** Yeni bir proje kurulacaksa
önce `migrations/20261017120459_grisilith_online_runs.sql`, ardından
`migrations/20261017120640_grisilith_content_catalog.sql`,
`migrations/20261017175709_grisilith_identity_leaderboard.sql` ve
`migrations/20261017182121_grisilith_name_phrase_boundaries.sql` ve
`migrations/20261017183850_grisilith_whitelist_covered_spans.sql` sırasıyla uygulanır.
Mevcut tablolar silinmez.

1. Anonim giriş bu projede açılmış ve doğrulanmıştır; ek kurulum adımı kalmamıştır.
   Mevcut CAPTCHA/güvenlik önlemlerini kapatma. İleride CAPTCHA açılırsa masaüstünde doğrulama
   akışı da eklenmeden yeni anonim kayıt tamamlanamaz; oyun bu durumda offline devam eder.
2. İsteğe bağlı, SQL Editor'dan aşağıdaki kontrol çalıştırılabilir:

   ```sql
   select public.grisilith_api_status();
   ```

   Sonuçta `api_version: 1`, `score_version: 1`, `game_version: "0.3"` bulunmalı.
   Katalog henüz yüklenmemişse API sürümü 0 döner ve oyun yeni anonim hesap oluşturmaz.
3. Veritabanı şifresini, secret/service-role anahtarını veya oturum token'ını paylaşma.

Kurulum sırasında hata alınırsa hatayı paylaş; dosyaları zorla değiştirme veya RLS'yi kapatma.
İlk dosya tek bir transaction içinde çalışır; hata olursa kısmi kurulum kalmaz.

## Teknik tercih

- PostgreSQL fonksiyonları (RPC) sunucu tarafındaki doğrulamayı ve Score hesaplamasını yapar.
  Bu ilk sürüm için ayrıca Edge Function dağıtımı gerekmez.
- Oyuncu kimliği `auth.uid()` üzerinden alınır; istemciden oyuncu ID'si kabul edilmez.
- Tablolar `grisilith_private` şemasındadır; RLS açık, istemciye doğrudan tablo okuma/yazma izni yoktur.
- Yetkili RPC'ler sadece gereken veriyi döndürür, sabit/boş `search_path` kullanır.
- Yetki yükselten uygulamalar özel şemadadır; `public` API fonksiyonları `security invoker`
  yönlendirmeleridir. Özel şema Data API'nin exposed schemas listesine eklenmemelidir.
- Özel isimler 3–24 İngilizce/Türkçe harf, rakam, boşluk ve onaylanan sembolleri kabul eder;
  en az bir harf veya rakam gerekir. İsim her girişte büyük harfe çevrilir. ICE / İCE görünümleri korunur.
  Aynı görünen büyük harfli özel isim tek kişiye aittir. İsim değişimi beklemesizdir, eski isim hemen boşalır.
- Kalıcı UUID 36 karakterdir; yalnızca oyuncunun kendi UUID'si varsayılan ad olarak bu sınırdan muaftır.
  Boş isim gönderimi UUID'ye sıfırlar. Adı girişten önce placeholder olarak gösteririz; ilk harfte kaybolur.
- İsim onayı sunucuda tutulur: offline açılışta istenmez; ilk online ana menü ziyaretinde bir kez istenir.
  Başarısız bağlantılı deneme tamamlanmış sayılmaz; bir sonraki uygun ana menü ziyaretinde tekrar denenir.
- `assets/online/name-policy.json` içindeki 220 kaynak ifade ve 175 kelime/özel isim istisnası
  sunucuda ve istemcide aynı verilerle uygulanır. Kaynak ifadeler I/İ eşdeğerliği nedeniyle kısmen örtüşür.
  Yalnızca filtrede I/İ eşdeğerdir; rakamlar harflere çevrilmez, diğer Türkçe harfler sadeleştirilmez.
- Semboller filtrede yok sayılır. Boşluklar korunur; yalnızca art arda 1–2 karakterlik parçalar
  ayrıca birleştirilip taranır. Açıkça yasaklı çok kelimeli ifadeler tam kelime sınırında aranır.
  Whitelist tüm ismi affetmez; birleşik isimlerde de sadece kendi içindeki eşleşmeleri kapsar
  (DARKPHOENIX gibi). İnceleme dosyası:
  `tools/online-backend/whitelist-inceleme.txt`. Liste sonlu ve küratörlüdür; bütün dilleri kapsamaz.
- Oyun sürümü **0.3**, Score sürümü **1**. Gradle paket sürümü değiştirilmedi.
- İçerik kataloğu gerçek v0.3 registry'sinden üretilmiştir (130 kimlik). Legendary/Artifact item'lar
  başlangıç build'inde reddedilir; run sonunda elde edilmiş olabilirler.
- Kayıtlara başlangıç/son build, öldürme kategorileri, stage, süre, hasar dağılımları, para,
  sandık ve sonuç bilgileri girer. Kişisel dosyalar, donanım seri numarası, görsel veya açıklama gönderilmez.
- Ayrıntılı FPS/kare süresi ölçümleri eklenmedi. Summary verisi performans profili yerine geçmez.

## Bağlantı ve uygunluk kuralları

- Ana menü yüklenirken arka planda API sürümü ve oyuncu oturumu kontrol edilir. Bir hata oyunun
  açılmasını engellemez. İlk kontrol bitmeden başlatılan run offline kabul edilir; sonradan online olmaz.
- Mevcut oyuncunun token'ı yenilenir. Kimlik yenilenemiyorsa sessizce başka oyuncu hesabı açılmaz.
- Backend hazırken başlatılan run'ın sunucu oturumu açılana kadar simülasyon bekler; en fazla
  8 saniye sonra offline devam eder. Yeni bir ekran eklenmez. Bu kısa beklemenin görsel durumu 8c'ye aittir.
- Heartbeat 10 saniyede bir, gerçek zamanla çalışır; oyun hızı ve oyun içi pause bundan bağımsızdır.
  Ayarlar ekranında da sürer. İstemci 25 saniyelik temas boşluğunda, sunucu 30 saniyede uygunluğu kaldırır.
- Başarısız bağlantı kontrolü run'ı kalıcı şekilde offline yapar. Yeniden bağlanmak o run'ı kurtarmaz.
  Ağın iki başarılı kontrol arasındaki çok kısa kesintileri kesin olarak tespit edilemez.
- Uygulama arka planda uzun süre çalışmazsa aynı zaman aşımı uygulanır. Run oynamaya devam edebilir.
- Offline run'lar için disk kuyruğu/retry yoktur. Test Map hiçbir online run başlatmaz veya göndermez.
- Sonuç tek kez gönderilir; sunucu aynı run ID'sine tekrarlanan isteği aynı kayıt olarak işler.
  Sunucu kaydı tamamlayıp yanıt ağda kaybolursa oyun bunu bilemeyebilir; yeni kayıt oluşturarak telafi etmez.
- Local Chronicals ve Score işlemleri sunucu kabulünden bağımsızdır; bağlantı hatası bunları silmez.

## Score ve sınırlar

Sunucu Score'u kendisi hesaplar:

`round_half_up((normal + unique × 3 + mini × 250 + final × 5000) × difficulty × (1 + stage × 0.1))`

Easy: 0.33 / Normal: 0.50 / Hard: 1.00. İstemcinin gönderdiği Score kullanılmaz.
Stage sınırları 25/35/50; Final Boss en fazla 1 ve yalnızca son stage'de olabilir.
Item'larla ek mini boss üretilebildiği için mini boss sayısı stage/5 ile kısıtlanmaz.
Genel makullük sınırı saniyede 1000 öldürme, simülasyon süresi sunucu duvar saatinin en fazla
2 katı + 10 saniye; online oturum en fazla 24 saat. Bunlar geniş hata/hile eleme sınırlarıdır,
oyun dengesinin veya tüm hilelerin kesin doğrulaması değildir.

Yeni `grisilith_leaderboard_all` tüm map/zorluklardan en iyi 10 run'ı döndürür; oyuncu başına
limit yoktur. Rank yalnız Score'a göre yarışma sırasıdır (1,2,2,4); eşit Score'lar süresi kısa
olan önce görünecek şekilde sıralanır. Sonraki eşitliklerde bitiş zamanı/run ID deterministikliği sağlar.
Altta oyuncunun en iyi sonucu, oyuncu ilk 10'da varsa 11. run gösterilir. 11. run yoksa boş durum gösterilir.
İsimler güncel profilden okunur; tam build veya özel summary başkalarına açılmaz.
İlk aşamadaki map-bazlı eski RPC geriye uyumluluk için durur; yeni arayüz onu kullanmaz.
Ana menü leaderboard'u 30 saniyede, boştaki bağlantı kontrolü 15 saniyede yenilenir.
Aktif run'larda önceki heartbeat çalışır. Ağ göstergesi anlık donanım sensörü değil son başarılı API durumudur.

## Kimlik saklama ve yayın öncesi işler

Token'lar `grisilith_online_session` adlı uygulama ayar dosyasında saklanır; log'lara gönderilmez,
Git'ten ve Android yedeklerinden hariç tutulur. Bu dosya işletim sistemi uygulama depolamasındadır;
özel bir şifreli kasa değildir. Destek için bu dosyayı göndermeyin.
Anonim hesabın oturum dosyası kaybolursa hesap kurtarma henüz yoktur.

Açık beta öncesinde CAPTCHA/uygun alternatif kayıt koruması, moderasyon geri bildirim süreci, veri toplama
bildirimi, hesap/veri silme ve saklama politikası tamamlanmalıdır. Anonim kayıt tek başına
kötüye kullanım koruması değildir. Tam sunucu simülasyonu olmadığından bu sistem hile geçirmez değildir.

## Yerel doğrulama

```
gradlew.bat -I tools/online-backend/smoke.gradle :lwjgl3:onlineBackendSmoke :lwjgl3:onlineScreenSmoke
gradlew.bat -I tools/online-backend/smoke.gradle :lwjgl3:onlineIdentityUiSmoke
gradlew.bat -I tools/combat-targeting/smoke.gradle :lwjgl3:scoreSmoke
node tools/online-backend/database-smoke.mjs
node tools/online-backend/name-policy-check.mjs
```

Veritabanı testi yalnızca yerel/in-memory PGlite PostgreSQL motorunu kullanır. Test bağımlılığı
`@electric-sql/pglite@0.5.10` paketini `tools/online-backend/runtime/package` altına çıkartın;
runtime klasörü Git'e dahil edilmez. Supabase auth şeması test içinde taklit edilir; hosted Auth
servisinin veya gerçek mobil/masaüstü ağ koşullarının yerini tutmaz.

Katalog yenilenirken `onlineCatalogExport` görevi registry'deki kimlikleri SQL olarak yazdırır.
Yayınlanmış migration'ı değiştirmek yerine yeni migration ekleyin.

Kaynaklar:
- https://supabase.com/docs/guides/database/functions
- https://supabase.com/docs/guides/database/postgres/row-level-security
- https://supabase.com/docs/guides/auth/auth-anonymous
- https://supabase.com/docs/guides/getting-started/api-keys
