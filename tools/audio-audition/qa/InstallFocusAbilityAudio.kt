package qa

import java.io.File
import kotlin.math.*

/** Reproducible offline installation of approved auditions only. Never changes balance/visuals. */
object InstallFocusAbilityAudio {
    private const val RATE=44100
    private val root=File("tools/audio-audition")
    private val out=File("assets/audio/sfx")
    private fun read(id:String)=InstallMagicNormalAudio.read(File(root,"listening-pack-$id.wav"))
    private fun save(name:String,pcm:FloatArray)=PrepareAudioAudition.wav(File(out,"$name.wav"),pcm)
    private fun loop(raw:FloatArray):FloatArray {
        val overlap=min(RATE/12,raw.size/4);val result=raw.copyOf(raw.size-overlap)
        repeat(overlap){i->val t=i.toFloat()/overlap;result[i]=raw[result.size+i]*(1-t)+raw[i]*t}
        repeat(64){i->val k=result.size-64+i;val t=i/63f;result[k]=result[k]*(1-t)+result[0]*t}
        return result
    }
    @JvmStatic fun main(args:Array<String>) {
        out.mkdirs()
        // These stems already have seamless wrap edits from the audition preparation.
        mapOf("focus_vortex" to "11/VP4-loop","focus_clouds" to "11/SC3-loop",
            "focus_acid" to "11/AP2-loop","focus_plague" to "11/PS4-loop",
            "focus_burning" to "11/BL5-loop","focus_animal" to "12/AN4",
            "focus_demon" to "12/DE1","focus_oathbreaker" to "12/OA1").forEach{(name,id)->
            File(root,"listening-pack-$id.wav").copyTo(File(out,"$name.wav"),overwrite=true)
        }
        listOf(Triple("focus_ignite","11/IG1",780),Triple("focus_erasing","11/ER3",650),
            Triple("focus_acid_explosion","12/PX2",780),Triple("glyph_lightning","12/CL2",860),
            Triple("glyph_meteor","12/MT4",860)).forEach{(name,id,ms)->save(name,InstallMagicNormalAudio.fit(read(id),ms))}
        save("glyph_tsunami",loop(read("12/TS3")))
        save("glyph_singularity",loop(read("12/SG3")))
        save("singularity_end",InstallMagicNormalAudio.fit(read("12/SG3"),1200))
        save("singularity_explosion",InstallMagicNormalAudio.fit(read("12/MT4"),900))
        // Retain the start of long voice recordings, cut at cast end in runtime. Short HM3 is
        // gently resampled to 2.5 seconds so it cannot finish before the drawing animation.
        mapOf("cast_lightning" to "13/HL2","cast_meteor" to "13/HM3",
            "cast_tsunami" to "14/SV2","cast_singularity" to "14/SV3").forEach{(name,id)->
            val raw=read(id)
            val pcm=if(raw.size>=RATE*2.5)raw else {
                FloatArray((RATE*2.5).toInt()){i->val p=i.toDouble()*(raw.size-1)/(RATE*2.5-1)
                    val k=p.toInt();val f=(p-k).toFloat();raw[k]*(1-f)+raw[min(k+1,raw.lastIndex)]*f}
            }
            save(name,pcm)
        }
        val names=listOf("focus_ignite","focus_erasing","focus_vortex","focus_clouds","focus_acid","focus_plague","focus_burning","focus_acid_explosion","focus_animal","focus_demon","focus_oathbreaker","glyph_lightning","glyph_meteor","glyph_tsunami","glyph_singularity","cast_lightning","cast_meteor","cast_tsunami","cast_singularity","singularity_end","singularity_explosion")
        names.forEach{val pcm=InstallMagicNormalAudio.read(File(out,"$it.wav"));check(pcm.any{abs(it)>.001f});println("$it: ${pcm.size*1000L/RATE}ms")}
        File("assets/audio/FOCUS-ABILITY-LICENSES.txt").writeText("""SPELL FOCUS ABILITY AUDIO — APPROVED SELECTIONS
IG1, ER3, VP4, SC3, AP2, PS4, BL5; PX2, AN4, DE1, OA1, CL2, TS3, MT4, SG3.
Casting: HL2 Call Lightning, HM3 Meteor (HS3 was a user typo), SV2 Tsunami, SV3 Singularity.

IG1: Ogrebane Teleport Spell (CC0) + artisticdude Fire & Evil Spell (CC BY 3.0).
https://opengameart.org/content/teleport-spell
https://opengameart.org/content/fire-evil-spell
ER3: qubodup, 3 dark magic spells + Kenney Impact Sounds, CC0.
VP4: SketchMan3 wind whoosh loop, CC0.
PS4: SketchMan3 wind whoosh loop + qubodup 3 dark magic spells, CC0.
https://opengameart.org/content/wind-whoosh-loop
https://opengameart.org/content/3-dark-magic-spells
https://kenney.nl/assets/impact-sounds
SC3 / CL2: Jerimee Thunder, CC BY 3.0. CL2 also includes Michel Baradari explosion, CC BY 3.0.
https://opengameart.org/content/thunder
https://opengameart.org/content/2-high-quality-explosions
AP2: spookymodem Bubbling Acid (CC BY 3.0) + Vehicle / Jan Schupke Tinysized (CC0).
https://opengameart.org/content/bubbling-acid
https://opengameart.org/content/fantasy-sound-effects-tinysized-sfx
BL5: PagDev Fireplace Sound loop + TinyWorlds boiling water loops + Vehicle Tinysized, CC0.
https://opengameart.org/content/fireplace-sound-loop
https://opengameart.org/content/boiling-water-loops
PX2: Michel Baradari explosion (CC BY 3.0) + PagDev fireplace (CC0).
DE1: spookymodem Goblin Scream, CC BY 3.0.
https://opengameart.org/content/goblin-scream
AN4 / focus_animal.wav: Secretlondon (submitted by qubodup), Farm animals, CC BY-SA 3.0.
https://opengameart.org/content/farm-animals
OA1 / focus_oathbreaker.wav: pal.zoltan.illes Male encounter voice pieces, CC BY-SA 3.0.
https://opengameart.org/content/male-encounter-voice-pieces
These two derivative AUDIO ASSETS remain CC BY-SA 3.0; no claim that the game code is under this license.
https://creativecommons.org/licenses/by-sa/3.0/
TS3: transitking Water Waves (CC0) + Jerimee Thunder (CC BY 3.0) + Almitory Air Woosh Move (CC0).
MT4 / SG3: Almitory Air Woosh Move (CC0) + Michel Baradari explosion and/or Jerimee Thunder (CC BY 3.0).
https://opengameart.org/content/water-waves
https://opengameart.org/content/air-woosh-move
HL2 / HM3: frosthardr, voice ancient arcane chant incantation, CC0. HQ MP3 preview-derived excerpts.
https://freesound.org/people/frosthardr/sounds/553453/
SV2 / SV3: Bananaboatman33, evilwitch_Master.wav, CC0. Source is already processed/reversed female demon gibberish.
https://freesound.org/people/Bananaboatman33/sounds/315655/

Other license links:
https://creativecommons.org/licenses/by/3.0/
https://creativecommons.org/publicdomain/zero/1.0/
Edits: source excerpts, mono/44.1kHz/16-bit, pitch/speed/filters/reversal/layers as auditioned,
peak matching (not perceived loudness matching), wrap crossfades on loop textures, attack fitting/fades.
Burst cues fit 0.65 / 0.78 / 0.86 second actual visual lifetimes. HM3 stretched from ~1.94 to 2.5s;
Singularity end/collapse: SG3-derived 1.20s; final explosion: MT4-derived 0.90s, same licensed sources above.
longer casting voices retain their starts and are stopped at the runtime's 2.5s drawing completion/cancellation.
All combat sounds follow game playback speed and SFX/master volume. No paid sounds.
""",Charsets.UTF_8)
        println("FOCUS_ABILITY_INSTALL_PASS: 19 approved assets + 2 requested Singularity phase derivatives; fitted bursts and owner-bound loops/casting.")
    }
}
