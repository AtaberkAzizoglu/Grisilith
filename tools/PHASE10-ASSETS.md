# Faz 10a asset kaydı

## Praise

- Yol: `assets/ui/icons/praise_v1.png`
- Üretim: yerleşik ImageGen, yeni görsel, transparent_background=true.
- Prompt:

> Create a single game UI asset: an upright thumbs-up praise icon for a dark medieval fantasy leaderboard, warm antique gold bronze metal silhouette with subtle beveled hand-engraved edges. Clear unmistakable thumbs-up gesture, compact square composition, readable at 24 pixels, restrained ornament, high contrast against dark charcoal. No badge surround, no frame, no text, no letters, no background. True transparent background. Centered icon occupies 85 percent of canvas.

## Siege başlangıç yazısı

- Yol: `assets/ui/text/siege_intro_v1.png`
- Mevcut Fira Sans Condensed fontundan, aynı metin ve 1.45 ölçek korunarak
  4x çözünürlükte deterministik PNG dışa aktarımı; şeffaf arka plan.
- Metin: `Defend your home, and survive.`
- Yeniden üretim: `qa.Phase10aSmoke.exportSiegeArt`.
- Çalışma anında görsel 1/4 boyutta çizilir; eski yazının baseline konumu korunur.

## Seçili zorluk butonları

- Dosyalar: `assets/ui/buttons/difficulty_selected_easy_v1.png`,
  `assets/ui/buttons/difficulty_selected_normal_v1.png`,
  `assets/ui/buttons/difficulty_selected_hard_v1.png`.
- Yerleşik ImageGen ile, kullanıcının üç ekran kırpımı ayrı ayrı edit referansı olarak kullanıldı.
  `transparent_background=true`; dış alpha korunur, iç parşömen opaktır.
- Normal durum: mevcut arka plan çerçevesi ve yazı çizimi değişmedi.
- Seçili durum: tek PNG (başlık + Score dahil), görünür alpha sınırlarına göre ortalanır;
  normal ölçünün %115'i. Çizim ve tıklama alanı aynı merkez/boyut hesabını kullanır.
- Varyant metinleri: `EASY (25 STAGES)` / `Score x0.33`,
  `NORMAL (35 STAGES)` / `Score x0.50`, `HARD (50 STAGES)` / `Score x1.00`.
- Ortak üretim prompt'u (her varyantta ilgili metinler ve kendi referans görseli kullanıldı):

> Use case: precise-object-edit. Asset type: selected-state game difficulty button, isolated transparent PNG. Input image 1 is the edit target and exact geometry/typeface reference. Recreate the SAME ornate hand-inked rectangular parchment frame, same thin linework, exact pointed side ornaments, tiny central diamonds and corner flourishes, same proportions and the SAME distinctive medieval serif typeface as the reference. Do not redesign the frame, do not add metal, gold edging or new ornaments. Change only selected-state colors, add score subtitle, remove exterior background. Frame is dark espresso brown, darker than reference. Interior is fully OPAQUE dark warm brown parchment with subtle original crumpled paper grain, no transparent interior. Outside the frame is truly transparent alpha, including around pointed side ornaments; no rectangular paper backdrop or shadow. Main text PURE WHITE, centered, exactly [VARIANT HEADLINE]. Score subtitle WHITE, legible about 70% of main font size, centered below it, exactly [VARIANT SCORE]. Keep same typeface as original headline. All text fits inside frame with generous padding, main line at about 44% interior height and score at 72%. Wide button aspect ratio about 3:1, not a whole menu. Entire ornament must be visible, centered with minimal equal transparent padding, no cropping. No additional text. Preserve reference line shapes and dark parchment theme.
