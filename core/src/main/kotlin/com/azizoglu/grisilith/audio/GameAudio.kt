package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.settings.GameSettings
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable

enum class UiSound(val path:String, val durationMs:Long, val priority:Int, val gain:Float=1f,val combat:Boolean=false) {
    ITEM_BROOM("item_broom.wav",662,4,.22f,true),
    ITEM_ROULETTE("item_roulette.wav",507,12,.4f,true),
    ITEM_HEALING_AURA("item_healing_aura.wav",1111,12,.4f,true),
    ITEM_PROTECTERS("item_protecters.wav",1563,12,.4f,true),
    ITEM_TREASURE("item_treasure.wav",483,12,.4f,true),
    ITEM_HOURGLASS("item_hourglass.wav",1821,12,.4f,true),
    ITEM_TOADS("item_toads.wav",673,12,.4f,true),
    ITEM_LOSE_GOLD("item_lose_gold.wav",416,12,.4f,true),
    ITEM_HOSTILE_BOSS("item_hostile_boss.wav",640,12,.4f,true),
    ITEM_SHRINK("item_shrink.wav",3599,12,.4f,true),
    ITEM_CURSE("item_curse.wav",1093,12,.4f,true),
    ITEM_CLOSE_CALL("item_close_call.wav",569,4,.25f,true),
    ITEM_MERCHANT("item_merchant.wav",919,12,.4f,true),
    ITEM_SECOND_HEART("item_second_heart.wav",1541,45,.55f,true),
    ITEM_BLOOD_VIAL("item_blood_vial.wav",633,12,.35f,true),
    ITEM_TRAP_BUILD("item_trap_build.wav",681,12,.35f,true),
    ITEM_TRAP_KILL("item_trap_kill.wav",384,12,.35f,true),
    ITEM_TRAP_END("item_trap_end.wav",285,12,.4f,true),
    ITEM_CASH_OUT("item_cash_out.wav",4358,85,.5f),
    ITEM_LOOT("loot_pickup.ogg",846,4,.3f,true),
    HOVER("ui_hover.ogg",446,1,.17f),
    WEAPON_MERGE("weapon_merge.ogg",261,72,.65f),
    HEAL("heal.ogg",1110,3,.12f,true),
    PORTAL_OPEN("portal_open.wav",786,120,.8f,true),
    LOOT_PICKUP("loot_pickup.ogg",846,72,.45f),
    CHARGER_CHARGE("charger_charge.ogg",954,5,.2f,true),
    CHARGER_EXPLOSION("charger_explosion.wav",1563,7,.65f,true),
    PHANTOM_STEED("phantom_steed.mp3",2187,5,.5f,true),
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
    COW_ATTACK("cow_attack.wav",800,4,.6f,true),
    COW_DEATH("cow_death.wav",2600,9,.6f,true),
    IMP_DEATH("imp_death.wav",881,9,.6f,true),
    OATHBREAKER_ATTACK("oathbreaker_attack.wav",507,4,.6f,true),
    OATHBREAKER_DEATH("oathbreaker_death.wav",967,9,.6f,true),
    TOAD_DEATH("toad_death.wav",950,9,.6f,true),
    EAGLE_ATTACK("eagle_attack.wav",650,4,.6f,true),
    EAGLE_DEATH("eagle_death.wav",1000,9,.6f,true),
    WOLF_ATTACK("wolf_attack.wav",750,4,.6f,true),
    WOLF_DEATH("wolf_death.wav",800,9,.6f,true),
    BOAR_ATTACK("boar_attack.wav",558,4,.6f,true),
    BOAR_DEATH("boar_death.wav",795,9,.6f,true),
    CROCODILE_ATTACK("crocodile_attack.wav",550,4,.6f,true),
    CROCODILE_DEATH("crocodile_death.wav",1800,9,.6f,true),
    SWARM_ATTACK("swarm_attack.wav",400,4,.6f,true),
    SWARM_DEATH("swarm_death.wav",650,9,.6f,true),
    BEAR_ATTACK("bear_attack.wav",850,4,.6f,true),
    BEAR_DEATH("bear_death.wav",1800,9,.6f,true),
    GORILLA_ATTACK("gorilla_attack.wav",700,4,.6f,true),
    GORILLA_DEATH("gorilla_death.wav",2000,9,.6f,true),
    BOAR_CHARGE("boar_charge.wav",1150,5,.6f,true),
    BOAR_PREPARE("boar_prepare.wav",1000,5,.6f,true),
    CROCODILE_JUMP("crocodile_jump.wav",550,5,.6f,true),
    CROCODILE_PREPARE("crocodile_prepare.wav",2000,5,.6f,true),
    CROCODILE_EGG("crocodile_egg.wav",407,5,.6f,true),
    SWARM_RESIST("swarm_resist.wav",250,5,.6f,true),
    SWARM_SPLIT("swarm_split.wav",800,5,.6f,true),
    BEAR_RAGE("bear_rage.wav",1581,5,.6f,true),
    GORILLA_PREPARE("gorilla_prepare.wav",500,5,.6f,true),
    GORILLA_THROW("gorilla_throw.wav",308,5,.6f,true),
    BANDIT_DEATH("bandit_death.wav",430,9,.6f,true),
    ARCHER_DEATH("archer_death.wav",377,9,.6f,true),
    GUARD_DEATH("guard_death.wav",736,9,.6f,true),
    SPEARMAN_DEATH("spearman_death.wav",1600,9,.6f,true),
    SPEARMAN_THROW("spearman_throw.wav",200,5,.6f,true),
    HORSEMAN_DEATH("horseman_death.wav",1600,9,.6f,true),
    HORSEMAN_ATTACK("horseman_attack.wav",225,5,.6f,true),
    HORSEMAN_SWIPE("horseman_swipe.wav",364,5,.6f,true),
    MIMIC_TRANSFORM("mimic_transform.wav",850,5,.6f,true),
    WITCH_DEATH("witch_death.wav",1215,95,.6f,true),
    SKELETON_DEATH("skeleton_death.wav",529,9,.6f,true),
    ZOMBIE_ATTACK("zombie_attack.wav",650,4,.6f,true),
    ZOMBIE_DEATH("zombie_death.wav",1691,9,.6f,true),
    GHOST_ATTACK("ghost_attack.wav",1355,4,.6f,true),
    GHOST_DEATH("ghost_death.wav",1245,9,.6f,true),
    GHOST_STORM("ghost_storm.wav",3510,5,.6f,true),
    SLIME_DEATH("slime_death.wav",1348,9,.6f,true),
    SLIME_PATH("slime_path.wav",500,4,.6f,true),
    GORILLA_STOMP("gorilla_stomp.wav",1800,5,.6f,true),
    MIMIC_ATTACK("mimic_attack.wav",525,4,.6f,true),
    MIMIC_DEATH("mimic_death.wav",714,9,.6f,true),
    VAMPIRE_HUNGER("vampire_hunger.wav",1200,5,.6f,true),
    VAMPIRE_BITE("vampire_bite.wav",350,4,.6f,true),
    VAMPIRE_MIST("vampire_mist.wav",800,5,.6f,true),
    VAMPIRE_FORM("vampire_form.wav",3200,5,.6f,true),
    VAMPIRE_TELEPORT("vampire_teleport.wav",750,5,.6f,true),
    VAMPIRE_CONE_CHARGE("vampire_cone_charge.wav",1000,5,.6f,true),
    VAMPIRE_SWIPE_CHARGE("vampire_swipe_charge.wav",400,5,.6f,true),
    VAMPIRE_DEATH("vampire_death.wav",4511,95,.6f,true),
    VAMPIRE_HURT("vampire_hurt.wav",1003,30,.6f,true),
    SLING_SPLIT("sling_split.wav",320,4,.45f,true),
    BOOMERANG_CATCH("boomerang_catch.wav",180,4,.4f,true),
    SHIELD_READY("shield_ready.wav",180,5,.5f,true),
    BEAR_HURT("bear_hurt.wav",480,4,.6f,true),
    GORILLA_HURT("gorilla_hurt.wav",400,4,.6f,true),
    SPEARMAN_HURT("spearman_hurt.wav",360,4,.6f,true),
    HORSEMAN_HURT("horseman_hurt.wav",450,4,.6f,true),
    IMBERT_SELECT("voice_im27s01.wav",5141,75,0.75f),
    IMBERT_HURT_1("voice_im27h09.wav",629,40,0.65f,true),
    IMBERT_HURT_2("voice_im27h08.wav",829,40,0.65f,true),
    IMBERT_HURT_3("voice_im27h07.wav",664,40,0.65f,true),
    IMBERT_HURT_4("voice_im27h02.wav",649,40,0.65f,true),
    IMBERT_HURT_5("voice_im27h01.wav",819,40,0.65f,true),
    IMBERT_DEATH("voice_im27d10.wav",1083,110,0.75f,true),
    YWALETH_SELECT("voice_yw27s01.wav",3599,75,0.75f),
    YWALETH_HURT_1("voice_yw27h01.wav",629,40,0.65f,true),
    YWALETH_HURT_2("voice_yw27h06.wav",604,40,0.65f,true),
    YWALETH_HURT_3("voice_yw27h08.wav",599,40,0.65f,true),
    YWALETH_DEATH("voice_yw27d10.wav",684,110,0.75f,true),
    LINLOS_SELECT("voice_ln27s01.wav",7437,75,0.75f),
    LINLOS_HURT_1("voice_ln27h03.wav",729,40,0.65f,true),
    LINLOS_HURT_2("voice_ln27h01.wav",624,40,0.65f,true),
    LINLOS_HURT_3("voice_ln27h06.wav",658,40,0.65f,true),
    LINLOS_HURT_4("voice_ln27h07.wav",873,40,0.65f,true),
    LINLOS_HURT_5("voice_ln27h09.wav",598,40,0.65f,true),
    LINLOS_DEATH("voice_ln27d01.wav",1386,110,0.75f,true),
    KOBA_SELECT("voice_kb27s01.wav",8044,75,0.75f),
    KOBA_HURT_1("voice_kb27h05.wav",1233,40,0.65f,true),
    KOBA_HURT_2("voice_kb27h06.wav",918,40,0.65f,true),
    KOBA_HURT_3("voice_kb27h08.wav",1327,40,0.65f,true),
    KOBA_HURT_4("voice_kb27h09.wav",968,40,0.65f,true),
    KOBA_HURT_5("voice_kb27h01.wav",948,40,0.65f,true),
    KOBA_DEATH("voice_kb27d09.wav",1717,110,0.75f,true),
    LAVENA_SELECT("voice_lv27s01.wav",6245,75,0.75f),
    LAVENA_HURT_1("voice_lv27h04.wav",564,40,0.65f,true),
    LAVENA_HURT_2("voice_lv27h01.wav",659,40,0.65f,true),
    LAVENA_HURT_3("voice_lv27h08.wav",659,40,0.65f,true),
    LAVENA_DEATH("voice_lv27d10.wav",1008,110,0.75f,true),
    WITCH_HURT_1("voice_wi27h02.wav",804,30,0.6f,true),
    WITCH_HURT_2("voice_wi27h04.wav",922,30,0.6f,true),
    WITCH_HURT_3("voice_wi27h03.wav",819,30,0.6f,true),
    WITCH_HURT_4("voice_wi27h06.wav",785,30,0.6f,true),
    WITCH_HURT_5("voice_wi27h10.wav",1178,30,0.6f,true),
    WITCH_QUOTE_1("voice_wi27p01.wav",2710,25,0.6f,true),
    WITCH_QUOTE_2("voice_wi27p02.wav",2013,25,0.6f,true),
    WITCH_QUOTE_3("voice_wi27p03.wav",2209,25,0.6f,true),
    WITCH_QUOTE_4("voice_wi27p05.wav",2335,25,0.6f,true),
    WITCH_QUOTE_5("voice_wi27p07.wav",1614,25,0.6f,true),
    WITCH_QUOTE_6("voice_wi27p09.wav",2183,25,0.6f,true),
    VAMPIRE_HURT_2("voice_vl27h05.wav",1453,30,0.6f,true),
    VAMPIRE_HURT_3("voice_vl27h08.wav",1312,30,0.6f,true),
    VAMPIRE_HURT_4("voice_vl27h09.wav",1108,30,0.6f,true),
    VAMPIRE_QUOTE_1("voice_vl27p01.wav",2165,25,0.6f,true),
    VAMPIRE_QUOTE_2("voice_vl27p02.wav",1986,25,0.6f,true),
    VAMPIRE_QUOTE_3("voice_vl27p03.wav",1901,25,0.6f,true),
    VAMPIRE_QUOTE_4("voice_vl27p08.wav",902,25,0.6f,true),
    VAMPIRE_QUOTE_5("voice_vl27p06.wav",1591,25,0.6f,true),
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
data class AudioSource(val x:Float,val y:Float,val radius:Float=0f,
    val unscaled:Boolean=false,val audibleOffscreen:Boolean=false,val finalBossSpeech:Boolean=false,
    val criticalNotification:Boolean=false)
private val UiSound.isDeathCue:Boolean get()=name.endsWith("_DEATH")
internal fun UiSound.playbackGain(ability:Boolean=false):Float {
    val weaponCue=ordinal in UiSound.LONGSWORD_SWING.ordinal..UiSound.MAGIC_POISON.ordinal ||
        ordinal in UiSound.IGNITE_DASH.ordinal..UiSound.CAST_SINGULARITY.ordinal
    return gain*(if(combat&&(ability||priority in 6..8))1.35f else 1f)*(if(weaponCue)1.1f else 1f)
}
data class AudioView(val left:Float,val bottom:Float,val width:Float,val height:Float) {
    fun contains(s:AudioSource):Boolean {
        val dx=s.x-s.x.coerceIn(left,left+width);val dy=s.y-s.y.coerceIn(bottom,bottom+height)
        return dx*dx+dy*dy<=s.radius.coerceAtLeast(0f).let{it*it}
    }
}

data class AudioLevels(val master:Float,val music:Float,val effects:Float,val directionalStereo:Boolean=true) {
    val musicGain get() = (master*music).coerceIn(0f,1f)
    val effectsGain get() = (master*effects).coerceIn(0f,1f)
    companion object {
        fun saved()=AudioLevels(GameSettings.masterVolume,GameSettings.musicVolume,GameSettings.soundEffectsVolume,GameSettings.directionalStereo)
    }
}

/** One game-owned mixer. Menus never own/restart the shared streamed music. */
class AudioController(
    private val loadMusic:()->Music,
    private val loadSound:(UiSound)->Sound,
    private val levels:()->AudioLevels=AudioLevels::saved,
    private val millis:()->Long={System.nanoTime()/1_000_000L},
    private val seekSound:((Sound,Long,Float)->Boolean)?=null,
    private val loadRunMusic:((MusicTrack)->Music)?=null
):Disposable {
    private data class Event(val kind:UiSound,val source:AudioSource?,val ability:Boolean,val gainMultiplier:Float)
    private data class Voice(val kind:UiSound,val sound:Sound,val id:Long,var remaining:Double,val source:AudioSource?,val ability:Boolean,val gainMultiplier:Float)
    private val sounds=mutableMapOf<UiSound,Sound>()
    private val voices=mutableListOf<Voice>()
    private data class Channel(val kind:UiSound,val looping:Boolean,var source:AudioSource?,var sound:Sound?=null,var id:Long=-1L,var elapsedSeconds:Double=0.0,var gainMultiplier:Float=1f)
    private val channels=linkedMapOf<Any,Channel>()
    private class EffectOwner
    /** World one-shots survive ordinary attack transients, but never a stage boundary. */
    fun effect(kind:UiSound,source:AudioSource) {
        if(disposed || paused || menu || combatStopped)return
        sustain(EffectOwner(),kind,looping=false,source=source)
    }
    private var view:AudioView?=null
    private var playerSource:AudioSource?=null
    fun worldView(value:AudioView,player:AudioSource) {advanceVoices(millis());view=value;playerSource=player;syncVolumesAndMusic()}
    private fun visible(source:AudioSource?)=source==null || source.audibleOffscreen || view?.contains(source)!=false
    fun sourceAudible(source:AudioSource)=visible(source) && !menu && !paused && !combatStopped && effective().effectsGain>0f
    private fun speed(source:AudioSource?)=if(source?.unscaled==true)1f else combatSpeed
    fun enemyDelta(delta:Float)=delta/(combatSpeed.takeIf{it>0f}?:1f)
    private val retiredOwners=java.util.Collections.newSetFromMap(java.util.WeakHashMap<Any,Boolean>())
    // Restrained stereo spread; UI remains centered and visibility still controls audibility.
    private fun pan(source:AudioSource?):Float {
        val listener=playerSource ?: return 0f
        if(source==null || !effective().directionalStereo)return 0f
        // Fixed world-space falloff: moving the camera cannot move the listener or change stereo.
        return ((source.x-listener.x)/500f).coerceIn(-1f,1f)*.75f
    }
    private fun cueGain(kind:UiSound,ability:Boolean=false)=kind.playbackGain(ability)
    private fun mixMultiplier(value:Float,source:AudioSource?)=
        if(value.isFinite())value.coerceIn(0f,if(source?.audibleOffscreen==true)16f else 2f) else 0f
    private fun speechActive()=!combatStopped && !paused && channels.values.any {
        it.source?.finalBossSpeech==true && (it.looping || it.elapsedSeconds*1000<it.kind.durationMs)
    }
    private fun speechDuck(source:AudioSource?)=if(speechActive() && source?.finalBossSpeech!=true && source?.criticalNotification!=true).65f else 1f

    /** Owner-bound loops are separate from disposable attack transients, so attacks cannot cut a chant. */
    fun sustain(owner:Any,kind:UiSound?,looping:Boolean=true,source:AudioSource?=null,gainMultiplier:Float=1f) {
        if(disposed)return
        if(owner in retiredOwners) {if(kind==null)retiredOwners.remove(owner);return}
        advanceVoices(millis())
        if(kind==null || channels[owner]?.kind!=kind) stopSustain(owner)
        if(kind!=null && !menu) { require(kind.combat);channels.getOrPut(owner){Channel(kind,looping,source?:playerSource)}.also{if(source!=null)it.source=source;it.gainMultiplier=mixMultiplier(gainMultiplier,it.source)} }
        syncChannels()
    }
    fun stopSustain(owner:Any) {retiredOwners.remove(owner);channels.remove(owner)?.let{if(it.id>=0)it.sound?.stop(it.id)}}
    /** A stage/reward boundary discards effects, unlike a resumable pause. */
    fun stopStageEffects() {
        voices.forEach{it.sound.stop(it.id)};voices.clear()
        pending=null;pendingCombat.clear();lastCombatAt.clear()
        val owners=channels.keys.toList()
        owners.forEach(::stopSustain);retiredOwners.addAll(owners)
    }
    fun stopVoices(kinds:Set<UiSound>) {
        if(pending?.kind in kinds)pending=null
        kinds.forEach(pendingCombat::remove)
        voices.filter{it.kind in kinds}.forEach{it.sound.stop(it.id)}
        voices.removeAll{it.kind in kinds}
    }
    private fun syncChannels() {
        channels.entries.filter{it.key is EffectOwner && it.value.elapsedSeconds*1000>=it.value.kind.durationMs}
            .map{it.key}.forEach(::stopSustain)
        val gain=effective().effectsGain
        // Bound simultaneous textures. Multiple copies of the same spell share its mix budget.
        val selected=channels.entries.filter{visible(it.value.source)}
            .sortedWith(compareByDescending<Map.Entry<Any,Channel>>{it.value.source?.criticalNotification==true}
                .thenByDescending{it.value.kind.priority}).take(12).map{it.key}.toSet()
        val counts=channels.filterKeys{it in selected}.values.groupingBy{it.kind}.eachCount()
        channels.forEach{(owner,c)->
            val audible=gain>0f && owner in selected
            // Desktop virtualizes hidden sources and seeks to their continuing game-time phase.
            // Backends without seeking keep the source running silently (never pause/restart it).
            if(!audible && seekSound!=null) {if(c.id>=0)c.sound?.stop(c.id);c.id=-1L;return@forEach}
            if(!c.looping && c.elapsedSeconds*1000>=c.kind.durationMs) {if(c.id>=0)c.sound?.stop(c.id);c.id=-1L;return@forEach}
            val volume=if(audible)(gain*cueGain(c.kind)*c.gainMultiplier*speechDuck(c.source)/kotlin.math.sqrt((counts[c.kind]?:1).toFloat())).coerceAtMost(1f) else 0f
            if(c.id<0 && !paused && !combatStopped && c.kind !in failedSounds) {
                val sound=sounds[c.kind]?:runCatching{loadSound(c.kind)}.getOrElse{failedSounds+=c.kind;return@forEach}.also{sounds[c.kind]=it}
                val startVolume=if(seekSound!=null&&c.elapsedSeconds>0.001)0f else volume
                c.sound=sound;c.id=runCatching{if(c.looping)sound.loop(startVolume,speed(c.source),pan(c.source)) else sound.play(startVolume,speed(c.source),pan(c.source))}.getOrDefault(-1L)
                if(c.id>=0 && seekSound!=null && c.elapsedSeconds>0.001) {
                    val duration=c.kind.durationMs/1000.0
                    val offset=if(c.looping&&duration>0)c.elapsedSeconds%duration else c.elapsedSeconds
                    if(!runCatching{seekSound?.invoke(sound,c.id,offset.toFloat())==true}.getOrDefault(false)){
                        sound.stop(c.id);c.id=-1L;return@forEach
                    }
                }
            }
            if(c.id>=0) {c.sound?.setPan(c.id,pan(c.source),volume);c.sound?.setPitch(c.id,speed(c.source))}
        }
    }
    private var music:Music?=null
    private var musicStarted=false
    private var musicSuspended=false
    private fun pauseMusic() {if(music?.isPlaying==true){music?.pause();musicSuspended=true}}
    private var musicTrack:MusicTrack?=null
    private var runMusic:MusicTrack?=null
    private var runMusicGain=1f
    private val failedMusic=mutableSetOf<MusicTrack>()
    fun runMusic(track:MusicTrack?,gain:Float=1f) {
        if(disposed)return
        require(track!=MusicTrack.MENU)
        runMusic=track;runMusicGain=gain.coerceIn(0f,1f);syncVolumesAndMusic()
    }
    private var disposed=false
    private var paused=false
    private var menu=false
    private var pending:Event?=null
    private val pendingCombat=linkedMapOf<UiSound,Event>()
    private val lastCombatAt=mutableMapOf<UiSound,Long>()
    private var combatCursor=0
    private var combatSpeed=1f
    private var combatStopped=false
    private var allowDeathTails=false
    private var lastVoiceUpdate:Long?=null
    private var lastSoundAt=Long.MIN_VALUE/2
    private var lastHoverAt=Long.MIN_VALUE/2
    private var previewOwner:Any?=null
    private var preview:AudioLevels?=null
    private val failedSounds=mutableSetOf<UiSound>()
    val inMenus get()=menu
    fun realTimeMillis()=millis()

    private fun advanceVoices(now:Long) {
        val elapsed=(now-(lastVoiceUpdate?:now)).coerceAtLeast(0).toDouble()
        lastVoiceUpdate=now
        voices.forEach { it.remaining-=elapsed*if(it.kind.combat) {
            if(combatStopped && !(allowDeathTails && it.kind.isDeathCue))0.0 else speed(it.source).toDouble()
        } else 1.0 }
        voices.filter{it.remaining<=0.0}.forEach{it.sound.stop(it.id)}
        voices.removeAll{it.remaining<=0.0}
        if(!paused&&!combatStopped) channels.values.forEach{it.elapsedSeconds+=elapsed*speed(it.source)/1000.0}
    }
    fun combatPlayback(speed:Float,stopped:Boolean,deathTails:Boolean=false) {
        if(disposed)return
        advanceVoices(millis())
        combatSpeed=if(speed.isFinite())speed.coerceIn(.5f,2f) else 1f
        val wasStopped=combatStopped;val previouslyAllowedDeathTails=allowDeathTails
        combatStopped=stopped;allowDeathTails=deathTails
        voices.filter{it.kind.combat}.forEach {
            it.sound.setPitch(it.id,speed(it.source))
            val previouslyFrozen=wasStopped && !(previouslyAllowedDeathTails && it.kind.isDeathCue)
            val frozen=stopped && !(deathTails && it.kind.isDeathCue)
            if(frozen&&!previouslyFrozen)it.sound.pause(it.id)
            else if(!frozen&&previouslyFrozen)it.sound.resume(it.id)
        }
        if(stopped)pendingCombat.clear()
        channels.values.forEach{c->if(c.id>=0){c.sound?.setPitch(c.id,speed(c.source))
            if(stopped&&!wasStopped)c.sound?.pause(c.id) else if(!stopped&&wasStopped&&!paused)c.sound?.resume(c.id)}}
        syncChannels()
    }

    fun setMenuContext(value:Boolean) {
        if(disposed)return
        if(menu!=value && !value) music?.stop()
        menu=value
        if(value) {
            runMusic=null
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
    fun request(kind:UiSound,source:AudioSource?=null,ability:Boolean=false,gainMultiplier:Float=1f) {
        if(disposed||paused)return
        val eventSource=if(kind.combat)source?:playerSource else null
        val event=Event(kind,eventSource,ability,mixMultiplier(gainMultiplier,eventSource))
        if(kind.combat) {
            // A hidden Imp/weapon must not replace an audible shot sharing the same sample.
            val previous=pendingCombat[kind]
            if(!menu&&!combatStopped && visible(event.source) &&
                (previous?.ability!=true || ability) && (previous==null || event.gainMultiplier>=previous.gainMultiplier))pendingCombat[kind]=event
            return
        }
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
            // An actor's final voice must not lose its slot to this frame's shots.
            val deaths=combatEvents.filter{it.kind.isDeathCue}.sortedByDescending{it.kind.priority}.take(2)
            deaths.forEach{playEvent(it,now)}
            val others=combatEvents.filterNot{it.kind.isDeathCue}
            if(others.isNotEmpty()) {
                val start=combatCursor%others.size
                val slots=minOf(2-deaths.size,others.size)
                repeat(slots){playEvent(others[(start+it)%others.size],now)}
                combatCursor=(start+slots)%others.size
            }
        }
    }
    private fun playEvent(request:Event,now:Long) {
        val event=request.kind
        val volume=(effective().effectsGain*cueGain(event,request.ability)*request.gainMultiplier*speechDuck(request.source)).coerceAtMost(1f)
        if(!visible(request.source))return
        val previous=if(event==UiSound.HOVER)lastHoverAt else if(event.combat)lastCombatAt[event]?:Long.MIN_VALUE/2 else lastSoundAt
        if(volume<=0f || now-previous<(if(event.combat)80L/speed(request.source) else 80f))return
        if(event in failedSounds)return
        val sound=sounds[event] ?: runCatching {loadSound(event)}.getOrElse {
            failedSounds+=event;return
        }.also{sounds[event]=it}
        if(voices.size>=3) {
            val oldestLowPriority=voices.minBy{it.kind.priority}
            if(oldestLowPriority.kind.priority>event.priority)return
            voices.remove(oldestLowPriority);oldestLowPriority.sound.stop(oldestLowPriority.id)
        }
        val id=runCatching{sound.play(volume,if(event.combat)speed(request.source) else 1f,pan(request.source))}.getOrDefault(-1L)
        if(id>=0) {
            voices+=Voice(event,sound,id,event.durationMs.toDouble(),request.source,request.ability,request.gainMultiplier)
            if(event==UiSound.HOVER)lastHoverAt=now else if(event.combat)lastCombatAt[event]=now else lastSoundAt=now
        }
    }
    private fun effective()=preview?:levels()
    private fun syncVolumesAndMusic() {
        syncChannels()
        val current=effective()
        voices.forEach { voice ->
            val gain=if(visible(voice.source))(current.effectsGain*cueGain(voice.kind,voice.ability)*voice.gainMultiplier*speechDuck(voice.source)).coerceAtMost(1f) else 0f
            if(current.effectsGain<=0f)voice.sound.stop(voice.id) else voice.sound.setPan(voice.id,pan(voice.source),gain)
        }
        if(current.effectsGain<=0f) voices.clear()
        val wanted=if(menu)MusicTrack.MENU else runMusic
        if(wanted==null || paused) {pauseMusic();return}
        if(musicTrack!=wanted) {
            music?.stop();music?.dispose();music=null;musicTrack=wanted;musicStarted=false;musicSuspended=false
        }
        if(music==null && wanted !in failedMusic && current.musicGain>0f) {
            music=runCatching{
                (if(wanted==MusicTrack.MENU)loadMusic() else loadRunMusic?.invoke(wanted))
                    ?.also{it.isLooping=wanted.looping}
            }.getOrElse{failedMusic+=wanted;null}
        }
        music?.let {
            // A restrained starting mix; every value is still multiplied by Settings master/music.
            it.volume=current.musicGain*wanted.baseGain*(if(menu)1f else runMusicGain)*(if(speechActive()).45f else 1f)
            if(current.musicGain<=0f) pauseMusic()
            else if(!it.isPlaying && (!musicStarted || musicSuspended || wanted.looping)) {
                it.play();musicStarted=true;musicSuspended=false
            }
        }
    }
    fun pause() {
        if(disposed)return
        paused=true;pending=null;pendingCombat.clear();pauseMusic()
        voices.forEach{it.sound.stop(it.id)};voices.clear()
        channels.values.forEach{if(it.id>=0)it.sound?.pause(it.id)}
    }
    fun resume() {if(!disposed){paused=false;if(!combatStopped)channels.values.forEach{if(it.id>=0)it.sound?.resume(it.id)};syncVolumesAndMusic()}}
    override fun dispose() {
        if(disposed)return
        disposed=true;pending=null;pendingCombat.clear();lastCombatAt.clear();voices.clear();previewOwner=null;preview=null
        channels.clear();retiredOwners.clear()
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
            seekSound=seekHandler,
            loadRunMusic={Gdx.audio.newMusic(Gdx.files.internal("audio/music/${it.path}"))}
        )
    }
    fun play(kind:UiSound) {controller?.request(kind)}
    fun playAt(kind:UiSound,x:Float,y:Float,radius:Float=0f,gainMultiplier:Float=1f) {controller?.request(kind,AudioSource(x,y,radius),gainMultiplier=gainMultiplier)}
    fun effectAt(kind:UiSound,x:Float,y:Float,radius:Float=0f) {controller?.effect(kind,AudioSource(x,y,radius))}
    fun portalOpened(x:Float,y:Float) {controller?.effect(UiSound.PORTAL_OPEN,
        AudioSource(x,y,audibleOffscreen=true,criticalNotification=true))}
    fun enemyAt(kind:UiSound,source:AudioSource,gainMultiplier:Float=1f) {controller?.request(kind,source,gainMultiplier=gainMultiplier)}
    fun enemyDelta(delta:Float)=controller?.enemyDelta(delta)?:delta
    fun realTimeMillis()=controller?.realTimeMillis()?:System.nanoTime()/1_000_000L
    fun sourceAudible(source:AudioSource)=controller?.sourceAudible(source)?:false
    fun stopStageEffects() {controller?.stopStageEffects()}
    fun runMusic(track:MusicTrack?,gain:Float=1f) {controller?.runMusic(track,gain)}
    fun projectile(sound:ProjectileSound,x:Float?=null,y:Float?=null,ability:Boolean=false) {
        controller?.request(sound.cue,if(x!=null&&y!=null)AudioSource(x,y) else null,ability)
    }
    fun ability(kind:UiSound,x:Float?=null,y:Float?=null,radius:Float=0f) {controller?.request(kind,if(x!=null&&y!=null)AudioSource(x,y,radius) else null,true)}
    fun sustain(owner:Any,kind:UiSound?,looping:Boolean=true,source:AudioSource?=null,gainMultiplier:Float=1f) {controller?.sustain(owner,kind,looping,source,gainMultiplier)}
    fun worldView(left:Float,bottom:Float,width:Float,height:Float,playerX:Float,playerY:Float) {
        controller?.worldView(AudioView(left,bottom,width,height),AudioSource(playerX,playerY))
    }
    fun stopSustain(owner:Any) {controller?.stopSustain(owner)}
    fun stopVoices(kinds:Set<UiSound>) {controller?.stopVoices(kinds)}
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
    /** The user chose to retain the shared confirmation cue for glyph selection. */
    fun glyphChoice() {play(UiSound.CONFIRM)}
    fun castCue(type:com.azizoglu.grisilith.weapon.glyph.GlyphType)=when(type) {
        com.azizoglu.grisilith.weapon.glyph.GlyphType.CALL_LIGHTNING->UiSound.CAST_LIGHTNING
        com.azizoglu.grisilith.weapon.glyph.GlyphType.METEOR->UiSound.CAST_METEOR
        com.azizoglu.grisilith.weapon.glyph.GlyphType.TSUNAMI->UiSound.CAST_TSUNAMI
        com.azizoglu.grisilith.weapon.glyph.GlyphType.SINGULARITY->UiSound.CAST_SINGULARITY
    }
    fun magicalShot(type:com.azizoglu.grisilith.content.DamageType,x:Float?=null,y:Float?=null) {
        ProjectileSound.magical(type)?.let{projectile(it,x,y)}
    }

    fun combatPlayback(speed:Float,stopped:Boolean,deathTails:Boolean=false) {controller?.combatPlayback(speed,stopped,deathTails)}
    fun menuContext(value:Boolean) {controller?.setMenuContext(value)}
    fun inMenus()=controller?.inMenus?:false
    fun preview(owner:Any,master:Float,music:Float,effects:Float,directionalStereo:Boolean=GameSettings.directionalStereo) {controller?.preview(owner,AudioLevels(master,music,effects,directionalStereo))}
    fun clearPreview(owner:Any) {controller?.clearPreview(owner)}
    fun update() {controller?.update()}
    fun pause() {controller?.pause()}
    fun resume() {controller?.resume()}
    fun dispose() {controller?.dispose();controller=null}

    /** Semantic outcomes may override a button's default click during the same frame. */
    fun button(@Suppress("UNUSED_PARAMETER") text:String):UiSound = UiSound.CLICK
}
