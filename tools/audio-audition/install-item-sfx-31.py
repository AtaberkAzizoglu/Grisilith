"""Install approved original recordings as PCM; only requested repetitions are concatenated."""
import json,hashlib,math
from pathlib import Path
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly

ROOT=Path(__file__).resolve().parent;GAME=ROOT.parents[1]
rows={r['code']:r for r in json.loads((ROOT/'listening-pack-31/SOURCES.json').read_text(encoding='utf-8'))}
cash={r['code']:r for r in json.loads((ROOT/'listening-pack-32/SOURCES.json').read_text(encoding='utf-8'))}
choices=[('AB1','ITEM_BROOM',1),('BT3','ITEM_ROULETTE',1),('HA5','ITEM_HEALING_AURA',1),
 ('PA1','ITEM_PROTECTERS',1),('TR3','ITEM_TREASURE',1),('HG3','ITEM_HOURGLASS',1),
 ('TD1','ITEM_TOADS',1),('LG4','ITEM_LOSE_GOLD',1),('HB3','ITEM_HOSTILE_BOSS',1),
 ('SG3','ITEM_SHRINK',3),('TC5','ITEM_CURSE',2),('CC5','ITEM_CLOSE_CALL',1),
 ('IM3','ITEM_MERCHANT',1),('SH3','ITEM_SECOND_HEART',1),('BV3','ITEM_BLOOD_VIAL',1),
 ('PT1','ITEM_TRAP_BUILD',1),('PT2','ITEM_TRAP_KILL',1),('PT5','ITEM_TRAP_END',1),
 ('CO8','ITEM_CASH_OUT',1)]
installed=[]
for code,cue,repeats in choices:
 row=(cash if cue=='ITEM_CASH_OUT' else rows)[code].copy()
 src=ROOT/('listening-pack-32' if cue=='ITEM_CASH_OUT' else 'listening-pack-31')/row['file']
 data,rate=sf.read(src,always_2d=True)
 if rate!=44100:
  divisor=math.gcd(rate,44100);data=resample_poly(data,44100//divisor,rate//divisor)
 data=np.tile(data,(repeats,1))
 target=GAME/'assets/audio/sfx'/(cue.lower()+'.wav');sf.write(target,data,44100,subtype='PCM_16')
 info=sf.info(target)
 row.update(cue=cue,installed=target.relative_to(GAME).as_posix(),repeats=repeats,
  duration_ms=math.ceil(info.duration*1000),processing='PCM16 / 44100Hz format conversion; no pitch, mixing or new sound design; repetitions='+str(repeats),
  installed_sha256=hashlib.sha256(target.read_bytes()).hexdigest())
 installed.append(row)
(GAME/'assets/audio/ITEM-SFX-SELECTIONS.json').write_text(json.dumps(installed,indent=2,ensure_ascii=False),encoding='utf-8')
(GAME/'assets/audio/ITEM-SFX-LICENSES.txt').write_text('\n\n'.join(f'{r["cue"]} — selection {r["code"]}\n{r["author"]}\n{r["source"]}\n{r["license"]}\n{r["processing"]}\nSource SHA256: {r["sha256"]}\nInstalled SHA256: {r["installed_sha256"]}' for r in installed),encoding='utf-8')
for r in installed:print(r['cue'],r['duration_ms'])
print('ITEM_INSTALL_READY: 19 approved cues; BV3 assumption recorded; portal and heal unchanged.')
