"""Print timestamps and silence groups for cut-boundary review."""
from pathlib import Path
import json,re,sys
import numpy as np
import soundfile as sf
from scipy.ndimage import binary_closing,label,find_objects

root=Path(__file__).resolve().parent/'sources/user-voices-analysis'
for file in root.glob('*.small.en.json'):
    if len(sys.argv)>1 and sys.argv[1].lower() not in file.name.lower():continue
    data=json.loads(file.read_text(encoding='utf-8'))
    print('\n===',file.name,'===')
    words=[w for s in data['segments'] for w in s['words']]
    for w in words:
        if re.search(r'\d|one|two|three|four|five|six|seven|eight|nine|ten|hurt|death|punch|line',w['word'],re.I):
            print(f"{w['start']:.3f}..{w['end']:.3f} {w['word']}")
    x,rate=sf.read(data['file']);hop=round(rate*.005);frames=len(x)//hop
    rms=np.sqrt(np.mean(x[:frames*hop].reshape(frames,hop)**2,axis=1))
    mask=binary_closing(rms>max(.002,np.max(rms)*.012),structure=np.ones(29))
    labs,_=label(mask)
    events=[(round(s[0].start*hop/rate,3),round(s[0].stop*hop/rate,3)) for s in find_objects(labs) if (s[0].stop-s[0].start)*hop/rate>.055]
    print('ENERGY',events)
