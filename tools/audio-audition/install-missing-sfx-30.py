"""Install selected existing recordings without changing their audio samples."""
import json, shutil, hashlib
from pathlib import Path
import soundfile as sf

ROOT=Path(__file__).resolve().parent
GAME=ROOT.parents[1]
rows={r['code']:r for r in json.loads((ROOT/'listening-pack-30/SOURCES.json').read_text(encoding='utf-8'))}
selected={'WM1':'weapon_merge','HE10':'heal','PO1':'portal_open','LT5':'loot_pickup',
          'CH8':'charger_charge','CE10':'charger_explosion','HG1':'phantom_steed'}
installed=[]
for code,name in selected.items():
    row=rows[code].copy();src=ROOT/'listening-pack-30'/row['file']
    target=GAME/'assets/audio/sfx'/(name+src.suffix)
    shutil.copyfile(src,target)
    row.update(installed=str(target.relative_to(GAME)),duration_ms=round(sf.info(target).duration*1000))
    assert hashlib.sha256(target.read_bytes()).hexdigest()==row['sha256']
    installed.append(row)
# A short original metal UI click, not a Counter-Strike recording.
src=ROOT/'sources/rpg-audio/Audio/metalClick.ogg'
target=GAME/'assets/audio/sfx/ui_hover.ogg';shutil.copyfile(src,target)
installed.append(dict(code='HOVER',installed=str(target.relative_to(GAME)),author='Kenney',
    source='https://kenney.nl/assets/rpg-audio',license='https://creativecommons.org/publicdomain/zero/1.0/',
    original=str(src.relative_to(ROOT)),sha256=hashlib.sha256(target.read_bytes()).hexdigest(),
    duration_ms=round(sf.info(target).duration*1000)))
(GAME/'assets/audio/MISSING-SFX-SELECTIONS.json').write_text(json.dumps(installed,indent=2,ensure_ascii=False),encoding='utf-8')
credits=['Selected Phase 9h recordings — unchanged source audio; no synthesis or layering.','']
for r in installed:
    credits += [r['code']+' — '+r['installed'],r['author'],r['source'],r['license'],r['sha256'],'']
(GAME/'assets/audio/MISSING-SFX-LICENSES.txt').write_text('\n'.join(credits),encoding='utf-8')
print([(r['code'],r['installed'],r['duration_ms']) for r in installed])
