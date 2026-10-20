# Grisilith v0.3.9 — Windows dağıtımı

Proje kökünde PowerShell açın:

```powershell
.\gradlew.bat :lwjgl3:clean :lwjgl3:packageWinX64
```

`clean`, önceki sürümün JAR'ının paket hazırlama klasöründe kalmasını engeller.
Bu yalnız desktop modülünün üretilmiş build dosyalarını temizler; gerçek oyuncu
kayıtlarını silmez. İlk çalıştırma/temizlik sonrası internetten build araçları ve
pakete gömülecek Windows Java ortamı indirilebilir.

Çıktı:

`lwjgl3/build/construo/dist/Grisilith-0.3.9-windows-x64.zip`

ZIP içinde `Grisilith-0.3.9/Grisilith.exe`, oyunun JAR'ı, gerekli runtime/app
klasörleri, çalıştırma talimatı ve lisans/atıf dosyaları bulunur.
Oyuncu ayrı Java kurmaz. Sadece EXE değil, ZIP'in tamamı dağıtılmalıdır.
`lwjgl3:distZip` aynı şey değildir; gömülü Java için `packageWinX64` kullanın.

Son kontrol: ZIP'i yeni bir klasöre çıkarın; EXE'yi açın; sesleri, offline oyunu
ve internet açıkken isim/leaderboard bağlantısını deneyin. Yeni profilde
1.000 coin olmalı; harcama yapıp yeniden açınca bakiye korunmalıdır. Kendi
cihazınızdaki mevcut profil yeni profil sayılmaz; bunun için kayıt silmek yerine
ayrı Windows kullanıcı hesabı veya temiz test cihazı tercih edin.

Yalnız kontrol/derleme için (ZIP oluşturmaz):

```powershell
.\gradlew.bat :lwjgl3:verifyReleaseJar :lwjgl3:stageReleaseNotices :core:verifyBetaStartingCoins
```

## Yayından önce açık kalan teyitler

- Kullanıcı tarafından sağlanan karakter/boss sesleri için dağıtım hakları.
- Tüm yerel/native bağımlılıkların (özellikle OpenAL Soft/Freetype vb.) lisans
  ve gerekiyorsa kaynak sağlama koşullarının son kontrolü. Otomatik JAR-notice
  toplama, tüm transitif/native bileşenlerin hukuki denetimi yerine geçmez.
- Çevrimiçi veriler için yayıncı iletişimi, saklama/silme yaklaşımı ve uygun
  gizlilik bildiriminin son hali. ONLINE-FEATURES.txt yalnız başlangıç açıklamasıdır.
- Public repo, `tools/audio-audition` altındaki eski kaynak/seçki seslerini ve
  Git geçmişini de açar. Oyuncu ZIP'inde olmayan bu arşivleri ayrıca değerlendirin.

Oyunun özgün kodu için bu aşamada MIT/GPL vb. lisans seçilmedi. Repo public
ayarına alınmadı, commit/push veya GitHub Release yayını yapılmadı.
