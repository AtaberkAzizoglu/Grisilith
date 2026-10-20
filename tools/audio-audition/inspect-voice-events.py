"""Waveform activity overview for reviewing user voice cuts (local only)."""
from pathlib import Path
import numpy as np
import soundfile as sf
from scipy.ndimage import binary_closing,label,find_objects

for file in Path('C:/Users/aaata/OneDrive/Masaüstü/Görseller/Voices').glob('*.mp3'):
    x,rate=sf.read(file)
    hop=round(rate*.005);frames=len(x)//hop
    rms=np.sqrt(np.mean(x[:frames*hop].reshape(frames,hop)**2,axis=1))
    mask=rms>max(.002,np.max(rms)*.012)
    mask=binary_closing(mask,structure=np.ones(9))
    labels,_=label(mask)
    events=[(round(s[0].start*hop/rate,3),round(s[0].stop*hop/rate,3))
            for s in find_objects(labels) if (s[0].stop-s[0].start)*hop/rate>.055]
    print(file.name,'events',len(events),events,flush=True)
