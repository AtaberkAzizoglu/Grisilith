"""Independent narrow-window recognition of every reviewed number exclusion."""
from pathlib import Path
import importlib.util,json,re,concurrent.futures
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly
from faster_whisper import WhisperModel

ROOT=Path(__file__).resolve().parent;WORK=ROOT/'sources/user-voices-analysis'
spec=importlib.util.spec_from_file_location('voice_boundaries',ROOT/'user-voice-boundaries.py')
module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
INPUT=Path('C:/Users/aaata/OneDrive/Masaüstü/Görseller/Voices')
modelpath=next((WORK/'models').glob('models--Systran--faster-whisper-small.en/snapshots/*/model.bin')).parent
model=WhisperModel(str(modelpath),device='cpu',compute_type='int8',cpu_threads=4,num_workers=2)
word_numbers=dict(one=1,two=2,three=3,four=4,five=5,six=6,seven=7,eight=8,nine=9,ten=10)
cachefile=WORK/'all-label-checks.json'
cache=json.loads(cachefile.read_text(encoding='utf-8')) if cachefile.exists() else {}
sources={s['file']:sf.read(INPUT/s['file'],dtype='float32') for s in module.SOURCES}
tasks=[]
for source in module.SOURCES:
    for group in source['groups']:
        for i,interval in enumerate(group['labels'],1):
            if i in group.get('missing_labels',[]):continue
            tasks.append((source,group,i,interval))

def run(item):
    source,group,i,(start,end)=item;key=f"{source['character']}27{group['kind']}{i:02}"
    old=cache.get(key)
    if old and old['file']==source['file'] and old['start']==start and old['end']==end:return key,old
    x,rate=sources[source['file']]
    clip=x[max(0,round((start-.035)*rate)):round((end+.035)*rate)]
    audio=np.concatenate([np.zeros(2400,dtype='float32'),resample_poly(clip,160,441),np.zeros(3200,dtype='float32')])
    segments,_=model.transcribe(audio,language='en',beam_size=5,condition_on_previous_text=False,
                               word_timestamps=True,temperature=0.,vad_filter=False)
    result=[dict(text=s.text,logprob=s.avg_logprob,
                 words=[dict(word=w.word,start=w.start,end=w.end,probability=w.probability) for w in s.words or []]) for s in segments]
    text=' '.join(s['text'] for s in result)
    detected=[int(n) for n in re.findall(r'\d+',text)]
    detected += [value for word,value in word_numbers.items() if re.search(r'\b'+word+r'\b',text,re.I)]
    record=dict(file=source['file'],start=start,end=end,expected=i,text=text,recognized=i in detected,segments=result)
    print(key,('OK' if record['recognized'] else 'REVIEW'),text,flush=True)
    return key,record

with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
    for key,record in pool.map(run,tasks):
        cache[key]=record
        cachefile.write_text(json.dumps(cache,ensure_ascii=False,indent=2),encoding='utf-8')
print('LABEL_CHECK_TOTAL',len(tasks),'REVIEW',[(k,v['text']) for k,v in cache.items() if not v['recognized']],flush=True)
