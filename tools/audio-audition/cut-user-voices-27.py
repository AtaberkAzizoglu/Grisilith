"""Reviewed, non-generative cuts of user recordings. No game assets are changed."""
from pathlib import Path
import importlib.util,csv,hashlib,html,json
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parent
INPUT=Path('C:/Users/aaata/OneDrive/Masaüstü/Görseller/Voices')
OUT=ROOT/'listening-pack-27'
WORK=ROOT/'sources/user-voices-analysis'
spec=importlib.util.spec_from_file_location('reviewed_voice_cuts',ROOT/'user-voice-boundaries.py')
boundaries=importlib.util.module_from_spec(spec);spec.loader.exec_module(boundaries)
TITLES=dict(S='Seçilme repliği',H='Hurt · Hasar alma',D='Death · Ölüm',P='Punchline · Replikler')
PUNCHLINES={
 'WI':["Hee-hee! Fresh little souls!","Come closer, my pretty!","I'll boil your bones!","Hee-hee! Such lovely suffering!","Rot! Rot from within!","Oh dear! Did that hurt, dearie?","You filthy little wretch!","Hehehe! Dance, little puppet!","I'll curse your bloodline!","No! My magic! My beautiful magic!"],
 'VL':["You dare challenge eternity!","Your blood shall be mine!","Kneel before your lord!","I have buried kings greater than you!","I can hear your heart trembling!","You will regret that!","Yes. Struggle for me!","Enough!","I am immortal!","No! My reign cannot end!"],
}
SELECTIONS=dict(IM='It is my oath! I will fight until the magic is gone from this realm!',
 YW="The footprints lead in this direction. I'm on the trail!",
 LN="I spoke with an ant the other day, but I didn't understand what he said. I guess I'm not so good at talking to animals.",
 KB='On behalf of all the outcasts and who have been called monsters... I will show them the REAL MONSTER!',
 LV='And they call these spells forbidden magic. Ha! What\'s the worst that could happen anyway?')

def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def trim_region(x,rate,start,end,numbered=False,activity=None):
    a=max(0,round(start*rate));b=min(len(x),round(end*rate))
    assert b>a
    region=x[a:b]
    width=max(1,round(rate*.003))
    rms=np.sqrt(np.convolve(region.astype(float)**2,np.ones(width)/width,mode='same'))
    threshold=max(.000055,float(rms.max())*.0025)
    active=np.flatnonzero(rms>threshold)
    assert len(active),f'Empty interval {start}..{end}'
    if numbered:
        # Very faint digit decay must not become the beginning of a reaction.
        # Retain each whole vocal core and its quiet margins, bounded by the
        # middle of the gaps toward the excluded labels on both sides.
        hop,global_rms,global_threshold=activity
        grid=np.arange(len(global_rms))*hop
        core=grid[(grid>=a)&(grid+hop<=b)&(global_rms>global_threshold)]
        assert len(core),f'No reviewed vocal core {start}..{end}'
        first_core=int(core[0])-a;last_core=int(core[-1])+hop-a
        leading=min(round(rate*.08),first_core//2)
        trailing=min(round(rate*.10),(len(region)-last_core)//2)
        first=first_core-leading
        last=last_core+trailing
    else:
        first=max(0,int(active[0])-round(rate*.025))
        last=min(len(region),int(active[-1])+round(rate*.065)+1)
    # Only outer silence is trimmed. All internal pauses stay in the same take.
    clip=region[first:last].astype(float).copy()
    peak=float(np.max(np.abs(clip)));gain=1.0 if peak<.9999 else .98/peak
    clip*=gain
    fade=min(round(rate*.002),len(clip)//8)
    clip[:fade]*=np.linspace(0,1,fade);clip[-fade:]*=np.linspace(1,0,fade)
    return clip,(a+first)/rate,(a+last)/rate,gain

def main():
    OUT.mkdir(exist_ok=True);(OUT/'originals').mkdir(exist_ok=True)
    checks=json.loads((WORK/'all-label-checks.json').read_text(encoding='utf-8'))
    reviewed={
      'IM27H02':'"Oh, too" is homophone of spoken 02; full sequence agrees.',
      'IM27D02':'"Oh, too" is homophone of spoken 02; full sequence agrees.',
      'KB27H02':'"Oh, too" is homophone of spoken 02; full sequence agrees.',
      'WI27H04':'Isolated "full" is a homophone-like ASR error; full transcript and sequence identify Four.',
      'WI27D04':'Isolated "full" is a homophone-like ASR error; full transcript and sequence identify Four.',
      'WI27P04':'Isolated "full" is a homophone-like ASR error; full transcript and sequence identify Four.',
    }
    clips=[];exclusions=[];source_records=[]
    for source in boundaries.SOURCES:
        file=INPUT/source['file'];sha=digest(file)
        x,rate=sf.read(file,dtype='float64');assert x.ndim==1 and rate==44100
        hop=round(rate*.005);frame_count=len(x)//hop
        global_rms=np.sqrt(np.mean(x[:frame_count*hop].reshape(frame_count,hop)**2,axis=1))
        activity=(hop,global_rms,max(.002,float(global_rms.max())*.012))
        rawcopy=OUT/'originals'/file.name
        rawcopy.write_bytes(file.read_bytes());assert digest(rawcopy)==sha
        source_records.append(dict(file=file.name,sha256=sha,frames=len(x),rate=rate,
                                   decoded_seconds=len(x)/rate,original_copy='originals/'+file.name))
        def save(kind,number,start,end,label=None,label_present=True):
            audio,a,b,gain=trim_region(x,rate,start,end,numbered=kind!='S',activity=activity)
            code=f"{source['character']}27{kind}{number:02}"
            target=OUT/f'{code}.wav';sf.write(target,audio,rate,subtype='PCM_16')
            text=SELECTIONS.get(source['character'],'') if kind=='S' else PUNCHLINES[source['character']][number-1] if kind=='P' else ''
            record=dict(id=code,character=source['character'],name=source['name'],kind=kind,
                        number=number,file=target.name,source=file.name,source_sha256=sha,
                        source_start=a,source_end=b,window_start=start,window_end=end,
                        duration=len(audio)/rate,rate=rate,frames=len(audio),gain=gain,
                        excluded_label=label,label_present=label_present,content_text=text,
                        sha256=digest(target),peak=float(np.max(np.abs(audio))))
            clips.append(record)
            print(f"CUT {code} {a:.3f}..{b:.3f} ({record['duration']:.3f}s)",flush=True)
        if source.get('selection'):save('S',1,*source['selection'])
        for group in source['groups']:
            assert len(group['labels'])==10
            for i,(start,end) in enumerate(group['labels'],1):
                key=f"{source['character']}27{group['kind']}{i:02}"
                present=i not in group.get('missing_labels',[])
                if present:
                    check=checks[key]
                    assert check['start']==start and check['end']==end and check['file']==file.name,key
                    assert check['recognized'] or key in reviewed,key
                    exclusions.append(dict(id=key,source=file.name,start=start,end=end,
                                           independent_text=check['text'],recognized=check['recognized'],
                                           review=reviewed.get(key,'Expected numeral independently recognized.')))
                next_start=group['labels'][i][0] if i<10 else group['end']
                save(group['kind'],i,end+.012,next_start-.012,[start,end] if present else None,present)
        assert digest(file)==sha,'Original changed'
    assert len(clips)==165 and len(exclusions)==159
    assert sum(c['kind']=='S' for c in clips)==5
    assert len({c['sha256'] for c in clips})==165
    write_page(clips,source_records)
    (OUT/'CUTS.json').write_text(json.dumps(dict(clips=clips,sources=source_records,exclusions=exclusions),ensure_ascii=False,indent=2),encoding='utf-8')
    with (OUT/'CUTS.csv').open('w',newline='',encoding='utf-8-sig') as stream:
        columns=['id','name','kind','number','source','source_start','source_end','duration','label_present','gain','sha256']
        writer=csv.DictWriter(stream,fieldnames=columns,extrasaction='ignore');writer.writeheader();writer.writerows(clips)
    (OUT/'KAYNAK-VE-KESIM-NOTLARI.txt').write_text("""SECKI 27 — USER-PROVIDED RECORDINGS; NOT INSTALLED
8 original MP3s, 160 numbered/ordered takes plus 5 opening character lines.
Hurt/Death: 10 each for all 7 characters/enemies. Witch/Vampire: 10 Punchlines each.
Player character recordings contain ONE opening selection line, not 10 punchlines.
Original MP3s copied byte-exact into originals/, SHA256 recorded in CUTS.json.
No speech generated, replaced, transposed, EQ'd, compressed, denoised or retimed.
Decoded at native mono44.1kHz, exported PCM16 WAV (no further lossy compression).
Only spoken headings/numerals and outer silence removed. Multi-part cries/laughs
and all pauses inside a take preserved. Reactions retain up to 80ms/100ms outer
margins, bounded by mid-gaps toward neighboring labels; 2ms edge fades.
Gain unchanged unless decoded MP3 overshoot would clip PCM16; per-clip gain logged.
Word timestamps + RMS/silence groups + 159 independent narrow numeral windows
cross-checked. Six homophone cases reviewed against longer context.
Special case: Witch Punchline02 has no separate confidently identified spoken
number; entire Come closer, my pretty phrase kept, including initial Come.
ASR text is not a human listening certification and may contain transcription errors.
Every original and cut interval is linked for direct comparison on DINLE.html.
CUTS.json/CSV contain exact source cut times, excluded numeral intervals, hashes.
Source and redistribution rights supplied by user are not independently established;
do not infer a license from these files. This preview does not install game audio.
Offline analysis tooling: faster-whisper MIT; small.en model by OpenAI/Systran
https://github.com/SYSTRAN/faster-whisper
https://huggingface.co/Systran/faster-whisper-small.en
Only the analysis model was downloaded; user recordings were never uploaded.
""",encoding='utf-8')
    # Validate decoded output and its unprocessed source interior sample-for-sample.
    for row in clips:
        file=OUT/row['file'];info=sf.info(file);audio,_=sf.read(file)
        assert info.channels==1 and info.samplerate==44100 and info.subtype=='PCM_16'
        assert info.frames==row['frames'] and .08<info.duration<15
        assert digest(file)==row['sha256'] and np.isfinite(audio).all()
        assert abs(audio[0])<.00005 and abs(audio[-1])<.00005
        original,_=sf.read(INPUT/row['source'])
        a=round(row['source_start']*44100);b=round(row['source_end']*44100)
        margin=100
        assert np.max(np.abs(audio[margin:-margin]-original[a+margin:b-margin]*row['gain']))<.000032
        if row['excluded_label']:
            assert row['source_start']>=row['excluded_label'][1]+.0119
    assert len(list(OUT.glob('PAIR-*.wav')))==80
    assert len(list(OUT.glob('*.wav')))==245
    print('USER_VOICE_CUTS_PASS: 8 preserved originals, 165 complete clips, 159 numeral exclusions, 80 comparison pairs, native rate and source interiors verified; no game installation',flush=True)

def write_page(clips,sources):
    esc=html.escape
    ordered=[]
    for source in boundaries.SOURCES:
        if source['character'] not in [c['character'] for c in ordered]:ordered.append(source)
    body=["""<h1>Kayıtlarından kesilen sesler · Seçki 27</h1>
<p><b>Bu sesler yeniden üretilmedi.</b> Verdiğin 8 MP3 kaydından kesildi. Orijinaller
korundu; hiçbir ses oyuna eklenmedi. Perde, ton ve hız değiştirilmedi.</p>
<p><b>165 ayrı ses:</b> 7 karakter/düşman için 10'ar Hurt + 10'ar Death = 140;
Witch ve Vampire Lord için 10'ar Punchline = 20; beş oyuncu karakterinin baştaki
tek seçilme cümlesi = 5. Kayıtlarda bu beş karakter için 10'ar seçilme cümlesi bulunmuyor.</p>
<p>Hurt/Death/Punchline başlıkları ve söylenen sıra numaraları kliplerin dışında
bırakıldı. Çok parçalı inilti ve kahkahalar, replik içindeki duraklamalar korunur.
Yalnız dıştaki sessizlik kısaltıldı ve 2ms kenar yumuşatma uygulandı.</p>
<p>Kod örneği: <b>IM27H01</b> = Imbert / Seçki27 / Hurt / 01.
H hasar, D ölüm, P Punchline, S baştaki seçilme cümlesi.
Witch'in 2. Punchline'ında ayrı bir sayı bölgesi belirlenemedi; <i>Come closer...</i>
cümlesinin başı kesilmeden korundu.</p>
<p>Kesimler konuşma zaman damgası, sessizlik aralıkları ve dar sayı pencereleriyle
çapraz kontrol edildi. Bu, insan tarafından dinleyerek verilmiş bir kusursuzluk
onayı değildir. Her kartın altında orijinaldeki yeri açılıp karşılaştırılabilir.</p>
<p>Repliklerin altındaki metinler yardımcı otomatik dökümdür; asıl içerik ses kaydıdır.
Son kesimlerdeki 25 konuşma cümlesi ayrıca tekrar çözümlenerek kontrol edildi.</p>
<p><a href='CUTS.csv'>Kesim tablosu</a> · <a href='CUTS.json'>Tam zamanlar/hash/kontrol dökümü</a> ·
<a href='KAYNAK-VE-KESIM-NOTLARI.txt'>Kaynak ve işlem notları</a></p>"""]
    body.append('<nav>'+' · '.join(f"<a href='#{s['character']}'>{esc(s['name'])}</a>" for s in ordered)+'</nav>')
    for source in ordered:
        cid=source['character']
        body.append(f"<section id='{cid}'><h2>{esc(source['name'])}</h2>")
        chosen=[c for c in clips if c['character']==cid]
        for kind in ('S','H','D','P'):
            group=sorted([c for c in chosen if c['kind']==kind],key=lambda c:c['number'])
            if not group:continue
            body.append(f"<h3>{TITLES[kind]} · {len(group)} ses</h3>")
            if cid=='KB' and kind=='H':body.append('<p>01–05 acıdan keyif alma, 06–10 rahatsızlık. İki gruptan ayrı seçim yapabilirsin.</p>')
            if kind!='S':
                for offset in range(0,10,2):
                    two=group[offset:offset+2];combined=[np.zeros(11025)]
                    for row in two:
                        a,_=sf.read(OUT/row['file']);combined.extend([a,np.zeros(33075)])
                    name=f"PAIR-{two[0]['id']}-{two[1]['id']}.wav"
                    sf.write(OUT/name,np.concatenate(combined),44100,subtype='PCM_16')
                    body.append(f"<details class='pair'><summary>İkili dinle: {two[0]['id']} → {two[1]['id']}</summary><audio controls preload='none' src='{name}'></audio></details>")
            body.append('<div class="grid">')
            for row in group:
                selectgroup=cid+kind
                if cid=='KB' and kind=='H':selectgroup+='-pleasure' if row['number']<=5 else '-pain'
                original='originals/'+row['source'];start=max(0,row['window_start']-.9);end=row['window_end']+.25
                body.append(f"<article><label><input type='radio' name='{selectgroup}' value='{row['id']}'><b>{row['id']}</b></label><p>{row['duration']:.2f} saniye</p>")
                if row['content_text']:body.append(f"<p>{esc(row['content_text'])}</p>")
                body.append(f"<audio controls preload='none' src='{row['file']}'></audio><details><summary>Kaynakta karşılaştır / kesim aralığı</summary><p>{esc(row['source'])} · {row['source_start']:.3f}–{row['source_end']:.3f}s</p><p>Bu orijinal karşılaştırmada sayılar/başlıklar duyulabilir; üstteki seçim klibine dahil değildir.</p><audio class='source' controls preload='none' data-start='{start:.3f}' data-stop='{end:.3f}' src='{esc(original)}'></audio><button class='seek' data-start='{start:.3f}'>İlgili yeri dinle</button></details></article>")
            body.append('</div>')
        body.append('</section>')
    body.append("""<section><h2>Seçim özeti</h2><button id='summary'>Seçtiklerimi listele</button><textarea id='result' rows='4' readonly></textarea><p>Bu sayfada seçim yapmak oyun dosyalarını değiştirmez.</p></section>""")
    page="""<!doctype html><html lang='tr'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Grisilith · Seçki27 özgün kayıt kesimleri</title><style>
body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1140px;margin:auto;padding:26px}a,h2{color:#e6c482}p{color:#d1d1d1}nav{position:sticky;top:0;background:#17191c;padding:14px;z-index:3}section{border-top:1px solid #555;padding-top:20px;margin-top:28px;scroll-margin-top:70px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:16px}article{background:#282c32;padding:18px;border-radius:8px}audio{width:100%;margin:10px 0}label,summary{cursor:pointer}input{margin-right:10px}button{padding:10px;background:#d5b77a;border:0;border-radius:4px;cursor:pointer}textarea{width:100%;margin-top:14px;background:#282c32;color:#eee}.pair{padding:10px;background:#22262a;margin:8px 0}@media(max-width:650px){main{padding:14px}.grid{grid-template-columns:1fr}nav{position:static}}</style><main>"""+''.join(body)+"""</main><script>
document.querySelectorAll('audio').forEach(a=>{a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()}));if(a.classList.contains('source'))a.addEventListener('timeupdate',()=>{if(a.currentTime>=Number(a.dataset.stop))a.pause()})});
document.querySelectorAll('.seek').forEach(b=>b.onclick=()=>{let a=b.previousElementSibling;a.currentTime=Number(b.dataset.start);a.play()});
document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(i=>i.value).join(', ');</script></html>"""
    (OUT/'DINLE.html').write_text(page,encoding='utf-8')

if __name__=='__main__':main()
