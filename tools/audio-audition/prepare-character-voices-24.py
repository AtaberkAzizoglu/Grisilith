"""Second Imbert/Koba auditions; never installs or modifies selected game audio."""
import importlib.util
from pathlib import Path
from fractions import Fraction
import numpy as np
from scipy.signal import butter, sosfilt, resample_poly, hilbert

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("character_auditions", ROOT / "prepare-character-voices.py")
base = importlib.util.module_from_spec(spec)
spec.loader.exec_module(base)
base.PACK_NUMBER = 24
base.OUT = ROOT / "listening-pack-24"
base.EXTRA_INTRO = """
<p><b>Onaylanan, henüz oyuna eklenmeyenler: Lavena LV3 · Ywaleth YW10 · Linlos LD1.</b></p>
<p>Sir Imbert ve Koba için 10'ar yeni ses taslağı. Kodlar IMB1–10 ve ORC1–10;
önceki seçkinin IM/KB kodlarıyla karışmaz. Replikler aynı, Koba'nın MONSTER yazımı düzeltilmiştir.</p>
<p>Koba: daha derin, gırtlaktan, canavarımsı ork tasarımları. Imbert: 50'lerinde, tok,
asker sertliğinde bir şövalye hedefi; IMB8–10 daha saldırgan/bağırış hedefli işlemedir.</p>
<p><b>Sınırlama:</b> Bunlar Kokoro sentetik ses taslaklarıdır, gerçek aktör kayıtları değildir.
Kesin yaş ve bağırarak oyunculuk motor tarafından kontrol edilemiyor. IMB8–10 gerçek
bir aktörün kükreyerek bağırışı değil; daha sert metin vurgusu, kompresyon ve pürüzle
yaklaşılmış örneklerdir. Koba'da perde indirme, EQ, kontrollü distorsiyon ve bazı
adaylarda düşük seviyeli subharmonik bas kullanıldı. Sadece sesi açmakla yetinilmedi;
tüm adayların tepe seviyeleri eşittir. Etiketler hedeflenen tınıdır, kalite onayı değildir.</p>
<p>Henüz hiçbir karakter sesi oyuna eklenmedi. Hasar alma/ölüm adayları bu seçimlerden sonra.</p>
<p><a href='../listening-pack-23/DINLE.html'>Önceki seçki ve seçtiğin üç ses</a> ·
<a href='LICENSE-SOURCES.txt'>Kaynak/lisans ve işlemler</a> · <a href='SECENEKLER.json'>Üretim dökümü</a></p>
"""
base.EXTRA_LICENSE = """
PACK 24 OVERRIDE: preceding pack23 default edits are historical where different.
Imbert: adult male voice banks, lower pitch, restrained bass/presence EQ, compression,
light saturation; 8-10 alternate emphatic punctuation/case, more aggressive saturation.
Koba: lower pitch (-4 to -6.8 semitones), high/low-pass, bass/body and presence EQ,
compressed/soft-clipped vocal grit. Some use a quiet subharmonic from the analytic
phase of the SAME voice's bass band, not a second actor, plus controlled 34Hz modulation.
Pitch uses resampling (also affects duration); no claim of independent formant control.
All normalized to -12dBFS peak, 10ms edge fades, mono44.1kHz PCM16.
Labels describe intended timbre; no guaranteed precise age, emotion or real shouting.
All words from user lines; capitalization/punctuation only vary. No voice cloning.
Approved LV3/YW10/LD1 are referenced, never rewritten or installed by this pack.
"""

def option(voice, label, pitch, *, speed=.96, lang="en-us", grit=1.5,
           bass=.22, presence=.1, shadow=0., rumble=0., text=None):
    result = base.profile(voice, label, speed, pitch, lang, .34, True)
    result.update(grit=grit, bass=bass, presence=presence, shadow=shadow, rumble=rumble,
                  processing_version="orc-veteran-v1")
    if text:
        result["text"] = text
    return result

IMB = "It is my oath! I will fight until the magic is gone from this realm!"
ORC = "On behalf of all the outcasts, on behalf of all those who have been called monsters... I will show them the REAL MONSTER!"
base.GROUPS = [
    dict(id="IMB", name="Sir Imbert", text=IMB,
         direction="50'lerinde bir insan şövalye hedefi; genç değil, kalın, sert ve otoriter. Son üçü bağırış hedefli işlenmiş taslaklardır.", choices=[
        option("bm_george", "Tecrübeli Britanyalı komutan · göğüslü, katı", -2.2, lang="en-gb"),
        option("am_michael", "Kalın asker tonu · ağır ve doğrudan", -2.8, grit=1.7),
        option("am_fenrir", "Çatallı savaş gazisi · keskin otorite", -2.5, grit=2., presence=.18),
        option("bm_fable", "Yaş almış şövalye hedefi · tok Britanya tınısı", -2.8, lang="en-gb", speed=.93),
        option("am_onyx", "En pes komutan · yavaş, tehditkâr", -3.1, speed=.94, bass=.32),
        option({"bm_george":.65,"am_michael":.35}, "Olgun komutan · ölçülü sertlik", -2.4, lang="en-gb", grit=1.8),
        option("am_eric", "Kuru ve pürüzlü asker · kararlı yemin", -2.4, grit=2., presence=.2),
        option("am_fenrir", "Bağırış hedefi · sert yemin / işlenmiş", -2.1, speed=1.03, grit=3.2, presence=.3,
               text="It is my OATH! I WILL FIGHT! Until the magic is gone from this realm!"),
        option("bm_george", "Bağırış hedefi · savaş emri / işlenmiş", -2.6, speed=1.02, lang="en-gb", grit=3.4, presence=.27,
               text="It is my OATH! I will FIGHT until the MAGIC is gone from this REALM!"),
        option("am_michael", "Bağırış hedefi · hiddetli komutan / işlenmiş", -2.5, speed=1.04, grit=3.6, bass=.3, presence=.25,
               text="IT IS MY OATH! I will fight until the magic is gone from this realm!"),
    ]),
    dict(id="ORC", name="Koba", text=ORC,
         direction="Half-orc barbarian; insan basından daha canavarımsı, korkutucu, kalın ve gırtlaktan. Son seçeneklerde daha güçlü yaratık işlemesi var.", choices=[
        option("am_fenrir", "Kaba ork savaşçısı · derin gırtlak", -4.4, speed=.94, grit=2.7, bass=.35, presence=.2),
        option("am_onyx", "Mağara bası · buyurgan ork", -5.8, speed=.97, grit=2.4, bass=.4, shadow=.08),
        option("am_michael", "Hırıltılı barbar · belirgin kelimeler", -4.7, speed=.98, grit=3.1, bass=.32, presence=.28),
        option("bm_george", "Yaşlı ork şefi · karanlık, ağır", -5.2, lang="en-gb", grit=2.7, bass=.4, shadow=.1),
        option("am_adam", "Pürüzlü canavar · sert ve pes", -4.8, grit=3.4, bass=.25, presence=.25, rumble=.08),
        option({"am_fenrir":.6,"am_onyx":.4}, "Ork savaş ağası · koyu, çatallı", -5.5, grit=3., bass=.35, shadow=.13),
        option("hm_omega", "Deneysel aksan · kalın barbar", -4.5, speed=.94, grit=2.8, bass=.3, presence=.2, shadow=.08),
        option("bm_fable", "Boğuk ork reisi · tehditkâr", -5.8, lang="en-gb", grit=3.5, bass=.4, shadow=.16, rumble=.1),
        option("am_fenrir", "Daha canavarımsı · pes alt katman ve hırıltı", -6.4, speed=1., grit=3.6, bass=.3, presence=.3, shadow=.22, rumble=.14),
        option("am_onyx", "En karanlık ork · ağır yaratık işlemesi", -6.8, speed=1.02, grit=3.8, bass=.4, presence=.25, shadow=.25, rumble=.18),
    ]),
]

def lowpass(x, hz, rate):
    return sosfilt(butter(2, hz, fs=rate, output="sos"), x)

def process(samples, rate, choice):
    ratio = Fraction(2 ** (-choice["pitch"] / 12)).limit_denominator(500)
    x = resample_poly(samples, ratio.numerator, ratio.denominator)
    x = sosfilt(butter(2, 55, btype="highpass", fs=rate, output="sos"), x)
    x = lowpass(x, 7200, rate)
    x += choice["bass"] * lowpass(x, 420, rate)
    presence = sosfilt(butter(2, [1100, 3200], btype="bandpass", fs=rate, output="sos"), x)
    x += choice["presence"] * presence
    scale = max(float(np.max(np.abs(x))), 1e-6)
    x /= scale
    # Soft compression retains intelligibility and produces vocal rasp, not hard clipping.
    x = np.tanh(x * choice["grit"]) / np.tanh(choice["grit"])
    if choice["shadow"]:
        bass_band = sosfilt(butter(2, [65, 240], btype="bandpass", fs=rate, output="sos"), x)
        analytic = hilbert(bass_band)
        shadow = np.sin(np.unwrap(np.angle(analytic)) * .5) * np.abs(analytic)
        x += choice["shadow"] * lowpass(shadow, 250, rate)
    if choice["rumble"]:
        x *= 1 + choice["rumble"] * np.sin(2*np.pi*34*np.arange(len(x))/rate)
    return x

base.process_voice = process
if __name__ == "__main__":
    base.main()
