package qa

import java.io.File
import kotlin.math.ceil

/** Only candidate previews, not the selected common popup cue. */
object PrepareNewSpellAudition {
    @JvmStatic fun main(args:Array<String>) {
        val out=File("tools/audio-audition/listening-pack-15").apply{mkdirs()}
        val html=StringBuilder();val notes=StringBuilder()
        val clips=(1..16).map{n->
            PrepareAudioAudition.render(PrepareAudioAudition.Candidate("NS$n",listOf(
                PrepareAudioAudition.Layer("new-spell","NS$n.wav",maxSeconds=6.0))))
        }
        for(group in 0..3){
            val slot=ceil(clips.subList(group*4,group*4+4).maxOf{it.size}.toDouble()/44100+1).toInt()
            val combined=FloatArray(44100*(1+slot*4))
            html.append("<section><h2>NS${group*4+1}–NS${group*4+4}</h2><p>Toplu dinleme: sırayla dört ses, $slot saniyelik aralıklar.</p><audio controls preload='none' src='group-${group+1}.wav'></audio><div class='grid'>")
            repeat(4){i->val n=group*4+i+1;val text=if(n<=8)"A new spell." else "New spell learned."
                val pcm=clips[n-1];PrepareAudioAudition.wav(File(out,"NS$n.wav"),pcm)
                pcm.copyInto(combined,44100*(1+i*slot))
                notes.append("NS$n: $text — libritts_r medium speaker_id ${n-1}; generated, not an actor recording.\n")
                html.append("<article><label><input type='radio' name='newspell' value='NS$n'> <b>NS$n</b> · Ses $n</label><p>$text</p><audio controls preload='none' src='NS$n.wav'></audio></article>")
            }
            PrepareAudioAudition.wav(File(out,"group-${group+1}.wav"),combined);html.append("</div></section>")
        }
        listOf("singularity_end","singularity_explosion").forEach{File("assets/audio/sfx/$it.wav").copyTo(File(out,"$it.wav"),overwrite=true)}
        File(out,"SECENEKLER.txt").writeText(notes.toString(),Charsets.UTF_8)
        File(out,"LICENSE-SOURCES.txt").writeText("""NEW SPELL CHOICE — 16 CANDIDATES, NOT INSTALLED
Synthetic speech generated locally with Piper (rhasspy), en_US-libritts_r-medium, speaker IDs 0–15.
No claim of 16 newly recorded human actors. No target celebrity/person impersonation requested.
Model: https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_US/libritts_r/medium
Model card: https://huggingface.co/rhasspy/piper-voices/blob/main/en/en_US/libritts_r/medium/MODEL_CARD
Dataset: LibriTTS-R, https://www.openslr.org/141/ — CC BY 4.0 per upstream model card.
https://creativecommons.org/licenses/by/4.0/
Upstream tool (MIT release used): https://github.com/rhasspy/piper/releases/tag/2023.11.14-2
Binary/model are audition-only tools; not packaged with the game. If selected, preserve model/dataset attribution.
Phrases: IDs 0–7 'A new spell.'; IDs 8–15 'New spell learned.'; default model rate/prosody.
Edits: mono/44.1kHz/16-bit WAV, peak matching -12dBFS (not perceived loudness), end fade.
Singularity previews are installed phase sounds derived from previously approved SG3 and MT4.
Their full sources/CC BY 3.0 and CC0 licenses: assets/audio/FOCUS-ABILITY-LICENSES.txt.
""",Charsets.UTF_8)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · Yeni büyü seçimi 15</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(230px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}button{padding:12px;background:#e6c482;border:0}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}</style>
<main><h1>Chalk Dust · Ortak seçim sesi</h1><p>16 farklı sesle “yeni büyü” duyurusu. <b>Sentetik seslendirme</b>; gerçek seslendirme sanatçısı kayıtları değil. Anlaşılır İngilizce cümleler, tek ses; müzik veya başka ses katmanı yok.</p>
<p>NS1–8: “A new spell.” (Yeni bir büyü.) · NS9–16: “New spell learned.” (Yeni büyü öğrenildi.) Bunlardan seçtiğin <b>bir ses tüm Chalk Dust seçimlerinde ortak</b> kullanılacak. Bu 16 aday henüz oyuna eklenmedi; önce sen seçeceksin.</p>
<p>Her bölümde dört adayı topluca veya tek tek dinleyebilirsin. Kaynaklar ücretsiz ve atıf koşullu: <a href='LICENSE-SOURCES.txt'>lisanslar ve kaynak bilgileri</a>.</p>
$html
<section><h2>Yeni Singularity bitişi ve patlaması · oyuna eklendi</h2><p>Onayladığın SG3'ten 1.2 saniyelik çöküş, MT4'ten 0.9 saniyelik final patlaması. Bunlar yeni seçim adayı değil, bu tur eklenen iki efektin önizlemesi. İstersen sonra değiştirebiliriz. Burada ham dosya düzeyi duyulur; oyunda Singularity miks kazancı daha yüksektir.</p>
<p>Çöküş / bitiş</p><audio controls preload='none' src='singularity_end.wav'></audio><p>Final patlaması</p><audio controls preload='none' src='singularity_explosion.wav'></audio></section>
<section><button id='summary'>Seçim özetini oluştur</button><textarea id='result' rows='2' readonly></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=document.querySelector('input:checked')?.value||'';</script></html>""",Charsets.UTF_8)
        check(out.listFiles()!!.count{it.extension=="wav"}==22)
        println("NEW_SPELL_AUDITION_PASS: 16 synthetic spoken choices + 4 comparisons, 2 installed Singularity phase previews.")
    }
}
