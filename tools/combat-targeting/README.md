# Targeting ve görsel düzenlemeleri

## Son revizyon

- Settings dört kompakt açılır liste kullanır; eski otomatik hedef sırası alt yazısı kaldırılmıştır.
- Ability Targeting: Prepare Aim / Instant Casting / Auto Casting. Eski boolean tercih yeni enum'a taşınır.
- Prepare Aim hem slot tuşuyla hem dünya üzerine sol fare tıklamasıyla erken onaylanır. UI buton tıklamaları onay sayılmaz.
- Instant Casting, ranged dahil, ekstra hazırlık süresi olmadan mevcut imleci kullanır. Auto Casting menzilde Final Boss > Mini Boss > Unique > Normal seçer; aynı sınıfta en yakın olan kazanır.
- Sling hazırlığında seviyeye uygun iniş/patlama alanları gösterilir.
- Chalk Dust'ın kırmızı alanı mevcut 2,5 saniyelik çizim animasyonu ile birlikte başlar. Animasyon sonunda doğrudan cast olur; ek 1 saniyelik bekleme yoktur. Instant modunda da animasyon korunur ve son andaki imleç konumu kullanılır.
- HP sayısı barın içine alındı. Durum sırası korunarak tamga sembolleri yerine Collection/Merchant'ın gerçek tamga ikonları kullanılır.
- Onaylanan beş karakter sheet'i `assets/characters/aligned_v4` altına; 16 karelik Lance ability animasyonu `assets/weapon_visuals/lance_spin_v4` altına eklendi.
- 978 PNG/JPG/WebP tarandı. 216 kullanılmayan/replaced görsel (44,02 MB) oyunun assets paketinden çıkarıldı, `retired-assets` içinde kurtarılabilir. Son tarama 762 görselde statik referans incelemesi gerektiren dosya bulmadı. Aktif/dinamik binici, summon ve efekt aileleri muhafaza edildi.
- `CAST_MODES_PASS`: beş ranged silah, mouse erken onay, Instant gecikme yokluğu, Auto rank, üç modda Chalk animasyon sonu cast ve imleç/hedef merkezi.

Alttaki ilk uygulama notları geçmiş davranışı belgelemek içindir; son revizyon önceliklidir.

## Uygulanan davranış

- Otomatik ability hedef sırası: Final Boss > Mini Boss > Unique > Normal; aynı sınıfta mesafe belirleyici.
- Normal saldırılar: Closest / Closest to mouse icon / Boss prioritized. Yeni silahlar da aynı seçiciyi kullanır.
- Double Swords ability hedef menzili normal menzilin 1,25 katıdır. Hedef yoksa kullanım reddedilir, cooldown tüketilmez ve hazır çerçevesi gösterilmez. Saldırı/ability çizimleri hedef merkezlidir.
- Ability Targeting varsayılan olarak hazırlık açıktır. İlk basış 1 saniyelik nişan/alan gösterimi başlatır; ikinci basış erken kullanır, basılmazsa süre bitiminde kullanır. Kullanıcı ikinci basış olmadan kullanımın gerçekleşmesini istemezse bu süre davranışı ayrıca değiştirilebilir.
- Hazırlık: Double Swords, Lance, Longsword, Warhammer, Whip, Orb of Sky, Amulet of Troll King. Steam Wand'ın mevcut hazırlığı bu ayara bağlandı. Chalk Meteor/Call Lightning çizimi başarılı olduktan sonra imleç ile alanı belirlenir; mevcut glyph çizme kuralı korunur.
- Normal menzil: ince, opak beyaz çember. Ability menzili: biraz daha kalın, opak kırmızı çember. İki ayar varsayılan Off. Hazırlık alanları %50 alfa ile ayrıca gösterilir.
- Totem ikinci basışta aynı konumda sabit kalır; hasar tickleri ve kalan süre yeniden başlamaz.
- Charger aktifken patlama alanının mevcut şarja bağlı ince gri çemberi görünür.
- Sekiz yeni tamga durum ikonu oyuncunun HP çubuğunun üstünde condition'lardan önce sıralanır. Aktif olmayan/tetiklenmemiş etkiler görünmez; sürekli çalışan pasifler etkinken görünür.
- Bear, Crocodile ve Spearman kesimleri özgün RGBA kaynaklarının bağlı bileşen sınırlarından yeniden ölçülmüştür. Kaynak çizimler değiştirilmemiştir.
- Mermi hasarı için açık bir işaret eklendi; sıfır yol katetmiş büyü mermileri de arı split trait'inde doğru sınıflanır.
- Normal Humanoid: Bandit %80 / Archer %20. Normal Undead: Warrior %40 / Zombie %40 / Skeleton Archer %20. Diğer türler, unique roll ve boss düzeni korunur.

## Doğrulama

`targetingSmoke`: dört hedef sınıfı, üç normal hedefleme modu, hedefsiz kullanım/cooldown, Double Swords ek menzil, erken/zaman aşımı kullanımı, gerçek native çizim, Totem sabitleme, sprite sınırları, sekiz ikon yükleme, arı split ayrımı.

Seed 42, 20.000 seçim: Humanoid Bandit 15984 / Archer 4016; Undead Warrior 8006 / Zombie 8032 / Archer 3962.

Mevcut `gearAdditionSmoke`, `physicalWeaponSmoke`, `characterRenderingSmoke` ve `weaponVisualSmoke` regresyonları da çalıştırıldı. Kullanıcının gerçek ayarları yerine yalnızca QA tercih klasörleri kullanıldı.

## Görsel üretim

Yerleşik imagegen kullanıldı. Tam promptlar, referanslar ve üretim yolları [art-prompts.json](art-prompts.json) içinde.

Yeni karakter sprite sheet'leri ve Lance animasyonu sadece önizlemedir. `assets` içine taşınmadı ve oyun registry/render kodlarına bağlanmadı. Aktif karakter görselleri değiştirilmedi.

[Önizleme galerisi](PREVIEWS.md)

Yeni tamga ikonları `assets/tamga_visuals/status_icons` altında oyunda kullanılmaktadır. Atlas kesimi `ImportStatusIcons.ps1` ile; düşman kesim sınırları `InspectEnemyCuts.ps1` ile tekrarlanabilir.
# 2026-10-15: preparation, settings steppers and summon-cut corrections

- Dagger and Boomerang always cast immediately, even in Prepare Aim and with no enemy in range. Auto Casting still chooses a ranked target when one exists, without requiring one for these two weapons.
- The four combat settings now use paired left/right arrow buttons and wrap through their options. Existing themed scrollbar arrow artwork was reused; the old dropdown popup implementation was removed.
- Ability targeting, damage execution and previews use the held weapon's position (including secondary carriers). Normal attack execution is unchanged. Previews match projectile collision sweeps, Bow's five trajectories, upgraded Hammer/Sling areas, Whip's actual ability range, Double Swords' capsules, and clamped spell placement. Steam previews now draw the actual outer damage radius instead of padded marker artwork.
- Tsunami retains its random direction, but that direction is chosen when drawing starts and shared with the cast so its preparation strip predicts the real path. Singularity additionally marks its initial explosion radius; later deaths can still enlarge that explosion as before.
- All five summon families were checked against their original source sheets. Eighty-eight frames were recut with transparent margins, preserved native pixel scale and stable feet pivots. Old production cuts are recoverable under `original-summon-cuts/`; `RepairSummonCuts.ps1` records the source paths and extraction boundaries.
- Witch warnings only draw during charging; animated spell effects remain after casting. Sling stones no longer draw a brown square underneath their sprite.
- Added native checks for summon margins, Witch warning lifetime, Sling fallback suppression, always-instant weapons and settings arrow navigation. Existing combat, gear and rendering regression suites are retained.

# 2026-10-15: Faz 7d final revisions

- Restored the Longsword cone and single Bow/Crossbow aim lines (Crossbow is thicker), without changing their actual damage calculations. Steam Wand now has a stronger inner circle over the same 50%-alpha outer preparation area.
- Tsunami has no preparation/range marker. At cast completion, it snapshots the player's position, chooses a random map edge, travels toward that position, and continues until its full wave leaves the map. Auto mode needs no enemy for this map-wide spell.
- Rogue Oathbreakers are exposed to weapon targeting through combat-only adapters, at mini-boss rank. Damage and conditions affect the real summon; these adapters never enter enemy counts, enemy rendering, spawning, or kill rewards. Player projectiles retain their existing rogue-summon collision path to avoid duplicate hits.
- Oathbreaker chooses bosses and the player at equal rank (distance breaks ties), then Unique enemies, then Normal enemies and other summons at equal rank. Existing player-acquisition/release distance hysteresis is retained.
- Combat choice panels are twice as wide and arranged below their labels. The static preparation hint was replaced by hover descriptions for all four settings, using the existing adaptive tooltip panel with larger text.
- Native checks cover Oathbreaker targeting in both directions, shared health/conditions and enemy-count isolation, plus Tsunami behavior in all three casting modes. Settings screenshots include normal and hover layouts.

