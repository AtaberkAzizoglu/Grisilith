"""Install the two approved Summary tracks and retain per-track CC BY provenance."""
import json,hashlib,shutil,urllib.request
from pathlib import Path

root=Path(__file__).resolve().parent;audio=root.parent.parent/'assets/audio'
rows={r['code']:r for r in json.loads((root/'listening-pack-29/MUSIC-SOURCES.json').read_text(encoding='utf-8'))}
selections=[]
for code,target in {'DF3':'summary_defeat.mp3','VC17':'summary_victory.mp3'}.items():
    r=rows[code];source=root/'listening-pack-29'/r['file']
    assert hashlib.sha256(source.read_bytes()).hexdigest()==r['sha256']
    shutil.copyfile(source,audio/'music'/target);selections.append(dict(r,installed=target))
(audio/'SUMMARY-MUSIC-SELECTIONS.json').write_text(json.dumps(selections,ensure_ascii=False,indent=2),encoding='utf-8')
credit=['Summary music — Kevin MacLeod (incompetech.com).',
    'Creative Commons Attribution 4.0 — https://creativecommons.org/licenses/by/4.0/',
    'Recordings unchanged; each plays once per Summary, not an endless fanfare loop.','']
for r in selections:credit += [r['title']+' — '+r['code'],r['source'],r['license'],'SHA256: '+r['sha256'],'']
(audio/'SUMMARY-MUSIC-LICENSES.txt').write_text('\n'.join(credit),encoding='utf-8')

# Audit every shipped MusicTrack against the current official attribution catalog.
base='https://incompetech.com/music/royalty-free/'
with urllib.request.urlopen(base+'pieces.json',timeout=60) as reply:catalog=json.load(reply)
by_isrc={r['isrc']:r for r in catalog}
with urllib.request.urlopen(base+'music.html',timeout=60) as reply:page=reply.read().decode('utf-8')
assert 'By Attribution 4.0' in page
all_rows=json.loads((audio/'RUN-MUSIC-SELECTIONS.json').read_text(encoding='utf-8'))+selections
all_rows.append(dict(title='Gregorian Chant',isrc='USUAN1600052',installed='gregorian_chant.mp3'))
audit=['Music attribution audit — 2026-10-20',base+'music.html',
    'Official catalog attribution: Creative Commons By Attribution 4.0.',
    'Thirteen installed tracks. Title/ISRC checked against the official catalog.','']
for r in all_rows:
    assert by_isrc[r['isrc']]['title']==r['title']
    f=audio/'music'/r['installed'];assert f.exists()
    audit += [r['title']+' — Kevin MacLeod (incompetech.com)',base+'index.html?isrc='+r['isrc'],
        'https://creativecommons.org/licenses/by/4.0/',
        'Recording unchanged; volume, ducking, pause and looping are playback controls only.',
        'SHA256: '+hashlib.sha256(f.read_bytes()).hexdigest(),'']
(audio/'MUSIC-ATTRIBUTION-AUDIT.txt').write_text('\n'.join(audit),encoding='utf-8')
print('SUMMARY_INSTALLED_AND_MUSIC_AUDITED: DF3/VC17 exact, 13 official title/ISRC/license entries.')
