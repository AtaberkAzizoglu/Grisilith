package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.DamageType

/** Hit-confirm audio is separate from telegraph/movement trait cues. */
object EnemyAudio {
    private var bossStage=false
    fun stageContext(type:com.azizoglu.grisilith.stage.StageType) {
        bossStage=type!=com.azizoglu.grisilith.stage.StageType.NORMAL
    }
    private fun smallDeath(enemy:Enemy,cue:UiSound)=cue.name.endsWith("_DEATH") &&
        enemy.type in setOf(EnemyType.NORMAL,EnemyType.UNIQUE)
    private fun deathMix()=if(bossStage).8f else .9f
    private fun isVocal(cue:UiSound)=cue.name.endsWith("_DEATH") ||
        "_HURT" in cue.name || "_QUOTE_" in cue.name
    private fun source(enemy:Enemy,cue:UiSound,x:Float=enemy.centerX,y:Float=enemy.centerY)=
        AudioSource(x,y,unscaled=isVocal(cue),audibleOffscreen=enemy.type==EnemyType.MINI_BOSS || enemy.type==EnemyType.FINAL_BOSS,
            finalBossSpeech=enemy.type==EnemyType.FINAL_BOSS && "_QUOTE_" in cue.name)
    private var lastSmallVocalAt:Long?=null
    private fun admitSmallVocal(enemy:Enemy,cue:UiSound):Boolean {
        if(!isVocal(cue) || enemy.type !in setOf(EnemyType.NORMAL,EnemyType.UNIQUE))return true
        if(!GameAudio.sourceAudible(source(enemy,cue)))return false
        val now=GameAudio.realTimeMillis()
        val interval=if(smallDeath(enemy,cue))kotlin.math.ceil(350.0/deathMix()).toLong() else 350L
        if(lastSmallVocalAt?.let{now>=it && now-it<interval}==true)return false
        lastSmallVocalAt=now // Global real-time budget: at most three small-enemy vocals/second.
        return true
    }
    private val channels=java.util.IdentityHashMap<Enemy,MutableSet<Any>>()
    private val hitLocks=java.util.IdentityHashMap<Enemy,MutableMap<UiSound,Float>>()
    private class BossVoiceState {
        val quoteOwner=Any()
        val hurtOwner=Any()
        var nextQuote=30f
        var quote:UiSound?=null
        var quoteRemaining=0f
        var hurt:UiSound?=null
        var hurtRemaining=0f
        var hurtCooldown=0f
    }
    private val bossVoices=java.util.IdentityHashMap<Enemy,BossVoiceState>()
    private fun quotes(enemy:Enemy)=when(enemy.kindId) {
        EnemyKindIds.WITCH -> CharacterVoices.witchQuotes
        EnemyKindIds.VAMPIRE_LORD -> CharacterVoices.vampireQuotes
        else -> emptyList()
    }
    private fun startQuote(enemy:Enemy,state:BossVoiceState) {
        val pool=quotes(enemy)
        if(pool.isEmpty())return
        val cue=CharacterVoices.pick(pool)
        state.quote=cue;state.quoteRemaining=cue.durationMs/1000f;state.nextQuote=30f
        GameAudio.stopSustain(state.quoteOwner)
        GameAudio.sustain(state.quoteOwner,cue,false,source(enemy,cue),gain(enemy,cue))
    }
    /** One initial line per real enemy instance; restoring/refreshing never duplicates it. */
    fun spawned(enemy:Enemy) {
        if(enemy.isDead || quotes(enemy).isEmpty() || bossVoices.containsKey(enemy))return
        val state=BossVoiceState();bossVoices[enemy]=state;startQuote(enemy,state)
    }
    private fun gain(enemy:Enemy,cue:UiSound):Float {
        val level=when {
            enemy.type==EnemyType.FINAL_BOSS && "_QUOTE_" in cue.name -> 1f
            enemy.type==EnemyType.MINI_BOSS || enemy.type==EnemyType.FINAL_BOSS -> .95f
            isVocal(cue) && enemy.type==EnemyType.UNIQUE -> .30f*1.12f
            isVocal(cue) -> .30f
            else -> .6f
        }
        return level*(if(smallDeath(enemy,cue))deathMix() else 1f)/cue.playbackGain()
    }
    fun play(enemy:Enemy,cue:UiSound,x:Float=enemy.centerX,y:Float=enemy.centerY) {
        if(!admitSmallVocal(enemy,cue))return
        GameAudio.enemyAt(cue,source(enemy,cue,x,y),gainMultiplier=gain(enemy,cue))
    }
    fun sustain(key:Any,enemy:Enemy,cue:UiSound?,looping:Boolean=false) {
        if(cue==null) {GameAudio.stopSustain(key);channels[enemy]?.remove(key);return}
        channels.getOrPut(enemy){mutableSetOf()}.add(key)
        GameAudio.sustain(key,cue,looping,source(enemy,cue),gain(enemy,cue))
    }
    fun once(key:Any,enemy:Enemy,cue:UiSound) {sustain(key,enemy,null);sustain(key,enemy,cue)}
    fun feedback(enemy:Enemy,cue:UiSound,interval:Float) {
        val locks=hitLocks.getOrPut(enemy){mutableMapOf()}
        if((locks[cue]?:0f)>0f)return
        locks[cue]=interval;play(enemy,cue)
    }
    fun update(enemy:Enemy,delta:Float) {
        val gameDt=delta.takeIf{it.isFinite()}?.coerceAtLeast(0f)?:0f
        val dt=GameAudio.enemyDelta(gameDt)
        hitLocks[enemy]?.entries?.removeAll{it.setValue(it.value-if(isVocal(it.key))dt else gameDt);it.value<=0f}
        if(enemy.isDead)return
        spawned(enemy)
        val state=bossVoices[enemy]?:return
        state.nextQuote-=dt;state.quoteRemaining-=dt;state.hurtRemaining-=dt
        state.hurtCooldown=(state.hurtCooldown-dt).coerceAtLeast(0f)
        if(state.nextQuote<=0f)startQuote(enemy,state)
        if(state.quoteRemaining>0f)state.quote?.let {
            GameAudio.sustain(state.quoteOwner,it,false,source(enemy,it),gain(enemy,it))
        } else GameAudio.stopSustain(state.quoteOwner)
        if(state.hurtRemaining>0f)state.hurt?.let {
            GameAudio.sustain(state.hurtOwner,it,false,source(enemy,it),gain(enemy,it))
        } else GameAudio.stopSustain(state.hurtOwner)
    }
    fun clear(enemy:Enemy) {
        channels.remove(enemy)?.forEach(GameAudio::stopSustain);hitLocks.remove(enemy)
        bossVoices.remove(enemy)?.let{GameAudio.stopSustain(it.quoteOwner);GameAudio.stopSustain(it.hurtOwner)}
    }
    fun attackCue(id:ContentId?):UiSound? = when(id) {
        EnemyKindIds.BALD_EAGLE -> UiSound.EAGLE_ATTACK
        EnemyKindIds.GRAY_WOLF -> UiSound.WOLF_ATTACK
        EnemyKindIds.BOAR -> UiSound.BOAR_ATTACK
        EnemyKindIds.CROCODILE -> UiSound.CROCODILE_ATTACK
        EnemyKindIds.BEE_SWARM -> UiSound.SWARM_ATTACK
        EnemyKindIds.GRIZZLY_BEAR -> UiSound.BEAR_ATTACK
        EnemyKindIds.SILVERBACK_GORILLA -> UiSound.GORILLA_ATTACK
        EnemyKindIds.BANDIT -> UiSound.LONGSWORD_SWING
        EnemyKindIds.ARCHER -> UiSound.BOW_SHOT
        EnemyKindIds.SPEARMAN -> UiSound.LANCE_THRUST
        EnemyKindIds.HORSEMAN -> UiSound.HORSEMAN_ATTACK
        EnemyKindIds.SKELETON_WARRIOR -> UiSound.LONGSWORD_SWING
        EnemyKindIds.SKELETON_ARCHER -> UiSound.BOW_SHOT
        EnemyKindIds.ZOMBIE -> UiSound.ZOMBIE_ATTACK
        EnemyKindIds.GHOST -> UiSound.GHOST_ATTACK
        EnemyKindIds.MIMIC -> UiSound.MIMIC_ATTACK
        EnemyKindIds.VAMPIRE_LORD -> UiSound.VAMPIRE_BITE
        else -> null
    }
    fun deathCue(id:ContentId?):UiSound? = when(id) {
        EnemyKindIds.BALD_EAGLE -> UiSound.EAGLE_DEATH
        EnemyKindIds.GRAY_WOLF -> UiSound.WOLF_DEATH
        EnemyKindIds.BOAR -> UiSound.BOAR_DEATH
        EnemyKindIds.CROCODILE -> UiSound.CROCODILE_DEATH
        EnemyKindIds.BEE_SWARM -> UiSound.SWARM_DEATH
        EnemyKindIds.GRIZZLY_BEAR -> UiSound.BEAR_DEATH
        EnemyKindIds.SILVERBACK_GORILLA -> UiSound.GORILLA_DEATH
        EnemyKindIds.BANDIT -> UiSound.BANDIT_DEATH
        EnemyKindIds.ARCHER -> UiSound.ARCHER_DEATH
        EnemyKindIds.SPEARMAN -> UiSound.SPEARMAN_DEATH
        EnemyKindIds.HORSEMAN -> UiSound.HORSEMAN_DEATH
        EnemyKindIds.WITCH -> UiSound.WITCH_DEATH
        EnemyKindIds.SKELETON_WARRIOR, EnemyKindIds.SKELETON_ARCHER -> UiSound.SKELETON_DEATH
        EnemyKindIds.ZOMBIE -> UiSound.ZOMBIE_DEATH
        EnemyKindIds.GHOST -> UiSound.GHOST_DEATH
        EnemyKindIds.SLIME -> UiSound.SLIME_DEATH
        EnemyKindIds.MIMIC -> UiSound.MIMIC_DEATH
        EnemyKindIds.VAMPIRE_LORD -> UiSound.VAMPIRE_DEATH
        else -> null
    }
    fun attack(enemy:Enemy,appliedDamage:Int,damageType:DamageType=enemy.audioAttackDamageType) {
        if(appliedDamage<=0)return
        // Storm owns a phase-bound crying channel, not a whisper per damage tick.
        if(enemy.kindId==EnemyKindIds.GHOST && enemy.audioAttackSourceName=="Ghost Storm")return
        if(enemy.kindId==EnemyKindIds.VAMPIRE_LORD && enemy.audioAttackSourceName=="Vampire Hunger") {
            feedback(enemy,UiSound.VAMPIRE_HUNGER,1.2f);return
        }
        val spellCue=if(enemy.kindId==EnemyKindIds.WITCH)when(enemy.audioAttackSourceName) {
            "Static Cloud" -> UiSound.STATIC_CLOUDS
            "Meteor" -> UiSound.GLYPH_METEOR
            "Tsunami" -> UiSound.GLYPH_TSUNAMI
            "Call Lightning" -> UiSound.GLYPH_LIGHTNING
            else -> null
        } else null
        val cue=spellCue ?: when {
            enemy.kindId==EnemyKindIds.SILVERBACK_GORILLA && enemy.audioAttackSourceName=="Stomp the Earth" -> UiSound.GORILLA_STOMP
            enemy.kindId==EnemyKindIds.VAMPIRE_LORD && enemy.audioAttackSourceName=="Vampire Longsword Cone" -> UiSound.LONGSWORD_SWING
            enemy.kindId==EnemyKindIds.VAMPIRE_LORD && enemy.audioAttackSourceName=="Swipe Around" -> UiSound.LANCE_THRUST
            enemy.kindId==EnemyKindIds.SPEARMAN && enemy.audioAttackSourceName=="Throw the Spear" -> UiSound.SPEARMAN_THROW
            enemy.kindId==EnemyKindIds.HORSEMAN && enemy.audioAttackSourceName=="Swipe Around" -> UiSound.HORSEMAN_SWIPE
            enemy.kindId==EnemyKindIds.WITCH -> ProjectileSound.magical(damageType)?.cue
            enemy.kindId==EnemyKindIds.SLIME && enemy.audioAttackSourceName in setOf("Slime Spit","Acid Projectile") -> ProjectileSound.magical(damageType)?.cue
            else -> attackCue(enemy.kindId)
        }
        cue ?: return
        if(spellCue!=null) {
            val locks=hitLocks.getOrPut(enemy){mutableMapOf()}
            if((locks[cue]?:0f)>0f)return
            locks[cue]=cue.durationMs/1000f
        }
        play(enemy,cue)
    }
    fun death(enemy:Enemy) {
        clear(enemy)
        deathCue(enemy.kindId)?.let{play(enemy,it)}
    }
    fun hurt(enemy:Enemy) {
        if(enemy.isDead || enemy.type !in setOf(EnemyType.MINI_BOSS,EnemyType.FINAL_BOSS))return
        val pool=when(enemy.kindId) {
            EnemyKindIds.WITCH -> CharacterVoices.witchHurt
            EnemyKindIds.VAMPIRE_LORD -> CharacterVoices.vampireHurt.takeUnless{enemy.isActiveSkillLocked}?:emptyList()
            else -> emptyList()
        }
        if(pool.isNotEmpty()) {
            spawned(enemy)
            val state=bossVoices[enemy]?:return
            // One lock for the WHOLE pool, not a separate lock for each random cut.
            if(state.hurtCooldown>0f)return
            val cue=CharacterVoices.pick(pool)
            state.hurt=cue;state.hurtRemaining=cue.durationMs/1000f
            state.hurtCooldown=maxOf(.8f,state.hurtRemaining+.1f)
            GameAudio.stopSustain(state.hurtOwner)
            GameAudio.sustain(state.hurtOwner,cue,false,source(enemy,cue),gain(enemy,cue))
            return
        }
        val cue=when(enemy.kindId) {
            EnemyKindIds.GRIZZLY_BEAR -> UiSound.BEAR_HURT
            EnemyKindIds.SILVERBACK_GORILLA -> UiSound.GORILLA_HURT
            EnemyKindIds.SPEARMAN -> UiSound.SPEARMAN_HURT
            EnemyKindIds.HORSEMAN -> UiSound.HORSEMAN_HURT
            // Swarm, Crocodile, Ghost and Slime deliberately have no hurt voice.
            else -> null
        }
        cue?.let{feedback(enemy,it,.8f)}
    }
    fun pathDamage(appliedDamage:Int,x:Float,y:Float,boss:Boolean=false) {
        if(appliedDamage>0)GameAudio.enemyAt(UiSound.SLIME_PATH,AudioSource(x,y,audibleOffscreen=boss),
            gainMultiplier=(if(boss).95f else .6f)/UiSound.SLIME_PATH.playbackGain())
    }
}
