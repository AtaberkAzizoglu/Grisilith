package qa

import java.io.File
import java.security.MessageDigest
import kotlin.math.*
import qa.PrepareAudioAudition.Candidate

/** Shared offline audition page writer; no game assets or preferences touched. */
object AuditionPage {
    data class Group(val id:String,val title:String,val note:String,val choices:List<Candidate>)
    data class Reference(val label:String,val file:File)
    fun write(number:Int,title:String,intro:String,groups:List<Group>,credits:File,
        references:Map<String,List<Reference>> = emptyMap(),
        transform:(Group,Int,FloatArray)->FloatArray={_,_,pcm->pcm}) {
        val rate=44100;val out=File("tools/audio-audition/listening-pack-$number").apply{mkdirs()}
        val body=StringBuilder();val manifest=StringBuilder("$title — PREVIEW ONLY, NOT INSTALLED\n")
        val hashes=mutableSetOf<String>();var count=0
        fun esc(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("'","&#39;")
        fun appendReferences(id:String) {
            references[id].orEmpty().forEachIndexed{i,ref->
                require(id.matches(Regex("[A-Z0-9]+")))
                val name="$id-reference-${i+1}.wav"
                ref.file.copyTo(File(out,name),overwrite=true)
                body.append("<article class='reference'><b>MEVCUT SES · ${esc(ref.label)}</b><p>Karşılaştırma için; yeni aday değil, yeniden seçmen gerekmiyor.</p><audio controls preload='none' src='$name'></audio></article>")
                manifest.append("REFERENCE $name · ${ref.label} · exact copy of ${ref.file.path}\n")
            }
        }
        groups.forEach{g->
            val clips=g.choices.mapIndexed{i,c->
                val rendered=try{PrepareAudioAudition.render(c)}catch(t:Exception){throw IllegalStateException("Candidate ${g.id}${i+1}: ${c.label}",t)}
                val pcm=transform(g,i,rendered)
                check(pcm.size>rate/20 && pcm.all{it.isFinite()})
                val fade=min(882,pcm.size/4);repeat(fade){n->pcm[n]*=n.toFloat()/fade;pcm[pcm.lastIndex-n]*=n.toFloat()/fade}
                val peak=pcm.maxOf{abs(it)};check(peak>.001f)
                pcm.indices.forEach{n->pcm[n]*=.25f/peak};pcm
            }
            val slot=ceil(clips.maxOf{it.size}.toDouble()/rate+.8).toInt()
            val combined=FloatArray(rate*(1+slot*clips.size))
            body.append("<section id='${g.id}'><h2>${g.id} · ${esc(g.title)}</h2><p>${esc(g.note)}</p>")
            appendReferences(g.id)
            body.append("<p>YENİ ADAYLAR · Toplu dinleme: 1 → ${clips.size}, $slot saniyelik aralıklarla.</p><audio controls preload='none' src='${g.id}-all.wav'></audio><div class='grid'>")
            g.choices.forEachIndexed{i,c->
                val id="${g.id}${i+1}";val file=File(out,"$id.wav")
                PrepareAudioAudition.wav(file,clips[i]);clips[i].copyInto(combined,rate*(1+i*slot))
                val hash=MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString(""){"%02x".format(it)}
                check(hashes.add(hash)){"Duplicate candidate $id"};count++
                val recipe=c.layers.joinToString(" + "){"${it.pack}/${it.file}: speed=${it.speed}, offset=${it.offset}s, max=${it.maxSeconds}s, gain=${it.gain}, lowPass=${it.lowPass}, highPass=${it.highPass}, take=${it.transient}"}
                val duration="%.2f".format(java.util.Locale.US,clips[i].size.toDouble()/rate)
                manifest.append("$id · ${c.label} · ${duration}s\n$recipe\n")
                body.append("<article><label><input type='radio' name='${g.id}' value='$id'> <b>$id</b> · ${esc(c.label)}</label><p>${duration}s</p><audio controls preload='none' src='$id.wav'></audio><details><summary>Kaynak / düzenleme</summary><small>${esc(recipe)}</small></details></article>")
            }
            PrepareAudioAudition.wav(File(out,"${g.id}-all.wav"),combined);body.append("</div></section>")
        }
        references.keys.filter{key->groups.none{it.id==key}}.forEach{key->
            body.append("<section id='$key'><h2>$key · Tamamlanmış mevcut sesler</h2>")
            appendReferences(key);body.append("</section>")
        }
        credits.copyTo(File(out,"LICENSE-SOURCES.txt"),overwrite=true)
        File(out,"SECENEKLER.txt").writeText(manifest.toString(),Charsets.UTF_8)
        File(out,"DINLE.html").writeText("""<!doctype html><html lang="tr"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Grisilith · ${esc(title)}</title>
<style>body{background:#17191c;color:#eee;font:17px/1.5 system-ui;margin:0}main{max-width:1150px;margin:auto;padding:28px}h2,a{color:#e6c482}p,small{color:#bbb}section{border-top:1px solid #555;margin-top:30px;padding-top:15px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{background:#25282d;padding:16px;border-radius:7px}audio{width:100%;margin:12px 0}small{display:block;overflow-wrap:anywhere}button{padding:12px;background:#e6c482;border:0;cursor:pointer}textarea{display:block;width:100%;background:#25282d;color:#eee;margin-top:12px}nav{position:sticky;top:0;background:#17191c;padding:12px;z-index:1}label{cursor:pointer}</style>
<main><h1>${esc(title)} · Seçki $number</h1><p>$count yeni aday; gruplarda 5 veya 10 seçenek. Yeni adaylar henüz oyuna eklenmedi.${if(references.isEmpty())"" else " Ayrıca ${references.values.sumOf{it.size}} mevcut ses karşılaştırma amacıyla eklendi; bunlar yeniden seçilmeyecek."}</p><p>$intro</p>
<p>Sesler ücretsiz kaynaklardan tasarımlar; bazıları kesit/perde/miks varyasyonlarıdır, beş bağımsız aktör kaydı iddiası değildir. Önce düşük sesle dinle. Peak −12dBFS, algısal loudness eşleme değil.</p>
<p><a href='LICENSE-SOURCES.txt'>Lisanslar / işlemler</a> · <a href='SECENEKLER.txt'>Aday dökümü</a></p><nav>${(groups.map{it.id}+references.keys.filter{key->groups.none{it.id==key}}).joinToString(" · "){"<a href='#$it'>$it</a>"}}</nav>$body
<section><h2>Seçimlerin</h2><button id='summary'>Seçim özetini oluştur</button><textarea id='result' readonly rows='4'></textarea></section></main><script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})));document.getElementById('summary').onclick=()=>document.getElementById('result').value=Array.from(document.querySelectorAll('input:checked')).map(e=>e.value).join(', ');</script></html>""",Charsets.UTF_8)
        check(count==groups.sumOf{it.choices.size} && out.listFiles()!!.count{it.extension=="wav"}==count+groups.size+references.values.sumOf{it.size})
        println("AUDITION_${number}_PASS: $count unique non-silent candidates + ${groups.size} comparisons; header/format/peak checked")
    }
}
