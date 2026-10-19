package com.azizoglu.grisilith.audio

import com.azizoglu.grisilith.enemy.Enemy
import com.azizoglu.grisilith.enemy.EnemyType
import com.azizoglu.grisilith.enemy.definition.EnemyKindIds
import com.azizoglu.grisilith.content.ContentId
import com.azizoglu.grisilith.content.DamageType

/** Hit-confirm audio is separate from telegraph/movement trait cues. */
object EnemyAudio {
    private val channels=java.util.IdentityHashMap<Enemy,MutableSet<Any>>()
    private val hitLocks=java.util.IdentityHashMap<Enemy,MutableMap<UiSound,Float>>()
    private fun boost(enemy:Enemy)=if(enemy.type==EnemyType.MINI_BOSS || enemy.type==EnemyType.FINAL_BOSS)1.35f else 1f
    // Shared weapon samples retain their timbre but use a consistent enemy mix budget.
    // Leaves headroom so the full +35% boss boost is not flattened by the 1.0 volume cap.
    private fun gain(enemy:Enemy,cue:UiSound)=.6f/cue.playbackGain()*boost(enemy)
    fun play(enemy:Enemy,cue:UiSound,x:Float=enemy.centerX,y:Float=enemy.centerY) {
        GameAudio.playAt(cue,x,y,gainMultiplier=gain(enemy,cue))
    }
    fun sustain(key:Any,enemy:Enemy,cue:UiSound?,looping:Boolean=false) {
        if(cue==null) {GameAudio.stopSustain(key);channels[enemy]?.remove(key);return}
        channels.getOrPut(enemy){mutableSetOf()}.add(key)
        GameAudio.sustain(key,cue,looping,AudioSource(enemy.centerX,enemy.centerY),gain(enemy,cue))
    }
    fun once(key:Any,enemy:Enemy,cue:UiSound) {sustain(key,enemy,null);sustain(key,enemy,cue)}
    fun feedback(enemy:Enemy,cue:UiSound,interval:Float) {
        val locks=hitLocks.getOrPut(enemy){mutableMapOf()}
        if((locks[cue]?:0f)>0f)return
        locks[cue]=interval;play(enemy,cue)
    }
    fun update(enemy:Enemy,delta:Float) {hitLocks[enemy]?.entries?.removeAll{it.setValue(it.value-delta);it.value<=0f}}
    fun clear(enemy:Enemy) {channels.remove(enemy)?.forEach(GameAudio::stopSustain);hitLocks.remove(enemy)}
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
        val cue=when(enemy.kindId) {
            EnemyKindIds.GRIZZLY_BEAR -> UiSound.BEAR_HURT
            EnemyKindIds.SILVERBACK_GORILLA -> UiSound.GORILLA_HURT
            EnemyKindIds.SPEARMAN -> UiSound.SPEARMAN_HURT
            EnemyKindIds.HORSEMAN -> UiSound.HORSEMAN_HURT
            EnemyKindIds.VAMPIRE_LORD -> UiSound.VAMPIRE_HURT.takeUnless{enemy.isActiveSkillLocked}
            // Swarm, Crocodile, Witch, Ghost and Slime deliberately have no hurt voice.
            else -> null
        }
        cue?.let{feedback(enemy,it,if(it==UiSound.VAMPIRE_HURT).7f else .8f)}
    }
    fun pathDamage(appliedDamage:Int,x:Float,y:Float,boss:Boolean=false) {
        if(appliedDamage>0)GameAudio.playAt(UiSound.SLIME_PATH,x,y,
            gainMultiplier=.6f/UiSound.SLIME_PATH.playbackGain()*(if(boss)1.35f else 1f))
    }
}
