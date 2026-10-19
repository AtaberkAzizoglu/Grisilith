"""Local, synthetic character-selection auditions only. Never writes game assets."""
from pathlib import Path
from fractions import Fraction
import concurrent.futures
import hashlib
import html
import json
import math
import os
import threading

import numpy as np
import onnxruntime as ort
import soundfile as sf
from scipy.signal import resample_poly
from kokoro_onnx import Kokoro

ROOT = Path(__file__).resolve().parent
MODEL = ROOT / "sources/character-tts"
OUT = ROOT / "listening-pack-23"
RATE = 44100
PACK_NUMBER = 23
EXTRA_INTRO = ""
EXTRA_LICENSE = ""

def process_voice(samples, rate, choice):
    """Default audition treatment; later audition packs may supply a custom processor."""
    if choice["pitch"]:
        ratio = Fraction(2 ** (-choice["pitch"] / 12)).limit_denominator(500)
        samples = resample_poly(samples, ratio.numerator, ratio.denominator)
    if choice["rough"]:
        samples = np.tanh(samples * 1.15)
    return samples

def profile(voice, label, speed=1.0, pitch=0.0, lang="en-us", pause=.3, rough=False):
    return dict(voice=voice, label=label, speed=speed, pitch=pitch, lang=lang, pause=pause, rough=rough)

GROUPS = [
    dict(id="IM", name="Sir Imbert", text="It is my oath! I will fight until the magic is gone from this realm!",
         direction="Yetişkin erkek insan şövalye; sert, otoriter ve kararlı. Vurgu hedefi: oath / magic.", choices=[
        profile("bm_george", "Britanya tabanlı; ağırbaşlı, otoriter hedef", .92, -.5, "en-gb"),
        profile("bm_fable", "Britanya tabanlı; kararlı savaşçı hedef", .93, -.8, "en-gb"),
        profile("am_fenrir", "Amerikan tabanlı; sert ve doğrudan hedef", .9, -.7),
        profile("am_michael", "Amerikan tabanlı; tok komutan hedef", .91, -.5),
        profile("bm_daniel", "Britanya tabanlı; kontrollü emir hedef", .94, -.6, "en-gb"),
        profile("am_onyx", "Daha koyu ve ağır şövalye hedef", .87, -1.0),
        profile("bm_lewis", "Britanya tabanlı; daha çevik savaşçı hedef", .97, -.6, "en-gb"),
        profile("am_eric", "Daha kuru ve ölçülü otorite hedef", .91, -.8),
        profile({"bm_george": .6, "am_michael": .4}, "Tek sentetik ses: Britanya/Amerikan tını karışımı", .9, -.7, "en-gb"),
        profile({"am_fenrir": .7, "bm_fable": .3}, "Tek sentetik ses: sert, tok yemin hedef", .88, -1.0),
    ]),
    dict(id="LV", name="Lavena Vol", text='And they call these spells "forbidden magic." Ha! What\'s the worst that could happen, anyway?',
         direction="Yetişkin kadın elf wizard; soğuk, ölçülü, etkileyici. Hafif küçümseyen Ha vurgusu hedefleniyor.", choices=[
        profile("bf_emma", "Britanya tabanlı; sakin, soğuk büyücü hedef", .95, -.3, "en-gb", .38),
        profile("bf_isabella", "Britanya tabanlı; donuk, aristokratik hedef", .92, -.5, "en-gb", .4),
        profile("af_nicole", "Amerikan tabanlı; nefesli ve kontrollü hedef", .95, -.4),
        profile("af_kore", "Amerikan tabanlı; koyu ve katı hedef", .92, -.5),
        profile("bf_lily", "Britanya tabanlı; kuru, ölçülü alay hedef", .94, -.2, "en-gb"),
        profile("af_aoede", "Amerikan tabanlı; sakin ama tehditkâr hedef", .93, -.4),
        profile("af_bella", "Amerikan tabanlı; daha tok ve etkileyici hedef", .95, -.8),
        profile("bf_alice", "Britanya tabanlı; net ve mesafeli hedef", .94, -.4, "en-gb"),
        profile({"bf_emma": .6, "bf_isabella": .4}, "Tek sentetik ses: soğuk elf tınısı hedef", .93, -.5, "en-gb", .4),
        profile({"af_kore": .6, "af_nicole": .4}, "Tek sentetik ses: alçak, büyüsel sakinlik hedef", .9, -.6, pause=.4),
    ]),
    dict(id="YW", name="Ywaleth Yoster", text="The footprints lead in this direction. I'm on the trail!",
         direction="Yetişkin kadın insan ranger; heyecanlı, hırslı ve canlı. 8–10 deneysel Asya tınılarıdır; aksan doğruluğu garanti edilmez.", choices=[
        profile("af_heart", "Amerikan tabanlı; canlı ve hevesli iz sürücü hedef", 1.1, .2, pause=.2),
        profile("af_sarah", "Amerikan tabanlı; net, hızlı ve kararlı hedef", 1.1, .0, pause=.2),
        profile("af_bella", "Amerikan tabanlı; özgüvenli kolcu hedef", 1.08, .3, pause=.2),
        profile("af_nova", "Amerikan tabanlı; atak ve heyecanlı hedef", 1.13, .3, pause=.18),
        profile("bf_alice", "Britanya tabanlı; hafif aksanlı, çevik hedef", 1.1, .2, "en-gb", .2),
        profile("bf_emma", "Britanya tabanlı; kararlı ve hevesli hedef", 1.08, .5, "en-gb", .2),
        profile("af_sky", "Daha hafif ve enerjik yetişkin kadın hedef", 1.05, .0, pause=.2),
        profile("hf_alpha", "Deneysel: Hint/Hindi taban sesinin İngilizce okuması", 1.07, .0, pause=.2),
        profile("jf_alpha", "Deneysel: Doğu Asya/Japonca taban sesinin İngilizce okuması", 1.07, .0, pause=.2),
        profile("zf_xiaobei", "Deneysel: Doğu Asya/Mandarin taban sesinin İngilizce okuması", 1.09, .0, pause=.2),
    ]),
    dict(id="KB", name="Koba", text="On behalf of all the outcasts, on behalf of all those who have been called monsters... I will show them the real monster!",
         direction="Yetişkin erkek half-orc barbarian; kaba, sert, yavaş ve buyurgan. 8–10 güçlü aksan/tını deneyidir. Hafif pürüz uygulanır, canavar sesi katmanı yoktur.", choices=[
        profile("am_fenrir", "Ağır, koyu ve sert barbar hedef", .8, -1.5, pause=.48, rough=True),
        profile("am_onyx", "Daha derin, yavaş ve emredici hedef", .79, -2.0, pause=.5, rough=True),
        profile("bm_george", "Britanya tabanlı; kalın, otoriter hedef", .82, -1.4, "en-gb", .45, True),
        profile("bm_fable", "Britanya tabanlı; kaba ve ağır anlatım hedef", .8, -1.6, "en-gb", .48, True),
        profile("am_michael", "Amerikan tabanlı; gövdeli ve talepkâr hedef", .81, -1.6, pause=.48, rough=True),
        profile("am_adam", "Hırıltıya yakın, daha karanlık taslak", .82, -1.0, pause=.5, rough=True),
        profile("bm_daniel", "Britanya tabanlı; kontrollü, ağır öfke hedef", .81, -1.4, "en-gb", .45, True),
        profile("hm_omega", "Deneysel güçlü tını: Hindi taban sesinin İngilizce okuması", .8, -1.5, pause=.5, rough=True),
        profile("im_nicola", "Deneysel güçlü tını: İtalyanca taban sesinin İngilizce okuması", .8, -1.5, pause=.5, rough=True),
        profile("hm_psi", "Deneysel güçlü tını: ikinci Hindi taban sesinin İngilizce okuması", .8, -1.6, pause=.5, rough=True),
    ]),
    dict(id="LD", name="Linlos Dustman", text="I spoke with an ant the other day, but I didn't understand what he said. I guess I'm not so good at talking to animals.",
         direction="Yetişkin erkek halfling druid; hafif çocuksu, heyecanlı ve sosyal açıdan biraz tuhaf. Gerçek çocuk sesi kullanılmıyor.", choices=[
        profile("am_puck", "Hafif, meraklı yetişkin erkek hedef", 1.05, 1.2, pause=.28),
        profile("bm_lewis", "Britanya tabanlı; heyecanlı ve ince tını hedef", 1.06, 1.0, "en-gb", .3),
        profile("am_liam", "Çocuksulaşmadan daha genç tını hedef", 1.05, 1.2, pause=.3),
        profile("am_echo", "Hızlı, tuhaf biçimde ciddi druid hedef", 1.07, .8, pause=.35),
        profile("am_eric", "Doğal ve hafif çekingen hedef", 1.03, .9, pause=.4),
        profile("bm_daniel", "Britanya tabanlı; ince ve biraz şaşkın hedef", 1.06, 1.3, "en-gb", .32),
        profile({"am_puck": .6, "bm_lewis": .4}, "Tek sentetik ses: sevimli ve heyecanlı hedef", 1.08, 1.0, pause=.3),
        profile("am_puck", "Daha duraksamalı, sosyal açıdan tuhaf hedef", .99, 1.2, pause=.65),
        profile({"am_liam": .4, "am_puck": .6}, "Tek sentetik ses: daha atak, meraklı hedef", 1.1, 1.0, pause=.25),
        profile("bm_lewis", "Britanya tabanlı; düşünerek ve duraksayarak hedef", 1.0, 1.3, "en-gb", .65),
    ]),
]

def main():
    OUT.mkdir(exist_ok=True)
    options = ort.SessionOptions()
    options.intra_op_num_threads = 3
    options.inter_op_num_threads = 1
    session = ort.InferenceSession(str(MODEL / "kokoro-v1.0.onnx"), sess_options=options, providers=["CPUExecutionProvider"])
    synth = Kokoro.from_session(session, str(MODEL / "voices-v1.0.bin"))
    token_lock = threading.Lock()
    manifest = []

    def render(group, index, choice):
        code = f"{group['id']}{index+1}"
        text = choice.get("text", group["text"])
        identity = {"character": group["name"], "text": text, **choice}
        target = OUT / f"{code}.wav"
        sidecar = OUT / f"{code}.json"
        if target.exists() and sidecar.exists() and json.loads(sidecar.read_text(encoding="utf-8"))["recipe"] == identity:
            return json.loads(sidecar.read_text(encoding="utf-8"))
        # Phonemizer setup is not assumed thread-safe; ONNX session inference is.
        with token_lock:
            phonemes = synth.tokenizer.phonemize(text, choice["lang"])
        voice = choice["voice"]
        if isinstance(voice, dict):
            voice = sum(synth.get_voice_style(name) * weight for name, weight in voice.items())
        samples, rate = synth.create(phonemes, voice=voice, speed=choice["speed"],
                                     is_phonemes=True, sentence_pause=choice["pause"], clause_pause=.16)
        samples = np.asarray(samples, dtype=np.float64)
        samples = process_voice(samples, rate, choice)
        samples = resample_poly(samples, 147, 80) if rate == 24000 else resample_poly(samples, RATE, rate)
        assert samples.size > RATE * 2 and np.isfinite(samples).all()
        peak = np.max(np.abs(samples))
        assert peak > .001
        samples *= .25 / peak
        fade = min(441, samples.size//4)
        samples[:fade] *= np.linspace(0, 1, fade)
        samples[-fade:] *= np.linspace(1, 0, fade)
        sf.write(target, samples, RATE, subtype="PCM_16")
        result = {"id": code, "recipe": identity, "duration": len(samples)/RATE,
                  "sha256": hashlib.sha256(target.read_bytes()).hexdigest(), "phonemes": phonemes}
        sidecar.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"VOICE_READY {code}: {result['duration']:.2f}s", flush=True)
        return result

    tasks = [(g, i, p) for g in GROUPS for i, p in enumerate(g["choices"])]
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(lambda task: render(*task), tasks))
    expected = sum(len(g["choices"]) for g in GROUPS)
    assert len(results) == expected and len({r["sha256"] for r in results}) == expected
    by_id = {r["id"]: r for r in results}
    parts = []
    for group in GROUPS:
        code = group["id"]
        esc = html.escape
        parts.append(f"<section id='{code}'><h2>{esc(group['name'])} · {code}1–10</h2><blockquote>{esc(group['text'])}</blockquote><p>{esc(group['direction'])}</p>")
        for start in (0, 5):
            combined = [np.zeros(RATE)]
            for i in range(start, start+5):
                audio, sr = sf.read(OUT / f"{code}{i+1}.wav", dtype="float32")
                assert sr == RATE
                combined.extend([audio, np.zeros(RATE)])
            sf.write(OUT / f"{code}-{start+1}-{start+5}.wav", np.concatenate(combined), RATE, subtype="PCM_16")
            parts.append(f"<p>Toplu dinleme {start+1}–{start+5}</p><audio controls preload='none' src='{code}-{start+1}-{start+5}.wav'></audio>")
        parts.append("<div class='grid'>")
        for i, choice in enumerate(group["choices"]):
            cid = f"{code}{i+1}"
            r = by_id[cid]
            parts.append(f"<article><label><input type='radio' name='{code}' value='{cid}'> <b>{cid}</b> · {esc(choice['label'])}</label><p>{r['duration']:.1f} saniye</p><audio controls preload='none' src='{cid}.wav'></audio><details><summary>Teknik kaynak / taslak ayrıntısı</summary><pre>{esc(json.dumps(r['recipe'], ensure_ascii=False, indent=2))}</pre></details></article>")
        parts.append("</div></section>")
    intro = """<h1>Karakter seçim replikleri · Seçki 23</h1>
<p>5 karakter × 10 = 50 sentetik ses/okuma taslağı. Yeni karakter sesleri oyuna eklenmedi.</p>
<p><b>Bunlar gerçek oyuncu kayıtları veya 50 farklı aktör değildir.</b> Ücretsiz yerel Kokoro modeliyle farklı ses bankaları, konuşma hızları, noktalama ve hafif perde düzenlemeleri kullanıldı. Birkaç adayda iki stil vektörü karıştırıldı; iki konuşma kaydı üst üste çalınmıyor.</p>
<p>Etiketler hedeflenen tını/oyunculuk yönüdür; model sertlik, alay, heyecan veya otoriteyi kesin biçimde kontrol edemez. Deneysel aksanlar özellikle etiketlendi; doğal/otantik aksan garantisi yok. Aksanlı adaylarda telaffuzu ayrıca değerlendir. Metinler kullanıcı replikleridir; Koba'daki MOSTER → MONSTER düzeltildi.</p>
<p>Ywaleth Yoster doğru karakter adıdır. Linlos yetişkin halfling olarak ele alındı, gerçek çocuk sesi yok. Ses taklidi veya belirli bir insanın sesi klonlanmadı. Müzik ve savaş efekti eklenmedi.</p>
<p>Her karakter için bir seçim yap. Sonraki hasar/ölüm adayları seçilen ses karakterine göre hazırlanacak; bunlar bu seçkiye eklenmedi.</p>
<p><a href='LICENSE-SOURCES.txt'>Kaynak/lisans</a> · <a href='SECENEKLER.json'>Tam üretim dökümü</a></p>"""
    if PACK_NUMBER != 23:
        intro = f"<h1>Karakter seçim replikleri · Seçki {PACK_NUMBER}</h1>" + EXTRA_INTRO
    page = """<!doctype html><html lang='tr'><meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1'><title>Grisilith · Karakter seçim sesleri</title><style>
body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p{color:#ccc}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}pre{font-size:13px;white-space:pre-wrap}nav{position:sticky;top:0;background:#17191c;padding:12px;z-index:1}label{cursor:pointer}button{padding:12px;background:#e6c482;border:0}textarea{width:100%;background:#25282d;color:#eee;margin-top:12px}blockquote{border-left:3px solid #e6c482;padding:12px}</style><main>"""
    page += intro + "<nav>" + " · ".join(f"<a href='#{g['id']}'>{html.escape(g['name'])}</a>" for g in GROUPS) + "</nav>" + "".join(parts)
    page += """<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' rows='3' readonly></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>"""
    (OUT / "DINLE.html").write_text(page, encoding="utf-8")
    (OUT / "SECENEKLER.json").write_text(json.dumps(results, ensure_ascii=False, indent=2), encoding="utf-8")
    (OUT / "LICENSE-SOURCES.txt").write_text(f"PACK {PACK_NUMBER} — SYNTHETIC CHARACTER VOICE AUDITIONS ONLY — NOT INSTALLED\n" + """
Kokoro-82M by hexgrad — Apache License 2.0 (model/voice weights).
https://huggingface.co/hexgrad/Kokoro-82M
https://huggingface.co/hexgrad/Kokoro-82M/blob/main/VOICES.md
https://www.apache.org/licenses/LICENSE-2.0
ONNX runtime integration: thewh1teagle/kokoro-onnx — MIT.
https://github.com/thewh1teagle/kokoro-onnx
Model/voice conversion release: model-files-v1.1, kokoro-v1.0.onnx / voices-v1.0.bin.
https://github.com/thewh1teagle/kokoro-onnx/releases/tag/model-files-v1.1
Runtime/model dependencies are audition tools, NOT shipped with the game.
Speech is synthetic, not a new recording of any actor, not targeted voice cloning.
Texts supplied by user; names/punctuation and MOSTER -> MONSTER corrected.
Edits: engine speed, optional averaged style vectors, small pitch resampling,
subtle tanh saturation for Koba, mono/44.1kHz PCM16, peak matching -12dBFS,
10ms edge fades. Not perceived loudness normalization.
Asian/Italian/Hindi voice-bank English readings are experimental cross-language
renderings, not a claim of authentic regional accents. No stereotype performance
or imitation of a specific person is intended. Linlos is an adult, no child recording.
Full settings / input text / phonemes / hashes: SECENEKLER.json and per-clip JSON.
No gameplay, character-selection UI, hurt/death audio or installed asset changed
by this preview generator. If selected, preserve relevant model/runtime notices.
""" + EXTRA_LICENSE, encoding="utf-8")
    for notice in ("MODEL-LICENSE.txt", "RUNTIME-LICENSE.txt", "VOICES.md"):
        if (MODEL / notice).is_file():
            (OUT / notice).write_bytes((MODEL / notice).read_bytes())
    assert len(list(OUT.glob("*.wav"))) == expected + len(GROUPS) * 2
    for row in results:
        candidate = OUT / f"{row['id']}.wav"
        assert hashlib.sha256(candidate.read_bytes()).hexdigest() == row["sha256"]
        info = sf.info(candidate)
        assert info.channels == 1 and info.samplerate == RATE and info.subtype == "PCM_16"
        data, _ = sf.read(candidate)
        assert np.isfinite(data).all() and .005 < np.sqrt(np.mean(data * data))
        assert np.max(np.abs(data)) < .251
        assert candidate.name in page
    print(f"CHARACTER_VOICES_PASS: {expected} unique non-silent synthetic candidates + {len(GROUPS)*2} comparisons; preview-only", flush=True)

if __name__ == "__main__":
    main()
