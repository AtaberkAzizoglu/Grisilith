"""Preview-only character reactions from approved voice profiles; no game asset writes."""
from pathlib import Path
import concurrent.futures
import hashlib
import html
import importlib.util
import json
import threading
import numpy as np
import onnxruntime as ort
import soundfile as sf
from scipy.signal import butter, sosfilt, resample_poly, resample, stft, istft
from kokoro_onnx import Kokoro

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "listening-pack-25"
MODEL = ROOT / "sources/character-tts"
RATE = 44100
VERSION = "character-reactions-v1"

def load_module(name, file):
    spec = importlib.util.spec_from_file_location(name, ROOT / file)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

plain = load_module("plain_character_voice", "prepare-character-voices.py")
monster = load_module("monster_character_voice", "prepare-character-voices-24.py")

def selected(pack, code):
    folder = ROOT / f"listening-pack-{pack}"
    record = json.loads((folder / f"{code}.json").read_text(encoding="utf-8"))
    path = folder / f"{code}.wav"
    assert hashlib.sha256(path.read_bytes()).hexdigest() == record["sha256"]
    return dict(pack=pack, code=code, path=path, record=record)

CHARACTERS = [
    dict(id="IM", name="Sir Imbert", source=selected(24,"IMB5"), processor="monster",
         notes="Acıyı kabullenen kısa tepki ve toparlanma nefesi. Ölümde savaşçıya uygun, daha doğal ve ölçülü yaklaşım hedefi."),
    dict(id="YW", name="Ywaleth Yoster", source=selected(23,"YW10"), processor="plain",
         notes="Acıyı kabullenen kısa ses ve derin nefes. Ölümde darbe tepkisi, güç kaybı ve son nefes hedefi."),
    dict(id="LN", name="Linlos Dustman", source=selected(23,"LD1"), processor="plain",
         notes="Hasarda abartılı kısa AAA; ölümde daha uzun ve tiz erkek çığlığı hedefi. Ses bankası ve perde ayarı LD1 ile aynı."),
    dict(id="KB", name="Koba", source=selected(24,"ORC10"), processor="monster",
         notes="İki ayrı hasar havuzu: acıdan keyif alma ve acının fazla gelmesi. Sonraki kurulumda bunlar dönüşümlü kullanılacak; bu tur mekanik eklenmiyor."),
    dict(id="LV", name="Lavena Vol", source=selected(23,"LV3"), processor="plain",
         notes="Hasarda yalnız nefes; kelime, inilti veya çığlık yok. Ölümde kısa, ince çığlık hedefi ve ardından derin nefes."),
]

HURT = ["ˈʌh!", "ˈəɡ!", "ˈɑh!", "ˈɜːh!", "hˈʌ!", "hˈə!", "ˈɑː!", "ˈʌːh!", "ˈəː!", "hˈɑː!"]
STARTLE = ["ˈɑː!", "ˈæː!", "hˈɑː!", "ˈaː!", "ˈɑː, ˈɑ!", "ˈæ, ˈæː!", "ˈɐː!", "hˈæː!", "ˈɑːh!", "ˈæːh!"]
LAUGH = ["hˈɑ, hˈɑ!", "hˈʌ, hˈʌ!", "hˈɛh! hˈɛh!", "hˈɑh! hˈə!", "ˈmː, hˈɑ!", "ˈəː, hˈɑh!", "hˈʌh, hˈɑh!", "hˈɑ, hˈɑ, hˈɑ!", "hˈɛ, hˈɛ, hˈɑ!", "hˈɑː, hˈʌh!"]
PAIN = ["ˈɜːɡ!", "ˈʌːɡ!", "ˈɑːɡ!", "ˈəːɡ!", "hˈɑː!", "hˈʌː!", "ˈɜːh, ˈɑ!", "ˈʌː, hˈɑ!", "ˈɑːh!", "ˈɜːɡ, ˈʌ!"]
DEATH = ["ˈɑːh!", "ˈɜːh!", "ˈʌɡ... ˈɑːh!", "hˈɑː... ˈə!", "ˈɑː... ˈʌ!", "ˈəːh... hˈɑ!", "hˈʌ! ˈɑːh!", "ˈɜːɡ... ˈɑː!", "ˈɑːh... hˈə!", "ˈɜːh... ˈʌ!"]
LINLOS_DEATH = ["ˈɑːh!", "ˈæːh!", "ˈɑːɑː!", "ˈæːæː!", "ˈɑː! ˈɑːh!", "ˈæː! ˈæːh!", "ˈɑːɑːh... hˈɑ!", "ˈæːæːh... hˈæ!", "ˈɑːɑːɑːh!", "ˈæːæːæːh!"]

def group(code, title, phonemes, pairs, mode, lengths, breath=.0):
    assert len(pairs)==5 and len(phonemes)==10
    return dict(code=code, title=title, phonemes=phonemes, pairs=pairs, mode=mode,
                lengths=lengths, breath=breath)

def groups(c):
    cid=c["id"]
    if cid in ("IM","YW"):
        prefix="IM" if cid=="IM" else "YW"
        return [
            group(prefix+"H", "Hasar alma · kısa acı + derin nefes", HURT,
                  ["Dişini sıkarak kabullenme", "Boğazda kalan acı", "Ani darbe, kontrollü toparlanma", "Sessiz direnç", "Yorgun ama dayanıklı"],
                  "stoic", [.38,.45,.48,.55,.32,.4,.58,.65,.42,.52], .8),
            group(prefix+"D", "Ölüm · savaşta güç kaybı", DEATH,
                  ["Darbe ve son nefes", "Boğuk acı, gücün tükenmesi", "Yaralı savaşçı", "Kısa mücadele, çöküş", "Uzayan son inilti"],
                  "battle_death", [.9,1.05,1.35,1.5,1.2,1.45,1.45,1.65,1.8,2.0], .8),
        ]
    if cid=="LN":
        return [
            group("LNH", "Hasar alma · abartılı kısa AAA", STARTLE,
                  ["Ani irkilme", "Panikli kısa çığlık", "Kesik iki tepki", "Şaşkın ve tiz", "Biraz daha uzun AAA"],
                  "startle", [.45,.52,.6,.7,.7,.8,.62,.72,.85,.95], .2),
            group("LND", "Ölüm · tiz erkek çığlığı", LINLOS_DEATH,
                  ["Kısa ve keskin", "Panikli, açık sesli", "İki aşamalı çığlık", "Nefesi tükenen çığlık", "Tek nefeste son haykırış"],
                  "scream", [1.05,1.2,1.5,1.65,1.5,1.8,1.9,2.05,2.25,2.5], .25),
        ]
    if cid=="KB":
        return [
            group("KBH", "Hasar alma A · acıdan keyif alma", LAUGH,
                  ["Kısa, karanlık kıkırdama", "Alaycı boğaz kahkahası", "Memnuniyet ve kaba gülüş", "Meydan okuyan kahkaha", "Daha rahatsız edici gülüş"],
                  "pleasure", [.75,.85,.9,1.,1.,1.1,1.15,1.25,1.3,1.45], .2),
            group("KBP", "Hasar alma B · acı artık fazla", PAIN,
                  ["İstemeden çıkan boğuk acı", "Sert ve rahatsız inleme", "Nefesle bastırılan acı", "Direnci kırılan ork", "Artık keyif almayan hırıltı"],
                  "overwhelmed", [.55,.65,.65,.75,.75,.85,.9,1.,1.05,1.2], .45),
            group("KBD", "Ölüm · vahşi ork", DEATH,
                  ["Kaba son homurtu", "Boğazdan vahşi çığlık", "İki aşamalı çöküş", "Direnerek son haykırış", "Uzun, canavarımsı ölüm"],
                  "feral_death", [1.1,1.3,1.65,1.85,1.8,2.,2.15,2.35,2.6,2.85], .4),
        ]
    return [
        group("LVH", "Hasar alma · yalnız derin nefes", HURT,
              ["Kontrollü iç çekiş", "Ani, derin soluk", "Uzun nefesle toparlanma", "Kesik alıp yavaş bırakma", "Yorgun, ağır soluk"],
              "breath_only", [1.,1.15,1.2,1.35,1.45,1.6,1.3,1.5,1.65,1.8]),
        group("LVD", "Ölüm · kısa çığlık + derin nefes", STARTLE,
              ["İnce, kısa tepki ve nefes", "Keskin çığlık, yavaş soluk", "Ani çığlık, gücün tükenmesi", "Nefesi kesilen büyücü", "Biraz daha uzun son tepki"],
              "lavena_death", [.35,.42,.45,.52,.5,.6,.58,.68,.7,.8], 1.1),
    ]

def stretch(x, rate):
    """Phase-vocoder duration change without changing the selected vocal pitch."""
    if abs(rate-1)<.015 or len(x)<2048:
        return x
    hop=256; nfft=1024
    _,_,spectrum=stft(x, nperseg=nfft, noverlap=nfft-hop, boundary="zeros")
    spectrum=np.pad(spectrum, ((0,0),(0,2)))
    steps=np.arange(0,spectrum.shape[1]-2,rate)
    output=np.empty((spectrum.shape[0],len(steps)), dtype=complex)
    phase=np.angle(spectrum[:,0]); advance=2*np.pi*hop*np.arange(spectrum.shape[0])/nfft
    for i,t in enumerate(steps):
        j=int(t); a=t-j
        magnitude=(1-a)*np.abs(spectrum[:,j])+a*np.abs(spectrum[:,j+1])
        output[:,i]=magnitude*np.exp(1j*phase)
        delta=np.angle(spectrum[:,j+1])-np.angle(spectrum[:,j])-advance
        phase+=advance+delta-2*np.pi*np.round(delta/(2*np.pi))
    _, audio=istft(output, nperseg=nfft, noverlap=nfft-hop)
    return audio[:round(len(x)/rate)]

def duration_shape(x, rate, wanted):
    current=len(x)/rate
    # Avoid extreme digital stretching: a target is a design direction, not a hard
    # truncate/pad command. Preserve the complete generated vocalization.
    return stretch(x, float(np.clip(current/wanted,.7,1.35)))

def breath(reference, rate, seconds, seed, cut=False, contour=0):
    """Designed unvoiced breath; NOT a recording of a human inhaling."""
    n=round(rate*seconds)
    ref=resample(reference, n) if len(reference)!=n else reference
    rng=np.random.default_rng(seed)
    magnitude=np.abs(np.fft.rfft(ref))
    # Smooth away voiced harmonics; retain broad spectral colour only.
    width=max(5,int(seconds*90))
    magnitude=np.convolve(magnitude,np.ones(width)/width,mode="same")
    signal=np.fft.irfft(magnitude*np.exp(1j*rng.uniform(-np.pi,np.pi,len(magnitude))), n=n)
    signal=sosfilt(butter(2,[220,6500], btype="bandpass", fs=rate, output="sos"),signal)
    signal/=max(float(np.max(np.abs(signal))),1e-6)
    t=np.linspace(0,1,n)
    if contour==1:  # startled intake, then release
        envelope=t**.2*(1-t)**1.2
    elif contour==2:  # slower, sustained air flow
        envelope=np.sin(np.pi*t)**.4
    elif contour==3:  # interrupted intake and second breath
        envelope=np.exp(-((t-.23)/.14)**2)+.65*np.exp(-((t-.67)/.23)**2)
    elif contour==4:  # tired breath with a slower attack
        envelope=t**.8*(1-t)**.5
    else:
        envelope=np.sin(np.pi*t)**.8
    envelope/=max(float(np.max(envelope)),1e-6)
    if cut:
        envelope*=1-.65*np.exp(-((t-.38)/.06)**2)
    return signal*envelope

def finish(x, rate):
    x=resample_poly(x,147,80) if rate==24000 else resample_poly(x,RATE,rate)
    assert len(x)>RATE*.12 and np.isfinite(x).all()
    x*=.25/max(float(np.max(np.abs(x))),1e-6)
    fade=min(441,len(x)//5)
    x[:fade]*=np.linspace(0,1,fade);x[-fade:]*=np.linspace(1,0,fade)
    return x

def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()

def main():
    OUT.mkdir(exist_ok=True)
    opts=ort.SessionOptions();opts.intra_op_num_threads=3;opts.inter_op_num_threads=1
    session=ort.InferenceSession(str(MODEL/"kokoro-v1.0.onnx"), sess_options=opts,providers=["CPUExecutionProvider"])
    engine=Kokoro.from_session(session,str(MODEL/"voices-v1.0.bin"))
    lock=threading.Lock()
    tasks=[]
    for character in CHARACTERS:
        for category in groups(character):
            for index in range(10):
                tasks.append((character,category,index))

    def render(character, category, index):
        code=category["code"]+str(index+1)
        original=character["source"]["record"]["recipe"]
        recipe=dict(version=VERSION+("-v2" if category["code"] in ("LND","LVH") else ""), character=character["name"], selection=character["source"]["code"],
                    approved_voice_profile=original, processor=character["processor"],
                    phonemes=category["phonemes"][index], mode=category["mode"],
                    target_seconds=category["lengths"][index], breath_seconds=category["breath"],
                    style=category["pairs"][index//2], take="A" if index%2==0 else "B",
                    speed_ratio=1.+(.035 if index%2 else -.035), seed=8000+tasks.index((character,category,index)))
        file=OUT/f"{code}.wav"; meta=OUT/f"{code}.json"
        if meta.exists() and file.exists():
            previous=json.loads(meta.read_text(encoding="utf-8"))
            if previous["recipe"]==recipe and previous["sha256"]==digest(file):
                return previous
        raw,rate=engine.create(recipe["phonemes"],voice=original["voice"],
                               speed=original["speed"]*recipe["speed_ratio"],
                               is_phonemes=True,sentence_pause=.07,clause_pause=.06)
        raw=np.asarray(raw,dtype=float)
        assert len(raw)>1000 and np.max(np.abs(raw))>.001, f"Bad synthesis {code}"
        processor=monster.process if character["processor"]=="monster" else plain.process_voice
        vocal=processor(raw.copy(),rate,original)
        vocal=duration_shape(vocal,rate,recipe["target_seconds"])
        peak=max(float(np.max(np.abs(vocal))),1e-6)
        vocal/=peak
        if category["mode"]=="breath_only":
            vocal=breath(vocal,rate,recipe["target_seconds"],recipe["seed"],cut=index>=6,contour=index//2)
        else:
            if category["mode"] in ("battle_death","scream","feral_death","lavena_death"):
                # Dying-away envelope, no extra pitch shift or different voice identity.
                vocal*=np.linspace(1.,.2,len(vocal))**.55
            if category["breath"]:
                b=breath(vocal,rate,category["breath"]*(1.12 if index%2 else 1.),recipe["seed"])
                b*=.35 if category["mode"]=="stoic" else .28
                vocal=np.concatenate([vocal,np.zeros(round(rate*.065)),b])
        audio=finish(vocal,rate)
        sf.write(file,audio,RATE,subtype="PCM_16")
        result=dict(id=code,recipe=recipe,duration=len(audio)/RATE,sha256=digest(file))
        meta.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding="utf-8")
        print(f"READY {code}: {result['duration']:.2f}s",flush=True)
        return result

    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        records=list(pool.map(lambda item:render(*item), tasks))
    assert len(records)==110 and len({r["sha256"] for r in records})==110
    by_id={r["id"]:r for r in records}
    references=[]
    for character in CHARACTERS:
        source=character["source"]
        target=OUT/f"SELECT-{character['id']}.wav"
        if character["id"]!="KB":
            target.write_bytes(source["path"].read_bytes())
            assert digest(target)==source["record"]["sha256"]
            record=source["record"]
        else:
            # New line is a preview only. Original ORC10 and all game assets stay intact.
            text="On behalf of all the outcasts and who have been called monsters... I will show them the REAL MONSTER!"
            profile=source["record"]["recipe"]
            with lock:
                phonemes=engine.tokenizer.phonemize(text,profile["lang"])
            raw,rate=engine.create(phonemes,voice=profile["voice"],speed=profile["speed"],
                                   is_phonemes=True,sentence_pause=profile["pause"],clause_pause=.16)
            audio=finish(monster.process(np.asarray(raw,dtype=float),rate,profile),rate)
            sf.write(target,audio,RATE,subtype="PCM_16")
            record=dict(recipe={**profile,"text":text},sha256=digest(target),duration=len(audio)/RATE,phonemes=phonemes)
        references.append(dict(character=character["name"],selected=source["code"],
                               file=target.name,record=record))
    (OUT/"SELECTED-REFERENCES.json").write_text(json.dumps(references,ensure_ascii=False,indent=2),encoding="utf-8")
    (OUT/"SECENEKLER.json").write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding="utf-8")
    write_page(by_id,references)
    notices="""PACK 25 — AUDITION ONLY / NO GAME INTEGRATION
Kokoro-82M by hexgrad, Apache License 2.0; kokoro-onnx integration, MIT.
https://huggingface.co/hexgrad/Kokoro-82M
https://github.com/thewh1teagle/kokoro-onnx
Model/license/voice provenance remains as in pack23/24; notices copied below.
Approved voice profiles: Sir Imbert IMB5, Ywaleth YW10, Linlos LD1,
Koba ORC10, Lavena LV3. Same voice bank, pitch and identity-processing settings.
New nonverbal phonemes/phrasing and small speaking-rate variations per take.
Duration: moderate phase-vocoder stretching (.7-1.35 rate), no extra pitch changes.
Death takes have a dying-away amplitude envelope. Koba retains selected distortion,
EQ, subharmonic and modulation. Breath is DESIGNED UNVOICED NOISE using smoothed
spectral colour from the selected-voice synthesis, NOT an actor inhalation recording.
Pure-breath Lavena hurt contains NO voiced layer. Other breath tails follow the voice.
No claim of real acted screams, laughter, pain or exact perceived identity guarantee.
110 synthetic reaction designs (11 groups x10), five pairs per group, 55 comparisons.
Four selection references copied byte-exact; Koba revised line newly previewed with
ORC10 settings. Old previews and all assets/audio and runtime files untouched.
Mono44.1kHz PCM16, -12dBFS peak, short edge fades. Not LUFS normalization.
Raw phonemes, complete approved voice settings, operations, seeds and hashes:
SECENEKLER.json / per-candidate JSON; reference data SELECTED-REFERENCES.json.
No external actor recording, voice cloning or paid service used.
"""
    (OUT/"LICENSE-SOURCES.txt").write_text(notices,encoding="utf-8")
    for name in ("MODEL-LICENSE.txt","RUNTIME-LICENSE.txt","VOICES.md"):
        (OUT/name).write_bytes((MODEL/name).read_bytes())
    verify(records,references)

def write_page(by_id,references):
    esc=html.escape
    body=["""<h1>Karakter sesleri · Seçki 25</h1>
<p><b>Oyuna henüz hiçbir karakter sesi eklenmedi.</b> Seçim replikleri onaylı;
bu sayfada yalnız hasar alma ve ölüm için seçim yapıyoruz.</p>
<p>110 yeni taslak: her kategoride 10 örnek, beş farklı tarzın ikişer varyantı.
Koba'nın keyif alma ve rahatsız olma hasar sesleri ayrı kategorilerdir.</p>
<p><b>Ses kimliği:</b> Seçilen ses bankası, perde, EQ ve Koba'nın yaratık işlemesi
korundu; yeni aktör/ses bankası kullanılmadı. Ancak aynı ayarlar, her ünlemde algılanan
sesin birebir aynı duyulacağını garanti etmez.</p>
<p><b>Önemli sınır:</b> Bunlar gerçek oyuncu performansı değil. Konuşma motoruyla
sözsüz ünlemler üretildi; gerçek bağırış, psikopatça kahkaha veya acı oyunculuğu
motorla kesin kontrol edilemiyor. Etiketler amaçlanan tarzı belirtir.
Nefesler sesin spektral renginden tasarlanmış <b>sentetik nefes dokuları</b>;
gerçek insanın derin nefes kaydı değildir. Lavena hasarda yalnız bu dokuyu kullanır.
Bu motorla özellikle çığlıkların doğallığı sınırlı kalabilir; beğenmezsen kurmayacağız.</p>
<p>Koba'nın yeni cümlesi aynen kullanıldı; dil bilgisi ayrıca değiştirilmedi.
Linlos adı yazımdaki Lanlos yerine oyundaki şekliyle kullanıldı.</p>
<p><a href='LICENSE-SOURCES.txt'>Kaynak ve işlemler</a> · <a href='SECENEKLER.json'>Üretim dökümü</a></p>"""]
    body.append("<nav>"+" · ".join(f"<a href='#{c['id']}'>{esc(c['name'])}</a>" for c in CHARACTERS)+"</nav>")
    refs={r["character"]:r for r in references}
    for c in CHARACTERS:
        ref=refs[c["name"]]
        body.append(f"<section id='{c['id']}'><h2>{esc(c['name'])}</h2><div class='reference'><h3>Onaylanan seçim sesi · {ref['selected']}</h3><p>{esc(ref['record']['recipe']['text'])}</p><audio controls preload='none' src='{ref['file']}'></audio></div><p>{esc(c['notes'])}</p>")
        for g in groups(c):
            body.append(f"<h3>{esc(g['title'])} · {g['code']}1–10</h3>")
            for pair in range(5):
                ids=[g["code"]+str(pair*2+1),g["code"]+str(pair*2+2)]
                combined=[np.zeros(round(RATE*.35))]
                for code in ids:
                    audio,sr=sf.read(OUT/f"{code}.wav");assert sr==RATE
                    combined.extend([audio,np.zeros(round(RATE*.7))])
                comparison=f"PAIR-{ids[0]}-{ids[1]}.wav"
                sf.write(OUT/comparison,np.concatenate(combined),RATE,subtype="PCM_16")
                body.append(f"<div class='pair'><h4>Tarz {pair+1} · {esc(g['pairs'][pair])}</h4><p>İkisini sırayla dinle: {ids[0]} → {ids[1]}</p><audio controls preload='none' src='{comparison}'></audio><div class='takes'>")
                for code in ids:
                    row=by_id[code]
                    body.append(f"<article><label><input type='radio' name='{g['code']}' value='{code}'><b>{code}</b> · Varyant {row['recipe']['take']}</label><p>{row['duration']:.2f} saniye</p><audio controls preload='none' src='{code}.wav'></audio><details><summary>Üretim ayrıntıları</summary><pre>{esc(json.dumps(row['recipe'],ensure_ascii=False,indent=2))}</pre></details></article>")
                body.append("</div></div>")
        body.append("</section>")
    body.append("""<section><h2>Seçim özeti</h2><p>Her başlıktan bir seçenek: Koba için keyif, rahatsızlık ve ölüm ayrı seçilir. Toplam 11 seçim.</p><button id='summary'>Özeti oluştur</button><textarea id='result' rows='4' readonly></textarea></section>""")
    page="""<!doctype html><html lang='tr'><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>Grisilith · Karakter hasar/ölüm sesleri</title><style>
body{margin:0;background:#17191c;color:#eee;font:17px/1.55 system-ui}main{max-width:1120px;margin:auto;padding:26px}a,h2{color:#e6c482}p{color:#d1d1d1}nav{position:sticky;top:0;z-index:2;background:#17191c;padding:14px}section{padding-top:22px;margin-top:25px;border-top:1px solid #555;scroll-margin-top:65px}h3{margin-top:32px}.reference{background:#30332b;padding:18px;border:1px solid #9b875b;border-radius:8px}.pair{background:#23262b;padding:18px;border-radius:8px;margin:18px 0}.takes{display:grid;grid-template-columns:1fr 1fr;gap:18px}article{padding:15px;background:#30333a;border-radius:6px}audio{width:100%;margin:8px 0}h4{margin:0}label{cursor:pointer}pre{white-space:pre-wrap;font-size:12px}button{padding:12px;background:#e6c482;border:0}textarea{width:100%;background:#30333a;color:white;margin-top:14px}input{margin-right:9px}@media(max-width:650px){.takes{grid-template-columns:1fr}main{padding:14px}nav{position:static}}</style><main>"""+"".join(body)+"""</main><script>
document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));
document.getElementById('summary').onclick=()=>{let chosen=Array.from(document.querySelectorAll('input:checked'));document.getElementById('result').value=chosen.map(x=>x.value).join(', ')+(chosen.length<11?'\nEksik kategori: '+(11-chosen.length):'');};</script></html>"""
    (OUT/"DINLE.html").write_text(page,encoding="utf-8")

def verify(records, refs):
    page=(OUT/"DINLE.html").read_text(encoding="utf-8")
    for row in records:
        file=OUT/f"{row['id']}.wav"
        assert digest(file)==row["sha256"] and file.name in page
        info=sf.info(file);assert info.channels==1 and info.samplerate==RATE and info.subtype=="PCM_16"
        x,_=sf.read(file);assert np.isfinite(x).all() and np.max(np.abs(x))<.251
        assert np.sqrt(np.mean(x*x))>.003 and info.duration<12
    assert len(list(OUT.glob('PAIR-*.wav')))==55
    assert len(list(OUT.glob('*.wav')))==170
    for ref in refs:
        assert digest(OUT/ref['file'])==ref['record']['sha256']
    for character in CHARACTERS:
        assert digest(character['source']['path'])==character['source']['record']['sha256']
    print("CHARACTER_REACTIONS_PASS: 110 distinct reactions, 55 paired comparisons, 5 selection references; approved source files unchanged; preview-only",flush=True)

if __name__=="__main__":
    main()
