"""Install only explicitly approved pack27 voices; source audition archive is untouched."""
from pathlib import Path
import hashlib,json,wave

ROOT=Path(__file__).resolve().parent
PACK=ROOT/'listening-pack-27'
ASSETS=ROOT.parent.parent/'assets/audio'
SFX=ASSETS/'sfx'

PLAYERS=[
 ('IMBERT','IM','IM27S01',['IM27H09','IM27H08','IM27H07','IM27H02','IM27H01'],'IM27D10'),
 ('YWALETH','YW','YW27S01',['YW27H01','YW27H06','YW27H08'],'YW27D10'),
 ('LINLOS','LN','LN27S01',['LN27H03','LN27H01','LN27H06','LN27H07','LN27H09'],'LN27D01'),
 ('KOBA','KB','KB27S01',['KB27H05','KB27H06','KB27H08','KB27H09','KB27H01'],'KB27D09'),
 ('LAVENA','LV','LV27S01',['LV27H04','LV27H01','LV27H08'],'LV27D10'),
]

def digest(file):return hashlib.sha256(file.read_bytes()).hexdigest()

def main():
    cuts={r['id']:r for r in json.loads((PACK/'CUTS.json').read_text(encoding='utf-8'))['clips']}
    installed=[]
    def install(enum,codes,priority,gain,combat=True,path=None):
        name=path or 'voice_'+codes[0].lower()+'.wav'
        target=SFX/name
        inputs=[PACK/(code+'.wav') for code in codes]
        for code,file in zip(codes,inputs):assert digest(file)==cuts[code]['sha256']
        if len(inputs)==1:target.write_bytes(inputs[0].read_bytes())
        else:
            # One atomic death sample: second take immediately follows the first.
            # No gap, retiming, re-normalization or extra fade across the join.
            chunks=[]
            for file in inputs:
                with wave.open(str(file)) as stream:
                    assert stream.getnchannels()==1 and stream.getsampwidth()==2 and stream.getframerate()==44100
                    chunks.append(stream.readframes(stream.getnframes()))
            with wave.open(str(target),'wb') as stream:
                stream.setnchannels(1);stream.setsampwidth(2);stream.setframerate(44100);stream.writeframes(b''.join(chunks))
        with wave.open(str(target)) as stream:frames=stream.getnframes();rate=stream.getframerate()
        record=dict(enum=enum,path=name,source_codes=codes,frames=frames,rate=rate,
                    duration_ms=round(frames*1000/rate),priority=priority,gain=gain,combat=combat,
                    sha256=digest(target),source_cuts=[cuts[c] for c in codes])
        installed.append(record)
    for name,cid,select,hurt,death in PLAYERS:
        install(name+'_SELECT',[select],75,.75,False)
        for i,code in enumerate(hurt,1):install(name+f'_HURT_{i}',[code],40,.65)
        install(name+'_DEATH',[death],110,.75)
    for i,code in enumerate(['WI27H02','WI27H04','WI27H03','WI27H06','WI27H10'],1):install(f'WITCH_HURT_{i}',[code],30,.6)
    install('WITCH_DEATH',['WI27D07'],95,.6,path='witch_death.wav')
    for i,code in enumerate(['WI27P01','WI27P02','WI27P03','WI27P05','WI27P07','WI27P09'],1):install(f'WITCH_QUOTE_{i}',[code],25,.6)
    for i,code in enumerate(['VL27H02','VL27H05','VL27H08','VL27H09'],1):
        install('VAMPIRE_HURT' if i==1 else f'VAMPIRE_HURT_{i}',[code],30,.6,path='vampire_hurt.wav' if i==1 else None)
    install('VAMPIRE_DEATH',['VL27D08','VL27D01'],95,.6,path='vampire_death.wav')
    for i,code in enumerate(['VL27P01','VL27P02','VL27P03','VL27P08','VL27P06'],1):install(f'VAMPIRE_QUOTE_{i}',[code],25,.6)
    assert len(installed)==53 and len({r['path'] for r in installed})==53
    (ASSETS/'CHARACTER-VOICE-SELECTIONS.json').write_text(json.dumps(installed,ensure_ascii=False,indent=2),encoding='utf-8')
    (ASSETS/'CHARACTER-VOICE-SOURCES.txt').write_text('''USER-APPROVED CHARACTER / WITCH / VAMPIRE RECORDINGS — PACK27
Original recordings supplied by the user (Voices folder); no synthetic replacement.
Selected cuts copied byte-exact; SHA256 and source cut times in CHARACTER-VOICE-SELECTIONS.json.
Vampire last death: VL27D08 then VL27D01, native PCM frames concatenated without a gap.
Replaces previous witch_death.wav, vampire_hurt.wav and vampire_death.wav content.
Other Vampire skill/bite/form/escape effects and all other actors are unchanged.
Old audition/source recordings remain outside the shipped assets for recovery.
No external recording license is inferred from user supply; user must retain usage rights.
No pitch/EQ/level adjustment or new lossy encoding performed during installation.
''',encoding='utf-8')
    print(json.dumps(installed,ensure_ascii=False))

if __name__=='__main__':main()
