"""Ten existing coin recordings for audition only. No synthesis, layering or game changes."""
import concurrent.futures, hashlib, html, json, re, shutil, urllib.request
from pathlib import Path
import soundfile as sf

ROOT=Path(__file__).resolve().parent
SRC=ROOT/'sources/cashout-32';OUT=ROOT/'listening-pack-32'
RECORDINGS=[
 ('SpaceJoe',485769,'Coins Shake - 4','Kısa ve yoğun para sallama'),
 ('SpaceJoe',485768,'Coins Shake - 3','Daha uzun para şıngırtısı'),
 ('SpaceJoe',485767,'Coins Shake - 2','Para sallama, farklı kayıt'),
 ('SpaceJoe',485766,'Coins Shake - 1','Para sallama, diğer varyant'),
 ('SpaceJoe',485776,'Heavy Money Bag - 5','Ağır para kesesinin bırakılması'),
 ('AKkingStudio',684167,'Coins Falling Into Pile','Para yığınının üzerine dökülen paralar'),
 ('haulaway',733333,'Bag of Coins Drop','Ödeme yapar gibi masaya bırakılan para kesesi'),
 ('clairinski',184372,'Coin Spill','Ahşap masaya dökülen bir avuç para'),
 ('keatonmarek',533781,'Shaking Coins in a Jar 2','Cam kavanozda sallanan paralar'),
 ('black_trillium',688750,'Playing and dropping Coins','Para yığınına küçük ve büyük miktarlarda para bırakma'),
]

def fetch(url,target):
 if not target.exists():
  req=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0 Grisilith audition source verification'})
  with urllib.request.urlopen(req,timeout=35) as response:target.write_bytes(response.read())
 return target

def prepare(pair):
 i,(author,sound,title,note)=pair
 folder=SRC/str(sound);folder.mkdir(parents=True,exist_ok=True)
 url=f'https://freesound.org/people/{author}/sounds/{sound}/'
 page=fetch(url,folder/'source-page.html').read_text(encoding='utf-8',errors='replace')
 assert re.search(r'https?://creativecommons.org/publicdomain/zero/1.0',page),(author,sound,'CC0 license missing')
 previews=re.findall(r'https://cdn\.freesound\.org/previews/[^"\s<>]+-hq\.mp3',page)
 assert previews,(author,sound,'HQ preview missing')
 preview=html.unescape(previews[0]);original=fetch(preview,folder/'preview.mp3')
 code=f'CO{i}';target=OUT/(code+'.mp3');shutil.copyfile(original,target)
 data,rate=sf.read(target,always_2d=True);assert data.size and abs(data).max()>0
 return dict(code=code,title=title,note=note,author=author,source=url,
  license='https://creativecommons.org/publicdomain/zero/1.0/',file=target.name,duration=len(data)/rate,
  preview_url=preview,preview=True,original=str(original.relative_to(ROOT)),
  sha256=hashlib.sha256(target.read_bytes()).hexdigest(),processing='None — unchanged public HQ preview')

def main():
 OUT.mkdir(exist_ok=True)
 with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:rows=list(pool.map(prepare,enumerate(RECORDINGS,1)))
 assert len(rows)==10 and len({r['sha256'] for r in rows})==10
 (OUT/'SOURCES.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
 (OUT/'LICENSE-SOURCES.txt').write_text('\n\n'.join(f'{r["code"]} — {r["title"]}\n{r["author"]}\n{r["source"]}\n{r["license"]}\nPublic HQ preview: {r["preview_url"]}\nSHA256: {r["sha256"]}\nNo processing.' for r in rows),encoding='utf-8')
 template=(ROOT/'listening-pack-30/DINLE.html').read_text(encoding='utf-8')
 css=template[template.index('<style>'):template.index('</style>')+8]
 script=template[template.index('<script>'):template.index('</script>')+9]
 cards=[]
 for r in rows:
  cards.append(f'<article><h3>{r["code"]} · {html.escape(r["note"])}</h3><audio controls preload="none" src="{r["file"]}"></audio><p>{html.escape(r["title"])} · {html.escape(r["author"])} · {r["duration"]:.2f} s</p><p>CC0 · Orijinal, herkese açık HQ önizleme; kesme/üretim/katmanlama yok.</p><a href="{r["source"]}">Kaynak ve lisans</a> <button data-code="{r["code"]}">Seç / kaldır</button></article>')
 page='<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Seçki 32 · Cash Out</title>'+css+'''<header><h1>Seçki 32 · Cash Out · CO1–CO10</h1><p>Bol para şıngırtısı için 10 farklı hazır kayıt. Yeni ses üretilmedi. Bu seçki ve önceki item seçimleri henüz oyuna eklenmedi.</p><p>Kayıtları tam uzunluklarıyla dinleyebilirsin; seçilince gerekiyorsa satın alma geri bildirimine uygun bölümü kesilir. Eski CO kodlarıyla karışmaması için “Seçki 32 CO...” şeklinde belirtebilirsin.</p><label>Dinleme düzeyi <input id="volume" type="range" min="0" max="1" step=".01" value=".45"></label><p id="selection">Seçim: —</p><button id="copy">Kodları kopyala</button> <a href="LICENSE-SOURCES.txt">Kaynaklar / lisanslar</a></header><main><div class="grid">'''+''.join(cards)+'</div></main>'+script+'</html>'
 (OUT/'DINLE.html').write_text(page,encoding='utf-8')
 print('CASHOUT_32_PASS: 10 distinct CC0 recordings, HQ previews decoded, source license pages retained, no game assets/code changed.')
 print([(r['code'],round(r['duration'],2),r['title']) for r in rows])
if __name__=='__main__':main()
