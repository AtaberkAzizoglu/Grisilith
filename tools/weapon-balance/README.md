# Faz 7a — Melee ve ranged hasar incelemesi

4 Ekim 2026 — değişiklik öncesi analiz arşivi. Aşağıdaki CSV/JSON ve deney modeli ilk incelemenin anlık görüntüsüdür; sonradan uygulanan Dagger, Crossbow, kritik ve ability düzeltmelerini içermez. Bu eski modeli güncel oyun dengesi olarak yorumlamayın veya sonuç dosyalarını güncel veri diye yeniden üretmeyin.

## Rapor ve tablolar

[Etkileşimli rapor](C:/Users/aaata/.cursor/projects/empty-window/canvases/weapon-balance.canvas.tsx) stat ve seviye seçilebilen normal saldırı, ability, çok-hedef ve Monte Carlo tablolarını, formülleri ve denge önerilerini içerir.

[CSV](<C:/Android Projects/Grisilith/tools/weapon-balance/results.csv>) / [JSON](<C:/Android Projects/Grisilith/tools/weapon-balance/results.json>) tüm 160 satırın tam sayısal sonuçlarını içerir. [Deney kodu](<C:/Android Projects/Grisilith/tools/weapon-balance/WeaponBalance.kt>) tekrar çalıştırılabilir.

## Sonuç

Başlangıçta Bow, Longsword, Double Swords ve Dagger'ın temel DPS değerleri yakın. Shield savunma ve geri itme nedeniyle daha düşük hasar taşıyabilir. Lance ve Crossbow tek hedefte yaklaşık %25–33 geride; menzil/alan/delme avantajları değerlendirilmeden aynı DPS'ye zorlanmamalı.

En büyük ölçekleme sorunu Dagger'da. Tüm statlar 40, Lv5 testinde yakın saldırı 1.224 kritiksiz hasar / 11.912,9 temel DPS; atış 360 hasar / 3.503,1 DPS; Bow 480 hasar / 2.840,6 DPS verir. Bunlar savunma öncesi ve aynı stat senaryosunda kesintisiz isabet varsayımıyla hesaplandı. All40 gerçek başlangıç build'i değil, stres testidir.

## Mevcut ana formüller

`R(x)=max(1,round(x))`. Tam sayılı sonuçlarda yuvarlama sırası korunmuştur.

- Melee: `R(Base × STR × L × secondary)`; ranged: `R(Base × DEX × L × secondary)`.
- `secondary=max(0.20, 1+Σ[(ikincil stat−10)×0.08×weight])`; ana stat ikinci kez hesaplanmaz.
- Dagger yakın: `R(9×STR×max(0.20,1+0.08×(DEX−10)))`; atış: `R(9×DEX)`. Knife Swarm şu an yakın formülünü kullanır.
- Shield: `R(15×AC×0.45)`; Lv4+ kritik uygulandıktan sonra `0.15×STR` eklenip yuvarlanır. Ek bonus kritikle çarpılmaz.
- Kritik şansı: `clamp((karakter taban kritik%+2×Crit Rate)/100,0,1)`.
- Kritik çarpanı: `max(1,(karakter taban kritik hasar%+3×Crit Damage)/100)`.
- Normal kritik: `R(D×C)`; Dagger Lv4+ normal saldırı kritiğine ayrıca ×1.5. Yakında iki aşamalı, atışta tek aşamalı yuvarlama olabilir. Ability bıçakları bu yükseltmeyi almaz.
- `APS=[APS0+(APSmax−APS0)×clamp(Attack Speed/maksimum stat,0,1)]×seviye hızı`. Deneyde maksimum stat 50, APSmax=5×APS0.
- Seviye hızları Lv1–5: `1 / 1.15 / 1.22 / 1.22 / 1.30`. **Seviye hasar çarpanı tüm seviyelerde ×1.** Katalogdaki ×1.10/1.20/1.35/1.50 parametreleri fabrikada kullanılmaz.

Ham taban hasarlar: Double Swords 7; Longsword 11; Lance 12; Shield 15; Dagger 9; Bow 12; Crossbow 15.

## Önce giderilmesi gereken tutarsızlıklar

1. Dagger'ın yakın saldırısı iki statın çarpımıyla büyür. Knife Swarm primary override vermediğinden STR temelli yakın formülünü kullanır. Yakın, atış ve Knife Swarm için ortak `9×DEX` öneriyorum.
2. Crossbow normal projectile hedef bütçesi 1; ilk isabet sonrası kapanır. İkinci hedefte ×0.5/×1 hasar ve 170/310 menzil uzaması bu haliyle çalışmaz. Önce hedef bütçesi 2 ile doğrulanmalı, hemen taban buff yapılmamalı.
3. Shield Burst'te kritik hesaplanır, sonra forcedBaseDamage bu sonucu ezer; kritik etiketi kalabilir ama hasar artmaz. Gerçek kritik uygulanmalı veya bu ability'nin kritiği kapatılmalı.
4. Seviye hasar politikası netleştirilmeli: ×1 bilinçli ise kullanılmayan katalog parametreleri kaldırılmalı; artış isteniyorsa ayrı bir denge kararıyla açılmalı. Körlemesine ×1.5 açmak tüm oyunun dengesini değiştirir.

Shield yığını aktivasyonda totalScore'dan kaldırılır, burst hesabına yakalanan yığın eklenir; bu işlem tek başına çift sayım değildir.

## Uygulanmamış playtest adayları

- Air Slash Lv3 2D / Lv5 2.5D; 20 saniye cooldown. Mevcut Lv5 All10 tam HP hedefte 110 / 35 saniye; aday 275 / 20 saniye.
- Spin Attack Lv3 2D / Lv5 2.5D; 25 saniye cooldown. Mevcut 1D/1.5D ve 40 saniye tek hedefte düşük katkı verir.
- Shield Burst normal AC tabanlı vuruşun 1–1.5 katı. All10 Lv5 +15 yığınlı AC=30 senaryosunda mevcut 23 yerine yaklaşık 203–304. Bu 9–13 kat değişikliktir, önce 1D alt sınırıyla ve geri itme/kaybedilen savunma birlikte test edilmeli.
- Lance taban 12→14: All10 Lv5 temel DPS yaklaşık 108.7→126.7. Double Swords ve Bow tabanı şimdilik korunmalı. Crossbow delme düzeltmesinden önce buff edilmemeli.

Bunlar final denge değerleri değil. Son karar için düşman HP/savunma, öldürme süresi, gerçek isabet yüzdesi ve oyuncunun hareket süresi gerekir. Magic silahlar ve enemy incelemesi sonraki aşamaya bırakıldı.

## Deney yöntemi

4 stat profili × 5 seviye × 8 saldırı modu = 160 satır; her satır 5.000 Monte Carlo denemesi, toplam 800.000. Seed 20261004. Tüm satırlarda ortalamanın matematiksel beklentiden uzaklığının 6 standart hata + 1 hasardan küçük olduğu doğrulandı.

Production hasar hesaplayıcısı, statlar, silah bonus senkronizasyonu, seviye tanımları ve normal saldırı timer'ı kullanılır. Ability çarpan/isabet davranışı koddan modellenir; bu **dünya/çarpışma simülasyonu değildir**. Hedef savunmasız, ölümsüz, hep menzilde; overkill, kaçırma, uçuş gecikmesi, engeller, trait, item, armor ve tamga yok. Ability normal saldırıyı durdurmaz.

Profiller silah bonusundan önce tüm statlar 10/20/40 veya STR10, diğerleri40. Tek silahın seviye stat bonusları dahildir. Nötr karakter taban kritik %0, taban kritik hasar %100; gerçek karakterler farklı olabilir.

Normal saldırılar 240 Hz timer ile `0≤t<60 saniye` için sayılır. Ability ilk anda hazır, cooldown sonunda kullanılır. Bow tam nişan 1 saniye, Crossbow 2 saniye dahil; erken ateşleme yok. Dar tek hedefte Bow/Dagger salvo/halka başına bir isabet, sekme yok; Double Swords 5/7, diğerleri bir isabet. Air Slash için tam HP. Shield hareketli, pasif yığın 0, burst 60 saniyelik toplamdan hariç. +15 yığınlı burst yalnızca ability tablosunda gösterilir.

P05–P95 denemelerin orta %90'ıdır; %95 güven aralığı değildir. Monte Carlo kritik rastgeleliğini ölçer, gerçek oyun değişkenliğini değil.

## Kaynaklar

- [Temel hasar](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/weapon/runtime/WeaponDamageCalculator.kt:205>)
- [Gerçek saldırı hızı](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/weapon/runtime/WeaponRuntime.kt:381>)
- [Kritik formülleri](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/statistics/PlayerStats.kt:818>)
- [Seviye fabrikası](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/weapon/definition/WeaponLevelProgressionFactory.kt:48>)
- [Ability uygulamaları](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/weapon/skill/PhysicalWeaponAbilityRuntime.kt:315>)
- [Projectile kritik/hedef bütçesi](<C:/Android Projects/Grisilith/core/src/main/kotlin/com/azizoglu/grisilith/weapon/Projectile.kt:639>)

Yeniden çalıştırma: `gradlew.bat -I tools/weapon-balance.gradle lwjgl3:weaponBalance --console=plain`. Yalnızca rapor JSON/CSV dosyalarını yeniler; oyun kayıtlarını veya denge değerlerini değiştirmez.
