"""70 found, licensed SFX choices. Original bytes, except three clearly labeled PCM excerpts."""
import json,hashlib,shutil,html
from pathlib import Path
import soundfile as sf

ROOT=Path(__file__).resolve().parent;SRC=ROOT/'sources';NEW=SRC/'missing-sfx-30';OUT=ROOT/'listening-pack-30'
CC0='https://creativecommons.org/publicdomain/zero/1.0/'
sources={r['id']:r for r in json.loads((NEW/'SOURCES.json').read_text(encoding='utf-8'))}
sources.update({
    'interface':dict(author='Kenney',source='https://kenney.nl/assets/interface-sounds',license=CC0),
    'rpg':dict(author='Kenney',source='https://kenney.nl/assets/rpg-audio',license=CC0),
    'rubberduck':dict(author='rubberduck',source='https://opengameart.org/content/80-cc0-rpg-sfx',license=CC0),
})
sources['ghost_centaur']['license']='https://creativecommons.org/licenses/by/4.0/'
sources['ghost_centaur']['author']='qubodup; source recordings: dobroide and 3bagbrew'
sources['ghost_centaur']['additional_licenses']=['https://creativecommons.org/licenses/by/3.0/']
sources['ghost_centaur']['source_recordings']=['https://freesound.org/people/dobroide/sounds/116267/','https://freesound.org/people/3bagbrew/sounds/59569/']

def locate(key,name):
    folder={'interface':SRC/'interface-sounds','rpg':SRC/'rpg-audio','rubberduck':SRC/'rubberduck-rpg'}.get(key,NEW/key)
    files=list(folder.rglob(name));assert len(files)==1,(key,name,files)
    return files[0]
def choice(key,name,note):return dict(key=key,name=name,note=note)
c=choice
groups=[
 ('WM','Weapon Merge','Mevcut metalik, başarı ve büyülü birleşme kayıtları; hiçbiri katmanlanmadı.',[
    c('rpg','metalLatch.ogg','Metal kilidin oturması'),c('rubberduck','metal_01.ogg','Kuru metal vurgusu'),
    c('rubberduck','metal_02.ogg','İkinci metalik vurgu'),c('rubberduck','metal_03.ogg','Daha farklı metal tınısı'),
    c('rubberduck','item_gem_01.ogg','Kristalimsi eşya tınısı'),c('rubberduck','item_gem_02.ogg','Parlak eşya vurgusu'),
    c('magic','magical_1_0.ogg','Büyülü birleşme alternatifi'),c('magic','magical_4.ogg','Daha dolgun büyülü vurgu'),
    c('interface','confirmation_001.ogg','Kısa onay tınısı'),c('scifi','impactMetal_004.ogg','Daha ağır metal birleşimi'),
 ]),
 ('BH','Button Hover','Tıklama değil, imleç ilk girdiğinde düşük düzeyde kullanılabilecek ham UI kayıtları.',[
    *[c('interface',f'click_{i:03}.ogg',f'Kısa UI tıkı {i}') for i in range(1,6)],
    c('interface','pluck_001.ogg','Kısa telimsi seçim'),c('interface','pluck_002.ogg','İkinci telimsi seçim'),
    c('interface','drop_001.ogg','Yumuşak küçük vurgu'),c('interface','drop_002.ogg','İkinci küçük vurgu'),
    c('interface','scroll_001.ogg','Hafif gezinme vurgusu'),
 ]),
 ('HE','Heal','Hazır iyileştirme ve hafif büyülü tınılar. Seçimden sonra sık heal tickleri gruplanabilir.',[
    *[c('cure',f'Cure{i}.wav',f'Cure Magic — orijinal {i}') for i in range(1,9)],
    c('magic','magical_2.ogg','Hafif büyülü iyileştirme alternatifi'),c('magic','magical_6_0.ogg','Kısa büyülü tını alternatifi'),
 ]),
 ('LT','Sandık loot toplama','Hazır para/eşya toplama kayıtları; açılma sesinden ayrı.',[
    *[c('rubberduck',f'item_coins_{i:02}.ogg',f'Para toplama {i}') for i in range(1,5)],
    c('rpg','handleCoins.ogg','Paraları ele alma'),
 ]),
 ('PO','Portal açılması','Mevcut teleport/faz geçişi kayıtları; sihirsel ve daha elektronik alternatifler.',[
    c('electricity','teleport_1.wav','Teleport açılması'),
    *[c('digital',f'phaseJump{i}.ogg',f'Faz geçişi {i}') for i in range(1,4)],
    c('magic','magical_7_0.ogg','Büyülü portal alternatifi'),
 ]),
 ('CH','Charger — yürürken charge','Hazır enerji birikimi ve sürekli enerji kayıtları. Loop/başlama ayrımı notlarda; henüz oyunda değil.',[
    c('electricity','charge.wav','Sürekli charge'),c('electricity','chargestart.wav','Charge başlangıcı'),
    c('electricity','recharge.wav','Yeniden enerji dolması'),c('electricity','powerup.wav','Güç birikimi'),
    c('electricity','turbo.wav','Hareketli enerji'),c('electricity','shieldloop.wav','Sürekli koruma/enerji dokusu'),
    c('electricity','crackleelectricityloop.wav','Çatırdayan enerji döngüsü'),
    c('scifi','forceField_000.ogg','Güç alanı uğultusu'),c('scifi','spaceEngineLow_000.ogg','Düşük frekanslı sürekli enerji'),
    c('scifi','spaceEngineLarge_002.ogg','Daha dolgun sürekli enerji'),
 ]),
 ('CE','Charger — patlama','Hazır patlamalar. Önce düşük dinleme düzeyiyle dene.',[
    *[c('scifi',f'explosionCrunch_{i:03}.ogg',f'Çatırdayan patlama {i+1}') for i in range(5)],
    c('scifi','lowFrequency_explosion_000.ogg','Bas ağırlıklı patlama 1'),
    c('scifi','lowFrequency_explosion_001.ogg','Bas ağırlıklı patlama 2'),
    c('electricity','deathboom.wav','Elektrikli güçlü patlama'),c('electricity','groundhit.wav','Yere vuran patlama'),
    c('electricity','shieldhit.wav','Enerji darbesi alternatifi'),
 ]),
 ('HN','Phantom Steed — normal at','Mevcut gerçek at kayıtları. Uzun saha kayıtlarında nefes ve çevre de bulunabilir.',[
    c('horse_kubuzz','preview.mp3','Kubuzz — doğal kişneme'),c('horse_good','preview.mp3','GoodListener — kısa kişneme'),
    c('horse_soft','preview.mp3','GoodListener — yumuşak kişneme'),c('horse_inspector','preview.mp3','InspectorJ — yakın kişneme'),
    c('horse_breath','preview.mp3','CraterZounds — nefes ve kısa kişneme; uzun kaynak'),
 ]),
 ('HG','Phantom Steed — hayalet/fantastik at','Önceden sanatçılarca işlenmiş hazır at vokalleri. HG3–5 aynı Centaur kaydının ayrı, sessizlikle ayrılan üç vokalidir; yeni efekt tasarlanmadı.',[
    c('ghost_demon','preview.mp3','Hazır demonic horse whinny'),c('ghost_moan','preview.mp3','Hazır işlenmiş at iniltisi'),
    dict(c('ghost_centaur','preview.mp3','Centaur kaydından ayrı vokal A'),cut=(5.30,7.65)),
    dict(c('ghost_centaur','preview.mp3','Centaur kaydından ayrı vokal B'),cut=(8.00,10.55)),
    dict(c('ghost_centaur','preview.mp3','Centaur kaydından ayrı vokal C'),cut=(10.75,12.1848)),
 ]),
]

def main():
    OUT.mkdir(exist_ok=True);manifest=[];sections=[]
    for prefix,title,note,choices in groups:
        cards=[]
        for i,r in enumerate(choices,1):
            code=f'{prefix}{i}';original=locate(r['key'],r['name']);ext='.wav' if 'cut' in r else original.suffix
            target=OUT/(code+ext)
            if 'cut' in r:
                with sf.SoundFile(original) as f:
                    start=round(r['cut'][0]*f.samplerate);end=min(f.frames,round(r['cut'][1]*f.samplerate))
                    f.seek(start);data=f.read(end-start,dtype='int16',always_2d=True);sf.write(target,data,f.samplerate,subtype='PCM_16')
            else:shutil.copyfile(original,target)
            info=sf.info(target);assert info.duration>0
            record=dict(code=code,group=title,note=r['note'],file=target.name,duration=info.duration,
                original=str(original.relative_to(ROOT)),source_sha256=hashlib.sha256(original.read_bytes()).hexdigest(),
                sha256=hashlib.sha256(target.read_bytes()).hexdigest(),cut=r.get('cut'),**sources[r['key']])
            manifest.append(record)
            license_label='CC0' if '/zero/' in record['license'] else 'CC BY (atıf gerekli)'
            cards.append(f'<article><h3>{code} · {html.escape(r["note"])}</h3><audio controls preload="none" src="{target.name}"></audio><p>{info.duration:.2f} s · {html.escape(record["author"])} · {license_label}</p><p>{"Yalnız kaynak kesiti; başka işlem yok." if "cut" in r else "Kaynak dosya birebir; efekt işlemi yok."}</p><a href="{record["source"]}" target="_blank" rel="noopener">Kaynak / lisans</a> <button data-code="{code}">Seç / kaldır</button></article>')
        sections.append(f'<section id="{prefix}"><h2>{html.escape(title)} · {prefix}1–{prefix}{len(choices)}</h2><p>{html.escape(note)}</p><div class="grid">'+''.join(cards)+'</div></section>')
    assert len(manifest)==70
    (OUT/'SOURCES.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    credits=['Seçki 30 — hazır kayıtlar; oyuna eklenmedi.',
        'Yeni ses üretilmedi, katmanlama/perde/hız/filtre uygulanmadı. HG3–5 yalnız ayrı kaynak vokallerinin kesitidir.',
        'Freesound kayıtları herkese açık HQ önizlemedir. Seçimden sonra gerektiğinde kaynak WAV/FLAC alınır.',
        'Centaur: qubodup; kaynak kayıtları dobroide ve 3bagbrew. Üçüne de atıf gerekir.',
        'Centaur source: https://freesound.org/people/qubodup/sounds/222130/ — CC BY 4.0; kaynaklar CC BY 3.0.',
        '']
    for r in manifest:credits += [r['code']+' — '+r['note'],r['author'],r['source'],r['license'],str(r.get('additional_licenses','')),str(r.get('source_recordings','')),'Original: '+r['original'],'Cut: '+str(r['cut']),'SHA256: '+r['sha256'],'']
    (OUT/'LICENSE-SOURCES.txt').write_text('\n'.join(credits),encoding='utf-8')
    nav=''.join(f'<a href="#{a}">{html.escape(b)}</a>' for a,b,_,_ in groups)
    page='''<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Seçki 30 · Atlanan Sesler</title><style>
:root{color-scheme:dark}*{box-sizing:border-box}body{font:17px/1.5 Arial,sans-serif;margin:0;background:#17191b;color:#e8e5dc}header,main{max-width:1300px;margin:auto;padding:24px}h1,h2{color:#e3c187}h3{font-size:18px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(330px,1fr));gap:14px}article{background:#25282b;border:1px solid #5e513d;border-radius:8px;padding:16px}article p{font-size:14px;color:#c9c8c2}audio{width:100%}a{color:#b2d5ee}button{background:#494235;border:1px solid #9b8055;color:white;padding:7px 12px;border-radius:5px;cursor:pointer}nav{position:sticky;top:0;background:#242729;padding:10px;display:flex;gap:13px;flex-wrap:wrap;z-index:2}nav a{font-size:14px}section{scroll-margin-top:110px}article.selected{border:2px solid #e3c187}#selection{font-weight:bold;color:#e3c187}
</style><header><h1>Seçki 30 · Atlanan Sesler</h1><p>70 hazır kayıt seçeneği. Hiçbiri henüz oyuna bağlanmadı. Yeni ses tasarımı/sentez yok.</p><p>Hayalet at grubundaki HG3–HG5 aynı hazır kaydın üç ayrı vokal kesitidir; beş bağımsız kaynak olarak sunulmuyor.</p><label>Dinleme düzeyi <input id="volume" type="range" min="0" max="1" step=".01" value=".45"></label><p id="selection">Seçim: —</p><button id="copy">Kodları kopyala</button> <a href="LICENSE-SOURCES.txt">Lisans / kaynak dökümü</a></header><nav>'''+nav+'</nav><main>'+''.join(sections)+'''</main><script>
const players=[...document.querySelectorAll('audio')],chosen=new Set();players.forEach(a=>{a.volume=.45;a.onplay=()=>players.forEach(b=>{if(a!==b)b.pause()})});document.querySelector('#volume').oninput=e=>players.forEach(a=>a.volume=+e.target.value);document.querySelectorAll('[data-code]').forEach(b=>b.onclick=()=>{const c=b.dataset.code;chosen.has(c)?chosen.delete(c):chosen.add(c);b.closest('article').classList.toggle('selected',chosen.has(c));document.querySelector('#selection').textContent='Seçim: '+([...chosen].join(', ')||'—')});document.querySelector('#copy').onclick=()=>navigator.clipboard.writeText([...chosen].join(', ')).catch(()=>alert([...chosen].join(', ')));
</script></html>'''
    (OUT/'DINLE.html').write_text(page,encoding='utf-8')
    print('SFX30_READY: 70 raw/licensed choices; HG3–5 explicitly documented source excerpts; no game SFX installed.')
if __name__=='__main__':main()
