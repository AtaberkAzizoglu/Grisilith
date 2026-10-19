package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.settings.GameSettings
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable

enum class UiSound(val path:String, val durationMs:Long, val priority:Int, val gain:Float=1f,val combat:Boolean=false) {
    CLICK("ui_click.wav",446,10,.8f),
    CONFIRM("ui_confirm.wav",262,60),
    BACK("ui_back.wav",92,40,.9f),
    ERROR("ui_error.wav",166,100),
    PAGE("page_turn.wav",769,70,.8f),
    CHEST("chest_open.wav",811,90),
    LONGSWORD_SWING("longsword_swing.wav",250,5,.65f,true),
    DOUBLE_SWORDS_SWING("double_swords_swing.wav",240,5,.65f,true),
    DOUBLE_SWORDS_UPGRADED("double_swords_upgraded.wav",240,5,.65f,true),
    LANCE_THRUST("lance_thrust.wav",240,5,.65f,true),
    SHIELD_STRIKE("shield_strike.wav",270,5,.5f,true),
    WHIP_LASH("whip_lash.wav",240,5,.5f,true),
    WARHAMMER_IMPACT("warhammer_impact.wav",240,5,.65f,true),
    BOW_SHOT("bow_shot.wav",125,5,.65f,true),
    CROSSBOW_SHOT("crossbow_shot.wav",145,5,.65f,true),
    DAGGER_THROW("dagger_throw.wav",57,5,.65f,true),
    SLING_THROW("sling_throw.wav",307,5,.65f,true),
    BOOMERANG_THROW("boomerang_throw.wav",472,5,.65f,true),
    BOW_VOLLEY("bow_volley.wav",265,6,.65f,true),
    SLING_IMPACT("sling_impact.wav",359,6,.65f,true),
    SPIN_ATTACK("spin_attack.wav",420,6,.65f,true),
    AIR_SLASH("air_slash.wav",673,6,.65f,true),
    AIR_SLASH_UPGRADED("air_slash_upgraded.wav",865,6,.65f,true),
    DEFENSIVE_BURST("defensive_burst.wav",550,6,.65f,true),
    EARTHBREAKER_WAVE("earthbreaker_wave.wav",350,6,.65f,true),
    WHIP_SWEEP("whip_sweep.wav",350,6,.5f,true),
    MAGIC_AIR("magic_air.wav",380,5,.6f,true),
    MAGIC_WATER("magic_water.wav",650,5,.6f,true),
    MAGIC_FIRE("magic_fire.wav",650,5,.6f,true),
    MAGIC_ACID("magic_acid.wav",320,5,.6f,true),
    MAGIC_NECROTIC("magic_necrotic.wav",650,5,.6f,true),
    MAGIC_LIGHTNING("magic_lightning.wav",400,5,.6f,true),
    MAGIC_POISON("magic_poison.wav",400,5,.6f,true),
    IGNITE_DASH("focus_ignite.wav",780,7,.65f,true),
    ERASING_SOUL("focus_erasing.wav",650,7,.65f,true),
    VORTEX("focus_vortex.wav",2316,7,.32f,true),
    STATIC_CLOUDS("focus_clouds.wav",1916,7,.35f,true),
    ACIDIC_POOL("focus_acid.wav",2103,7,.35f,true),
    PLAGUE_STORM("focus_plague.wav",2316,7,.32f,true),
    BURNING_POOL("focus_burning.wav",2316,7,.3f,true),
    ACID_EXPLOSION("focus_acid_explosion.wav",780,7,.65f,true),
    SUMMON_ANIMAL("focus_animal.wav",1800,7,.6f,true),
    SUMMON_DEMON("focus_demon.wav",1500,7,.6f,true),
    SUMMON_OATHBREAKER("focus_oathbreaker.wav",2700,7,.6f,true),
    GLYPH_LIGHTNING("glyph_lightning.wav",860,7,.65f,true),
    GLYPH_METEOR("glyph_meteor.wav",860,7,.65f,true),
    GLYPH_TSUNAMI("glyph_tsunami.wav",2716,7,.38f,true),
    GLYPH_SINGULARITY("glyph_singularity.wav",2216,7,.85f,true),
    SINGULARITY_END("singularity_end.wav",1200,7,.85f,true),
    SINGULARITY_EXPLOSION("singularity_explosion.wav",900,7,.85f,true),
    CAST_LIGHTNING("cast_lightning.wav",3140,8,.65f,true),
    CAST_METEOR("cast_meteor.wav",2500,8,.65f,true),
    CAST_TSUNAMI("cast_tsunami.wav",6400,8,.65f,true),
    CAST_SINGULARITY("cast_singularity.wav",7110,8,.65f,true),
    ANIMAL_CHOICE("focus_animal.wav",1800,70,.6f),
    DEMON_CHOICE("focus_demon.wav",996,70,.6f),
    OATHBREAKER_CHOICE("focus_oathbreaker.wav",2990,70,.6f)
}

/** World-space footprint; UI/audio previews intentionally have no world source. */
data class AudioSource(val x:Float,val y:Float,val radius:Float=0f)
data class AudioView(val left:Float,val bottom:Float,val width:Float,val height:Float) {
    fun contains(s:AudioSource):Boolean {
        val dx=s.x-s.x.coerceIn(left,left+width);val dy=s.y-s.y.coerceIn(bottom,bottom+height)
        return dx*dx+dy*dy<=s.radius.coerceAtLeast(0f).let{it*it}
    }
}

data class AudioLevels(val master:Float,val music:Float,val effects:Float) {
    val musicGain get() = (master*music).coerceIn(0f,1f)
    val effectsGain get() = (master*effects).coerceIn(0f,1f)
    companion object {
        fun saved()=AudioLevels(GameSettings.masterVolume,GameSettings.musicVolume,GameSettings.soundEffectsVolume)
    }
}

/** One game-owned mixer. Menus never own/restart the shared streamed music. */
class AudioController(
    private val loadMusic:()->Music,
    private val loadSound:(UiSound)->Sound,
    private val levels:()->AudioLevels=AudioLevels::saved,
    private val millis:()->Long={System.nanoTime()/1_000_000L},
    private val seekSound:((Sound,Long,Float)->Boolean)?=null
):Disposable {
    private data class Event(val kind:UiSound,val source:AudioSource?,val ability:Boolean)
    private data class Voice(val kind:UiSound,val sound:Sound,val id:Long,var remaining:Double,val source:AudioSource?,val ability:Boolean)
    private val sounds=mutableMapOf<UiSound,Sound>()
    private val voices=mutableListOf<Voice>()
    private data class Channel(val kind:UiSound,val looping:Boolean,var source:AudioSource?,var sound:Sound?=null,var id:Long=-1L,var elapsedSeconds:Double=0.0)
    private val channels=linkedMapOf<Any,Channel>()
    private var view:AudioView?=null
    private var playerSource:AudioSource?=null
    fun worldView(value:AudioView,player:AudioSource) {advanceVoices(millis());view=value;playerSource=player;syncVolumesAndMusic()}
    private fun visible(source:AudioSource?)=source==null || view?.contains(source)!=false
    private fun cueGain(kind:UiSound,ability:Boolean=false)=kind.gain*if(kind.combat&&(ability||kind.priority in 6..8))1.35f else 1f

    /** Owner-bound loops are separate from disposable attack transients, so attacks cannot cut a chant. */
    fun sustain(owner:Any,kind:UiSound?,looping:Boolean=true,source:AudioSource?=null) {
        if(disposed)return
        advanceVoices(millis())
        if(kind==null || channels[owner]?.kind!=kind) stopSustain(owner)
        if(kind!=null && !menu) { require(kind.combat);channels.getOrPut(owner){Channel(kind,looping,source?:playerSource)}.also{if(source!=null)it.source=source} }
        syncChannels()
    }
    fun stopSustain(owner:Any) {channels.remove(owner)?.let{if(it.id>=0)it.sound?.stop(it.id)}}
    private fun syncChannels() {
        val gain=effective().effectsGain
        // Bound simultaneous textures. Multiple copies of the same spell share its mix budget.
        val selected=channels.entries.filter{visible(it.value.source)}.sortedByDescending{it.value.kind.priority}.take(12).map{it.key}.toSet()
        val counts=channels.filterKeys{it in selected}.values.groupingBy{it.kind}.eachCount()
        channels.forEach{(owner,c)->
            val audible=gain>0f && owner in selected
            // Desktop virtualizes hidden sources and seeks to their continuing game-time phase.
            // Backends without seeking keep the source running silently (never pause/restart it).
            if(!audible && seekSound!=null) {if(c.id>=0)c.sound?.stop(c.id);c.id=-1L;return@forEach}
            if(!c.looping && c.elapsedSeconds*1000>=c.kind.durationMs) {if(c.id>=0)c.sound?.stop(c.id);c.id=-1L;return@forEach}
            val volume=if(audible)(gain*cueGain(c.kind)/kotlin.math.sqrt((counts[c.kind]?:1).toFloat())).coerceAtMost(1f) else 0f
            if(c.id<0 && !paused && !combatStopped && c.kind !in failedSounds) {
                val sound=sounds[c.kind]?:runCatching{loadSound(c.kind)}.getOrElse{failedSounds+=c.kind;return@forEach}.also{sounds[c.kind]=it}
                val startVolume=if(seekSound!=null&&c.elapsedSeconds>0.001)0f else volume
                c.sound=sound;c.id=runCatching{if(c.looping)sound.loop(startVolume,combatSpeed,0f) else sound.play(startVolume,combatSpeed,0f)}.getOrDefault(-1L)
                if(c.id>=0 && seekSound!=null && c.elapsedSeconds>0.001) {
                    val duration=c.kind.durationMs/1000.0
                    val offset=if(c.looping&&duration>0)c.elapsedSeconds%duration else c.elapsedSeconds
                    if(!runCatching{seekSound?.invoke(sound,c.id,offset.toFloat())==true}.getOrDefault(false)){
                        sound.stop(c.id);c.id=-1L;return@forEach
                    }
                }
            }
            if(c.id>=0) {c.sound?.setVolume(c.id,volume);c.sound?.setPitch(c.id,combatSpeed)}
        }
    }
    private var music:Music?=null
    private var musicFailed=false
    private var disposed=false
    private var paused=false
    private var menu=false
    private var pending:Event?=null
    private val pendingCombat=linkedMapOf<UiSound,Event>()
    private val lastCombatAt=mutableMapOf<UiSound,Long>()
    private var combatCursor=0
    private var combatSpeed=1f
    private var combatStopped=false
    private var lastVoiceUpdate:Long?=null
    private var lastSoundAt=Long.MIN_VALUE/2
    private var previewOwner:Any?=null
    private var preview:AudioLevels?=null
    private val failedSounds=mutableSetOf<UiSound>()
    val inMenus get()=menu

    private fun advanceVoices(now:Long) {
        val elapsed=(now-(lastVoiceUpdate?:now)).coerceAtLeast(0).toDouble()
        lastVoiceUpdate=now
        voices.forEach { it.remaining-=elapsed*if(it.kind.combat) {
            if(combatStopped)0.0 else combatSpeed.toDouble()
        } else 1.0 }
        voices.filter{it.remaining<=0.0}.forEach{it.sound.stop(it.id)}
        voices.removeAll{it.remaining<=0.0}
        if(!paused&&!combatStopped) channels.values.forEach{it.elapsedSeconds+=elapsed*combatSpeed/1000.0}
    }
    fun combatPlayback(speed:Float,stopped:Boolean) {
        if(disposed)return
        advanceVoices(millis())
        combatSpeed=if(speed.isFinite())speed.coerceIn(.5f,2f) else 1f
        val wasStopped=combatStopped;combatStopped=stopped
        voices.filter{it.kind.combat}.forEach {
            it.sound.setPitch(it.id,combatSpeed)
            if(stopped&&!wasStopped)it.sound.pause(it.id)
            else if(!stopped&&wasStopped)it.sound.resume(it.id)
        }
        if(stopped)pendingCombat.clear()
        channels.values.forEach{c->if(c.id>=0){c.sound?.setPitch(c.id,combatSpeed)
            if(stopped&&!wasStopped)c.sound?.pause(c.id) else if(!stopped&&wasStopped&&!paused)c.sound?.resume(c.id)}}
        syncChannels()
    }

    fun setMenuContext(value:Boolean) {
        if(disposed)return
        if(menu!=value && !value) music?.stop()
        menu=value
        if(value) {
            channels.keys.toList().forEach(::stopSustain)
            pendingCombat.clear()
            voices.filter{it.kind.combat}.forEach{it.sound.stop(it.id)}
            voices.removeAll{it.kind.combat}
        }
        syncVolumesAndMusic()
    }
    fun preview(owner:Any,value:AudioLevels) {
        if(disposed)return
        previewOwner=owner;preview=value;syncVolumesAndMusic()
    }
    fun clearPreview(owner:Any) {
        if(previewOwner===owner) { previewOwner=null;preview=null;syncVolumesAndMusic() }
    }
    /** Coalesce competing UI events in a frame: failures override click/confirm, chest overrides clicks. */
    fun request(kind:UiSound,source:AudioSource?=null,ability:Boolean=false) {
        if(disposed||paused)return
        val event=Event(kind,if(kind.combat)source?:playerSource else null,ability)
        if(kind.combat) {if(!menu&&!combatStopped && (pendingCombat[kind]?.ability!=true || ability))pendingCombat[kind]=event;return}
        if(pending==null || kind.priority>pending!!.kind.priority) pending=event
    }
    fun update() {
        if(disposed)return
        syncVolumesAndMusic()
        val now=millis()
        advanceVoices(now)
        val event=pending;pending=null
        val combatEvents=pendingCombat.values.toList();pendingCombat.clear()
        if(paused)return
        if(event!=null) playEvent(event,now)
        // Two distinct weapon cues per frame, rotated so a fixed update order cannot starve a weapon.
        if(!menu && combatEvents.isNotEmpty()) {
            val start=combatCursor%combatEvents.size
            repeat(minOf(2,combatEvents.size)){playEvent(combatEvents[(start+it)%combatEvents.size],now)}
            combatCursor=(start+minOf(2,combatEvents.size))%combatEvents.size
        }
    }
    private fun playEvent(request:Event,now:Long) {
        val event=request.kind
        val volume=(effective().effectsGain*cueGain(event,request.ability)).coerceAtMost(1f)
        if(!visible(request.source))return
        val previous=if(event.combat)lastCombatAt[event]?:Long.MIN_VALUE/2 else lastSoundAt
        if(volume<=0f || now-previous<(if(event.combat)80L/combatSpeed else 80f))return
        if(event in failedSounds)return
        val sound=sounds[event] ?: runCatching {loadSound(event)}.getOrElse {
            failedSounds+=event;return
        }.also{sounds[event]=it}
        if(voices.size>=3) {
            val oldestLowPriority=voices.minBy{it.kind.priority}
            if(oldestLowPriority.kind.priority>event.priority)return
            voices.remove(oldestLowPriority);oldestLowPriority.sound.stop(oldestLowPriority.id)
        }
        val id=runCatching{sound.play(volume,if(event.combat)combatSpeed else 1f,0f)}.getOrDefault(-1L)
        if(id>=0) {
            voices+=Voice(event,sound,id,event.durationMs.toDouble(),request.source,request.ability)
            if(event.combat)lastCombatAt[event]=now else lastSoundAt=now
        }
    }
    private fun effective()=preview?:levels()
    private fun syncVolumesAndMusic() {
        syncChannels()
        val current=effective()
        voices.forEach { voice ->
            val gain=if(visible(voice.source))(current.effectsGain*cueGain(voice.kind,voice.ability)).coerceAtMost(1f) else 0f
            if(current.effectsGain<=0f)voice.sound.stop(voice.id) else voice.sound.setVolume(voice.id,gain)
        }
        if(current.effectsGain<=0f) voices.clear()
        if(!menu||paused) {if(music?.isPlaying==true)music?.pause();return}
        if(music==null && !musicFailed && current.musicGain>0f) {
            music=runCatching{loadMusic().also{it.isLooping=true}}.getOrElse{musicFailed=true;null}
        }
        music?.let {
            // A restrained starting mix; every value is still multiplied by Settings master/music.
            it.volume=current.musicGain*.45f
            if(current.musicGain<=0f) {if(it.isPlaying)it.pause()}
            else if(!it.isPlaying)it.play()
        }
    }
    fun pause() {
        if(disposed)return
        paused=true;pending=null;pendingCombat.clear();music?.pause()
        voices.forEach{it.sound.stop(it.id)};voices.clear()
        channels.values.forEach{if(it.id>=0)it.sound?.pause(it.id)}
    }
    fun resume() {if(!disposed){paused=false;if(!combatStopped)channels.values.forEach{if(it.id>=0)it.sound?.resume(it.id)};syncVolumesAndMusic()}}
    override fun dispose() {
        if(disposed)return
        disposed=true;pending=null;pendingCombat.clear();lastCombatAt.clear();voices.clear();previewOwner=null;preview=null
        channels.clear()
        sounds.values.forEach{it.dispose()};sounds.clear();music?.dispose();music=null
    }
}

/** Safe no-op before startup/after disposal, so isolated screens and tests need no audio device. */
object GameAudio {
    private var controller:AudioController?=null
    private var seekHandler:((Sound,Long,Float)->Boolean)?=null
    fun installSeekHandler(handler:(Sound,Long,Float)->Boolean) {seekHandler=handler}
    fun initialize() {
        dispose()
        controller=AudioController(
            {Gdx.audio.newMusic(Gdx.files.internal("audio/music/gregorian_chant.mp3"))},
            {Gdx.audio.newSound(Gdx.files.internal("audio/sfx/${it.path}"))},
            seekSound=seekHandler
        )
    }
    fun play(kind:UiSound) {controller?.request(kind)}
    fun playAt(kind:UiSound,x:Float,y:Float,radius:Float=0f) {controller?.request(kind,AudioSource(x,y,radius))}
    fun ability(kind:UiSound,x:Float?=null,y:Float?=null,radius:Float=0f) {controller?.request(kind,if(x!=null&&y!=null)AudioSource(x,y,radius) else null,true)}
    fun sustain(owner:Any,kind:UiSound?,looping:Boolean=true,source:AudioSource?=null) {controller?.sustain(owner,kind,looping,source)}
    fun worldView(left:Float,bottom:Float,width:Float,height:Float,playerX:Float,playerY:Float) {
        controller?.worldView(AudioView(left,bottom,width,height),AudioSource(playerX,playerY))
    }
    fun stopSustain(owner:Any) {controller?.stopSustain(owner)}
    fun summonCue(choice:com.azizoglu.grisilith.weapon.summon.IvySummonChoice)=when(choice) {
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_ANIMALS->UiSound.SUMMON_ANIMAL
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_DEMONS->UiSound.SUMMON_DEMON
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.CALL_THE_KNIGHT->UiSound.SUMMON_OATHBREAKER
    }
    fun summonChoice(choice:com.azizoglu.grisilith.weapon.summon.IvySummonChoice) {play(when(choice){
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_ANIMALS->UiSound.ANIMAL_CHOICE
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.SUMMON_DEMONS->UiSound.DEMON_CHOICE
        com.azizoglu.grisilith.weapon.summon.IvySummonChoice.CALL_THE_KNIGHT->UiSound.OATHBREAKER_CHOICE
    })}
    /** Shared placeholder until the user chooses one of the new spoken NS audition cues. */
    fun glyphChoice() {play(UiSound.CONFIRM)}
    fun castCue(type:com.azizoglu.grisilith.weapon.glyph.GlyphType)=when(type) {
        com.azizoglu.grisilith.weapon.glyph.GlyphType.CALL_LIGHTNING->UiSound.CAST_LIGHTNING
        com.azizoglu.grisilith.weapon.glyph.GlyphType.METEOR->UiSound.CAST_METEOR
        com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI->UiSound.CAST_TSUNAMI
        com.azizoglu.grisilith.weapon.glyph.GlyphType.SINGULARITY->UiSound.CAST_SINGULARITY
    }
    fun magicalShot(type:com.azizoglu.grisilith.content.DamageType) {
        val cue=when(type) {
            com.azizoglu.grisilith.content.DamageType.AIR->UiSound.MAGIC_AIR
            com.azizoglu.grisilith.content.DamageType.WATER->UiSound.MAGIC_WATER
            com.azizoglu.grisilith.content.DamageType.FIRE->UiSound.MAGIC_FIRE
            com.azizoglu.grisilith.content.DamageType.ACID->UiSound.MAGIC_ACID
            com.azizoglu.grisilith.content.DamageType.NECROTIC->UiSound.MAGIC_NECROTIC
            com.azizoglu.grisilith.content.DamageType.LIGHTNING->UiSound.MAGIC_LIGHTNING
            com.azizoglu.grisilith.content.DamageType.POISON->UiSound.MAGIC_POISON
            else->return
        }
        play(cue)
    }
    fun combatPlayback(speed:Float,stopped:Boolean) {controller?.combatPlayback(speed,stopped)}
    fun menuContext(value:Boolean) {controller?.setMenuContext(value)}
    fun inMenus()=controller?.inMenus?:false
    fun preview(owner:Any,master:Float,music:Float,effects:Float) {controller?.preview(owner,AudioLevels(master,music,effects))}
    fun clearPreview(owner:Any) {controller?.clearPreview(owner)}
    fun update() {controller?.update()}
    fun pause() {controller?.pause()}
    fun resume() {controller?.resume()}
    fun dispose() {controller?.dispose();controller=null}

    /** Semantic outcomes may override a button's default click during the same frame. */
    fun button(@Suppress("UNUSED_PARAMETER") text:String):UiSound = UiSound.CLICK
}
