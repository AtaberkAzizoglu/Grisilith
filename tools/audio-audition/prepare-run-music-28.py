"""Official, attribution-licensed music audition only; does not install game music."""
import concurrent.futures
import hashlib
import html
import json
from pathlib import Path
import time
import urllib.parse
import urllib.request
import soundfile as sf

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "listening-pack-28"
BASE = "https://incompetech.com/music/royalty-free/"
LICENSE = "https://creativecommons.org/licenses/by/4.0/"
PACK_TITLE = "Seçki 28 · Harita ve Market müzikleri"
PACK_DESCRIPTION = "4 kategori × 10 ayrı tam parça. Henüz oyuna eklenmedi. Mini/final boss müziği bu seçkide yok."
PACK_LABEL = "MUSIC28"
# Notes are editorial suggestions, not a claim that these were auditioned by a human.
GROUPS = [
    ("CF", "Crolymnos Forest", [
        ("Magic Forest", "Gizemli orman; hafif karanlık.", True),
        ("Ascending the Vale", "Doğal ve pastoral; daha umutlu.", True),
        ("Enchanted Valley", "Hafif ve büyülü; daha aydınlık.", False),
        ("Celtic Impulse", "Halk ezgisi; ölçülü hareket.", True),
        ("Lord of the Land", "Ortaçağ yolculuğu; hafif tedirginlik.", False),
        ("Skye Cuillin", "Kelt rengi; daha geniş atmosfer.", False),
        ("Windswept", "Sakin dış mekân; gitar ve yaylılar.", False),
        ("Dreamy Flashback", "Masalsı ve hafif; daha kısa.", False),
        ("Frozen Star", "Arp ve flüt; dingin büyü.", False),
        ("Artifact", "Doğal ritimler; daha gölgeli alternatif.", False),
    ]),
    ("VS", "Vogtsweier Siege", [
        ("Crusade", "Kuşatma ağırlığı; koyu orkestral.", True),
        ("The Descent", "İleri akan savaş gerilimi.", True),
        ("Intrepid", "Daha ağır ve karanlık yolculuk.", True),
        ("Oppressive Gloom", "Baskıcı ve yavaş savaş atmosferi.", False),
        ("Stormfront", "Vurmalılar önde; tehditkâr ritim.", False),
        ("Hero Down", "Dramatik orkestral; sonu büyür.", False),
        ("Morgana Rides", "Ölçülü hareket; karanlık ritim.", False),
        ("Night Vigil", "Kuşatma öncesi bekleyiş; daha sakin.", False),
        ("Five Armies", "Üst tempo alternatifi; daha güçlü.", False),
        ("Clenched Teeth", "En hareketli sınır; kısa parça.", False),
    ]),
    ("HD", "Hallowgrave Depths", [
        ("Ossuary 1 - A Beginning", "Yavaş, tekinsiz derinlik.", True),
        ("Lightless Dawn", "Koyu atmosfer; gerilimli boşluk.", True),
        ("The Dread", "Düşük uğultu; huzursuz bekleyiş.", True),
        ("Dark Fog", "Daha ritmik karanlık alternatif.", False),
        ("Ossuary 2 - Turn", "Giderek rahatsız edici atmosfer.", False),
        ("Ossuary 6 - Air", "Ağır, yaklaşan tehlike hissi.", False),
        ("Echoes of Time", "Derin doku; uzaktaki çanlar.", False),
        ("Darkest Child", "Gotik, ince melodili gerilim.", False),
        ("The Chamber", "Gizemli oda; orkestral büyüme.", False),
        ("Decay", "Metal ve çan dokulu tekinsizlik.", False),
    ]),
    ("MK", "Market / Güvenli durak", [
        ("Midnight Tale", "Sakin lavta; güvenli mola.", True),
        ("Teller of the Tales", "Yumuşak ve sade teller.", True),
        ("Angevin", "Ağırbaşlı, hafif karanlık han.", True),
        ("Thatched Villagers", "Sıcak ve canlı köy havası.", False),
        ("Folk Round", "Kamp ateşi; hafif gölgeli.", False),
        ("Pippin the Hunchback", "Ölçülü ve gizemli ortaçağ.", False),
        ("Master of the Feast", "Daha neşeli ve kalabalık han.", False),
        ("Achaidh Cheide", "Kelt halk ezgisi; hareketli mola.", False),
        ("The Britons", "Lavta ve flüt; klasik tavern.", False),
        ("Fiddles McGinty", "En hareketli folk alternatifi.", False),
    ]),
]

def fetch(url):
    for attempt in range(3):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "Grisilith-audio-audition/1.0"}), timeout=90) as response:
                return response.read()
        except Exception:
            if attempt == 2:
                raise
            time.sleep(1 + attempt)

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    catalog = json.loads(fetch(BASE + "pieces.json"))
    by_title = {row["title"]: row for row in catalog}
    site = fetch(BASE + "music.html").decode("utf-8")
    assert "By Attribution 4.0" in site
    selections = []
    for prefix, group, rows in GROUPS:
        for i, (title, note, recommended) in enumerate(rows, 1):
            piece = by_title[title]
            selections.append(dict(code=f"{prefix}{i}", group=group, title=title,
                note=note, recommended=recommended, artist="Kevin MacLeod",
                length=piece["length"], instruments=piece.get("instruments"),
                isrc=piece["isrc"], source=BASE + "index.html?isrc=" + piece["isrc"],
                download=BASE + "mp3-royaltyfree/" + urllib.parse.quote(piece["filename"]),
                license=LICENSE, file=f"{prefix}{i}.mp3"))
    assert len(selections) == 40 and len({row['isrc'] for row in selections}) == 40

    def download(row):
        target = OUT / row["file"]
        if not target.exists() or target.stat().st_size < 10000:
            payload = fetch(row["download"])
            assert len(payload) > 10000 and not payload.lstrip().startswith(b"<"), row["code"]
            target.write_bytes(payload)
        data = target.read_bytes()
        row["sha256"] = hashlib.sha256(data).hexdigest()
        row["bytes"] = len(data)
        info=sf.info(target)
        row["catalog_length"]=row["length"]
        row["decoded_seconds"]=info.duration
        row["sample_rate"]=info.samplerate
        row["channels"]=info.channels
        seconds=round(info.duration)
        row["length"]=f"{seconds//3600:02}:{seconds//60%60:02}:{seconds%60:02}"
        print(f'{row["code"]}: {row["title"]} ({len(data)} bytes)', flush=True)

    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        list(pool.map(download, selections))
    (OUT / "MUSIC-SOURCES.json").write_text(json.dumps(selections, ensure_ascii=False, indent=2), encoding="utf-8")
    credits = [PACK_TITLE + " — 40 müzik adayı, yalnız dinleme; oyuna kurulmadı.",
        "Kaynak: " + BASE + "music.html", "Lisans doğrulama: resmî katalogdaki Attribution Code — CC BY 4.0.",
        "Ücretsiz kullanımda eser adı, sanatçı, kaynak, lisans ve varsa değişiklik bilgisi belirtilmeli.",
        "Buradaki MP3'ler değiştirilmemiş tam parçalar; loop kesimi veya ses yüksekliği işlemi yapılmadı.",
        "Tarz notları katalog/enstrüman bilgilerine dayalı editoryal adaylandırmadır; dinleyip son seçimi yapın.", ""]
    for row in selections:
        credits += [f'{row["code"]} — "{row["title"]}" Kevin MacLeod (incompetech.com)',
            "Licensed under Creative Commons: By Attribution 4.0 License", LICENSE,
            row["source"], "SHA256: " + row["sha256"], ""]
    (OUT / "LICENSE-SOURCES.txt").write_text("\n".join(credits), encoding="utf-8")
    sections = []
    for prefix, group, _ in GROUPS:
        cards = []
        for row in (r for r in selections if r["code"].startswith(prefix)):
            star = " · İlk bakılacak aday" if row["recommended"] else ""
            cards.append(f'''<article data-code="{row['code']}"><h3>{row['code']} · {html.escape(row['title'])}</h3>
<p class="note">{html.escape(row['note'])}{star}</p>
<p class="meta">Kevin MacLeod · {row['length'][3:]} · {html.escape(row['instruments'] or '')}</p>
<audio controls preload="none" src="{row['file']}"></audio>
<div class="actions"><button data-seek="0">Başlangıç</button><button data-seek="30">0:30</button><button data-seek="90">1:30</button><button data-pick="{row['code']}">Seç / kaldır</button>
<a href="{row['source']}" target="_blank" rel="noopener">Resmî kaynak</a></div></article>''')
        sections.append(f'<section id="{prefix}"><h2>{html.escape(group)}</h2><div class="grid">'+"\n".join(cards)+"</div></section>")
    page = '''<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Grisilith · Seçki 28 · Run ve Market müzikleri</title><style>
:root{color-scheme:dark}*{box-sizing:border-box}body{margin:0;background:#141516;color:#eae6dc;font:17px/1.5 Arial,sans-serif}
header,main{max-width:1300px;margin:auto;padding:24px}header{padding-bottom:12px}h1{font-size:28px;color:#e6c58b}h2{padding-top:20px;color:#e6c58b}h3{font-size:19px;margin:0 0 8px}
nav{position:sticky;top:0;z-index:2;background:#202225;padding:12px;display:flex;gap:16px;flex-wrap:wrap;border-bottom:1px solid #736044}
a{color:#b8d7ef}nav a{text-decoration:none}section{scroll-margin-top:80px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(380px,1fr));gap:16px}
article{background:#222426;border:1px solid #514939;border-radius:9px;padding:18px;overflow:hidden}article.picked{border:2px solid #e6c58b;padding:17px}.note{margin:5px 0}.meta{color:#bab9b4;font-size:14px}
audio{width:100%;margin:10px 0}.actions{display:flex;gap:8px;align-items:center;flex-wrap:wrap}button{background:#393a3b;border:1px solid #817150;border-radius:5px;color:#fff;padding:8px 12px;cursor:pointer}
#picked{color:#f4d498;font-weight:bold}.small{font-size:15px;color:#c8c8c3}@media(max-width:500px){.grid{grid-template-columns:1fr}header,main{padding:15px}}
</style><header><h1>Seçki 28 · Harita ve Market müzikleri</h1>
<p>4 kategori × 10 ayrı tam parça. Henüz oyuna eklenmedi. Mini/final boss müziği bu seçkide yok.</p>
<p class="small">Ücretsiz CC BY 4.0 · Kullanımda kaynak belirtilecek. Sesler değiştirilmedi; parça içindeki güçlü bölümleri de dinleyin. Stil notları resmî katalog bilgilerine dayalı aday önerileridir.</p>
<p id="picked">Seçim: —</p><button id="copy">Seçim kodlarını kopyala</button> <label>Dinleme düzeyi <input id="volume" type="range" min="0" max="1" step=".01" value=".55"></label></header>
<nav><a href="#CF">Forest · CF1–CF10</a><a href="#VS">Siege · VS1–VS10</a><a href="#HD">Depths · HD1–HD10</a><a href="#MK">Market · MK1–MK10</a><a href="LICENSE-SOURCES.txt">Lisanslar</a></nav><main>'''+"\n".join(sections)+'''</main><script>
const players=[...document.querySelectorAll('audio')],vol=document.querySelector('#volume'),picked=new Set();
players.forEach(a=>{a.volume=+.55;a.addEventListener('play',()=>players.forEach(b=>{if(a!==b)b.pause()}));a.addEventListener('error',()=>{a.after(Object.assign(document.createElement('p'),{textContent:'Dosya açılamadı. Resmî kaynak bağlantısından da dinleyebilirsin.'}))},{once:true})});
vol.oninput=()=>players.forEach(a=>a.volume=+vol.value);
document.querySelectorAll('[data-seek]').forEach(b=>b.onclick=()=>{const a=b.closest('article').querySelector('audio');const seek=()=>{a.currentTime=Math.min(+b.dataset.seek,Math.max(0,a.duration-1));a.play().catch(()=>{})};if(a.readyState)a.currentTime=0,seek();else a.addEventListener('loadedmetadata',seek,{once:true}),a.load()});
document.querySelectorAll('[data-pick]').forEach(b=>b.onclick=()=>{const c=b.dataset.pick;picked.has(c)?picked.delete(c):picked.add(c);b.closest('article').classList.toggle('picked',picked.has(c));document.querySelector('#picked').textContent='Seçim: '+([...picked].join(', ')||'—')});
document.querySelector('#copy').onclick=()=>navigator.clipboard.writeText([...picked].join(', ')).catch(()=>{alert([...picked].join(', '))});
</script></html>'''
    page=page.replace("Seçki 28 · Harita ve Market müzikleri",html.escape(PACK_TITLE))
    page=page.replace("Grisilith · Seçki 28 · Run ve Market müzikleri",html.escape("Grisilith · "+PACK_TITLE))
    page=page.replace("4 kategori × 10 ayrı tam parça. Henüz oyuna eklenmedi. Mini/final boss müziği bu seçkide yok.",html.escape(PACK_DESCRIPTION))
    nav=''.join(f'<a href="#{prefix}">{html.escape(group)} · {prefix}1–{prefix}{len(rows)}</a>' for prefix,group,rows in GROUPS)
    start=page.index('<nav>');end=page.index('</nav>',start)
    page=page[:start]+'<nav>'+nav+'<a href="LICENSE-SOURCES.txt">Lisanslar</a>'+page[end:]
    (OUT / "DINLE.html").write_text(page, encoding="utf-8")
    print(f"{PACK_LABEL}_READY: {len(selections)} verified downloads, {sum(r['bytes'] for r in selections)/1e6:.1f} MB; {OUT / 'DINLE.html'}", flush=True)

if __name__ == "__main__":
    main()
