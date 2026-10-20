"""Fetch existing licensed recordings; no synthesis, pitch changes or layering."""
import json,re,urllib.request,urllib.parse,html,zipfile,time
from pathlib import Path

ROOT=Path(__file__).resolve().parent/'sources'/'missing-sfx-30'
ROOT.mkdir(parents=True,exist_ok=True)
def fetch(url):
    for attempt in range(3):
        try:
            with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0 Grisilith audio audition'}),timeout=60) as response:return response.read()
        except Exception:
            if attempt==2:raise
            time.sleep(1)
def save(url,target):
    if not target.exists():target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(fetch(url))
def archive(url,folder):
    target=ROOT/folder/'archive.zip';save(url,target)
    dest=(ROOT/folder).resolve()
    with zipfile.ZipFile(target) as z:
        for member in z.infolist():
            path=(dest/member.filename).resolve()
            assert path.is_relative_to(dest)
        z.extractall(dest)
pages=[
    ('cure','https://opengameart.org/content/cure-magic','Someoneman'),
    ('electricity','https://opengameart.org/content/electricity-game-sound-pack','faxcorp'),
    ('magic','https://opengameart.org/content/magic-spell-sfx','JaggedStone'),
    ('scifi','https://kenney.nl/assets/sci-fi-sounds','Kenney'),
    ('digital','https://kenney.nl/assets/digital-audio','Kenney'),
]
meta=[]
for key,url,author in pages:
    folder=ROOT/key;folder.mkdir(exist_ok=True)
    saved=folder/'source-page.html';save(url,saved);page=saved.read_text(encoding='utf-8',errors='replace')
    assert 'CC0' in page or 'publicdomain/zero' in page
    links=list(dict.fromkeys(html.unescape(u) for u in re.findall(r'href=["\']([^"\']+)["\']',page)))
    links=[urllib.parse.urljoin(url,u) for u in links if re.search(r'\.(zip|wav|ogg)(?:\?|$)',u,re.I)]
    if key in ('scifi','digital'):
        links=[u for u in links if 'kenney' in u and '.zip' in u]
    for u in links:
        if '.zip' in u:archive(u,key)
        else:save(u,folder/urllib.parse.unquote(urllib.parse.urlsplit(u).path.rsplit('/',1)[-1]))
    meta.append(dict(id=key,source=url,author=author,license='https://creativecommons.org/publicdomain/zero/1.0/',files=links))
    print(key,links,flush=True)

horses=[('horse_kubuzz','Kubuzz',347036),('horse_good','GoodListener',322445),
    ('horse_soft','GoodListener',322450),('horse_inspector','InspectorJ',419231),
    ('horse_breath','CraterZounds',189738),('ghost_demon','Mastersoundboy2005',769937),
    ('ghost_moan','GoodListener',322444),('ghost_centaur','qubodup',222130)]
for key,author,sound in horses:
    url=f'https://freesound.org/people/{author}/sounds/{sound}/'
    folder=ROOT/key;folder.mkdir(exist_ok=True);saved=folder/'source-page.html';save(url,saved)
    page=saved.read_text(encoding='utf-8',errors='replace')
    licenses=re.findall(r'https?://creativecommons.org/(?:licenses|publicdomain)/[^"\s<>]+',page)
    licenses=list(dict.fromkeys(licenses));licenses=[x for x in licenses if '/by/' in x or '/zero/' in x]
    assert licenses,(key,'No permissive license on original page')
    previews=re.findall(r'https://cdn\.freesound\.org/previews/[^"\s<>]+-hq\.mp3',page)
    assert previews,(key,'No HQ preview')
    save(html.unescape(previews[0]),folder/'preview.mp3')
    meta.append(dict(id=key,source=url,author=author,license=licenses[0],files=[previews[0]],preview=True))
    print(key,licenses[0],previews[0],flush=True)
(ROOT/'SOURCES.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2),encoding='utf-8')
print('SOURCE_FETCH_READY: recorded sources and page license evidence retained.')
