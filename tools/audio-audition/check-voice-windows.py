"""Independent ASR checks on ambiguous spoken-number windows; analysis only."""
from pathlib import Path
import json
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly
from faster_whisper import WhisperModel

ROOT=Path(__file__).resolve().parent
WORK=ROOT/'sources/user-voices-analysis'
modelpath=next((WORK/'models').glob('models--Systran--faster-whisper-small.en/snapshots/*/model.bin')).parent
model=WhisperModel(str(modelpath),device='cpu',compute_type='int8',cpu_threads=4)
cases={
 'imbert death.mp3':[(10.5,11.85),(12.02,13.07),(13.13,13.98),(14.,15.15),(15.35,16.4),(16.46,17.7),(23.,23.95),(24.,24.75)],
 'imbert hasar.mp3':[(16.06,16.99),(17.1,17.6),(17.8,18.4),(18.65,19.55)],
 'Witch Voices.mp3':[(25.3,26.3),(26.32,27.1),(27.1,28.08),(28.12,29.13),(29.12,29.96),(29.94,31.06),(31.08,31.88),(31.9,33.1),(33.12,33.78),(33.76,34.7),(34.75,35.49),(35.51,36.54),(36.62,37.27),(37.28,38.45),(38.72,39.59),(39.7,40.3),(43.2,43.64),(43.64,45.25)],
 'Ywaleth Voices.mp3':[(6.2,6.85),(7.08,7.91),(8.13,8.76),(9.07,9.96)],
 'Koba Voices.mp3':[(13.49,14.62),(14.64,15.79),(16.,17.05),(54.16,55.18),(55.25,58.78),(58.85,60.01),(61.86,62.63)],
}
results=[]
for name,ranges in cases.items():
    x,rate=sf.read(Path('C:/Users/aaata/OneDrive/Masaüstü/Görseller/Voices')/name,dtype='float32')
    for start,end in ranges:
        clip=x[round(start*rate):round(end*rate)]
        audio=np.concatenate([np.zeros(2400,dtype='float32'),resample_poly(clip,160,441),np.zeros(4000,dtype='float32')])
        segments,_=model.transcribe(audio,language='en',beam_size=5,condition_on_previous_text=False,word_timestamps=True,temperature=0.,vad_filter=False)
        parts=[dict(text=s.text,logprob=s.avg_logprob,words=[dict(word=w.word,start=w.start,end=w.end,prob=w.probability) for w in s.words or []]) for s in segments]
        record=dict(file=name,start=start,end=end,segments=parts)
        results.append(record)
        print(name,start,end,' | '.join(s['text'] for s in parts),flush=True)
(WORK/'ambiguous-window-checks.json').write_text(json.dumps(results,indent=2,ensure_ascii=False),encoding='utf-8')
