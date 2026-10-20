"""Ready-made item sound proposals, byte-exact originals, not installed in the game."""
import json,html,shutil,hashlib
from pathlib import Path
import soundfile as sf

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'listening-pack-31'
CC0='https://creativecommons.org/publicdomain/zero/1.0/'
sources={
 'rpg':('sources/rpg-audio/Audio','Kenney','https://kenney.nl/assets/rpg-audio'),
 'rd':('sources/rubberduck-rpg/Audio','rubberduck','https://opengameart.org/content/80-cc0-rpg-sfx'),
 'magic':('sources/missing-sfx-30/magic','JaggedStone','https://opengameart.org/content/magic-spell-sfx'),
 'elec':('sources/missing-sfx-30/electricity','faxcorp','https://opengameart.org/content/electricity-game-sound-pack'),
 'cure':('sources/missing-sfx-30/cure','Someoneman','https://opengameart.org/content/cure-magic'),
}
def c(key,name,note):return(key,name,note)
def magic(*nums):return [c('magic',{1:'magical_1_0.ogg',6:'magical_6_0.ogg',7:'magical_7_0.ogg'}.get(n,f'magical_{n}.ogg'),f'Hazır büyü tınısı {n}') for n in nums]
groups=[
 ('AB','Animated Broom','Toplama/hareket geri bildirimi. 3 saniye, ekstra kopya başına −0,3 saniye; önerilen alt sınır 0,3 saniye.',[
  c('rpg','cloth1.ogg','Hafif kumaş sürtünmesi'),c('rpg','cloth2.ogg','İkinci kuru sürtünme'),c('rpg','cloth3.ogg','Daha dolgun hareket'),c('rpg','cloth4.ogg','Farklı kumaş hareketi'),c('rpg','clothBelt.ogg','Kısa deri/kumaş hareketi')]),
 ('BT','Bone Totem — rulet','Rulet başına tek kayıt; seçimden sonra 1,5 saniyelik rulet süresiyle eşleştirilecek.',[
  c('rpg','handleCoins.ogg','Madeni parçalar'),c('rpg','handleCoins2.ogg','İkinci parça hareketi'),c('rd','chain_01.ogg','Kuru zincir hareketi'),c('rd','item_stone_01.ogg','Taşların hareketi'),c('rd','item_wood_03.ogg','Ahşap tıkırtı')]),
 ('HA','Bone Totem — Healing Aura','Sonuç başına tek geri bildirim; iyileştirme tiklerinde ortak HE10 bütçesi geçerli.',[
  *[c('cure',f'Cure{i}.wav',f'Hazır Cure Magic {i}') for i in (1,3,5)],*magic(2,6)]),
 ('PA','Bone Totem — Protecters Area','Koruyucu alan oluşumu; her hedefe veya her hasar tikine ayrı ses yok.',[
  c('elec','shieldhit.wav','Kalkan enerjisi'),c('elec','powerup.wav','Enerji yükselişi'),*magic(1,4,5)]),
 ('TR','Bone Totem — Buried Treasure','Hazine sonucu başına tek ses.',[
  c('rd','item_coins_01.ogg','Para 1'),c('rd','item_coins_02.ogg','Para 2'),c('rd','item_coins_03.ogg','Para 3'),c('rd','item_gem_01.ogg','Değerli taş'),c('rpg','handleCoins2.ogg','Hazine paraları')]),
 ('HG','Bone Totem — Hourglass','Zaman etkisinin başlangıcında tek ses.',magic(1,2,3,5,7)),
 ('TD','Bone Totem — Toads','Toplu ortaya çıkma için tek geri bildirim; her kurbağaya ayrı ses yok.',[
  c('rd','creature_slime_01.ogg','Organik çıkış 1'),c('rd','creature_slime_02.ogg','Organik çıkış 2'),c('rd','creature_slime_03.ogg','Organik çıkış 3'),*magic(4,6)]),
 ('LG','Bone Totem — Lose Half Gold','Altın kaybı sonucu başına bir ses.',[
  c('rd','item_coins_04.ogg','Dağılan paralar'),c('rd','lock_01.ogg','Kuru kapanış'),c('rd','metal_03.ogg','Sert metal vurgu'),c('rpg','dropLeather.ogg','Düşen kese hissi'),c('rpg','doorClose_3.ogg','Ağır olumsuz vurgu')]),
 ('HB','Bone Totem — Hostile Mini Boss','Tehdit sonucu için bir ses; ayrıca yeni bir düşman doğma sesi eklenmeyecek.',[
  c('rd','creature_monster_01.ogg','Canavarsı uyarı 1'),c('rd','creature_monster_02.ogg','Canavarsı uyarı 2'),c('rd','creature_roar_01.ogg','Tehdit kükremesi'),*magic(3,5)]),
 ('SG','Bone Totem — Shrinking Ground','Daralan alanın başlangıç geri bildirimi.',[
  c('rd','stones_01.ogg','Taş hareketi 1'),c('rd','stones_02.ogg','Taş hareketi 2'),c('rd','stones_03.ogg','Taş hareketi 3'),c('rd','stones_04.ogg','Taş hareketi 4'),c('rpg','creak3.ogg','Gerilen zemin')]),
 ('TC','Bone Totem — Totem Curse','Lanet sonucu başına bir ses.',[
  *magic(3,4,5),c('rd','spell_01.ogg','Karanlık büyü alternatifi 1'),c('rd','spell_02.ogg','Karanlık büyü alternatifi 2')]),
 ('CC','Close Call','Gerçek dodge değil, isabet etmeyen yakın saldırı geçişi; yalnız tetiklendiğinde.',[
  c('rd','blade_01.ogg','Hızlı hava kesişi 1'),c('rd','blade_02.ogg','Hızlı hava kesişi 2'),c('rd','blade_03.ogg','Hızlı hava kesişi 3'),c('rpg','knifeSlice.ogg','Kısa savrulma 1'),c('rpg','knifeSlice2.ogg','Kısa savrulma 2')]),
 ('LE','Loot the Enemies','Özel collectible toplandığında. İyileşme ayrıca HE10 ile, limitli ve düşük sesli. LE5 daha önce seçtiğin LT5 ile aynı.',[
  *[c('rd',f'item_coins_{i:02}.ogg',f'Toplama {i}') for i in range(1,5)],c('rpg','handleCoins.ogg','Seçilmiş ortak LT5')]),
 ('CP','Call the Portal','CP1 daha önce seçtiğin PO1. Ortak portal sesini korumak istersen yeniden seçmek gerekmiyor.',[
  c('elec','teleport_1.wav','Seçilmiş ortak PO1'),c('elec','powerup.wav','Enerji açılışı'),*magic(1,4,7)]),
 ('IM','Is It Merchant?','Merchant belirdiğinde tek ses, ekranda kaldığı sürece tekrar yok.',[
  c('rpg','handleSmallLeather.ogg','Kese hareketi'),c('rpg','handleCoins2.ogg','Tüccar paraları'),c('rpg','doorOpen_1.ogg','Dükkân gelişi'),c('rd','item_gem_02.ogg','Kısa eşya bildirimi'),c('rpg','metalLatch.ogg','Kasa/kilit bildirimi')]),
 ('SH','Second Heart','Ölümden kurtulma gerçekleştiğinde; oyuncu ölüm sesi çalmamalı.',[
  *magic(1,4,7),c('elec','recharge.wav','Yeniden güç kazanma'),c('cure','Cure8.wav','Güçlü iyileştirme alternatifi')]),
 ('PT','Pit Trap','Tuzak gerçekten tetiklendiğinde tek ses, bekleme döngüsü yok.',[
  c('rd','stones_01.ogg','Zemin çökmesi 1'),c('rd','stones_04.ogg','Zemin çökmesi 2'),c('rpg','metalLatch.ogg','Mekanizma bırakması'),c('rd','wood_04.ogg','Kapanan tahta mekanizma'),c('elec','groundhit.wav','Sert zemin darbesi')]),
 ('BV','Sacrificed Blood Vial','Gerçekten can harcayıp cooldown atlandığında tek ses.',[
  c('rd','creature_slime_04.ogg','Organik sıvı 1'),c('rd','creature_slime_02.ogg','Organik sıvı 2'),c('rd','spell_01.ogg','Kısa kan büyüsü alternatifi'),*magic(3,5)]),
 ('CO','Cash Out','Yalnız markette başarıyla satın alındığında; run tetiklenmesi değil.',[
  *[c('rd',f'item_coins_{i:02}.ogg',f'Para sonucu {i}') for i in (1,3,4)],c('rpg','handleCoins2.ogg','Daha dolgun para hareketi'),c('rd','item_gem_04.ogg','Parlak alışveriş sonucu')]),
]

def main():
 OUT.mkdir(exist_ok=True);records=[];sections=[]
 for prefix,title,note,choices in groups:
  assert len(choices)==5
  cards=[]
  for i,(key,name,description) in enumerate(choices,1):
   folder,author,url=sources[key];files=list((ROOT/folder).rglob(name));assert len(files)==1,(key,name,files)
   src=files[0];code=f'{prefix}{i}';target=OUT/(code+src.suffix);shutil.copyfile(src,target)
   data,rate=sf.read(target,always_2d=True);assert data.size and abs(data).max()>0
   digest=hashlib.sha256(target.read_bytes()).hexdigest();assert digest==hashlib.sha256(src.read_bytes()).hexdigest()
   r=dict(code=code,group=title,note=description,file=target.name,author=author,source=url,license=CC0,
    original=str(src.relative_to(ROOT)),sha256=digest,duration=len(data)/rate,processing='None — byte-exact original')
   records.append(r)
   cards.append(f'<article><h3>{code} · {html.escape(description)}</h3><audio controls preload="none" src="{target.name}"></audio><p>{r["duration"]:.2f} s · {author} · CC0</p><p>Orijinal dosya, işlem yok.</p><a href="{url}">Kaynak / lisans</a> <button data-code="{code}">Seç / kaldır</button></article>')
  sections.append(f'<section id="{prefix}"><h2>{html.escape(title)} · {prefix}1–5</h2><p>{html.escape(note)}</p><div class="grid">'+''.join(cards)+'</div></section>')
 assert len(records)==95
 (OUT/'SOURCES.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
 (OUT/'LICENSE-SOURCES.txt').write_text('\n\n'.join(f'{r["code"]} — {r["group"]}\n{r["author"]}\n{r["source"]}\n{r["license"]}\nOriginal: {r["original"]}\nSHA256: {r["sha256"]}\nNo processing.' for r in records),encoding='utf-8')
 old=(ROOT/'listening-pack-30/DINLE.html').read_text(encoding='utf-8')
 css=old[old.index('<style>'):old.index('</style>')+8]
 script=old[old.index('<script>'):old.index('</script>')+9]
 nav=''.join(f'<a href="#{prefix}">{html.escape(title)}</a>' for prefix,title,_,_ in groups)
 page='<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Seçki 31 · Item Sesleri</title>'+css+'''<header><h1>Seçki 31 · Item Sesleri</h1><p>19 olay × 5 seçenek = 95 öneri. Yeni ses üretilmedi; hazır CC0 kayıtları kullanıldı. Bu item seçenekleri henüz oyuna eklenmedi. Bazı ortak kayıtlar farklı olaylarda tekrar öneriliyor; 95 bağımsız kayıt değildir.</p><p>Gold Pouch ortak HE10 iyileştirme sesini kullanacak, ayrı ses seçimine gerek yok. Call the Portal için PO1 zaten seçildi; CP grubu yalnız alternatif dinlemek içindir.</p><label>Dinleme düzeyi <input id="volume" type="range" min="0" max="1" step=".01" value=".45"></label><p id="selection">Seçim: —</p><button id="copy">Kodları kopyala</button> <a href="LICENSE-SOURCES.txt">Lisanslar / kaynaklar</a></header><nav>'''+nav+'</nav><main>'+''.join(sections)+'</main>'+script+'</html>'
 (OUT/'DINLE.html').write_text(page,encoding='utf-8')
 print('ITEM_SFX_31_PASS: 19 groups x 5 = 95 choices, source-exact CC0 files, all decoded, provenance saved; not installed.')
if __name__=='__main__':main()
