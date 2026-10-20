"""Post-cut check of complete spoken lines, without number prompts."""
from pathlib import Path
import json,concurrent.futures,re
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly
from faster_whisper import WhisperModel

ROOT=Path(__file__).resolve().parent;OUT=ROOT/'listening-pack-27';WORK=ROOT/'sources/user-voices-analysis'
path=next((WORK/'models').glob('models--Systran--faster-whisper-small.en/snapshots/*/model.bin')).parent
model=WhisperModel(str(path),device='cpu',compute_type='int8',cpu_threads=4,num_workers=2)
data=json.loads((OUT/'CUTS.json').read_text(encoding='utf-8'))
rows=[r for r in data['clips'] if r['kind'] in ('S','P')]
def check(row):
    x,rate=sf.read(OUT/row['file'],dtype='float32')
    audio=np.concatenate([np.zeros(2400,dtype='float32'),resample_poly(x,160,441),np.zeros(2400,dtype='float32')])
    segments,_=model.transcribe(audio,language='en',beam_size=5,word_timestamps=True,
                                temperature=0.,condition_on_previous_text=False,vad_filter=False)
    parts=[dict(text=s.text,words=[dict(word=w.word,start=w.start,end=w.end) for w in s.words or []]) for s in segments]
    text=' '.join(s['text'] for s in parts)
    print(row['id'],text,flush=True)
    return dict(id=row['id'],text=text,expected_reference=row['content_text'],segments=parts)
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:results=list(pool.map(check,rows))
(WORK/'post-cut-spoken-lines.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
print('POST_CUT_SPOKEN_LINES_CHECKED',len(results),flush=True)
