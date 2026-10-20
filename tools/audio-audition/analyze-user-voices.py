"""Local-only transcription/energy inspection of user recordings; no asset writes."""
from pathlib import Path
import hashlib
import json
import os
import sys
import time
os.environ['HF_HUB_DISABLE_XET']='1'
os.environ['HF_HUB_DISABLE_SYMLINKS_WARNING']='1'
os.environ['HF_HUB_DOWNLOAD_TIMEOUT']='120'
import numpy as np
import soundfile as sf
from scipy.signal import resample_poly
from faster_whisper import WhisperModel

ROOT=Path(__file__).resolve().parent
INPUT=Path('C:/Users/aaata/OneDrive/Masaüstü/Görseller/Voices')
WORK=ROOT/'sources/user-voices-analysis'
MODEL=WORK/'models'

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    WORK.mkdir(parents=True,exist_ok=True)
    name=sys.argv[1] if len(sys.argv)>1 else 'small.en'
    print('LOADING_LOCAL_ASR '+name,flush=True)
    cached=list(MODEL.glob('models--Systran--faster-whisper-'+name+'/snapshots/*/model.bin'))
    model=WhisperModel(str(cached[0].parent) if cached else name,device='cpu',compute_type='int8',cpu_threads=4,num_workers=1,download_root=str(MODEL))
    files=list(INPUT.glob('*.mp3'))
    if len(sys.argv)>2:files=[p for p in files if sys.argv[2].lower() in p.name.lower()]
    for file in files:
        target=WORK/(file.stem+'.'+name+'.json')
        if target.exists():
            saved=json.loads(target.read_text(encoding='utf-8'))
            if saved.get('sha256')==digest(file):continue
        started=time.monotonic()
        pcm,rate=sf.read(file,dtype='float32',always_2d=True)
        mono=np.mean(pcm,axis=1)
        asr_audio=resample_poly(mono,160,441) if rate==44100 else resample_poly(mono,16000,rate)
        segments,info=model.transcribe(asr_audio,language='en',beam_size=5,word_timestamps=True,
                vad_filter=False,condition_on_previous_text=False,temperature=0.,
                initial_prompt='Character voice recording: Hurt, Death, Punchline. Numbered audio samples.')
        result=[]
        for seg in segments:
            record=dict(start=seg.start,end=seg.end,text=seg.text,avg_logprob=seg.avg_logprob,
                        no_speech_prob=seg.no_speech_prob,
                        words=[dict(start=w.start,end=w.end,word=w.word,probability=w.probability) for w in seg.words or []])
            result.append(record)
            print(f'{file.name}: {seg.start:.2f}-{seg.end:.2f} {seg.text}',flush=True)
        hop=max(1,round(rate*.005));frame=max(1,round(rate*.01))
        rms=np.sqrt(np.convolve(mono.astype(float)**2,np.ones(frame)/frame,mode='same')[::hop])
        np.savez_compressed(WORK/(file.stem+'.energy.npz'),rms=rms,hop=hop,rate=rate)
        data=dict(file=str(file),sha256=digest(file),model=name,duration=len(pcm)/rate,rate=rate,
                  channels=pcm.shape[1],segments=result,elapsed=time.monotonic()-started)
        target.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
        print('ANALYZED '+file.name+' '+str(round(data['elapsed'],1))+'s',flush=True)

if __name__=='__main__':main()
