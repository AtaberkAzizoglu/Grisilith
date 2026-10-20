"""100 table-directed, preview-only reactions; preserves the five approved voices."""
import importlib.util
from pathlib import Path
import concurrent.futures
import hashlib
import html
import json
import re
import sys
import numpy as np
import onnxruntime as ort
import soundfile as sf
from kokoro_onnx import Kokoro

ROOT=Path(__file__).resolve().parent
OUT=ROOT/"listening-pack-26"
spec=importlib.util.spec_from_file_location("reactions25",ROOT/"prepare-character-reactions-25.py")
previous=importlib.util.module_from_spec(spec);spec.loader.exec_module(previous)
RATE=previous.RATE
SOURCE=Path(sys.argv[1]) if len(sys.argv)>1 else Path("C:/Users/aaata/Downloads/Grisilith_Character_Voice_Prompts.txt")

# Nonword pronunciations: bracketed acting directions are NEVER spoken, nor
# misrepresented as an instruction-following capability of the Kokoro model.
PHONEMES={
 "Ngh!":"ŋˈə!", "Ugh!":"ˈʌɡ!", "Hngh!":"hŋˈə!", "Grrh!":"ɡɹˈɜːh!", "Nnh!":"nˈə!",
 "Guh!":"ɡˈʌ!", "Argh!":"ˈɑɹɡ!", "Rgh!":"ɹˈəɡ!", "Urgh!":"ˈɜɹɡ!", "Agh!":"ˈɑɡ!",
 "Arrghhh...":"ˈɑːɹɡh...", "Arghhh...":"ˈɑɹɡh...", "Urrrgh...":"ˈɜːɹɡ...", "Aaargh...":"ˈɑːɑɹɡ...",
 "Grrrhh...":"ɡɹˈɜːh...", "Arghhh... ngh...":"ˈɑɹɡh... ŋˈə...",
 "Ughhh...":"ˈʌːɡh...", "Raaaagh...":"ɹˈɑːɑːɡ...", "Nnnargh...":"nˈɑːɹɡ...",
 "Urrghh...":"ˈɜːɹɡh...", "Aaarrrgh!":"ˈɑːɑɹɡ!",
 "Ah!":"ˈɑh!", "Ahh!":"ˈɑːh!", "Gah!":"ɡˈɑ!", "Mngh!":"mŋˈə!", "Egh!":"ˈɛɡ!",
 "Aaagh...":"ˈɑːɑɡ...", "Nnngh...":"nŋˈəː...", "Aagh!":"ˈɑːɡ!", "Ahhh...":"ˈɑːh...",
 "Urrgh...":"ˈɜːɹɡ...", "Aaarrgh...":"ˈɑːɑɹɡ...", "Nghhh...":"ŋˈəːh...", "Aaah!":"ˈɑːɑh!",
 "Aa!":"ˈɑ!", "Aah!":"ˈɑːh!", "Eeeah!":"ˈiːɑh!", "Aaa!":"ˈɑːɑ!", "Yaa!":"jˈɑː!",
 "Eek!":"ˈiːk!", "Aiyah!":"ˈaɪjɑ!", "Waaa!":"wˈɑːɑ!", "Aee!":"ˈɑiː!", "Aaaa!":"ˈɑːɑː!",
 "Aaaaaaaah!":"ˈɑːɑːɑːh!", "Eeeeeaaaah!":"ˈiːiːɑːɑh!", "Aaaaaaagh!":"ˈɑːɑːɑɡh!",
 "Yeeeeaaah!":"jˈiːiːɑːh!", "Aaaaaeee!":"ˈɑːɑːiː!", "Aieeeee!":"ˈaɪiːiː!",
 "Aaaaaaa!":"ˈɑːɑːɑː!", "Eeeeeegh!":"ˈiːiːiːɡh!", "Yiaaaaa!":"jˈiɑːɑː!", "AAAAAH!":"ˈɑːɑːh!",
 "Heh-heh!":"hˈɛh hˈɛh!", "Ooh!":"ˈuːh!", "Hah!":"hˈɑh!", "Ah-ha!":"ˈɑ hˈɑ!", "Hehehe!":"hˈɛ hˈɛ hˈɛ!",
 "GRAH!":"ɡɹˈɑh!", "ARGH!":"ˈɑɹɡ!", "Rrraagh!":"ɹˈɑːɡh!", "Urrgh!":"ˈɜːɹɡ!", "GAAAH!":"ɡˈɑːɑh!",
 "GRRAAAAGH!":"ɡɹˈɑːɑːɡh!", "RRAAAARGH!":"ɹˈɑːɑːɹɡh!", "GRAAAAAH!":"ɡɹˈɑːɑːɑh!",
 "HRAAAAGH!":"hɹˈɑːɑːɡh!", "URRAAAGH!":"ˈɜɹɑːɑɡh!", "RRAAAAAH!":"ɹˈɑːɑːɑh!",
 "GRRRRAGH!":"ɡɹˈɜɹɑɡh!", "Hahaha... AAAARGH!":"hˈɑ hˈɑ hˈɑ... ˈɑːɑɹɡh!",
 "GRAAAAUUGH!":"ɡɹˈɑːɑʌːɡh!", "RRRRAAAAGH!":"ɹˈɜɹɑːɑːɡh!",
 "Mm!":"ˈmː!", "Hm!":"hˈm!", "Nn!":"ˈnː!", "Mmh!":"ˈmːh!", "Hn!":"hˈn!",
 "Mmgh!":"mˈəɡh!", "Mmm!":"ˈmːm!", "Hng!":"hˈəŋ!",
 "Eah!":"ˈɛɑh!", "Aie!":"ˈaɪ!", "Aagh!":"ˈɑːɡh!", "Aaah...":"ˈɑːɑh...", "Eeh!":"ˈiːh!",
}

HEADINGS={"SIR IMBERT":"IM","YWALETH YOSTER":"YW","LINLOS / LANLOS":"LN","KOBA":"KB","LAVENA VOL":"LV"}

def parse(text):
    rows=[]; character=None; category=None
    for line in text.splitlines():
        line=line.strip()
        if line in HEADINGS: character=HEADINGS[line];category=None
        elif character and line.startswith("HURT"): category="H"
        elif character and line.startswith("DEATH"): category="D"
        else:
            match=re.fullmatch(r"(\d{2})\.\s*\[([^\]]+)\]\s*(.+)",line)
            if match:
                assert character and category
                number=int(match[1]);direction=match[2];vocal=match[3]
                assert vocal in PHONEMES, f"Missing pronunciation: {vocal}"
                rows.append(dict(character=character,category=category,number=number,
                                 direction=direction,vocal=vocal,phonemes=PHONEMES[vocal],
                                 id=f"{character}26{category}{number:02}"))
    assert len(rows)==100
    for cid in HEADINGS.values():
        for category in ("H","D"):
            assert sorted(r['number'] for r in rows if r['character']==cid and r['category']==category)==list(range(1,11))
    return rows

def main():
    OUT.mkdir(exist_ok=True)
    raw_source=SOURCE.read_bytes();text=raw_source.decode("utf-8-sig")
    rows=parse(text)
    (OUT/"USER-PROMPTS.txt").write_bytes(raw_source)
    characters={c['id']:c for c in previous.CHARACTERS}
    opts=ort.SessionOptions();opts.intra_op_num_threads=3;opts.inter_op_num_threads=1
    engine=Kokoro.from_session(ort.InferenceSession(str(previous.MODEL/'kokoro-v1.0.onnx'),sess_options=opts,providers=['CPUExecutionProvider']),str(previous.MODEL/'voices-v1.0.bin'))

    def render(row):
        c=characters[row['character']];profile=c['source']['record']['recipe']
        mode='death' if row['category']=='D' else 'hurt'
        # Performance pacing changes, but not the approved bank, pitch or identity EQ.
        pace=(.93 if mode=='death' else 1.04)+((row['number']%3)-1)*.025
        if c['id']=='LV': pace=1.01+((row['number']%3)-1)*.025
        target=(1.55+row['number']*.06) if mode=='death' else (.32+row['number']*.027)
        if c['id']=='LV': target=.52 if mode=='death' else .28
        if c['id']=='KB': target=2.15 if mode=='death' else .7
        if c['id']=='LN': target=2.0 if mode=='death' else .55
        recipe=dict(version='table-reactions26-v1',**row,selection=c['source']['code'],
                    approved_voice_profile=profile,processor=c['processor'],pace=pace,target_seconds=target,
                    prompt_handling='Directions are design metadata, not spoken or an engine instruction.',
                    added_breath=False)
        file=OUT/f"{row['id']}.wav";sidecar=OUT/f"{row['id']}.json"
        if file.exists() and sidecar.exists():
            cached=json.loads(sidecar.read_text(encoding='utf-8'))
            if cached['recipe']==recipe and previous.digest(file)==cached['sha256']:return cached
        samples,rate=engine.create(row['phonemes'],voice=profile['voice'],speed=profile['speed']*pace,
                                   is_phonemes=True,sentence_pause=.1,clause_pause=.07)
        samples=np.asarray(samples,dtype=float)
        assert samples.size>500 and np.max(np.abs(samples))>.0001,row['id']
        processor=previous.monster.process if c['processor']=='monster' else previous.plain.process_voice
        samples=processor(samples,rate,profile)
        samples=previous.duration_shape(samples,rate,target)
        if mode=='death':
            # Preserve the actual generated grunt; taper instead of adding breath or
            # substituting another actor. Final exclamations have a less gradual tail.
            end=.3 if row['vocal'].endswith('!') else .13
            samples*=np.linspace(1.,end,len(samples))**.55
        output=previous.finish(samples,rate)
        sf.write(file,output,RATE,subtype='PCM_16')
        result=dict(id=row['id'],recipe=recipe,duration=len(output)/RATE,sha256=previous.digest(file))
        sidecar.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
        print(f"READY {row['id']}: {result['duration']:.2f}s",flush=True)
        return result

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        records=list(pool.map(render,rows))
    assert len({r['sha256'] for r in records})==100
    (OUT/'SECENEKLER.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
    reference_records=json.loads((ROOT/'listening-pack-25/SELECTED-REFERENCES.json').read_text(encoding='utf-8'))
    for ref in reference_records:
        src=ROOT/'listening-pack-25'/ref['file']
        assert previous.digest(src)==ref['record']['sha256']
        (OUT/ref['file']).write_bytes(src.read_bytes())
        assert previous.digest(OUT/ref['file'])==ref['record']['sha256']
    (OUT/'SELECTED-REFERENCES.json').write_text(json.dumps(reference_records,ensure_ascii=False,indent=2),encoding='utf-8')
    write_page(records,reference_records)
    license_text="""PACK26 — USER TABLE REACTIONS; AUDITION ONLY / NOT INSTALLED
Kokoro-82M (hexgrad), Apache2: https://huggingface.co/hexgrad/Kokoro-82M
kokoro-onnx (thewh1teagle), MIT: https://github.com/thewh1teagle/kokoro-onnx
Identities retained: IMB5 am_onyx/-3.1, YW10 zf_xiaobei/0, LD1 am_puck/+1.2,
ORC10 am_onyx/-6.8, LV3 af_nicole/-0.4. Exact selected processors from pack23/24.
100 rows parsed from user-provided Grisilith_Character_Voice_Prompts.txt.
Bracketed directions are NOT spoken. Kokoro cannot directly act those prompt
instructions; only phonetic vocalizations, pacing, moderate duration treatment
and death amplitude envelopes approximate that direction. No acted scream guarantee.
Nonwords mapped manually to IPA, no vocabulary/phrases added to the reactions.
NO designed breath/noise tails in new clips. The base voice may have airy timbre.
Selected distortion/EQ/subharmonics are preserved. No different actor/layer added.
100 candidates, 50 pair comparisons, 5 byte-exact selected selection references.
Four are original pack23/24 choices; Koba reference is the approved ORC10 profile
with user's revised line from pack25. Previous previews are not overwritten.
Mono44.1kHz PCM16, -12dBFS peaks, 10ms fades; NOT perceived loudness normalization.
Profiles, IPA, surface nonwords, user acting directions, timing and hashes recorded
in SECENEKLER.json. Source table copied verbatim into USER-PROMPTS.txt.
No game asset, runtime integration or paid service involved.
"""
    (OUT/'LICENSE-SOURCES.txt').write_text(license_text,encoding='utf-8')
    for notice in ('MODEL-LICENSE.txt','RUNTIME-LICENSE.txt','VOICES.md'):
        (OUT/notice).write_bytes((previous.MODEL/notice).read_bytes())
    page=(OUT/'DINLE.html').read_text(encoding='utf-8')
    for r in records:
        file=OUT/f"{r['id']}.wav";info=sf.info(file);x,_=sf.read(file)
        assert previous.digest(file)==r['sha256'] and file.name in page
        assert info.samplerate==RATE and info.channels==1 and info.subtype=='PCM_16'
        assert .1<info.duration<10 and np.isfinite(x).all() and np.max(np.abs(x))<.251
        assert np.sqrt(np.mean(x*x))>.003
        assert r['recipe']['approved_voice_profile']==characters[r['recipe']['character']]['source']['record']['recipe']
    assert len(list(OUT.glob('PAIR-*.wav')))==50 and len(list(OUT.glob('*.wav')))==155
    assert (OUT/'USER-PROMPTS.txt').read_bytes()==raw_source
    print('TABLE_REACTIONS_PASS: 100 table rows, 100 unique candidates, exact approved identities, 5 byte-exact selected references, 50 pairs, no added breath, preview-only',flush=True)

def write_page(records,refs):
    esc=html.escape
    body=["""<h1>Karakter sesleri · Seçki 26</h1>
<p><b>Henüz oyuna eklenmedi.</b> Eklediğin tablodan 100 yeni örnek: 5 karakter için 10'ar hasar ve 10'ar ölüm.
Her karakterin en başında onayladığın seçilme sesi birebir dosya olarak bulunuyor.</p>
<p>IMB5, ORC10, LV3, YW10, LD1 ses bankaları ve perde/tını işlemleri korundu.
Koba'nın referansı son verdiğin yeni replikle Seçki25'teki önizlemenin birebir kopyasıdır.</p>
<p><b>Yeni örneklerde nefes katmanı yok.</b> Dosyandaki yeni yön esas alındı:
Lavena hasarda kısa kapalı ağız ünlemleri; ölümde kısa çığlık, nefes eklenmeden.</p>
<p><b>Üretim sınırı:</b> Köşeli parantezdeki oyunculuk tarifi seslendirilmedi.
Kokoro bu tarifi doğrudan uygulayan bir oyunculuk motoru değil; tablondaki ünlemleri
seçili sesle, fonetik giriş ve tempo/süre/zarf düzenlemeleriyle taslak olarak ürettim.
Gerçek çığlık/kahkaha/acı oyunculuğu ve algılanan birebir ses benzerliği garanti değil.
Aşağıda İngilizce tarifler <i>hedeflenen yorum</i> olarak gösterilir, gerçekleştiği iddiası değildir.</p>
<p>Yeni kodlar <b>IM26H01</b> gibi başlar: IM/YW/LN/KB/LV karakter, 26 seçki,
H hasar, D ölüm. Her kartta tam kod var. 01–05 ana yön, 06–10 alternatif;
Koba hasarda özel olarak 01–05 keyif alma, 06–10 fazla acı tepkisi.</p>
<p><a href='USER-PROMPTS.txt'>Gönderdiğin tablo</a> · <a href='LICENSE-SOURCES.txt'>Kaynak/işlemler</a> ·
<a href='../listening-pack-25/DINLE.html'>Önceki hasar/ölüm seçkisi (korundu)</a></p>"""]
    body.append("<nav>"+' · '.join(f"<a href='#{c['id']}'>{esc(c['name'])}</a>" for c in previous.CHARACTERS)+"</nav>")
    ref_by_name={r['character']:r for r in refs}
    for c in previous.CHARACTERS:
        ref=ref_by_name[c['name']]
        body.append(f"<section id='{c['id']}'><h2>{esc(c['name'])}</h2><div class='reference'><h3>Önceden seçilen ses · {ref['selected']}</h3><p>{esc(ref['record']['recipe']['text'])}</p><audio controls preload='none' src='{ref['file']}'></audio></div>")
        for category in ('H','D'):
            title='Hasar alma' if category=='H' else 'Ölüm'
            body.append(f"<h3>{title} · 10 yeni örnek</h3>")
            subset=[r for r in records if r['recipe']['character']==c['id'] and r['recipe']['category']==category]
            if c['id']=='KB' and category=='H': body.append('<p>01–05 acıdan keyif alma; 06–10 artık dayanamadığı acı. Bunlar ayrı havuzlar olarak değerlendirilecek.</p>')
            for start in range(0,10,2):
                two=subset[start:start+2];parts=[np.zeros(round(.3*RATE))]
                for r in two:
                    audio,sr=sf.read(OUT/f"{r['id']}.wav");assert sr==RATE
                    parts.extend([audio,np.zeros(round(.65*RATE))])
                pair=f"PAIR-{two[0]['id']}-{two[1]['id']}.wav"
                sf.write(OUT/pair,np.concatenate(parts),RATE,subtype='PCM_16')
                body.append(f"<div class='pair'><h4>{two[0]['id']} → {two[1]['id']}</h4><audio controls preload='none' src='{pair}'></audio><div class='takes'>")
                for r in two:
                    data=r['recipe'];group=f"{c['id']}-{category}"
                    # Koba gets separate choices for pleasure vs overwhelmed pain.
                    if c['id']=='KB' and category=='H':group+='-pleasure' if data['number']<=5 else '-pain'
                    body.append(f"<article><label><input type='radio' name='{group}' value='{r['id']}'><b>{r['id']}</b></label><p class='vocal'>{esc(data['vocal'])}</p><p>Hedef yorum: {esc(data['direction'])}</p><p>{r['duration']:.2f} saniye</p><audio controls preload='none' src='{r['id']}.wav'></audio></article>")
                body.append('</div></div>')
        body.append('</section>')
    body.append("""<section><h2>Seçimlerin</h2><p>Koba için iki ayrı hasar tepkisi seçebilirsin. Seçim özeti oyun dosyalarını değiştirmez.</p><button id='summary'>Özeti oluştur</button><textarea id='result' rows='4' readonly></textarea></section>""")
    page="""<!doctype html><html lang='tr'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Grisilith · Seçki26 karakter tepkileri</title><style>
body{margin:0;background:#17191c;color:#eee;font:17px/1.55 system-ui}main{max-width:1120px;margin:auto;padding:26px}a,h2{color:#e6c482}p{color:#d1d1d1}nav{position:sticky;top:0;background:#17191c;padding:14px;z-index:2}section{border-top:1px solid #555;margin-top:30px;padding-top:20px;scroll-margin-top:65px}.reference{background:#30332b;padding:18px;border:1px solid #9b875b;border-radius:8px}.pair{background:#23262b;padding:18px;border-radius:8px;margin:18px 0}.takes{display:grid;grid-template-columns:1fr 1fr;gap:18px}article{background:#30333a;padding:15px;border-radius:6px}audio{width:100%;margin:8px 0}.vocal{font-size:24px;color:#e6c482}button{padding:12px;background:#e6c482;border:0}textarea{width:100%;background:#30333a;color:white;margin-top:14px}label{cursor:pointer}input{margin-right:9px}@media(max-width:650px){.takes{grid-template-columns:1fr}main{padding:14px}nav{position:static}}</style><main>"""+''.join(body)+"""</main><script>
document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));
document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(x=>x.value).join(', ');</script></html>"""
    (OUT/'DINLE.html').write_text(page,encoding='utf-8')

if __name__=='__main__': main()
